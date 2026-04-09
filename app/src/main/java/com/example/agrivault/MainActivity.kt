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
import com.example.agrivault.data.TransactionEntity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgriVaultUI()
        }
    }
}

@Composable
fun AgriVaultUI() {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            onValueChange = {
                title = it
                errorMessage = null
            },
            label = { Text("Title") },
            isError = errorMessage != null && title.isBlank()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it
                errorMessage = null
            },
            label = { Text("Amount (₹)") },
            isError = errorMessage != null && (amount.toDoubleOrNull() == null || amount.toDoubleOrNull()!! <= 0.0)
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val parsedAmount = amount.toDoubleOrNull()
            when {
                // Security: reject null/blank title
                title.isBlank() ->
                    errorMessage = "Title cannot be empty. Please enter a valid expense name."
                // Security: reject zero-value and null transactions
                parsedAmount == null ->
                    errorMessage = "Invalid amount. Please enter a numeric value."
                parsedAmount <= 0.0 ->
                    errorMessage = "Amount must be greater than ₹0.00. Zero-value transactions are not allowed."
                // Security: reject unreasonably large amounts (>10 Cr) as likely data entry errors
                parsedAmount > 10_00_00_000.0 ->
                    errorMessage = "Amount exceeds allowed limit of ₹10,00,00,000."
                else -> {
                    transactions.add(
                        TransactionEntity(
                            id = transactions.size,
                            title = title.trim(),
                            amount = parsedAmount,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    title = ""
                    amount = ""
                    errorMessage = null
                }
            }
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
