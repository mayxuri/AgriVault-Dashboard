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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
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
            .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only
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
fun AgriVaultUI(viewModel: TransactionViewModel = viewModel()) {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val transactions by viewModel.transactions.collectAsState()

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
            label = { Text("Amount (₹)") }
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val parsedAmount = amount.toDoubleOrNull()
            when {
                // Fix 2: Validate empty title
                title.isBlank() -> errorMessage = "Title cannot be empty"
                // Fix 2: Validate zero or null amount
                parsedAmount == null || parsedAmount <= 0.0 -> errorMessage = "Amount must be greater than ₹0.00"
                else -> {
                    viewModel.addTransaction(title.trim(), parsedAmount)
                    // Fix 3: Clear input fields after logging to prevent duplicate entries
                    title = ""
                    amount = ""
                    errorMessage = null
                }
            }
        }) {
            Text("Log Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fix 4: Corrected label from "Total Balance" to "Total Spending"
        Text("Total Spending: ₹${"%.2f".format(transactions.sumOf { it.amount })}")

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(transactions) { txn ->
                Text("${txn.title} - ₹${"%.2f".format(txn.amount)}")
            }
        }
    }
}
