package ru.moderators.studytogether

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ru.moderators.studytogether.client.ApiViewModel

@Composable
fun <V: View> XmlScreen(
    modifier: Modifier = Modifier,
    layoutRes: Int,
    viewModel: ApiViewModel = viewModel(),
    navController: NavHostController,
    onViewCreated: (V, NavHostController, ApiViewModel) -> Unit = { _, _, _ -> }
) {
    AndroidView(
        modifier = modifier,
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