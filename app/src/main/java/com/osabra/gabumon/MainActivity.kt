package com.osabra.gabumon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private data class Digimon(val name: String)
private val digimon = listOf("Gabumon","Agumon","Patamon","Gatomon","Tentomon","Gomamon","Palmon","Biyomon","Veemon","Wormmon","Guilmon","Renamon").map(::Digimon)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { GabumonApp() } }
}

@Composable fun GabumonApp() {
    var loading by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf(0) }
    var chosen by remember { mutableStateOf<Digimon?>(null) }
    LaunchedEffect(Unit) { delay(3000); loading = false }
    MaterialTheme {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF06111F),Color(0xFF102C45),Color(0xFF02070D))))) {
            when { loading -> DigiviceSplash(); chosen == null -> Selector(selected,{selected=it},{chosen=digimon[selected]}); else -> Companion(chosen!!){chosen=null} }
        }
    }
}

@Composable private fun DigiviceSplash() {
    val transition = rememberInfiniteTransition(label="digivice")
    val pulse by transition.animateFloat(.97f,1.03f,infiniteRepeatable(tween(900),RepeatMode.Reverse),label="pulse")
    val progress by transition.animateFloat(0f,1f,infiniteRepeatable(tween(1500,easing=LinearEasing),RepeatMode.Restart),label="progress")
    Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
        Text("DIGIVICE",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.size(330.dp,285.dp).scale(pulse),contentAlignment=Alignment.Center){
            Canvas(Modifier.fillMaxSize()) {
                val w=size.width; val h=size.height
                val body=Path().apply {
                    moveTo(w*.07f,h*.25f); lineTo(w*.24f,h*.08f); lineTo(w*.76f,h*.05f); lineTo(w*.93f,h*.25f)
                    lineTo(w*.97f,h*.72f); lineTo(w*.78f,h*.91f); lineTo(w*.20f,h*.94f); lineTo(w*.04f,h*.72f); close()
                }
                drawPath(body,Brush.linearGradient(listOf(Color(0xFFE9FCFF),Color(0xFF8DDBE8),Color(0xFFC9F5FA))))
                drawPath(body,Color(0xFF101820),style=Stroke(6f,cap=StrokeCap.Round))
                val screenR=Rect(w*.24f,h*.18f,w*.70f,h*.72f)
                drawRoundRect(screenR,22f,22f,Color(0xFF06131B))
                drawRoundRect(screenR,22f,22f,Color(0xFF16DFFF),style=Stroke(5f))
                drawRoundRect(Rect(w*.27f,h*.21f,w*.67f,h*.69f),18f,18f,Color(0xFF062B39),style=Stroke(2f))
                val cx=w*.47f; val cy=h*.44f; val radius=w*.145f
                drawCircle(Color(0xFF0A1821),radius,Offset(cx,cy))
                drawCircle(Color(0xFF2FE8FF),radius,Offset(cx,cy),style=Stroke(7f))
                drawCircle(Color(0xFF8EFAFF),radius*.72f,Offset(cx,cy),style=Stroke(3f))
                drawCircle(Color(0xFF27DFFF),radius*.42f,Offset(cx,cy),style=Stroke(2f))
                val hourglass=Path().apply {
                    moveTo(cx-radius*.34f,cy-radius*.55f); lineTo(cx+radius*.34f,cy-radius*.55f)
                    lineTo(cx+radius*.18f,cy-radius*.08f); lineTo(cx-radius*.18f,cy+radius*.08f)
                    lineTo(cx-radius*.34f,cy+radius*.55f); lineTo(cx+radius*.34f,cy+radius*.55f)
                }
                drawPath(hourglass,Color.White,style=Stroke(5f,cap=StrokeCap.Round))
                drawCircle(Color.White,radius*.13f,Offset(cx,cy+radius*.19f))
                val bx=w*.30f; val by=h*.62f; val bw=w*.34f; val bh=h*.045f
                drawRoundRect(Rect(bx,by,bx+bw,by+bh),bh/2,bh/2,Color(0xFF03151D),style=Stroke(3f))
                val moving=bx+(bw-bh*.65f)*progress
                drawRoundRect(Rect(bx+3f,by+3f,moving,by+bh-3f),bh/2,bh/2,Brush.horizontalGradient(listOf(Color(0xFFBFFFFF),Color(0xFF19DFFF))))
                listOf(Offset(w*.14f,h*.47f),Offset(w*.78f,h*.30f),Offset(w*.78f,h*.50f)).forEach {
                    drawCircle(Color(0xFF586FAE),w*.065f,it)
                    drawCircle(Color(0xFF9FB9F4),w*.065f,it,style=Stroke(3f))
                    drawCircle(Color(0xFF3D568F),w*.035f,it)
                }
                for(i in 0 until 12){
                    val a=(i/12f)*Math.PI*2
                    val gx=cx+cos(a).toFloat()*w*.25f
                    val gy=cy+sin(a).toFloat()*h*.27f
                    drawCircle(Color(0xFF9EDCE4),4f,Offset(gx,gy))
                }
            }
        }
        Spacer(Modifier.height(8.dp)); Text("Iniciando…",color=Color.White.copy(alpha=.85f),fontSize=15.sp)
        Spacer(Modifier.height(5.dp)); Text("Sincronizando con el Mundo Digital",color=Color(0xFF7EEFFF),fontSize=11.sp)
    }
}

@Composable fun Selector(index:Int,setIndex:(Int)->Unit,choose:()->Unit){
    val d=digimon[index]
    Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Spacer(Modifier.height(32.dp)); Text("ELIGE A TU COMPAÑERO",color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Bold)
        Spacer(Modifier.height(8.dp)); Text(d.name,color=Color(0xFF4DE7FF),fontSize=32.sp,fontWeight=FontWeight.ExtraBold)
        Spacer(Modifier.height(18.dp)); Card(Modifier.fillMaxWidth().weight(1f),shape=RoundedCornerShape(28.dp)){ AndroidView(factory={Digimon3DView(it,d.name)},update={it.setDigimon(d.name)},modifier=Modifier.fillMaxSize()) }
        Spacer(Modifier.height(14.dp)); Row(verticalAlignment=Alignment.CenterVertically){Button(onClick={setIndex((index-1+digimon.size)%digimon.size)}){Text("‹")}; Spacer(Modifier.width(25.dp)); Text("${index+1} / ${digimon.size}",color=Color.White,fontWeight=FontWeight.Bold); Spacer(Modifier.width(25.dp)); Button(onClick={setIndex((index+1)%digimon.size)}){Text("›")}}
        Spacer(Modifier.height(10.dp)); Button(onClick=choose,modifier=Modifier.fillMaxWidth().height(54.dp)){Text("ELEGIR COMO COMPAÑERO",fontWeight=FontWeight.Bold)}
    }
}

@Composable private fun Companion(d:Digimon,onBack:()->Unit){
    Column(Modifier.fillMaxSize().padding(20.dp)){
        Button(onClick=onBack){Text("←  Volver a elegir Digimon",fontWeight=FontWeight.Bold)}
        Text(d.name,Modifier.align(Alignment.CenterHorizontally).padding(top=12.dp),color=Color.White,fontSize=36.sp,fontWeight=FontWeight.Bold)
        AndroidView(factory={Digimon3DView(it,d.name)},modifier=Modifier.fillMaxWidth().weight(1f).padding(vertical=12.dp))
        Text("Arrastra para girar",Modifier.align(Alignment.CenterHorizontally),color=Color.LightGray)
    }
}
