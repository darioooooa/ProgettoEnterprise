import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.app.Application
import com.example.enterprisemobile.data.api.RetrofitClient
import com.example.enterprisemobile.data.security.SessionManager
import com.example.enterprisemobile.data.service.ServizioChat
import com.example.enterprisemobile.viewmodels.ChatViewModel

class ChatViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {

            val sessionManager = SessionManager(application)
            val token = sessionManager.ottieniTokenAccesso() ?: ""


            val servizioChat = ServizioChat(token)
            val chiamateApiChat = RetrofitClient.ottieniChatService(application)

            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(servizioChat, chiamateApiChat) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}