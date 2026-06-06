package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blockchain.BlockchainEngine
import com.example.data.database.BlockEntity
import com.example.data.database.CandidateEntity
import com.example.data.database.UserEntity
import com.example.data.repository.VotingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Landing : Screen()
    object Registration : Screen()
    object Login : Screen()
    object VoterDashboard : Screen()
    object CandidateManagement : Screen()
    object Voting : Screen()
    object BlockchainExplorer : Screen()
    object ElectionResults : Screen()
    object AdminDashboard : Screen()
}

class VotingViewModel(private val repository: VotingRepository) : ViewModel() {

    // --- State Managers ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Landing)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _blockchainHealth = MutableStateFlow(true)
    val blockchainHealth: StateFlow<Boolean> = _blockchainHealth.asStateFlow()

    // Status messages (toast / bottom sheet triggers)
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // OTP verification flows
    private val _pendingRegistration = MutableStateFlow<UserEntity?>(null)
    val pendingRegistration: StateFlow<UserEntity?> = _pendingRegistration.asStateFlow()

    private val _otpDialogVisible = MutableStateFlow(false)
    val otpDialogVisible: StateFlow<Boolean> = _otpDialogVisible.asStateFlow()

    private val _simulatedOtpCode = MutableStateFlow("")
    val simulatedOtpCode: StateFlow<String> = _simulatedOtpCode.asStateFlow()

    // Loading states
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // --- Flows from DB auto-syncing into Jetpack Compose screens ---
    val candidates: StateFlow<List<CandidateEntity>> = repository.allCandidates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blocks: StateFlow<List<BlockEntity>> = repository.allBlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically check blockchain health on launch
        checkChainValidity()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _errorMessage.value = null
        _statusMessage.value = null
    }

    fun logout() {
        _currentUser.value = null
        _isAdminMode.value = false
        navigateTo(Screen.Landing)
    }

    fun clearStatusMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }

    // --- Authentication ---
    fun register(
        name: String,
        email: String,
        mobile: String,
        aadhaar: String,
        voterId: String,
        password: String
    ) {
        if (name.isBlank() || email.isBlank() || mobile.isBlank() || aadhaar.isBlank() || voterId.isBlank() || password.isBlank()) {
            _errorMessage.value = "All fields are strictly required."
            return
        }

        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        if (!email.matches(emailRegex)) {
            _errorMessage.value = "Registration Error: Please enter a valid email address (e.g., user@domain.com)."
            return
        }

        if (aadhaar.length != 12 || !aadhaar.all { it.isDigit() }) {
            _errorMessage.value = "Aadhaar must be a valid 12-digit numeric code."
            return
        }

        if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
            _errorMessage.value = "Mobile number must be exactly 10 digits."
            return
        }

        if (password.length < 8) {
            _errorMessage.value = "Secure Password Protocol: Password must be at least 8 characters long."
            return
        }
        if (!password.any { it.isUpperCase() }) {
            _errorMessage.value = "Secure Password Protocol: Password must contain at least one uppercase letter."
            return
        }
        if (!password.any { it.isDigit() }) {
            _errorMessage.value = "Secure Password Protocol: Password must contain at least one digit."
            return
        }

        _isProcessing.value = true
        viewModelScope.launch {
            try {
                // Pre-check duplicates in background
                val emailOk = repository.getUserByEmail(email)
                val phoneOk = repository.getUserByMobile(mobile)
                val aadhaarOk = repository.getUserByAadhaar(aadhaar)
                val voterIdOk = repository.getUserByVoterId(voterId)

                if (emailOk != null || phoneOk != null || aadhaarOk != null || voterIdOk != null) {
                    _errorMessage.value = "Registration Error: User credentials (Email, Phone, Aadhaar, or Voter ID) already exist."
                    _isProcessing.value = false
                    return@launch
                }

                // Prepare entity & trigger simulated OTP verification
                val passwordHash = BlockchainEngine.sha256(password)
                val newUser = UserEntity(
                    name = name,
                    email = email,
                    mobile = mobile,
                    aadhaar = aadhaar,
                    voterId = voterId,
                    passwordHash = passwordHash,
                    hasVoted = false
                )

                _pendingRegistration.value = newUser
                // Generate secure simulated 6-digit OTP code to present to user
                _simulatedOtpCode.value = (100000..999999).random().toString()
                _otpDialogVisible.value = true
                _isProcessing.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred during validation."
                _isProcessing.value = false
            }
        }
    }

    fun confirmOtp(enteredOtp: String) {
        if (enteredOtp != _simulatedOtpCode.value) {
            _errorMessage.value = "Incorrect OTP code. Please trace the verified notification alert."
            return
        }

        val user = _pendingRegistration.value ?: return
        _isProcessing.value = true
        _otpDialogVisible.value = false

        viewModelScope.launch {
            try {
                repository.registerUser(user)
                _statusMessage.value = "Voter Registered successfully! Please login with your credentials."
                navigateTo(Screen.Login)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Registration failed."
            } finally {
                _isProcessing.value = false
                _pendingRegistration.value = null
            }
        }
    }

    fun dismissOtpDialog() {
        _otpDialogVisible.value = false
        _pendingRegistration.value = null
    }

    fun loginWithPassword(emailOrMobile: String, pass: String) {
        if (emailOrMobile.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please fill in all security fields."
            return
        }

        // Demo shortcut: admin logic
        if (emailOrMobile.lowercase() == "admin" && pass == "admin123") {
            _isAdminMode.value = true
            // Generate dummy user record representing the election officer
            _currentUser.value = UserEntity(
                id = -99,
                name = "Chief Election Officer (Admin)",
                email = "admin@election.gov.in",
                mobile = "9999999999",
                aadhaar = "000000000000",
                voterId = "CEO9999",
                passwordHash = "",
                hasVoted = false
            )
            _statusMessage.value = "Logged in securely as Election Officer"
            navigateTo(Screen.AdminDashboard)
            return
        }

        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val hashed = BlockchainEngine.sha256(pass)
                // Search by email first
                var user = repository.getUserByEmail(emailOrMobile)
                if (user == null) {
                    // Search by mobile
                    user = repository.getUserByMobile(emailOrMobile)
                }
                if (user == null) {
                    // Search by Voter ID
                    user = repository.getUserByVoterId(emailOrMobile)
                }

                if (user != null && user.passwordHash == hashed) {
                    _currentUser.value = user
                    _isAdminMode.value = false
                    _statusMessage.value = "Voter authenticated successfully!"
                    navigateTo(Screen.VoterDashboard)
                } else {
                    _errorMessage.value = "Invalid login credentials. Please audit inputs or use pre-seeded account info."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Authentication error: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // --- Core Voting Process ---
    fun castVote(candidateId: Int) {
        val user = _currentUser.value
        if (user == null) {
            _errorMessage.value = "Session expired. Please log back in."
            navigateTo(Screen.Login)
            return
        }

        _isProcessing.value = true
        viewModelScope.launch {
            val result = repository.castVote(user.id, candidateId)
            _isProcessing.value = false

            if (result.first) {
                // Refresh local current user object to reflect the 'hasVoted = true' state change
                val updatedUser = repository.getUserById(user.id)
                _currentUser.value = updatedUser
                _statusMessage.value = "Voted Successfully! Block mined & appended to security ledger."
                checkChainValidity() // Automatically verify integrity following a write
                navigateTo(Screen.VoterDashboard)
            } else {
                _errorMessage.value = "Vote Rejected: ${result.second}"
            }
        }
    }

    // --- Candidate Management (Admin Action) ---
    fun addCandidate(name: String, party: String, symbol: String) {
        if (name.isBlank() || party.isBlank() || symbol.isBlank()) {
            _errorMessage.value = "All fields are required to seed candidates."
            return
        }

        viewModelScope.launch {
            repository.addCandidate(
                CandidateEntity(
                    name = name,
                    party = party,
                    symbol = symbol
                )
            )
            _statusMessage.value = "Candidate appended to ballot sheet successfully!"
        }
    }

    fun deleteCandidate(candidate: CandidateEntity) {
        viewModelScope.launch {
            repository.deleteCandidate(candidate)
            _statusMessage.value = "Candidate removed from active ballot paper."
        }
    }

    // --- Blockchain Audits & Security ---
    fun checkChainValidity() {
        viewModelScope.launch {
            val status = repository.verifyChainHealth()
            _blockchainHealth.value = status
        }
    }

    fun simulateChainTampering() {
        viewModelScope.launch {
            val success = repository.simulateChainTampering()
            if (success) {
                _blockchainHealth.value = false
                _errorMessage.value = "CRITICAL ALERT: Blockchain has detected data tampering! Ledger integrity broken."
                checkChainValidity()
            } else {
                _errorMessage.value = "Tampering failed. Build a larger block list first by casting votes."
            }
        }
    }

    fun resetAndRepairChain() {
        _isProcessing.value = true
        viewModelScope.launch {
            repository.resetBlockchain()
            
            // Refresh logged-in user if necessary
            val user = _currentUser.value
            if (user != null && user.id > 0) {
                _currentUser.value = repository.getUserById(user.id)
            }

            _blockchainHealth.value = true
            _statusMessage.value = "Elections blockchain fully repaired, reset, and re-synchronized."
            _isProcessing.value = false
        }
    }
}

class VotingViewModelFactory(private val repository: VotingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VotingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VotingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
