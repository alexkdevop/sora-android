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

package jp.co.soramitsu.test_shared

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import jp.co.soramitsu.shared_utils.runtime.RuntimeSnapshot
import jp.co.soramitsu.shared_utils.runtime.definitions.TypeDefinitionParser
import jp.co.soramitsu.shared_utils.runtime.definitions.TypeDefinitionsTree
import jp.co.soramitsu.shared_utils.runtime.definitions.dynamic.DynamicTypeResolver
import jp.co.soramitsu.shared_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.shared_utils.runtime.definitions.registry.v14Preset
import jp.co.soramitsu.shared_utils.runtime.definitions.v14.TypesParserV14
import jp.co.soramitsu.shared_utils.runtime.metadata.RuntimeMetadataReader
import jp.co.soramitsu.shared_utils.runtime.metadata.builder.VersionedRuntimeBuilder
import jp.co.soramitsu.shared_utils.runtime.metadata.v14.RuntimeMetadataSchemaV14

object TestRuntimeProvider {

    fun buildRuntime(networkName: String): RuntimeSnapshot {
        val runtimeMetadataReader = buildRawMetadata(networkName)
        val typeRegistry = buildRegistry(networkName, runtimeMetadataReader)

        val metadata = VersionedRuntimeBuilder.buildMetadata(runtimeMetadataReader, typeRegistry)

        return RuntimeSnapshot(typeRegistry, metadata)
    }

    private fun buildRawMetadata(networkName: String) =
        getFileContentFromResources("${networkName}_metadata").run {
            RuntimeMetadataReader.read(this)
        }

    private fun buildRegistry(
        networkName: String,
        runtimeMetadataReader: RuntimeMetadataReader
    ): TypeRegistry {
        val gson = Gson()
        val soraReader = JsonReader(getResourceReader("$networkName.json"))

        val soraTree =
            gson.fromJson<TypeDefinitionsTree>(soraReader, TypeDefinitionsTree::class.java)

        val networkParsed = TypeDefinitionParser.parseNetworkVersioning(
            tree = soraTree,
            typePreset = TypesParserV14.parse(
                runtimeMetadataReader.metadata[RuntimeMetadataSchemaV14.lookup],
                v14Preset()
            ).typePreset,
            currentRuntimeVersion = 1,
            upto14 = false,
        )

        return TypeRegistry(
            types = networkParsed.typePreset,
            dynamicTypeResolver = DynamicTypeResolver.defaultCompoundResolver(),
        )
    }
}
