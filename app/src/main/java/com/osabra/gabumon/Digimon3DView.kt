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

/** Mobile-friendly stylized 3D companion. The entire character rotates as one piece. */
class Digimon3DView(context: Context) : GLSurfaceView(context) {
    private val renderer = Renderer()
    private var lastX = 0f
    init { setEGLContextClientVersion(2); setRenderer(renderer); renderMode = RENDERMODE_CONTINUOUSLY }
    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { lastX=e.x; return true }
            MotionEvent.ACTION_MOVE -> { renderer.rotation += (e.x-lastX)*0.45f; lastX=e.x; return true }
        }
        return true
    }
    private class Renderer : GLSurfaceView.Renderer {
        var rotation=0f
        private var start=System.nanoTime()
        private val projection=FloatArray(16); private val view=FloatArray(16); private val model=FloatArray(16); private val mvp=FloatArray(16)
        private lateinit var mesh:Mesh
        override fun onSurfaceCreated(gl:GL10?,c:EGLConfig?){GLES20.glClearColor(.015f,.04f,.065f,1f);GLES20.glEnable(GLES20.GL_DEPTH_TEST);mesh=Mesh();mesh.program()}
        override fun onSurfaceChanged(gl:GL10?,w:Int,h:Int){GLES20.glViewport(0,0,w,h);Matrix.perspectiveM(projection,0,42f,w.toFloat()/h.toFloat(),.1f,100f)}
        override fun onDrawFrame(gl:GL10?){
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            val t=(System.nanoTime()-start)/1_000_000_000f; val breathe=1f+sin(t*2.2f)*.015f
            // Face is on negative Z; camera is placed on that side.
            Matrix.setLookAtM(view,0,0f,1.55f,-7f,0f,1.45f,0f,0f,1f,0f)
            // Body
            p(0f,1.15f,.05f,1.0f,1.15f*breathe,.75f,.08f,.38f,.62f)
            p(0f,1.10f,-.67f,.65f,.82f*breathe,.16f,.94f,.80f,.45f)
            // Head and muzzle
            p(0f,2.38f,0f,.78f,.76f,.68f,.86f,.88f,.84f)
            p(0f,2.10f,-.64f,.45f,.30f,.30f,.95f,.86f,.70f)
            p(0f,2.10f,-.93f,.13f,.09f,.08f,.03f,.03f,.03f)
            // Ears
            p(-.56f,2.82f,0f,.20f,.50f,.19f,.10f,.38f,.60f); p(.56f,2.82f,0f,.20f,.50f,.19f,.10f,.38f,.60f)
            p(-.56f,2.83f,-.19f,.09f,.30f,.06f,.90f,.65f,.48f); p(.56f,2.83f,-.19f,.09f,.30f,.06f,.90f,.65f,.48f)
            // Eyes
            p(-.25f,2.40f,-.68f,.105f,.13f,.07f,.02f,.02f,.025f); p(.25f,2.40f,-.68f,.105f,.13f,.07f,.02f,.02f,.025f)
            p(-.25f,2.42f,-.755f,.055f,.08f,.025f,.55f,.08f,.06f); p(.25f,2.42f,-.755f,.055f,.08f,.025f,.55f,.08f,.06f)
            p(-.22f,2.47f,-.785f,.02f,.025f,.012f,1f,1f,1f); p(.28f,2.47f,-.785f,.02f,.025f,.012f,1f,1f,1f)
            // Yellow crest
            p(0f,2.93f,-.01f,.14f,.38f,.13f,.95f,.68f,.10f); p(0f,3.20f,-.01f,.075f,.20f,.07f,1f,.78f,.15f)
            // Pelt / mantle behind the face
            p(0f,1.98f,.48f,.72f,.82f,.27f,.92f,.89f,.80f); p(-.63f,1.70f,.42f,.32f,.70f,.27f,.94f,.91f,.82f); p(.63f,1.70f,.42f,.32f,.70f,.27f,.94f,.91f,.82f)
            stripe(-.63f,2.03f,.47f,.33f,.09f,.29f); stripe(.63f,2.03f,.47f,.33f,.09f,.29f); stripe(-.78f,1.69f,.40f,.27f,.09f,.25f); stripe(.78f,1.69f,.40f,.27f,.09f,.25f)
            // Arms and legs
            p(-.82f,1.20f,0f,.25f,.63f,.27f,.08f,.38f,.62f); p(.82f,1.20f,0f,.25f,.63f,.27f,.08f,.38f,.62f)
            p(-.38f,.25f,0f,.31f,.64f,.34f,.08f,.38f,.62f); p(.38f,.25f,0f,.31f,.64f,.34f,.08f,.38f,.62f)
            p(-.40f,-.22f,-.32f,.38f,.20f,.52f,.94f,.86f,.70f); p(.40f,-.22f,-.32f,.38f,.20f,.52f,.94f,.86f,.70f)
            claw(-.90f,.62f,-.48f); claw(-.76f,.60f,-.50f); claw(.90f,.62f,-.48f); claw(.76f,.60f,-.50f)
            claw(-.55f,-.27f,-.70f); claw(-.39f,-.29f,-.73f); claw(.39f,-.29f,-.73f); claw(.55f,-.27f,-.70f)
            // Tail
            p(-1.02f,1.25f,.45f,.24f,.24f,.88f,.08f,.38f,.62f)
        }
        private fun stripe(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float)=p(x,y,z,sx,sy,sz,.07f,.32f,.58f)
        private fun claw(x:Float,y:Float,z:Float)=p(x,y,z,.055f,.13f,.085f,.72f,.04f,.04f)
        private fun p(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,r:Float,g:Float,b:Float){
            // Global rotation first, then local placement: this prevents the body parts from orbiting individually.
            Matrix.setIdentityM(model,0); Matrix.rotateM(model,0,rotation,0f,1f,0f); Matrix.translateM(model,0,x,y,z); Matrix.scaleM(model,0,sx,sy,sz)
            Matrix.multiplyMM(mvp,0,view,0,model,0); Matrix.multiplyMM(mvp,0,projection,0,mvp,0); mesh.draw(mvp,r,g,b)
        }
    }
    private class Mesh {
        private lateinit var v:FloatBuffer; private lateinit var i:ShortBuffer; private var prog=0; private var pos=0; private var mat=0; private var col=0
        init { val stacks=18; val slices=24; val d=ArrayList<Float>(); for(a in 0..stacks){val ph=Math.PI*a/stacks; val yy=cos(ph).toFloat(); val rr=sin(ph).toFloat(); for(b in 0 until slices){val th=2*Math.PI*b/slices; d+=(rr*cos(th)).toFloat(); d+=yy; d+=(rr*sin(th)).toFloat()}}; val q=ArrayList<Short>(); for(a in 0 until stacks)for(b in 0 until slices){val x=(a*slices+b).toShort();val y=(a*slices+(b+1)%slices).toShort();val z=((a+1)*slices+b).toShort();val w=((a+1)*slices+(b+1)%slices).toShort();q+=x;q+=z;q+=y;q+=y;q+=z;q+=w};v=ByteBuffer.allocateDirect(d.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(d.toFloatArray()).position(0)};i=ByteBuffer.allocateDirect(q.size*2).order(ByteOrder.nativeOrder()).asShortBuffer().apply{put(q.toShortArray()).position(0)} }
        fun program(){val vs="attribute vec4 p;uniform mat4 m;void main(){gl_Position=m*p;}";val fs="precision mediump float;uniform vec4 c;void main(){gl_FragColor=c;}";val a=sh(GLES20.GL_VERTEX_SHADER,vs);val b=sh(GLES20.GL_FRAGMENT_SHADER,fs);prog=GLES20.glCreateProgram();GLES20.glAttachShader(prog,a);GLES20.glAttachShader(prog,b);GLES20.glLinkProgram(prog);pos=GLES20.glGetAttribLocation(prog,"p");mat=GLES20.glGetUniformLocation(prog,"m");col=GLES20.glGetUniformLocation(prog,"c")}
        private fun sh(t:Int,s:String)=GLES20.glCreateShader(t).also{GLES20.glShaderSource(it,s);GLES20.glCompileShader(it)}
        fun draw(m:FloatArray,r:Float,g:Float,b:Float){GLES20.glUseProgram(prog);GLES20.glEnableVertexAttribArray(pos);GLES20.glVertexAttribPointer(pos,3,GLES20.GL_FLOAT,false,12,v);GLES20.glUniformMatrix4fv(mat,1,false,m,0);GLES20.glUniform4f(col,r,g,b,1f);GLES20.glDrawElements(GLES20.GL_TRIANGLES,i.capacity(),GLES20.GL_UNSIGNED_SHORT,i);GLES20.glDisableVertexAttribArray(pos)}
    }
}
