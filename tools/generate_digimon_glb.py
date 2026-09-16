import os
import math
import trimesh
from trimesh.visual.material import PBRMaterial

OUT = 'app/src/main/assets/digimon'
os.makedirs(OUT, exist_ok=True)

COLORS = {
    'blue': (0.035, 0.20, 0.55), 'darkblue': (0.015, 0.07, 0.22),
    'cream': (0.88, 0.82, 0.66), 'white': (0.97, 0.97, 0.94),
    'orange': (0.95, 0.30, 0.035), 'yellow': (0.95, 0.62, 0.035),
    'red': (0.72, 0.025, 0.025), 'black': (0.008, 0.008, 0.012),
    'pink': (0.95, 0.18, 0.40), 'green': (0.08, 0.46, 0.10),
    'purple': (0.42, 0.08, 0.62), 'lightblue': (0.10, 0.48, 0.86),
    'brown': (0.35, 0.16, 0.06), 'grey': (0.38, 0.42, 0.45)
}
M = {k: PBRMaterial(name=k, baseColorFactor=[round(v * 255) for v in rgb] + [255], roughnessFactor=0.62, metallicFactor=0.0) for k, rgb in COLORS.items()}


def add(scene, mesh, mat):
    mesh.visual.material = M[mat]
    scene.add_geometry(mesh)


def uv(scene, radius, pos, mat, scale=(1, 1, 1), seg=24, rings=16):
    m = trimesh.creation.uv_sphere(radius=radius, count=[seg, rings])
    m.apply_scale(scale)
    m.apply_translation(pos)
    add(scene, m, mat)
    return m


def box(scene, extents, pos, mat, rot=None, bevel=0.0):
    m = trimesh.creation.box(extents=extents)
    if bevel:
        m = m.subdivide().subdivide()
    if rot:
        m.apply_transform(trimesh.transformations.euler_matrix(*rot))
    m.apply_translation(pos)
    add(scene, m, mat)
    return m


def cyl(scene, radius, height, pos, mat, axis='y'):
    m = trimesh.creation.cylinder(radius=radius, height=height, sections=24)
    if axis == 'x': m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0]))
    if axis == 'z': m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    m.apply_translation(pos)
    add(scene, m, mat)
    return m


def cone(scene, r1, r2, h, pos, mat, axis='y'):
    m = trimesh.creation.conical_frustum(radius1=r1, radius2=r2, height=h, sections=24)
    if axis == 'x': m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0]))
    if axis == 'z': m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    m.apply_translation(pos)
    add(scene, m, mat)
    return m


def eye_pair(s, y, z, iris='red', size=.115):
    for x in (-.205, .205):
        uv(s, size, (x, y, z), 'black', (1, 1, 1.05), 20, 14)
        uv(s, size * .52, (x, y - .018, z - .055), iris, (1, 1, 1), 20, 14)
        uv(s, size * .18, (x - .025, y - .035, z - .075), 'white', (1, 1, 1), 16, 10)


def normalize(s):
    b = s.bounds
    c = (b[0] + b[1]) / 2
    s.apply_translation([-c[0], -b[0][1], -c[2]])


def gabumon():
    s = trimesh.Scene()
    # Blue body underneath the pelt.
    uv(s, .62, (0, .82, 0), 'blue', (1.0, 1.28, .82))
    uv(s, .70, (0, 1.70, 0), 'cream', (1.0, 1.0, .92))
    # Characteristic striped fur pelt on head and torso.
    uv(s, .50, (0, 1.22, -.33), 'cream', (1.0, .95, .55))
    for x, z, sc in [(-.38,-.33,.9),(-.18,-.38,.72),(.18,-.38,.72),(.38,-.33,.9)]:
        box(s, (.10, .48, .035), (x, 1.40, z), 'blue', rot=(0, 0, (-.22 if x < 0 else .22)))
    # Ears, horn, muzzle and nose.
    uv(s, .27, (-.54, 1.88, -.02), 'blue', (.65, 1.0, .45)); uv(s, .27, (.54, 1.88, -.02), 'blue', (.65, 1.0, .45))
    uv(s, .15, (-.55, 1.88, -.18), 'pink', (.55, 1, .45)); uv(s, .15, (.55, 1.88, -.18), 'pink', (.55, 1, .45))
    cone(s, .17, .08, .48, (0, 2.42, 0), 'yellow')
    uv(s, .22, (-.25, 1.52, -.57), 'cream', (1.25, .75, .7)); uv(s, .22, (.25, 1.52, -.57), 'cream', (1.25, .75, .7))
    uv(s, .10, (0, 1.49, -.75), 'black', (1.2, .7, .65))
    eye_pair(s, 1.74, -.57, 'red', .125)
    # Arms, feet and claws.
    for x in (-.56, .56):
        uv(s, .24, (x, .78, -.03), 'blue', (.8, 1.25, .85)); uv(s, .20, (x, .40, -.27), 'blue', (1.15, .65, 1.0))
        for dx in (-.07, 0, .07): cone(s, .045, .012, .16, (x + dx, .37, -.40), 'white')
    normalize(s); return s


def agumon():
    s=trimesh.Scene(); uv(s,.67,(0,.78,0),'orange',(1,1.3,.85)); uv(s,.62,(0,1.75,0),'orange',(1,.95,.9))
    uv(s,.34,(0,1.20,-.48),'cream',(1.0,.85,.55)); eye_pair(s,1.79,-.55,'red',.12)
    uv(s,.08,(0,1.55,-.75),'black',(1,.7,.6))
    for x in (-.47,.47): uv(s,.20,(x,1.55,-.03),'orange',(.75,1.3,.7)); uv(s,.20,(x,.35,-.25),'orange',(1.2,.7,1))
    cone(s,.18,.05,.38,(-.30,2.35,0),'red'); cone(s,.18,.05,.38,(.30,2.35,0),'red')
    for x in (-.53,.53):
        for dx in (-.07,0,.07): cone(s,.05,.012,.18,(x+dx,.28,-.40),'cream')
    normalize(s); return s


def renamon():
    s=trimesh.Scene(); uv(s,.56,(0,.90,0),'yellow',(1,.98,.70)); uv(s,.60,(0,1.75,0),'yellow',(1,.95,.82))
    uv(s,.28,(0,1.25,-.48),'cream',(1,.9,.55)); eye_pair(s,1.78,-.53,'red',.105)
    # Tall ears and purple tips, long arms/legs and tail.
    for x in (-.30,.30): cone(s,.16,.06,.72,(x,2.45,0),'yellow'); cone(s,.12,.025,.25,(x,2.78,0),'purple')
    for x in (-.48,.48): uv(s,.17,(x,1.10,0),'yellow',(.7,1.7,.7)); uv(s,.18,(x,.30,-.22),'yellow',(1.0,.65,1.25))
    uv(s,.22,(0,.70,.55),'purple',(.75,.7,2.3))
    normalize(s); return s


def guilmon():
    s=trimesh.Scene(); uv(s,.66,(0,.82,0),'red',(1,1.25,.85)); uv(s,.64,(0,1.72,0),'red',(1,.98,.88))
    uv(s,.32,(0,1.25,-.50),'white',(1,.9,.55)); eye_pair(s,1.78,-.55,'red',.12)
    cone(s,.19,.06,.50,(-.30,2.38,0),'white'); cone(s,.19,.06,.50,(.30,2.38,0),'white')
    for x in (-.52,.52): uv(s,.22,(x,.70,0),'red',(.8,1.3,.8)); uv(s,.21,(x,.28,-.22),'red',(1.2,.7,1.1))
    uv(s,.30,(0,.95,.50),'red',(.7,.7,1.8)); normalize(s); return s


def generic(name, body, accent, wing=False, horn=True):
    s=trimesh.Scene(); uv(s,.65,(0,.82,0),body,(1,1.28,.84)); uv(s,.62,(0,1.72,0),body,(1,.96,.88))
    uv(s,.30,(0,1.24,-.48),'cream',(1,.9,.55)); eye_pair(s,1.78,-.55,'red',.115)
    if horn:
        cone(s,.16,.05,.55,(-.28,2.40,0),accent); cone(s,.16,.05,.55,(.28,2.40,0),accent)
    for x in (-.50,.50):
        uv(s,.22,(x,.78,0),body,(.85,1.3,.8)); uv(s,.20,(x,.28,-.23),body,(1.15,.7,1.0))
    if wing:
        uv(s,.38,(-.62,1.22,.05),accent,(.55,1.5,.25)); uv(s,.38,(.62,1.22,.05),accent,(.55,1.5,.25))
    uv(s,.20,(0,1.23,-.57),accent,(1.2,.5,.35)); normalize(s); return s

BUILDERS = {
    'Gabumon': gabumon,
    'Agumon': agumon,
    'Guilmon': guilmon,
    'Renamon': renamon,
    'Patamon': lambda: generic('Patamon','cream','yellow',True,False),
    'Gatomon': lambda: generic('Gatomon','cream','purple',False,True),
    'Tentomon': lambda: generic('Tentomon','yellow','blue',True,True),
    'Gomamon': lambda: generic('Gomamon','white','blue',True,False),
    'Palmon': lambda: generic('Palmon','green','pink',True,False),
    'Biyomon': lambda: generic('Biyomon','yellow','red',True,False),
    'Veemon': lambda: generic('Veemon','lightblue','red',False,True),
    'Wormmon': lambda: generic('Wormmon','green','yellow',False,True),
}

for name, builder in BUILDERS.items():
    scene = builder()
    scene.export(os.path.join(OUT, name + '.glb'), file_type='glb')
