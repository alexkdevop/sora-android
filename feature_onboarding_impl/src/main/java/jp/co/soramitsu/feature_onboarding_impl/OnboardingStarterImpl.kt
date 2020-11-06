package jp.co.soramitsu.feature_onboarding_impl

import android.content.Context
import jp.co.soramitsu.feature_account_api.domain.model.OnboardingState
import jp.co.soramitsu.feature_onboarding_api.OnboardingStarter
import jp.co.soramitsu.feature_onboarding_impl.presentation.OnboardingActivity
import javax.inject.Inject

class OnboardingStarterImpl @Inject constructor() : OnboardingStarter {

    override fun startWithInviteLink(context: Context) {
        OnboardingActivity.startWithInviteLink(context)
    }

    override fun start(context: Context, onboardingState: OnboardingState) {
        OnboardingActivity.start(context, onboardingState)
    }
}