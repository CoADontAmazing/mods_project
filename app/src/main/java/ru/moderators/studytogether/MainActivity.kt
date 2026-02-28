package ru.moderators.studytogether

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.moderators.studytogether.api.ClaimType
import ru.moderators.studytogether.ui.MainViewModel
import ru.moderators.studytogether.ui.theme.StudyTogetherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyTogetherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = MainViewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    val claims by viewModel.claims.collectAsState()
    val matches by viewModel.matches.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ClaimType.OFFER) }

    Column(modifier = Modifier.padding(16.dp)) {
        if (currentUser == null) {
            // Регистрация
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Имя") })
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            Button(onClick = { viewModel.createUser(name, email) }) {
                Text("Создать пользователя")
            }
        } else {
            Text("Привет, ${currentUser!!.name}")

            Spacer(modifier = Modifier.height(8.dp))

            // Создание заявки
            Row {
                Text("Тип:")
                RadioButton(selected = selectedType == ClaimType.OFFER, onClick = { selectedType = ClaimType.OFFER })
                Text("Могу помочь")
                RadioButton(selected = selectedType == ClaimType.NEED, onClick = { selectedType = ClaimType.NEED })
                Text("Нужна помощь")
            }
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Предмет") })
            OutlinedTextField(value = grade, onValueChange = { grade = it }, label = { Text("Класс") })
            Button(onClick = {
                val g = grade.toIntOrNull() ?: 5
                viewModel.createClaim(selectedType, subject, g)
            }) {
                Text("Создать заявку")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Список всех заявок
            Text("Все заявки:", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(claims) { claim ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("${claim.type}: ${claim.subject} (${claim.grade} класс)")
                            Text("Пользователь: ${claim.userId}")
                            Button(onClick = { viewModel.findMatchesForClaim(claim.id) }) {
                                Text("Найти пару")
                            }
                        }
                    }
                }
            }

            // Результаты мэтчинга
            if (matches.isNotEmpty()) {
                Text("Найдено пар:", style = MaterialTheme.typography.titleMedium)
                matches.forEach { match ->
                    Text("С пользователем ${match.matchedUserId} по предмету ${match.subject}")
                }
            }
        }
    }
}