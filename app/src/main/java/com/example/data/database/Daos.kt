package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): UserEntity?

    @Query("SELECT * FROM users WHERE aadhaar = :aadhaar LIMIT 1")
    suspend fun getUserByAadhaar(aadhaar: String): UserEntity?

    @Query("SELECT * FROM users WHERE voterId = :voterId LIMIT 1")
    suspend fun getUserByVoterId(voterId: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface CandidateDao {
    @Query("SELECT * FROM candidates ORDER BY name ASC")
    fun getAllCandidates(): Flow<List<CandidateEntity>>

    @Query("SELECT * FROM candidates WHERE id = :id")
    suspend fun getCandidateById(id: Int): CandidateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: CandidateEntity): Long

    @Update
    suspend fun updateCandidate(candidate: CandidateEntity)

    @Delete
    suspend fun deleteCandidate(candidate: CandidateEntity)
}

@Dao
interface ElectionDao {
    @Query("SELECT * FROM elections ORDER BY id DESC")
    fun getAllElections(): Flow<List<ElectionEntity>>

    @Query("SELECT * FROM elections WHERE id = :id")
    suspend fun getElectionById(id: Int): ElectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElection(election: ElectionEntity): Long

    @Update
    suspend fun updateElection(election: ElectionEntity)
}

@Dao
interface BlockchainDao {
    @Query("SELECT * FROM blockchain ORDER BY `index` ASC")
    fun getAllBlocks(): Flow<List<BlockEntity>>

    @Query("SELECT * FROM blockchain ORDER BY `index` ASC")
    suspend fun getBlocksList(): List<BlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockEntity)

    @Query("DELETE FROM blockchain")
    suspend fun clearAllBlocks()
}
