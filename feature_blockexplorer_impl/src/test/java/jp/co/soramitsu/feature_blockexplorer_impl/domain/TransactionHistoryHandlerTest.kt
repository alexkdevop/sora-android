/*
This file is part of the SORA network and Polkaswap app.

Copyright (c) 2020, 2021, Polka Biome Ltd. All rights reserved.
SPDX-License-Identifier: BSD-4-Clause

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or other
materials provided with the distribution.

All advertising materials mentioning features or use of this software must display
the following acknowledgement: This product includes software developed by Polka Biome
Ltd., SORA, and Polkaswap.

Neither the name of the Polka Biome Ltd. nor the names of its contributors may be used
to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Polka Biome Ltd. AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Polka Biome Ltd. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package jp.co.soramitsu.feature_blockexplorer_impl.domain

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import jp.co.soramitsu.common.date.DateTimeFormatter
import jp.co.soramitsu.common.domain.CoroutineManager
import jp.co.soramitsu.common.domain.Token
import jp.co.soramitsu.common.domain.iconUri
import jp.co.soramitsu.common.resourses.LanguagesHolder
import jp.co.soramitsu.common.resourses.ResourceManager
import jp.co.soramitsu.common.util.NumbersFormatter
import jp.co.soramitsu.common.util.ext.safeCast
import jp.co.soramitsu.common.util.ext.unsafeCast
import jp.co.soramitsu.feature_account_api.domain.interfaces.UserRepository
import jp.co.soramitsu.feature_assets_api.data.interfaces.AssetsRepository
import jp.co.soramitsu.feature_blockexplorer_api.data.TransactionHistoryRepository
import jp.co.soramitsu.feature_blockexplorer_api.domain.HistoryState
import jp.co.soramitsu.feature_blockexplorer_api.domain.TransactionHistoryHandler
import jp.co.soramitsu.feature_blockexplorer_api.presentation.txhistory.EventUiModel
import jp.co.soramitsu.feature_blockexplorer_api.presentation.txhistory.TransactionMappers
import jp.co.soramitsu.feature_blockexplorer_api.presentation.txhistory.TransactionStatus
import jp.co.soramitsu.feature_blockexplorer_api.presentation.txhistory.TransactionsInfo
import jp.co.soramitsu.feature_blockexplorer_impl.presentation.txhistory.TransactionMappersImpl
import jp.co.soramitsu.feature_blockexplorer_impl.testdata.TestTransactions
import jp.co.soramitsu.test_data.TestAccounts
import jp.co.soramitsu.test_data.TestTokens
import jp.co.soramitsu.test_shared.MainCoroutineRule
import jp.co.soramitsu.test_shared.test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.util.Locale

@ExperimentalCoroutinesApi
class TransactionHistoryHandlerTest {

    @Rule
    @JvmField
    val rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var assetsRepository: AssetsRepository

    @MockK
    private lateinit var transactionHistoryRepository: TransactionHistoryRepository

    @MockK
    private lateinit var resourceManager: ResourceManager

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var language: LanguagesHolder

    @MockK
    private lateinit var coroutineManager: CoroutineManager

    @MockK
    private lateinit var coroutineScope: CoroutineScope

    @MockK
    private lateinit var dateTimeFormatter: DateTimeFormatter

    private val txMapper: TransactionMappers by lazy {
        TransactionMappersImpl(resourceManager, NumbersFormatter(), dateTimeFormatter)
    }

    private val mockedUri = mockk<Uri>()

    private val tokens = listOf(TestTokens.xorToken)
    private val txHash = "txHash"

    private val transactionsWithHeaders = listOf(
        EventUiModel.EventTimeSeparatorUiModel(title = "01 Feb 1970"),
        EventUiModel.EventTxUiModel.EventTransferOutUiModel(
            "txHash",
            mockedUri,
            "cnRuoXdU9t5bv5EQAiXT2gQozAvrVawqZkT2AQS1Msr8T8ZZu",
            "11:58",
            1673918013,
            "10 VAL",
            "~0",
            TransactionStatus.COMMITTED,
        ),
        EventUiModel.EventTxUiModel.EventTransferOutUiModel(
            "txHash2",
            mockedUri,
            "cnRuoXdU9t5bv5EQAiXT2gQozAvrVawqZkT2AQS1Msr8T8ZZu",
            "13:38",
            1679918013,
            "1 VAL",
            "~0",
            TransactionStatus.REJECTED,
        )
    )

    private lateinit var transactionHistoryHandler: TransactionHistoryHandler

    @Before
    fun setUp() = runTest {
        mockkStatic(Uri::parse)
        every { Uri.parse(any()) } returns mockedUri
        mockkStatic(Token::iconUri)
        every { TestTokens.xorToken.iconUri() } returns mockedUri
        every { TestTokens.valToken.iconUri() } returns mockedUri
        every { language.getCurrentLocale() } returns Locale.ENGLISH
        every { dateTimeFormatter.formatTimeWithoutSeconds(any()) } returns "01 Feb 1970"
        every { dateTimeFormatter.dateToDayWithoutCurrentYear(any(), any(), any()) } returns "01 Feb 1970"
        every { resourceManager.getString(any()) } returns ""
        every { transactionHistoryRepository.state } returns flowOf(true)
        every { coroutineManager.applicationScope } returns coroutineScope
        every { coroutineScope.coroutineContext } returns coroutineContext
        every { transactionHistoryRepository.onSoraAccountChange() } returns Unit
        coEvery { assetsRepository.tokensList() } returns tokens
        every { userRepository.flowCurSoraAccount() } returns flow {
            emit(TestAccounts.soraAccount)
            emit(TestAccounts.soraAccount2)
        }
        coEvery {
            transactionHistoryRepository.getTransaction(txHash, tokens, TestAccounts.soraAccount)
        } returns TestTransactions.sendSuccessfulTx
        coEvery { userRepository.getCurSoraAccount() } returns TestAccounts.soraAccount
        coEvery {
            transactionHistoryRepository.getTransactionHistory(any(), any(), any(), any())
        } returns TransactionsInfo(
            listOf(TestTransactions.sendSuccessfulTx),
            true
        )
        coEvery {
            transactionHistoryRepository.getLastTransactions(
                TestAccounts.soraAccount,
                listOf(TestTokens.xorToken),
                1,
                null
            )
        } returns listOf(TestTransactions.sendFailedTx)

        transactionHistoryHandler = TransactionHistoryHandlerImpl(
            assetsRepository,
            txMapper,
            transactionHistoryRepository,
            resourceManager,
            userRepository,
            dateTimeFormatter,
            coroutineManager
        )
    }

    @Test
    fun `cache plus new tx`() = runTest {
        val curTime = transactionHistoryHandler.getCachedEvents(1).getOrNull(0)
            ?.safeCast<EventUiModel.EventTxUiModel>()?.timestamp!!
        assertEquals(1679918013, curTime)
        transactionHistoryHandler.historyState.test(this) {
            transactionHistoryHandler.refreshHistoryEvents()
            var flowValue = this.awaitValue(0)
            assertTrue(flowValue is HistoryState.Loading)
            flowValue = this.awaitValue(1)
            assertTrue(flowValue is HistoryState.History)
            assertEquals(2, flowValue.unsafeCast<HistoryState.History>().events.size)
            this.finishAssertion()
        }
    }

    @Test
    fun `has new tx`() = runTest {
        advanceUntilIdle()
        val new = transactionHistoryHandler.hasNewTransaction()
        assertTrue(new)
    }

    @Test
    fun `init successful`() = runTest {
        verify { transactionHistoryRepository.onSoraAccountChange() }
    }

    @Test
    fun `flowLocalTransactions() called`() = runTest {
        transactionHistoryHandler.flowLocalTransactions().collectLatest {
            assertTrue(it)
        }
    }

    @Test
    fun `onMoreHistoryEventsRequested() called`() = runTest {
        advanceUntilIdle()
        val state = HistoryState.History(
            true,
            transactionsWithHeaders.subList(0, 2)
        )

        transactionHistoryHandler.historyState.test(
            this
        ) {
            transactionHistoryHandler.refreshHistoryEvents()
            advanceUntilIdle()
            var flowValue = this.awaitValue(0)
            assertEquals(flowValue, HistoryState.Loading)
            transactionHistoryHandler.onMoreHistoryEventsRequested()
            advanceUntilIdle()
            flowValue = this.awaitValue(1).unsafeCast<HistoryState.History>()
            assertEquals(flowValue.endReached, state.endReached)
            assertEquals(flowValue.pullToRefresh, state.pullToRefresh)
            assertEquals(flowValue.hasErrorLoadingNew, state.hasErrorLoadingNew)
            assertEquals(flowValue.events.size, state.events.size)
            assertEquals(
                flowValue.events[0].unsafeCast<EventUiModel.EventTimeSeparatorUiModel>().title,
                state.events[0].unsafeCast<EventUiModel.EventTimeSeparatorUiModel>().title,
            )
            assertEquals(
                flowValue.events[1].unsafeCast<EventUiModel.EventTxUiModel.EventTransferOutUiModel>().txHash,
                state.events[1].unsafeCast<EventUiModel.EventTxUiModel.EventTransferOutUiModel>().txHash,
            )
            this.finishAssertion()
        }
    }

    @Test
    fun `getCachedEvents() called`() = runTest {
        val result = transactionHistoryHandler.getCachedEvents(1)
        val a =
            transactionsWithHeaders[2].unsafeCast<EventUiModel.EventTxUiModel.EventTransferOutUiModel>()
        val b = result[0].unsafeCast<EventUiModel.EventTxUiModel.EventTransferOutUiModel>()

        assertEquals(a.txHash, b.txHash)
        assertEquals(a.timestamp, b.timestamp)
        assertEquals(a.status, b.status)
    }

    @Test
    fun `getTransaction() called`() = runTest {
        assertEquals(
            TestTransactions.sendSuccessfulTx,
            transactionHistoryHandler.getTransaction(txHash)
        )
    }
}
