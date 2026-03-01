package ru.moderators.studytogether

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.moderators.studytogether.client.ApiViewModel
import ru.moderators.studytogether.ui.screens.*
import ru.moderators.studytogether.ui.theme.StudyTogetherTheme

val Context.dataStore by preferencesDataStore("auth")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StudyTogetherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFDDA0DD) // цвет фона
                ) {
                    val apiViewModel: ApiViewModel = viewModel(factory = ApiViewModel.Factory)
                    AppNavigation(apiViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(apiViewModel: ApiViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf("login") }

    // Загружаем сохранённого пользователя
    LaunchedEffect(Unit) {
        val savedUserId = context.dataStore.data.first()[stringPreferencesKey("user_id")]
        startDestination = if (savedUserId != null) "main" else "login"
    }

    // Пока не определён startDestination, показываем загрузку
    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(apiViewModel) {
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[stringPreferencesKey("user_id")] = apiViewModel.currentUser.value?.id ?: ""
                    }
                }
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        composable("main") {
            MainScreen(
                apiViewModel = apiViewModel,
                onNavigateToChats = { navController.navigate("chats") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToMeetings = { navController.navigate("meetings") },
                onNavigateToCreateClaim = { navController.navigate("create_claim") },
                onNavigateToMatches = { claimId -> navController.navigate("matches/$claimId") },
                onClaimClick = { userId -> navController.navigate("user/$userId") },
                onLogout = {
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences.remove(stringPreferencesKey("user_id"))
                        }
                        apiViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("profile") {
            ProfileScreen(apiViewModel) { navController.popBackStack() }
        }
        composable("meetings") {
            MeetingsScreen(
                apiViewModel = apiViewModel,
                onBack = { navController.popBackStack() },
                onChat = { meetingId -> navController.navigate("chat/$meetingId") }
            )
        }
        composable("create_claim") {
            CreateClaimScreen(apiViewModel) { navController.popBackStack() }
        }
        composable("matches/{claimId}") { backStackEntry ->
            val claimId = backStackEntry.arguments?.getString("claimId") ?: ""
            MatchesScreen(
                claimId = claimId,
                apiViewModel = apiViewModel,
                onBack = { navController.popBackStack() },
                onSchedule = { userId, claimId, dateTime ->
                    navController.navigate("schedule/$userId/${claimId ?: "null"}/$dateTime")
                }
            )
        }
        composable("chat/{meetingId}") { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: ""
            ChatScreen(meetingId, apiViewModel) { navController.popBackStack() }
        }
        composable("schedule/{userId}/{claimId}/{dateTime}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val claimId = backStackEntry.arguments?.getString("claimId")?.takeIf { it != "null" }
            val dateTime = backStackEntry.arguments?.getString("dateTime")?.toLongOrNull() ?: 0L
            ScheduleMeetingScreen(
                userId = userId,
                claimId = claimId,
                initialDateTime = dateTime,
                apiViewModel = apiViewModel,
                onBack = { navController.popBackStack() },
                onScheduled = {
                    navController.navigate("meetings") {
                        popUpTo("schedule") { inclusive = true }
                    }
                }
            )
        }
        composable("user/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserProfileScreen(
                userId = userId,
                apiViewModel = apiViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("chats") {
            ChatsScreen(
                apiViewModel = apiViewModel,
                onBack = { navController.popBackStack() },
                onChatClick = { meetingId -> navController.navigate("chat/$meetingId") }
            )
        }
    }
}