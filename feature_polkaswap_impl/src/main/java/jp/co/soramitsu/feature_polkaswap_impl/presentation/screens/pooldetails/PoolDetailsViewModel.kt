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

package jp.co.soramitsu.feature_polkaswap_impl.presentation.screens.pooldetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.domain.AssetHolder
import jp.co.soramitsu.common.domain.DEFAULT_ICON_URI
import jp.co.soramitsu.common.domain.iconUri
import jp.co.soramitsu.common.presentation.viewmodel.BaseViewModel
import jp.co.soramitsu.common.util.NumbersFormatter
import jp.co.soramitsu.common.util.ext.lazyAsync
import jp.co.soramitsu.common_wallet.presentation.compose.util.PolkaswapFormulas
import jp.co.soramitsu.feature_polkaswap_api.domain.interfaces.PoolsInteractor
import jp.co.soramitsu.feature_polkaswap_api.launcher.PolkaswapRouter
import jp.co.soramitsu.feature_polkaswap_impl.domain.DemeterFarmingInteractor
import jp.co.soramitsu.feature_polkaswap_impl.presentation.states.PoolDetailsDemeterState
import jp.co.soramitsu.feature_polkaswap_impl.presentation.states.PoolDetailsState
import jp.co.soramitsu.ui_core.component.toolbar.Action
import jp.co.soramitsu.ui_core.component.toolbar.BasicToolbarState
import jp.co.soramitsu.ui_core.component.toolbar.SoramitsuToolbarState
import jp.co.soramitsu.ui_core.component.toolbar.SoramitsuToolbarType
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PoolDetailsViewModel @AssistedInject constructor(
    private val poolsInteractor: PoolsInteractor,
    private val numbersFormatter: NumbersFormatter,
    private val polkaswapRouter: PolkaswapRouter,
    private val demeterFarmingInteractor: DemeterFarmingInteractor,
    @Assisted("id1") private val token1Id: String,
    @Assisted("id2") private val token2Id: String,
) : BaseViewModel() {

    @AssistedFactory
    interface AssistedPoolDetailsViewModelFactory {
        fun create(@Assisted("id1") id1: String, @Assisted("id2") id2: String): PoolDetailsViewModel
    }

    internal var detailsState by mutableStateOf(
        PoolDetailsState(
            DEFAULT_ICON_URI, DEFAULT_ICON_URI, DEFAULT_ICON_URI,
            "", "", "", "", "", "",
            true, true, "", emptyList(), false,
        )
    )

    private val rewardTokenAsync by viewModelScope.lazyAsync { poolsInteractor.getRewardToken() }
    private suspend fun rewardToken() = rewardTokenAsync.await()

    init {
        _toolbarState.value = SoramitsuToolbarState(
            type = SoramitsuToolbarType.SmallCentered(),
            basic = BasicToolbarState(
                title = R.string.pool_details,
                navIcon = null,
                menu = listOf(Action.Close()),
            ),
        )
        viewModelScope.launch {
            poolsInteractor.subscribePoolCacheOfCurAccount(token1Id, token2Id)
                .catch { onError(it) }
                .collectLatest { data ->
                    if (data == null) {
                        detailsState = detailsState.copy(
                            addEnabled = false,
                            removeEnabled = false,
                        )
                    } else {
                        val userData = data.user
                        val pools = demeterFarmingInteractor.getFarmedPools()?.filter { pool ->
                            pool.tokenBase.id == token1Id && pool.tokenTarget.id == token2Id
                        }?.map { farming ->
                            PoolDetailsDemeterState(
                                rewardsUri = farming.tokenReward.iconUri(),
                                rewardsTokenSymbol = farming.tokenReward.symbol,
                                percent = if (userData != null) {
                                    PolkaswapFormulas.calculateShareOfPoolFromAmount(
                                        farming.amount,
                                        userData.poolProvidersBalance,
                                    ).toFloat()
                                } else {
                                    0.0f
                                },
                            )
                        }
                        val pools100 = pools?.any {
                            it.percent == 100.0f
                        }
                        detailsState = PoolDetailsState(
                            token1Icon = data.basic.baseToken.iconUri(),
                            token2Icon = data.basic.targetToken.iconUri(),
                            rewardsUri = rewardToken().iconUri(),
                            rewardsTokenSymbol = rewardToken().symbol,
                            symbol1 = data.basic.baseToken.symbol,
                            symbol2 = data.basic.targetToken.symbol,
                            apy = data.basic.sbapy?.let { apy ->
                                "%s%%".format(
                                    numbersFormatter.format(
                                        apy,
                                        2,
                                    )
                                )
                            } ?: "",
                            pooled1 = userData?.basePooled?.let {
                                data.basic.baseToken.printBalance(
                                    it,
                                    numbersFormatter,
                                    AssetHolder.ROUNDING,
                                )
                            },
                            pooled2 = userData?.targetPooled?.let {
                                data.basic.targetToken.printBalance(
                                    it,
                                    numbersFormatter,
                                    AssetHolder.ROUNDING,
                                )
                            },
                            addEnabled = true,
                            removeEnabled = (userData != null) && ((pools100 == null) || (pools100 == false)),
                            userPoolSharePercent = userData?.poolShare?.let {
                                "%s%%".format(
                                    numbersFormatter.format(it, 2, true)
                                )
                            },
                            demeterPools = pools,
                            demeter100Percent = pools100 == true,
                        )
                    }
                }
        }
    }

    override fun onMenuItem(action: Action) {
        this.onBackPressed()
    }

    fun onSupply() {
        polkaswapRouter.showAddLiquidity(token1Id, token2Id)
    }

    fun onRemove() {
        polkaswapRouter.showRemoveLiquidity(token1Id to token2Id)
    }
}
