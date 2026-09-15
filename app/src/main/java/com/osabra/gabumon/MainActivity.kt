package com.osabra.gabumon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { GabumonApp() } }
}

@Composable fun GabumonApp() {
    var loading by remember { mutableStateOf(true) }; var selected by remember { mutableStateOf(0) }; var chosen by remember { mutableStateOf<Digimon?>(null) }
    LaunchedEffect(Unit) { delay(2200); loading = false }
    MaterialTheme { Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF06111F),Color(0xFF102C45),Color(0xFF02070D))))) {
        when { loading -> DigiviceSplash(); chosen == null -> Selector(selected,{selected=it},{chosen=digimon[selected]}); else -> Companion(chosen!!){chosen=null} }
    } }
}

@Composable private fun DigiviceSplash() {
    val t=rememberInfiniteTransition(label="pulse"); val p by t.animateFloat(.94f,1.06f,infiniteRepeatable(tween(700),RepeatMode.Reverse),label="pulse")
    Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){ Text("DIGIVICE",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.height(18.dp)); Box(Modifier.size(210.dp,270.dp).scale(p).background(Color(0xFF263A4D),RoundedCornerShape(45.dp)),contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ Box(Modifier.size(145.dp,105.dp).background(Color(0xFF07141D),RoundedCornerShape(24.dp)),contentAlignment=Alignment.Center){Text("◆",color=Color(0xFF4DE7FF),fontSize=58.sp)}; Spacer(Modifier.height(22.dp)); Text("DIGITAL WORLD",color=Color.LightGray,fontSize=12.sp)}}; Spacer(Modifier.height(20.dp)); Text("Iniciando…",color=Color.White.copy(alpha=.8f)) }
}

@Composable private fun Selector(index:Int,setIndex:(Int)->Unit,choose:()->Unit){ val d=digimon[index]; Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){ Spacer(Modifier.height(32.dp)); Text("ELIGE A TU COMPAÑERO",color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(d.name,color=Color(0xFF4DE7FF),fontSize=32.sp,fontWeight=FontWeight.ExtraBold); Spacer(Modifier.height(18.dp)); Card(Modifier.fillMaxWidth().weight(1f),shape=RoundedCornerShape(28.dp)){ AndroidView(factory={Digimon3DView(it)},modifier=Modifier.fillMaxSize()) }; Spacer(Modifier.height(14.dp)); Row(verticalAlignment=Alignment.CenterVertically){Button(onClick={setIndex((index-1+digimon.size)%digimon.size)}){Text("‹")}; Spacer(Modifier.width(25.dp)); Text("${index+1} / ${digimon.size}",color=Color.White,fontWeight=FontWeight.Bold); Spacer(Modifier.width(25.dp)); Button(onClick={setIndex((index+1)%digimon.size)}){Text("›")}}; Spacer(Modifier.height(10.dp)); Button(onClick=choose,modifier=Modifier.fillMaxWidth().height(54.dp)){Text("ELEGIR COMO COMPAÑERO",fontWeight=FontWeight.Bold)} } }

@Composable private fun Companion(d:Digimon,onBack:()->Unit){ Column(Modifier.fillMaxSize().padding(20.dp)){Button(onClick=onBack){Text("←  Volver a elegir Digimon",fontWeight=FontWeight.Bold)}; Text(d.name,Modifier.align(Alignment.CenterHorizontally).padding(top=12.dp),color=Color.White,fontSize=36.sp,fontWeight=FontWeight.Bold); AndroidView(factory={Digimon3DView(it)},modifier=Modifier.fillMaxWidth().weight(1f).padding(vertical=12.dp)); Text("Arrastra para girar",Modifier.align(Alignment.CenterHorizontally),color=Color.LightGray) } }
