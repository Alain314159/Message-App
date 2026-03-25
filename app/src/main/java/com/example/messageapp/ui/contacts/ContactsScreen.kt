package com.example.messageapp.ui.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.messageapp.data.ContactsRepository
import kotlinx.coroutines.launch

data class ContactItem(val uid: String, val name: String, val photo: String?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    myUid: String,
    onOpenChat: (String) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var items by remember { mutableStateOf(listOf<ContactItem>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val contactsRepo = remember { ContactsRepository() }

    val (deviceContacts, requestPermission) = rememberDeviceContacts()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = { Text("Contactos") }
            )
        }
    ) { insets ->
        // ✅ Cargar contactos desde Supabase
        LaunchedEffect(myUid) {
            isLoading = true
            val result = contactsRepo.listContacts(myUid)
            result.onSuccess { contacts ->
                items = contacts.map { contact ->
                    ContactItem(
                        uid = contact.userId,
                        name = contact.alias.ifBlank { contact.displayName },
                        photo = contact.photoUrl
                    )
                }
            }
            isLoading = false
        }

        val filtered = remember(items, query) {
            val q = query.text.trim()
            if (q.isBlank()) items
            else items.filter { it.name.contains(q, true) || it.uid.contains(q, true) }
        }

        Column(modifier.padding(insets).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = requestPermission) { Text("Importar do aparelho") }
                if (deviceContacts.isNotEmpty()) Text("${deviceContacts.size} importados")
            }
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay contactos")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { contact ->
                        Card(
                            onClick = { onOpenChat(contact.uid) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { 
                                        // TODO: Implementar eliminación de contacto
                                        scope.launch {
                                            // Implementar lógica de eliminación
                                        }
                                    }
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
