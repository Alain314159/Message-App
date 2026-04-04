package com.example.messageapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.messageapp.data.AuthRepository
import com.example.messageapp.ui.auth.AuthScreen
import com.example.messageapp.ui.avatar.AvatarPickerScreen
import com.example.messageapp.ui.chat.ChatScreen
import com.example.messageapp.ui.chatlist.ChatListScreen
import com.example.messageapp.ui.chatlist.ChatListScreenParams
import com.example.messageapp.ui.contacts.ContactsScreen
import com.example.messageapp.ui.groups.GroupCreateScreen
import com.example.messageapp.ui.profile.ProfileScreen
import com.example.messageapp.viewmodel.AuthViewModel
import com.example.messageapp.viewmodel.AvatarViewModel
import com.example.messageapp.viewmodel.ChatListViewModel
import com.example.messageapp.viewmodel.ChatViewModel

/**
 * Host de navegación principal de la aplicación
 *
 * Responsabilidad única: Gestionar toda la navegación y routing de la app
 *
 * Destinos:
 * - auth: Pantalla de autenticación
 * - home: Lista de chats
 * - contacts: Contactos del dispositivo
 * - groupNew: Crear nuevo grupo
 * - profile: Perfil del usuario
 * - avatar-picker: Selector de avatar
 * - chat/{chatId}: Chat individual
 * - chatInfo/{chatId}: Información del chat
 */
@Composable
fun AppNavigationHost(
    authVm: AuthViewModel,
    isLogged: Boolean,
    initialChatId: String?
) {
    val nav = rememberNavController()
    var consumedChatId by remember { mutableStateOf(false) }

    // Inicializar AuthViewModel
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
        // ============================================
        // AUTH SCREEN
        // ============================================
        composable("auth") {
            AuthRoute(
                onLoginSuccess = {
                    nav.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        // ============================================
        // HOME SCREEN (Chat List)
        // ============================================
        composable("home") {
            HomeRoute(
                authVm = authVm,
                openChat = { id -> nav.navigate("chat/$id") },
                openContacts = { nav.navigate("contacts") },
                openNewGroup = { nav.navigate("groupNew") },
                openProfile = { nav.navigate("profile") },
                logout = {
                    authVm.signOut()
                    nav.navigate("auth") { popUpTo(0) }
                }
            )

            // Navegar al chat inicial si viene de notificación
            LaunchedEffect(isLogged, initialChatId, consumedChatId) {
                if (isLogged && initialChatId != null && !consumedChatId) {
                    nav.navigate("chat/$initialChatId")
                    consumedChatId = true
                }
            }
        }

        // ============================================
        // CONTACTS SCREEN
        // ============================================
        composable("contacts") {
            val uid = authVm.currentUserId.value.orEmpty()
            ContactsScreen(
                myUid = uid,
                onOpenChat = { id ->
                    nav.navigate("chat/$id") {
                        popUpTo("contacts") { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        // ============================================
        // NEW GROUP SCREEN
        // ============================================
        composable("groupNew") {
            GroupCreateScreen(
                onCreated = { id ->
                    nav.navigate("chat/$id") {
                        popUpTo("groupNew") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCancel = { nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }

        // ============================================
        // PROFILE SCREEN
        // ============================================
        composable("profile") {
            ProfileScreen(
                onLoggedOut = {
                    authVm.signOut()
                    nav.navigate("auth") { popUpTo(0) }
                },
                onBack = { nav.popBackStack() },
                onOpenProfile = {
                    nav.navigate("avatar-picker")
                }
            )
        }

        // ============================================
        // AVATAR PICKER SCREEN
        // ============================================
        composable("avatar-picker") {
            val avatarVm: AvatarViewModel = viewModel()
            val userId = authVm.currentUserId.value.orEmpty()

            AvatarPickerScreen(
                viewModel = avatarVm,
                userId = userId,
                onAvatarSelected = { nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }

        // ============================================
        // CHAT SCREEN
        // ============================================
        composable(
            "chat/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStack ->
            val chatId = backStack.arguments?.getString("chatId").orEmpty()
            val chatVm: ChatViewModel = viewModel()
            val myUid = authVm.currentUserId.value.orEmpty()

            LaunchedEffect(chatId, myUid) {
                if (myUid.isNotEmpty()) {
                    chatVm.start(chatId, myUid)
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    chatVm.stop()
                }
            }

            ChatScreen(
                chatId = chatId,
                vm = chatVm,
                onBack = { nav.popBackStack() },
                onOpenInfo = { id -> nav.navigate("chatInfo/$id") }
            )
        }

        // ============================================
        // CHAT INFO SCREEN
        // ============================================
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

// ============================================
// AUTH ROUTE
// ============================================

/**
 * Route composable para autenticación
 */
@Composable
private fun AuthRoute(
    onLoginSuccess: () -> Unit
) {
    val repo = remember { AuthRepository() }
    AuthScreen(repo = repo) {
        onLoginSuccess()
    }
}

// ============================================
// HOME ROUTE
// ============================================

/**
 * Route composable para la pantalla principal
 */
@Composable
private fun HomeRoute(
    authVm: AuthViewModel,
    openChat: (String) -> Unit,
    openContacts: () -> Unit,
    openNewGroup: () -> Unit,
    openProfile: () -> Unit,
    logout: () -> Unit
) {
    val chatListVm: ChatListViewModel = viewModel()
    val myUid by authVm.currentUserId.collectAsState()

    LaunchedEffect(myUid) {
        if (!myUid.isNullOrBlank()) {
            chatListVm.start(myUid)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            chatListVm.stop()
        }
    }

    ChatListScreen(
        ChatListScreenParams(
            myUid = myUid.orEmpty(),
            vm = chatListVm,
            onOpenChat = openChat,
            onOpenContacts = openContacts,
            onOpenNewGroup = openNewGroup,
            onOpenProfile = openProfile,
            onLogout = logout
        )
    )
}
