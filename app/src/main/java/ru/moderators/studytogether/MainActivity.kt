package ru.moderators.studytogether

import ru.moderators.studytogether.ui.theme.StudyTogetherTheme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StudyTogetherTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "first",
        modifier = modifier
    ) {
        composable("first") {
            FirstScreen { name, email ->
                // Передаём данные на второй экран через аргументы
                navController.navigate("second/$name/$email")
            }
        }
        composable("second/{name}/{email}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            SecondScreen(name = name, email = email) {
                navController.popBackStack() // возврат назад
            }
        }
    }
}

@Composable
fun FirstScreen(onNavigate: (String, String) -> Unit) {
    // Ваш UI для первого экрана, например:
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    Column {
        TextField(value = name, onValueChange = { name = it })
        TextField(value = email, onValueChange = { email = it })
        Button(onClick = { onNavigate(name, email) }) {
            Text("Перейти")
        }
    }
}

@Composable
fun SecondScreen(name: String, email: String, onBack: () -> Unit) {
    Column {
        Text("Имя: $name")
        Text("Email: $email")
        Button(onClick = onBack) {
            Text("Назад")
        }
    }
}