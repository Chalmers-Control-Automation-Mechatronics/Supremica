strComputer = "."

set FSO = WScript.CreateObject("Scripting.FileSystemObject")
set SHO = WScript.CreateObject("WScript.Shell")
set ENV = SHO.Environment("Process")
set REG = GetObject("winmgmts:{impersonationLevel=impersonate}!\\" &_ 
                    strComputer & "\root\default:StdRegProv")

Home = ENV("HOMEDRIVE") & ENV("HOMEPATH")

set ScriptFile = FSO.GetFile(WScript.ScriptFullName)
ScriptDir = FSO.GetParentFolderName(ScriptFile)

PropName = "waters.properties"
PropPath = HOME & "\" & PropName
IF NOT FSO.FileExists(PropPath) THEN
	SourcePath = ScriptDir &  "\" & PropName
	FSO.CopyFile SourcePath, PropPath 	
END IF

q = """"
javaCmd = FindJava()
IF javaCmd = "" THEN
  WScript.echo("Could not locate Java 8, 9, or 10." & VBNewLine & "Please make sure it is installed correctly.")
  Wscript.Quit(1)
END IF
Jar = ScriptDir & "\Supremica.jar"
WatersCmd = javaCmd & " -classpath " & q & Jar & q & " org.supremica.gui.ide.IDE -p " & q & PropPath & q
limit = WScript.Arguments.Count - 1
FOR i = 0 to limit
  arg = WScript.Arguments.Item(i)
  WatersCmd = WatersCmd & " " & q & arg & q
NEXT
SHO.Run WatersCmd, 0, False


FUNCTION FindJava()
  options = Array("", "\Wow6432Node")
  FOR EACH opt IN options
    javaCmd = FindJava8(opt)
    IF javaCmd <> "" THEN
      FindJava = javaCmd
      EXIT FUNCTION
    END IF
    javaCmd = FindJava10(opt)
    IF javaCmd <> "" THEN
      FindJava = javaCmd
      EXIT FUNCTION
    END IF
  NEXT
  javaHome = ENV("JAVA_HOME")
  IF javaHome <> "" THEN
    javaCmd = CheckJavaCommand(javaHome & "\bin\java")
    IF javaCmd <> "" THEN
      FindJava = javaCmd
      EXIT FUNCTION
    END IF
  END IF
  FindJava = CheckJavaCommand("java")
END FUNCTION

FUNCTION FindJava8(wow6432)
  prefix = "HKEY_LOCAL_MACHINE\SOFTWARE" & wow6432 & "\JavaSoft"
  JavaCmd = FindJavaInRegistryGroup(prefix & "\Java Runtime Environment")
  IF JavaCmd <> "" THEN
    FindJava8 = JavaCmd
    EXIT FUNCTION
  END IF
  FindJava8 = FindJavaInRegistryGroup(prefix & "\Java Development Kit")
END FUNCTION

FUNCTION FindJava10(wow6432)
  prefix = "HKEY_LOCAL_MACHINE\SOFTWARE" & wow6432 & "\JavaSoft"
  options = Array("\JRE", "\JDK")
  FOR EACH opt IN options
    JavaCmd = FindJavaInRegistryGroup(prefix & opt)
    IF JavaCmd <> "" THEN
      FindJava10 = JavaCmd
      EXIT FUNCTION
    END IF
  NEXT
  const HKEY_LOCAL_MACHINE = &H80000002
  FOR EACH opt IN options
    REG.EnumKey HKEY_LOCAL_MACHINE, prefix & opt, subKeys
    IF NOT IsNull(subKeys) THEN
      FOR EACH version IN subKeys
        IF InStr(version, "9.") = 1 OR InStr(version, "10.") = 1 THEN
          key = group & "\" & version & "\JavaHome"
          ON ERROR RESUME NEXT
          javaHome = SHO.RegRead(key)
          IF err.Number = 0 THEN
            ON ERROR GOTO 0
            q = """"
            FindJava10 = q & javaHome & "\bin\java" & q & " --add-modules java.xml.bind"
            EXIT FUNCTION
          ELSE
            err.Clear
            ON ERROR GOTO 0
          END IF
        END IF
      NEXT
    END IF
  NEXT
  FindJava10 = ""
END FUNCTION

FUNCTION FindJavaInRegistryGroup(group)
  key = group & "\CurrentVersion"
  ON ERROR RESUME NEXT
  version = SHO.RegRead(key)
  IF err.Number <> 0 THEN
    err.Clear
    ON ERROR GOTO 0
    FindJavaInRegistryGroup = ""
    EXIT FUNCTION
  END IF
  ON ERROR GOTO 0
  IF version = "1.8" OR InStr(version, "1.8.") = 1 THEN
    v = 8
  ELSEIF InStr(version, "9.") = 1 THEN
    v = 9
  ELSEIF InStr(version, "10.") = 1 THEN
    v = 10
  ELSE
    FindJavaInRegistryGroup = ""
    EXIT FUNCTION
  END IF
  key = group & "\" & version & "\JavaHome"
  ON ERROR RESUME NEXT
  javaHome = SHO.RegRead(key)
  IF err.Number <> 0 THEN
    err.Clear
    ON ERROR GOTO 0
    FindJavaInRegistryGroup = ""
    EXIT FUNCTION
  END IF
  ON ERROR GOTO 0
  q = """"
  javaCmd = q & javaHome & "\bin\java" & q
  IF v > 8 THEN
    javaCmd = javaCmd & " --add-modules java.xml.bind"
  END IF
  FindJavaInRegistryGroup = javaCmd
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
    .Pattern = "java version ""([0-9\._]+)"""
    .IgnoreCase = False
    .Global = False
  end with
  set match = re.Execute(line)
  IF match.Count <> 1 THEN
    CheckJavaCommand = ""
    EXIT FUNCTION
  END IF
  version = match.Item(0).Submatches(0)
  IF version = "1.8" OR InStr(version, "1.8.") = 1 THEN
    CheckJavaCommand = javaCmd
  ELSEIF InStr(version, "9.") = 1 OR InStr(version, "10.") = 1 THEN
    CheckJavaCommand = javaCmd & " --add-modules java.xml.bind"
  ELSE
    CheckJavaCommand = ""
  END IF
END FUNCTION
