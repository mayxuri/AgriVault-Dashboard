package com.example.agrivault.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.agrivault.data.AppDatabase

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val dao = AppDatabase.getDatabase(applicationContext).transactionDao()
            val unsynced = dao.getUnsyncedTransactions()

            for (transaction in unsynced) {
                // TODO: Replace with actual MongoDB Atlas sync call
                // e.g. mongoCollection.insertOne(transaction.toDocument())
                dao.markAsSynced(transaction.id)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
