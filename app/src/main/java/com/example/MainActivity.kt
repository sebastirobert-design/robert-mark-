package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavDestination
import com.example.ui.dialogs.UserGuideDialog
import com.example.ui.screens.CertificateScreen
import com.example.ui.screens.ConsolidationScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarksEntryScreen
import com.example.ui.screens.ParentLetterScreen
import com.example.ui.screens.SchoolSettingsScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.viewmodel.SchoolMarksViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SchoolMarksViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: SchoolMarksViewModel) {
    var currentDestination by remember { mutableStateOf(AppNavDestination.DASHBOARD) }
    var showUserGuideDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val uiMessage by viewModel.uiMessage.collectAsState()
    val schoolProfile by viewModel.schoolProfile.collectAsState()

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text(
                            text = currentDestination.titleTamil,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (schoolProfile.udiseCode.isNotBlank()) {
                                "${schoolProfile.schoolName} (UDISE: ${schoolProfile.udiseCode})"
                            } else {
                                schoolProfile.schoolName.ifEmpty { "மதிப்பெண் பதிவேடு" }
                            },
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    if (currentDestination != AppNavDestination.DASHBOARD) {
                        IconButton(onClick = { currentDestination = AppNavDestination.DASHBOARD }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "பின்செல்",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showUserGuideDialog = true },
                        modifier = Modifier.testTag("nav_user_guide_btn")
                    ) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = "பயனர் வழிகாட்டி",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { currentDestination = AppNavDestination.SETTINGS },
                        modifier = Modifier.testTag("nav_settings_btn")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "பள்ளி அமைப்புகள்",
                            tint = if (currentDestination == AppNavDestination.SETTINGS) AmberGold else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp
            ) {
                val navItems = listOf(
                    AppNavDestination.DASHBOARD,
                    AppNavDestination.STUDENTS,
                    AppNavDestination.MARKS_ENTRY,
                    AppNavDestination.CONSOLIDATION,
                    AppNavDestination.CERTIFICATE
                )

                navItems.forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.titleTamil,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = dest.titleTamil,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = NavyPrimary.copy(alpha = 0.12f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (currentDestination) {
            AppNavDestination.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onNavigate = { currentDestination = it },
                modifier = Modifier.padding(innerPadding)
            )
            AppNavDestination.STUDENTS -> StudentsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppNavDestination.MARKS_ENTRY -> MarksEntryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppNavDestination.CONSOLIDATION -> ConsolidationScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppNavDestination.CERTIFICATE -> CertificateScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppNavDestination.PARENT_LETTERS -> ParentLetterScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppNavDestination.SETTINGS -> SchoolSettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showUserGuideDialog) {
        UserGuideDialog(onDismiss = { showUserGuideDialog = false })
    }
}
