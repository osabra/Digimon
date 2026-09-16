import os
import trimesh
from trimesh.visual.material import PBRMaterial

OUT = 'app/src/main/assets/digimon'
os.makedirs(OUT, exist_ok=True)

COLORS = {
    'pink': (0.92, 0.25, 0.42),
    'pink_dark': (0.70, 0.08, 0.20),
    'cream': (0.96, 0.84, 0.58),
    'white': (0.98, 0.97, 0.92),
    'blue': (0.05, 0.30, 0.72),
    'blue_dark': (0.02, 0.10, 0.32),
    'orange': (0.95, 0.42, 0.04),
    'red': (0.75, 0.03, 0.06),
    'black': (0.008, 0.008, 0.012),
}
M = {k: PBRMaterial(name=k, baseColorFactor=[round(v * 255) for v in rgb] + [255], roughnessFactor=0.58, metallicFactor=0.0) for k, rgb in COLORS.items()}


def add(scene, mesh, mat):
    mesh.visual.material = M[mat]
    scene.add_geometry(mesh)


def uv(scene, radius, pos, mat, scale=(1, 1, 1), seg=32, rings=20):
    mesh = trimesh.creation.uv_sphere(radius=radius, count=[seg, rings])
    mesh.apply_scale(scale)
    mesh.apply_translation(pos)
    add(scene, mesh, mat)


def cone(scene, r1, r2, h, pos, mat):
    mesh = trimesh.creation.conical_frustum(radius1=r1, radius2=r2, height=h, sections=32)
    mesh.apply_translation(pos)
    add(scene, mesh, mat)


def biyomon():
    s = trimesh.Scene()

    # Compact bird-like anime silhouette: rounded pink body and oversized head.
    uv(s, .62, (0, .82, 0), 'pink', (1.00, 1.20, .82))
    uv(s, .58, (0, 1.66, -.01), 'pink', (1.02, .92, .88))

    # Cream face mask and belly, separated meshes for a clean 360-degree read.
    uv(s, .46, (0, 1.62, -.47), 'cream', (1.00, .76, .43))
    uv(s, .40, (0, 1.00, -.53), 'cream', (1.03, 1.02, .38))

    # Large blue anime eyes with white highlights.
    for x in (-.19, .19):
        uv(s, .105, (x, 1.78, -.76), 'blue_dark', (1, 1, 1.05), 24, 16)
        uv(s, .057, (x, 1.78, -.812), 'blue', (1, 1, 1), 20, 14)
        uv(s, .018, (x - .018, 1.805, -.855), 'white', (1, 1, 1), 12, 8)

    # Orange beak, split into upper/lower rounded pieces.
    uv(s, .15, (0, 1.56, -.82), 'orange', (1.45, .55, .72), 24, 14)
    uv(s, .105, (0, 1.47, -.80), 'orange', (1.35, .48, .62), 24, 14)

    # Red crest/feather and small side feathers.
    uv(s, .18, (0, 2.23, -.01), 'red', (.62, 1.55, .65))
    for x in (-.16, .16):
        uv(s, .13, (x, 2.13, -.04), 'red', (.62, 1.35, .55))

    # Pink wings with blue feather tips, built from overlapping rounded feather meshes.
    for side in (-1, 1):
        sx = side
        uv(s, .32, (sx * .52, 1.18, .00), 'pink', (.72, 1.25, .62))
        uv(s, .27, (sx * .62, .95, .04), 'pink_dark', (.68, 1.10, .58))
        for i, (y, z, sc) in enumerate([(1.28, .02, .27), (1.10, .00, .24), (.92, -.01, .21)]):
            uv(s, sc, (sx * (.65 + i * .035), y, z), 'blue', (.70, 1.15, .55))

    # Orange feet with three small toe/claw forms.
    for x in (-.27, .27):
        uv(s, .23, (x, .20, -.23), 'orange', (1.25, .62, 1.30))
        for dx in (-.075, 0, .075):
            uv(s, .055, (x + dx, .17, -.38), 'orange', (1.0, .70, .85), 16, 10)

    # Short pink tail with blue feather tip, visible from rear rotation.
    uv(s, .20, (0, .73, .56), 'pink', (.70, .70, 1.70))
    uv(s, .15, (0, .73, .77), 'blue', (.72, .72, 1.35))

    # Normalize to the same floor/center convention as the other assets.
    bounds = s.bounds
    center = (bounds[0] + bounds[1]) / 2
    s.apply_translation([-center[0], -bounds[0][1], -center[2]])
    return s


biyomon().export(os.path.join(OUT, 'Biyomon.glb'), file_type='glb')
