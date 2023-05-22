/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.common.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.presentation.viewmodel.BaseViewModel

abstract class BaseFragment<T : BaseViewModel>(@LayoutRes layoutRes: Int) : Fragment(layoutRes) {

    abstract val viewModel: T

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.errorLiveData.observe {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.common_error_general_title)
                .setMessage(it)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .show()
        }
        viewModel.alertDialogLiveData.observe {
            AlertDialog.Builder(requireActivity())
                .setTitle(it.first)
                .setMessage(it.second)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .show()
        }

        viewModel.errorFromResourceLiveData.observe {
            showErrorFromResponse(it.first, it.second)
        }
    }

    protected fun showErrorFromResponse(title: Int, messageResId: Int) {
        AlertDialog.Builder(requireActivity())
            .setTitle(title)
            .setMessage(messageResId)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }

    fun <V> LiveData<V>.observe(observer: (V) -> Unit) {
        observe(viewLifecycleOwner, observer)
    }
}
