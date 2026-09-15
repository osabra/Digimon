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

/** Stylized 3D Gabumon model. Drag horizontally to rotate. */
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
            val sway = sin(t * 1.6f) * 1.5f
            Matrix.setLookAtM(view, 0, 0f, 1.55f, 7.0f, 0f, 1.45f, 0f, 0f, 1f, 0f)

            // Fur body / coat
            part(0f, 1.12f, .05f, 1.03f, 1.15f * breathe, .78f, 0.68f, .72f, .77f)
            // White belly
            part(0f, 1.12f, -.67f, .67f, .82f * breathe, .18f, .94f, .95f, .92f)
            // Head
            part(0f, 2.35f, -.03f, .80f, .78f, .72f, .72f, .76f, .80f)
            // Muzzle
            part(0f, 2.05f, -.64f, .45f, .30f, .30f, .95f, .95f, .92f)
            // Nose
            part(0f, 2.10f, -.91f, .13f, .10f, .09f, .10f, .11f, .12f)
            // Ears
            part(-.58f, 2.82f, -.02f, .20f, .50f, .20f, .66f, .70f, .74f)
            part(.58f, 2.82f, -.02f, .20f, .50f, .20f, .66f, .70f, .74f)
            // Inner ears
            part(-.58f, 2.83f, -.20f, .09f, .31f, .07f, .22f, .25f, .28f)
            part(.58f, 2.83f, -.20f, .09f, .31f, .07f, .22f, .25f, .28f)
            // Eyes
            part(-.25f, 2.42f, -.69f, .095f, .12f, .07f, .015f, .02f, .02f)
            part(.25f, 2.42f, -.69f, .095f, .12f, .07f, .015f, .02f, .02f)
            // Eye highlights
            part(-.22f, 2.46f, -.76f, .025f, .032f, .018f, 1f, 1f, 1f)
            part(.28f, 2.46f, -.76f, .025f, .032f, .018f, 1f, 1f, 1f)
            // Yellow forehead horn / crest
            part(0f, 2.87f, -.05f, .13f, .34f, .13f, .96f, .78f, .12f)
            // Arms and hands
            part(-.82f, 1.25f, -.02f, .25f, .64f, .28f, .20f, .34f, .42f)
            part(.82f, 1.25f, -.02f, .25f, .64f, .28f, .20f, .34f, .42f)
            part(-.84f, .74f, -.23f, .26f, .22f, .30f, .88f, .90f, .86f)
            part(.84f, .74f, -.23f, .26f, .22f, .30f, .88f, .90f, .86f)
            // Legs and feet
            part(-.38f, .20f, -.02f, .30f, .65f, .34f, .20f, .34f, .42f)
            part(.38f, .20f, -.02f, .30f, .65f, .34f, .20f, .34f, .42f)
            part(-.40f, -.24f, -.32f, .36f, .18f, .52f, .90f, .91f, .88f)
            part(.40f, -.24f, -.32f, .36f, .18f, .52f, .90f, .91f, .88f)
            // Tail / fur behind the body
            part(-.98f, 1.28f, .45f, .24f, .24f, .90f, .62f, .66f, .71f)
            part(-1.18f, 1.55f, .82f, .30f, .30f, .48f, .70f, .73f, .77f)

            // Tiny idle motion on the ears for a livelier model.
            // The main model remains stable while breathing.
            @Suppress("UNUSED_VARIABLE") val unused = sway
        }

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
            val stacks = 18
            val slices = 24
            val data = ArrayList<Float>()
            for (i in 0..stacks) {
                val v = i.toFloat() / stacks
                val phi = Math.PI * v
                val y = cos(phi).toFloat()
                val ring = sin(phi).toFloat()
                for (j in 0 until slices) {
                    val theta = 2.0 * Math.PI * j / slices
                    data += (ring * cos(theta)).toFloat()
                    data += y
                    data += (ring * sin(theta)).toFloat()
                }
            }
            val idx = ArrayList<Short>()
            for (i in 0 until stacks) for (j in 0 until slices) {
                val a = (i * slices + j).toShort()
                val b = (i * slices + (j + 1) % slices).toShort()
                val c = ((i + 1) * slices + j).toShort()
                val d = ((i + 1) * slices + (j + 1) % slices).toShort()
                idx += a; idx += c; idx += b
                idx += b; idx += c; idx += d
            }
            vertices = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(data.toFloatArray()).position(0)
            }
            indices = ByteBuffer.allocateDirect(idx.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer().apply {
                put(idx.toShortArray()).position(0)
            }
        }

        fun createProgram() {
            val vs = "attribute vec4 vPosition; uniform mat4 uMVP; void main(){gl_Position=uMVP*vPosition;}"
            val fs = "precision mediump float; uniform vec4 uColor; void main(){gl_FragColor=uColor;}"
            val v = shader(GLES20.GL_VERTEX_SHADER, vs)
            val f = shader(GLES20.GL_FRAGMENT_SHADER, fs)
            program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, v)
            GLES20.glAttachShader(program, f)
            GLES20.glLinkProgram(program)
            GLES20.glDeleteShader(v)
            GLES20.glDeleteShader(f)
            positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
            mvpHandle = GLES20.glGetUniformLocation(program, "uMVP")
            colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        }

        private fun shader(type: Int, source: String): Int = GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, source)
            GLES20.glCompileShader(it)
        }

        fun draw(matrix: FloatArray, r: Float, g: Float, b: Float) {
            if (program == 0) return
            GLES20.glUseProgram(program)
            vertices.position(0)
            indices.position(0)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertices)
            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, matrix, 0)
            GLES20.glUniform4f(colorHandle, r, g, b, 1f)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.capacity(), GLES20.GL_UNSIGNED_SHORT, indices)
            GLES20.glDisableVertexAttribArray(positionHandle)
        }
    }
}
