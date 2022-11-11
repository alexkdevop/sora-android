/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.common.util.ext

import jp.co.soramitsu.common.domain.OptionsProvider
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

fun BigDecimal.divideBy(
    divisor: BigDecimal,
    scale: Int? = null
): BigDecimal {
    return if (scale == null) {
        val maxScale = max(this.scale(), divisor.scale())

        if (maxScale != 0) {
            this.divide(divisor, maxScale, RoundingMode.HALF_EVEN)
        } else {
            this.divide(divisor, OptionsProvider.defaultScale, RoundingMode.HALF_EVEN)
        }
    } else {
        this.divide(divisor, scale, RoundingMode.HALF_EVEN)
    }
}

fun BigDecimal.safeDivide(
    divisor: BigDecimal,
    scale: Int? = null
): BigDecimal {
    return if (divisor.compareTo(BigDecimal.ZERO) == 0) {
        BigDecimal.ZERO
    } else {
        divideBy(divisor, scale)
    }
}
