package com.example.agrivault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrivault.data.AppDatabase
import com.example.agrivault.data.TransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).transactionDao()

    val transactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(title: String, amount: Double) {
        viewModelScope.launch {
            dao.insert(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
