/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.sora.splash.presentation

import androidx.lifecycle.ViewModel
import jp.co.soramitsu.feature_account_api.domain.model.OnboardingState
import jp.co.soramitsu.sora.splash.domain.SplashInteractor
import jp.co.soramitsu.sora.splash.domain.SplashRouter

class SplashViewModel(
    private val interactor: SplashInteractor,
    private val router: SplashRouter
) : ViewModel() {

    fun nextScreen() {
        when (val state = interactor.getRegistrationState()) {
            OnboardingState.REGISTRATION_FINISHED -> {
                interactor.restoreAuth()
                router.showMainScreen()
            }
            OnboardingState.PHONE_NUMBER_CONFIRMED -> {
                interactor.restoreAuth()
                router.showOnBoardingScreen(state)
            }
            OnboardingState.INITIAL -> {
                router.showOnBoardingScreen(state)
            }
        }
    }

    fun handleDeepLink(invitationCode: String) {
        val state = interactor.getRegistrationState()
        interactor.saveInviteCode(invitationCode)

        if (OnboardingState.INITIAL == state || OnboardingState.PHONE_NUMBER_CONFIRMED == state) {
            router.showOnBoardingScreenViaInviteLink()
        } else {
            router.showMainScreenFromInviteLink()
        }
    }
}