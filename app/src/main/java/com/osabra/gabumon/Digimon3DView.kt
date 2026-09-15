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

class Digimon3DView(context: Context) : GLSurfaceView(context) {
    private val renderer=Renderer(); private var lastX=0f
    init { setEGLContextClientVersion(2); setRenderer(renderer); renderMode=RENDERMODE_CONTINUOUSLY }
    override fun onTouchEvent(e:MotionEvent):Boolean { when(e.actionMasked){ MotionEvent.ACTION_DOWN->{lastX=e.x;return true}; MotionEvent.ACTION_MOVE->{renderer.rotation+=(e.x-lastX)*.42f;lastX=e.x;return true} };return true }
    private class Renderer:GLSurfaceView.Renderer {
        var rotation=0f; private val p=FloatArray(16);private val v=FloatArray(16);private val m=FloatArray(16);private val mvp=FloatArray(16);private lateinit var mesh:Mesh;private var t0=System.nanoTime()
        override fun onSurfaceCreated(g:GL10?,c:EGLConfig?){GLES20.glClearColor(.008f,.022f,.038f,1f);GLES20.glEnable(GLES20.GL_DEPTH_TEST);mesh=Mesh();mesh.program()}
        override fun onSurfaceChanged(g:GL10?,w:Int,h:Int){GLES20.glViewport(0,0,w,h);Matrix.perspectiveM(p,0,40f,w.toFloat()/h,.1f,100f)}
        override fun onDrawFrame(g:GL10?){
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT);val t=(System.nanoTime()-t0)/1e9f;val br=1f+sin(t*2.1f)*.012f
            Matrix.setLookAtM(v,0,0f,1.45f,-7.5f,0f,1.45f,0f,0f,1f,0f)
            // Body
            part(0f,1.08f,.04f,.90f,1.05f*br,.66f,BLUE);part(0f,1.02f,-.59f,.55f,.70f*br,.18f,YELLOW)
            // Head, muzzle and nose
            part(0f,2.34f,0f,.72f,.69f,.61f,PELT);part(0f,2.07f,-.57f,.42f,.29f,.30f,MUZZLE);part(0f,2.07f,-.86f,.115f,.08f,.07f,BLACK)
            // Ears
            part(-.52f,2.79f,.02f,.18f,.46f,.18f,BLUE);part(.52f,2.79f,.02f,.18f,.46f,.18f,BLUE);part(-.52f,2.80f,-.17f,.085f,.27f,.055f,PINK);part(.52f,2.80f,-.17f,.085f,.27f,.055f,PINK)
            // Eyes
            eye(-.245f);eye(.245f)
            // Horn
            part(0f,2.92f,-.01f,.13f,.34f,.12f,GOLD);part(0f,3.17f,-.01f,.07f,.18f,.065f,YELLOW)
            // White pelt with blue stripes
            part(0f,1.90f,.43f,.69f,.75f,.27f,PELT);part(-.59f,1.68f,.39f,.30f,.63f,.25f,PELT);part(.59f,1.68f,.39f,.30f,.63f,.25f,PELT)
            stripe(-.60f,2.02f,.45f,.30f,.085f,.28f);stripe(.60f,2.02f,.45f,.30f,.085f,.28f);stripe(-.75f,1.70f,.40f,.24f,.085f,.24f);stripe(.75f,1.70f,.40f,.24f,.085f,.24f)
            // Arms, hands and claws
            arm(-.82f);arm(.82f)
            // Legs, feet and claws
            part(-.34f,.30f,0f,.30f,.61f,.32f,BLUE);part(.34f,.30f,0f,.30f,.61f,.32f,BLUE);part(-.38f,-.18f,-.34f,.38f,.18f,.47f,FOOT);part(.38f,-.18f,-.34f,.38f,.18f,.47f,FOOT)
            claw(-.53f,-.28f,-.68f);claw(-.38f,-.30f,-.72f);claw(.38f,-.30f,-.72f);claw(.53f,-.28f,-.68f)
            // Tail behind the body
            part(1.00f,1.18f,.43f,.23f,.24f,.78f,BLUE);part(1.24f,1.32f,.77f,.26f,.26f,.40f,BLUE)
        }
        private fun eye(x:Float){part(x,2.38f,-.625f,.105f,.135f,.065f,BLACK);part(x,2.40f,-.692f,.05f,.078f,.018f,RED);part(x+.02f,2.445f,-.712f,.018f,.024f,.01f,WHITE)}
        private fun arm(x:Float){part(x,1.18f,-.01f,.24f,.59f,.25f,BLUE);part(x,.66f,-.28f,.25f,.22f,.27f,BLUE);claw(x-.10f,.63f,-.51f);claw(x+.10f,.63f,-.51f)}
        private fun stripe(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float)=part(x,y,z,sx,sy,sz,STRIPE)
        private fun claw(x:Float,y:Float,z:Float)=part(x,y,z,.052f,.13f,.075f,CLAW)
        private fun part(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,c:FloatArray){Matrix.setIdentityM(m,0);Matrix.rotateM(m,0,rotation,0f,1f,0f);Matrix.translateM(m,0,x,y,z);Matrix.scaleM(m,0,sx,sy,sz);Matrix.multiplyMM(mvp,0,v,0,m,0);Matrix.multiplyMM(mvp,0,p,0,mvp,0);mesh.draw(mvp,c[0],c[1],c[2])}
    }
    private class Mesh {
        private lateinit var vb:FloatBuffer;private lateinit var ib:ShortBuffer;private var prog=0;private var pos=0;private var mat=0;private var col=0
        init{val st=20;val sl=28;val d=ArrayList<Float>();for(a in 0..st){val ph=Math.PI*a/st;val y=cos(ph).toFloat();val r=sin(ph).toFloat();for(b in 0 until sl){val th=2*Math.PI*b/sl;d+=(r*cos(th)).toFloat();d+=y;d+=(r*sin(th)).toFloat()}};val q=ArrayList<Short>();for(a in 0 until st)for(b in 0 until sl){val x=(a*sl+b).toShort();val y=(a*sl+(b+1)%sl).toShort();val z=((a+1)*sl+b).toShort();val w=((a+1)*sl+(b+1)%sl).toShort();q+=x;q+=z;q+=y;q+=y;q+=z;q+=w};vb=ByteBuffer.allocateDirect(d.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(d.toFloatArray()).position(0)};ib=ByteBuffer.allocateDirect(q.size*2).order(ByteOrder.nativeOrder()).asShortBuffer().apply{put(q.toShortArray()).position(0)}}
        fun program(){val vs="attribute vec4 p;uniform mat4 m;void main(){gl_Position=m*p;}";val fs="precision mediump float;uniform vec4 c;void main(){gl_FragColor=c;}";val a=sh(GLES20.GL_VERTEX_SHADER,vs);val b=sh(GLES20.GL_FRAGMENT_SHADER,fs);prog=GLES20.glCreateProgram();GLES20.glAttachShader(prog,a);GLES20.glAttachShader(prog,b);GLES20.glLinkProgram(prog);pos=GLES20.glGetAttribLocation(prog,"p");mat=GLES20.glGetUniformLocation(prog,"m");col=GLES20.glGetUniformLocation(prog,"c")}
        private fun sh(t:Int,s:String)=GLES20.glCreateShader(t).also{GLES20.glShaderSource(it,s);GLES20.glCompileShader(it)}
        fun draw(m:FloatArray,r:Float,g:Float,b:Float){GLES20.glUseProgram(prog);GLES20.glEnableVertexAttribArray(pos);GLES20.glVertexAttribPointer(pos,3,GLES20.GL_FLOAT,false,12,vb);GLES20.glUniformMatrix4fv(mat,1,false,m,0);GLES20.glUniform4f(col,r,g,b,1f);GLES20.glDrawElements(GLES20.GL_TRIANGLES,ib.capacity(),GLES20.GL_UNSIGNED_SHORT,ib);GLES20.glDisableVertexAttribArray(pos)}
    }
    companion object{private val BLUE=floatArrayOf(.08f,.38f,.62f);private val PELT=floatArrayOf(.91f,.89f,.80f);private val MUZZLE=floatArrayOf(.94f,.83f,.63f);private val YELLOW=floatArrayOf(1f,.72f,.08f);private val GOLD=floatArrayOf(1f,.84f,.16f);private val PINK=floatArrayOf(1f,.48f,.40f);private val BLACK=floatArrayOf(.015f,.015f,.018f);private val RED=floatArrayOf(.78f,.03f,.035f);private val WHITE=floatArrayOf(1f,1f,1f);private val STRIPE=floatArrayOf(.06f,.28f,.52f);private val FOOT=floatArrayOf(.90f,.84f,.69f);private val CLAW=floatArrayOf(.72f,.025f,.025f)}
}
