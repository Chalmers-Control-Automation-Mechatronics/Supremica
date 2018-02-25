
set FSO = WScript.CreateObject("Scripting.FileSystemObject")
set SHO = WScript.CreateObject("WScript.Shell")
set ENV = SHO.Environment("Process")

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
JavaCmd = FindJava("1.8")
IF JavaCmd = "" THEN
  WScript.echo("Could not locate Java 1.8." & VBNewLine & "Please make sure it is installed correctly.")
  Wscript.Quit(1)
END IF
Jar = ScriptDir & "\Supremica.jar"
WatersCmd = q & JavaCmd & q & " -classpath " & q & Jar & q & " org.supremica.gui.ide.IDE -p " & q & PropPath & q
limit = WScript.Arguments.Count - 1
FOR i = 0 to limit
  arg = WScript.Arguments.Item(i)
  WatersCmd = WatersCmd & " " & q & arg & q
NEXT
SHO.Run WatersCmd, 0, False


FUNCTION FindJava(version)
  Part1 = "HKEY_LOCAL_MACHINE\SOFTWARE"
  Part2 = "\JavaSoft"
  Part3 = "\" & version & "\JavaHome"
  RegistryKey = Part1 & Part2 & "\Java Runtime Environment" & Part3
  JavaCmd = FindJavaInRegistry(RegistryKey)
  IF JavaCmd <> "" THEN
    FindJava = JavaCmd
    EXIT FUNCTION
  END IF
  RegistryKey = Part1 & Part2 & "\Java Development Kit" & Part3
  JavaCmd = FindJavaInRegistry(RegistryKey)
  IF JavaCmd <> "" THEN
    FindJava = JavaCmd
    EXIT FUNCTION
  END IF
  RegistryKey = Part1 & "\Wow6432Node\" & Part2 & "\Java Runtime Environment" & Part3
  JavaCmd = FindJavaInRegistry(RegistryKey)
  IF JavaCmd <> "" THEN
    FindJava = JavaCmd
    EXIT FUNCTION
  END IF
  RegistryKey = Part1 & "\Wow6432Node\" & Part2 & "\Java Development Kit" & Part3
  JavaCmd = FindJavaInRegistry(RegistryKey)
  IF JavaCmd <> "" THEN
    FindJava = JavaCmd
    EXIT FUNCTION
  END IF
  IF GetJavaVersion("java") = version THEN
    FindJava = "java"
    EXIT FUNCTION
  END IF
  JavaHome = ENV("JAVA_HOME")
  JavaTest = JavaHome & "\bin\java"
  IF GetJavaVersion(JavaTest) = version THEN
    FindJava = JavaTest
  ELSE
    FindJava = ""
  END IF
END FUNCTION

FUNCTION FindJavaInRegistry(RegistryKey)
  On Error Resume Next
  JavaHome = SHO.RegRead(RegistryKey)
  IF err.Number = 0 THEN
    FindJavaInRegistry = JavaHome & "\bin\java"
  ELSE
    err.Clear
    FindJavaInRegistry = ""
  END IF
END FUNCTION

FUNCTION GetJavaVersion(cmd)
  On Error Resume Next  
  set Exec = SHO.Exec(cmd & " -version")
  IF err.Number <> 0 THEN
    err.Clear
    On Error Goto 0
    GetJavaVersion = "???"
    EXIT FUNCTION
  END IF
  On Error Goto 0
  set StdErr = Exec.StdErr
  line = StdErr.ReadLine
  set re = new RegExp
  with re
    .Pattern = "java version ""([0-9]+\.[0-9]+)[0-9\._]*"""
    .IgnoreCase = False
    .Global = False
  end with
  set match = re.Execute(line)
  IF match.Count = 1 THEN
    GetJavaVersion = match.Item(0).Submatches(0)
  ELSE
    GetJavaVersion = "???"
  END IF
END FUNCTION