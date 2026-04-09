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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgriVaultUI()
        }
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
                title.isBlank() -> errorMessage = "Title cannot be empty"
                parsedAmount == null || parsedAmount <= 0.0 -> errorMessage = "Amount must be greater than ₹0.00"
                else -> {
                    viewModel.addTransaction(title.trim(), parsedAmount)
                    title = ""
                    amount = ""
                    errorMessage = null
                }
            }
        }) {
            Text("Log Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Total Spending: ₹${"%.2f".format(transactions.sumOf { it.amount })}")

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(transactions) { txn ->
                Text("${txn.title} - ₹${"%.2f".format(txn.amount)}")
            }
        }
    }
}
