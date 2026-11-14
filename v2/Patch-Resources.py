"""Offline resource experiment. Produces sources, not a flashable APK."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
root=Path(sys.argv[1])
res=root/'res'
if not (res/'layout/qs_panel.xml').is_file():
    raise SystemExit('Expected decoded Android 11 SystemUI resources missing')
changes=[]
# Adjust existing dimensions only, never invent resource names.
targets={'qs_tile_corner_radius':'24dp','qs_tile_height':'84dp','qs_tile_margin_horizontal':'8dp','qs_tile_margin_vertical':'8dp','qs_panel_padding':'12dp','qs_tile_text_size':'12sp'}
for p in sorted(res.glob('values*/dimens.xml')):
    tree=ET.parse(p)
    hit=False
    for e in tree.getroot():
        n=e.get('name')
        if n in targets and e.text and e.text.strip().endswith(('dp','sp','dip')):
            old=e.text
            e.text=targets[n]
            changes.append(f'{p.relative_to(root)}: {n}: {old} -> {e.text}')
            hit=True
    if hit: tree.write(p,encoding='utf-8',xml_declaration=True)
print('\n'.join(changes) or 'No matching dimensions; manual port required.')
if not changes: raise SystemExit(2)
