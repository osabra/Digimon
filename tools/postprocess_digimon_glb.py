import os
import trimesh
from trimesh.visual.material import PBRMaterial

OUT = "app/src/main/assets/digimon"
NAMES = [
    "Agumon", "Gabumon", "Guilmon", "Renamon", "Biyomon", "Patamon",
    "Gatomon", "Tentomon", "Gomamon", "Palmon", "Veemon", "Wormmon",
]

# Non-destructive final pass. Keep character parts separate: eyes, claws,
# ears, tails and markings are intentional anime details and must not be
# voxel-unioned into a generic blob.

def improve_mesh(mesh):
    if not isinstance(mesh, trimesh.Trimesh):
        return
    try:
        mesh.update_faces(mesh.unique_faces())
    except Exception:
        try:
            mesh.remove_duplicate_faces()
        except Exception:
            pass
    try:
        mesh.update_faces(mesh.nondegenerate_faces())
    except Exception:
        try:
            mesh.remove_degenerate_faces()
        except Exception:
            pass
    mesh.merge_vertices()
    mesh.process(validate=True)
    try:
        # Very light smoothing only; the generator already uses dense UV
        # spheres/capsules and we want to preserve character-specific shapes.
        trimesh.smoothing.filter_taubin(mesh, lamb=0.055, nu=0.08, iterations=2)
    except Exception:
        pass
    mesh.merge_vertices()
    mesh.fix_normals()

def improve_material(mesh):
    mat = getattr(getattr(mesh, "visual", None), "material", None)
    if isinstance(mat, PBRMaterial):
        mat.metallicFactor = 0.0
        mat.roughnessFactor = 0.72
        try:
            mat.doubleSided = True
        except Exception:
            pass

for name in NAMES:
    path = os.path.join(OUT, name + ".glb")
    if not os.path.exists(path):
        raise FileNotFoundError(path)
    scene = trimesh.load(path, force="scene")
    for geom in scene.geometry.values():
        improve_mesh(geom)
        improve_material(geom)
    scene.export(path, file_type="glb")
    print("Final anime cleanup:", name)
