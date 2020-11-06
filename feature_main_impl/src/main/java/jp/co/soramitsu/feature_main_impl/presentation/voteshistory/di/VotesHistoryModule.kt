package jp.co.soramitsu.feature_main_impl.presentation.voteshistory.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import jp.co.soramitsu.common.delegate.WithPreloaderImpl
import jp.co.soramitsu.common.interfaces.WithPreloader
import jp.co.soramitsu.core_di.holder.viewmodel.ViewModelKey
import jp.co.soramitsu.core_di.holder.viewmodel.ViewModelModule
import jp.co.soramitsu.feature_main_api.launcher.MainRouter
import jp.co.soramitsu.feature_main_impl.domain.MainInteractor
import jp.co.soramitsu.feature_main_impl.domain.TimeSectionInteractor
import jp.co.soramitsu.feature_main_impl.presentation.voteshistory.VotesHistoryViewModel

@Module(
    includes = [
        ViewModelModule::class
    ]
)
class VotesHistoryModule {

    @Provides
    fun providePreloader(preloader: WithPreloaderImpl): WithPreloader = preloader

    @Provides
    @IntoMap
    @ViewModelKey(VotesHistoryViewModel::class)
    fun provideViewModel(interactor: MainInteractor, router: MainRouter, preloader: WithPreloader, timeSectionInteractor: TimeSectionInteractor): ViewModel {
        return VotesHistoryViewModel(interactor, router, preloader, timeSectionInteractor)
    }

    @Provides
    fun provideViewModelCreator(fragment: Fragment, viewModelFactory: ViewModelProvider.Factory): VotesHistoryViewModel {
        return ViewModelProviders.of(fragment, viewModelFactory).get(VotesHistoryViewModel::class.java)
    }
}