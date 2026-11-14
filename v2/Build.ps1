param([Parameter(Mandatory=$true)][string]$Apktool,[string]$SystemUI="SystemUI.apk",[string]$Framework="framework-res.apk")
$ErrorActionPreference="Stop"
$Apktool=(Resolve-Path $Apktool).Path
$SystemUI=(Resolve-Path $SystemUI).Path
$Framework=(Resolve-Path $Framework).Path
$work=Join-Path $PSScriptRoot ("build-"+[guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory $work | Out-Null
$frames=Join-Path $work "framework"
New-Item -ItemType Directory $frames | Out-Null
function Run([string]$exe,[string[]]$a){ & $exe @a; if($LASTEXITCODE -ne 0){throw "$exe failed ($LASTEXITCODE)"} }
Run java @("-jar",$Apktool,"if","-p",$frames,$Framework)
$decoded=Join-Path $work "SystemUI"
Run java @("-jar",$Apktool,"d","-s","-p",$frames,"-o",$decoded,$SystemUI)
Run python @((Join-Path $PSScriptRoot "Theme.py"),$decoded)
$out=Join-Path $work "SystemUI-UNSIGNED-DO-NOT-FLASH.apk"
Run java @("-jar",$Apktool,"b","-p",$frames,"-o",$out,$decoded)
Write-Host "Unsigned development build: $out"
Write-Warning "Do not install. Original platform signing key and device testing required."
