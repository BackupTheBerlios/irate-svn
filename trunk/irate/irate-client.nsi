; ipaqdate.nsi
;
; This script is perhaps one of the simplest NSIs you can make. All of the
; optional settings are left to their default settings. The installer simply 
; prompts the user asking them where to install, and drops a copy of makensisw.exe
; there. 
;

; The name of the installer
Name "iRATE radio"

; The file to write
OutFile "irate-client-installer.exe"

; The default installation directory
InstallDir "$PROGRAMFILES\iRATE radio"

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM SOFTWARE\irate_radio "Install_Dir"

; The text to prompt the user to enter a directory
DirText "This will install iRATE radio on your computer."

; The stuff to install
Section "iRATE radio"
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  ; Put file there
  File "irate-client.jar"
  File "jl020.jar"  
  File "xercesImpl.jar"  
  File "xmlParserAPIs.jar"
;  File "madplay.exe"
  
  CreateDirectory "$SMPROGRAMS\iRATE radio"
  CreateShortCut "$SMPROGRAMS\iRate radio\iRATE radio.lnk" "$INSTDIR\irate-client.jar" ""

  WriteUninstaller "uninstall.exe"
SectionEnd ; end the section

UninstallText "This will uninstall Survey View. Select next to continue."

Section "Uninstall"
  DeleteRegKey HKLM SOFTWARE\linetrek_ipaqdate
  Delete "$SMPROGRAMS\iRATE radio\iRATE radio.lnk"
  RMDir "$SMPROGRAMS\iRATE radio"
  Delete "$INSTDIR\irate-client.jar"
  Delete "$INSTDIR\jl020.jar"
  Delete "$INSTDIR\xercesImpl.jar"
  Delete "$INSTDIR\xmlParserAPIs.jar"
;  Delete "$INSTDIR\madplay.exe"
  Delete "$INSTDIR\uninstall.exe"
  RMDir "$INSTDIR\download"
  RMDir "$INSTDIR"
SectionEnd ;

; eof
