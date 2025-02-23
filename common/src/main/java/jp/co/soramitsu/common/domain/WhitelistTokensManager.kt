/*
This file is part of the SORA network and Polkaswap app.

Copyright (c) 2020, 2021, Polka Biome Ltd. All rights reserved.
SPDX-License-Identifier: BSD-4-Clause

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or other
materials provided with the distribution.

All advertising materials mentioning features or use of this software must display
the following acknowledgement: This product includes software developed by Polka Biome
Ltd., SORA, and Polkaswap.

Neither the name of the Polka Biome Ltd. nor the names of its contributors may be used
to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Polka Biome Ltd. AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Polka Biome Ltd. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package jp.co.soramitsu.common.domain

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import jp.co.soramitsu.common.io.FileManager
import jp.co.soramitsu.common.logger.FirebaseWrapper
import jp.co.soramitsu.xnetworking.sorawallet.tokenwhitelist.SoraTokensWhitelistManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class TokenDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
)

@Singleton
class WhitelistTokensManager @Inject constructor(
    private val manager: SoraTokensWhitelistManager,
    private val fileManager: FileManager,
) {

    companion object {
        private const val whitelistFileName = "whitelist_tokens.json"
    }

    var whitelistIds: List<String> = emptyList()
        private set

    private val curWhitelist = mutableListOf<TokenDto>()

    private fun updateWhitelist(list: List<TokenDto>) {
        curWhitelist.clear()
        curWhitelist.addAll(list)
        whitelistIds = curWhitelist.map { it.id }
    }

    init {
        fileManager.readInternalFile(whitelistFileName)?.let {
            val ids = Json.decodeFromString(ListSerializer(TokenDto.serializer()), it)
            updateWhitelist(ids)
        }
    }

    fun getTokenIconUri(tokenId: String): Uri {
        val type = curWhitelist.find { it.id == tokenId }?.type
        return if (type == null) {
            DEFAULT_ICON_URI
        } else {
            fileManager.readInternalCacheFileAsUri("$tokenId.$type") ?: DEFAULT_ICON_URI
        }
    }

    suspend fun updateWhitelistStorage() {
        runCatching { manager.getTokens() }.onSuccess { dtoList ->
            val ids = dtoList.map { TokenDto(it.address, it.type) }
            updateWhitelist(ids)
            fileManager.writeInternalFile(whitelistFileName, Json.encodeToString(ids))
            dtoList.forEach { dto ->
                when (dto.rawIcon) {
                    is String -> {
                        fileManager.writeInternalCacheFile(
                            "${dto.address}.${dto.type}",
                            dto.rawIcon as String,
                        )
                    }
                    is ByteArray -> {
                        fileManager.writeInternalCacheFile(
                            "${dto.address}.${dto.type}",
                            dto.rawIcon as ByteArray,
                        )
                    }
                    else -> {}
                }
            }
        }.onFailure {
            FirebaseWrapper.recordException(it)
            if (fileManager.existsInternalFile(whitelistFileName).not()) {
                updateWhitelist(
                    AssetHolder.getIds().map { id ->
                        TokenDto(id, "")
                    }
                )
            }
        }
    }
}
