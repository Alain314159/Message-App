package com.example.messageapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.messageapp.data.AuthRepository
import com.example.messageapp.data.NotificationRepository
import com.example.messageapp.ui.auth.AuthScreen
import com.example.messageapp.ui.chat.ChatScreen
import com.example.messageapp.ui.chatlist.ChatListScreen
import com.example.messageapp.ui.contacts.ContactsScreen
import com.example.messageapp.ui.groups.GroupCreateScreen
import com.example.messageapp.ui.profile.ProfileScreen
import com.example.messageapp.viewmodel.AuthViewModel
import com.onesignal.OneSignal
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authVm = AuthViewModel()
    private val notificationRepo = NotificationRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar OneSignal y obtener Player ID
        initializeOneSignal()
        
        setContent {
            MaterialTheme {
                val nav = rememberNavController()
                RequestPostNotificationsOnce()

                val isLogged by authVm.isLogged.collectAsStateWithLifecycle()
                val initialChatId = remember { intent?.getStringExtra("chatId") }
                var consumedChatId by remember { mutableStateOf(false) }

                // Actualizar estado de autenticación
                LaunchedEffect(Unit) {
                    authVm.init()
                }
                
                // Actualizar presencia cuando cambia el estado de login
                LaunchedEffect(isLogged) {
                    if (isLogged) {
                        authVm.updatePresence(true)
                    }
                }

                NavHost(
                    navController = nav,
                    startDestination = if (isLogged) "home" else "auth"
                ) {
                    composable("auth") {
                        val repo = remember { AuthRepository() }
                        AuthScreen(repo = repo) {
                            // Después de login exitoso, actualizar OneSignal
                            updateOneSignalPlayerId()
                            nav.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    }
                    composable("home") {
                        HomeWrapper(
                            openChat = { id -> nav.navigate("chat/$id") },
                            openContacts = { nav.navigate("contacts") },
                            openNewGroup = { nav.navigate("groupNew") },
                            openProfile = { nav.navigate("profile") },
                            logout = { 
                                authVm.signOut()
                                nav.navigate("auth") { popUpTo(0) }
                            }
                        )
                        LaunchedEffect(isLogged, initialChatId, consumedChatId) {
                            if (isLogged && initialChatId != null && !consumedChatId) {
                                nav.navigate("chat/$initialChatId")
                                consumedChatId = true
                            }
                        }
                    }
                    composable("contacts") {
                        val uid = authVm.currentUserId.value.orEmpty()
                        ContactsScreen(
                            myUid = uid,
                            onOpenChat = { id ->
                                nav.navigate("chat/$id") { popUpTo("contacts") { inclusive = true } }
                            },
                            onBack = { nav.popBackStack() }
                        )
                    }
                    composable("groupNew") {
                        GroupCreateScreen(
                            onCreated = { id ->
                                nav.navigate("chat/$id") {
                                    popUpTo("groupNew") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onCancel = { nav.popBackStack() },
                            onBack   = { nav.popBackStack() }
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            onLoggedOut = {
                                authVm.signOut()
                                nav.navigate("auth") { popUpTo(0) }
                            },
                            onBack = { nav.popBackStack() }
                        )
                    }
                    composable(
                        "chat/{chatId}",
                        arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                    ) { backStack ->
                        val chatId = backStack.arguments?.getString("chatId").orEmpty()
                        val vm: com.example.messageapp.viewmodel.ChatViewModel = viewModel()
                        val myUid = authVm.currentUserId.value.orEmpty()
                        
                        LaunchedEffect(chatId, myUid) {
                            if (myUid.isNotEmpty()) {
                                vm.start(chatId, myUid)
                            }
                        }
                        
                        DisposableEffect(Unit) {
                            onDispose {
                                vm.stop()
                            }
                        }
                        
                        ChatScreen(
                            chatId = chatId,
                            vm = vm,
                            onBack = { nav.popBackStack() },
                            onOpenInfo = { id -> nav.navigate("chatInfo/$id") }
                        )
                    }
                    composable(
                        "chatInfo/{chatId}",
                        arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                    ) { backStack ->
                        val chatId = backStack.arguments?.getString("chatId").orEmpty()
                        com.example.messageapp.ui.chat.ChatInfoScreen(
                            chatId = chatId,
                            onBack = { nav.popBackStack() }
                        )
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
    
    override fun onPause() {
        super.onPause()
        // Marcar como offline cuando la app está en segundo plano
        authVm.updatePresence(false)
    }
    
    override fun onResume() {
        super.onResume()
        // Marcar como online cuando la app está en primer plano
        authVm.updatePresence(true)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Marcar como offline
        authVm.updatePresence(false)
    }
    
    /**
     * Inicializa OneSignal y registra el Player ID
     */
    private fun initializeOneSignal() {
        if (notificationRepo.isOneSignalAvailable()) {
            notificationRepo.initialize(this)
            
            // Obtener Player ID y guardar en Supabase
            updateOneSignalPlayerId()
        }
    }
    
    /**
     * Actualiza el OneSignal Player ID en Supabase
     */
    private fun updateOneSignalPlayerId() {
        if (!notificationRepo.isOneSignalAvailable()) return
        
        lifecycleScope.launch {
            try {
                val playerId = notificationRepo.getPlayerId()
                if (playerId != null) {
                    val authRepo = AuthRepository()
                    authRepo.updateOneSignalPlayerId(playerId)
                    android.util.Log.d("MainActivity", "OneSignal Player ID actualizado: $playerId")
                }
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Error al actualizar OneSignal ID", e)
            }
        }
    }
}

@Composable
private fun HomeWrapper(
    openChat: (String) -> Unit,
    openContacts: () -> Unit,
    openNewGroup: () -> Unit,
    openProfile: () -> Unit,
    logout: () -> Unit
) {
    val vm: com.example.messageapp.viewmodel.ChatListViewModel = viewModel()
    val authVm: com.example.messageapp.viewmodel.AuthViewModel = viewModel()
    val myUid by authVm.currentUserId.collectAsStateWithLifecycle()
    
    LaunchedEffect(myUid) {
        if (!myUid.isNullOrBlank()) {
            vm.start(myUid)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            vm.stop()
        }
    }
    
    ChatListScreen(
        myUid = myUid.orEmpty(),
        vm = vm,
        onOpenChat = openChat,
        onOpenContacts = openContacts,
        onOpenNewGroup = openNewGroup,
        onOpenProfile = openProfile,
        onLogout = logout
    )
}

@Composable
private fun RequestPostNotificationsOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        val perm = Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(perm)
    }
}
