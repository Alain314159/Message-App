package com.example.messageapp.ui.chatlist
import androidx.compose.material3.ExperimentalMaterial3Api

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class ChatListTopBarState(
    val showHidden: Boolean,
    val title: String
)

/**
 * Data class para parámetros de ChatListTopBar
 * Reduce LongParameterList detekt warning
 */
data class ChatListTopBarParams(
    val state: ChatListTopBarState,
    val onMenuClick: () -> Unit,
    val onOpenContacts: () -> Unit,
    val onOpenNewGroup: () -> Unit,
    val onOpenProfile: () -> Unit,
    val onLogout: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListTopBar(params: ChatListTopBarParams) {
    var topMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    TopAppBar(
        title = { Text(params.state.title) },
        actions = {
            IconButton(onClick = { topMenu = true; params.onMenuClick() }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = topMenu, onDismissRequest = { topMenu = false }) {
                DropdownMenuItem(
                    text = { Text(if (params.state.showHidden) "Ver conversas ativas" else "Ver conversas Arquivadas") },
                    onClick = { topMenu = false; params.onMenuClick() }
                )
                DropdownMenuItem(text = { Text("Contatos") }, onClick = {
                    topMenu = false; params.onOpenContacts()
                })
                DropdownMenuItem(text = { Text("Novo grupo") }, onClick = {
                    topMenu = false; params.onOpenNewGroup()
                })
                DropdownMenuItem(text = { Text("Meu perfil") }, onClick = {
                    topMenu = false; params.onOpenProfile()
                })
                DropdownMenuItem(text = { Text("Sair") }, onClick = {
                    topMenu = false; params.onLogout()
                })
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = null)
    }
}
