package ru.moderators.studytogether

import ru.moderators.studytogether.ui.theme.StudyTogetherTheme

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import ru.moderators.studytogether.ui.MainViewModel
import ru.moderators.studytogether.util.XmlScreen
import ru.moderators.studytogether.util.showToast

val model: MainViewModel = MainViewModel()
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

    LaunchedEffect(Unit) {
        model.register("admin", "admin", "admin")
    }

    NavHost(
        navController = navController,
        startDestination = "home_page",
        modifier = modifier
    ) {
        composable("home_page") {
            HomeScreen()
        }
        /*
        composable("first") {
            FirstScreen { name, email ->
                // Передаём данные на второй экран через аргументы
                navController.navigate("second/$name/$email")
            }
        }
        composable("second/{username}/{email}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("username") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            SecondScreen(name = name, email = email) {
                //navController.popBackStack() // возврат назад
                navController.navigate("first")
            }
        }
        composable("second/{username}/{email}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("username") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            SecondScreen(name = name, email = email) {
                //navController.popBackStack() // возврат назад
                navController.navigate("first")
            }
        }*/
    }
}

@Composable
fun HomeScreen() {
    XmlScreen<ConstraintLayout>(R.layout.home_page, model, navController = rememberNavController()) { view, controller, model ->
        view.addView(Button(view.context).apply {
            text = "Тестовая кнопка"
            setOnClickListener { it ->
                context.showToast("On Button Click", Toast.LENGTH_SHORT)
            }
        })
    }
}


@Composable
fun FirstScreen(onNavigate: (String, String) -> Unit) {
    // Ваш UI для первого экрана, например:
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    /*Column {
        TextField(value = username, onValueChange = { username = it })
        TextField(value = email, onValueChange = { email = it })
        Button(onClick = { onNavigate(username, email) }) {
            Text("Перейти")
        }
    }*/
    /*AndroidView(
        factory = { context ->
            XmlView(context, R.layout.perehod) {
                findViewById<Button>(R.id.back_to_lenta).setOnClickListener { _ ->
                    print("Start")

                    val msg = model.claims.value.stream()
                        .map {
                            ("API $it")
                        }.collect(Collectors.toList()).toString()

                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

                    onNavigate("7", msg)
                }
            }
        }
    )*/
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