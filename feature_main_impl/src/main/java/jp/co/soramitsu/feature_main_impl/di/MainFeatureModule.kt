package jp.co.soramitsu.feature_main_impl.di

import dagger.Module
import dagger.Provides
import jp.co.soramitsu.feature_main_api.launcher.MainStarter
import jp.co.soramitsu.feature_main_impl.MainStarterImpl

@Module
class MainFeatureModule {

    @Provides
    fun provideOnboardingStarter(starter: MainStarterImpl): MainStarter = starter
}