package jp.co.soramitsu.feature_ethereum_api.domain.model

import java.math.BigDecimal
import java.math.BigInteger

data class GasEstimation(
    val type: Type,
    val amount: BigInteger,
    val amountInEth: BigDecimal,
    val timeInSeconds: Long
) {
    enum class Type {
        SLOW,
        REGULAR,
        FAST
    }
}