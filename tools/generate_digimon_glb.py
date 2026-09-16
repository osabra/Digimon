import os
import math
import trimesh
from trimesh.visual.material import PBRMaterial

OUT = 'app/src/main/assets/digimon'
os.makedirs(OUT, exist_ok=True)

# Stylised, anime-faithful materials.  Geometry is deliberately smooth/high-poly
# so the companion reads correctly while rotating through the full 360 degrees.
COLORS = {
    'blue': (0.025, 0.19, 0.58), 'blue2': (0.05, 0.31, 0.82), 'darkblue': (0.012, 0.055, 0.16),
    'cream': (0.90, 0.84, 0.68), 'white': (0.97, 0.97, 0.94),
    'orange': (0.95, 0.34, 0.035), 'orange2': (1.0, 0.52, 0.04), 'yellow': (0.96, 0.65, 0.035),
    'red': (0.72, 0.025, 0.035), 'red2': (0.90, 0.08, 0.06), 'pink': (0.88, 0.12, 0.30),
    'pink2': (0.98, 0.24, 0.43), 'green': (0.08, 0.47, 0.12), 'green2': (0.20, 0.66, 0.15),
    'purple': (0.43, 0.08, 0.62), 'lightblue': (0.10, 0.49, 0.88),
    'brown': (0.30, 0.12, 0.035), 'grey': (0.40, 0.44, 0.47), 'black': (0.006, 0.006, 0.009)
}
M = {
    k: PBRMaterial(name=k, baseColorFactor=[round(v * 255) for v in rgb] + [255],
                   roughnessFactor=0.55, metallicFactor=0.0)
    for k, rgb in COLORS.items()
}


def add(s, mesh, mat):
    mesh.remove_duplicate_faces() if hasattr(mesh, 'remove_duplicate_faces') else None
    mesh.visual.material = M[mat]
    mesh.process(validate=True)
    s.add_geometry(mesh)


def sphere(s, r, pos, mat, scale=(1, 1, 1), seg=48, rings=32):
    m = trimesh.creation.uv_sphere(radius=r, count=[seg, rings])
    m.apply_scale(scale)
    m.apply_translation(pos)
    add(s, m, mat)
    return m


def capsule(s, r, h, pos, mat, axis='y', scale=(1, 1, 1)):
    # Cylinder + rounded ends gives better limbs than stretched spheres.
    m = trimesh.creation.capsule(radius=r, height=h, count=[48, 24])
    m.apply_scale(scale)
    if axis == 'x':
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0]))
    elif axis == 'z':
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    m.apply_translation(pos)
    add(s, m, mat)
    return m


def cone(s, r1, r2, h, pos, mat, axis='y'):
    m = trimesh.creation.cone(radius=r1, radius2=r2, height=h, sections=48)
    if axis == 'x':
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [0, 1, 0]))
    elif axis == 'z':
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi / 2, [1, 0, 0]))
    m.apply_translation(pos)
    add(s, m, mat)
    return m


def eye(s, x, y, z, iris='red', size=.115):
    sphere(s, size, (x, y, z), 'black', (1, 1, 1.04), 36, 24)
    sphere(s, size * .55, (x, y - .018, z - .055), iris, (1, 1, 1), 28, 20)
    sphere(s, size * .17, (x - .025, y - .035, z - .075), 'white', (1, 1, 1), 20, 14)


def eyes(s, y, z, iris='red', x=.20, size=.115):
    eye(s, -x, y, z, iris, size); eye(s, x, y, z, iris, size)


def claw(s, x, y, z, mat='white', n=3, spread=.07):
    for i in range(n):
        dx = (i - (n - 1) / 2) * spread
        cone(s, .045, .008, .16, (x + dx, y, z), mat)


def tail(s, points, mat, radius=.16, tip_mat=None):
    # Chain of tapered rounded segments gives a readable tail from every angle.
    for i, (x, y, z) in enumerate(points):
        rr = radius * (1 - .55 * i / max(1, len(points) - 1))
        sphere(s, rr, (x, y, z), mat, (1.0, 1.0, 1.15), 36, 24)
    if tip_mat:
        x, y, z = points[-1]
        cone(s, radius*.65, .01, radius*2.4, (x, y, z + radius*.75), tip_mat)


def normalize(s):
    b = s.bounds
    c = (b[0] + b[1]) / 2
    s.apply_translation([-c[0], -b[0][1], -c[2]])
    return s


def agumon():
    s=trimesh.Scene()
    sphere(s,.62,(0,.78,0),'orange',(1.0,1.22,.86))
    sphere(s,.60,(0,1.62,-.02),'orange',(1.0,.98,.88))
    sphere(s,.38,(0,1.20,-.49),'cream',(1.02,.90,.55))
    eyes(s,1.72,-.55,'red',.205,.12)
    sphere(s,.10,(0,1.50,-.78),'black',(1.35,.65,.55))
    # Agumon's distinctive small horns and muzzle.
    cone(s,.15,.045,.34,(-.30,2.18,-.02),'orange2'); cone(s,.15,.045,.34,(.30,2.18,-.02),'orange2')
    for side in (-1,1):
        capsule(s,.18,.40,(side*.48,1.25,-.01),'orange',axis='y',scale=(.78,1.35,.82))
        sphere(s,.20,(side*.48,.30,-.24),'orange',(1.20,.68,1.05))
        claw(s,side*.48,.17,-.42,'cream')
    tail(s,[(0,.70,.42),(0,.48,.57),(0,.25,.48)],'orange',.20)
    return normalize(s)


def gabumon():
    s=trimesh.Scene()
    sphere(s,.60,(0,.76,0),'blue',(1.02,1.18,.84))
    sphere(s,.47,(0,1.24,-.01),'blue',(1.04,.78,.82))
    sphere(s,.60,(0,1.70,-.01),'cream',(1.0,1.0,.86))
    sphere(s,.44,(0,1.22,-.42),'cream',(1.08,.88,.62))
    # Fur hood strips: separate raised meshes, visible in rotation.
    for x,y,sy in [(-.32,1.82,.28),(-.18,1.95,.34),(.18,1.95,.34),(.32,1.82,.28),(-.43,1.48,.34),(.43,1.48,.34)]:
        sphere(s,.09,(x,y,-.59 if y>1.6 else -.46),'white',(.9,sy/.09,.25),32,20)
    eyes(s,1.80,-.60,'red',.205,.125)
    sphere(s,.105,(0,1.49,-.78),'black',(1.25,.68,.58))
    sphere(s,.23,(-.22,1.53,-.64),'cream',(1.3,.72,.65)); sphere(s,.23,(.22,1.53,-.64),'cream',(1.3,.72,.65))
    # Long ears/horns and arms.
    cone(s,.17,.07,.42,(0,2.36,-.01),'yellow')
    for side in (-1,1):
        sphere(s,.23,(side*.52,1.58,-.02),'blue',(.70,1.15,.55))
        sphere(s,.15,(side*.52,1.58,-.23),'pink',(.68,1.05,.38))
        sphere(s,.22,(side*.54,.84,-.02),'blue',(.80,1.25,.82))
        sphere(s,.19,(side*.28,.25,-.23),'blue',(1.2,.65,1.15)); claw(s,side*.28,.15,-.40,'white')
    tail(s,[(0,.72,.50),(0,.50,.68)],'blue',.17)
    return normalize(s)


def guilmon():
    s=trimesh.Scene(); sphere(s,.66,(0,.78,0),'red',(1,1.25,.88)); sphere(s,.63,(0,1.68,0),'red',(1,.98,.90))
    sphere(s,.36,(0,1.20,-.50),'white',(1,.88,.55)); eyes(s,1.76,-.55,'red',.21,.12)
    # White brow/horn structures and characteristic chest markings.
    for side in (-1,1): cone(s,.19,.055,.50,(side*.29,2.35,0),'white')
    cone(s,.12,.03,.22,(0,1.47,-.79),'black')
    for side in (-1,1):
        sphere(s,.23,(side*.52,.88,0),'red',(.80,1.25,.82)); sphere(s,.20,(side*.28,.25,-.24),'red',(1.25,.68,1.1)); claw(s,side*.28,.16,-.40,'white')
    tail(s,[(0,.68,.45),(0,.48,.68)],'red',.20)
    return normalize(s)


def renamon():
    s=trimesh.Scene(); sphere(s,.53,(0,.84,0),'yellow',(1,.98,.72)); sphere(s,.58,(0,1.74,0),'yellow',(1,.96,.84))
    sphere(s,.30,(0,1.22,-.48),'cream',(1,.88,.56)); eyes(s,1.80,-.54,'red',.20,.105)
    for side in (-1,1):
        cone(s,.15,.045,.72,(side*.30,2.42,0),'yellow'); cone(s,.10,.018,.23,(side*.30,2.84,0),'purple')
        sphere(s,.18,(side*.48,1.15,0),'yellow',(.70,1.7,.70)); sphere(s,.18,(side*.42,.38,-.18),'yellow',(1,.65,1.2)); claw(s,side*.42,.20,-.38,'white')
    tail(s,[(0,.68,.50),(0,.42,.72),(0,.18,.67)],'yellow',.18, 'purple')
    return normalize(s)


def biyomon():
    s=trimesh.Scene(); sphere(s,.62,(0,.80,0),'pink',(1,1.20,.84)); sphere(s,.58,(0,1.67,-.01),'pink',(1.02,.93,.88))
    sphere(s,.45,(0,1.62,-.49),'cream',(1,.76,.45)); sphere(s,.40,(0,1.00,-.53),'cream',(1.03,1.02,.38))
    eyes(s,1.79,-.76,'blue2',.19,.105)
    sphere(s,.14,(0,1.57,-.83),'orange2',(1.45,.55,.70)); sphere(s,.10,(0,1.48,-.80),'orange',(1.35,.48,.62))
    # Three-part red crest and layered wings.
    sphere(s,.18,(0,2.24,-.02),'red',(0.62,1.55,.65)); sphere(s,.13,(-.16,2.13,-.04),'red2',(.62,1.35,.55)); sphere(s,.13,(.16,2.13,-.04),'red2',(.62,1.35,.55))
    for side in (-1,1):
        sphere(s,.34,(side*.52,1.20,.02),'pink',(.72,1.25,.62)); sphere(s,.27,(side*.63,.98,.04),'pink2',(.68,1.10,.58))
        for i,(y,z,rr) in enumerate([(1.28,.02,.25),(1.08,.0,.22),(.90,-.01,.19)]): sphere(s,rr,(side*(.68+i*.03),y,z),'blue2',(.72,1.15,.55))
        sphere(s,.23,(side*.27,.22,-.23),'orange',(1.25,.62,1.30)); claw(s,side*.27,.16,-.39,'orange',3,.07)
    tail(s,[(0,.72,.48),(0,.52,.70)],'pink',.17,'blue2')
    return normalize(s)


def patamon():
    s=trimesh.Scene(); sphere(s,.60,(0,.82,0),'cream',(1,1.20,.84)); sphere(s,.58,(0,1.68,0),'cream',(1,.95,.86))
    sphere(s,.31,(0,1.20,-.49),'cream',(1,.9,.55)); eyes(s,1.78,-.55,'red',.20,.11)
    # Huge floppy ears are essential to the silhouette.
    for side in (-1,1):
        sphere(s,.40,(side*.48,1.60,.02),'cream',(.55,1.35,.34)); sphere(s,.28,(side*.65,1.15,.02),'cream',(.52,1.45,.30))
        sphere(s,.21,(side*.28,.27,-.24),'cream',(1.25,.65,1.15)); claw(s,side*.28,.16,-.40,'white')
    tail(s,[(0,.68,.46),(0,.45,.66)],'cream',.14,'yellow')
    return normalize(s)


def gatomon():
    s=trimesh.Scene(); sphere(s,.47,(0,.86,0),'cream',(1,.98,.72)); sphere(s,.55,(0,1.72,0),'cream',(1,.96,.84))
    sphere(s,.28,(0,1.24,-.48),'cream',(1,.9,.55)); eyes(s,1.80,-.54,'blue2',.20,.10)
    for side in (-1,1):
        cone(s,.12,.035,.52,(side*.27,2.40,0),'yellow')
        sphere(s,.17,(side*.43,1.18,0),'cream',(.72,1.55,.65)); sphere(s,.17,(side*.31,.38,-.22),'cream',(1,.65,1.15)); claw(s,side*.31,.18,-.38,'white')
    # Ring-like collar and long tail.
    sphere(s,.32,(0,1.30,-.50),'yellow',(1.18,.22,.22)); tail(s,[(0,.70,.48),(.12,.48,.66),(.22,.25,.72)],'cream',.13,'yellow')
    return normalize(s)


def tentomon():
    s=trimesh.Scene(); sphere(s,.66,(0,.82,0),'red',(1,1.20,.88)); sphere(s,.60,(0,1.67,0),'red',(1,.95,.88))
    sphere(s,.31,(0,1.20,-.50),'yellow',(1,.9,.55)); eyes(s,1.78,-.55,'blue2',.21,.105)
    # Insect antennae, shell wings and black spots.
    for side in (-1,1):
        cone(s,.07,.018,.48,(side*.25,2.30,0),'yellow'); sphere(s,.08,(side*.25,2.56,0),'yellow')
        sphere(s,.40,(side*.43,1.22,.08),'yellow',(.70,1.25,.32)); sphere(s,.20,(side*.45,1.10,-.18),'black',(.75,1,.25))
        sphere(s,.19,(side*.28,.28,-.24),'yellow',(1.25,.65,1.1)); claw(s,side*.28,.16,-.40,'black')
    tail(s,[(0,.68,.46),(0,.45,.66)],'red',.16)
    return normalize(s)


def gomamon():
    s=trimesh.Scene(); sphere(s,.60,(0,.82,0),'white',(1,1.25,.86)); sphere(s,.58,(0,1.68,0),'white',(1,.96,.86))
    sphere(s,.31,(0,1.22,-.50),'white',(1,.9,.55)); eyes(s,1.79,-.55,'blue2',.20,.11)
    # Red whisker dots, ears and long flippers.
    for side in (-1,1):
        sphere(s,.10,(side*.50,1.75,-.18),'pink'); sphere(s,.28,(side*.55,1.12,.02),'white',(.55,1.55,.34)); sphere(s,.20,(side*.28,.25,-.24),'white',(1.25,.65,1.15)); claw(s,side*.28,.16,-.40,'pink')
    tail(s,[(0,.70,.46),(0,.48,.67)],'white',.17)
    return normalize(s)


def palmon():
    s=trimesh.Scene(); sphere(s,.63,(0,.82,0),'green',(1,1.20,.86)); sphere(s,.57,(0,1.68,0),'green',(1,.95,.86))
    sphere(s,.31,(0,1.22,-.49),'cream',(1,.9,.55)); eyes(s,1.78,-.55,'red',.20,.11)
    # Leaf crown and flower-like hands.
    for i,a in enumerate((-1.05,-.55,0,.55,1.05)):
        x=.32*math.sin(a); z=.02+.16*math.cos(a)
        sphere(s,.18,(x,2.15,z),'green2',(.55,1.25,.28))
    for side in (-1,1):
        sphere(s,.25,(side*.48,1.12,0),'green',(.75,1.45,.60)); sphere(s,.22,(side*.28,.26,-.24),'green',(1.2,.65,1.1)); claw(s,side*.28,.16,-.40,'pink')
    # Large flower/leaf tail.
    for side in (-1,1): sphere(s,.17,(side*.18,.70,.52),'green2',(.7,1.0,1.6))
    return normalize(s)


def veemon():
    s=trimesh.Scene(); sphere(s,.62,(0,.82,0),'lightblue',(1,1.20,.86)); sphere(s,.59,(0,1.68,0),'lightblue',(1,.98,.88))
    sphere(s,.32,(0,1.20,-.49),'cream',(1,.9,.55)); eyes(s,1.79,-.55,'red',.205,.115)
    # Long V-shaped ears, arm bands and chest emblem.
    for side in (-1,1):
        cone(s,.15,.035,.65,(side*.27,2.35,0),'lightblue'); cone(s,.10,.015,.30,(side*.27,2.72,0),'yellow')
        sphere(s,.22,(side*.50,1.12,0),'lightblue',(.75,1.35,.72)); sphere(s,.20,(side*.28,.28,-.24),'lightblue',(1.2,.65,1.1)); claw(s,side*.28,.16,-.40,'white')
    cone(s,.18,.01,.28,(0,1.30,-.69),'yellow')
    tail(s,[(0,.70,.45),(0,.45,.70)],'lightblue',.18,'yellow')
    return normalize(s)


def wormmon():
    s=trimesh.Scene(); sphere(s,.62,(0,.82,0),'green',(1,1.30,.88)); sphere(s,.58,(0,1.70,0),'green',(1,.98,.88))
    sphere(s,.31,(0,1.22,-.50),'yellow',(1,.9,.55)); eyes(s,1.79,-.55,'red',.21,.11)
    # Long feelers and segmented body, rather than a generic humanoid.
    for side in (-1,1):
        cone(s,.09,.02,.58,(side*.25,2.32,0),'green2'); sphere(s,.08,(side*.25,2.63,0),'yellow')
        for i in range(3): sphere(s,.18,(side*(.45+i*.03),1.22-i*.12,.04),'green2',(.65,1.1,.65))
        sphere(s,.20,(side*.28,.27,-.24),'green',(1.2,.65,1.1)); claw(s,side*.28,.16,-.40,'yellow')
    tail(s,[(0,.68,.46),(0,.44,.66),(0,.22,.58)],'green',.18,'yellow')
    return normalize(s)


BUILDERS = {
    'Agumon': agumon, 'Gabumon': gabumon, 'Guilmon': guilmon, 'Renamon': renamon,
    'Biyomon': biyomon, 'Patamon': patamon, 'Gatomon': gatomon, 'Tentomon': tentomon,
    'Gomamon': gomamon, 'Palmon': palmon, 'Veemon': veemon, 'Wormmon': wormmon,
}

for name, builder in BUILDERS.items():
    builder().export(os.path.join(OUT, name + '.glb'), file_type='glb')
    print('Generated', name)
