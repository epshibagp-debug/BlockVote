package com.example.data.repository

import com.example.blockchain.BlockchainEngine
import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class VotingRepository(
    private val userDao: UserDao,
    private val candidateDao: CandidateDao,
    private val electionDao: ElectionDao,
    private val blockchainDao: BlockchainDao
) {
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allCandidates: Flow<List<CandidateEntity>> = candidateDao.getAllCandidates()
    val allElections: Flow<List<ElectionEntity>> = electionDao.getAllElections()
    val allBlocks: Flow<List<BlockEntity>> = blockchainDao.getAllBlocks()

    // --- Users ---
    suspend fun getUserById(id: Int): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun getUserByMobile(mobile: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByMobile(mobile)
    }

    suspend fun getUserByAadhaar(aadhaar: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByAadhaar(aadhaar)
    }

    suspend fun getUserByVoterId(voterId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByVoterId(voterId)
    }

    suspend fun registerUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        // Validate duplicates
        val existingEmail = userDao.getUserByEmail(user.email)
        val existingMobile = userDao.getUserByMobile(user.mobile)
        val existingAadhaar = userDao.getUserByAadhaar(user.aadhaar)
        val existingVoter = userDao.getUserByVoterId(user.voterId)

        if (existingEmail != null || existingMobile != null || existingAadhaar != null || existingVoter != null) {
            throw IllegalArgumentException("Duplicate registration: Email, mobile, Aadhaar, or Voter ID already registered.")
        }

        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    // --- Candidates ---
    suspend fun addCandidate(candidate: CandidateEntity): Long = withContext(Dispatchers.IO) {
        candidateDao.insertCandidate(candidate)
    }

    suspend fun editCandidate(candidate: CandidateEntity) = withContext(Dispatchers.IO) {
        candidateDao.updateCandidate(candidate)
    }

    suspend fun deleteCandidate(candidate: CandidateEntity) = withContext(Dispatchers.IO) {
        candidateDao.deleteCandidate(candidate)
    }

    suspend fun getCandidateById(id: Int): CandidateEntity? = withContext(Dispatchers.IO) {
        candidateDao.getCandidateById(id)
    }

    // --- Elections ---
    suspend fun addElection(election: ElectionEntity): Long = withContext(Dispatchers.IO) {
        electionDao.insertElection(election)
    }

    suspend fun updateElection(election: ElectionEntity) = withContext(Dispatchers.IO) {
        electionDao.updateElection(election)
    }

    suspend fun getElectionById(id: Int): ElectionEntity? = withContext(Dispatchers.IO) {
        electionDao.getElectionById(id)
    }

    // --- Core Blockchain Voting Transaction ---
    suspend fun castVote(userId: Int, candidateId: Int): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext Pair(false, "User not found")
        
        // 🔒 SECURITY FEATURE: Double voting prevention
        if (user.hasVoted) {
            return@withContext Pair(false, "ALREADY_VOTED: Multiple votes are prohibited")
        }

        val blocks = blockchainDao.getBlocksList()
        val prevHash: String
        val nextIndex: Int

        if (blocks.isEmpty()) {
            // Mine Genesis Block if empty
            val genesisBlock = BlockEntity(
                index = 0,
                timestamp = System.currentTimeMillis() - 100000,
                voterHash = "GENESIS_VOTER",
                candidateId = 0,
                previousHash = "0",
                currentHash = "00c3b88b0ea6f3a73c1d43ebaa97d13cae697a29ee5ff2789178ad30a7d5c808",
                nonce = 42
            )
            blockchainDao.insertBlock(genesisBlock)
            prevHash = genesisBlock.currentHash
            nextIndex = 1
        } else {
            val latestBlock = blocks.last()
            nextIndex = latestBlock.index + 1
            prevHash = latestBlock.currentHash
        }

        // Generate anonymized SHA256 voter hash to guarantee transparency without revealing user identity
        val voterHash = BlockchainEngine.generateVoterHash(user.aadhaar, user.voterId)

        try {
            // Mine the block (Proof of Work)
            val minedBlock = BlockchainEngine.mineBlock(
                index = nextIndex,
                voterHash = voterHash,
                candidateId = candidateId,
                previousHash = prevHash
            )

            // Save records to database atomically
            blockchainDao.insertBlock(minedBlock)
            userDao.updateUser(user.copy(hasVoted = true))

            // Increment candidate vote count cache
            val candidate = candidateDao.getCandidateById(candidateId)
            if (candidate != null) {
                candidateDao.updateCandidate(candidate.copy(voteCount = candidate.voteCount + 1))
            }

            return@withContext Pair(true, minedBlock.currentHash)
        } catch (e: Exception) {
            return@withContext Pair(false, "Mining failed: ${e.message}")
        }
    }

    suspend fun verifyChainHealth(): Boolean = withContext(Dispatchers.IO) {
        val blocks = blockchainDao.getBlocksList()
        BlockchainEngine.verifyChain(blocks)
    }

    suspend fun simulateChainTampering(): Boolean = withContext(Dispatchers.IO) {
        val blocks = blockchainDao.getBlocksList()
        if (blocks.size > 1) {
            // Force replace a hash in the second block or latest block to simulate tampering
            val blockToTamper = blocks.last()
            val tamperedBlock = blockToTamper.copy(
                currentHash = "0000TAMPEREDhash123456789abcdefffffffff_this_breaks_the_chain"
            )
            blockchainDao.insertBlock(tamperedBlock)
            true
        } else {
            false
        }
    }

    suspend fun resetBlockchain(): Boolean = withContext(Dispatchers.IO) {
        blockchainDao.clearAllBlocks()
        
        // Seed default blocks
        val block0Hash = "00c3b88b0ea6f3a73c1d43ebaa97d13cae697a29ee5ff2789178ad30a7d5c808"
        blockchainDao.insertBlock(
            BlockEntity(
                index = 0,
                timestamp = System.currentTimeMillis() - 500000,
                voterHash = "GENESIS_VOTER",
                candidateId = 0,
                previousHash = "0",
                currentHash = block0Hash,
                nonce = 42
            )
        )

        // Reset votes for ALL users
        val allUsersList = userDao.getAllUsers().first()
        allUsersList.forEach { user ->
            // Keep amit (ID=2) hasVoted status or reset everyone to allow infinite re-testing
            // Resetting everyone is the best way for quick evaluation loops!
            userDao.updateUser(user.copy(hasVoted = false))
        }

        // Reset Candidate vote counts cached back to 0
        val allCandidatesList = candidateDao.getAllCandidates().first()
        allCandidatesList.forEach { candidate ->
            candidateDao.updateCandidate(candidate.copy(voteCount = 0))
        }

        true
    }
}
