package jp.co.soramitsu.feature_onboarding_impl.presentation.tutorial.di

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import jp.co.soramitsu.core_di.holder.scope.ScreenScope
import jp.co.soramitsu.feature_onboarding_impl.presentation.OnboardingRouter
import jp.co.soramitsu.feature_onboarding_impl.presentation.tutorial.TutorialFragment

@Subcomponent(
    modules = [
        TutorialModule::class
    ]
)
@ScreenScope
interface TutorialComponent {

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun withFragment(fragment: Fragment): Builder

        @BindsInstance
        fun withRouter(router: OnboardingRouter): Builder

        fun build(): TutorialComponent
    }

    fun inject(tutorialFragment: TutorialFragment)
}