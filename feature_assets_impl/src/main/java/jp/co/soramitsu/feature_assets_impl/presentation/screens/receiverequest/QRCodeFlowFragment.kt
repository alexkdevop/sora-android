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

package jp.co.soramitsu.feature_assets_impl.presentation.screens.receiverequest

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ScrollState
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import com.journeyapps.barcodescanner.ScanOptions
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import jp.co.soramitsu.androidfoundation.intent.ShareUtil
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.base.SoraBaseFragment
import jp.co.soramitsu.common.domain.BottomBarController
import jp.co.soramitsu.common_wallet.presentation.compose.components.SelectSearchTokenScreen
import jp.co.soramitsu.core_di.viewmodel.CustomViewModelFactory
import jp.co.soramitsu.feature_assets_impl.presentation.components.compose.receiverequest.QrCodeMainScreen
import jp.co.soramitsu.feature_assets_impl.presentation.components.compose.receiverequest.RequestTokenConfirmScreen
import jp.co.soramitsu.feature_assets_impl.presentation.screens.scan.QRCodeScannerActivity
import jp.co.soramitsu.feature_assets_impl.presentation.screens.scan.ScanTextContract

@AndroidEntryPoint
class QRCodeFlowFragment : SoraBaseFragment<QRCodeFlowViewModel>() {

    @Inject
    lateinit var vmf: QRCodeFlowViewModel.AssistedQRCodeFlowViewModelFactory

    override val viewModel: QRCodeFlowViewModel by viewModels {
        CustomViewModelFactory {
            vmf.create(
                isLaunchedFromSoraCard = requireArguments()
                    .getBoolean(IS_LAUNCHED_FROM_SORA_CARD)
            )
        }
    }

    private val barcodeScanOptions by lazy {
        ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt("")
            .setBeepEnabled(false)
            .setCaptureActivity(QRCodeScannerActivity::class.java)
    }

    private val barcodeLauncher: ActivityResultLauncher<ScanOptions> =
        registerForActivityResult(ScanTextContract()) { result ->
            result?.let {
                viewModel.onReceiveQRCodeScanUriResult(it)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as BottomBarController).hideBottomBar()
        super.onViewCreated(view, savedInstanceState)

        parseFragmentArguments()

        viewModel.shareQrCodeEvent.observe { (qrCodeFileUri, message) ->
            context?.let { context ->
                ShareUtil.shareImageFile(
                    context = context,
                    title = getString(R.string.common_share),
                    file = qrCodeFileUri,
                    description = message
                )
            }
        }
    }

    private fun parseFragmentArguments() =
        with(arguments) {
            if (this == null)
                return@with

            if (getBoolean(NAVIGATE_TO_SCANNER_DIRECTLY_KEY))
                barcodeLauncher.launch(barcodeScanOptions)
        }

    @OptIn(ExperimentalAnimationApi::class)
    override fun NavGraphBuilder.content(
        scrollState: ScrollState,
        navController: NavHostController
    ) {
        composable(route = QRCodeFlowRoute.MainScreen.route) {
            QrCodeMainScreen(
                scrollState = scrollState,
                receiveTokenByQrScreenState = viewModel.receiveTokenScreenState,
                requestTokenScreenState = viewModel.requestTokenByQrScreenState,
                receiveToken_onUserAddressClick = viewModel::onUserAddressClickInReceiveScreen,
                receiveToken_onScanQrClick = {
                    PermissionX.init(this@QRCodeFlowFragment)
                        .permissions(Manifest.permission.CAMERA)
                        .request { allGranted, _, _ ->
                            if (allGranted) {
                                barcodeLauncher.launch(barcodeScanOptions)
                            }
                        }
                },
                receiveToken_onShareCodeClick = viewModel::onShareQrCodeInReceiveScreen,
                receiveToken_onTryAgainClick = viewModel::onLoadReceiveScreenDataAgainClick,
                requestToken_onUserAddressClick = viewModel::onUserAddressClickInReceiveScreen,
                requestToken_onAmountChanged = viewModel::onRequestAmountChange,
                requestToken_onTokenSelect = {
                    navController.navigate(QRCodeFlowRoute.SelectToken.route)
                },
                requestToken_onFocusChange = viewModel::onFocusChange,
                requestToken_onCreateRequestClick = {
                    viewModel.onLoadRequestConfirmScreenDataAgainClick()
                    navController.navigate(QRCodeFlowRoute.ConfirmRequestByQRCode.route)
                },
                requestToken_onTryAgainClick = viewModel::onLoadRequestScreenDataClick
            )
        }

        composable(route = QRCodeFlowRoute.SelectToken.route) {
            SelectSearchTokenScreen(
                state = viewModel.selectTokenScreenState,
                scrollState = scrollState,
                onAssetSelect = {
                    viewModel.onSelectToken(it)
                    navController.popBackStack()
                },
            )
        }

        composable(route = QRCodeFlowRoute.ConfirmRequestByQRCode.route) {
            RequestTokenConfirmScreen(
                scrollState = scrollState,
                state = viewModel.requestTokenConfirmScreenState,
                onUserAddressClick = viewModel::onUserAddressClickInRequestConfirmScreen,
                onFocusChange = viewModel::onFocusChange,
                onShareCodeClick = viewModel::onShareQrCodeInRequestConfirmScreen,
                onTryAgainClick = viewModel::onLoadRequestConfirmScreenDataAgainClick
            )
        }
    }

    companion object {

        const val IS_LAUNCHED_FROM_SORA_CARD = "is_launched_from_sora_card"
        const val NAVIGATE_TO_SCANNER_DIRECTLY_KEY = "navigate_to_scanner_activity"

        fun createBundle(
            shouldNavigateToScanner: Boolean = false,
            isLaunchedFromSoraCard: Boolean = false
        ) = Bundle().apply {
            putBoolean(NAVIGATE_TO_SCANNER_DIRECTLY_KEY, shouldNavigateToScanner)
            putBoolean(IS_LAUNCHED_FROM_SORA_CARD, isLaunchedFromSoraCard)
        }
    }
}
