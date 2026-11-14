param([switch]$Restore)
$ErrorActionPreference='Stop'
$adb=(Get-Command adb.exe -ErrorAction Stop).Source
function A([string[]]$a) { $r=& $adb @a; if($LASTEXITCODE -ne 0){throw "ADB failed: $a"}; return $r }
if ((A @('get-state')) -ne 'device') {throw 'Connect and authorize Android first.'}
if ((A @('shell','getprop','ro.product.model')) -ne 'SM-J500F') {throw 'Wrong device.'}
if ((A @('shell','getprop','ro.build.version.sdk')) -ne '30') {throw 'Android 11 required.'}
$backup=Join-Path $PSScriptRoot 'original-settings.json'
if($Restore){
 if(!(Test-Path $backup)){throw 'Backup not found.'}
 $b=Get-Content $backup -Raw | ConvertFrom-Json
 foreach($x in $b){if($x.value -eq 'null'){A @('shell','settings','delete',$x.ns,$x.key) | Out-Null}else{A @('shell','settings','put',$x.ns,$x.key,$x.value) | Out-Null}}
 Write-Host 'Original settings restored.'; exit
}
$changes=@(@{ns='secure';key='sysui_qs_tiles';value='wifi,bt,cell,airplane,flashlight,rotation,dnd,battery'},@{ns='secure';key='sysui_qqs_count';value='4'})
if(!(Test-Path $backup)){
 $b=@(foreach($x in $changes){@{ns=$x.ns;key=$x.key;value=[string](A @('shell','settings','get',$x.ns,$x.key))}})
 $b | ConvertTo-Json | Set-Content -Encoding UTF8 $backup
}
foreach($x in $changes){A @('shell','settings','put',$x.ns,$x.key,$x.value) | Out-Null; $v=A @('shell','settings','get',$x.ns,$x.key); if($v -ne $x.value){throw "Setting not saved: $($x.key)"}}
Write-Host 'Settings saved. Expand Quick Settings to inspect. Visual support depends on ROM.'
