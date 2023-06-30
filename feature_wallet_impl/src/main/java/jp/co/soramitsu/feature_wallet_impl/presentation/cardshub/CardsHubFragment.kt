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

package jp.co.soramitsu.feature_wallet_impl.presentation.cardshub

import android.os.Bundle
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import dagger.hilt.android.AndroidEntryPoint
import jp.co.soramitsu.common.base.SoraBaseFragment
import jp.co.soramitsu.common.base.theOnlyRoute
import jp.co.soramitsu.common.domain.BottomBarController
import jp.co.soramitsu.common_wallet.presentation.compose.components.PoolsList
import jp.co.soramitsu.common_wallet.presentation.compose.states.BuyXorState
import jp.co.soramitsu.common_wallet.presentation.compose.states.FavoriteAssetsCardState
import jp.co.soramitsu.common_wallet.presentation.compose.states.FavoritePoolsCardState
import jp.co.soramitsu.common_wallet.presentation.compose.states.SoraCardState
import jp.co.soramitsu.common_wallet.presentation.compose.states.TitledAmountCardState
import jp.co.soramitsu.oauth.base.sdk.contract.SoraCardResult
import jp.co.soramitsu.oauth.base.sdk.signin.SoraCardSignInContract
import jp.co.soramitsu.ui_core.resources.Dimens

@AndroidEntryPoint
class CardsHubFragment : SoraBaseFragment<CardsHubViewModel>() {

    override val viewModel: CardsHubViewModel by viewModels()

    private val soraCardSignIn = registerForActivityResult(
        SoraCardSignInContract()
    ) { result ->
        when (result) {
            is SoraCardResult.Failure -> {
            }
            is SoraCardResult.Success -> {
                viewModel.updateSoraCardInfo(
                    result.accessToken,
                    result.refreshToken,
                    result.accessTokenExpirationTime,
                    result.status.toString(),
                )
            }
            is SoraCardResult.NavigateTo -> {
            }

            SoraCardResult.Canceled -> {}
            SoraCardResult.Logout -> {}
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as BottomBarController).showBottomBar()
        viewModel.launchSoraCardSignIn.observe { contractData ->
            soraCardSignIn.launch(contractData)
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun NavGraphBuilder.content(
        scrollState: ScrollState,
        navController: NavHostController
    ) {
        composable(
            route = theOnlyRoute,
        ) {
            var qrSelection by remember { mutableStateOf(false) }
            val onQrClick: () -> Unit = {
                qrSelection = true
            }
            if (qrSelection) {
                viewModel.openQrCodeFlow()
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val state = viewModel.state
                TopBar(
                    account = state.curAccount,
                    onAccountClick = viewModel::onAccountClick,
                    onQrClick = onQrClick,
                )
                Spacer(modifier = Modifier.size(size = 16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = Dimens.x2)
                ) {
                    state.cards.forEach { cardState ->
                        when (cardState) {
                            is TitledAmountCardState -> {
                                CommonHubCard(
                                    title = cardState.title,
                                    amount = cardState.amount,
                                    onExpandClick = cardState.onExpandClick,
                                    collapseState = cardState.collapsedState,
                                    onCollapseClick = cardState.onCollapseClick
                                ) {
                                    when (cardState.state) {
                                        is FavoriteAssetsCardState -> AssetsCard(
                                            cardState.state as FavoriteAssetsCardState,
                                            viewModel::onAssetClick,
                                        )
                                        is FavoritePoolsCardState -> PoolsList(
                                            (cardState.state as FavoritePoolsCardState).state,
                                            viewModel::onPoolClick,
                                        )
                                    }
                                }
                            }

                            is SoraCardState -> {
                                AnimatedVisibility(
                                    visible = cardState.visible
                                ) {
                                    SoraCard(
                                        state = cardState,
                                        onCardStateClicked = viewModel::onCardStateClicked,
                                        onCloseClicked = viewModel::onRemoveSoraCard
                                    )
                                }
                            }

                            is BuyXorState -> {
                                BuyXorCard(
                                    visible = cardState.visible,
                                    onBuyXorClicked = viewModel::onBuyCrypto,
                                    onCloseCard = viewModel::onRemoveBuyXorToken
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(size = 16.dp))
                    }
                }
            }
        }
    }
}
