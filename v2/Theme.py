"""Experimental resource-only iOS-inspired skin for decoded Android 11 SystemUI."""
from pathlib import Path
import sys, xml.etree.ElementTree as ET
root=Path(sys.argv[1]); res=root/'res'
if not (res/'layout/qs_panel.xml').exists(): sys.exit('Wrong SystemUI source')
A='{http://schemas.android.com/apk/res/android}'
ET.register_namespace('android', A[1:-1])
changes=[]
dims={'qs_tile_corner_radius':'24dp','qs_tile_height':'88dp','qs_tile_margin_horizontal':'8dp','qs_tile_margin_vertical':'8dp','qs_panel_padding':'12dp','qs_tile_text_size':'12sp','notification_corner_radius':'20dp','notification_material_rounded_rect_radius':'20dp','keyguard_clock_text_size':'72sp'}
colors={'qs_background_dark':'#FF141419','qs_background_light':'#FFF2F2F7','qs_tile_icon_on':'#FFFFFFFF','qs_tile_icon_off':'#FFE5E5EA','qs_tile_background_active':'#FF007AFF','qs_tile_background_inactive':'#FF34343A'}
for p in sorted(res.glob('values*/*.xml')):
    if p.name not in ('dimens.xml','colors.xml'): continue
    tree=ET.parse(p); hit=False
    for e in tree.getroot():
        name=e.get('name'); val=(e.text or '').strip()
        new=dims.get(name) if e.tag=='dimen' and val.endswith(('dp','sp','dip')) else colors.get(name) if e.tag=='color' and val.startswith('#') else None
        if new and val!=new:
            e.text=new; hit=True; changes.append(f'{p.relative_to(root)} {name}: {val} -> {new}')
    if hit: tree.write(p,encoding='utf-8',xml_declaration=True)
def vector(name,paths):
    p=res/'drawable'/(name+'.xml')
    if not p.exists(): return
    old=ET.parse(p).getroot()
    if old.tag!='vector': return
    v=ET.Element('vector',{A+'width':'24dp',A+'height':'24dp',A+'viewportWidth':'24',A+'viewportHeight':'24'})
    for d,alpha in paths:
        ET.SubElement(v,'path',{A+'fillColor':'#FFFFFFFF',A+'fillAlpha':str(alpha),A+'pathData':d})
    ET.ElementTree(v).write(p,encoding='utf-8',xml_declaration=True); changes.append(str(p.relative_to(root)))
arcs=['M2,7 Q12,-1 22,7 L20.2,9.2 Q12,3 3.8,9.2 Z','M5.5,11 Q12,5.5 18.5,11 L16.5,13.2 Q12,9.5 7.5,13.2 Z','M9,15 Q12,12.5 15,15 L12,18.5 Z']
for level in range(5):
    vector('ic_qs_wifi_'+str(level),[(d,1 if level>=3-i else .22) for i,d in enumerate(arcs)])
vector('ic_qs_bluetooth_on',[('M11,2 L18,8 L13,12 L18,16 L11,22 L11,14 L6,19 L4.5,17.5 L10,12 L4.5,6.5 L6,5 L11,10 Z M13,6 L13,9.5 L15,8 Z M13,14.5 L13,18 L15,16 Z',1)])
for name in ('notification_material_bg','notification_material_bg_dim','qs_background_primary','qs_media_background'):
    p=res/'drawable'/(name+'.xml')
    if not p.exists(): continue
    tree=ET.parse(p); hit=False
    for e in tree.iter('corners'):
        if A+'radius' in e.attrib:
            e.set(A+'radius','22dp'); hit=True
    if hit: tree.write(p,encoding='utf-8',xml_declaration=True); changes.append(str(p.relative_to(root)))
print('\n'.join(changes))
if not changes: sys.exit('No matching resources; stopped')
