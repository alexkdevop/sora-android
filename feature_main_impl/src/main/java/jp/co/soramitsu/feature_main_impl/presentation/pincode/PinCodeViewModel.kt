package jp.co.soramitsu.feature_main_impl.presentation.pincode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.co.soramitsu.common.interfaces.WithProgress
import jp.co.soramitsu.common.presentation.viewmodel.BaseViewModel
import jp.co.soramitsu.common.util.Event
import jp.co.soramitsu.common.util.ext.setValueIfNew
import jp.co.soramitsu.feature_main_api.domain.model.PinCodeAction
import jp.co.soramitsu.feature_main_api.launcher.MainRouter
import jp.co.soramitsu.feature_main_impl.R
import jp.co.soramitsu.feature_main_impl.domain.PinCodeInteractor
import java.util.concurrent.TimeUnit

class PinCodeViewModel(
    private val interactor: PinCodeInteractor,
    private val mainRouter: MainRouter,
    private val progress: WithProgress,
    private val maxPinCodeLength: Int
) : BaseViewModel(), WithProgress by progress {

    companion object {
        private const val COMPLETE_PIN_CODE_DELAY: Long = 12
    }

    private lateinit var action: PinCodeAction
    private var tempCode = ""

    private val inputCodeLiveData = MutableLiveData<String>()

    val backButtonVisibilityLiveData = MutableLiveData<Boolean>()
    val toolbarTitleResLiveData = MutableLiveData<Int>()
    val wrongPinCodeEventLiveData = MutableLiveData<Event<Unit>>()
    val showFingerPrintEventLiveData = MutableLiveData<Event<Unit>>()
    val startFingerprintScannerEventLiveData = MutableLiveData<Event<Unit>>()
    val fingerPrintDialogVisibilityLiveData = MutableLiveData<Boolean>()
    val fingerPrintAutFailedLiveData = MutableLiveData<Event<Unit>>()
    val fingerPrintErrorLiveData = MutableLiveData<Event<String>>()
    val pinCodeProgressLiveData = MediatorLiveData<Int>()
    val deleteButtonVisibilityLiveData = MediatorLiveData<Boolean>()

    private val _closeAppLiveData = MutableLiveData<Event<Unit>>()
    val closeAppLiveData: LiveData<Event<Unit>> = _closeAppLiveData

    private val _checkInviteLiveData = MutableLiveData<Event<Unit>>()
    val checkInviteLiveData: LiveData<Event<Unit>> = _checkInviteLiveData

    private val _ethServiceEvent = MutableLiveData<Event<Unit>>()
    val ethServiceEvent: LiveData<Event<Unit>> = _ethServiceEvent

    init {
        pinCodeProgressLiveData.addSource(inputCodeLiveData) {
            pinCodeProgressLiveData.value = it.length
        }

        deleteButtonVisibilityLiveData.addSource(inputCodeLiveData) {
            deleteButtonVisibilityLiveData.setValueIfNew(it.isNotEmpty())
        }

        inputCodeLiveData.value = ""
    }

    fun startAuth(pinCodeAction: PinCodeAction) {
        action = pinCodeAction
        when (action) {
            PinCodeAction.CREATE_PIN_CODE -> {
                toolbarTitleResLiveData.value = R.string.pincode_set_your_pin_code
                backButtonVisibilityLiveData.value = false
            }
            PinCodeAction.OPEN_PASSPHRASE -> {
                toolbarTitleResLiveData.value = R.string.pincode_enter_pin_code
                showFingerPrintEventLiveData.value = Event(Unit)
                backButtonVisibilityLiveData.value = true
            }
            PinCodeAction.TIMEOUT_CHECK -> {
                disposables.add(
                    interactor.isCodeSet()
                        .subscribe({
                            if (it) {
                                toolbarTitleResLiveData.value = R.string.pincode_enter_pin_code
                                showFingerPrintEventLiveData.value = Event(Unit)
                                backButtonVisibilityLiveData.value = false
                            } else {
                                toolbarTitleResLiveData.value = R.string.pincode_set_your_pin_code
                                backButtonVisibilityLiveData.value = false
                                action = PinCodeAction.CREATE_PIN_CODE
                            }
                        }, {
                            onError(it)
                            action = PinCodeAction.CREATE_PIN_CODE
                        })
                )
            }
        }
    }

    fun pinCodeNumberClicked(pinCodeNumber: String) {
        inputCodeLiveData.value?.let { inputCode ->
            if (inputCode.length >= maxPinCodeLength) {
                return
            }
            val newCode = inputCode + pinCodeNumber
            inputCodeLiveData.value = newCode
            if (newCode.length == maxPinCodeLength) {
                pinCodeEntered(newCode)
            }
        }
    }

    fun pinCodeDeleteClicked() {
        inputCodeLiveData.value?.let { inputCode ->
            if (inputCode.isEmpty()) {
                return
            }
            inputCodeLiveData.value = inputCode.substring(0, inputCode.length - 1)
        }
    }

    private fun pinCodeEntered(pin: String) {
        disposables.add(
            Completable.complete()
                .delay(COMPLETE_PIN_CODE_DELAY, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (PinCodeAction.CREATE_PIN_CODE == action) {
                        if (tempCode.isEmpty()) {
                            tempCode = pin
                            inputCodeLiveData.value = ""
                            toolbarTitleResLiveData.value = R.string.pincode_confirm_your_pin_code
                            backButtonVisibilityLiveData.value = true
                        } else {
                            pinCodeEnterComplete(pin)
                        }
                    } else {
                        checkPinCode(pin)
                    }
                }, {
                    logException(it)
                })
        )
    }

    private fun pinCodeEnterComplete(pinCode: String) {
        if (tempCode == pinCode) {
            registerPinCode(pinCode)
        } else {
            tempCode = ""
            inputCodeLiveData.value = ""
            toolbarTitleResLiveData.value = R.string.pincode_set_your_pin_code
            backButtonVisibilityLiveData.value = false
            onError(R.string.pincode_repeat_error)
        }
    }

    private fun registerPinCode(code: String) {
        disposables.add(
            interactor.savePin(code)
                .subscribe({
                    _ethServiceEvent.value = Event(Unit)
                    mainRouter.popBackStack()
                    _checkInviteLiveData.value = Event(Unit)
                }, {
                    it.printStackTrace()
                })
        )
    }

    private fun checkPinCode(code: String) {
        disposables.add(
            interactor.checkPin(code)
                .subscribe({
                    if (PinCodeAction.OPEN_PASSPHRASE == action) {
                        mainRouter.popBackStack()
                        mainRouter.showPassphrase()
                    } else {
                        _ethServiceEvent.value = Event(Unit)
                        mainRouter.showVerification()
                    }
                }, {
                    it.printStackTrace()
                    inputCodeLiveData.value = ""
                    wrongPinCodeEventLiveData.value = Event(Unit)
                })
        )
    }

    fun backPressed() {
        if (PinCodeAction.CREATE_PIN_CODE == action) {
            if (tempCode.isEmpty()) {
                _closeAppLiveData.value = Event(Unit)
            } else {
                tempCode = ""
                inputCodeLiveData.value = ""
                backButtonVisibilityLiveData.value = false
                toolbarTitleResLiveData.value = R.string.pincode_set_your_pin_code
            }
        } else {
            if (PinCodeAction.TIMEOUT_CHECK == action) {
                _closeAppLiveData.value = Event(Unit)
            } else {
                mainRouter.popBackStack()
            }
        }
    }

    fun onResume() {
        if (action != PinCodeAction.CREATE_PIN_CODE) {
            startFingerprintScannerEventLiveData.value = Event(Unit)
        }
    }

    fun onAuthenticationError(errString: String) {
        fingerPrintErrorLiveData.value = Event(errString)
    }

    fun onAuthenticationSucceeded() {
        if (PinCodeAction.OPEN_PASSPHRASE == action) {
            mainRouter.popBackStack()
            mainRouter.showPassphrase()
        } else {
            _ethServiceEvent.value = Event(Unit)
            mainRouter.showVerification()
        }
    }

    fun onAuthenticationFailed() {
        fingerPrintAutFailedLiveData.value = Event(Unit)
    }
}