/********************** AnalyzerRunCript.java *****************/
package org.supremica.gui.ide.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;
import org.supremica.properties.Config;

import javax.tools.*;
import java.io.*;
import java.util.stream.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/* This class allows to runtime load, compile, and run java code
 * https://blog.frankel.ch/compilation-java-code-on-the-fly/
 */
class JavaExecutor
{
  static String fileName = null;

  static String getFilenameWithoutExt(String sourcePath) throws IOException
  {
    Path path = Paths.get(sourcePath);
    String filename = path.getFileName().toString(); // includes .ext

    return filename.substring(0, filename.lastIndexOf(".")); // strip ext
  }

  static String readCode(String sourcePath) throws FileNotFoundException
  {
      InputStream stream = new FileInputStream(sourcePath);
      String separator = System.getProperty("line.separator");
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      return reader.lines().collect(Collectors.joining(separator));
  }

  static Path saveSource(String source) throws IOException
  {
      String tmpProperty = Config.FILE_TEMP_PATH.getValue().getPath(); // "R:/"; // System.getProperty("java.io.tmpdir");
      Path sourcePath = Paths.get(tmpProperty, JavaExecutor.fileName + ".java");
      // System.out.println("saveSource: " + sourcePath);
      Files.write(sourcePath, source.getBytes(StandardCharsets.UTF_8));
      return sourcePath;
  }

  static Path compileSource(Path javaFile)
  {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      compiler.run(null, null, null, javaFile.toFile().getAbsolutePath());
      return javaFile.getParent().resolve(JavaExecutor.fileName + ".class");
  }

    static void runClass(Path javaClass, IDE ide)
        throws MalformedURLException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
  {
      URL classUrl = javaClass.getParent().toFile().toURI().toURL();
      // System.out.println("classUrl: " + classUrl);
      URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classUrl});
      Class<?> clazz = Class.forName(JavaExecutor.fileName, true, classLoader);
      // clazz.newInstance(); // Can only access zero argument constructor (not main()!)
      Constructor<?> constr = clazz.getDeclaredConstructor(org.supremica.gui.ide.IDE.class);
      constr.newInstance(ide);

  }

    public static void exeJava(String sourcePath, final IDE ide) throws Exception
    {
        JavaExecutor.fileName = getFilenameWithoutExt(sourcePath);

        // System.out.println(JavaExecutor.fileName);

        final String source = JavaExecutor.readCode(sourcePath);
        final Path javaFile = JavaExecutor.saveSource(source);
        final Path classFile = JavaExecutor.compileSource(javaFile);

        // System.out.println("Exe java: "+ classFile);

        JavaExecutor.runClass(classFile, ide);
    }
}
/************************************************************/
// This class allows to run both java and Lua files as script
public class AnalyzerRunScript
{
    private static java.io.File initDir = Config.FILE_SCRIPT_PATH.getValue(); // static, to save in-between calls

	//** This is an ugly hack, but right now I cannot think of
	//** another way to get access to the ide from outside
	//** setIDE here is called from RunScriptAction when it is initialized
    private static IDE ide;
    static void setIDE(final IDE ide)
    {
		AnalyzerRunScript.ide = ide;
	}
	public static IDE getIDE()
	{
		return AnalyzerRunScript.ide;
	}
	//** end ugly hack

    // https://www.baeldung.com/java-file-extension
    static java.util.Optional<String> getExtension(String filename)
    {
        return java.util.Optional.ofNullable(filename)
          .filter(f -> f.contains("."))
          .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public static void chooseAndRunScript(final IDE ide) throws Exception
    {
        if(AnalyzerRunScript.initDir == null)
        {
          AnalyzerRunScript.initDir = javax.swing.filechooser.FileSystemView.getFileSystemView().getHomeDirectory();
        }

        javax.swing.JFileChooser jfc =
            new javax.swing.JFileChooser(AnalyzerRunScript.initDir);

        int returnValue = jfc.showOpenDialog(null);

        if (returnValue != javax.swing.JFileChooser.APPROVE_OPTION)
            return;

        AnalyzerRunScript.initDir = jfc.getCurrentDirectory();
        final java.io.File selectedFile = jfc.getSelectedFile();
        final String script = selectedFile.getPath();

        // if extension is ".java" then compile and run it as java
        // else if extension is ".lua", then compile and run it as lua
        final String ext = getExtension(script).get();
        if (ext.equals("java"))
        {
            runJavaScript(script, ide);
            return;
        }

        if(!ext.equals("lua")) return;

        runLuaScript(script, ide);

    }

    public static void runJavaScript(final String script, final IDE ide) throws Exception
    {
      logger.info("Java script: " + script);
      JavaExecutor.exeJava(script, ide);
    }

    public static void runLuaScript(final String script, final IDE ide) throws Exception
    {
      logger.info("Lua script: " + script);
      // create an environment to run in
      final org.luaj.vm2.Globals globals = org.luaj.vm2.lib.jse.JsePlatform.standardGlobals();
      // Use the convenience function on Globals to load a chunk.
      final org.luaj.vm2.LuaValue chunk = globals.loadfile(script);
      // Use any of the "call()" or "invoke()" functions directly on the chunk.
      final org.luaj.vm2.LuaValue luaIDE = org.luaj.vm2.lib.jse.CoerceJavaToLua.coerce(ide);
      final org.luaj.vm2.LuaValue luaScript = org.luaj.vm2.lib.jse.CoerceJavaToLua.coerce(script);
      chunk.call(luaScript, luaIDE);
    }
    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(AnalyzerRunScript.class);

}