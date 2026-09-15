package com.osabra.gabumon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GabumonApp() }
    }
}

@androidx.compose.runtime.Composable
fun GabumonApp() {
    var mood by remember { mutableStateOf("¡Hola! Soy Gabumon 🐺") }
    var affection by remember { mutableStateOf(72) }
    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "breath"
    )

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF10141A)).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("GABUMON", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Tu compañero digital", color = Color(0xFFB9C2CC), fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A222C))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🐺", fontSize = 120.sp, modifier = Modifier.scale(scale))
                        Spacer(Modifier.height(12.dp))
                        Text(mood, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Cariño: $affection%", color = Color(0xFFB9C2CC))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("👋 Saludar") {
                    mood = "¡Me alegra verte!"
                    affection = (affection + 2).coerceAtMost(100)
                }
                ActionButton("❤️ Acariciar") {
                    mood = "Gabu... gabu... ❤️"
                    affection = (affection + 5).coerceAtMost(100)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("⚡ Jugar") { mood = "¡Vamos a entrenar! ⚡" }
                ActionButton("😴 Dormir") { mood = "Zzz... hasta mañana..." }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3743))
    ) { Text(text, fontSize = 14.sp) }
}
