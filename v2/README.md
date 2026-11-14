# J5OS v2 — experimental native SystemUI theme
This is a resource-only prototype, not a complete iOS replica or flashable ROM.
## Build locally
Install Java, Python and Apktool from https://apktool.org/. Put the original SystemUI.apk and framework-res.apk in your current directory. From PowerShell run:
```powershell
powershell -ExecutionPolicy Bypass -File .\v2\Build.ps1 -Apktool C:\tools\apktool.jar
```
The script decodes the original APK, patches existing resource names, logs changes and attempts a build. It never writes to the phone. The result is unsigned and must not be flashed. Build and device compatibility remain untested.
## Remaining
Actual grouped iOS controls, vertical volume/brightness sliders, lockscreen behavior and Settings redesign require Java/Kotlin source changes. Matching platform signing credentials and a ROM-specific integration/test process are required. Preserve original APKs and a TWRP backup. No vendor binaries are included in this public repository.
