/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.core_db.di

import jp.co.soramitsu.core_db.AppDatabase

interface DbApi {

    fun provideDatabase(): AppDatabase
}