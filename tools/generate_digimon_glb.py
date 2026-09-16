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


def uv(scene, radius, pos, mat, scale=(1, 1, 1), seg=32, rings=20):
    m = trimesh.creation.uv_sphere(radius=radius, count=[seg, rings])
    m.apply_scale(scale)
    m.apply_translation(pos)
    add(scene, m, mat)
    return m


def box(scene, extents, pos, mat, rot=None):
    m = trimesh.creation.box(extents=extents)
    if rot:
        m.apply_transform(trimesh.transformations.euler_matrix(*rot))
    m.apply_translation(pos)
    add(scene, m, mat)
    return m


def cyl(scene, radius, height, pos, mat, axis='y'):
    m = trimesh.creation.cylinder(radius=radius, height=height, sections=32)
    if axis == 'x':
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0]))
    if axis == 'z':
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    m.apply_translation(pos)
    add(scene, m, mat)
    return m


def cone(scene, r1, r2, h, pos, mat, axis='y'):
    # trimesh 5.x exposes this primitive as creation.cone(radius, height, radius2).
    # conical_frustum is not part of the public creation API on current runners.
    m = trimesh.creation.cone(radius=r1, height=h, sections=32, radius2=r2)
    if axis == 'x':
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0]))
    if axis == 'z':
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    m.apply_translation(pos)
    add(scene, m, mat)
    return m


def eye_pair(s, y, z, iris='red', size=.115):
    for x in (-.205, .205):
        uv(s, size, (x, y, z), 'black', (1, 1, 1.05), 24, 16)
        uv(s, size * .52, (x, y - .018, z - .055), iris, (1, 1, 1), 20, 14)
        uv(s, size * .18, (x - .025, y - .035, z - .075), 'white', (1, 1, 1), 16, 10)


def patch(s, pos, scale, mat='blue', rot=(0, 0, 0)):
    uv(s, 1.0, pos, mat, scale, 28, 18)


def normalize(s):
    b = s.bounds
    c = (b[0] + b[1]) / 2
    s.apply_translation([-c[0], -b[0][1], -c[2]])


def gabumon():
    s = trimesh.Scene()
    uv(s, .58, (0, .78, 0), 'blue', (1.02, 1.18, .78))
    uv(s, .43, (0, 1.18, -.02), 'blue', (1.05, .72, .82))
    uv(s, .61, (0, 1.66, -.01), 'cream', (1.00, 1.00, .84))
    uv(s, .47, (0, 1.13, -.37), 'cream', (1.05, .90, .58))
    for x, y, sx, sy, sz in [
        (-.31, 1.82, .105, .28, .045), (-.17, 1.94, .09, .34, .045),
        (.17, 1.94, .09, .34, .045), (.31, 1.82, .105, .28, .045),
        (-.40, 1.45, .10, .34, .05), (-.22, 1.36, .10, .42, .05),
        (.22, 1.36, .10, .42, .05), (.40, 1.45, .10, .34, .05),
    ]:
        patch(s, (x, y, -.585 if y > 1.6 else -.455), (sx, sy, sz), 'blue')
    for side in (-1, 1):
        for y, z, sy, sz in [(1.73, -.18, .30, .04), (1.50, -.20, .26, .04), (1.27, -.23, .22, .04)]:
            patch(s, (side * .505, y, z), (.045, sy, sz), 'blue')
    for x in (-.53, .53):
        uv(s, .25, (x, 1.90, -.01), 'blue', (.62, 1.15, .46))
        uv(s, .145, (x, 1.90, -.235), 'pink', (.62, 1.10, .38))
    cone(s, .17, .075, .43, (0, 2.38, -.01), 'yellow')
    uv(s, .225, (-.235, 1.55, -.64), 'cream', (1.28, .72, .66))
    uv(s, .225, (.235, 1.55, -.64), 'cream', (1.28, .72, .66))
    uv(s, .10, (0, 1.49, -.78), 'black', (1.25, .68, .58))
    eye_pair(s, 1.80, -.60, 'red', .125)
    for x in (-.58, .58):
        uv(s, .235, (x, .82, -.02), 'blue', (.78, 1.20, .82))
        uv(s, .19, (x, .43, -.30), 'blue', (1.15, .68, .95))
        for dx in (-.075, 0, .075):
            cone(s, .043, .010, .17, (x + dx, .37, -.43), 'white')
    for x in (-.27, .27):
        uv(s, .25, (x, .20, -.22), 'blue', (1.20, .62, 1.28))
        for dx in (-.075, 0, .075):
            cone(s, .043, .010, .14, (x + dx, .17, -.39), 'white')
    uv(s, .19, (0, .70, .56), 'blue', (.72, .72, 1.75))
    normalize(s)
    return s


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
