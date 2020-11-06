/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.feature_ethereum_impl.data.network.model

import com.google.gson.annotations.SerializedName

data class WithdrawalLimits(
    @SerializedName("remainingLimit") val remainingLimit: String,
    @SerializedName("timestampDue") val timestampdDue: Long,
    @SerializedName("totalLimit") val totalLimit: String
)