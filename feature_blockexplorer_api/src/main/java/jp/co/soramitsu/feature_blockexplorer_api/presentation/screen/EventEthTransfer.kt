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

package jp.co.soramitsu.feature_blockexplorer_api.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.domain.DEFAULT_ICON_URI
import jp.co.soramitsu.common.presentation.compose.TokenIcon
import jp.co.soramitsu.feature_blockexplorer_api.presentation.txhistory.EventUiModel
import jp.co.soramitsu.feature_blockexplorer_api.presentation.txhistory.TransactionStatus
import jp.co.soramitsu.ui_core.resources.Dimens
import jp.co.soramitsu.ui_core.theme.customColors
import jp.co.soramitsu.ui_core.theme.customTypography

@Composable
internal fun EventEthTransfer(
    eventUiModel: EventUiModel.EventTxUiModel.EventEthTransfer,
    modifier: Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = Dimens.x1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConstraintLayout(
            modifier = Modifier.wrapContentSize()
        ) {
            val (token1, token2, typeIcon) = createRefs()
            TokenIcon(
                modifier = Modifier.constrainAs(token1) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                },
                uri = eventUiModel.tokenUri,
                size = 28.dp,
            )
            TokenIcon(
                modifier = Modifier.constrainAs(token2) {
                    top.linkTo(token1.top, margin = 12.dp)
                    start.linkTo(token1.start, margin = 12.dp)
                },
                uri = eventUiModel.ethTokenUri,
                size = 28.dp,
            )
            Icon(
                modifier = Modifier
                    .size(Dimens.x2)
                    .clip(CircleShape)
                    .background(MaterialTheme.customColors.bgSurface)
                    .padding(2.dp)
                    .constrainAs(typeIcon) {
                        bottom.linkTo(token2.bottom)
                        start.linkTo(token2.start, margin = (-16).dp)
                    },
                painter = painterResource(id = R.drawable.ic_refresh_24),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = Dimens.x1)
        ) {
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "Sent to Eth",
                        style = MaterialTheme.customTypography.textM,
                        color = MaterialTheme.customColors.fgPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        text = eventUiModel.amountFormatted,
                        style = MaterialTheme.customTypography.textM,
                        color = MaterialTheme.customColors.fgPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (eventUiModel.status != TransactionStatus.COMMITTED) {
                    val statusIcon = if (eventUiModel.status == TransactionStatus.REJECTED) {
                        R.drawable.ic_error_16
                    } else {
                        R.drawable.ic_pending_16
                    }

                    Icon(
                        modifier = Modifier
                            .padding(start = Dimens.x1_2)
                            .size(Dimens.x2),
                        painter = painterResource(id = statusIcon),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
            Text(
                modifier = Modifier
                    .wrapContentSize(),
                text = eventUiModel.sidechainAddress,
                style = MaterialTheme.customTypography.textXSBold,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.customColors.fgSecondary,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEventEthTransfer() {
    EventEthTransfer(
        eventUiModel = eventPreview,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    )
}

private val eventPreview = EventUiModel.EventTxUiModel.EventEthTransfer(
    hash = "hash",
    timestamp = 1313123132,
    status = TransactionStatus.PENDING,
    tokenUri = DEFAULT_ICON_URI,
    ethTokenUri = DEFAULT_ICON_URI,
    dateTime = "09.03.2001",
    amountFormatted = "12133.2 XOR",
    fiatFormatted = "$123.4",
    requestHash = "0xde0c89ffb5ba12386b25234b98a5154123e7d040157836c40999c8f735678be9",
    sidechainAddress = "0x123b8d3a4cd1a859567b18d5e2d1012372aaa6f5",
)
