/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.common.di.api

import jp.co.soramitsu.common.data.network.NetworkApi
import java.util.concurrent.locks.ReentrantLock

abstract class FeatureApiHolder(
    private val mFeatureContainer: FeatureContainer
) {
    private val mFeatureLocker = ReentrantLock()

    private var mFeatureApi: Any? = null

    fun <T> getFeatureApi(): T {
        mFeatureLocker.lock()
        if (mFeatureApi == null) {
            mFeatureApi = initializeDependencies()
        }
        mFeatureLocker.unlock()
        return mFeatureApi as T
    }

    fun releaseFeatureApi() {
        mFeatureLocker.lock()
        mFeatureApi = null
        destroyDependencies()
        mFeatureLocker.unlock()
    }

    fun commonApi(): CommonApi {
        return mFeatureContainer.commonApi()
    }

    fun networkApi(): NetworkApi {
        return mFeatureContainer.networkApi()
    }

    fun didApi(): DidFeatureApi {
        return mFeatureContainer.didFeatureApi()
    }

    protected fun <T> getFeature(key: Class<T>): T {
        return mFeatureContainer.getFeature<T>(key) ?: throw RuntimeException()
    }

    protected fun releaseFeature(key: Class<*>) {
        mFeatureContainer.releaseFeature(key)
    }

    protected abstract fun initializeDependencies(): Any

    protected fun destroyDependencies() {
    }
}