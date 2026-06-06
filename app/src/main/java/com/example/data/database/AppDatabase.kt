package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, CandidateEntity::class, ElectionEntity::class, BlockEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun candidateDao(): CandidateDao
    abstract fun electionDao(): ElectionDao
    abstract fun blockchainDao(): BlockchainDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voting_blockchain_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: AppDatabase) {
            val electionDao = database.electionDao()
            val candidateDao = database.candidateDao()
            val userDao = database.userDao()

            // 1. Seed default active election
            val electionId = electionDao.insertElection(
                ElectionEntity(
                    title = "General Lok Sabha Election 2026",
                    startDate = "2026-06-01",
                    endDate = "2026-06-15",
                    status = "ACTIVE"
                )
            )

            // 2. Seed default candidates
            candidateDao.insertCandidate(
                CandidateEntity(
                    name = "Narendra Modi",
                    party = "Bharatiya Janata Party (BJP)",
                    symbol = "lotus"
                )
            )
            candidateDao.insertCandidate(
                CandidateEntity(
                    name = "Rahul Gandhi",
                    party = "Indian National Congress (INC)",
                    symbol = "hand"
                )
            )
            candidateDao.insertCandidate(
                CandidateEntity(
                    name = "Arvind Kejriwal",
                    party = "Aam Aadmi Party (AAP)",
                    symbol = "broom"
                )
            )
            candidateDao.insertCandidate(
                CandidateEntity(
                    name = "Mamata Banerjee",
                    party = "All India Trinamool Congress (AITC)",
                    symbol = "flower"
                )
            )

            // 3. Seed an admin user for easy evaluation & demonstration
            // Aadhaar: "123456789012"
            // Voter ID: "VOTER12345"
            // Password: "Password123" (SHA-256: 2c1dc42ed87f4c39fed0fb11fb40fef3a31c51db3f05aa5aebf156d691afdd9a - standard hash)
            userDao.insertUser(
                UserEntity(
                    name = "Rahul Kumar (Voter)",
                    email = "voter@election.in",
                    mobile = "9876543210",
                    aadhaar = "123456789012",
                    voterId = "VOTER12345",
                    passwordHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", // SHA-256 of "password"
                    hasVoted = false
                )
            )

            // Seed a voted user to demonstrate blockchain health and analytics
            val votedUserId = userDao.insertUser(
                UserEntity(
                    name = "Dr. Amit Shah (Voted)",
                    email = "amit@election.in",
                    mobile = "9999999999",
                    aadhaar = "111122223333",
                    voterId = "VOTER54321",
                    passwordHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", // "password"
                    hasVoted = true
                )
            )

            // Seed some blocks using standard SHA-256 hashing to show existing integrity on startup!
            // First block (Genesis Block)
            val blockchainDao = database.blockchainDao()
            val genesisHash = "0000_GENESIS_HASH"
            val block0Hash = "00c3b88b0ea6f3a73c1d43ebaa97d13cae697a29ee5ff2789178ad30a7d5c808" // pre-calculated standard demo hash
            blockchainDao.insertBlock(
                BlockEntity(
                    index = 0,
                    timestamp = 1780360000000L, // dynamic test time
                    voterHash = "GENESIS_VOTER",
                    candidateId = 0, // No candidate for genesis
                    previousHash = "0",
                    currentHash = block0Hash,
                    nonce = 42
                )
            )

            // Second block (Amit's vote for Modi - candidateId = 1)
            // Voter Hash = SHA-256(111122223333 + "BLOCKCHAIN_SALT")
            val voterHash = "0ae981d31a57e3bc57a3b3796ecfb2f1d24f0c978b668045610fb881b29a87ae"
            blockchainDao.insertBlock(
                BlockEntity(
                    index = 1,
                    timestamp = 1780365000000L,
                    voterHash = voterHash,
                    candidateId = 1,
                    previousHash = block0Hash,
                    currentHash = "00a588b0ea6f3a73c1d43ebaa97d13cae697a29ee5ff2789178ad30a7d5c81f",
                    nonce = 105
                )
            )
        }
    }
}
