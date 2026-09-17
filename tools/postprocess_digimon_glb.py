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

# Quality pass for every generated companion.  The generator provides the
# character-specific geometry; this pass makes the final GLBs cleaner and
# considerably closer to a polished anime/toon 3D asset: smoother normals,
# cleaner embedded textures and less plastic-looking materials.

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

    # Several gentle Taubin passes remove the faceted/rough appearance without
    # collapsing the stylised silhouettes or changing the proportions.
    try:
        trimesh.smoothing.filter_taubin(
            mesh, lamb=0.18, nu=0.22, iterations=4
        )
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
        image = image.convert('RGB')
        # Higher resolution plus light denoising removes the visible procedural
        # rings/noise while retaining the hand-painted colour variation.
        image = image.resize((512, 512), Image.Resampling.LANCZOS)
        image = image.filter(ImageFilter.GaussianBlur(radius=0.65))
        image = ImageEnhance.Contrast(image).enhance(1.04)
        image = ImageEnhance.Color(image).enhance(1.08)
        image = ImageEnhance.Sharpness(image).enhance(1.18)
        return image
    except Exception:
        return image


def improve_material(visual):
    mat = getattr(visual, 'material', None)
    if mat is None:
        return

    # Clean the embedded texture if the loader exposed it as a PIL image.
    try:
        if hasattr(mat, 'image') and mat.image is not None:
            mat.image = clean_texture(mat.image)
    except Exception:
        pass

    if isinstance(mat, PBRMaterial):
        mat.metallicFactor = 0.0
        mat.roughnessFactor = 0.62
        # Filament/Android renderers generally respect these values and produce
        # a softer anime-like response than the previous shiny plastic look.
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
