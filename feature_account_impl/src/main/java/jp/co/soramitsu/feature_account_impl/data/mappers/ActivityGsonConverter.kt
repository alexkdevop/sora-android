/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.feature_account_impl.data.mappers

import com.google.gson.JsonObject
import jp.co.soramitsu.common.resourses.ResourceManager
import jp.co.soramitsu.common.util.ActivityFeedTypes
import jp.co.soramitsu.common.util.NumbersFormatter
import jp.co.soramitsu.feature_account_api.domain.model.ActivityFeed
import jp.co.soramitsu.feature_account_impl.R
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

class ActivityGsonConverter @Inject constructor(
    private val resourceManager: ResourceManager,
    private val numbersFormatter: NumbersFormatter
) {
    fun convertActivityItems(activities: List<JsonObject>, projectDict: JsonObject, userDict: JsonObject, userDid: String): List<ActivityFeed> {
        return mutableListOf<ActivityFeed>().apply {
            activities.forEach {
                val activityVm = fromGsonToVm(it, projectDict, userDict, userDid)
                if (activityVm.type.isNotEmpty()) add(activityVm)
            }
        }
    }

    private fun fromGsonToVm(jsonActivity: JsonObject, projectDict: JsonObject, userDict: JsonObject, userDid: String): ActivityFeed {
        val projectId = jsonActivity.getAsJsonPrimitive("projectId")?.asString
        val projectData = projectDict.getAsJsonObject(projectId)
        val projectName = projectData?.getAsJsonPrimitive("projectName")?.asString
            ?: resourceManager.getString(R.string.activity_project)

        var firstName = resourceManager.getString(R.string.activity_user)
        var lastName = ""

        when (jsonActivity.getAsJsonPrimitive("type")?.asString) {
            ActivityFeedTypes.FRIEND_REGISTERED.typeCode -> {
                val userData = userDict.getAsJsonObject(jsonActivity.getAsJsonPrimitive("userId").asString)
                firstName = userData?.getAsJsonPrimitive("firstName")?.asString ?: firstName
                lastName = userData?.getAsJsonPrimitive("lastName")?.asString ?: lastName

                return ActivityFeed(
                    resourceManager.getString(ActivityFeedTypes.FRIEND_REGISTERED.typeStringResource),
                    resourceManager.getString(ActivityFeedTypes.FRIEND_REGISTERED.titleStringResource).format(firstName, lastName),
                    resourceManager.getString(ActivityFeedTypes.FRIEND_REGISTERED.descriptionStringResource),
                    "",
                    Date(jsonActivity.getAsJsonPrimitive("issuedAt")?.asString!!.toLong() * 1000L),
                    ActivityFeedTypes.FRIEND_REGISTERED.iconDrawable
                )
            }
            ActivityFeedTypes.XOR_BETWEEN_USERS_TRANSFERRED.typeCode -> {
                val userData = if (jsonActivity.getAsJsonPrimitive("receiver").asString == userDid) userDict.getAsJsonObject(jsonActivity.getAsJsonPrimitive("sender").asString) else userDict.getAsJsonObject(jsonActivity.getAsJsonPrimitive("receiver").asString)
                firstName = userData?.getAsJsonPrimitive("firstName")?.asString ?: firstName
                lastName = userData?.getAsJsonPrimitive("lastName")?.asString ?: lastName

                return ActivityFeed(
                    resourceManager.getString(ActivityFeedTypes.XOR_BETWEEN_USERS_TRANSFERRED.typeStringResource),
                    "$firstName $lastName",
                    "",
                    numbersFormatter.formatBigDecimal(jsonActivity.getAsJsonPrimitive("amount").asBigDecimal),
                    Date(jsonActivity.getAsJsonPrimitive("issuedAt")?.asString!!.toLong() * 1000L),
                    ActivityFeedTypes.XOR_BETWEEN_USERS_TRANSFERRED.iconDrawable
                )
            }
            ActivityFeedTypes.VOTING_RIGHTS_CREDITED.typeCode ->
                return ActivityFeed(
                    resourceManager.getString(ActivityFeedTypes.VOTING_RIGHTS_CREDITED.typeStringResource),
                    resourceManager.getString(ActivityFeedTypes.VOTING_RIGHTS_CREDITED.titleStringResource),
                    "",
                    numbersFormatter.formatInteger(jsonActivity.getAsJsonPrimitive("votingRights").asBigDecimal),
                    Date(jsonActivity.getAsJsonPrimitive("issuedAt")?.asString!!.toLong() * 1000L),
                    ActivityFeedTypes.VOTING_RIGHTS_CREDITED.iconDrawable,
                    R.drawable.heart_shape
                )
            ActivityFeedTypes.PROJECT_FUNDED.typeCode -> {
                return ActivityFeed(resourceManager.getString(ActivityFeedTypes.PROJECT_FUNDED.typeStringResource),
                    resourceManager.getString(ActivityFeedTypes.PROJECT_FUNDED.titleStringResource).format(projectName),
                    resourceManager.getString(ActivityFeedTypes.PROJECT_FUNDED.descriptionStringResource),
                    "",
                    Date(jsonActivity.getAsJsonPrimitive("issuedAt")?.asString!!.toLong() * 1000L),
                    ActivityFeedTypes.PROJECT_FUNDED.iconDrawable
                )
            }
            ActivityFeedTypes.PROJECT_CREATED.typeCode ->
                return ActivityFeed(resourceManager.getString(ActivityFeedTypes.PROJECT_CREATED.typeStringResource),
                    resourceManager.getString(ActivityFeedTypes.PROJECT_CREATED.titleStringResource).format(projectName),
                    resourceManager.getString(ActivityFeedTypes.PROJECT_CREATED.descriptionStringResource)
                        .format(jsonActivity.getAsJsonPrimitive("description").asString),
                    "",
                    Date(jsonActivity.getAsJsonPrimitive("issuedAt")?.asString!!.toLong() * 1000L),
                    ActivityFeedTypes.PROJECT_CREATED.iconDrawable
                )
            ActivityFeedTypes.PROJECT_CLOSED.typeCode ->
                return ActivityFeed(resourceManager.getString(ActivityFeedTypes.PROJECT_CLOSED.typeStringResource),
                    resourceManager.getString(ActivityFeedTypes.PROJECT_CLOSED.titleStringResource).format(projectName),
                    "",
                    "",
                    Date(jsonActivity.getAsJsonPrimitive("issuedAt")?.asString!!.toLong() * 1000L),
                    ActivityFeedTypes.PROJECT_CLOSED.iconDrawable
                )
            ActivityFeedTypes.XOR_REWARD_CREDITED_FROM_PROJECT.typeCode ->
                return ActivityFeed(
                    resourceManager.getString(ActivityFeedTypes.XOR_REWARD_CREDITED_FROM_PROJECT.typeStringResource),
                    projectName,
                    "",
                    numbersFormatter.formatBigDecimal(BigDecimal(jsonActivity.getAsJsonPrimitive("reward").asString)),
                    Date(jsonActivity.getAsJsonPrimitive("issuedAt")?.asString!!.toLong() * 1000L),
                    ActivityFeedTypes.XOR_REWARD_CREDITED_FROM_PROJECT.iconDrawable
                )
            ActivityFeedTypes.USER_RANK_CHANGED.typeCode ->
                return ActivityFeed(resourceManager.getString(ActivityFeedTypes.USER_RANK_CHANGED.typeStringResource),
                    resourceManager.getString(ActivityFeedTypes.USER_RANK_CHANGED.titleStringResource)
                        .format(
                            jsonActivity.getAsJsonPrimitive("rank").asString,
                            jsonActivity.getAsJsonPrimitive("totalRank").asString
                    ),
                    "",
                    "",
                    Date(jsonActivity.getAsJsonPrimitive("issuedAt")?.asString!!.toLong() * 1000L),
                    ActivityFeedTypes.USER_RANK_CHANGED.iconDrawable
                )
            ActivityFeedTypes.USER_VOTED_FOR_PROJECT.typeCode -> {
                val userData = userDict.getAsJsonObject(jsonActivity.getAsJsonPrimitive("userId").asString)
                firstName = userData?.getAsJsonPrimitive("firstName")?.asString ?: firstName
                lastName = userData?.getAsJsonPrimitive("lastName")?.asString ?: lastName

                return ActivityFeed(resourceManager.getString(ActivityFeedTypes.USER_VOTED_FOR_PROJECT.typeStringResource),
                    resourceManager.getString(ActivityFeedTypes.USER_VOTED_FOR_PROJECT.titleStringResource).format(firstName, lastName),
                    resourceManager.getString(ActivityFeedTypes.USER_VOTED_FOR_PROJECT.descriptionStringResource)
                        .format(
                            firstName,
                            numbersFormatter.formatInteger(jsonActivity.getAsJsonPrimitive("givenVotes").asBigDecimal),
                            projectName
                        ),
                    "",
                    Date(jsonActivity.getAsJsonPrimitive("issuedAt")?.asString!!.toLong() * 1000L),
                    ActivityFeedTypes.USER_VOTED_FOR_PROJECT.iconDrawable
                )
            }
        }

        return ActivityFeed("", "", "", "", Date(), -1)
    }
}