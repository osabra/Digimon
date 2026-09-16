import os
import numpy as np
import trimesh
from trimesh.visual.material import PBRMaterial
OUT='app/src/main/assets/digimon'
os.makedirs(OUT,exist_ok=True)
COLORS={'blue':(.05,.22,.55),'cream':(.82,.78,.62),'white':(.95,.95,.92),'orange':(.95,.34,.05),'yellow':(.95,.68,.05),'red':(.75,.04,.04),'black':(.015,.015,.02),'pink':(.95,.25,.45),'green':(.08,.50,.12),'purple':(.45,.12,.65),'lightblue':(.18,.55,.85)}
M={k:PBRMaterial(name=k,baseColorFactor=[int(v*255) for v in rgb]+[255],roughnessFactor=.78,metallicFactor=0) for k,rgb in COLORS.items()}
def add(s,m,c): m.visual.material=M[c]; s.add_geometry(m)
def sph(s,r,p,c,scale=(1,1,1)):
 m=trimesh.creation.icosphere(subdivisions=2,radius=r); m.apply_scale(scale); m.apply_translation(p); add(s,m,c)
def cone(s,r,h,p,c):
 m=trimesh.creation.cone(radius=r,height=h,sections=20); m.apply_translation(p); add(s,m,c)
def build(name,body,accent,wings=False,horns=False,tail=False,belly='cream'):
 s=trimesh.Scene(); sph(s,.70,(0,1.62,0),body,(1,1,.95)); sph(s,.60,(0,.70,0),body,(1,1.25,.80)); sph(s,.33,(0,1.0,-.44),belly,(1,.82,.48)); sph(s,.22,(-.48,.75,0),body,(.85,1,.85)); sph(s,.22,(.48,.75,0),body,(.85,1,.85)); sph(s,.19,(-.25,.12,0),body,(1.2,1,.8)); sph(s,.19,(.25,.12,0),body,(1.2,1,.8))
 if horns: cone(s,.20,.62,(-.30,2.35,0),accent); cone(s,.20,.62,(.30,2.35,0),accent)
 else: cone(s,.14,.48,(-.28,2.28,0),accent); cone(s,.14,.48,(.28,2.28,0),accent)
 sph(s,.13,(-.21,1.78,-.55),'black'); sph(s,.13,(.21,1.78,-.55),'black'); sph(s,.055,(-.21,1.78,-.66),accent); sph(s,.055,(.21,1.78,-.66),accent)
 if wings: sph(s,.42,(-.60,1.30,.04),accent,(.55,1.35,.25)); sph(s,.42,(.60,1.30,.04),accent,(.55,1.35,.25))
 if tail: sph(s,.52,(0,.05,.30),body,(.85,.55,1.8))
 sph(s,.22,(0,1.22,-.50),accent,(1.15,.55,.35)); b=s.bounds; center=(b[0]+b[1])/2; s.apply_translation([-center[0],-b[0][1],-center[2]]); s.export(os.path.join(OUT,name+'.glb'),file_type='glb')
spec=[('Gabumon','blue','yellow',False,True,False),('Agumon','orange','red',False,True,False),('Patamon','cream','yellow',True,False,False),('Gatomon','cream','purple',False,True,False),('Tentomon','yellow','blue',True,True,False),('Gomamon','white','blue',True,False,False),('Palmon','green','pink',True,False,False),('Biyomon','yellow','red',True,False,False),('Veemon','lightblue','red',False,True,False),('Wormmon','green','yellow',False,True,True),('Guilmon','red','white',False,True,False),('Renamon','yellow','purple',False,True,False)]
for args in spec: build(*args)
