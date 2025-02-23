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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.util.ext.testTagAsId
import jp.co.soramitsu.common_wallet.presentation.compose.states.SoraCardState
import jp.co.soramitsu.oauth.base.sdk.contract.SoraCardCommonVerification
import jp.co.soramitsu.ui_core.component.button.BleachedButton
import jp.co.soramitsu.ui_core.component.button.FilledButton
import jp.co.soramitsu.ui_core.component.button.TonalButton
import jp.co.soramitsu.ui_core.component.button.properties.Order
import jp.co.soramitsu.ui_core.component.button.properties.Size
import jp.co.soramitsu.ui_core.resources.Dimens
import jp.co.soramitsu.ui_core.theme.borderRadius

@Composable
fun SoraCard(
    modifier: Modifier = Modifier,
    state: SoraCardState,
    onCardStateClicked: () -> Unit,
    onCloseClicked: () -> Unit,
) {
    val shape = RoundedCornerShape(MaterialTheme.borderRadius.ml)
    Box(
        modifier = modifier
            .clip(shape)
            .clickable { onCardStateClicked() }
            .fillMaxWidth()
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.sora_card),
            contentDescription = null,
            contentScale = ContentScale.FillWidth
        )

        CardStateButton(
            modifier = Modifier
                .wrapContentWidth()
                .padding(bottom = Dimens.x3)
                .align(Alignment.BottomCenter),
            kycStatus = state.kycStatus,
            onCardStateClicked = onCardStateClicked
        )

        BleachedButton(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.TopEnd)
                .padding(Dimens.x1),
            size = Size.ExtraSmall,
            order = Order.TERTIARY,
            shape = CircleShape,
            onClick = onCloseClicked,
            leftIcon = painterResource(R.drawable.ic_cross),
        )
    }
}

@Composable
private fun CardStateButton(
    modifier: Modifier = Modifier,
    kycStatus: String?,
    onCardStateClicked: () -> Unit
) {
    if (kycStatus == null) {
        FilledButton(
            modifier = modifier
                .testTagAsId("GetSoraCard"),
            size = Size.Large,
            order = Order.SECONDARY,
            onClick = onCardStateClicked,
            text = stringResource(R.string.sora_card_see_details),
        )
    } else {
        if (kycStatus != SoraCardCommonVerification.Successful.toString())
            TonalButton(
                modifier = modifier
                    .testTagAsId("SoraCardButton"),
                size = Size.Large,
                order = Order.TERTIARY,
                onClick = onCardStateClicked,
                text = kycStatus
            )
    }
}

@Composable
@Preview
private fun PreviewSoraCard() {
    SoraCard(
        modifier = Modifier.fillMaxWidth(),
        state = SoraCardState(kycStatus = null),
        onCloseClicked = {},
        onCardStateClicked = {}
    )
}
