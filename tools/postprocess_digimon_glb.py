import os
import trimesh
from trimesh.visual.material import PBRMaterial

OUT = 'app/src/main/assets/digimon'
NAMES = [
    'Agumon', 'Gabumon', 'Guilmon', 'Renamon', 'Biyomon', 'Patamon',
    'Gatomon', 'Tentomon', 'Gomamon', 'Palmon', 'Veemon', 'Wormmon'
]

# Final quality pass shared by every generated companion.  The models are built
# from smooth primitives, so this pass removes small mesh defects, recomputes
# normals and applies a very light Taubin smoothing pass without changing the
# character proportions.
def improve_mesh(mesh):
    if not isinstance(mesh, trimesh.Trimesh):
        return mesh
    mesh.remove_duplicate_faces()
    mesh.remove_degenerate_faces()
    mesh.merge_vertices()
    mesh.process(validate=True)
    # Two conservative iterations reduce visible faceting while preserving the
    # silhouette and the intentionally stylised anime proportions.
    try:
        trimesh.smoothing.filter_taubin(mesh, lamb=0.28, nu=0.30, iterations=2)
    except Exception:
        pass
    mesh.remove_duplicate_faces()
    mesh.remove_degenerate_faces()
    mesh.merge_vertices()
    mesh.fix_normals()
    return mesh


def improve_scene(scene):
    for name, geom in list(scene.geometry.items()):
        improve_mesh(geom)
        mat = getattr(geom.visual, 'material', None)
        if isinstance(mat, PBRMaterial):
            mat.roughnessFactor = 0.46
            mat.metallicFactor = 0.0
    return scene


for name in NAMES:
    path = os.path.join(OUT, name + '.glb')
    if not os.path.exists(path):
        raise FileNotFoundError(path)
    scene = trimesh.load(path, force='scene')
    improve_scene(scene)
    scene.export(path, file_type='glb')
    print('Post-processed', name)
