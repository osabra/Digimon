import bpy, math, os
from mathutils import Vector

OUT = "app/src/main/assets/digimon/gabumon.glb"
os.makedirs(os.path.dirname(OUT), exist_ok=True)

# Clean scene
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete(use_global=False)

COL = {
    "yellow": (0.95,0.55,0.035,1), "cream": (0.82,0.79,0.66,1),
    "white": (0.94,0.93,0.86,1), "blue": (0.035,0.22,0.58,1),
    "red": (0.65,0.015,0.025,1), "black": (0.008,0.006,0.008,1),
    "pink": (0.9,0.035,0.22,1)
}
M = {}
for name,c in COL.items():
    m=bpy.data.materials.new("Gabumon_"+name)
    m.diffuse_color=c
    m.use_nodes=True
    bs=m.node_tree.nodes.get("Principled BSDF")
    bs.inputs["Base Color"].default_value=c
    bs.inputs["Roughness"].default_value=.82
    M[name]=m

def smooth(obj, sub=2):
    if obj.type=="MESH":
        for p in obj.data.polygons: p.use_smooth=True
        mod=obj.modifiers.new("OrganicSubdivision","SUBSURF")
        mod.levels=sub; mod.render_levels=sub
    return obj

def sphere(name, loc, scale, mat, seg=64, rings=40):
    bpy.ops.mesh.primitive_uv_sphere_add(segments=seg, ring_count=rings, location=loc)
    o=bpy.context.object; o.name=name; o.scale=scale
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
    o.data.materials.append(M[mat]); return smooth(o,2)

def cone(name, loc, r1, r2, depth, mat, rot=(0,0,0)):
    bpy.ops.mesh.primitive_cone_add(vertices=64, radius1=r1, radius2=r2, depth=depth, location=loc, rotation=rot)
    o=bpy.context.object; o.name=name; o.data.materials.append(M[mat]); return smooth(o,2)

def curve_tube(name, pts, radius, mat):
    cu=bpy.data.curves.new(name,"CURVE"); cu.dimensions="3D"; cu.resolution_u=10
    cu.bevel_depth=radius; cu.bevel_resolution=5
    sp=cu.splines.new("BEZIER"); sp.bezier_points.add(len(pts)-1)
    for b,p in zip(sp.bezier_points,pts):
        b.co=Vector(p); b.handle_left_type=b.handle_right_type="AUTO"
    o=bpy.data.objects.new(name,cu); bpy.context.collection.objects.link(o); o.data.materials.append(M[mat]); return o

# Yellow reptile body: compact, upright and rounded.
sphere("Body",(0,0.0,0),(0.72,0.58,0.92),"yellow")
sphere("Head",(0,0.02,1.05),(0.67,0.60,0.66),"yellow")

# Garurumon pelt hood: a rounded organic shell behind/around the head.
pelt=sphere("Pelt",(0,0.17,1.10),(0.82,0.70,0.78),"white")
# Hide the lower/front part with the face and body; add a characteristic shaggy hem.
for i in range(13):
    a=math.radians(-150 + i*25)
    x=.69*math.sin(a); z=1.04+.58*math.cos(a)
    sphere("FurTuft_%02d"%i,(x,0.16,z),(.15,.14,.22),"white",48,28)

# Blue pelt stripes following the hood curvature, not floating disks.
for side in (-1,1):
    for j in range(4):
        x=side*(0.54+0.025*j); z=1.55-j*.20
        curve_tube("PeltStripe",[(x,0.79,z+.12),(x*1.02,0.74,z),(x*1.03,0.67,z-.12)],.055,"blue")

# Long wolf-like muzzle and cheek pads.
sphere("MuzzleL",(-.24,-.54,1.04),(.34,.31,.27),"cream")
sphere("MuzzleR",(.24,-.54,1.04),(.34,.31,.27),"cream")
sphere("Nose",(0,-.82,1.02),(.15,.09,.12),"black")
sphere("Chin",(0,-.47,.82),(.38,.27,.30),"cream")

# Large anime eyes: black outer eye, red iris, white highlight.
for x in (-.22,.22):
    sphere("EyeOuter",(x,-.60,1.36),(.145,.09,.17),"black",48,32)
    sphere("Iris",(x,-.685,1.36),(.075,.035,.095),"red",40,28)
    sphere("Highlight",(x-.025,-.715,1.415),(.025,.012,.032),"white",24,18)

# One horn, two floppy ears and inner ear color.
cone("Horn",(0,.05,1.92),.18,.015,.55,"yellow",rot=(0,0,0))
for side in (-1,1):
    sphere("Ear", (side*.49,.05,1.56),(.24,.18,.40),"white")
    sphere("EarBlue",(side*.51,-.12,1.56),(.13,.05,.27),"blue")

# Characteristic blue-and-pink chest mark.
sphere("ChestMark",(0,-.57,.40),(.23,.045,.29),"blue")
sphere("ChestMarkPink",(0,-.615,.40),(.12,.025,.18),"pink")

# Arms, feet and claws.
for side in (-1,1):
    sphere("Arm",(side*.53,-.02,.48),(.22,.25,.47),"yellow")
    sphere("Cuff",(side*.53,-.24,.34),(.22,.18,.20),"white")
    for i in range(3):
        xx=side*(.53+(i-1)*.075)
        cone("Claw",(xx,-.40,.26),.055,.008,.19,"white",rot=(math.pi/2,0,0))
    sphere("Foot",(side*.29,-.17,-.82),(.30,.34,.18),"yellow")
    for i in range(3):
        xx=side*(.29+(i-1)*.075)
        cone("Toe",(xx,-.43,-.84),.05,.008,.17,"white",rot=(math.pi/2,0,0))

# Short segmented tail visible from the rear/side.
curve_tube("Tail",[(0,.42,-.20),(0,.68,-.25),(0,.82,-.48)],.16,"yellow")

# Normalize around the origin for the app viewer.
objs=[o for o in bpy.context.scene.objects if o.type in {"MESH","CURVE"}]
bpy.ops.object.select_all(action='DESELECT')
for o in objs: o.select_set(True)
bpy.context.view_layer.objects.active=objs[0]
bpy.ops.object.convert(target='MESH')
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.transform_apply(location=False,rotation=False,scale=False)

# Export a real Blender-generated glTF binary.
bpy.ops.export_scene.gltf(
    filepath=OUT, export_format='GLB',
    use_selection=True, export_apply=True,
    export_materials='EXPORT'
)
print("WROTE", OUT)
