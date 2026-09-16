import os
import trimesh
from trimesh.visual.material import PBRMaterial

OUT = 'app/src/main/assets/digimon/Biyomon.glb'
os.makedirs(os.path.dirname(OUT), exist_ok=True)

COLORS = {
    'pink': (0.88, 0.12, 0.30), 'pink2': (0.98, 0.24, 0.43),
    'red': (0.72, 0.025, 0.035), 'red2': (0.90, 0.08, 0.06),
    'cream': (0.92, 0.86, 0.69), 'orange': (0.95, 0.34, 0.035),
    'orange2': (1.0, 0.52, 0.04), 'blue': (0.025, 0.19, 0.58),
    'blue2': (0.05, 0.31, 0.82), 'black': (0.006, 0.006, 0.009),
    'white': (0.98, 0.98, 0.96)
}
M = {k: PBRMaterial(name=k, baseColorFactor=[round(v*255) for v in rgb] + [255], roughnessFactor=0.48, metallicFactor=0.0) for k, rgb in COLORS.items()}

def add(scene, mesh, mat):
    if hasattr(mesh, 'remove_duplicate_faces'):
        mesh.remove_duplicate_faces()
    mesh.visual.material = M[mat]
    mesh.process(validate=True)
    scene.add_geometry(mesh)

def sphere(scene, r, pos, mat, scale=(1,1,1), seg=64, rings=40):
    m = trimesh.creation.uv_sphere(radius=r, count=[seg, rings])
    m.apply_scale(scale); m.apply_translation(pos); add(scene, m, mat)

def eye(scene, x, y, z):
    sphere(scene, .128, (x,y,z), 'black', (1,1,1.06), 48, 32)
    sphere(scene, .071, (x,y-.018,z-.065), 'blue2', (1,1,1), 36, 24)
    sphere(scene, .022, (x-.028,y-.035,z-.090), 'white', (1,1,1), 24, 18)

def claw(scene, x, y, z, spread=.075):
    for i in range(3):
        dx = (i-1)*spread
        m = trimesh.creation.cone(radius=.052, radius2=.008, height=.20, sections=32)
        m.apply_transform(trimesh.transformations.rotation_matrix(-1.25, [1,0,0]))
        m.apply_translation((x+dx,y,z)); add(scene, m, 'orange2')

def normalize(scene):
    b = scene.bounds; c = (b[0] + b[1]) / 2
    scene.apply_translation([-c[0], -b[0][1], -c[2]])
    return scene

s = trimesh.Scene()
# Pear-shaped body and separate head for a clear anime silhouette.
sphere(s, .60, (0,.86,0), 'pink', (1.03,1.22,.86))
sphere(s, .56, (0,1.68,-.03), 'pink2', (1.02,.92,.88))
# Face and belly patches.
sphere(s, .43, (0,1.63,-.57), 'cream', (1.08,.78,.46), 56, 36)
sphere(s, .40, (0,1.05,-.60), 'cream', (1.08,1.10,.42), 56, 36)
# Large eyes and pronounced two-part beak.
eye(s,-.19,1.79,-.88); eye(s,.19,1.79,-.88)
sphere(s,.155,(0,1.58,-.91),'orange2',(1.30,.62,.74),48,30)
sphere(s,.108,(0,1.48,-.92),'orange',(1.18,.50,.68),44,28)
# Layered red crest.
sphere(s,.20,(0,2.24,-.02),'red',(.70,1.70,.62),52,34)
sphere(s,.145,(-.16,2.13,-.04),'red2',(.70,1.48,.56),48,30)
sphere(s,.145,(.16,2.13,-.04),'red2',(.70,1.48,.56),48,30)
sphere(s,.105,(-.28,2.02,-.05),'red2',(.76,1.25,.52),42,28)
sphere(s,.105,(.28,2.02,-.05),'red2',(.76,1.25,.52),42,28)
# Large blue wings with distinct feather layers per side.
for side in (-1,1):
    sphere(s,.35,(side*.52,1.27,.00),'blue2',(.78,1.30,.62),52,34)
    sphere(s,.30,(side*.66,1.05,.00),'blue2',(.72,1.24,.56),48,30)
    sphere(s,.25,(side*.76,.84,-.01),'blue',(.66,1.16,.50),44,28)
    for i,(yy,zz,rr) in enumerate(((1.30,.01,.23),(1.08,-.01,.205),(.86,-.02,.18))):
        sphere(s,rr,(side*(.72+i*.06),yy,zz),'blue2',(.62,1.20,.48),40,26)
    sphere(s,.22,(side*.28,.22,-.28),'orange',(1.35,.64,1.30),48,30)
    claw(s,side*.28,.14,-.49)
# Three tail feathers, visible in rear and three-quarter views.
sphere(s,.20,(0,.67,.49),'pink2',(1,.86,1.28),44,28)
sphere(s,.17,(0,.50,.69),'pink2',(.92,.80,1.28),40,26)
sphere(s,.13,(0,.33,.79),'pink2',(.82,.72,1.20),36,24)

normalize(s)
s.export(OUT)
print(f'Generated improved Biyomon GLB: {OUT}')
