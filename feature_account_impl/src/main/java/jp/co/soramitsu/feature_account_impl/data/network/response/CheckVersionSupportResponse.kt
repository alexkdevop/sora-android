package jp.co.soramitsu.feature_account_impl.data.network.response

import com.google.gson.annotations.SerializedName
import jp.co.soramitsu.common.data.network.dto.StatusDto

data class CheckVersionSupportResponse(
    @SerializedName("result") val result: Boolean,
    @SerializedName("url") val url: String?,
    @SerializedName("status") val status: StatusDto
)