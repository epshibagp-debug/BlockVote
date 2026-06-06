package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val mobile: String,
    val aadhaar: String,
    val voterId: String,
    val passwordHash: String, // SHA-256 of password
    val hasVoted: Boolean = false
)

@Entity(tableName = "candidates")
data class CandidateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val party: String,
    val symbol: String, // Identifier for drawable or vector icon
    val photoUrl: String = "", // Base64 or local description
    val voteCount: Int = 0 // Cached sum, though blockchain is source of truth
)

@Entity(tableName = "elections")
data class ElectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startDate: String,
    val endDate: String,
    val status: String // "ACTIVE", "UPCOMING", "COMPLETED"
)

@Entity(tableName = "blockchain")
data class BlockEntity(
    @PrimaryKey(autoGenerate = true) val blockId: Int = 0,
    val index: Int,
    val timestamp: Long,
    val voterHash: String,
    val candidateId: Int,
    val previousHash: String,
    val currentHash: String,
    val nonce: Int
)
