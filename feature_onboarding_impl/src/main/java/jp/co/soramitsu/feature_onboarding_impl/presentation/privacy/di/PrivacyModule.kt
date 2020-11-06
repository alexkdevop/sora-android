package jp.co.soramitsu.feature_onboarding_impl.presentation.privacy.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import jp.co.soramitsu.core_di.holder.viewmodel.ViewModelKey
import jp.co.soramitsu.core_di.holder.viewmodel.ViewModelModule
import jp.co.soramitsu.feature_onboarding_impl.presentation.OnboardingRouter
import jp.co.soramitsu.feature_onboarding_impl.presentation.privacy.PrivacyViewModel

@Module(
    includes = [
        ViewModelModule::class
    ]
)
class PrivacyModule {

    @Provides
    @IntoMap
    @ViewModelKey(PrivacyViewModel::class)
    fun provideViewModel(router: OnboardingRouter): ViewModel {
        return PrivacyViewModel(router)
    }

    @Provides
    fun provideViewModelCreator(fragment: Fragment, viewModelFactory: ViewModelProvider.Factory): PrivacyViewModel {
        return ViewModelProviders.of(fragment, viewModelFactory).get(PrivacyViewModel::class.java)
    }
}