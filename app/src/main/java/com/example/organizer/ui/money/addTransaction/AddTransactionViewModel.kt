package com.example.organizer.ui.money.addTransaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.organizer.R
import com.example.organizer.database.enums.TransactionType
import com.example.organizer.database.entity.Account
import com.example.organizer.database.entity.Category
import com.example.organizer.database.entity.Transaction
import com.example.organizer.database.services.TransactionsService
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class AddTransactionViewModel : ViewModel() {
    var transactionType = MutableLiveData<Int>()
    val amount = MutableLiveData<String>()
    val details = MutableLiveData<String>()
    val fromAccount = MutableLiveData<Account?>()
    val toAccount = MutableLiveData<Account?>()
    val category = MutableLiveData<Category?>()
    var backgroundColor = MutableLiveData<Int>()
    val showFromAccount = MutableLiveData<Boolean>()
    val showToAccount = MutableLiveData<Boolean>()
    lateinit var transactionService: TransactionsService
    val selectedAccount = MutableLiveData<Account>()
    var initializedNotAllowed = false
    var fieldPendingToSetAfterNavigateBack: FIELDS = FIELDS.NONE
    val amountHint = MutableLiveData<String>("0")
    val errorEnabled = MutableLiveData<Boolean>(false)
    val navigateBack = MutableLiveData<Boolean>()

    companion object {
        enum class FIELDS {
            FROM_ACCOUNT, TO_ACCOUNT, CATEGORY, NONE
        }
    }

    init {
        selectTransactionType(0)
        navigateBack.value = false
    }

    fun selectTransferType() {
        if(transactionType.value!! != TransactionType.TRANSFER.typeCode) {
            selectTransactionType(TransactionType.TRANSFER.typeCode)
        }
    }

    fun selectIncomeType() {
        if(transactionType.value!! != TransactionType.INCOME.typeCode) {
            selectTransactionType(TransactionType.INCOME.typeCode)
        }
    }

    fun selectExpenseType() {
        if(transactionType.value!! != TransactionType.EXPENSE.typeCode) {
            selectTransactionType(TransactionType.EXPENSE.typeCode)
        }
    }

    fun setNavigateBack() {
        navigateBack.value = true
    }

    fun isTheAmountAllowed(amountValue: Double): Boolean {
        return amountValue.compareTo(0.0) > 0 && (
                fromAccount.value == null
                        || fromAccount.value!!.balance.compareTo(amountValue) >= 0
                )
    }

    fun createTransaction() {
        var amountValue = 0.0
        try {
            amountValue = NumberUtils.calculateValueFromString(amount.value?:"", 0).value
        } catch (ex: Exception) {
            errorEnabled.value = true
            amountHint.value = ex.message
        }
        if (errorEnabled.value == false && isTheAmountAllowed(amountValue)) {
            viewModelScope.launch {
                val transaction = Transaction(
                    0,
                    transactionType.value!!,
                    amountValue,
                    if (fromAccount.value == null) null else fromAccount.value!!.id,
                    if (toAccount.value == null) null else toAccount.value!!.id,
                    null,
                    if (category.value == null) null else category.value!!.id,
                    details.value?.trim(),
                    Date().time,
                    null,
                    null,null,null,null
                )
                try {
                    transactionService.insert(transaction)
                    setNavigateBack()
                } catch (ex: Exception) {
                    println("Expection occured" + ex.message)
                }
            }
        }
    }

    fun selectTransactionType(type: Int) {
        println(type)
        transactionType.value = type
        category.value = null
        if (TransactionType.TRANSFER.typeCode == type) {
            backgroundColor.value = R.color.TransferColor
            showFromAccount.value = true
            showToAccount.value = true
            fromAccount.value = selectedAccount.value
            toAccount.value = null
        } else if (TransactionType.INCOME.typeCode == type) {
            backgroundColor.value = R.color.IncomeColor
            showToAccount.value = true
            showFromAccount.value = false
            fromAccount.value = null
            toAccount.value = selectedAccount.value
        } else if (TransactionType.EXPENSE.typeCode == type) {
            backgroundColor.value = R.color.ExpenseColor
            showFromAccount.value = true
            showToAccount.value = false
            toAccount.value = null
            fromAccount.value = selectedAccount.value
        }
    }
}