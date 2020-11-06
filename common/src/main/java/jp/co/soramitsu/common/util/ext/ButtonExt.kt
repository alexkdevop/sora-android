/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.common.util.ext

import android.widget.Button

fun Button.enable() {
    this.isEnabled = true
    this.alpha = 1.0f
}

fun Button.disable() {
    this.isEnabled = false
    this.alpha = 0.5f
}