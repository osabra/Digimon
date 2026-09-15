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

/** Stylized 3D Gabumon-inspired companion. Front-facing by default; drag horizontally to rotate. */
class Digimon3DView(context: Context) : GLSurfaceView(context) {
    private val renderer = Renderer()
    private var lastX = 0f
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { lastX = event.x; return true }
            MotionEvent.ACTION_MOVE -> {
                renderer.rotation += (event.x - lastX) * 0.45f
                lastX = event.x
                return true
            }
        }
        return true
    }

    private class Renderer : GLSurfaceView.Renderer {
        var rotation = 0f
        private var start = System.nanoTime()
        private val projection = FloatArray(16)
        private val view = FloatArray(16)
        private val model = FloatArray(16)
        private val mvp = FloatArray(16)
        private lateinit var mesh: Mesh

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(.015f, .04f, .065f, 1f)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)
            mesh = Mesh()
            mesh.createProgram()
        }
        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            Matrix.perspectiveM(projection, 0, 42f, width.toFloat() / height.toFloat(), .1f, 100f)
        }
        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            val t = (System.nanoTime() - start) / 1_000_000_000f
            val breathe = 1f + sin(t * 2.2f) * .018f
            // The face is on the negative-Z side, so the camera must look from negative Z.
            Matrix.setLookAtM(view, 0, 0f, 1.55f, -7.0f, 0f, 1.45f, 0f, 1f, 0f, 0f)

            part(0f, 1.16f, .05f, 1.02f, 1.16f * breathe, .78f, .10f, .42f, .63f)
            part(0f, 1.12f, -.68f, .67f, .84f * breathe, .18f, .93f, .82f, .48f)
            part(0f, 2.38f, -.03f, .81f, .78f, .72f, .88f, .90f, .86f)
            part(0f, 2.10f, -.65f, .46f, .31f, .31f, .94f, .86f, .70f)
            part(0f, 2.12f, -.94f, .13f, .10f, .09f, .025f, .025f, .025f)

            part(-.58f, 2.82f, -.01f, .20f, .51f, .20f, .12f, .40f, .61f)
            part(.58f, 2.82f, -.01f, .20f, .51f, .20f, .12f, .40f, .61f)
            part(-.58f, 2.83f, -.20f, .09f, .31f, .07f, .93f, .68f, .50f)
            part(.58f, 2.83f, -.20f, .09f, .31f, .07f, .93f, .68f, .50f)

            part(-.25f, 2.42f, -.69f, .105f, .13f, .075f, .07f, .025f, .025f)
            part(.25f, 2.42f, -.69f, .105f, .13f, .075f, .07f, .025f, .025f)
            part(-.25f, 2.43f, -.755f, .060f, .085f, .030f, .55f, .08f, .06f)
            part(.25f, 2.43f, -.755f, .060f, .085f, .030f, .55f, .08f, .06f)
            part(-.22f, 2.48f, -.785f, .022f, .028f, .015f, 1f, 1f, 1f)
            part(.28f, 2.48f, -.785f, .022f, .028f, .015f, 1f, 1f, 1f)

            part(0f, 2.91f, -.02f, .14f, .37f, .14f, .94f, .68f, .13f)
            part(0f, 3.19f, -.02f, .08f, .22f, .08f, .98f, .78f, .18f)

            // Pelt is positioned behind the face and wraps around the shoulders.
            part(0f, 2.00f, .49f, .72f, .82f, .28f, .93f, .90f, .82f)
            part(-.63f, 1.72f, .43f, .33f, .72f, .28f, .94f, .91f, .83f)
            part(.63f, 1.72f, .43f, .33f, .72f, .28f, .94f, .91f, .83f)
            stripe(-.63f, 2.05f, .48f, .34f, .10f, .30f)
            stripe(.63f, 2.05f, .48f, .34f, .10f, .30f)
            stripe(-.78f, 1.70f, .42f, .28f, .10f, .27f)
            stripe(.78f, 1.70f, .42f, .28f, .10f, .27f)
            stripe(-.84f, 1.38f, -.01f, .24f, .11f, .30f)
            stripe(.84f, 1.38f, -.01f, .24f, .11f, .30f)

            part(-.83f, 1.25f, -.02f, .25f, .65f, .28f, .10f, .42f, .63f)
            part(.83f, 1.25f, -.02f, .25f, .65f, .28f, .10f, .42f, .63f)
            part(-.84f, .73f, -.25f, .27f, .23f, .31f, .94f, .88f, .73f)
            part(.84f, .73f, -.25f, .27f, .23f, .31f, .94f, .88f, .73f)
            claw(-.98f, .67f, -.49f); claw(-.80f, .63f, -.52f); claw(.98f, .67f, -.49f); claw(.80f, .63f, -.52f)

            part(-.38f, .22f, -.02f, .31f, .66f, .35f, .10f, .42f, .63f)
            part(.38f, .22f, -.02f, .31f, .66f, .35f, .10f, .42f, .63f)
            part(-.40f, -.24f, -.34f, .38f, .19f, .54f, .94f, .88f, .73f)
            part(.40f, -.24f, -.34f, .38f, .19f, .54f, .94f, .88f, .73f)
            claw(-.58f, -.27f, -.72f); claw(-.39f, -.29f, -.76f); claw(.39f, -.29f, -.76f); claw(.58f, -.27f, -.72f)

            part(-1.02f, 1.28f, .52f, .25f, .25f, .92f, .10f, .42f, .63f)
            part(-1.22f, 1.55f, .88f, .31f, .31f, .50f, .94f, .70f, .16f)
            part(0f, 1.70f, .78f, .48f, .66f, .18f, .94f, .70f, .16f)
        }

        private fun stripe(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float) = part(x,y,z,sx,sy,sz,.08f,.35f,.60f)
        private fun claw(x:Float,y:Float,z:Float) = part(x,y,z,.055f,.14f,.09f,.72f,.05f,.05f)

        private fun part(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float, r: Float, g: Float, b: Float) {
            Matrix.setIdentityM(model, 0)
            Matrix.translateM(model, 0, x, y, z)
            Matrix.rotateM(model, 0, rotation, 0f, 1f, 0f)
            Matrix.scaleM(model, 0, sx, sy, sz)
            Matrix.multiplyMM(mvp, 0, view, 0, model, 0)
            Matrix.multiplyMM(mvp, 0, projection, 0, mvp, 0)
            mesh.draw(mvp, r, g, b)
        }
    }

    private class Mesh {
        private lateinit var vertices: FloatBuffer
        private lateinit var indices: ShortBuffer
        private var program = 0
        private var positionHandle = 0
        private var mvpHandle = 0
        private var colorHandle = 0
        init {
            val stacks = 20; val slices = 28; val data = ArrayList<Float>()
            for (i in 0..stacks) { val v=i.toFloat()/stacks; val phi=Math.PI*v; val y=cos(phi).toFloat(); val ring=sin(phi).toFloat(); for(j in 0 until slices){val theta=2.0*Math.PI*j/slices; data += (ring*cos(theta)).toFloat(); data += y; data += (ring*sin(theta)).toFloat()} }
            val idx=ArrayList<Short>(); for(i in 0 until stacks) for(j in 0 until slices){val a=(i*slices+j).toShort(); val b=(i*slices+(j+1)%slices).toShort(); val c=((i+1)*slices+j).toShort(); val d=((i+1)*slices+(j+1)%slices).toShort(); idx+=a;idx+=c;idx+=b;idx+=b;idx+=c;idx+=d}
            vertices=ByteBuffer.allocateDirect(data.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(data.toFloatArray()).position(0)}
            indices=ByteBuffer.allocateDirect(idx.size*2).order(ByteOrder.nativeOrder()).asShortBuffer().apply{put(idx.toShortArray()).position(0)}
        }
        fun createProgram(){val vs="attribute vec4 vPosition; uniform mat4 uMVP; void main(){gl_Position=uMVP*vPosition;}"; val fs="precision mediump float; uniform vec4 uColor; void main(){gl_FragColor=uColor;}"; val v=shader(GLES20.GL_VERTEX_SHADER,vs); val f=shader(GLES20.GL_FRAGMENT_SHADER,fs); program=GLES20.glCreateProgram();GLES20.glAttachShader(program,v);GLES20.glAttachShader(program,f);GLES20.glLinkProgram(program);GLES20.glDeleteShader(v);GLES20.glDeleteShader(f);positionHandle=GLES20.glGetAttribLocation(program,"vPosition");mvpHandle=GLES20.glGetUniformLocation(program,"uMVP");colorHandle=GLES20.glGetUniformLocation(program,"uColor")}
        private fun shader(type:Int,source:String):Int=GLES20.glCreateShader(type).also{GLES20.glShaderSource(it,source);GLES20.glCompileShader(it)}
        fun draw(matrix:FloatArray,r:Float,g:Float,b:Float){if(program==0)return;GLES20.glUseProgram(program);vertices.position(0);indices.position(0);GLES20.glEnableVertexAttribArray(positionHandle);GLES20.glVertexAttribPointer(positionHandle,3,GLES20.GL_FLOAT,false,12,vertices);GLES20.glUniformMatrix4fv(mvpHandle,1,false,matrix,0);GLES20.glUniform4f(colorHandle,r,g,b,1f);GLES20.glDrawElements(GLES20.GL_TRIANGLES,indices.capacity(),GLES20.GL_UNSIGNED_SHORT,indices);GLES20.glDisableVertexAttribArray(positionHandle)}
    }
}
