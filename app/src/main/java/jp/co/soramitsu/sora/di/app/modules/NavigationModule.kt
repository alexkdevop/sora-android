package jp.co.soramitsu.sora.di.app.modules

import dagger.Module
import dagger.Provides
import jp.co.soramitsu.feature_main_api.launcher.MainRouter
import jp.co.soramitsu.feature_wallet_api.launcher.WalletRouter
import jp.co.soramitsu.sora.navigation.Navigator
import javax.inject.Singleton

@Module
class NavigationModule {

    @Singleton
    @Provides
    fun provideNavigator(): Navigator = Navigator()

    @Singleton
    @Provides
    fun provideMainRouter(navigator: Navigator): MainRouter = navigator

    @Singleton
    @Provides
    fun provideWalletRouter(navigator: Navigator): WalletRouter = navigator
}