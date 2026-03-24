package com.example.messageapp.ui.chatlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

data class ChatListTopBarState(
    val showHidden: Boolean,
    val title: String
)

@Composable
fun ChatListTopBar(
    state: ChatListTopBarState,
    onMenuClick: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenNewGroup: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var topMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    TopAppBar(
        title = { Text(state.title) },
        actions = {
            IconButton(onClick = { topMenu = true; onMenuClick() }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = topMenu, onDismissRequest = { topMenu = false }) {
                DropdownMenuItem(
                    text = { Text(if (state.showHidden) "Ver conversas ativas" else "Ver conversas Arquivadas") },
                    onClick = { topMenu = false; onMenuClick() }
                )
                DropdownMenuItem(text = { Text("Contatos") }, onClick = {
                    topMenu = false; onOpenContacts()
                })
                DropdownMenuItem(text = { Text("Novo grupo") }, onClick = {
                    topMenu = false; onOpenNewGroup()
                })
                DropdownMenuItem(text = { Text("Meu perfil") }, onClick = {
                    topMenu = false; onOpenProfile()
                })
                DropdownMenuItem(text = { Text("Sair") }, onClick = {
                    topMenu = false; onLogout()
                })
            }
        }
    )
}

@Composable
fun ChatListFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = null)
    }
}
