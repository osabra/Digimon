import os
import numpy as np
import trimesh
from PIL import Image, ImageEnhance, ImageFilter
from trimesh.visual.material import PBRMaterial
from trimesh.visual.texture import TextureVisuals
from trimesh.voxel.ops import matrix_to_marching_cubes

OUT = 'app/src/main/assets/digimon'
NAMES = [
    'Agumon', 'Gabumon', 'Guilmon', 'Renamon', 'Biyomon', 'Patamon',
    'Gatomon', 'Tentomon', 'Gomamon', 'Palmon', 'Veemon', 'Wormmon'
]

# The source models are made from many smooth primitives.  The visible problem
# in the app is not polygon count but the hard seams where those primitives
# overlap.  This pass turns each shared-colour cluster into one organic surface
# with marching cubes, then restores clean toon materials and normals.


def material_key(visual):
    mat = getattr(visual, 'material', None)
    tex = getattr(mat, 'baseColorTexture', None)
    if tex is not None:
        try:
            a = np.asarray(tex.convert('RGB'), dtype=np.uint8)
            return ('tex', tuple(np.median(a.reshape(-1, 3), axis=0).astype(int)))
        except Exception:
            pass
    factor = getattr(mat, 'baseColorFactor', None)
    if factor is not None:
        return ('factor', tuple(np.asarray(factor).astype(int).tolist()))
    return ('material', str(type(mat)), str(getattr(mat, 'name', '')))


def organic_union(meshes, pitch=0.024):
    """Voxel-union a set of overlapping meshes into one smooth watertight blob."""
    if len(meshes) < 2:
        return None
    try:
        mins = np.min(np.vstack([m.bounds[0] for m in meshes]), axis=0)
        maxs = np.max(np.vstack([m.bounds[1] for m in meshes]), axis=0)
        origin = np.floor(mins / pitch - 2) * pitch
        extent = np.ceil((maxs - origin) / pitch + 3).astype(int)
        # Protect CI from an accidental pathological mesh.
        if np.prod(extent) > 7_000_000:
            pitch = 0.032
            origin = np.floor(mins / pitch - 2) * pitch
            extent = np.ceil((maxs - origin) / pitch + 3).astype(int)
        occ = np.zeros(tuple(extent), dtype=bool)
        for mesh in meshes:
            vg = mesh.voxelized(pitch).fill()
            pts = vg.points
            idx = np.rint((pts - origin) / pitch).astype(np.int32)
            valid = np.all((idx >= 0) & (idx < extent), axis=1)
            idx = idx[valid]
            if len(idx):
                occ[idx[:, 0], idx[:, 1], idx[:, 2]] = True
        if not occ.any():
            return None
        out = matrix_to_marching_cubes(occ, pitch=pitch)
        out.apply_translation(origin)
        out.remove_unreferenced_vertices()
        out.process(validate=True)
        try:
            trimesh.smoothing.filter_taubin(out, lamb=0.10, nu=0.14, iterations=2)
        except Exception:
            pass
        out.fix_normals()
        return out
    except Exception as exc:
        print('Organic union skipped:', exc)
        return None


def clean_texture(image):
    if image is None:
        return None
    try:
        if not isinstance(image, Image.Image):
            image = Image.fromarray(np.asarray(image))
        src = np.asarray(image.convert('RGB'), dtype=np.float32)
        base = np.median(src.reshape(-1, 3), axis=0)
        base = np.clip(base, 0, 255)
        h = w = 512
        yy, xx = np.mgrid[0:h, 0:w]
        light = np.exp(-(((xx-w*0.34)/(w*0.58))**2 + ((yy-h*0.30)/(h*0.64))**2))
        shade = 0.91 + 0.15 * light
        arr = np.clip(base.reshape(1,1,3) * shade[...,None], 0, 255).astype(np.uint8)
        out = Image.fromarray(arr, 'RGB')
        out = ImageEnhance.Color(out).enhance(1.06)
        out = ImageEnhance.Contrast(out).enhance(1.04)
        return out.filter(ImageFilter.GaussianBlur(radius=0.25))
    except Exception:
        return image


def improve_mesh(mesh):
    if not isinstance(mesh, trimesh.Trimesh):
        return mesh
    try:
        mesh.update_faces(mesh.unique_faces())
    except (AttributeError, TypeError):
        try:
            mesh.remove_duplicate_faces()
        except AttributeError:
            pass
    try:
        mesh.update_faces(mesh.nondegenerate_faces())
    except (AttributeError, TypeError):
        try:
            mesh.remove_degenerate_faces()
        except AttributeError:
            pass
    mesh.merge_vertices()
    mesh.process(validate=True)
    try:
        trimesh.smoothing.filter_taubin(mesh, lamb=0.12, nu=0.18, iterations=3)
    except Exception:
        pass
    mesh.merge_vertices()
    mesh.fix_normals()
    return mesh


def improve_material(visual):
    mat = getattr(visual, 'material', None)
    if mat is None:
        return
    try:
        tex = getattr(mat, 'baseColorTexture', None)
        if tex is not None:
            mat.baseColorTexture = clean_texture(tex)
    except Exception:
        pass
    if isinstance(mat, PBRMaterial):
        mat.metallicFactor = 0.0
        mat.roughnessFactor = 0.84
        try:
            mat.doubleSided = True
        except Exception:
            pass


def rebuild_scene(scene):
    groups = {}
    for geom in scene.geometry.values():
        if isinstance(geom, trimesh.Trimesh):
            groups.setdefault(material_key(geom.visual), []).append(geom)

    rebuilt = trimesh.Scene()
    for key, meshes in groups.items():
        # Only union substantial clusters. Tiny details such as claws, eyes and
        # teeth remain independent so their shapes are not melted together.
        total_vertices = sum(len(m.vertices) for m in meshes)
        if len(meshes) >= 2 and total_vertices >= 1200:
            merged = organic_union(meshes)
            if merged is not None:
                source_visual = meshes[0].visual
                try:
                    merged.visual = TextureVisuals(
                        uv=None,
                        material=source_visual.material.copy(),
                    )
                except Exception:
                    merged.visual = source_visual
                rebuilt.add_geometry(merged)
                continue
        for mesh in meshes:
            rebuilt.add_geometry(mesh)

    return rebuilt


for name in NAMES:
    path = os.path.join(OUT, name + '.glb')
    if not os.path.exists(path):
        raise FileNotFoundError(path)
    scene = trimesh.load(path, force='scene')
    scene = rebuild_scene(scene)
    for geom in scene.geometry.values():
        improve_mesh(geom)
        improve_material(getattr(geom, 'visual', None))
    scene.export(path, file_type='glb')
    print('Organic anime pass:', name)
