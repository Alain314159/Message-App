// app/src/main/java/com/example/messageapp/ui/chatlist/ChatsTab.kt
package com.example.messageapp.ui.chatlist

import androidx.compose.runtime.Composable
import com.example.messageapp.viewmodel.ChatListViewModel

/**
 * Data class para parámetros de ChatsTab
 * Reduce LongParameterList detekt warning
 */
data class ChatsTabParams(
    val myUid: String,
    val vm: ChatListViewModel,
    val onOpenChat: (String) -> Unit,
    val onOpenContacts: () -> Unit = {},
    val onOpenNewGroup: () -> Unit = {},
    val onOpenProfile: () -> Unit = {},
    val onLogout: () -> Unit = {}
)

@Composable
fun ChatsTab(params: ChatsTabParams) {
    ChatListScreen(
        ChatListScreenParams(
            myUid = params.myUid,
            vm = params.vm,
            onOpenChat = params.onOpenChat,
            onOpenContacts = params.onOpenContacts,
            onOpenNewGroup = params.onOpenNewGroup,
            onOpenProfile = params.onOpenProfile,
            onLogout = params.onLogout
        )
    )
}
