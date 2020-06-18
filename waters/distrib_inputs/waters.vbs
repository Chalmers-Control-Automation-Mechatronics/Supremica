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
  options = Array("", "\Wow6432Node")
  FOR EACH opt IN options
    javaCmd = FindJavaDefault(opt)
    IF javaCmd <> "" THEN
      FindJava = javaCmd
      EXIT FUNCTION
    END IF
    javaCmd = FindJavaVersion(opt, 8)
    IF javaCmd <> "" THEN
      FindJava = javaCmd
      EXIT FUNCTION
    END IF
    javaCmd = FindJavaVersion(opt, 32767)
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

FUNCTION FindJavaDefault(wow6432)
  prefix = "HKEY_LOCAL_MACHINE\SOFTWARE" & wow6432 & "\JavaSoft"
  options = Array("\JRE", "\JDK", "\Java Runtime Environment", "\Java Development Kit")
  FOR EACH opt IN options
    JavaCmd = FindJavaDefault1(prefix & opt)
    IF JavaCmd <> "" THEN
      FindJavaDefault = JavaCmd
      EXIT FUNCTION
    END IF
  NEXT
  FindJavaDefault = ""
END FUNCTION

FUNCTION FindJavaDefault1(group)
  key = group & "\CurrentVersion"
  ON ERROR RESUME NEXT
  version = SHO.RegRead(key)
  IF err.Number <> 0 THEN
    err.Clear
    ON ERROR GOTO 0
    FindJavaDefault1 = ""
    EXIT FUNCTION
  END IF
  ON ERROR GOTO 0
  v = GetMajorJavaVersion(version)
  IF v < 8 THEN
    FindJavaDefault1 = ""
    EXIT FUNCTION
  END IF
  key = group & "\" & version & "\JavaHome"
  ON ERROR RESUME NEXT
  javaHome = SHO.RegRead(key)
  IF err.Number <> 0 THEN
    err.Clear
    ON ERROR GOTO 0
    FindJavaDefault1 = ""
    EXIT FUNCTION
  END IF
  ON ERROR GOTO 0
  q = """"
  FindJavaDefault1 = q & javaHome & "\bin\java" & q
END FUNCTION

FUNCTION FindJavaVersion(wow6432, maxVersion)
  prefix = "HKEY_LOCAL_MACHINE\SOFTWARE" & wow6432 & "\JavaSoft"
  options = Array("\JRE", "\JDK", "\Java Runtime Environment", "\Java Development Kit")
  const HKEY_LOCAL_MACHINE = &H80000002
  FOR EACH opt IN options
    REG.EnumKey HKEY_LOCAL_MACHINE, prefix & opt, subKeys
    IF NOT IsNull(subKeys) THEN
      FOR EACH version IN subKeys
        v = GetMajorJavaVersion(version)
	IF v > 0 AND v <= maxVersion THEN
          key = group & "\" & version & "\JavaHome"
          ON ERROR RESUME NEXT
          javaHome = SHO.RegRead(key)
          IF err.Number = 0 THEN
            ON ERROR GOTO 0
            q = """"
            FindJavaVersion = q & javaHome & "\bin\java" & q
            EXIT FUNCTION
          ELSE
            err.Clear
            ON ERROR GOTO 0
          END IF
        END IF
      NEXT
    END IF
  NEXT
  FindJavaVersion = ""
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
