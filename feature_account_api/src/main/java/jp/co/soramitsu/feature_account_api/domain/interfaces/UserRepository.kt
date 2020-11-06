package jp.co.soramitsu.feature_account_api.domain.interfaces

import io.reactivex.Completable
import io.reactivex.Single
import jp.co.soramitsu.feature_account_api.domain.model.ActivityFeed
import jp.co.soramitsu.feature_account_api.domain.model.ActivityFeedAnnouncement
import jp.co.soramitsu.feature_account_api.domain.model.AppVersion
import jp.co.soramitsu.feature_account_api.domain.model.Country
import jp.co.soramitsu.feature_account_api.domain.model.Invitations
import jp.co.soramitsu.feature_account_api.domain.model.Language
import jp.co.soramitsu.feature_account_api.domain.model.OnboardingState
import jp.co.soramitsu.feature_account_api.domain.model.Reputation
import jp.co.soramitsu.feature_account_api.domain.model.User
import jp.co.soramitsu.feature_account_api.domain.model.UserCreatingCase

interface UserRepository {

    fun getRegistrationState(): OnboardingState

    fun getAllCountries(): Single<List<Country>>

    fun savePin(pin: String)

    fun retrievePin(): String

    fun getInvitedUsers(updateCached: Boolean): Single<Invitations>

    fun getUser(updateCached: Boolean): Single<User>

    fun getActivityFeed(count: Int, offset: Int, updateCached: Boolean): Single<List<ActivityFeed>>

    fun getUserReputation(updateCached: Boolean): Single<Reputation>

    fun saveUserInfo(firstName: String, lastName: String): Completable

    fun verifySMSCode(code: String): Completable

    fun requestSMSCode(): Single<Int>

    fun saveRegistrationState(onboardingState: OnboardingState)

    fun createUser(phoneNumber: String): Single<UserCreatingCase>

    fun clearUserData(): Completable

    fun register(firstName: String, lastName: String, countryIso: String, inviteCode: String): Single<Boolean>

    fun checkAppVersion(): Single<AppVersion>

    fun getAppVersion(): Single<String>

    fun getAnnouncements(updateCached: Boolean): Single<List<ActivityFeedAnnouncement>>

    fun saveParentInviteCode(inviteCode: String)

    fun getParentInviteCode(): Single<String>

    fun checkInviteCodeAvailable(): Completable

    fun enterInviteCode(inviteCode: String): Completable

    fun applyInvitationCode(): Completable

    fun getAvailableLanguages(): Single<Pair<List<Language>, String>>

    fun changeLanguage(language: String): Single<String>

    fun getInvitationLink(): Single<String>

    fun getSelectedLanguage(): Single<Language>
}