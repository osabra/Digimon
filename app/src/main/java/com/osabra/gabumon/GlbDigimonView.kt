package com.osabra.gabumon

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.view.MotionEvent
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lightweight glTF 2.0/GLB viewer used by the Digimon companion screen.
 * Supports mesh primitives, POSITION/NORMAL/TEXCOORD_0, indexed geometry,
 * node transforms and embedded base-color textures. Unsupported features
 * (skins, morph targets, animations) are intentionally left for a later pass.
 */
class GlbDigimonView(context: Context, initialDigimon: String = "Gabumon") : GLSurfaceView(context) {
    private val renderer = Renderer(context.applicationContext, initialDigimon)
    private var lastX = 0f

    init {
        setEGLContextClientVersion(3)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun setDigimon(name: String) { renderer.name = name }

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

    private class Renderer(
        private val context: Context,
        var name: String
    ) : GLSurfaceView.Renderer {
        var rotation = 0f
        private val projection = FloatArray(16)
        private val view = FloatArray(16)
        private val spin = FloatArray(16)
        private val model = FloatArray(16)
        private val pv = FloatArray(16)
        private val mvp = FloatArray(16)
        private val inverse = FloatArray(16)
        private val normal = FloatArray(9)
        private var program = 0
        private var posLoc = -1
        private var normalLoc = -1
        private var uvLoc = -1
        private var mvpLoc = -1
        private var normalMatLoc = -1
        private var colorLoc = -1
        private var texLoc = -1
        private var useTexLoc = -1
        private var lightLoc = -1
        private var startNs = System.nanoTime()
        private var assetName = ""
        private var modelData: Model? = null

        override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
            GLES30.glClearColor(0.006f, 0.018f, 0.032f, 1f)
            GLES30.glEnable(GLES30.GL_DEPTH_TEST)
            GLES30.glEnable(GLES30.GL_CULL_FACE)
            program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
            posLoc = GLES30.glGetAttribLocation(program, "aPosition")
            normalLoc = GLES30.glGetAttribLocation(program, "aNormal")
            uvLoc = GLES30.glGetAttribLocation(program, "aUv")
            mvpLoc = GLES30.glGetUniformLocation(program, "uMvp")
            normalMatLoc = GLES30.glGetUniformLocation(program, "uNormalMat")
            colorLoc = GLES30.glGetUniformLocation(program, "uColor")
            texLoc = GLES30.glGetUniformLocation(program, "uTexture")
            useTexLoc = GLES30.glGetUniformLocation(program, "uUseTexture")
            lightLoc = GLES30.glGetUniformLocation(program, "uLight")
            startNs = System.nanoTime()
        }

        override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
            Matrix.perspectiveM(projection, 0, 35f, width.toFloat() / height.toFloat(), 0.05f, 100f)
        }

        override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
            val wanted = "$name.glb"
            if (wanted != assetName) {
                assetName = wanted
                modelData?.dispose()
                modelData = try { GlbParser(context).load("digimon/$wanted") } catch (_: Throwable) { null }
            }
            val data = modelData ?: return
            val t = (System.nanoTime() - startNs) / 1_000_000_000f
            val bob = sin(t * 2.0f) * 0.018f
            Matrix.setLookAtM(view, 0, 0f, 1.45f, 6.4f, 0f, 1.25f, 0f, 0f, 1f, 0f)
            Matrix.setIdentityM(spin, 0)
            Matrix.rotateM(spin, 0, rotation, 0f, 1f, 0f)
            Matrix.translateM(spin, 0, 0f, bob, 0f)
            Matrix.multiplyMM(pv, 0, projection, 0, view, 0)

            GLES30.glUseProgram(program)
            GLES30.glUniform3f(lightLoc, -0.45f, 0.85f, 0.70f)
            for (part in data.parts) {
                Matrix.multiplyMM(model, 0, spin, 0, part.nodeMatrix, 0)
                Matrix.multiplyMM(mvp, 0, pv, 0, model, 0)
                GLES30.glUniformMatrix4fv(mvpLoc, 1, false, mvp, 0)
                if (!Matrix.invertM(inverse, 0, model, 0)) Matrix.setIdentityM(inverse, 0)
                Matrix.transposeM(inverse, 0, inverse, 0)
                normal[0] = inverse[0]; normal[1] = inverse[1]; normal[2] = inverse[2]
                normal[3] = inverse[4]; normal[4] = inverse[5]; normal[5] = inverse[6]
                normal[6] = inverse[8]; normal[7] = inverse[9]; normal[8] = inverse[10]
                GLES30.glUniformMatrix3fv(normalMatLoc, 1, false, normal, 0)
                GLES30.glUniform4f(colorLoc, part.color[0], part.color[1], part.color[2], part.color[3])
                GLES30.glUniform1i(useTexLoc, if (part.textureId != 0) 1 else 0)
                if (part.textureId != 0) {
                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, part.textureId)
                    GLES30.glUniform1i(texLoc, 0)
                }
                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, part.vbo)
                GLES30.glEnableVertexAttribArray(posLoc)
                GLES30.glVertexAttribPointer(posLoc, 3, GLES30.GL_FLOAT, false, 32, 0)
                GLES30.glEnableVertexAttribArray(normalLoc)
                GLES30.glVertexAttribPointer(normalLoc, 3, GLES30.GL_FLOAT, false, 32, 12)
                GLES30.glEnableVertexAttribArray(uvLoc)
                GLES30.glVertexAttribPointer(uvLoc, 2, GLES30.GL_FLOAT, false, 32, 24)
                GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, part.ibo)
                GLES30.glDrawElements(part.mode, part.indexCount, part.indexType, 0)
                GLES30.glDisableVertexAttribArray(posLoc)
                GLES30.glDisableVertexAttribArray(normalLoc)
                GLES30.glDisableVertexAttribArray(uvLoc)
            }
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
        }
    }

    private data class Part(
        val vbo: Int,
        val ibo: Int,
        val indexCount: Int,
        val indexType: Int,
        val mode: Int,
        val nodeMatrix: FloatArray,
        val color: FloatArray,
        val textureId: Int
    )

    private class Model(val parts: List<Part>) {
        fun dispose() {
            val buffers = IntArray(parts.size * 2)
            var i = 0
            parts.forEach { buffers[i++] = it.vbo; buffers[i++] = it.ibo }
            if (buffers.isNotEmpty()) GLES30.glDeleteBuffers(buffers.size, buffers, 0)
            parts.map { it.textureId }.filter { it != 0 }.distinct().forEach {
                GLES30.glDeleteTextures(1, intArrayOf(it), 0)
            }
        }
    }

    private class GlbParser(private val context: Context) {
        fun load(assetPath: String): Model {
            val bytes = context.assets.open(assetPath).use { it.readBytes() }
            require(bytes.size >= 20)
            val file = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            require(file.int == 0x46546C67) { "Not a GLB file" }
            require(file.int == 2) { "Only glTF 2.0 is supported" }
            file.int
            var json: JSONObject? = null
            var bin = ByteArray(0)
            while (file.remaining() >= 8) {
                val length = file.int
                val type = file.int
                val chunk = ByteArray(length)
                file.get(chunk)
                when (type) {
                    0x4E4F534A -> json = JSONObject(String(chunk, Charsets.UTF_8).trim('\u0000', ' ', '\n', '\r', '\t'))
                    0x004E4942 -> bin = chunk
                }
            }
            val root = requireNotNull(json)
            val buffers = root.optJSONArray("buffers") ?: JSONArray()
            val bufferViews = root.optJSONArray("bufferViews") ?: JSONArray()
            val accessors = root.optJSONArray("accessors") ?: JSONArray()
            val meshes = root.optJSONArray("meshes") ?: JSONArray()
            val materials = root.optJSONArray("materials") ?: JSONArray()
            val textures = root.optJSONArray("textures") ?: JSONArray()
            val images = root.optJSONArray("images") ?: JSONArray()
            val nodes = root.optJSONArray("nodes") ?: JSONArray()
            val parts = ArrayList<Part>()
            val nodeMatrices = Array(nodes.length()) { FloatArray(16) }
            val worldDone = BooleanArray(nodes.length())
            fun nodeWorld(index: Int): FloatArray {
                if (worldDone[index]) return nodeMatrices[index]
                val n = nodes.getJSONObject(index)
                val local = nodeLocal(n)
                val parent = findParent(nodes, index)
                if (parent >= 0) {
                    val out = FloatArray(16)
                    Matrix.multiplyMM(out, 0, nodeWorld(parent), 0, local, 0)
                    nodeMatrices[index] = out
                } else nodeMatrices[index] = local
                worldDone[index] = true
                return nodeMatrices[index]
            }
            for (i in 0 until nodes.length()) nodeWorld(i)
            for (nodeIndex in 0 until nodes.length()) {
                val node = nodes.getJSONObject(nodeIndex)
                val meshIndex = node.optInt("mesh", -1)
                if (meshIndex < 0 || meshIndex >= meshes.length()) continue
                val mesh = meshes.getJSONObject(meshIndex)
                val primitives = mesh.optJSONArray("primitives") ?: continue
                for (p in 0 until primitives.length()) {
                    val primitive = primitives.getJSONObject(p)
                    val attrs = primitive.getJSONObject("attributes")
                    val pos = readVec3(accessors, bufferViews, buffers, bin, attrs.getInt("POSITION"))
                    val normals = if (attrs.has("NORMAL")) readVec3(accessors, bufferViews, buffers, bin, attrs.getInt("NORMAL")) else generatedNormals(pos)
                    val uvs = if (attrs.has("TEXCOORD_0")) readVec2(accessors, bufferViews, buffers, bin, attrs.getInt("TEXCOORD_0")) else FloatArray((pos.size / 3) * 2)
                    val vertexCount = pos.size / 3
                    require(normals.size >= vertexCount * 3)
                    val packed = FloatArray(vertexCount * 8)
                    for (v in 0 until vertexCount) {
                        val a = v * 3; val b = v * 2; val c = v * 8
                        packed[c] = pos[a]; packed[c + 1] = pos[a + 1]; packed[c + 2] = pos[a + 2]
                        packed[c + 3] = normals[a]; packed[c + 4] = normals[a + 1]; packed[c + 5] = normals[a + 2]
                        packed[c + 6] = uvs[b]; packed[c + 7] = uvs[b + 1]
                    }
                    val vbo = glBuffer(GLES30.GL_ARRAY_BUFFER, packed)
                    val indexInfo = if (primitive.has("indices")) readIndices(accessors, bufferViews, buffers, bin, primitive.getInt("indices")) else sequentialIndices(vertexCount)
                    val ibo = glBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexInfo.buffer)
                    val materialIndex = primitive.optInt("material", -1)
                    val material = if (materialIndex in 0 until materials.length()) materials.getJSONObject(materialIndex) else null
                    val color = materialColor(material)
                    val textureId = loadBaseColorTexture(material, textures, images, bufferViews, buffers, bin)
                    parts += Part(vbo, ibo, indexInfo.count, indexInfo.type, primitive.optInt("mode", GLES30.GL_TRIANGLES), nodeWorld(nodeIndex), color, textureId)
                }
            }
            return Model(parts)
        }

        private fun nodeLocal(n: JSONObject): FloatArray {
            val out = FloatArray(16)
            if (n.has("matrix")) {
                val a = n.getJSONArray("matrix")
                for (i in 0 until 16) out[i] = a.getDouble(i).toFloat()
                return out
            }
            Matrix.setIdentityM(out, 0)
            val t = n.optJSONArray("translation")
            if (t != null) Matrix.translateM(out, 0, t.getDouble(0).toFloat(), t.getDouble(1).toFloat(), t.getDouble(2).toFloat())
            val q = n.optJSONArray("rotation")
            if (q != null) multiplyInPlace(out, quatMatrix(q))
            val s = n.optJSONArray("scale")
            if (s != null) Matrix.scaleM(out, 0, s.getDouble(0).toFloat(), s.getDouble(1).toFloat(), s.getDouble(2).toFloat())
            return out
        }

        private fun quatMatrix(q: JSONArray): FloatArray {
            val x = q.getDouble(0).toFloat(); val y = q.getDouble(1).toFloat(); val z = q.getDouble(2).toFloat(); val w = q.getDouble(3).toFloat()
            val m = FloatArray(16)
            m[0] = 1 - 2 * (y*y + z*z); m[1] = 2 * (x*y + z*w); m[2] = 2 * (x*z - y*w)
            m[4] = 2 * (x*y - z*w); m[5] = 1 - 2 * (x*x + z*z); m[6] = 2 * (y*z + x*w)
            m[8] = 2 * (x*z + y*w); m[9] = 2 * (y*z - x*w); m[10] = 1 - 2 * (x*x + y*y); m[15] = 1f
            return m
        }

        private fun multiplyInPlace(a: FloatArray, b: FloatArray) {
            val out = FloatArray(16); Matrix.multiplyMM(out, 0, a, 0, b, 0); out.copyInto(a)
        }

        private fun findParent(nodes: JSONArray, child: Int): Int {
            for (i in 0 until nodes.length()) {
                val children = nodes.getJSONObject(i).optJSONArray("children") ?: continue
                for (j in 0 until children.length()) if (children.getInt(j) == child) return i
            }
            return -1
        }

        private fun readVec3(accessors: JSONArray, views: JSONArray, buffers: JSONArray, bin: ByteArray, accessorIndex: Int): FloatArray {
            return readFloats(accessors, views, buffers, bin, accessorIndex, 3)
        }
        private fun readVec2(accessors: JSONArray, views: JSONArray, buffers: JSONArray, bin: ByteArray, accessorIndex: Int): FloatArray {
            return readFloats(accessors, views, buffers, bin, accessorIndex, 2)
        }
        private fun readFloats(accessors: JSONArray, views: JSONArray, buffers: JSONArray, bin: ByteArray, accessorIndex: Int, components: Int): FloatArray {
            val a = accessors.getJSONObject(accessorIndex)
            val count = a.getInt("count")
            val componentType = a.getInt("componentType")
            val viewIndex = a.optInt("bufferView", -1)
            if (viewIndex < 0) return FloatArray(count * components)
            val view = views.getJSONObject(viewIndex)
            val bufferIndex = view.optInt("buffer", 0)
            val source = if (bufferIndex == 0) bin else ByteArray(0)
            val offset = view.optInt("byteOffset", 0) + a.optInt("byteOffset", 0)
            val stride = view.optInt("byteStride", componentSize(componentType) * components)
            val out = FloatArray(count * components)
            val bb = ByteBuffer.wrap(source).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until count) for (c in 0 until components) {
                val p = offset + i * stride + c * componentSize(componentType)
                out[i * components + c] = componentAsFloat(bb, p, componentType, a.optBoolean("normalized", false))
            }
            return out
        }

        private data class IndexInfo(val buffer: ByteArray, val count: Int, val type: Int)
        private fun readIndices(accessors: JSONArray, views: JSONArray, buffers: JSONArray, bin: ByteArray, accessorIndex: Int): IndexInfo {
            val a = accessors.getJSONObject(accessorIndex)
            val count = a.getInt("count")
            val type = a.getInt("componentType")
            val view = views.getJSONObject(a.getInt("bufferView"))
            val source = if (view.optInt("buffer", 0) == 0) bin else ByteArray(0)
            val offset = view.optInt("byteOffset", 0) + a.optInt("byteOffset", 0)
            val size = componentSize(type)
            val out = ByteArray(count * size)
            val src = ByteBuffer.wrap(source).order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until count) for (b in 0 until size) out[i * size + b] = src.get(offset + i * size + b)
            return IndexInfo(out, count, glIndexType(type))
        }

        private fun sequentialIndices(count: Int): IndexInfo {
            if (count <= 65535) {
                val bb = ByteBuffer.allocate(count * 2).order(ByteOrder.LITTLE_ENDIAN)
                repeat(count) { bb.putShort(it.toShort()) }
                return IndexInfo(bb.array(), count, GLES30.GL_UNSIGNED_SHORT)
            }
            val bb = ByteBuffer.allocate(count * 4).order(ByteOrder.LITTLE_ENDIAN)
            repeat(count) { bb.putInt(it) }
            return IndexInfo(bb.array(), count, GLES30.GL_UNSIGNED_INT)
        }

        private fun generatedNormals(pos: FloatArray): FloatArray {
            val out = FloatArray(pos.size)
            for (i in pos.indices step 9) {
                if (i + 8 >= pos.size) break
                val ax = pos[i+3]-pos[i]; val ay = pos[i+4]-pos[i+1]; val az = pos[i+5]-pos[i+2]
                val bx = pos[i+6]-pos[i]; val by = pos[i+7]-pos[i+1]; val bz = pos[i+8]-pos[i+2]
                val nx = ay*bz-az*by; val ny = az*bx-ax*bz; val nz = ax*by-ay*bx
                for (v in 0..2) { out[i+v*3]=nx; out[i+v*3+1]=ny; out[i+v*3+2]=nz }
            }
            return out
        }

        private fun materialColor(m: JSONObject?): FloatArray {
            val p = m?.optJSONObject("pbrMetallicRoughness")
            val a = p?.optJSONArray("baseColorFactor")
            return if (a != null) floatArrayOf(a.getDouble(0).toFloat(), a.getDouble(1).toFloat(), a.getDouble(2).toFloat(), a.getDouble(3).toFloat()) else floatArrayOf(1f,1f,1f,1f)
        }

        private fun loadBaseColorTexture(m: JSONObject?, textures: JSONArray, images: JSONArray, views: JSONArray, buffers: JSONArray, bin: ByteArray): Int {
            val p = m?.optJSONObject("pbrMetallicRoughness") ?: return 0
            val ti = p.optJSONObject("baseColorTexture")?.optInt("index", -1) ?: -1
            if (ti !in 0 until textures.length()) return 0
            val sourceIndex = textures.getJSONObject(ti).optInt("source", -1)
            if (sourceIndex !in 0 until images.length()) return 0
            val image = images.getJSONObject(sourceIndex)
            val bytes = when {
                image.has("uri") -> return 0 // External image files are intentionally not required for GLB-only packaging.
                image.has("bufferView") -> {
                    val v = views.getJSONObject(image.getInt("bufferView"))
                    val off = v.optInt("byteOffset", 0)
                    val len = v.getInt("byteLength")
                    bin.copyOfRange(off, off + len)
                }
                else -> return 0
            }
            val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(bytes)) ?: return 0
            val id = IntArray(1)
            GLES30.glGenTextures(1, id, 0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id[0])
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
            bitmap.recycle()
            return id[0]
        }

        private fun glBuffer(target: Int, data: FloatArray): Int {
            val id = IntArray(1); GLES30.glGenBuffers(1, id, 0); GLES30.glBindBuffer(target, id[0])
            val fb: FloatBuffer = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(data).position(0) as FloatBuffer
            GLES30.glBufferData(target, data.size * 4, fb, GLES30.GL_STATIC_DRAW); GLES30.glBindBuffer(target, 0); return id[0]
        }
        private fun glBuffer(target: Int, data: ByteArray): Int {
            val id = IntArray(1); GLES30.glGenBuffers(1, id, 0); GLES30.glBindBuffer(target, id[0])
            val bb = ByteBuffer.allocateDirect(data.size).order(ByteOrder.nativeOrder()).put(data).position(0) as ByteBuffer
            GLES30.glBufferData(target, data.size, bb, GLES30.GL_STATIC_DRAW); GLES30.glBindBuffer(target, 0); return id[0]
        }

        private fun componentSize(type: Int) = when (type) { 5120,5121 -> 1; 5122,5123 -> 2; 5125,5126 -> 4; else -> error("Unsupported component type $type") }
        private fun glIndexType(type: Int) = when (type) { 5121 -> GLES30.GL_UNSIGNED_BYTE; 5123 -> GLES30.GL_UNSIGNED_SHORT; 5125 -> GLES30.GL_UNSIGNED_INT; else -> error("Unsupported index type $type") }
        private fun componentAsFloat(bb: ByteBuffer, p: Int, type: Int, normalized: Boolean): Float = when (type) {
            5126 -> bb.getFloat(p)
            5125 -> bb.getInt(p).toFloat()
            5123 -> (bb.getShort(p).toInt() and 0xFFFF).toFloat() / if (normalized) 65535f else 1f
            5122 -> if (normalized) (bb.getShort(p).toShort() / 32767f).coerceIn(-1f,1f) else bb.getShort(p).toShort().toFloat()
            5121 -> (bb.get(p).toInt() and 0xFF).toFloat() / if (normalized) 255f else 1f
            5120 -> if (normalized) (bb.get(p).toByte() / 127f).coerceIn(-1f,1f) else bb.get(p).toByte().toFloat()
            else -> 0f
        }
    }

    companion object {
        private const val VERTEX_SHADER = """
            #version 300 es
            precision highp float;
            layout(location=0) in vec3 aPosition;
            layout(location=1) in vec3 aNormal;
            layout(location=2) in vec2 aUv;
            uniform mat4 uMvp;
            uniform mat3 uNormalMat;
            out vec3 vNormal;
            out vec2 vUv;
            void main(){ vNormal=normalize(uNormalMat*aNormal); vUv=aUv; gl_Position=uMvp*vec4(aPosition,1.0); }
        """
        private const val FRAGMENT_SHADER = """
            #version 300 es
            precision mediump float;
            in vec3 vNormal;
            in vec2 vUv;
            uniform vec4 uColor;
            uniform sampler2D uTexture;
            uniform int uUseTexture;
            uniform vec3 uLight;
            out vec4 fragColor;
            void main(){
                vec4 tex = uUseTexture==1 ? texture(uTexture,vUv) : vec4(1.0);
                vec4 base = tex*uColor;
                vec3 n=normalize(vNormal);
                float d=max(dot(n,normalize(uLight)),0.0);
                float toon=0.58+0.42*smoothstep(0.08,0.92,d);
                float rim=pow(1.0-max(dot(n,vec3(0,0,1)),0.0),2.4);
                vec3 rgb=base.rgb*toon + base.rgb*0.08*rim;
                fragColor=vec4(rgb,base.a);
            }
        """

        private fun createProgram(vertex: String, fragment: String): Int {
            fun compile(type: Int, source: String): Int {
                val s=GLES30.glCreateShader(type); GLES30.glShaderSource(s,source); GLES30.glCompileShader(s)
                val ok=IntArray(1); GLES30.glGetShaderiv(s,GLES30.GL_COMPILE_STATUS,ok,0)
                if(ok[0]==0) { val log=GLES30.glGetShaderInfoLog(s); GLES30.glDeleteShader(s); error(log) }
                return s
            }
            val v=compile(GLES30.GL_VERTEX_SHADER,vertex); val f=compile(GLES30.GL_FRAGMENT_SHADER,fragment)
            val p=GLES30.glCreateProgram(); GLES30.glAttachShader(p,v); GLES30.glAttachShader(p,f); GLES30.glLinkProgram(p)
            val ok=IntArray(1); GLES30.glGetProgramiv(p,GLES30.GL_LINK_STATUS,ok,0)
            if(ok[0]==0) { val log=GLES30.glGetProgramInfoLog(p); GLES30.glDeleteProgram(p); error(log) }
            GLES30.glDeleteShader(v); GLES30.glDeleteShader(f); return p
        }
    }
}
