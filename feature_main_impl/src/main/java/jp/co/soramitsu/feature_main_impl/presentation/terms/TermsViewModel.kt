package jp.co.soramitsu.feature_main_impl.presentation.terms

import jp.co.soramitsu.common.presentation.viewmodel.BaseViewModel
import jp.co.soramitsu.feature_main_api.launcher.MainRouter

class TermsViewModel(
    private val router: MainRouter
) : BaseViewModel() {

    fun onBackPressed() {
        router.popBackStack()
    }
}