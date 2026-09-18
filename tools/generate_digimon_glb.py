import os, math, numpy as np, trimesh
from trimesh.visual.texture import TextureVisuals
from trimesh.visual.material import PBRMaterial

OUT = "app/src/main/assets/digimon"
os.makedirs(OUT, exist_ok=True)

# Clean, character-specific 3D designs based on the recognizable anime silhouettes.
# Smooth primitives are used only as construction pieces; the final meshes are
# dense, rounded and deliberately avoid the old generic blob proportions.

COL = {
    "blue": (20, 83, 185), "darkblue": (8, 38, 98), "cream": (239, 226, 185),
    "white": (250, 249, 244), "orange": (239, 92, 18), "orange2": (255, 139, 26),
    "yellow": (246, 177, 38), "red": (207, 28, 38), "red2": (242, 54, 61),
    "pink": (225, 39, 92), "pink2": (249, 76, 117), "green": (39, 153, 54),
    "green2": (82, 194, 67), "purple": (112, 48, 160), "lightblue": (46, 164, 231),
    "brown": (103, 55, 28), "grey": (128, 142, 151), "black": (4, 5, 9),
}

MATS = {
    k: PBRMaterial(
        name=k,
        baseColorFactor=np.array([v[0], v[1], v[2], 255], dtype=np.uint8),
        metallicFactor=0.0,
        roughnessFactor=0.88,
    )
    for k, v in COL.items()
}

def add(s, mesh, mat):
    # Keep one shared vertex per geometric point so the exported GLB has
    # genuinely smooth anime-style shading instead of a faceted/triangulated
    # "stone" appearance.
    mesh.remove_duplicate_faces()
    mesh.remove_unreferenced_vertices()
    mesh.merge_vertices()
    mesh.process(validate=True)
    mesh.fix_normals()
    _ = mesh.vertex_normals
    mesh.visual = TextureVisuals(uv=None, material=MATS[mat])
    s.add_geometry(mesh)

def uv_sphere(s, r, p, mat, scale=(1,1,1), seg=64, rings=40):
    m = trimesh.creation.uv_sphere(radius=r, count=[seg, rings])
    m.apply_scale(scale)
    m.apply_translation(p)
    add(s, m, mat)
    return m

def capsule(s, radius, height, p, mat, scale=(1,1,1)):
    m = trimesh.creation.capsule(radius=radius, height=height, count=[64,32])
    m.apply_scale(scale)
    m.apply_translation(p)
    add(s, m, mat)
    return m

def cone(s, r1, r2, h, p, mat, axis="y"):
    m = trimesh.creation.cone(radius=r1, radius2=r2, height=h, sections=64)
    if axis == "x":
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi/2, [0,1,0]))
    elif axis == "z":
        m.apply_transform(trimesh.transformations.rotation_matrix(math.pi/2, [1,0,0]))
    m.apply_translation(p)
    add(s, m, mat)
    return m

def rotate(mesh, angle, axis):
    mesh.apply_transform(trimesh.transformations.rotation_matrix(angle, axis))
    return mesh

def eye(s, x, y, z, iris="red", size=.12, look=(0,0,0)):
    uv_sphere(s, size, (x,y,z), "black", (1,1,1.08), 48, 32)
    uv_sphere(s, size*.53, (x+look[0], y+look[1], z+look[2]-.055), iris, (1,1,1), 40, 28)
    uv_sphere(s, size*.15, (x-.035+look[0], y-.04, z-.09+look[2]), "white", (1,1,1), 24, 18)

def claw_row(s, x, y, z, mat, n=3, spread=.075, length=.20):
    for i in range(n):
        dx = (i-(n-1)/2)*spread
        cone(s, .048, .008, length, (x+dx, y, z), mat)

def tuft(s, p, mat, size=.16, tilt=0):
    m = cone(s, size, .015, size*2.2, p, mat)
    rotate(m, tilt, [0,0,1])
    return m

def normalize(s):
    b = s.bounds
    c = (b[0]+b[1])/2
    s.apply_translation([-c[0], -b[0][1], -c[2]])
    return s

def tail_segments(s, pts, mat, r=.16, tip=None):
    n = len(pts)
    for i,p in enumerate(pts):
        rr = r*(1-.58*i/max(1,n-1))
        uv_sphere(s, rr, p, mat, (1,1,1.25), 48, 30)
    if tip:
        cone(s, r*.65, .01, r*2.3, pts[-1], tip)

def belly(s, p, scale, mat="cream"):
    uv_sphere(s, .42, p, mat, scale, 64, 40)

def arm_pair(s, y, z, mat, radius=.18, length=.45, claw="white"):
    for side in (-1,1):
        capsule(s, radius, length, (side*.50,y,z), mat, (.82,1.18,.9))
        claw_row(s, side*.50, y-length*.55, z-.18, claw)

def make_agumon():
    s=trimesh.Scene()
    uv_sphere(s,.64,(0,.88,0),"orange",(1.04,1.28,.90))
    uv_sphere(s,.59,(0,1.68,-.02),"orange",(1.03,1.02,.90))
    belly(s,(0,1.23,-.51),(1.08,.92,.58))
    eye(s,-.205,1.79,-.63,"red",.125); eye(s,.205,1.79,-.63,"red",.125)
    uv_sphere(s,.15,(0,1.56,-.83),"black",(1.45,.62,.58))
    for x in (-.30,.30): tuft(s,(x,2.28,-.02),"orange2",.14,0)
    arm_pair(s,1.25,-.02,"orange",.18,.45,"cream")
    for side in (-1,1): uv_sphere(s,.22,(side*.29,.30,-.28),"orange",(1.2,.72,1.08),56,36)
    tail_segments(s,[(0,.72,.42),(0,.49,.64),(0,.28,.52)],"orange",.20)
    return normalize(s)

def make_gabumon():
    s=trimesh.Scene()
    # Blue body with the characteristic pale fur head and muzzle.
    uv_sphere(s,.62,(0,.82,0),"blue",(1.05,1.25,.88))
    uv_sphere(s,.50,(0,1.34,.01),"blue",(1.05,.82,.86))
    uv_sphere(s,.60,(0,1.77,-.01),"cream",(1.02,1.02,.90))
    belly(s,(0,1.20,-.53),(1.10,.88,.56))
    uv_sphere(s,.23,(-.22,1.55,-.67),"cream",(1.32,.72,.62))
    uv_sphere(s,.23,(.22,1.55,-.67),"cream",(1.32,.72,.62))
    eye(s,-.205,1.84,-.66,"red",.125); eye(s,.205,1.84,-.66,"red",.125)
    uv_sphere(s,.11,(0,1.53,-.84),"black",(1.25,.70,.58))
    for side in (-1,1):
        cone(s,.18,.045,.58,(side*.30,2.40,-.01),"yellow")
        uv_sphere(s,.24,(side*.54,1.43,0),"blue",(.70,1.28,.64))
        uv_sphere(s,.16,(side*.55,1.53,-.23),"pink",(.72,1.12,.40))
        uv_sphere(s,.20,(side*.30,.28,-.27),"blue",(1.25,.68,1.08))
        claw_row(s,side*.30,.13,-.45,"white",3,.075,.18)
    # Fur tufts around the forehead.
    for x,y,sc in [(-.40,2.03,.72),(-.20,2.14,.78),(0,2.19,.86),(.20,2.14,.78),(.40,2.03,.72)]:
        tuft(s,(x,y,.10),"white",.10*sc,0)
    tail_segments(s,[(0,.72,.45),(0,.48,.67)],"blue",.18)
    return normalize(s)

def make_guilmon():
    s=trimesh.Scene()
    uv_sphere(s,.67,(0,.84,0),"red",(1.05,1.30,.92))
    uv_sphere(s,.63,(0,1.72,0),"red",(1.04,1.03,.92))
    belly(s,(0,1.22,-.53),(1.04,.90,.58),"white")
    eye(s,-.21,1.79,-.64,"red",.13); eye(s,.21,1.79,-.64,"red",.13)
    uv_sphere(s,.11,(0,1.51,-.84),"black",(1.3,.68,.55))
    for side in (-1,1):
        cone(s,.21,.05,.54,(side*.30,2.40,0),"white")
        capsule(s,.18,.48,(side*.52,1.10,0),"red",(.82,1.28,.88))
        uv_sphere(s,.20,(side*.29,.28,-.28),"red",(1.22,.70,1.10))
        claw_row(s,side*.29,.14,-.45,"white")
    for x in (-.14,0,.14): cone(s,.07,.01,.18,(x,1.34,-.84),"black")
    tail_segments(s,[(0,.72,.45),(0,.47,.69),(0,.27,.61)],"red",.20)
    return normalize(s)

def make_renamon():
    s=trimesh.Scene()
    uv_sphere(s,.55,(0,.82,0),"yellow",(1,.98,.76))
    uv_sphere(s,.58,(0,1.75,0),"yellow",(1,.99,.86))
    belly(s,(0,1.21,-.51),(1,.90,.55),"cream")
    eye(s,-.20,1.81,-.61,"red",.115); eye(s,.20,1.81,-.61,"red",.115)
    for side in (-1,1):
        cone(s,.17,.035,.82,(side*.30,2.47,0),"yellow")
        cone(s,.09,.018,.28,(side*.30,2.95,0),"purple")
        capsule(s,.17,.50,(side*.48,1.08,0),"yellow",(.72,1.48,.72))
        uv_sphere(s,.17,(side*.42,.37,-.22),"yellow",(1,.65,1.20))
        claw_row(s,side*.42,.16,-.42,"white")
    tail_segments(s,[(0,.68,.48),(0,.43,.74),(0,.18,.67)],"yellow",.18,"purple")
    return normalize(s)

def make_biyomon():
    s=trimesh.Scene()
    uv_sphere(s,.64,(0,.82,0),"pink",(1.05,1.26,.88))
    uv_sphere(s,.58,(0,1.69,0),"pink",(1.04,.96,.89))
    belly(s,(0,1.10,-.55),(1.04,1.0,.42),"cream")
    eye(s,-.20,1.82,-.76,"blue",.12); eye(s,.20,1.82,-.76,"blue",.12)
    uv_sphere(s,.15,(0,1.58,-.88),"orange2",(1.35,.58,.68))
    uv_sphere(s,.105,(0,1.47,-.86),"orange",(1.30,.50,.64))
    for x,y,sc in [(0,2.30,1),(-.18,2.16,.82),(.18,2.16,.82),(-.34,2.04,.64),(.34,2.04,.64)]:
        tuft(s,(x,y,0),"red2",.17*sc,0)
    for side in (-1,1):
        for i,(yy,rr) in enumerate([(1.36,.28),(1.12,.25),(.91,.21)]):
            uv_sphere(s,rr,(side*(.53+i*.04),yy,.02),"blue",( .70,1.20,.58),56,36)
        uv_sphere(s,.23,(side*.29,.24,-.27),"orange",(1.25,.68,1.25))
        claw_row(s,side*.29,.13,-.43,"orange")
    tail_segments(s,[(0,.72,.48),(0,.52,.72)],"pink",.17,"blue")
    return normalize(s)

def make_patamon():
    s=trimesh.Scene()
    uv_sphere(s,.62,(0,.84,0),"cream",(1.02,1.25,.88))
    uv_sphere(s,.58,(0,1.70,0),"cream",(1,.98,.87))
    belly(s,(0,1.20,-.51),(1,.91,.56),"cream")
    eye(s,-.20,1.81,-.59,"red",.11); eye(s,.20,1.81,-.59,"red",.11)
    for side in (-1,1):
        # Large floppy ears/wings are the defining Patamon silhouette.
        uv_sphere(s,.42,(side*.52,1.58,.03),"cream",(.56,1.45,.34),64,40)
        uv_sphere(s,.30,(side*.69,1.10,.03),"cream",(.50,1.45,.30),64,40)
        uv_sphere(s,.21,(side*.29,.30,-.26),"cream",(1.25,.70,1.12))
        claw_row(s,side*.29,.14,-.43,"white")
    tail_segments(s,[(0,.68,.46),(0,.44,.67)],"cream",.14,"yellow")
    return normalize(s)

def make_gatomon():
    s=trimesh.Scene()
    uv_sphere(s,.49,(0,.87,0),"cream",(1,.99,.74))
    uv_sphere(s,.55,(0,1.74,0),"cream",(1,.98,.86))
    belly(s,(0,1.22,-.51),(1,.90,.54),"cream")
    eye(s,-.20,1.81,-.60,"blue",.108); eye(s,.20,1.81,-.60,"blue",.108)
    for side in (-1,1):
        cone(s,.14,.035,.62,(side*.28,2.44,0),"yellow")
        capsule(s,.17,.50,(side*.46,1.16,0),"cream",(.72,1.48,.68))
        uv_sphere(s,.17,(side*.31,.38,-.23),"cream",(1,.66,1.16))
        claw_row(s,side*.31,.17,-.42,"white")
    uv_sphere(s,.33,(0,1.31,-.52),"yellow",(1.2,.20,.22))
    tail_segments(s,[(0,.70,.47),(.12,.47,.69),(.23,.23,.73)],"cream",.13,"yellow")
    return normalize(s)

def make_tentomon():
    s=trimesh.Scene()
    uv_sphere(s,.66,(0,.84,0),"red",(1.02,1.24,.92))
    uv_sphere(s,.57,(0,1.68,0),"red",(1,.94,.88))
    belly(s,(0,1.20,-.52),(1,.88,.55),"yellow")
    eye(s,-.21,1.79,-.59,"blue",.11); eye(s,.21,1.79,-.59,"blue",.11)
    for side in (-1,1):
        cone(s,.07,.018,.52,(side*.25,2.30,0),"yellow")
        uv_sphere(s,.08,(side*.25,2.59,0),"yellow")
        uv_sphere(s,.42,(side*.47,1.24,.08),"yellow",(.70,1.30,.34))
        uv_sphere(s,.21,(side*.47,1.09,-.18),"black",(.75,1,.26))
        uv_sphere(s,.20,(side*.28,.28,-.27),"yellow",(1.25,.68,1.10))
        claw_row(s,side*.28,.14,-.43,"black")
    tail_segments(s,[(0,.68,.45),(0,.45,.68)],"red",.16)
    return normalize(s)

def make_gomamon():
    s=trimesh.Scene()
    uv_sphere(s,.62,(0,.83,0),"white",(1,1.28,.88))
    uv_sphere(s,.59,(0,1.68,0),"white",(1,.98,.87))
    belly(s,(0,1.20,-.51),(1,.90,.55),"white")
    eye(s,-.20,1.80,-.59,"blue",.11); eye(s,.20,1.80,-.59,"blue",.11)
    for side in (-1,1):
        uv_sphere(s,.12,(side*.51,1.77,-.18),"pink")
        uv_sphere(s,.30,(side*.56,1.14,.02),"white",(.55,1.62,.35))
        uv_sphere(s,.20,(side*.28,.27,-.27),"white",(1.25,.68,1.15))
        claw_row(s,side*.28,.14,-.43,"pink")
    for x in (-.32,-.16,0,.16,.32): tuft(s,(x,2.22,.05),"red",.075,0)
    tail_segments(s,[(0,.70,.46),(0,.48,.67)],"white",.17)
    return normalize(s)

def make_palmon():
    s=trimesh.Scene()
    uv_sphere(s,.64,(0,.83,0),"green",(1,1.25,.88))
    uv_sphere(s,.57,(0,1.68,0),"green",(1,.97,.86))
    belly(s,(0,1.20,-.51),(1,.90,.55),"cream")
    eye(s,-.20,1.80,-.59,"red",.11); eye(s,.20,1.80,-.59,"red",.11)
    # Leaf crown, broad and irregular rather than cones.
    for i,a in enumerate(np.linspace(-1.15,1.15,5)):
        uv_sphere(s,.19,(.38*math.sin(a),2.18,.05+.15*math.cos(a)),"green2",(.52,1.40,.28),48,30)
    for side in (-1,1):
        uv_sphere(s,.26,(side*.49,1.13,0),"green",(.76,1.45,.62))
        uv_sphere(s,.22,(side*.28,.27,-.27),"green",(1.20,.68,1.10))
        claw_row(s,side*.28,.14,-.43,"pink")
    uv_sphere(s,.18,(-.18,.72,.53),"green2",(.72,1,1.6))
    uv_sphere(s,.18,(.18,.72,.53),"green2",(.72,1,1.6))
    return normalize(s)

def make_veemon():
    s=trimesh.Scene()
    uv_sphere(s,.63,(0,.83,0),"lightblue",(1.03,1.24,.89))
    uv_sphere(s,.59,(0,1.69,0),"lightblue",(1,.99,.88))
    belly(s,(0,1.20,-.51),(1,.90,.55),"cream")
    eye(s,-.205,1.80,-.59,"red",.115); eye(s,.205,1.80,-.59,"red",.115)
    for side in (-1,1):
        cone(s,.16,.035,.72,(side*.29,2.38,0),"lightblue")
        cone(s,.10,.015,.30,(side*.29,2.80,0),"yellow")
        capsule(s,.22,.48,(side*.50,1.12,0),"lightblue",(.76,1.42,.72))
        uv_sphere(s,.20,(side*.28,.29,-.27),"lightblue",(1.20,.68,1.10))
        claw_row(s,side*.28,.14,-.43,"white")
    cone(s,.18,.01,.28,(0,1.31,-.70),"yellow")
    tail_segments(s,[(0,.70,.46),(0,.46,.70)],"lightblue",.18,"yellow")
    return normalize(s)

def make_wormmon():
    s=trimesh.Scene()
    uv_sphere(s,.63,(0,.83,0),"green",(1,1.30,.89))
    uv_sphere(s,.58,(0,1.70,0),"green",(1,.99,.88))
    belly(s,(0,1.20,-.51),(1,.90,.55),"yellow")
    eye(s,-.21,1.80,-.59,"red",.11); eye(s,.21,1.80,-.59,"red",.11)
    for side in (-1,1):
        cone(s,.09,.02,.62,(side*.25,2.35,0),"green2")
        uv_sphere(s,.08,(side*.25,2.68,0),"yellow")
        for i in range(3):
            uv_sphere(s,.19,(side*(.45+i*.035),1.23-i*.12,.04),"green2",(.65,1.12,.65))
        uv_sphere(s,.20,(side*.28,.28,-.27),"green",(1.20,.68,1.10))
        claw_row(s,side*.28,.14,-.43,"yellow")
    tail_segments(s,[(0,.68,.46),(0,.44,.68),(0,.22,.58)],"green",.18,"yellow")
    return normalize(s)

BUILDERS = {
    "Agumon": make_agumon, "Gabumon": make_gabumon, "Guilmon": make_guilmon,
    "Renamon": make_renamon, "Biyomon": make_biyomon, "Patamon": make_patamon,
    "Gatomon": make_gatomon, "Tentomon": make_tentomon, "Gomamon": make_gomamon,
    "Palmon": make_palmon, "Veemon": make_veemon, "Wormmon": make_wormmon,
}

for name, builder in BUILDERS.items():
    path = os.path.join(OUT, name + ".glb")
    scene = builder()
    # Recompute smooth normals after the complete character is assembled.
    for geom in scene.geometry.values():
        if isinstance(geom, trimesh.Trimesh):
            geom.remove_unreferenced_vertices()
            geom.merge_vertices()
            geom.process(validate=True)
            geom.fix_normals()
            _ = geom.vertex_normals
    scene.export(path, file_type="glb")
    print("Generated anime-style", name, os.path.getsize(path))
