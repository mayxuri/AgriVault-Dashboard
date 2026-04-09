package com.example.agrivault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.example.agrivault.data.TransactionEntity
import com.example.agrivault.sync.SyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleSyncWorker()
        setContent {
            AgriVaultUI()
        }
    }

    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only — no mobile data sync
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "agrivault_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

@Composable
fun AgriVaultUI() {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val transactions = remember {
        mutableStateListOf<TransactionEntity>().apply {
            addAll(com.example.agrivault.data.DummyData.transactions)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = "AgriVault", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            transactions.add(
                TransactionEntity(
                    id = transactions.size,
                    title = title,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    timestamp = System.currentTimeMillis()
                )
            )
        }) {
            Text("Log Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Total Balance: ₹${transactions.sumOf { it.amount }}")

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(transactions) { txn ->
                Text("${txn.title} - ₹${txn.amount}")
            }
        }
    }
}
