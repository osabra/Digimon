package com.osabra.gabumon

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class Digimon3DView(context: Context, initialDigimon: String = "Gabumon") : GLSurfaceView(context) {
    private val renderer = Renderer(initialDigimon)
    private var lastX = 0f
    init { setEGLContextClientVersion(2); setRenderer(renderer); renderMode = RENDERMODE_CONTINUOUSLY }
    fun setDigimon(name: String) { renderer.name = name }
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.actionMasked == MotionEvent.ACTION_DOWN) { lastX=e.x; return true }
        if (e.actionMasked == MotionEvent.ACTION_MOVE) { renderer.rotation += (e.x-lastX)*.42f; lastX=e.x; return true }
        return true
    }

    private class Renderer(var name: String) : GLSurfaceView.Renderer {
        var rotation=0f
        private val p=FloatArray(16); private val v=FloatArray(16); private val model=FloatArray(16); private val mvp=FloatArray(16); private val normal=FloatArray(9); private val rot=FloatArray(16)
        private lateinit var mesh: Mesh; private var t0=System.nanoTime()
        override fun onSurfaceCreated(g:GL10?,c:EGLConfig?){GLES20.glClearColor(.006f,.018f,.032f,1f);GLES20.glEnable(GLES20.GL_DEPTH_TEST);GLES20.glEnable(GLES20.GL_CULL_FACE);mesh=Mesh();mesh.program()}
        override fun onSurfaceChanged(g:GL10?,w:Int,h:Int){GLES20.glViewport(0,0,w,h);Matrix.perspectiveM(p,0,38f,w.toFloat()/h,.1f,100f)}
        override fun onDrawFrame(g:GL10?){
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            val t=(System.nanoTime()-t0)/1e9f; val bob=sin(t*2.1f)*.012f; Matrix.setLookAtM(v,0,0f,1.48f,-7.4f,0f,1.42f,0f,0f,1f,0f);Matrix.setIdentityM(rot,0);Matrix.rotateM(rot,0,rotation,0f,1f,0f)
            when(name){
                "Gabumon"->gabumon(bob)
                "Agumon"->agumon(bob)
                "Patamon"->patamon(bob)
                "Gatomon"->gatomon(bob)
                "Tentomon"->tentomon(bob)
                "Gomamon"->gomamon(bob)
                "Palmon"->palmon(bob)
                "Biyomon"->biyomon(bob)
                "Veemon"->veemon(bob)
                "Wormmon"->wormmon(bob)
                "Guilmon"->guilmon(bob)
                "Renamon"->renamon(bob)
            }
        }

        private fun base(b:Float, body:FloatArray, head:FloatArray, belly:FloatArray?=null){
            part(0f,1.04f+b,.02f,.82f,1.00f,.60f,body); part(0f,2.28f+b,-.02f,.68f,.68f,.57f,head)
            if(belly!=null) part(0f,1.02f+b,-.58f,.50f,.68f,.15f,belly)
            eye(-.23f,2.40f+b,head);eye(.23f,2.40f+b,head)
            arm(-.76f,1.12f+b,body);arm(.76f,1.12f+b,body)
            part(-.30f,.31f+b,0f,.28f,.55f,.28f,body);part(.30f,.31f+b,0f,.28f,.55f,.28f,body)
            part(-.34f,-.17f+b,-.34f,.36f,.18f,.43f,body);part(.34f,-.17f+b,-.34f,.36f,.18f,.43f,body)
        }
        private fun eye(x:Float,y:Float,head:FloatArray){part(x,y,-.61f,.105f,.14f,.055f,BLACK);part(x,y+.015f,-.665f,.052f,.08f,.014f,if(head===YELLOW)BLACK else RED);part(x+.018f,y+.045f,-.681f,.017f,.024f,.008f,WHITE)}
        private fun arm(x:Float,y:Float,c:FloatArray){part(x,y,-.02f,.23f,.55f,.23f,c);part(x,.64f,-.28f,.24f,.21f,.24f,c);part(x-.09f,.60f,-.50f,.05f,.12f,.06f,CLAW);part(x+.09f,.60f,-.50f,.05f,.12f,.06f,CLAW)}
        private fun horn(x:Float,y:Float,z:Float,sx:Float,sy:Float,c:FloatArray){part(x,y,z,sx,sy,sx,c)}

        private fun gabumon(b:Float){
            base(b,BLUE,PELT,ORANGE); part(-.53f,2.76f+b,.01f,.18f,.43f,.18f,BLUE);part(.53f,2.76f+b,.01f,.18f,.43f,.18f,BLUE);part(-.53f,2.77f+b,-.17f,.08f,.26f,.05f,PINK);part(.53f,2.77f+b,-.17f,.08f,.26f,.05f,PINK)
            part(-.28f,2.15f+b,-.53f,.30f,.23f,.24f,MUZZLE);part(.28f,2.15f+b,-.53f,.30f,.23f,.24f,MUZZLE);part(0f,2.02f+b,-.79f,.11f,.05f,.03f,BLACK);horn(0f,2.94f+b,-.01f,.13f,.32f,GOLD);horn(0f,3.18f+b,-.01f,.07f,.17f,YELLOW)
            for(s in listOf(-.62f,.62f,-.75f,.75f)) part(s,1.75f+b,.43f,.23f,.07f,.23f,STRIPE)
            part(.92f,1.12f+b,.44f,.23f,.24f,.68f,BLUE);part(1.20f,1.30f+b,.70f,.26f,.25f,.40f,BLUE)
        }
        private fun agumon(b:Float){
            base(b,ORANGE,ORANGE,CREAM);part(0f,2.82f+b,.0f,.15f,.30f,.13f,ORANGE);part(-.48f,2.74f+b,.0f,.16f,.28f,.14f,ORANGE);part(.48f,2.74f+b,.0f,.16f,.28f,.14f,ORANGE)
            part(-.28f,2.13f+b,-.53f,.28f,.20f,.23f,MUZZLE);part(.28f,2.13f+b,-.53f,.28f,.20f,.23f,MUZZLE);part(0f,1.98f+b,-.77f,.12f,.06f,.03f,BLACK)
            part(-.83f,1.05f+b,.08f,.20f,.55f,.22f,ORANGE);part(.83f,1.05f+b,.08f,.20f,.55f,.22f,ORANGE);part(0f,.82f+b,.60f,.42f,.34f,.55f,ORANGE);part(0f,.70f+b,.98f,.34f,.22f,.35f,ORANGE)
        }
        private fun patamon(b:Float){
            base(b,GOLD,CREAM,CREAM);part(-.72f,2.28f+b,.05f,.40f,.22f,.16f,CREAM);part(.72f,2.28f+b,.05f,.40f,.22f,.16f,CREAM);part(-.50f,2.77f+b,.0f,.15f,.34f,.15f,GOLD);part(.50f,2.77f+b,.0f,.15f,.34f,.15f,GOLD)
            part(0f,2.05f+b,-.64f,.22f,.12f,.05f,MUZZLE);part(0f,2.00f+b,-.70f,.07f,.04f,.02f,BLACK);part(-.87f,1.15f+b,.18f,.38f,.70f,.12f,CREAM);part(.87f,1.15f+b,.18f,.38f,.70f,.12f,CREAM)
        }
        private fun gatomon(b:Float){
            base(b,CREAM,CREAM,CREAM);part(-.49f,2.86f+b,.0f,.13f,.42f,.13f,CREAM);part(.49f,2.86f+b,.0f,.13f,.42f,.13f,CREAM);part(-.49f,2.92f+b,-.08f,.07f,.22f,.05f,PINK);part(.49f,2.92f+b,-.08f,.07f,.22f,.05f,PINK)
            part(-.82f,1.02f+b,-.03f,.24f,.48f,.25f,WHITE);part(.82f,1.02f+b,-.03f,.24f,.48f,.25f,WHITE);part(0f,2.02f+b,-.66f,.09f,.05f,.025f,PINK);part(0f,.83f+b,.66f,.20f,.18f,.65f,GOLD)
        }
        private fun tentomon(b:Float){
            base(b,RED,RED,RED);part(0f,2.88f+b,.0f,.22f,.24f,.20f,RED);part(-.72f,2.70f+b,.0f,.17f,.48f,.17f,RED);part(.72f,2.70f+b,.0f,.17f,.48f,.17f,RED);part(0f,2.12f+b,-.64f,.16f,.11f,.04f,BLACK)
            for(x in listOf(-.45f,.45f))part(x,1.30f+b,-.38f,.18f,.65f,.22f,BLUE);part(0f,1.03f+b,.55f,.44f,.70f,.28f,RED);part(-.90f,.98f+b,-.05f,.35f,.20f,.32f,BLUE);part(.90f,.98f+b,-.05f,.35f,.20f,.32f,BLUE)
        }
        private fun gomamon(b:Float){
            base(b,WHITE,WHITE,WHITE);part(-.50f,2.72f+b,.0f,.15f,.32f,.14f,WHITE);part(.50f,2.72f+b,.0f,.15f,.32f,.14f,WHITE);part(0f,2.05f+b,-.66f,.13f,.08f,.03f,BLACK)
            for(x in listOf(-.48f,-.24f,0f,.24f,.48f))part(x,1.83f+b,.55f,.12f,.16f,.08f,WHITE)
            part(0f,1.10f+b,.56f,.60f,.78f,.20f,BLUE);part(.88f,1.02f+b,.38f,.32f,.22f,.48f,BLUE)
        }
        private fun palmon(b:Float){
            base(b,GREEN,PALE,PALE);for(x in listOf(-.50f,-.25f,0f,.25f,.50f))part(x,2.87f+b,.0f,.13f,.36f,.11f,GREEN);part(0f,2.02f+b,-.65f,.11f,.06f,.03f,BLACK)
            part(0f,1.90f+b,.55f,.36f,.20f,.16f,PINK);part(0f,1.62f+b,.57f,.48f,.18f,.18f,PINK);part(-.92f,1.45f+b,.15f,.34f,.18f,.18f,GREEN);part(.92f,1.45f+b,.15f,.34f,.18f,.18f,GREEN)
        }
        private fun biyomon(b:Float){
            base(b,PINK,PEACH,CREAM);part(-.45f,2.83f+b,.0f,.18f,.38f,.15f,PINK);part(.45f,2.83f+b,.0f,.18f,.38f,.15f,PINK);part(0f,2.08f+b,-.65f,.20f,.11f,.04f,ORANGE)
            part(-.92f,1.18f+b,.10f,.32f,.68f,.18f,PINK);part(.92f,1.18f+b,.10f,.32f,.68f,.18f,PINK);part(0f,.78f+b,.58f,.26f,.28f,.62f,PINK)
        }
        private fun veemon(b:Float){
            base(b,VEEMON,VEEMON,CREAM);part(-.48f,2.87f+b,.0f,.14f,.48f,.13f,VEEMON);part(.48f,2.87f+b,.0f,.14f,.48f,.13f,VEEMON);part(-.25f,2.16f+b,-.56f,.30f,.18f,.20f,MUZZLE);part(.25f,2.16f+b,-.56f,.30f,.18f,.20f,MUZZLE);part(0f,2.00f+b,-.74f,.10f,.05f,.03f,BLACK);part(0f,1.48f+b,.60f,.32f,.25f,.22f,VEEMON)
        }
        private fun wormmon(b:Float){
            base(b,GREEN,PALE,PALE);part(-.45f,2.86f+b,.0f,.15f,.42f,.14f,GREEN);part(.45f,2.86f+b,.0f,.15f,.42f,.14f,GREEN);part(0f,2.03f+b,-.66f,.11f,.06f,.03f,BLACK);part(0f,1.72f+b,.55f,.30f,.34f,.20f,GREEN);part(-.92f,1.18f+b,.0f,.24f,.70f,.22f,GREEN);part(.92f,1.18f+b,.0f,.24f,.70f,.22f,GREEN)
        }
        private fun guilmon(b:Float){
            base(b,CRIMSON,CREAM,CREAM);part(-.50f,2.83f+b,.0f,.18f,.38f,.16f,CRIMSON);part(.50f,2.83f+b,.0f,.18f,.38f,.16f,CRIMSON);part(-.27f,2.15f+b,-.55f,.29f,.19f,.22f,CREAM);part(.27f,2.15f+b,-.55f,.29f,.19f,.22f,CREAM);part(0f,2.00f+b,-.75f,.11f,.06f,.03f,BLACK);part(0f,1.15f+b,.60f,.40f,.28f,.62f,CRIMSON);part(0f,.86f+b,1.02f,.31f,.22f,.40f,CRIMSON)
        }
        private fun renamon(b:Float){
            base(b,GOLDEN,CREAM,CREAM);part(-.42f,2.92f+b,.0f,.12f,.56f,.12f,GOLDEN);part(.42f,2.92f+b,.0f,.12f,.56f,.12f,GOLDEN);part(-.78f,2.64f+b,.0f,.14f,.28f,.13f,GOLDEN);part(.78f,2.64f+b,.0f,.14f,.28f,.13f,GOLDEN);part(0f,2.02f+b,-.65f,.10f,.06f,.03f,BLACK);part(0f,1.08f+b,.60f,.25f,.80f,.20f,GOLDEN);part(-.86f,1.18f+b,.0f,.18f,.78f,.18f,GOLDEN);part(.86f,1.18f+b,.0f,.18f,.78f,.18f,GOLDEN)
        }

        private fun part(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,c:FloatArray){Matrix.setIdentityM(model,0);Matrix.multiplyMM(model,0,rot,0,model,0);Matrix.translateM(model,0,x,y,z);Matrix.scaleM(model,0,sx,sy,sz);Matrix.multiplyMM(mvp,0,v,0,model,0);Matrix.multiplyMM(mvp,0,p,0,mvp,0);normal[0]=rot[0];normal[1]=rot[1];normal[2]=rot[2];normal[3]=rot[4];normal[4]=rot[5];normal[5]=rot[6];normal[6]=rot[8];normal[7]=rot[9];normal[8]=rot[10];mesh.draw(mvp,normal,c[0],c[1],c[2])}
    }

    private class Mesh {
        private lateinit var vb:FloatBuffer;private lateinit var ib:ShortBuffer;private var prog=0;private var pos=0;private var mat=0;private var nmat=0;private var col=0;private var light=0
        init{val rings=20;val segs=32;val d=ArrayList<Float>();for(a in 0..rings){val ph=Math.PI*a/rings;val y=cos(ph).toFloat();val r=sin(ph).toFloat();for(b in 0 until segs){val th=2*Math.PI*b/segs;d+=(r*cos(th)).toFloat();d+=y;d+=(r*sin(th)).toFloat()}};val q=ArrayList<Short>();for(a in 0 until rings)for(b in 0 until segs){val x=(a*segs+b).toShort();val y=(a*segs+(b+1)%segs).toShort();val z=((a+1)*segs+b).toShort();val w=((a+1)*segs+(b+1)%segs).toShort();q+=x;q+=z;q+=y;q+=y;q+=z;q+=w};vb=ByteBuffer.allocateDirect(d.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(d.toFloatArray()).position(0)};ib=ByteBuffer.allocateDirect(q.size*2).order(ByteOrder.nativeOrder()).asShortBuffer().apply{put(q.toShortArray()).position(0)}}
        fun program(){val vs="""attribute vec4 p;uniform mat4 m;uniform mat3 nmat;varying vec3 n;void main(){n=normalize(nmat*normalize(p.xyz));gl_Position=m*p;}""";val fs="""precision mediump float;uniform vec4 c;uniform vec3 light;varying vec3 n;void main(){vec3 nn=normalize(n);vec3 ll=normalize(light);float d=max(dot(nn,ll),0.0);float fill=max(dot(nn,normalize(vec3(.55,.35,.75))),0.0);float spec=pow(max(dot(reflect(-ll,nn),vec3(0.0,0.0,1.0)),0.0),26.0);vec3 base=c.rgb*(0.44+0.46*d+0.10*fill)+vec3(1.0)*0.07*spec;gl_FragColor=vec4(base,c.a);}""";val a=sh(GLES20.GL_VERTEX_SHADER,vs);val b=sh(GLES20.GL_FRAGMENT_SHADER,fs);prog=GLES20.glCreateProgram();GLES20.glAttachShader(prog,a);GLES20.glAttachShader(prog,b);GLES20.glLinkProgram(prog);pos=GLES20.glGetAttribLocation(prog,"p");mat=GLES20.glGetUniformLocation(prog,"m");nmat=GLES20.glGetUniformLocation(prog,"nmat");col=GLES20.glGetUniformLocation(prog,"c");light=GLES20.glGetUniformLocation(prog,"light")}
        private fun sh(t:Int,s:String)=GLES20.glCreateShader(t).also{GLES20.glShaderSource(it,s);GLES20.glCompileShader(it)}
        fun draw(m:FloatArray,n:FloatArray,r:Float,g:Float,b:Float){GLES20.glUseProgram(prog);GLES20.glEnableVertexAttribArray(pos);GLES20.glVertexAttribPointer(pos,3,GLES20.GL_FLOAT,false,12,vb);GLES20.glUniformMatrix4fv(mat,1,false,m,0);GLES20.glUniformMatrix3fv(nmat,1,false,n,0);GLES20.glUniform4f(col,r,g,b,1f);GLES20.glUniform3f(light,-.35f,.80f,-.75f);GLES20.glDrawElements(GLES20.GL_TRIANGLES,ib.capacity(),GLES20.GL_UNSIGNED_SHORT,ib);GLES20.glDisableVertexAttribArray(pos)}
    }
    companion object{
        private val BLUE=floatArrayOf(.055f,.34f,.58f);private val PELT=floatArrayOf(.91f,.89f,.80f);private val ORANGE=floatArrayOf(1f,.58f,.04f);private val CREAM=floatArrayOf(.94f,.86f,.68f);private val MUZZLE=floatArrayOf(.94f,.83f,.63f);private val GOLD=floatArrayOf(1f,.80f,.10f);private val YELLOW=floatArrayOf(1f,.72f,.07f);private val PINK=floatArrayOf(1f,.35f,.38f);private val BLACK=floatArrayOf(.01f,.01f,.015f);private val RED=floatArrayOf(.78f,.025f,.035f);private val WHITE=floatArrayOf(1f,1f,1f);private val STRIPE=floatArrayOf(.03f,.22f,.43f);private val CLAW=floatArrayOf(.72f,.025f,.025f);private val GREEN=floatArrayOf(.18f,.60f,.22f);private val PALE=floatArrayOf(.91f,.91f,.78f);private val PEACH=floatArrayOf(1f,.72f,.58f);private val VEEMON=floatArrayOf(.04f,.43f,.72f);private val CRIMSON=floatArrayOf(.72f,.08f,.07f);private val GOLDEN=floatArrayOf(.86f,.65f,.18f)
    }
}
