; -- Sample3.iss --
; Same as Sample1.iss, but creates some registry entries too.

; SEE THE DOCUMENTATION FOR DETAILS ON CREATING .ISS SCRIPT FILES!

[Setup]
AppName=iRATE radio
AppVerName=iRATE radio 0.2
DefaultDirName={pf}\iRATE radio
DefaultGroupName=iRATE radio
UninstallDisplayIcon={app}\irate-client.exe

[Files]
Source: "irate-client.exe"; DestDir: "{app}"
Source: "bin\madplay.exe"; DestDir: "{app}"
Source: "lib\swt-win32-2133.dll"; DestDir: "{app}"
;Source: "irate-client.jar"; DestDir: "{app}"

Source: "README"; DestDir: "{app}"
Source: "COPYING"; DestDir: "{app}"
;Flags: isreadme
;Source: "irate\client\help\missingplayer.txt"; DestDir: "{app}\irate\client\help";
;Source: "irate\client\help\about.txt"; DestDir: "{app}\irate\client\help";
;Source: "irate\client\help\connectionfailed.txt"; DestDir: "{app}\irate\client\help"
;Source: "irate\client\help\connectionrefused.txt"; DestDir: "{app}\irate\client\help"
;Source: "irate\client\help\connectiontimeout.txt"; DestDir: "{app}\irate\client\help"
;Source: "irate\client\help\empty.txt"; DestDir: "{app}\irate\client\help"
;Source: "irate\client\help\getstuffed.txt"; DestDir: "{app}\irate\client\help"
;Source: "irate\client\help\gettingstarted.txt"; DestDir: "{app}\irate\client\help"
;Source: "irate\client\help\hostnotfound.txt"; DestDir: "{app}\irate\client\help"
;Source: "irate\client\help\malformedurl.txt"; DestDir: "{app}\irate\client\help"
;Source: "irate\client\help\password.txt"; DestDir: "{app}\irate\client\help"
;Source: "irate\client\help\user.txt"; DestDir: "{app}\irate\client\help"

;Source: "lib\jl020.jar"; DestDir: "{app}\lib\jl020.jar"
;Source: "lib\nanoxml-lite-2.2.3.jar"; DestDir: "{app}\lib\nanoxml-lite-2.2.3.jar"


[Icons]
Name: "{group}\iRATE radio"; Filename: "{app}\irate-client.exe"; WorkingDir: "{app}"

; NOTE: Most apps do not need registry entries to be pre-created. If you
; don't know what the registry is or if you need to use it, then chances are
; you don't need a [Registry] section.

[Registry]
; Start "Software\My Company\My Program" keys under HKEY_CURRENT_USER
; and HKEY_LOCAL_MACHINE. The flags tell it to always delete the
; "My Program" keys upon uninstall, and delete the "My Company" keys
; if there is nothing left in them.
;Root: HKCU; Subkey: "Software\My Company"; Flags: uninsdeletekeyifempty
;Root: HKCU; Subkey: "Software\My Company\My Program"; Flags: uninsdeletekey
;Root: HKLM; Subkey: "Software\My Company"; Flags: uninsdeletekeyifempty
;Root: HKLM; Subkey: "Software\My Company\My Program"; Flags: uninsdeletekey
;Root: HKLM; Subkey: "Software\My Company\My Program\Settings"; ValueType: string; ValueName: "Path"; ValueData: "{app}"
