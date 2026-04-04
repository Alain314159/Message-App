package com.example.messageapp.ui.chat
import androidx.compose.animation.core.RepeatMode

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.runtime.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.messageapp.model.Message
import com.example.messageapp.model.MessageStatus
import com.example.messageapp.ui.theme.GrisKoala
import com.example.messageapp.ui.theme.RosaChanchita

// Tag constante para logging
private const val TAG = "MessageApp.ChatComponents"

// ============================================================================
// AUTOR DEL MENSAJE (Author Row)
// ============================================================================

@Composable
fun MessageAuthorRow(isMine: Boolean, authorName: String?, authorPhoto: String?, senderId: String) {
    val ctx = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = ImageRequest.Builder(ctx).data(authorPhoto).build(), contentDescription = null, modifier = Modifier.size(22.dp).clip(CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(text = if (isMine) "Tú" else (authorName ?: "@${senderId.take(6)}"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ============================================================================
// CONTENIDO MULTIMEDIA (Image, Video, Audio, File)
// ============================================================================

@Composable
fun MessageMediaContent(m: Message) {
    val ctx = LocalContext.current
    when (m.type) {
        "image" -> {
            if (!m.mediaUrl.isNullOrBlank()) {
                AsyncImage(model = ImageRequest.Builder(ctx).data(m.mediaUrl).build(), contentDescription = "Imagen", modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 260.dp))
            } else { Text("[imagen]", color = MaterialTheme.colorScheme.onSurface) }
        }
        "video", "audio", "file" -> {
            TextButton(onClick = {
                try {
                    if (!m.mediaUrl.isNullOrBlank()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(m.mediaUrl))
                        ctx.startActivity(intent)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open media: ${m.type}", e)
                }
            }) {
                Icon(imageVector = when (m.type) { "video" -> Icons.Outlined.VideoFile; "audio" -> Icons.Outlined.Mic; else -> Icons.Outlined.AttachFile }, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Abrir ${m.type}")
            }
        }
        else -> Text("[archivo]", color = MaterialTheme.colorScheme.onSurface)
    }
}

// ============================================================================
// HIGHLIGHT DE BÚSQUEDA
// ============================================================================

@Composable
fun buildHighlighted(text: String, query: String): AnnotatedString {
    if (query.isBlank() || text.isBlank()) return AnnotatedString(text)
    val lower = text.lowercase()
    val q = query.lowercase()
    val b = AnnotatedString.Builder()
    var i = 0
    while (i < text.length) {
        val found = lower.indexOf(q, startIndex = i)
        if (found < 0) { b.append(text.substring(i)); break }
        if (found > i) b.append(text.substring(i, found))
        b.pushStyle(SpanStyle(background = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f), fontWeight = FontWeight.SemiBold))
        b.append(text.substring(found, found + query.length))
        b.pop()
        i = found + query.length
    }
    return b.toAnnotatedString()
}

// ============================================================================
// INDICADORES DE ESTADO (Delivery Ticks, Status)
// ============================================================================

@Composable
fun DeliveryTicks(m: Message) {
    // ✅ CORREGIDO: deliveredAt y readAt son Long? (timestamp), no listas
    val delivered = m.deliveredAt != null
    val read = m.readAt != null
    val hasTicks = delivered || read
    if (hasTicks) {
        val status = when {
            read -> MessageStatus.READ
            delivered -> MessageStatus.DELIVERED
            else -> MessageStatus.SENT
        }
        MessageStatusIndicator(status = status, isMine = true)
    }
}

@Composable
fun MessageStatusIndicator(status: MessageStatus, isMine: Boolean, modifier: Modifier = Modifier) {
    if (!isMine) return
    Row(modifier = modifier) {
        when (status) {
            MessageStatus.SENT -> Icon(Icons.Default.Check, "Enviado", tint = GrisKoala, modifier = Modifier.size(16.dp))
            MessageStatus.DELIVERED -> {
                Icon(Icons.Default.Check, "Entregado", tint = GrisKoala, modifier = Modifier.size(16.dp))
                Icon(Icons.Default.Check, null, tint = GrisKoala, modifier = Modifier.size(16.dp).offset(x = (-6).dp))
            }
            MessageStatus.READ -> {
                Icon(Icons.Default.Check, "Leído", tint = RosaChanchita, modifier = Modifier.size(16.dp))
                Icon(Icons.Default.Check, null, tint = RosaChanchita, modifier = Modifier.size(16.dp).offset(x = (-6).dp))
            }
        }
    }
}

@Composable
fun DoubleCheckIndicator(isRead: Boolean, modifier: Modifier = Modifier) {
    val tickColor = if (isRead) RosaChanchita else GrisKoala
    Row(modifier = modifier) {
        Icon(Icons.Default.Check, contentDescription = if (isRead) "Leído" else "Entregado", tint = tickColor, modifier = Modifier.size(14.dp))
        Icon(Icons.Default.Check, contentDescription = null, tint = tickColor, modifier = Modifier.size(14.dp).offset(x = (-5).dp))
    }
}

// ============================================================================
// INDICADORES DE ESCRITURA (Typing)
// ============================================================================

@Composable
fun TypingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dot1Alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, easing = EaseInOut), repeatMode = RepeatMode.Reverse), label = "dot1")
    val dot2Alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 200, easing = EaseInOut), repeatMode = RepeatMode.Reverse), label = "dot2")
    val dot3Alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 400, easing = EaseInOut), repeatMode = RepeatMode.Reverse), label = "dot3")
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = ".", style = MaterialTheme.typography.titleLarge, color = RosaChanchita, modifier = Modifier.graphicsLayer { alpha = dot1Alpha })
        Text(text = ".", style = MaterialTheme.typography.titleLarge, color = RosaChanchita, modifier = Modifier.graphicsLayer { alpha = dot2Alpha })
        Text(text = ".", style = MaterialTheme.typography.titleLarge, color = RosaChanchita, modifier = Modifier.graphicsLayer { alpha = dot3Alpha })
    }
}

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dot1Alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, easing = EaseInOut), repeatMode = RepeatMode.Reverse), label = "dot1")
    val dot2Alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 200, easing = EaseInOut), repeatMode = RepeatMode.Reverse), label = "dot2")
    val dot3Alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = 400, easing = EaseInOut), repeatMode = RepeatMode.Reverse), label = "dot3")
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Escribiendo", style = MaterialTheme.typography.bodySmall, color = RosaChanchita, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = ".", style = MaterialTheme.typography.bodySmall, color = RosaChanchita, modifier = Modifier.graphicsLayer { alpha = dot1Alpha })
        Text(text = ".", style = MaterialTheme.typography.bodySmall, color = RosaChanchita, modifier = Modifier.graphicsLayer { alpha = dot2Alpha })
        Text(text = ".", style = MaterialTheme.typography.bodySmall, color = RosaChanchita, modifier = Modifier.graphicsLayer { alpha = dot3Alpha })
    }
}

// ============================================================================
// BARRA DE MENSAJE FIJADO
// ============================================================================

@Composable
fun PinnedMessageBar(snippet: String?, onRemove: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0x11000000)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.PushPin, contentDescription = null)
        Spacer(Modifier.width(6.dp))
        Text("Fixada: $snippet")
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onRemove) { Text("Remover") }
    }
}

// ============================================================================
// NAVEGACIÓN DE BÚSQUEDA
// ============================================================================

@Composable
fun SearchNavigation(matchIds: List<String>, currentMatchIdx: Int, onIdxChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = if (matchIds.isEmpty()) "0 resultados" else "${currentMatchIdx + 1}/${matchIds.size} resultados", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.weight(1f))
        OutlinedButton(enabled = matchIds.isNotEmpty(), onClick = { if (matchIds.isNotEmpty()) onIdxChange((currentMatchIdx - 1 + matchIds.size) % matchIds.size) }) { Text("Anterior") }
        OutlinedButton(enabled = matchIds.isNotEmpty(), onClick = { if (matchIds.isNotEmpty()) onIdxChange((currentMatchIdx + 1) % matchIds.size) }) { Text("Próximo") }
    }
}
