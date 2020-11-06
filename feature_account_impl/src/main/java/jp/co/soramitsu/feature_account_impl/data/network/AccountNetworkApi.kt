package jp.co.soramitsu.feature_account_impl.data.network

import io.reactivex.Single
import jp.co.soramitsu.common.data.network.response.BaseResponse
import jp.co.soramitsu.feature_account_impl.data.network.model.DeviceFingerPrintRemote
import jp.co.soramitsu.feature_account_impl.data.network.request.CreateUserRequest
import jp.co.soramitsu.feature_account_impl.data.network.request.RegistrationRequest
import jp.co.soramitsu.feature_account_impl.data.network.request.SaveUserDataRequest
import jp.co.soramitsu.feature_account_impl.data.network.request.VerifyCodeRequest
import jp.co.soramitsu.feature_account_impl.data.network.response.CheckInviteCodeAvailableResponse
import jp.co.soramitsu.feature_account_impl.data.network.response.CheckVersionSupportResponse
import jp.co.soramitsu.feature_account_impl.data.network.response.GetCountriesResponse
import jp.co.soramitsu.feature_account_impl.data.network.response.GetReputationResponse
import jp.co.soramitsu.feature_account_impl.data.network.response.GetUserResponse
import jp.co.soramitsu.feature_account_impl.data.network.response.InvitedUsersResponse
import jp.co.soramitsu.feature_account_impl.data.network.response.SendSMSResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountNetworkApi {

    @GET("/account/v1/user/invited")
    fun getInvitedUsers(): Single<InvitedUsersResponse>

    @GET("/account/v1/user")
    fun getUser(): Single<GetUserResponse>

    @GET("/account/v1/user/reputation")
    fun getUserReputation(): Single<GetReputationResponse>

    @GET("/information/v1/information/country")
    fun getAllCountries(): Single<GetCountriesResponse>

    @POST("/account/v1/user/register")
    fun register(@Body registrationRequest: RegistrationRequest): Single<BaseResponse>

    @PUT("/account/v1/user")
    fun saveUserData(@Body saveUserDataRequest: SaveUserDataRequest): Single<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("/account/v1/smscode/send")
    fun requestSMSCode(): Single<SendSMSResponse>

    @POST("/account/v1/smscode/verify")
    fun verifySMSCode(@Body code: VerifyCodeRequest): Single<BaseResponse>

    @POST("/account/v1/user/create")
    fun createUser(@Body phoneNumber: CreateUserRequest): Single<SendSMSResponse>

    @GET("/information/v1/supported/android")
    fun checkVersionSupported(@Query("version") versionName: String): Single<CheckVersionSupportResponse>

    @POST("/account/v1/check")
    fun checkInviteCodeAvailable(@Body deviceFingerPrint: DeviceFingerPrintRemote): Single<CheckInviteCodeAvailableResponse>

    @POST("/account/v1/invitation/accept/{invitationCode}")
    fun addInvitationCode(@Path("invitationCode") invitationCode: String): Single<BaseResponse>
}