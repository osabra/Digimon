package com.osabra.gabumon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

private data class Digimon(val name: String)
private val digimon = listOf("Gabumon","Agumon","Patamon","Gatomon","Tentomon","Gomamon","Palmon","Biyomon","Veemon","Wormmon","Guilmon","Renamon").map(::Digimon)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GabumonApp() }
    }
}

@Composable
fun GabumonApp() {
    var loading by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf(0) }
    var chosen by remember { mutableStateOf<Digimon?>(null) }

    LaunchedEffect(Unit) {
        delay(3000)
        loading = false
    }

    MaterialTheme {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF06111F), Color(0xFF102C45), Color(0xFF02070D)))
            )
        ) {
            when {
                loading -> DigiviceSplash()
                chosen == null -> Selector(selected, { selected = it }, { chosen = digimon[selected] })
                else -> Companion(chosen!!) { chosen = null }
            }
        }
    }
}

@Composable
private fun DigiviceSplash() {
    val transition = rememberInfiniteTransition(label = "loading")
    val pulse by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "pulse"
    )
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DIGIVICE", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Box(
            Modifier.size(300.dp, 360.dp).scale(pulse)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFEFFFFF), Color(0xFF7AC9D7), Color(0xFFD7FAFF))),
                    RoundedCornerShape(48.dp)
                )
                .border(5.dp, Color(0xFF17222B), RoundedCornerShape(48.dp))
                .padding(30.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(150.dp, 205.dp)
                        .background(Color(0xFF06151C), RoundedCornerShape(22.dp))
                        .border(5.dp, Color(0xFF16DFFF), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(82.dp)
                                .background(Color(0xFF0A1B25), CircleShape)
                                .border(5.dp, Color(0xFF37E9FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⌛", color = Color.White, fontSize = 32.sp)
                        }
                        Spacer(Modifier.height(22.dp))
                        Box(
                            Modifier.fillMaxWidth(.72f).height(9.dp)
                                .background(Color(0xFF03151D), RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                Modifier.fillMaxWidth(progress).fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFFBFFFFF), Color(0xFF19DFFF))),
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DigiviceButton(); DigiviceButton(); DigiviceButton()
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Iniciando…", color = Color.White.copy(alpha = .9f), fontSize = 15.sp)
        Spacer(Modifier.height(5.dp))
        Text("Sincronizando con el Mundo Digital", color = Color(0xFF7EEFFF), fontSize = 11.sp)
    }
}

@Composable
private fun DigiviceButton() {
    Box(
        Modifier.size(38.dp).background(Color(0xFF536AA8), CircleShape)
            .border(2.dp, Color(0xFFB1C7FF), CircleShape)
    )
}

@Composable
fun Selector(index: Int, setIndex: (Int) -> Unit, choose: () -> Unit) {
    val d = digimon[index]
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(32.dp))
        Text("ELIGE A TU COMPAÑERO", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(d.name, color = Color(0xFF4DE7FF), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(18.dp))
        Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(28.dp)) {
            AndroidView(
                factory = { DigimonModelView(it, d.name) },
                update = { it.setDigimon(d.name) },
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { setIndex((index - 1 + digimon.size) % digimon.size) }) { Text("‹") }
            Spacer(Modifier.width(25.dp))
            Text("${index + 1} / ${digimon.size}", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(25.dp))
            Button(onClick = { setIndex((index + 1) % digimon.size) }) { Text("›") }
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = choose, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("ELEGIR COMO COMPAÑERO", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Companion(d: Digimon, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) { Text("←  Volver a elegir Digimon", fontWeight = FontWeight.Bold) }
        Text(
            d.name,
            Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp),
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        AndroidView(
            factory = { DigimonModelView(it, d.name) },
            update = { it.setDigimon(d.name) },
            modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 12.dp)
        )
        Text("Arrastra para girar", Modifier.align(Alignment.CenterHorizontally), color = Color.LightGray)
    }
}
