import os, math, numpy as np, trimesh
from PIL import Image, ImageDraw
from trimesh.visual.texture import TextureVisuals, SimpleMaterial

OUT = 'app/src/main/assets/digimon'
os.makedirs(OUT, exist_ok=True)

# Original stylised 3D companions with embedded base-colour textures.
# The geometry is deliberately rounded and layered so each character keeps a
# recognizable silhouette while remaining light enough for an Android viewer.
COL = {
    'blue': (18,78,190), 'blue2': (34,112,235), 'darkblue': (8,35,95),
    'cream': (235,222,178), 'white': (250,249,242),
    'orange': (238,92,18), 'orange2': (255,139,26), 'yellow': (245,174,35),
    'red': (205,25,34), 'red2': (244,50,54), 'pink': (225,40,91),
    'pink2': (250,70,115), 'green': (36,151,53), 'green2': (76,190,61),
    'purple': (122,50,164), 'lightblue': (45,165,232), 'brown': (105,55,25),
    'grey': (130,145,155), 'black': (5,6,10)
}


def texture_for(name, base):
    # Embedded hand-painted/toon texture: subtle grain, vignette and highlight.
    rng = np.random.default_rng(abs(hash(name)) % (2**32))
    h = w = 128
    a = np.array(base, dtype=np.float32).reshape(1, 1, 3)
    noise = rng.normal(0, 5, (h, w, 1))
    yy, xx = np.mgrid[0:h, 0:w]
    vignette = ((xx - w/2)**2 + (yy - h/2)**2) / (w*w)
    arr = np.clip(a + noise - 7*vignette[..., None], 0, 255).astype(np.uint8)
    im = Image.fromarray(arr, 'RGB')
    d = ImageDraw.Draw(im)
    d.ellipse((10, 10, 35, 35), fill=tuple(np.clip(a[0,0] + 25, 0, 255).astype(int)))
    return im


MATS = {k: SimpleMaterial(image=texture_for(k, v)) for k, v in COL.items()}


def spherical_uv(mesh):
    v = mesh.vertices
    r = np.linalg.norm(v, axis=1)
    r[r < 1e-6] = 1
    u = np.arctan2(v[:, 2], v[:, 0]) / (2*np.pi) + .5
    vv = np.arcsin(np.clip(v[:, 1] / r, -1, 1)) / np.pi + .5
    return np.c_[u, vv]


def add(scene, mesh, mat):
    if hasattr(mesh, 'remove_duplicate_faces'):
        mesh.remove_duplicate_faces()
    mesh.visual = TextureVisuals(uv=spherical_uv(mesh), material=MATS[mat])
    mesh.process(validate=True)
    scene.add_geometry(mesh)


def sph(scene, radius, pos, mat, scale=(1,1,1), seg=64, rings=40):
    mesh = trimesh.creation.uv_sphere(radius=radius, count=[seg, rings])
    mesh.apply_scale(scale)
    mesh.apply_translation(pos)
    add(scene, mesh, mat)


def cap(scene, radius, height, pos, mat, scale=(1,1,1)):
    mesh = trimesh.creation.capsule(radius=radius, height=height, count=[64,32])
    mesh.apply_scale(scale)
    mesh.apply_translation(pos)
    add(scene, mesh, mat)


def cone(scene, r1, r2, height, pos, mat, axis='y'):
    mesh = trimesh.creation.cone(radius=r1, radius2=r2, height=height, sections=64)
    if axis == 'x':
        mesh.apply_transform(trimesh.transformations.rotation_matrix(math.pi/2, [0,1,0]))
    elif axis == 'z':
        mesh.apply_transform(trimesh.transformations.rotation_matrix(math.pi/2, [1,0,0]))
    mesh.apply_translation(pos)
    add(scene, mesh, mat)


def eye(scene, x, y, z, iris='red', size=.13):
    sph(scene, size, (x,y,z), 'black', (1,1,1.04), 48, 32)
    sph(scene, size*.55, (x,y-.015,z-.06), iris, (1,1,1), 40, 28)
    sph(scene, size*.16, (x-.03,y-.035,z-.09), 'white', (1,1,1), 24, 18)


def claws(scene, x, y, z, mat, n=3, spread=.065):
    for i in range(n):
        cone(scene, .045, .008, .17, (x+(i-(n-1)/2)*spread,y,z), mat)


def tail(scene, points, mat, radius=.16, tip=None):
    for i, p in enumerate(points):
        sph(scene, radius*(1-.48*i/max(1,len(points)-1)), p, mat, (1,1,1.18), 48, 30)
    if tip:
        cone(scene, radius*.6, .01, radius*2.2, points[-1], tip)


def normalize(scene):
    b = scene.bounds
    c = (b[0] + b[1]) / 2
    scene.apply_translation([-c[0], -b[0][1], -c[2]])
    return scene


def agumon():
    s=trimesh.Scene(); sph(s,.62,(0,.78,0),'orange',(1.03,1.25,.9)); sph(s,.58,(0,1.63,-.03),'orange',(1.02,1,.9)); sph(s,.37,(0,1.2,-.51),'cream',(1.05,.9,.55)); eye(s,-.2,1.75,-.61,'red',.125); eye(s,.2,1.75,-.61,'red',.125); sph(s,.13,(0,1.53,-.8),'black',(1.35,.62,.55))
    for x in (-.3,.3): cone(s,.14,.045,.36,(x,2.17,-.02),'orange2')
    for side in (-1,1): cap(s,.17,.42,(side*.5,1.25,-.02),'orange',(.82,1.25,.85)); sph(s,.2,(side*.48,.3,-.25),'orange',(1.25,.7,1.05)); claws(s,side*.48,.16,-.43,'cream')
    tail(s,[(0,.7,.43),(0,.48,.6),(0,.25,.5)],'orange',.2); return normalize(s)


def gabumon():
    s=trimesh.Scene(); sph(s,.61,(0,.78,0),'blue',(1.04,1.2,.87)); sph(s,.49,(0,1.3,0),'blue',(1.04,.82,.85)); sph(s,.6,(0,1.72,-.02),'cream',(1,.98,.88)); sph(s,.4,(0,1.22,-.52),'cream',(1.08,.9,.58)); eye(s,-.2,1.8,-.64,'red',.125); eye(s,.2,1.8,-.64,'red',.125); sph(s,.11,(0,1.5,-.81),'black',(1.25,.7,.58)); sph(s,.23,(-.22,1.54,-.67),'cream',(1.3,.72,.62)); sph(s,.23,(.22,1.54,-.67),'cream',(1.3,.72,.62))
    for side in (-1,1): cone(s,.17,.05,.52,(side*.3,2.3,-.02),'yellow'); sph(s,.24,(side*.52,1.42,0),'blue',(.72,1.25,.62)); sph(s,.16,(side*.53,1.5,-.22),'pink',(.7,1.1,.4)); sph(s,.2,(side*.3,.28,-.25),'blue',(1.25,.68,1.1)); claws(s,side*.3,.15,-.42,'white')
    for x,y,z,sc in [(-.4,1.95,.2,.7),(-.2,2.08,.18,.75),(0,2.14,.18,.8),(.2,2.08,.18,.75),(.4,1.95,.2,.7)]: sph(s,.09,(x,y,z),'white',(sc,1.8,.35),36,24)
    tail(s,[(0,.72,.48),(0,.5,.7)],'blue',.18); return normalize(s)


def guilmon():
    s=trimesh.Scene(); sph(s,.66,(0,.8,0),'red',(1.03,1.25,.9)); sph(s,.63,(0,1.7,0),'red',(1.02,1,.9)); sph(s,.37,(0,1.2,-.52),'white',(1,.9,.55)); eye(s,-.21,1.77,-.62,'red',.13); eye(s,.21,1.77,-.62,'red',.13); sph(s,.11,(0,1.5,-.82),'black',(1.3,.65,.55))
    for side in (-1,1): cone(s,.2,.055,.5,(side*.29,2.35,0),'white'); cap(s,.18,.45,(side*.52,1.0,0),'red',(.8,1.3,.85)); sph(s,.2,(side*.28,.28,-.25),'red',(1.25,.68,1.1)); claws(s,side*.28,.16,-.43,'white')
    for p in [(-.12,1.33,-.82),(0,1.25,-.85),(.12,1.33,-.82)]: cone(s,.07,.015,.18,p,'black')
    tail(s,[(0,.7,.46),(0,.48,.68)],'red',.2); return normalize(s)


def renamon():
    s=trimesh.Scene(); sph(s,.54,(0,.82,0),'yellow',(1,.98,.75)); sph(s,.58,(0,1.74,0),'yellow',(1,.98,.86)); sph(s,.31,(0,1.2,-.5),'cream',(1,.9,.55)); eye(s,-.2,1.8,-.58,'red',.11); eye(s,.2,1.8,-.58,'red',.11)
    for side in (-1,1): cone(s,.16,.04,.76,(side*.3,2.43,0),'yellow'); cone(s,.09,.018,.25,(side*.3,2.88,0),'purple'); cap(s,.17,.5,(side*.48,1.05,0),'yellow',(.7,1.5,.72)); sph(s,.17,(side*.42,.38,-.2),'yellow',(1,.65,1.2)); claws(s,side*.42,.18,-.4,'white')
    tail(s,[(0,.68,.5),(0,.43,.74),(0,.18,.68)],'yellow',.18,'purple'); return normalize(s)


def biyomon():
    s=trimesh.Scene(); sph(s,.63,(0,.8,0),'pink',(1.04,1.23,.87)); sph(s,.58,(0,1.68,0),'pink',(1.03,.95,.88)); sph(s,.44,(0,1.62,-.5),'cream',(1.02,.78,.48)); sph(s,.4,(0,1.02,-.55),'cream',(1.05,1,.4)); eye(s,-.2,1.8,-.76,'blue2',.12); eye(s,.2,1.8,-.76,'blue2',.12); sph(s,.15,(0,1.57,-.86),'orange2',(1.4,.58,.7)); sph(s,.105,(0,1.47,-.84),'orange',(1.3,.5,.65))
    for x,yy,sc in [(0,2.25,1),(-.17,2.12,.78),(.17,2.12,.78),(-.3,2.02,.62),(.3,2.02,.62)]: sph(s,.17 if x==0 else .12,(x,yy,-.02),'red2',(sc,1.5,.6))
    for side in (-1,1):
        sph(s,.36,(side*.53,1.22,.02),'blue2',(.72,1.3,.64)); sph(s,.29,(side*.65,.99,.03),'blue',(.68,1.18,.58))
        for i,(y,rr) in enumerate([(1.32,.25),(1.1,.22),(.9,.19)]): sph(s,rr,(side*(.72+i*.045),y,.0),'blue2',(.68,1.15,.55))
        sph(s,.23,(side*.28,.22,-.24),'orange',(1.25,.65,1.3)); claws(s,side*.28,.16,-.4,'orange',3,.07)
    tail(s,[(0,.72,.48),(0,.52,.72)],'pink',.17,'blue2'); return normalize(s)


def patamon():
    s=trimesh.Scene(); sph(s,.61,(0,.82,0),'cream',(1,1.22,.86)); sph(s,.58,(0,1.68,0),'cream',(1,.96,.86)); sph(s,.31,(0,1.2,-.5),'cream',(1,.9,.55)); eye(s,-.2,1.79,-.57,'red',.11); eye(s,.2,1.79,-.57,'red',.11)
    for side in (-1,1): sph(s,.39,(side*.5,1.58,.02),'cream',(.55,1.4,.35)); sph(s,.28,(side*.66,1.1,.02),'cream',(.5,1.5,.3)); sph(s,.21,(side*.28,.28,-.25),'cream',(1.25,.68,1.15)); claws(s,side*.28,.16,-.41,'white')
    tail(s,[(0,.68,.46),(0,.45,.68)],'cream',.14,'yellow'); return normalize(s)


def gatomon():
    s=trimesh.Scene(); sph(s,.48,(0,.86,0),'cream',(1,.98,.73)); sph(s,.55,(0,1.72,0),'cream',(1,.96,.85)); sph(s,.28,(0,1.22,-.5),'cream',(1,.9,.55)); eye(s,-.2,1.8,-.57,'blue2',.105); eye(s,.2,1.8,-.57,'blue2',.105)
    for side in (-1,1): cone(s,.13,.035,.58,(side*.27,2.42,0),'yellow'); cap(s,.17,.48,(side*.45,1.17,0),'cream',(.72,1.5,.67)); sph(s,.17,(side*.31,.38,-.22),'cream',(1,.65,1.15)); claws(s,side*.31,.18,-.39,'white')
    sph(s,.32,(0,1.31,-.5),'yellow',(1.2,.2,.2)); tail(s,[(0,.7,.48),(.12,.47,.68),(.23,.24,.72)],'cream',.13,'yellow'); return normalize(s)


def tentomon():
    s=trimesh.Scene(); sph(s,.66,(0,.82,0),'red',(1,1.2,.9)); sph(s,.6,(0,1.67,0),'red',(1,.96,.88)); sph(s,.31,(0,1.2,-.5),'yellow',(1,.9,.55)); eye(s,-.21,1.79,-.57,'blue2',.11); eye(s,.21,1.79,-.57,'blue2',.11)
    for side in (-1,1): cone(s,.07,.018,.52,(side*.25,2.3,0),'yellow'); sph(s,.08,(side*.25,2.58,0),'yellow'); sph(s,.4,(side*.45,1.22,.08),'yellow',(.7,1.3,.34)); sph(s,.21,(side*.45,1.1,-.19),'black',(.75,1,.25)); sph(s,.2,(side*.28,.28,-.25),'yellow',(1.25,.68,1.1)); claws(s,side*.28,.16,-.41,'black')
    tail(s,[(0,.68,.46),(0,.45,.66)],'red',.16); return normalize(s)


def gomamon():
    s=trimesh.Scene(); sph(s,.61,(0,.82,0),'white',(1,1.26,.87)); sph(s,.58,(0,1.68,0),'white',(1,.96,.86)); sph(s,.31,(0,1.22,-.5),'white',(1,.9,.55)); eye(s,-.2,1.79,-.57,'blue2',.11); eye(s,.2,1.79,-.57,'blue2',.11)
    for side in (-1,1): sph(s,.11,(side*.5,1.76,-.18),'pink'); sph(s,.29,(side*.56,1.13,.02),'white',(.55,1.6,.35)); sph(s,.2,(side*.28,.25,-.25),'white',(1.25,.68,1.15)); claws(s,side*.28,.16,-.41,'pink')
    for x in [-.32,-.16,0,.16,.32]: sph(s,.08,(x,2.2,.05),'red',(.75,1.6,.55))
    tail(s,[(0,.7,.46),(0,.48,.67)],'white',.17); return normalize(s)


def palmon():
    s=trimesh.Scene(); sph(s,.63,(0,.82,0),'green',(1,1.22,.87)); sph(s,.57,(0,1.68,0),'green',(1,.96,.86)); sph(s,.31,(0,1.22,-.5),'cream',(1,.9,.55)); eye(s,-.2,1.79,-.57,'red',.11); eye(s,.2,1.79,-.57,'red',.11)
    for a in np.linspace(-1.1,1.1,5): sph(s,.19,(.34*math.sin(a),2.15,.05+.16*math.cos(a)),'green2',(.55,1.3,.3))
    for side in (-1,1): sph(s,.25,(side*.49,1.12,0),'green',(.75,1.45,.62)); sph(s,.22,(side*.28,.27,-.25),'green',(1.2,.68,1.1)); claws(s,side*.28,.16,-.41,'pink')
    for side in (-1,1): sph(s,.18,(side*.18,.7,.53),'green2',(.7,1,1.6))
    return normalize(s)


def veemon():
    s=trimesh.Scene(); sph(s,.62,(0,.82,0),'lightblue',(1,1.22,.87)); sph(s,.59,(0,1.68,0),'lightblue',(1,.98,.88)); sph(s,.32,(0,1.2,-.5),'cream',(1,.9,.55)); eye(s,-.205,1.79,-.57,'red',.115); eye(s,.205,1.79,-.57,'red',.115)
    for side in (-1,1): cone(s,.15,.035,.7,(side*.28,2.36,0),'lightblue'); cone(s,.1,.015,.3,(side*.28,2.76,0),'yellow'); cap(s,.22,.46,(side*.5,1.12,0),'lightblue',(.75,1.4,.72)); sph(s,.2,(side*.28,.28,-.25),'lightblue',(1.2,.68,1.1)); claws(s,side*.28,.16,-.41,'white')
    cone(s,.18,.01,.28,(0,1.3,-.7),'yellow'); tail(s,[(0,.7,.46),(0,.45,.7)],'lightblue',.18,'yellow'); return normalize(s)


def wormmon():
    s=trimesh.Scene(); sph(s,.62,(0,.82,0),'green',(1,1.3,.88)); sph(s,.58,(0,1.7,0),'green',(1,.98,.88)); sph(s,.31,(0,1.22,-.5),'yellow',(1,.9,.55)); eye(s,-.21,1.79,-.57,'red',.11); eye(s,.21,1.79,-.57,'red',.11)
    for side in (-1,1): cone(s,.09,.02,.62,(side*.25,2.34,0),'green2'); sph(s,.08,(side*.25,2.66,0),'yellow')
    for side in (-1,1):
        for i in range(3): sph(s,.19,(side*(.45+i*.03),1.22-i*.12,.04),'green2',(.65,1.1,.65))
        sph(s,.2,(side*.28,.27,-.25),'green',(1.2,.68,1.1)); claws(s,side*.28,.16,-.41,'yellow')
    tail(s,[(0,.68,.46),(0,.44,.67),(0,.22,.58)],'green',.18,'yellow'); return normalize(s)


BUILDERS = {
    'Agumon': agumon, 'Gabumon': gabumon, 'Guilmon': guilmon, 'Renamon': renamon,
    'Biyomon': biyomon, 'Patamon': patamon, 'Gatomon': gatomon, 'Tentomon': tentomon,
    'Gomamon': gomamon, 'Palmon': palmon, 'Veemon': veemon, 'Wormmon': wormmon,
}

for name, builder in BUILDERS.items():
    path = os.path.join(OUT, name + '.glb')
    builder().export(path, file_type='glb')
    print('Generated textured', name, os.path.getsize(path))
