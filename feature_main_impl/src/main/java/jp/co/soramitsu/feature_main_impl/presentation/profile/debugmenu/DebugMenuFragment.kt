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

package jp.co.soramitsu.feature_main_impl.presentation.profile.debugmenu

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.work.WorkInfo
import com.google.accompanist.navigation.animation.composable
import dagger.hilt.android.AndroidEntryPoint
import jp.co.soramitsu.common.base.SoraBaseFragment
import jp.co.soramitsu.common.base.theOnlyRoute
import jp.co.soramitsu.common.util.ext.getOsName
import jp.co.soramitsu.common.util.ext.getSize

@AndroidEntryPoint
class DebugMenuFragment : SoraBaseFragment<DebugMenuViewModel>() {

    override val viewModel: DebugMenuViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun NavGraphBuilder.content(
        scrollState: ScrollState,
        navController: NavHostController
    ) {
        composable(route = theOnlyRoute) {
            val dm = remember {
                activity?.getSize()
            }
            val pushState = NewHistoryEventsWorker.getInfo(requireContext()).observeAsState().value
            val pushEnabled =
                (pushState != null) && (pushState.size > 0) && ((pushState[0].state == WorkInfo.State.RUNNING) || (pushState[0].state == WorkInfo.State.ENQUEUED))
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = "%s %.3f".format("Density", dm?.first ?: 0.0))
                Text(text = "%s %d".format("Width", dm?.second ?: 0.0))
                Text(text = "%s %d".format("Height", dm?.third ?: 0.0))
                Text(text = activity?.getOsName().orEmpty())
                Button(onClick = viewModel::onResetRuntimeClick) {
                    Text(text = "Reset runtime")
                }
                Button(
                    modifier = Modifier.wrapContentSize().background(color = if (pushEnabled) Color.Green else Color.Gray),
                    onClick = {
                        if (pushEnabled) {
                            NewHistoryEventsWorker.stop(requireContext())
                        } else {
                            NewHistoryEventsWorker.start(requireContext())
                        }
                    },
                    content = {
                        Text(text = if (pushEnabled) "Disable" else "Enable")
                    }
                )
                DebugMenuScreen(state = viewModel.state)
            }
        }
    }
}
