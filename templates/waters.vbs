
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
JavaCmd = FindJava("1.7")
IF JavaCmd = "" THEN
  JavaCmd = FindJava("1.8")
  IF JavaCmd = "" THEN
    WScript.echo("Could not locate Java 1.7 or 1.8." & VBNewLine & "Please make sure it is installed correctly.")
    Wscript.Quit(1)
  END IF
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
  On Error Resume Next
  RegistryPrefix = "HKEY_LOCAL_MACHINE\SOFTWARE"
  RegistrySuffix = "JavaSoft\Java Runtime Environment\" & version & "\JavaHome"
  RegistryKey = RegistryPrefix & "\" & RegistrySuffix
  JavaHome = SHO.RegRead(RegistryKey)
  IF err.Number = 0 THEN
    FindJava = JavaHome & "\bin\java"
    EXIT FUNCTION
  END IF
  err.Clear
  RegistryKey = RegistryPrefix & "\Wow6432Node\" & RegistrySuffix
  JavaHome = SHO.RegRead(RegistryKey)
  IF err.Number = 0 THEN
    FindJava = JavaHome & "\bin\java"
    EXIT FUNCTION
  END IF
  err.Clear
  On Error Goto 0
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