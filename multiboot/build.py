from PIL import Image, ImageOps, ImageSequence
from urllib.request import urlopen, Request
from io import BytesIO
from zipfile import ZipFile, ZIP_STORED, ZIP_DEFLATED
from pathlib import Path
names=['apple-electrocution','oneplus','pixel','windows','ea','xbox','nsa']
base='https://raw.githubusercontent.com/John0n1/SMbootFX/master/examples/'
out=Path('dist'); out.mkdir(exist_ok=True)
fps=20
desc=['720 1280 20']
last=None
with ZipFile(out/'bootanimation.zip','w',ZIP_STORED) as z:
 for i,name in enumerate(names):
  url=base+name+'-bootfx.'+('png' if name=='nsa' else 'gif')
  with urlopen(Request(url,headers={'User-Agent':'J5OS-build'}),timeout=60) as r: im=Image.open(BytesIO(r.read()))
  part=f'part{i:02d}'; desc.append(f'c 1 0 {part}'); count=0; elapsed=0
  for frame in ImageSequence.Iterator(im):
   duration=max(20,frame.info.get('duration',100) or 100) if name!='nsa' else 2000
   elapsed+=duration
   target=round(elapsed*fps/1000)
   if target<=count: continue
   fg=ImageOps.contain(frame.convert('RGB'),(720,1280),Image.Resampling.LANCZOS)
   canvas=Image.new('RGB',(720,1280)); canvas.paste(fg,((720-fg.width)//2,(1280-fg.height)//2))
   buf=BytesIO(); canvas.quantize(colors=128).save(buf,format='PNG',optimize=True); last=buf.getvalue()
   while count<target:
    z.writestr(f'{part}/{count:06d}.png',last); count+=1
  if not count: raise RuntimeError(name+' empty')
  print(name,count,'frames',flush=True)
 desc.append('p 0 0 hold'); z.writestr('hold/000000.png',last); z.writestr('desc.txt','\n'.join(desc)+'\n')
with ZipFile(out/'bootanimation.zip') as z:
 assert z.testzip() is None
with ZipFile(out/'J5OS-MultiBoot-Magisk.zip','w',ZIP_DEFLATED) as z:
 z.write(out/'bootanimation.zip','system/media/bootanimation.zip')
 z.writestr('module.prop','id=j5os_multiboot\nname=J5OS MultiBoot\nversion=1.0\nversionCode=1\nauthor=J5OS\ndescription=Seven sequential boot animations for AOSP Android 11\n')
 z.writestr('customize.sh','SKIPMOUNT=false\nPROPFILE=false\nPOSTFSDATA=false\nLATESTARTSERVICE=false\n[ "$API" = 30 ] || abort "Android 11 required"\n[ "$(getprop ro.product.model)" = SM-J500F ] || abort "SM-J500F required"\nset_perm_recursive "$MODPATH" 0 0 0755 0644\n')
with ZipFile(out/'J5OS-MultiBoot-Magisk.zip') as z: assert z.testzip() is None
