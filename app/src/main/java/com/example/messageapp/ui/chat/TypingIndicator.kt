package com.example.messageapp.ui.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.messageapp.ui.theme.RosaChanchita

/**
 * Indicador de "Escribiendo..." animado
 * 
 * Muestra 3 puntos que aparecen y desaparecen suavemente
 * en color Rosa Chanchita
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animación infinita para los puntos
        val infiniteTransition = rememberInfiniteTransition(label = "typing")
        
        // Cada punto tiene un delay diferente para crear efecto de onda
        val dot1Alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot1"
        )
        
        val dot2Alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = 200, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot2"
        )
        
        val dot3Alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = 400, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot3"
        )
        
        // Texto "Escribiendo"
        Text(
            text = "Escribiendo",
            style = MaterialTheme.typography.bodySmall,
            color = RosaChanchita,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Tres puntos animados
        Text(
            text = ".",
            style = MaterialTheme.typography.bodySmall,
            color = RosaChanchita,
            modifier = Modifier.graphicsLayer { alpha = dot1Alpha }
        )
        
        Text(
            text = ".",
            style = MaterialTheme.typography.bodySmall,
            color = RosaChanchita,
            modifier = Modifier.graphicsLayer { alpha = dot2Alpha }
        )
        
        Text(
            text = ".",
            style = MaterialTheme.typography.bodySmall,
            color = RosaChanchita,
            modifier = Modifier.graphicsLayer { alpha = dot3Alpha }
        )
    }
}

/**
 * Versión simplificada solo con los puntos animados
 */
@Composable
fun TypingDots(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ".",
            style = MaterialTheme.typography.titleLarge,
            color = RosaChanchita,
            modifier = Modifier.graphicsLayer { alpha = dot1Alpha }
        )
        Text(
            text = ".",
            style = MaterialTheme.typography.titleLarge,
            color = RosaChanchita,
            modifier = Modifier.graphicsLayer { alpha = dot2Alpha }
        )
        Text(
            text = ".",
            style = MaterialTheme.typography.titleLarge,
            color = RosaChanchita,
            modifier = Modifier.graphicsLayer { alpha = dot3Alpha }
        )
    }
}
