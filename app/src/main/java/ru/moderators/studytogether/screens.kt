@file:OptIn(ExperimentalMaterial3Api::class)

package ru.moderators.studytogether.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ru.moderators.studytogether.api.*
import ru.moderators.studytogether.client.ApiViewModel
import java.text.SimpleDateFormat
import java.util.*

// -------------------- LoginScreen --------------------
@Composable
fun LoginScreen(
    apiViewModel: ApiViewModel,
    onNavigateToMain: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading by apiViewModel.isLoading.collectAsState()
    val currentUser by apiViewModel.currentUser.collectAsState()
    val snackbarMessage by apiViewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser) {
        if (currentUser != null) onNavigateToMain()
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage!!)
            apiViewModel.clearSnackbar()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Имя") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isLoginMode) {
                        Button(
                            onClick = { apiViewModel.login(email, password) },
                            enabled = email.isNotBlank() && password.isNotBlank()
                        ) {
                            Text("Войти")
                        }
                        TextButton(onClick = { isLoginMode = false }) {
                            Text("Нет аккаунта? Зарегистрироваться")
                        }
                    } else {
                        Button(
                            onClick = { apiViewModel.register(name, email, password) },
                            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                        ) {
                            Text("Зарегистрироваться")
                        }
                        TextButton(onClick = { isLoginMode = true }) {
                            Text("Уже есть аккаунт? Войти")
                        }
                    }
                }
            }
        }
    }
}

// -------------------- MainScreen --------------------
@Composable
fun MainScreen(
    apiViewModel: ApiViewModel,
    onNavigateToChats: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMeetings: () -> Unit,
    onNavigateToCreateClaim: () -> Unit,
    onNavigateToMatches: (String) -> Unit,
    onClaimClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val claims by apiViewModel.claims.collectAsState()
    val currentUser by apiViewModel.currentUser.collectAsState()
    val snackbarMessage by apiViewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        apiViewModel.loadAllClaims()
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage!!)
            apiViewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Study Together") },
                actions = {
                    IconButton(onClick = onNavigateToChats) {
                        Icon(Icons.Default.Email, contentDescription = "Чаты")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль")
                    }
                    IconButton(onClick = onNavigateToMeetings) {
                        Icon(Icons.Default.DateRange, contentDescription = "Встречи")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Выход")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateClaim) {
                Icon(Icons.Default.Add, contentDescription = "Создать заявку")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(claims) { claim ->
                ClaimCardWidget(
                    claim = claim,
                    currentUserId = currentUser?.id,
                    onFindMatch = { onNavigateToMatches(claim.id) },
                    onClick = { onClaimClick(claim.userId) }
                )
            }
        }
    }
}

@Composable
fun ClaimCardWidget(
    claim: Claim,
    currentUserId: String?,
    onFindMatch: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (claim.userId == currentUserId)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = when (claim.type) {
                    ClaimType.OFFER -> "Могу помочь"
                    ClaimType.NEED -> "Нужна помощь"
                },
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "${claim.subject} (${claim.grade} класс)",
                style = MaterialTheme.typography.titleMedium
            )
            if (claim.topic.isNotBlank()) {
                Text(text = claim.topic, style = MaterialTheme.typography.bodyMedium)
            }
            if (claim.userId != currentUserId && claim.type == ClaimType.NEED) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onFindMatch, modifier = Modifier.align(Alignment.End)) {
                    Text("Найти пару")
                }
            }
        }
    }
}

// -------------------- CreateClaimScreen --------------------
@Composable
fun CreateClaimScreen(
    apiViewModel: ApiViewModel,
    onBack: () -> Unit
) {
    var type by remember { mutableStateOf(ClaimType.OFFER) }
    var subject by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    val isLoading by apiViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создание заявки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Тип заявки")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = type == ClaimType.OFFER,
                    onClick = { type = ClaimType.OFFER }
                )
                Text("Могу помочь")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = type == ClaimType.NEED,
                    onClick = { type = ClaimType.NEED }
                )
                Text("Нужна помощь")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Предмет") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = grade,
                onValueChange = { grade = it },
                label = { Text("Класс") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Тема (необязательно)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val gradeInt = grade.toIntOrNull() ?: return@Button
                    apiViewModel.createClaim(type, subject, gradeInt, topic)
                    onBack()
                },
                enabled = subject.isNotBlank() && grade.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать")
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

// -------------------- ProfileScreen --------------------
@Composable
fun ProfileScreen(
    apiViewModel: ApiViewModel,
    onBack: () -> Unit
) {
    val currentUser by apiViewModel.currentUser.collectAsState()
    val isLoading by apiViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = currentUser?.name ?: "Неизвестно",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = currentUser?.email ?: "",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Рейтинг")
                    Text(String.format(Locale.ROOT, "%.2f", currentUser?.rating ?: 0.0))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Лайки")
                    Text("${currentUser?.likes ?: 0}")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Назад")
            }
        }
    }
}

@Composable
fun UserProfileScreen(
    userId: String,
    apiViewModel: ApiViewModel,
    onBack: () -> Unit
) {
    val user by apiViewModel.getUser(userId).collectAsState(initial = null)
    val isLoading = user == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль пользователя") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = user!!.name, style = MaterialTheme.typography.headlineMedium)
                    Text(text = user!!.email, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Рейтинг")
                            Text(String.format(Locale.ROOT, "%.2f", user!!.rating))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Лайки")
                            Text("${user!!.likes}")
                        }
                    }
                }
            }
        }
    }
}

// -------------------- MeetingsScreen --------------------
@Composable
fun MeetingsScreen(
    apiViewModel: ApiViewModel,
    onBack: () -> Unit,
    onChat: (String) -> Unit
) {
    val meetings by apiViewModel.meetings.collectAsState()
    val currentUser by apiViewModel.currentUser.collectAsState()
    val isLoading by apiViewModel.isLoading.collectAsState()
    var selectedMeetingForRating by remember { mutableStateOf<Meeting?>(null) }

    LaunchedEffect(Unit) {
        apiViewModel.loadMeetings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои встречи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && meetings.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(meetings) { meeting ->
                        MeetingCard(
                            meeting = meeting,
                            currentUserId = currentUser?.id ?: "",
                            onChatClick = { onChat(meeting.id) },
                            onRateClick = { selectedMeetingForRating = meeting }
                        )
                    }
                }
            }
        }
    }

    selectedMeetingForRating?.let { meeting ->
        RatingDialog(
            meeting = meeting,
            apiViewModel = apiViewModel,
            onDismiss = { selectedMeetingForRating = null }
        )
    }
}

@Composable
fun MeetingCard(
    meeting: Meeting,
    currentUserId: String,
    onChatClick: () -> Unit,
    onRateClick: () -> Unit
) {
    val otherUserId = if (meeting.user1Id == currentUserId) meeting.user2Id else meeting.user1Id
    val date = remember(meeting.dateTime) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(meeting.dateTime))
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("С пользователем: $otherUserId", style = MaterialTheme.typography.titleMedium)
            Text("Дата: $date")
            Text("Тип: ${if (meeting.isOnline) "Онлайн" else "Оффлайн"}")
            if (meeting.location.isNotBlank()) Text("Место: ${meeting.location}")
            Text("Статус: ${meeting.status.name}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (meeting.status == MeetingStatus.PLANNED) {
                    Button(onClick = onChatClick) { Text("Чат") }
                }
                if (meeting.status == MeetingStatus.COMPLETED) {
                    Button(onClick = onRateClick) { Text("Оценить") }
                }
            }
        }
    }
}

// -------------------- ChatScreen --------------------
@Composable
fun ChatScreen(
    meetingId: String,
    apiViewModel: ApiViewModel,
    onBack: () -> Unit
) {
    val messages by apiViewModel.messages.collectAsState()
    val currentUser by apiViewModel.currentUser.collectAsState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(meetingId) {
        apiViewModel.loadMessages(meetingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чат") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true,
                contentPadding = PaddingValues(8.dp)
            ) {
                items(messages.reversed()) { msg ->
                    MessageBubble(msg, currentUser?.id)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введите сообщение") }
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            apiViewModel.sendMessage(meetingId, inputText)
                            inputText = ""
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить")
                }
            }
        }
    }
}

@Composable
fun ChatsScreen(
    apiViewModel: ApiViewModel,
    onBack: () -> Unit,
    onChatClick: (String) -> Unit
) {
    val meetings by apiViewModel.meetings.collectAsState()
    val currentUser by apiViewModel.currentUser.collectAsState()
    val isLoading by apiViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        apiViewModel.loadMeetings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чаты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && meetings.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(meetings.filter { it.status == MeetingStatus.PLANNED }) { meeting ->
                        ChatItem(
                            meeting = meeting,
                            currentUserId = currentUser?.id ?: "",
                            onClick = { onChatClick(meeting.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    meeting: Meeting,
    currentUserId: String,
    onClick: () -> Unit
) {
    val otherUserId = if (meeting.user1Id == currentUserId) meeting.user2Id else meeting.user1Id
    val date = remember(meeting.dateTime) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(meeting.dateTime))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Собеседник: $otherUserId", style = MaterialTheme.typography.titleMedium)
                Text(text = "Встреча: $date", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = if (meeting.isOnline) "🌐" else "📍")
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    currentUserId: String?
) {
    val isMine = message.senderId == currentUserId
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMine)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = message.text, modifier = Modifier.padding(8.dp))
        }
    }
}

// -------------------- MatchesScreen --------------------
@Composable
fun MatchesScreen(
    claimId: String,
    apiViewModel: ApiViewModel,
    onBack: () -> Unit,
    onSchedule: (String, String?, Long) -> Unit // теперь с параметром даты
) {
    val matches by apiViewModel.matches.collectAsState()
    val isLoading by apiViewModel.isLoading.collectAsState()
    var selectedMatch by remember { mutableStateOf<Match?>(null) }

    // Функция для выбора даты и времени
    val picker = rememberDateTimePicker { dateTime ->
        selectedMatch?.let { match ->
            onSchedule(match.matchedUserId, match.claimId, dateTime)
        }
    }

    LaunchedEffect(claimId) {
        apiViewModel.findMatches(claimId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Подходящие пары") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && matches.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matches) { match ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Пользователь: ${match.matchedUserId}")
                                Text("Предмет: ${match.subject}, класс: ${match.grade}")
                                Text("Рейтинг совместимости: ${match.score}")
                                Button(
                                    onClick = {
                                        selectedMatch = match
                                        picker() // открываем выбор даты
                                    }
                                ) {
                                    Text("Назначить встречу")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------- ScheduleMeetingScreen --------------------
@Composable
fun ScheduleMeetingScreen(
    userId: String,
    claimId: String?,
    initialDateTime: Long, // новый параметр
    apiViewModel: ApiViewModel,
    onBack: () -> Unit,
    onScheduled: () -> Unit
) {
    var selectedDateTime by remember { mutableLongStateOf(initialDateTime) } // используем переданную дату
    var isOnline by remember { mutableStateOf(true) }
    var location by remember { mutableStateOf("") }

    // Если нужно, можно убрать кнопку выбора даты, так как дата уже выбрана
    // или оставить возможность изменить

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Планирование встречи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Дата встречи: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(selectedDateTime))}")
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Онлайн")
                Switch(checked = isOnline, onCheckedChange = { isOnline = it })
            }
            if (!isOnline) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Место") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    apiViewModel.createMeeting(
                        user2Id = userId,
                        claimId = claimId,
                        dateTime = selectedDateTime,
                        isOnline = isOnline,
                        location = location
                    )
                    onScheduled()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Подтвердить встречу")
            }
        }
    }
}

// -------------------- RatingDialog --------------------
@Composable
fun RatingDialog(
    meeting: Meeting,
    apiViewModel: ApiViewModel,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val currentUser by apiViewModel.currentUser.collectAsState()
    val targetUserId = if (meeting.user1Id == currentUser?.id) meeting.user2Id else meeting.user1Id

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Оцените встречу") },
        text = {
            Column {
                Text("Оценка (1-5):")
                Row {
                    (1..5).forEach { i ->
                        Button(
                            onClick = { rating = i },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rating == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(i.toString())
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            apiViewModel.incrementLikes(targetUserId)
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Like")
                        Text("Лайк")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    apiViewModel.submitRating(meeting.id, targetUserId, rating, comment)
                    onDismiss()
                },
                enabled = rating in 1..5
            ) {
                Text("Отправить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Позже") }
        }
    )
}

// -------------------- Вспомогательная функция выбора даты --------------------
@Composable
fun rememberDateTimePicker(
    onDateTimeSelected: (Long) -> Unit
): () -> Unit {
    val context = LocalContext.current
    return {
        val calendar = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                android.app.TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        onDateTimeSelected(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}