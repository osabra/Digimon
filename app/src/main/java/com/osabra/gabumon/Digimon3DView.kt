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

/** Stylized procedural 3D Gabumon. Drag horizontally to rotate. */
class Digimon3DView(context: Context) : GLSurfaceView(context) {
    private val renderer = Renderer()
    private var lastX = 0f
    init { setEGLContextClientVersion(2); setRenderer(renderer); renderMode = RENDERMODE_CONTINUOUSLY }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { lastX = event.x; return true }
            MotionEvent.ACTION_MOVE -> { renderer.rotation += (event.x - lastX) * 0.45f; lastX = event.x; return true }
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
            Matrix.setLookAtM(view, 0, 0f, 1.65f, 6.4f, 0f, 1.45f, 0f, 0f, 1f, 0f)
            drawPart(0f, 1.05f, 0f, 1.05f, 1.12f * breathe, .78f, 0.10f, 0.42f, 0.58f)
            drawPart(0f, 2.25f, -.05f, .78f, .76f * breathe, .70f, 0.90f, 0.93f, 0.86f)
            drawPart(0f, 1.92f, -.62f, .42f, .28f, .32f, 0.93f, 0.94f, 0.88f)
            drawPart(-.58f, 2.68f, -.02f, .18f, .45f, .20f, 0.92f, 0.93f, 0.84f)
            drawPart(.58f, 2.68f, -.02f, .18f, .45f, .20f, 0.92f, 0.93f, 0.84f)
            drawPart(-.36f, .20f, -.03f, .28f, .65f, .30f, 0.08f, 0.30f, 0.42f)
            drawPart(.36f, .20f, -.03f, .28f, .65f, .30f, 0.08f, 0.30f, 0.42f)
            drawPart(-.70f, 1.00f, -.02f, .26f, .65f, .30f, 0.08f, 0.30f, 0.42f)
            drawPart(.70f, 1.00f, -.02f, .26f, .65f, .30f, 0.08f, 0.30f, 0.42f)
            drawPart(-.22f, 2.36f, -.68f, .09f, .10f, .08f, 0.02f, 0.02f, 0.02f)
            drawPart(.22f, 2.36f, -.68f, .09f, .10f, .08f, 0.02f, 0.02f, 0.02f)
            drawPart(0f, 2.70f, -.02f, .12f, .34f, .12f, 0.95f, 0.78f, 0.15f)
            drawPart(-.92f, 1.28f, .18f, .22f, .22f, .75f, 0.08f, 0.30f, 0.42f)
        }
        private fun drawPart(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float, r: Float, g: Float, b: Float) {
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
            val stacks = 14
            val slices = 20
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
            vertices = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(data.toFloatArray()).position(0) }
            indices = ByteBuffer.allocateDirect(idx.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer().apply { put(idx.toShortArray()).position(0) }
        }
        fun createProgram() {
            val vs = "attribute vec4 vPosition; uniform mat4 uMVP; void main(){gl_Position=uMVP*vPosition;}"
            val fs = "precision mediump float; uniform vec4 uColor; void main(){gl_FragColor=uColor;}"
            val v = shader(GLES20.GL_VERTEX_SHADER, vs)
            val f = shader(GLES20.GL_FRAGMENT_SHADER, fs)
            program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, v); GLES20.glAttachShader(program, f); GLES20.glLinkProgram(program)
            GLES20.glDeleteShader(v); GLES20.glDeleteShader(f)
            positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
            mvpHandle = GLES20.glGetUniformLocation(program, "uMVP")
            colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        }
        private fun shader(type: Int, source: String): Int = GLES20.glCreateShader(type).also { GLES20.glShaderSource(it, source); GLES20.glCompileShader(it) }
        fun draw(matrix: FloatArray, r: Float, g: Float, b: Float) {
            GLES20.glUseProgram(program)
            vertices.position(0); indices.position(0)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertices)
            GLES20.glUniformMatrix4fv(mvpHandle, 1, false, matrix, 0)
            GLES20.glUniform4f(colorHandle, r, g, b, 1f)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.capacity(), GLES20.GL_UNSIGNED_SHORT, indices)
            GLES20.glDisableVertexAttribArray(positionHandle)
        }
    }
}
