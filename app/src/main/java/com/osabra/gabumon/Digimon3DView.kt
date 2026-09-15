package com.osabra.gabumon

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** Lightweight interactive 3D placeholder renderer. It will be replaced by real GLB models. */
class Digimon3DView(context: Context) : GLSurfaceView(context) {
    private val renderer = Renderer()
    private var lastX = 0f
    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) { lastX = event.x; return true }
        if (event.action == MotionEvent.ACTION_MOVE) {
            renderer.rotation += (event.x - lastX) * 0.5f
            lastX = event.x
            return true
        }
        return true
    }
    private class Renderer : GLSurfaceView.Renderer {
        var rotation = 0f
        private val projection = FloatArray(16)
        private val view = FloatArray(16)
        private val model = FloatArray(16)
        private val mvp = FloatArray(16)
        private val cube = Cube()
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(0.02f, 0.05f, 0.08f, 1f)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }
        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            Matrix.perspectiveM(projection, 0, 45f, width.toFloat() / height, .1f, 100f)
        }
        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            Matrix.setLookAtM(view, 0, 0f, 1.3f, 5.5f, 0f, 1.1f, 0f, 0f, 1f, 0f)
            part(0f, 1.15f, 0f, 1.05f, 1.15f, .75f)
            part(0f, 2.2f, 0f, .72f, .72f, .68f)
            part(-.72f, 2.2f, 0f, .25f, .5f, .35f)
            part(.72f, 2.2f, 0f, .25f, .5f, .35f)
            part(-.35f, .15f, 0f, .35f, .7f, .4f)
            part(.35f, .15f, 0f, .35f, .7f, .4f)
        }
        private fun part(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float) {
            Matrix.setIdentityM(model, 0)
            Matrix.translateM(model, 0, x, y, z)
            Matrix.rotateM(model, 0, rotation, 0f, 1f, 0f)
            Matrix.scaleM(model, 0, sx, sy, sz)
            Matrix.multiplyMM(mvp, 0, view, 0, model, 0)
            Matrix.multiplyMM(mvp, 0, projection, 0, mvp, 0)
            cube.draw(mvp)
        }
    }
    private class Cube {
        private val vertices: FloatBuffer = ByteBuffer.allocateDirect(8 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(floatArrayOf(-1f,-1f,-1f, 1f,-1f,-1f, 1f,1f,-1f, -1f,1f,-1f, -1f,-1f,1f, 1f,-1f,1f, 1f,1f,1f, -1f,1f,1f)).position(0)
        }
        private val indices = ByteBuffer.allocateDirect(36).apply {
            put(byteArrayOf(0,1,2,0,2,3,4,6,5,4,7,6,0,4,5,0,5,1,2,6,7,2,7,3,0,3,7,0,7,4,1,5,6,1,6,2)).position(0)
        }
        private val program: Int
        init {
            val vs = "attribute vec4 vPosition; uniform mat4 uMVP; void main(){gl_Position=uMVP*vPosition;}"
            val fs = "precision mediump float; void main(){gl_FragColor=vec4(0.18,0.62,0.78,1.0);}"
            program = GLES20.glCreateProgram().also { p ->
                GLES20.glAttachShader(p, shader(GLES20.GL_VERTEX_SHADER, vs))
                GLES20.glAttachShader(p, shader(GLES20.GL_FRAGMENT_SHADER, fs))
                GLES20.glLinkProgram(p)
            }
        }
        private fun shader(type:Int, source:String):Int = GLES20.glCreateShader(type).also { s -> GLES20.glShaderSource(s, source); GLES20.glCompileShader(s) }
        fun draw(mvp:FloatArray) {
            GLES20.glUseProgram(program)
            val pos=GLES20.glGetAttribLocation(program,"vPosition")
            val mat=GLES20.glGetUniformLocation(program,"uMVP")
            GLES20.glEnableVertexAttribArray(pos)
            vertices.position(0)
            GLES20.glVertexAttribPointer(pos,3,GLES20.GL_FLOAT,false,12,vertices)
            GLES20.glUniformMatrix4fv(mat,1,false,mvp,0)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,36,GLES20.GL_UNSIGNED_BYTE,indices)
            GLES20.glDisableVertexAttribArray(pos)
        }
    }
}
