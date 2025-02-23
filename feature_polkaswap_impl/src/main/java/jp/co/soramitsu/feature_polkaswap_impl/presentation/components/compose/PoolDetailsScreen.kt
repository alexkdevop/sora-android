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

package jp.co.soramitsu.feature_polkaswap_impl.presentation.components.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.domain.DEFAULT_ICON_URI
import jp.co.soramitsu.common.presentation.compose.TokenIcon
import jp.co.soramitsu.common.presentation.compose.components.DetailsItem
import jp.co.soramitsu.feature_polkaswap_impl.presentation.states.PoolDetailsDemeterState
import jp.co.soramitsu.feature_polkaswap_impl.presentation.states.PoolDetailsState
import jp.co.soramitsu.ui_core.component.button.FilledButton
import jp.co.soramitsu.ui_core.component.button.TonalButton
import jp.co.soramitsu.ui_core.component.button.properties.Order
import jp.co.soramitsu.ui_core.component.button.properties.Size
import jp.co.soramitsu.ui_core.component.card.ContentCard
import jp.co.soramitsu.ui_core.resources.Dimens
import jp.co.soramitsu.ui_core.theme.customColors
import jp.co.soramitsu.ui_core.theme.customTypography

@Composable
internal fun PoolDetailsScreen(
    state: PoolDetailsState,
    onSupply: () -> Unit,
    onRemove: () -> Unit,
) {
    ContentCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.x3)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ConstraintLayout(
                    modifier = Modifier.wrapContentSize()
                ) {
                    val (token1, token2) = createRefs()
                    TokenIcon(
                        uri = state.token1Icon,
                        size = Size.Small,
                        modifier = Modifier
                            .constrainAs(token1) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                            }
                    )
                    TokenIcon(
                        uri = state.token2Icon,
                        size = Size.Small,
                        modifier = Modifier
                            .constrainAs(token2) {
                                top.linkTo(parent.top)
                                start.linkTo(token1.start, margin = 24.dp)
                            }
                    )
                }
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .padding(horizontal = Dimens.x2),
                    color = MaterialTheme.customColors.fgPrimary,
                    style = MaterialTheme.customTypography.headline2,
                    text = "%s-%s".format(state.symbol1, state.symbol2),
                    maxLines = 1,
                )
            }
            Divider(
                color = Color.Transparent,
                thickness = Dimens.x2,
                modifier = Modifier.fillMaxWidth(),
            )
            DetailsItem(
                text = stringResource(id = R.string.pool_apy_title),
                value1 = state.apy,
                value1Bold = true,
                hint = stringResource(id = R.string.polkaswap_sb_apy_info),
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.x1),
                color = MaterialTheme.customColors.bgPage,
                thickness = 1.dp,
            )
            DetailsItem(
                text = stringResource(id = R.string.polkaswap_reward_payout),
                value1 = state.rewardsTokenSymbol,
                value1Uri = state.rewardsUri,
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.x1),
                color = MaterialTheme.customColors.bgPage,
                thickness = 1.dp,
            )
            if (state.userPoolSharePercent != null) {
                DetailsItem(
                    text = stringResource(id = R.string.pool_share_title_1),
                    value1 = state.userPoolSharePercent,
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.x1),
                    color = MaterialTheme.customColors.bgPage,
                    thickness = 1.dp,
                )
            }
            if (state.pooled1 != null) {
                DetailsItem(
                    text = stringResource(id = R.string.your_pooled).format(state.symbol1),
                    value1 = state.pooled1,
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.x1),
                    color = MaterialTheme.customColors.bgPage,
                    thickness = 1.dp,
                )
            }
            if (state.pooled2 != null) {
                DetailsItem(
                    text = stringResource(id = R.string.your_pooled).format(state.symbol2),
                    value1 = state.pooled2,
                )
                Divider(
                    color = Color.Transparent,
                    thickness = Dimens.x3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            FilledButton(
                size = Size.Large,
                order = Order.PRIMARY,
                enabled = state.addEnabled,
                text = stringResource(id = R.string.common_supply_liquidity_title),
                modifier = Modifier.fillMaxWidth(),
                onClick = onSupply,
            )
            Divider(
                color = Color.Transparent,
                thickness = Dimens.x2,
                modifier = Modifier.fillMaxWidth(),
            )
            TonalButton(
                size = Size.Large,
                order = Order.PRIMARY,
                enabled = state.removeEnabled,
                text = stringResource(id = R.string.remove_liquidity_title),
                modifier = Modifier.fillMaxWidth(),
                onClick = onRemove,
            )
            if (state.demeter100Percent) {
                Text(
                    modifier = Modifier
                        .padding(top = Dimens.x2)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.customColors.fgSecondary,
                    style = MaterialTheme.customTypography.paragraphXSBold,
                    text = stringResource(
                        id = R.string.polkaswap_farming_unstake_to_remove,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
    state.demeterPools?.forEach { pool ->
        Divider(
            color = Color.Transparent,
            thickness = Dimens.x1,
            modifier = Modifier.fillMaxWidth(),
        )
        ContentCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Column(
                modifier = Modifier
                    .padding(Dimens.x3)
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    color = MaterialTheme.customColors.fgPrimary,
                    style = MaterialTheme.customTypography.headline2,
                    text = stringResource(
                        id = R.string.polkaswap_farming_staked_for,
                        pool.rewardsTokenSymbol,
                    ),
                    maxLines = 1,
                )
                Text(
                    modifier = Modifier
                        .padding(top = Dimens.x1_2)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    color = MaterialTheme.customColors.fgTertiary,
                    style = MaterialTheme.customTypography.textXSBold,
                    text = stringResource(
                        id = R.string.polkaswap_farming_demeter_power,
                    ),
                    maxLines = 1,
                )
                DetailsItem(
                    modifier = Modifier.padding(top = Dimens.x3),
                    text = stringResource(id = R.string.polkaswap_reward_payout),
                    value1 = pool.rewardsTokenSymbol,
                    value1Uri = pool.rewardsUri,
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.x1),
                    color = MaterialTheme.customColors.bgPage,
                    thickness = 1.dp,
                )
                DetailsItem(
                    text = stringResource(id = R.string.polkaswap_farming_pool_share),
                    value1 = "%.4f %%".format(pool.percent),
                    value1Percent = pool.percent / 100,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPoolDetailsScreen() {
    Column {
        PoolDetailsScreen(
            PoolDetailsState(
                token1Icon = DEFAULT_ICON_URI,
                token2Icon = DEFAULT_ICON_URI,
                rewardsUri = DEFAULT_ICON_URI,
                rewardsTokenSymbol = "PSWAP",
                apy = "23.3%",
                symbol1 = "XOR",
                symbol2 = "VAL",
                pooled1 = "123 VAL",
                pooled2 = "2424.2 XOR",
                addEnabled = true,
                removeEnabled = true,
                userPoolSharePercent = "12.3%",
                demeterPools = listOf(
                    PoolDetailsDemeterState(
                        DEFAULT_ICON_URI,
                        "DEO",
                        12.4f,
                    )
                ),
                demeter100Percent = true,
            ),
            onRemove = {},
            onSupply = {},
        )
    }
}
