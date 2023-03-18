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
  
  private static final String scriptPackage = "Lupremica"; 
  // Java script classes in the default package cannot be accessed by other scripts
  // So we put all java scripts in package Lupremica;
  // The compiled *.class file then has to reside in classURL/Lupremica
  
  static String getFilenameWithoutExt(final String sourcePath) throws IOException
  {
    final Path path = Paths.get(sourcePath);
    final String filename = path.getFileName().toString(); 
    return filename.substring(0, filename.lastIndexOf(".")); // strip .ext
  }

  static String readCode(final String sourcePath) throws FileNotFoundException
  {
      final InputStream stream = new FileInputStream(sourcePath);
      final String separator = System.getProperty("line.separator");
      final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      return reader.lines().collect(Collectors.joining(separator));
  }

  static Path saveSource(final String source) throws IOException
  {
      final String tmpProperty = Config.FILE_TEMP_PATH.getValue().getPath();
      final Path lupremicaPackage = Paths.get(tmpProperty, scriptPackage);
      Files.createDirectories(lupremicaPackage); // Check if the scriptPackage folder exists in the temp path, else create it
      final Path sourcePath = Paths.get(tmpProperty, scriptPackage, JavaExecutor.fileName + ".java");
      // System.out.println("sourcePath: " + sourcePath);
      Files.write(sourcePath, source.getBytes(StandardCharsets.UTF_8));
      return sourcePath;
  }

  static Path compileSource(final Path javaFile)
  {
      final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      // System.out.println("javaFile.toFile().getAbsolutePath() == " + javaFile.toFile().getAbsolutePath());
      compiler.run(null, null, null, javaFile.toFile().getAbsolutePath());
      return javaFile.getParent().resolve(JavaExecutor.fileName + ".class");
  }

    static void runClass(final Path classFile, final IDE ide)
        throws MalformedURLException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
  {
      // System.out.println("classFile == " + classFile);
      final Path parent = classFile.getParent();
      final Path grandParent = parent.getParent();
      final URL classUrl = grandParent.toFile().toURI().toURL();
      // System.out.println("classUrl: " + classUrl);
      
      final URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classUrl});
      final String className = scriptPackage + "." + JavaExecutor.fileName;
      // System.out.println("className == " + className);
      final Class<?> clazz = Class.forName(className, true, classLoader);
      // System.out.println("clazz == " + clazz.getName());
      
      final Constructor<?> constr = clazz.getDeclaredConstructor(org.supremica.gui.ide.IDE.class);
      constr.newInstance(ide);

  }

    public static void exeJava(final String sourcePath, final IDE ide) throws Exception
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

        javax.swing.filechooser.FileNameExtensionFilter filter =
        	new javax.swing.filechooser.FileNameExtensionFilter("Lua scripts (*.lua)", "lua");
    	jfc.setFileFilter(filter);

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
      final org.luaj.vm2.LuaValue print = globals.load(printCode);
      final org.luaj.vm2.LuaValue chunk = globals.loadfile(script);
      // Use any of the "call()" or "invoke()" functions directly on the chunk.
      final org.luaj.vm2.LuaValue luaIDE = org.luaj.vm2.lib.jse.CoerceJavaToLua.coerce(ide);
      final org.luaj.vm2.LuaValue luaScript = org.luaj.vm2.lib.jse.CoerceJavaToLua.coerce(script);
      // Get a logger named for this script specifically
      final Logger log = LogManager.getLogger(script);
      final org.luaj.vm2.LuaValue luaLogger = org.luaj.vm2.lib.jse.CoerceJavaToLua.coerce(log);
      print.call(luaLogger);
      chunk.call(luaScript, luaIDE, luaLogger);
    }
    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(AnalyzerRunScript.class);
	private static final String printCode = // Lua code to redirect print to logger.info
		"local log = ... " +
		"print = function(...) " +
			"local args = {...} " +
			"local str = table.concat(args) " +
			"log:info(str, 0) " +
		"end\n";
}