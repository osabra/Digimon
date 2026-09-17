import os
import numpy as np
import trimesh
from PIL import Image, ImageEnhance, ImageFilter
from trimesh.visual.material import PBRMaterial

OUT = 'app/src/main/assets/digimon'
NAMES = [
    'Agumon', 'Gabumon', 'Guilmon', 'Renamon', 'Biyomon', 'Patamon',
    'Gatomon', 'Tentomon', 'Gomamon', 'Palmon', 'Veemon', 'Wormmon'
]

# Final quality pass for every companion. The goal is a clean, rounded,
# anime/toon presentation rather than a noisy procedural/plastic look.

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
    # Extra gentle smoothing preserves the stylised silhouette.
    try:
        trimesh.smoothing.filter_taubin(mesh, lamb=0.16, nu=0.22, iterations=6)
    except Exception:
        pass
    mesh.merge_vertices()
    mesh.fix_normals()
    return mesh


def clean_texture(image):
    if image is None:
        return None
    try:
        if not isinstance(image, Image.Image):
            image = Image.fromarray(np.asarray(image))
        src = np.asarray(image.convert('RGB'), dtype=np.float32)
        # Collapse the old grain/rings into a stable palette colour. This is
        # intentionally clean and graphic, like an anime 3D asset.
        base = np.median(src.reshape(-1, 3), axis=0)
        base = np.clip(base, 0, 255)
        h = w = 512
        yy, xx = np.mgrid[0:h, 0:w]
        light = np.exp(-(((xx-w*0.34)/(w*0.55))**2 + ((yy-h*0.30)/(h*0.62))**2))
        shade = 0.90 + 0.16 * light
        arr = np.clip(base.reshape(1,1,3) * shade[...,None], 0, 255).astype(np.uint8)
        out = Image.fromarray(arr, 'RGB')
        out = ImageEnhance.Color(out).enhance(1.05)
        out = ImageEnhance.Contrast(out).enhance(1.03)
        out = out.filter(ImageFilter.GaussianBlur(radius=0.35))
        return out
    except Exception:
        return image


def improve_material(visual):
    mat = getattr(visual, 'material', None)
    if mat is None:
        return
    try:
        if hasattr(mat, 'image') and mat.image is not None:
            mat.image = clean_texture(mat.image)
    except Exception:
        pass
    if isinstance(mat, PBRMaterial):
        mat.metallicFactor = 0.0
        mat.roughnessFactor = 0.78
        try:
            mat.doubleSided = True
        except Exception:
            pass


def improve_scene(scene):
    for _, geom in list(scene.geometry.items()):
        improve_mesh(geom)
        improve_material(getattr(geom, 'visual', None))
    return scene


for name in NAMES:
    path = os.path.join(OUT, name + '.glb')
    if not os.path.exists(path):
        raise FileNotFoundError(path)
    scene = trimesh.load(path, force='scene')
    improve_scene(scene)
    scene.export(path, file_type='glb')
    print('Upgraded', name)
