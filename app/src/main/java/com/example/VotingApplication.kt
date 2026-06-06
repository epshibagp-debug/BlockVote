package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.VotingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class VotingApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy {
        VotingRepository(
            database.userDao(),
            database.candidateDao(),
            database.electionDao(),
            database.blockchainDao()
        )
    }
}
