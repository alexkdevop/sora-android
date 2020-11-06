package jp.co.soramitsu.sora.splash.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import jp.co.soramitsu.feature_account_api.domain.model.OnboardingState
import jp.co.soramitsu.sora.splash.domain.SplashInteractor
import jp.co.soramitsu.sora.splash.domain.SplashRouter
import jp.co.soramitsu.test_shared.RxSchedulersRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SplashViewModelTest {

    @Rule @JvmField val rule: TestRule = InstantTaskExecutorRule()
    @Rule @JvmField val rxSchedulerRule = RxSchedulersRule()

    @Mock private lateinit var interactor: SplashInteractor
    @Mock private lateinit var router: SplashRouter

    private lateinit var splashViewModel: SplashViewModel

    @Before fun setUp() {
        splashViewModel = SplashViewModel(interactor, router)
    }

    @Test fun `nextScreen with REGISTRATION_FINISHED`() {
        given(interactor.getRegistrationState()).willReturn(OnboardingState.REGISTRATION_FINISHED)

        splashViewModel.nextScreen()

        verify(interactor).restoreAuth()
        verify(router).showMainScreen()
    }

    @Test fun `nextScreen with PHONE_NUMBER_CONFIRMED`() {
        val state = OnboardingState.PHONE_NUMBER_CONFIRMED

        given(interactor.getRegistrationState()).willReturn(state)

        splashViewModel.nextScreen()

        verify(interactor).restoreAuth()
        verify(router).showOnBoardingScreen(state)
    }

    @Test fun `nextScreen with INITIAL`() {
        val state = OnboardingState.INITIAL

        given(interactor.getRegistrationState()).willReturn(state)

        splashViewModel.nextScreen()

        verify(router).showOnBoardingScreen(state)
    }
}