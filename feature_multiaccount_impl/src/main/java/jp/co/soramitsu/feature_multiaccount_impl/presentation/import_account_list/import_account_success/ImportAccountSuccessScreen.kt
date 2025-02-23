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

package jp.co.soramitsu.feature_multiaccount_impl.presentation.import_account_list.import_account_success

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import jp.co.soramitsu.backup.domain.models.BackupAccountMeta
import jp.co.soramitsu.common.R
import jp.co.soramitsu.feature_multiaccount_impl.presentation.BackupAccountMetaWithIcon
import jp.co.soramitsu.feature_multiaccount_impl.presentation.ImportAccountPasswordState
import jp.co.soramitsu.feature_multiaccount_impl.presentation.import_account_list.import_account_password.AccountWithIcon
import jp.co.soramitsu.ui_core.component.button.FilledButton
import jp.co.soramitsu.ui_core.component.button.LoaderWrapper
import jp.co.soramitsu.ui_core.component.button.OutlinedButton
import jp.co.soramitsu.ui_core.component.button.properties.Order
import jp.co.soramitsu.ui_core.component.button.properties.Size
import jp.co.soramitsu.ui_core.component.card.ContentCard
import jp.co.soramitsu.ui_core.resources.Dimens
import jp.co.soramitsu.ui_core.theme.customColors
import jp.co.soramitsu.ui_core.theme.customTypography

@Composable
fun ImportAccountSuccessScreen(
    importAccountScreenState: ImportAccountPasswordState,
    onContinueClicked: () -> Unit,
    onImportMoreClicked: () -> Unit
) {
    ContentCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = Dimens.x2, end = Dimens.x2, bottom = Dimens.x2),
    ) {
        Column {
            Text(
                modifier = Modifier
                    .padding(Dimens.x3),
                text = stringResource(id = R.string.succesfully_imported_account_title),
                style = MaterialTheme.customTypography.headline2,
                textAlign = TextAlign.Center
            )
            importAccountScreenState.selectedAccount?.let {
                AccountWithIcon(
                    modifier = Modifier
                        .background(MaterialTheme.customColors.bgPage)
                        .padding(vertical = Dimens.x2, horizontal = Dimens.x3),
                    address = it.backupAccountMeta.address,
                    accountName = it.backupAccountMeta.name,
                    accountIcon = it.icon
                )
            }

            val bottomPadding = if (importAccountScreenState.isImportMoreAvailable) {
                Dimens.x2
            } else {
                Dimens.x3
            }

            FilledButton(
                modifier = Modifier
                    .padding(
                        start = Dimens.x3,
                        end = Dimens.x3,
                        bottom = bottomPadding,
                        top = Dimens.x4
                    )
                    .fillMaxWidth(),
                size = Size.Large,
                order = Order.PRIMARY,
                text = stringResource(id = R.string.common_continue),
                onClick = onContinueClicked
            )

            if (importAccountScreenState.isImportMoreAvailable) {
                LoaderWrapper(
                    modifier = Modifier
                        .padding(start = Dimens.x3, end = Dimens.x3, bottom = Dimens.x3)
                        .fillMaxWidth(),
                    loading = importAccountScreenState.isLoading,
                    loaderSize = Size.Large,
                ) { modifier, elevation ->
                    OutlinedButton(
                        modifier = modifier
                            .fillMaxWidth(),
                        size = Size.Large,
                        order = Order.PRIMARY,
                        text = stringResource(id = R.string.import_more_title),
                        onClick = onImportMoreClicked
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewEnterPassphraseScreen() {
    ImportAccountSuccessScreen(
        ImportAccountPasswordState(
            selectedAccount = BackupAccountMetaWithIcon(
                BackupAccountMeta("aa", "add"), Drawable.createFromPath("")!!
            )
        ),
        {},
        {}
    )
}
