package jp.co.soramitsu.feature_votable_api.domain.model

import java.util.Date

interface Votable {
    val id: String
    val deadline: Date
    val statusUpdateTime: Date
    fun isSameAs(another: Votable): Boolean
}