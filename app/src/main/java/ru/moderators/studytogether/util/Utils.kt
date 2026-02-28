package ru.moderators.studytogether.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ru.moderators.studytogether.ui.MainViewModel

@Composable
fun <V: View> XmlScreen(
    layoutRes: Int,
    viewModel: MainViewModel = viewModel(),
    navController: NavHostController,
    onViewCreated: (V, NavHostController, MainViewModel) -> Unit = { _, _, _ -> }
) {
    AndroidView(
        factory = { context ->
            LayoutInflater.from(context).inflate(layoutRes, null).apply {
                @Suppress("UNCHECKED_CAST")
                onViewCreated(this as V, navController, viewModel)
            }
        }
    )
}

fun Context.showToast(message: String, duration: Int) {
    Toast.makeText(this, message, duration).show()
}

@Composable
fun rememberToast(): (String, Int) -> Unit {
    val context = LocalContext.current
    return { message, duration ->
        Toast.makeText(context, message, duration).show()
    }
}

@Composable
fun AutoRegisterIfNeeded(
    viewModel: MainViewModel,
    name: String = "Test User",
    email: String = "test@test.com",
    password: String = "test"
) {
    LaunchedEffect(Unit) {
        if (viewModel.currentUser.value == null) {
            viewModel.register(name, email, password)
        }
    }
}

suspend fun <T> safeCall(
    error: (Exception) -> Unit,
    block: suspend () -> T
): T? {
    return try {
        block()
    } catch (e: Exception) {
        error(e)
        null
    }
}