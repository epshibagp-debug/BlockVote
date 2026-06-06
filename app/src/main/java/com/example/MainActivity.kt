package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VoterAppBackground
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.VotingViewModel
import com.example.ui.viewmodel.VotingViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: VotingViewModel = viewModel(
                    factory = VotingViewModelFactory((application as VotingApplication).repository)
                )
                
                val currentScreen by viewModel.currentScreen.collectAsState()
                val user by viewModel.currentUser.collectAsState()

                // Intercept Android Back Pressed to map navigation seamlessly
                BackHandler(enabled = currentScreen != Screen.Landing) {
                    when (currentScreen) {
                        is Screen.Registration -> viewModel.navigateTo(Screen.Landing)
                        is Screen.Login -> viewModel.navigateTo(Screen.Landing)
                        is Screen.VoterDashboard -> viewModel.logout()
                        is Screen.AdminDashboard -> viewModel.logout()
                        is Screen.Voting -> viewModel.navigateTo(Screen.VoterDashboard)
                        is Screen.BlockchainExplorer -> {
                            if (user?.id == -99) {
                                viewModel.navigateTo(Screen.AdminDashboard)
                            } else {
                                viewModel.navigateTo(Screen.VoterDashboard)
                            }
                        }
                        is Screen.ElectionResults -> {
                            if (user?.id == -99) {
                                viewModel.navigateTo(Screen.AdminDashboard)
                            } else {
                                viewModel.navigateTo(Screen.VoterDashboard)
                            }
                        }
                        is Screen.CandidateManagement -> viewModel.navigateTo(Screen.AdminDashboard)
                        else -> viewModel.navigateTo(Screen.Landing)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    VoterAppBackground(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        BoxWithScreen(
                            currentScreen = currentScreen,
                            viewModel = viewModel,
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BoxWithScreen(
    currentScreen: Screen,
    viewModel: VotingViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()

    // Firewall dynamic router protection
    val guardedScreen = when (currentScreen) {
        is Screen.Landing, is Screen.Registration, is Screen.Login -> currentScreen
        is Screen.AdminDashboard, is Screen.CandidateManagement -> {
            if (currentUser != null && currentUser?.id == -99 && isAdminMode) {
                currentScreen
            } else {
                Screen.Login
            }
        }
        is Screen.VoterDashboard, is Screen.Voting, is Screen.BlockchainExplorer, is Screen.ElectionResults -> {
            if (currentUser != null) {
                currentScreen
            } else {
                Screen.Login
            }
        }
    }

    // Elegant screen dispatcher with strict firewall verification
    when (guardedScreen) {
        is Screen.Landing -> LandingPage(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
        is Screen.Registration -> RegistrationPage(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
        is Screen.Login -> LoginPage(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
        is Screen.VoterDashboard -> VoterDashboard(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
        is Screen.CandidateManagement -> CandidateManagementPage(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
        is Screen.Voting -> VotingPage(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
        is Screen.BlockchainExplorer -> BlockchainExplorerPage(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
        is Screen.ElectionResults -> ElectionResultsPage(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
        is Screen.AdminDashboard -> AdminDashboard(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
    }
}
