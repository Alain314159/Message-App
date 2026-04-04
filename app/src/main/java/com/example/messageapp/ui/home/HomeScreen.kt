package com.example.messageapp.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messageapp.ui.chatlist.ChatListScreen
import com.example.messageapp.ui.chatlist.ChatListScreenParams
import com.example.messageapp.ui.contacts.ContactsScreen
import com.example.messageapp.ui.profile.ProfileScreen
import com.example.messageapp.viewmodel.AuthViewModel
import com.example.messageapp.viewmodel.ChatListViewModel

@Composable
fun HomeScreen(
    onOpenChat: (String) -> Unit,
    onLoggedOut: () -> Unit,
    onOpenNewGroup: () -> Unit = {},
    onOpenContacts: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    var tab by remember { mutableStateOf(0) }
    
    // ✅ Usar AuthViewModel en lugar de FirebaseAuth
    val authVm: AuthViewModel = remember { AuthViewModel() }
    val myUid by authVm.currentUserId.collectAsStateWithLifecycle()
    val safeUid = myUid.orEmpty()

    val listVm = remember { ChatListViewModel() }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                    label = { Text("Chats") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Contacts, contentDescription = null) },
                    label = { Text("Contactos") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { insets ->
        when (tab) {
            0 -> ChatListScreen(
                ChatListScreenParams(
                    myUid = safeUid,
                    vm = listVm,
                    onOpenChat = onOpenChat,
                    onOpenProfile = onOpenProfile,
                    onOpenNewGroup = onOpenNewGroup,
                    onOpenContacts = { /* TODO */ },
                    onLogout = onLoggedOut
                )
            )
            1 -> ContactsScreen(
                myUid = safeUid,
                onOpenChat = onOpenChat,
                onBack = {}
            )
            2 -> ProfileScreen(
                onLoggedOut = onLoggedOut,
                onBack = {}
            )
        }
    }
}
