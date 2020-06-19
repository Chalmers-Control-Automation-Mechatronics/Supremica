strComputer = "."

set FSO = WScript.CreateObject("Scripting.FileSystemObject")
set SHO = WScript.CreateObject("WScript.Shell")
set ENV = SHO.Environment("Process")
set REG = GetObject("winmgmts:{impersonationLevel=impersonate}!\\" &_ 
                    strComputer & "\root\default:StdRegProv")

set ScriptFile = FSO.GetFile(WScript.ScriptFullName)
ScriptDir = FSO.GetParentFolderName(ScriptFile)

q = """"
javaCmd = FindJava()
IF javaCmd = "" THEN
  WScript.echo("Could not locate Java 8 or higher." & VBNewLine & "Please make sure it is installed correctly.")
  Wscript.Quit(1)
END IF
Jar = ScriptDir & "\Supremica.jar"

Home = ENV("HOMEDRIVE") & ENV("HOMEPATH")
PropName = "waters.properties"
PropPath = HOME & "\" & PropName
IF NOT FSO.FileExists(PropPath) THEN
  SourcePath = ScriptDir &  "\" & PropName
  FSO.CopyFile SourcePath, PropPath 	
END IF

WatersCmd = javaCmd & " -classpath " & q & Jar & q & " org.supremica.gui.ide.IDE -p " & q & PropPath & q
limit = WScript.Arguments.Count - 1
FOR i = 0 to limit
  arg = WScript.Arguments.Item(i)
  WatersCmd = WatersCmd & " " & q & arg & q
NEXT

SHO.Run WatersCmd, 0, False


FUNCTION FindJava()
  javaCmd = CheckJavaCommand("java")
  IF javaCmd <> "" THEN
    FindJava = javaCmd
    EXIT FUNCTION
  END IF
  javaHome = ENV("JAVA_HOME")
  IF javaHome <> "" THEN
    javaCmd = CheckJavaCommand(javaHome & "\bin\java")
    IF javaCmd <> "" THEN
      FindJava = javaCmd
      EXIT FUNCTION
    END IF
  END IF
  javaCmd = FindJavaInRegistry(8)
  IF javaCmd <> "" THEN
    FindJava = javaCmd
    EXIT FUNCTION
  END IF
  FindJava = FindJavaInRegistry(32767)
END FUNCTION

FUNCTION FindJavaInRegistry(maxVersion)
  vendors = Array("JavaSoft", "AdoptOpenJDK")
  wows = Array("", "\Wow6432Node")
  FOR EACH vendor IN vendors
    FOR EACH wow IN wows
      found = FindSpecificJavaInRegistry(wow, vendor, maxVersion)
      IF found <> "" THEN
        FindJavaInRegistry = found
        EXIT FUNCTION
      END IF
    NEXT
  NEXT
  FindJavaInRegistry = ""
END FUNCTION

FUNCTION FindSpecificJavaInRegistry(wow, vendor, maxVersion)
  hklm = "HKEY_LOCAL_MACHINE\"
  root = "SOFTWARE"
  group = root & wow & "\" & vendor
  const HKEY_LOCAL_MACHINE = &H80000002
  REG.EnumKey HKEY_LOCAL_MACHINE, group, subKeys
  IF NOT IsNull(subKeys) THEN
    FOR EACH subKey IN subKeys
      subGroup = group & "\" & subKey
      REG.EnumKey HKEY_LOCAL_MACHINE, subGroup, versions
      IF NOT IsNull(versions) THEN
        FOR EACH version IN versions
          v = GetMajorJavaVersion(version)
          IF v > 0 AND v <= maxVersion THEN
            subSubGroup = subGroup & "\" & version
            IF vendor = "JavaSoft" THEN
              key = hklm & subSubGroup & "\JavaHome"
              javaHome = ReadRegistryKey(key)
            ELSE
              REG.EnumKey HKEY_LOCAL_MACHINE, subSubGroup, subVersions
              IF NOT IsNull(subVersions) THEN
                FOR EACH subVersion IN subVersions
                  key = hklm & subSubGroup & "\" & subVersion & "\MSI\Path"
                  javaHome = ReadRegistryKey(key)
                  IF javaHome <> "" THEN
                    EXIT FOR
                  END IF
                NEXT
              END IF
            END IF
            IF javaHome <> "" THEN
              q = """"
              FindSpecificJavaInRegistry = q & javaHome & "\bin\java" & q
              EXIT FUNCTION
            END IF  
          END IF
        NEXT
      END IF
    NEXT
  END IF
  FindSpecificJavaInRegistry = ""
END FUNCTION

FUNCTION ReadRegistryKey(key)
  ON ERROR RESUME NEXT
  value = SHO.RegRead(key)
  IF err.Number <> 0 THEN
    err.Clear
    value = ""
  END IF
  ON ERROR GOTO 0
  ReadRegistryKey = value
END FUNCTION

FUNCTION CheckJavaCommand(cmd)
  On Error Resume Next  
  javaCmd = q & cmd & q
  set Exec = SHO.Exec(javaCmd & " -version")
  IF err.Number <> 0 THEN
    err.Clear
    On Error Goto 0
    CheckJavaCommand = ""
    EXIT FUNCTION
  END IF
  On Error Goto 0
  set StdErr = Exec.StdErr
  line = StdErr.ReadLine
  set re = new RegExp
  with re
    .Pattern = "version ""([0-9\._]+)"""
    .IgnoreCase = False
    .Global = False
  end with
  set match = re.Execute(line)
  IF match.Count <> 1 THEN
    CheckJavaCommand = ""
    EXIT FUNCTION
  END IF
  version = match.Item(0).Submatches(0)
  v = GetMajorJavaVersion(version)
  IF v >= 8 THEN
    CheckJavaCommand = javaCmd
  ELSE
    CheckJavaCommand = ""
  END IF
END FUNCTION

FUNCTION GetMajorJavaVersion(version)
  IF version = "1.8" OR InStr(version, "1.8.") = 1 THEN
    GetMajorJavaVersion = 8
  ELSEIF InStr(version, "1.") = 1 THEN
    GetMajorJavaVersion = 0
  ELSE   
    dotPos = InStr(version, ".")
    IF dotPos = 0 THEN
      GetMajorJavaVersion = 0
      EXIT FUNCTION
    END IF
    major = Left(version, dotPos)
    ON ERROR RESUME NEXT
    GetMajorJavaVersion = CInt(major)
    IF err.Number <> 0 THEN
      err.Clear
      ON ERROR GOTO 0
      GetMajorJavaVersion = 0
      EXIT FUNCTION
    END IF
    ON ERROR GOTO 0
  END IF
END FUNCTION
