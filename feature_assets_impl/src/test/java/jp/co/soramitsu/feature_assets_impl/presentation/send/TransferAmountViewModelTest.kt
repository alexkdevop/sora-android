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

package jp.co.soramitsu.feature_assets_impl.presentation.send

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.account.AccountAvatarGenerator
import jp.co.soramitsu.common.domain.Token
import jp.co.soramitsu.common.domain.iconUri
import jp.co.soramitsu.common.resourses.ClipboardManager
import jp.co.soramitsu.common.resourses.ResourceManager
import jp.co.soramitsu.common.util.NumbersFormatter
import jp.co.soramitsu.common.util.ext.equalTo
import jp.co.soramitsu.feature_assets_api.domain.interfaces.AssetsInteractor
import jp.co.soramitsu.feature_assets_api.presentation.launcher.AssetsRouter
import jp.co.soramitsu.feature_assets_impl.presentation.screens.send.TransferAmountViewModel
import jp.co.soramitsu.feature_wallet_api.launcher.WalletRouter
import jp.co.soramitsu.test_data.PolkaswapTestData
import jp.co.soramitsu.test_data.TestAssets
import jp.co.soramitsu.test_data.TestTokens
import jp.co.soramitsu.test_shared.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.math.BigDecimal

@ExperimentalCoroutinesApi
class TransferAmountViewModelTest {

    @Rule
    @JvmField
    val rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var assetsInteractor: AssetsInteractor

    @MockK
    private lateinit var walletRouter: WalletRouter

    @MockK
    private lateinit var assetsRouter: AssetsRouter

    @MockK
    private lateinit var drawable: PictureDrawable

    @MockK
    private lateinit var resourceManager: ResourceManager

    @MockK
    private lateinit var avatarGenerator: AccountAvatarGenerator

    private val mockedUri = mockk<Uri>()

    @MockK
    private lateinit var clipboardManager: ClipboardManager

    private val recipientId =
        "recipientIdrecipientIdrecipientIdrecipientIdrecipientIdrecipientIdrecipientId"
    private val recipientFullName = "recipientFull Name"
    private val networkFee = 0.07.toBigDecimal()
    private lateinit var transferAmountViewModel: TransferAmountViewModel

    @Before
    fun setUp() = runTest {
        mockkStatic(Uri::parse)
        every { Uri.parse(any()) } returns mockedUri
        mockkStatic(Token::iconUri)
        every { TestTokens.xorToken.iconUri() } returns mockedUri
        every { TestTokens.valToken.iconUri() } returns mockedUri
        every { resourceManager.getString(R.string.error_transaction_fee_title) } returns "Not enough funds"
        every { avatarGenerator.createAvatar(any(), any()) } returns drawable
        coEvery {
            assetsInteractor.isEnoughXorLeftAfterTransaction(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns false
        coEvery {
            assetsInteractor.calcTransactionFee(
                recipientId,
                TestTokens.xorToken,
                BigDecimal.ONE
            )
        } returns networkFee
    }

    @Test
    fun `initialized correctly`() = runTest {
        initViewModel(BigDecimal.ZERO)
        advanceUntilIdle()
        val state = transferAmountViewModel.sendState
        assertEquals(
            "recipientIdrecipientIdrecipientIdrecipientIdrecipientIdrecipientIdrecipientId",
            state.address
        )
    }

    @Test
    fun `select percent option with balance more than fee EXPECT amount value is set`() = runTest {
        initViewModel(BigDecimal.valueOf(4))
        advanceUntilIdle()
        transferAmountViewModel.optionSelected(75)
        advanceUntilIdle()
        val state = transferAmountViewModel.sendState
        assertTrue(state.input?.amount?.equalTo(BigDecimal.valueOf(3)) == true)
    }

    @Test
    fun `select percent option with balance less than fee EXPECT amount value is set`() = runTest {
        initViewModel(0.0001.toBigDecimal())
        advanceUntilIdle()
        transferAmountViewModel.optionSelected(75)
        advanceUntilIdle()
        val state = transferAmountViewModel.sendState
        assertTrue(state.input?.amount?.equalTo(BigDecimal.valueOf(0.000075)) == true)
    }

    private fun initViewModel(
        balance: BigDecimal,
        firstTokenId: String? = null,
        initialSendAmount: String? = null
    ) {
        every { assetsInteractor.subscribeAssetsActiveOfCurAccount() } returns flowOf(
            listOf(
                TestAssets.xorAsset(balance),
                TestAssets.valAsset(balance),
                TestAssets.pswapAsset(balance),
            )
        )
        transferAmountViewModel = TransferAmountViewModel(
            interactor = assetsInteractor,
            walletRouter = walletRouter,
            assetsRouter = assetsRouter,
            numbersFormatter = NumbersFormatter(),
            clipboardManager = clipboardManager,
            resourceManager = resourceManager,
            avatarGenerator = avatarGenerator,
            recipientId = recipientId,
            assetId = firstTokenId ?: TestTokens.xorToken.id,
            initialSendAmount = initialSendAmount
        )
    }

    @Test
    fun `WHEN user enters amount starting with XOR EXPECT transaction reminder is checked`() =
        runTest {
            initViewModel(
                BigDecimal.ZERO,
                firstTokenId = PolkaswapTestData.XOR_ASSET.token.id
            )
            advanceUntilIdle()
            transferAmountViewModel.amountChanged(BigDecimal.ONE)
            advanceUntilIdle()
            coVerify(atMost = 1) {
                assetsInteractor.isEnoughXorLeftAfterTransaction(
                    primaryToken = PolkaswapTestData.XOR_ASSET.token,
                    primaryTokenAmount = BigDecimal.ONE,
                    secondaryToken = null,
                    secondaryTokenAmount = null,
                    networkFeeInXor = networkFee
                )
            }
            transferAmountViewModel.onTokenChange(PolkaswapTestData.VAL_ASSET.token.id)
            advanceUntilIdle()
            transferAmountViewModel.amountChanged(BigDecimal.TEN)
            advanceUntilIdle()
            coVerify(atMost = 1) {
                assetsInteractor.isEnoughXorLeftAfterTransaction(
                    primaryToken = PolkaswapTestData.VAL_ASSET.token,
                    primaryTokenAmount = BigDecimal.TEN,
                    secondaryToken = null,
                    secondaryTokenAmount = null,
                    networkFeeInXor = networkFee
                )
            }
        }

    @Test
    fun `WHEN user enters amount starting without XOR EXPECT transaction reminder is checked`() =
        runTest {
            initViewModel(
                BigDecimal.ZERO,
                firstTokenId = TestAssets.pswapAsset().token.id
            )
            advanceUntilIdle()
            transferAmountViewModel.amountChanged(BigDecimal.ONE)
            advanceUntilIdle()
            coVerify(atMost = 1) {
                assetsInteractor.isEnoughXorLeftAfterTransaction(
                    primaryToken = any(),
                    primaryTokenAmount = any(),
                    secondaryToken = any(),
                    secondaryTokenAmount = any(),
                    networkFeeInXor = any()
                )
            }
            transferAmountViewModel.onTokenChange(PolkaswapTestData.XOR_ASSET.token.id)
            advanceUntilIdle()
            transferAmountViewModel.amountChanged(BigDecimal.TEN)
            advanceUntilIdle()
            coVerify(atMost = 1) {
                assetsInteractor.isEnoughXorLeftAfterTransaction(
                    primaryToken = PolkaswapTestData.XOR_ASSET.token,
                    primaryTokenAmount = BigDecimal.TEN,
                    secondaryToken = null,
                    secondaryTokenAmount = null,
                    networkFeeInXor = networkFee
                )
            }
        }
}
