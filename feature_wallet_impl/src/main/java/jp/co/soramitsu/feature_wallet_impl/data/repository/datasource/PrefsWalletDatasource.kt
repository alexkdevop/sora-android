/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.feature_wallet_impl.data.repository.datasource

import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import jp.co.soramitsu.common.data.Preferences
import jp.co.soramitsu.common.domain.Serializer
import jp.co.soramitsu.feature_wallet_api.domain.interfaces.WalletDatasource
import jp.co.soramitsu.feature_wallet_api.domain.model.Account
import jp.co.soramitsu.feature_wallet_api.domain.model.Asset
import jp.co.soramitsu.feature_wallet_api.domain.model.FeeType
import jp.co.soramitsu.feature_wallet_api.domain.model.TransferMeta
import javax.inject.Inject

class PrefsWalletDatasource @Inject constructor(
    private val preferences: Preferences,
    private val serializer: Serializer
) : WalletDatasource {

    companion object {
        private const val KEY_BALANCE = "key_balance"
        private const val KEY_CONTACTS = "key_contacts"
        private const val KEY_TRANSFER_META_FEE_RATE = "key_transfer_meta_rate"
        private const val KEY_TRANSFER_META_FEE_TYPE = "key_transfer_meta_type"
        private const val KEY_WITHDRAW_META_FEE_RATE = "key_withdraw_meta_rate"
        private const val KEY_WITHDRAW_META_FEE_TYPE = "key_withdraw_meta_type"
    }

    private val transferMetaSubject = BehaviorSubject.create<TransferMeta>()
    private val withdrawMetaSubject = BehaviorSubject.create<TransferMeta>()

    init {
        val transferMeta = retrieveTransferMeta()
        if (transferMeta != null) {
            transferMetaSubject.onNext(transferMeta)
        }

        val withdrawMeta = retrieveWithdrawMeta()
        if (withdrawMeta != null) {
            withdrawMetaSubject.onNext(withdrawMeta)
        }
    }

    override fun saveBalance(balance: Array<Asset>) {
        preferences.putString(KEY_BALANCE, serializer.serialize(balance))
    }

    override fun retrieveBalance(): Array<Asset>? {
        val balanceJson = preferences.getString(KEY_BALANCE)

        return if (balanceJson.isEmpty()) {
            null
        } else {
            serializer.deserialize<Array<Asset>>(balanceJson, object : TypeToken<Array<Asset>>() {}.type)
        }
    }

    override fun saveContacts(results: List<Account>) {
        preferences.putString(KEY_CONTACTS, serializer.serialize(results))
    }

    override fun retrieveContacts(): List<Account>? {
        val contactsJson = preferences.getString(KEY_CONTACTS)

        return if (contactsJson.isEmpty()) {
            null
        } else {
            serializer.deserialize<List<Account>>(contactsJson, object : TypeToken<List<Account>>() {}.type)
        }
    }

    override fun saveTransferMeta(transferMeta: TransferMeta) {
        transferMetaSubject.onNext(transferMeta)
        preferences.putDouble(KEY_TRANSFER_META_FEE_RATE, transferMeta.feeRate)
        preferences.putString(KEY_TRANSFER_META_FEE_TYPE, transferMeta.feeType.toString())
    }

    private fun retrieveTransferMeta(): TransferMeta? {
        val feeRate = preferences.getDouble(KEY_TRANSFER_META_FEE_RATE, -1.0)

        if (feeRate != -1.0) {
            return TransferMeta(feeRate, FeeType.valueOf(preferences.getString(KEY_TRANSFER_META_FEE_TYPE)))
        }

        return null
    }

    override fun observeTransferMeta(): Observable<TransferMeta> {
        return transferMetaSubject
    }

    override fun saveWithdrawMeta(transferMeta: TransferMeta) {
        withdrawMetaSubject.onNext(transferMeta)
        preferences.putDouble(KEY_WITHDRAW_META_FEE_RATE, transferMeta.feeRate)
        preferences.putString(KEY_WITHDRAW_META_FEE_TYPE, transferMeta.feeType.toString())
    }

    private fun retrieveWithdrawMeta(): TransferMeta? {
        val feeRate = preferences.getDouble(KEY_WITHDRAW_META_FEE_RATE, -1.0)

        if (feeRate != -1.0) {
            return TransferMeta(feeRate, FeeType.valueOf(preferences.getString(KEY_WITHDRAW_META_FEE_TYPE)))
        }

        return null
    }

    override fun observeWithdrawMeta(): Observable<TransferMeta> {
        return withdrawMetaSubject
    }
}