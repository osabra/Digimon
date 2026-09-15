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
    private val renderer = Renderer()
    private var lastX = 0f

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { lastX = e.x; return true }
            MotionEvent.ACTION_MOVE -> {
                renderer.rotation += (e.x - lastX) * .42f
                lastX = e.x
                return true
            }
        }
        return true
    }

    private class Renderer : GLSurfaceView.Renderer {
        var rotation = 0f
        private val p = FloatArray(16)
        private val v = FloatArray(16)
        private val m = FloatArray(16)
        private val mvp = FloatArray(16)
        private lateinit var mesh: Mesh
        private var t0 = System.nanoTime()

        override fun onSurfaceCreated(g: GL10?, c: EGLConfig?) {
            GLES20.glClearColor(.006f, .018f, .032f, 1f)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)
            mesh = Mesh()
            mesh.program()
        }

        override fun onSurfaceChanged(g: GL10?, w: Int, h: Int) {
            GLES20.glViewport(0, 0, w, h)
            Matrix.perspectiveM(p, 0, 38f, w.toFloat() / h, .1f, 100f)
        }

        override fun onDrawFrame(g: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            val t = (System.nanoTime() - t0) / 1e9f
            val breathe = 1f + sin(t * 2.1f) * .009f

            Matrix.setLookAtM(v, 0, 0f, 1.48f, -7.4f, 0f, 1.42f, 0f, 0f, 1f, 0f)

            // Cuerpo compacto y redondeado.
            part(0f, 1.03f, .02f, .91f, 1.03f * breathe, .66f, BLUE)
            part(0f, 1.03f, -.58f, .55f, .72f * breathe, .17f, BELLY)

            // Cabeza, mejillas y hocico.
            part(0f, 2.30f, 0f, .73f, .70f, .62f, PELT)
            part(-.28f, 2.16f, -.54f, .30f, .23f, .25f, MUZZLE)
            part(.28f, 2.16f, -.54f, .30f, .23f, .25f, MUZZLE)
            part(0f, 2.08f, -.79f, .14f, .09f, .07f, BLACK)

            // Orejas azules con interior rosado.
            part(-.53f, 2.77f, .02f, .18f, .45f, .18f, BLUE)
            part(.53f, 2.77f, .02f, .18f, .45f, .18f, BLUE)
            part(-.53f, 2.79f, -.17f, .087f, .27f, .055f, PINK)
            part(.53f, 2.79f, -.17f, .087f, .27f, .055f, PINK)

            // Ojos grandes y expresivos.
            eye(-.245f)
            eye(.245f)

            // Cuerno amarillo.
            part(0f, 2.91f, -.01f, .13f, .32f, .12f, GOLD)
            part(0f, 3.16f, -.01f, .072f, .18f, .066f, YELLOW)

            // Piel de Gabumon, escalonada para una silueta más cercana a una piel real.
            part(0f, 1.90f, .43f, .69f, .76f, .27f, PELT)
            part(-.58f, 1.70f, .40f, .30f, .64f, .25f, PELT)
            part(.58f, 1.70f, .40f, .30f, .64f, .25f, PELT)
            part(0f, 2.32f, .43f, .58f, .48f, .24f, PELT)
            tuft(-.30f, 2.02f, .58f, .23f, .15f, .10f)
            tuft(.30f, 2.02f, .58f, .23f, .15f, .10f)
            tuft(-.47f, 1.72f, .55f, .20f, .13f, .10f)
            tuft(.47f, 1.72f, .55f, .20f, .13f, .10f)
            tuft(0f, 1.48f, .57f, .28f, .13f, .10f)

            // Rayas azules características.
            stripe(-.60f, 2.04f, .46f, .30f, .075f, .27f)
            stripe(.60f, 2.04f, .46f, .30f, .075f, .27f)
            stripe(-.75f, 1.73f, .41f, .23f, .075f, .23f)
            stripe(.75f, 1.73f, .41f, .23f, .075f, .23f)
            stripe(-.47f, 1.47f, .50f, .19f, .065f, .20f)
            stripe(.47f, 1.47f, .50f, .19f, .065f, .20f)

            // Brazos y manos.
            arm(-.82f)
            arm(.82f)

            // Piernas y pies.
            part(-.34f, .30f, 0f, .30f, .60f, .32f, BLUE)
            part(.34f, .30f, 0f, .30f, .60f, .32f, BLUE)
            part(-.38f, -.17f, -.34f, .39f, .18f, .47f, FOOT)
            part(.38f, -.17f, -.34f, .39f, .18f, .47f, FOOT)
            claw(-.53f, -.28f, -.68f); claw(-.38f, -.30f, -.72f)
            claw(.38f, -.30f, -.72f); claw(.53f, -.28f, -.68f)

            // Cola redondeada hacia atrás.
            part(.91f, 1.14f, .43f, .23f, .25f, .72f, BLUE)
            part(1.20f, 1.30f, .72f, .27f, .26f, .42f, BLUE)
            part(1.40f, 1.45f, .96f, .20f, .20f, .27f, BLUE)
        }

        private fun eye(x: Float) {
            part(x, 2.38f, -.625f, .108f, .14f, .065f, BLACK)
            part(x, 2.40f, -.693f, .052f, .082f, .018f, RED)
            part(x + .018f, 2.445f, -.713f, .018f, .025f, .01f, WHITE)
        }

        private fun arm(x: Float) {
            part(x, 1.18f, -.01f, .24f, .59f, .25f, BLUE)
            part(x, .66f, -.28f, .25f, .22f, .27f, BLUE)
            claw(x - .10f, .63f, -.51f)
            claw(x + .10f, .63f, -.51f)
        }

        private fun stripe(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float) =
            part(x, y, z, sx, sy, sz, STRIPE)

        private fun tuft(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float) =
            part(x, y, z, sx, sy, sz, PELT_HIGHLIGHT)

        private fun claw(x: Float, y: Float, z: Float) =
            part(x, y, z, .052f, .13f, .075f, CLAW)

        private fun part(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float, c: FloatArray) {
            Matrix.setIdentityM(m, 0)
            Matrix.rotateM(m, 0, rotation, 0f, 1f, 0f)
            Matrix.translateM(m, 0, x, y, z)
            Matrix.scaleM(m, 0, sx, sy, sz)
            Matrix.multiplyMM(mvp, 0, v, 0, m, 0)
            Matrix.multiplyMM(mvp, 0, p, 0, mvp, 0)
            mesh.draw(mvp, c[0], c[1], c[2])
        }
    }

    private class Mesh {
        private lateinit var vb: FloatBuffer
        private lateinit var ib: ShortBuffer
        private var prog = 0
        private var pos = 0
        private var mat = 0
        private var col = 0
        private var light = 0

        init {
            val rings = 22
            val segs = 32
            val d = ArrayList<Float>()
            for (a in 0..rings) {
                val ph = Math.PI * a / rings
                val y = cos(ph).toFloat()
                val r = sin(ph).toFloat()
                for (b in 0 until segs) {
                    val th = 2 * Math.PI * b / segs
                    d += (r * cos(th)).toFloat()
                    d += y
                    d += (r * sin(th)).toFloat()
                }
            }
            val q = ArrayList<Short>()
            for (a in 0 until rings) for (b in 0 until segs) {
                val x = (a * segs + b).toShort()
                val y = (a * segs + (b + 1) % segs).toShort()
                val z = ((a + 1) * segs + b).toShort()
                val w = ((a + 1) * segs + (b + 1) % segs).toShort()
                q += x; q += z; q += y; q += y; q += z; q += w
            }
            vb = ByteBuffer.allocateDirect(d.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
                .apply { put(d.toFloatArray()).position(0) }
            ib = ByteBuffer.allocateDirect(q.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
                .apply { put(q.toShortArray()).position(0) }
        }

        fun program() {
            val vs = """
                attribute vec4 p;
                uniform mat4 m;
                varying vec3 n;
                void main(){
                    n=normalize(p.xyz);
                    gl_Position=m*p;
                }
            """.trimIndent()
            val fs = """
                precision mediump float;
                uniform vec4 c;
                uniform vec3 light;
                varying vec3 n;
                void main(){
                    float d=max(dot(normalize(n),normalize(light)),0.0);
                    float rim=pow(1.0-max(dot(normalize(n),vec3(0.0,0.0,1.0)),0.0),2.0);
                    float spec=pow(max(dot(reflect(-normalize(light),normalize(n)),vec3(0.0,0.0,1.0)),0.0),20.0);
                    vec3 base=c.rgb*(0.44+0.56*d);
                    base += c.rgb*0.08*rim;
                    base += vec3(1.0)*0.06*spec;
                    gl_FragColor=vec4(base,c.a);
                }
            """.trimIndent()
            val a = sh(GLES20.GL_VERTEX_SHADER, vs)
            val b = sh(GLES20.GL_FRAGMENT_SHADER, fs)
            prog = GLES20.glCreateProgram()
            GLES20.glAttachShader(prog, a)
            GLES20.glAttachShader(prog, b)
            GLES20.glLinkProgram(prog)
            pos = GLES20.glGetAttribLocation(prog, "p")
            mat = GLES20.glGetUniformLocation(prog, "m")
            col = GLES20.glGetUniformLocation(prog, "c")
            light = GLES20.glGetUniformLocation(prog, "light")
        }

        private fun sh(t: Int, s: String) = GLES20.glCreateShader(t).also {
            GLES20.glShaderSource(it, s)
            GLES20.glCompileShader(it)
        }

        fun draw(m: FloatArray, r: Float, g: Float, b: Float) {
            GLES20.glUseProgram(prog)
            GLES20.glEnableVertexAttribArray(pos)
            GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 12, vb)
            GLES20.glUniformMatrix4fv(mat, 1, false, m, 0)
            GLES20.glUniform4f(col, r, g, b, 1f)
            GLES20.glUniform3f(light, -.35f, .80f, -.75f)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, ib.capacity(), GLES20.GL_UNSIGNED_SHORT, ib)
            GLES20.glDisableVertexAttribArray(pos)
        }
    }

    companion object {
        private val BLUE = floatArrayOf(.055f, .34f, .58f)
        private val PELT = floatArrayOf(.91f, .89f, .80f)
        private val PELT_HIGHLIGHT = floatArrayOf(.98f, .96f, .88f)
        private val MUZZLE = floatArrayOf(.94f, .83f, .63f)
        private val BELLY = floatArrayOf(1f, .67f, .055f)
        private val YELLOW = floatArrayOf(1f, .72f, .07f)
        private val GOLD = floatArrayOf(1f, .83f, .14f)
        private val PINK = floatArrayOf(1f, .40f, .36f)
        private val BLACK = floatArrayOf(.012f, .012f, .016f)
        private val RED = floatArrayOf(.78f, .025f, .035f)
        private val WHITE = floatArrayOf(1f, 1f, 1f)
        private val STRIPE = floatArrayOf(.035f, .22f, .43f)
        private val FOOT = floatArrayOf(.90f, .84f, .69f)
        private val CLAW = floatArrayOf(.72f, .025f, .025f)
    }
}
