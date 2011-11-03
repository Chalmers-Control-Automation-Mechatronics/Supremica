//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Build Tools
//# PACKAGE: net.sourceforge.waters.build.arch
//# CLASS:   ArchitectureDetector
//###########################################################################
//# $Id: 9bd0175b5a1b7006bb9b95311dfffbc5d3c6a213 $
//###########################################################################

package net.sourceforge.waters.build.arch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;


/**
 * <P>A command line tool to determine the OS and architecture the VM is
 * running on. This is used by the main ant build script to choose which
 * dynamic libraries should be copied into the distribution directory.</P>
 *
 * <P>USAGE:<BR>
 * &nbsp;&nbsp;<CODE>java
 *   net.sourceforge.waters.build.arch.ArchitectureDetector
 *   &lt;searchpath&gt; &lt;propfile&gt;</CODE><BR>
 * Examines each subdirectory &lt;subdir&gt of the given directory
 * &lt;searchpath&gt, recursively searching for dynamic libraries.
 * The name of the first subdirectory containing library that can be
 * successfully loaded is placed as a property in the file &lt;propfile&gt,
 * which will contain a line like this:<BR>
 * &nbsp;&nbsp;<CODE>java.arch = &lt;subdir&gt;</CODE></P>
 *
 * <P><I>Notes.</I></P>
 * <UL>
 * <LI>The program attempts to load dynamic libraries matching the following
 * patterns: <CODE>*.dll</CODE>, <CODE>lib*.so</CODE>,
 * <CODE>*.dylib</CODE>.</LI>
 * <LI>It will not attempt to load more than one dynamic library per
 * subdirectory. As soon as one attempt fails, the next subdirectory will
 * be searched.</LI>
 * <LI>If not loadable library can be found, the <CODE>java.arch</CODE>
 * variable will be defined to be empty.</LI>
 * </UL>
 *
 * @author Robi Malik
 */
public class ArchitectureDetector
{

  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    try {
      if (args.length != 2) {
        usage();
      }
      final File searchPath = new File(args[0]);
      final File[] subdirs = searchPath.listFiles(DIRECTORY_FILTER);
      if (subdirs == null) {
        usage();
      }
      String arch = null;
      for (final File subdir : subdirs) {
        if (containsLoadableLibrary(subdir)) {
          arch = subdir.getName();
          System.out.println("Found architecture: " + arch);
          break;
        }
      }
      writePropertiesFile(args[1], arch);
    } catch (final IOException exception) {
      System.err.println("Error writing properties file: " + args[1]);
      System.err.println(exception.getMessage());
      System.exit(1);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private static void usage()
  {
    System.err.println("USAGE: " + ArchitectureDetector.class.getName() +
                       " <searchpath> <propfile>");
    System.exit(1);
  }


  private static boolean containsLoadableLibrary(final File subdir)
  {
    final File[] children = subdir.listFiles(LIBRARY_FILTER);
    for (final File child : children) {
      if (child.isDirectory()) {
        if (containsLoadableLibrary(child)) {
          return true;
        }
      } else {
        final String name = child.getName();
        final String libname = LIBRARY_FILTER.getLibraryName(name);
        try {
          System.setProperty("java.library.path", subdir.toString());
          final Field fieldSysPath =
            ClassLoader.class.getDeclaredField("sys_paths");
          fieldSysPath.setAccessible(true);
          fieldSysPath.set(null, null);
          System.loadLibrary(libname);
          return true;
        } catch (final UnsatisfiedLinkError error) {
          final String msg = error.getMessage();
          if (!msg.contains("Can't find dependent libraries")) {
            return false;
          }
        } catch (final SecurityException exception) {
          throw new RuntimeException(exception);
        } catch (final NoSuchFieldException exception) {
          throw new RuntimeException(exception);
        } catch (final IllegalAccessException exception) {
          throw new RuntimeException(exception);
        }
      }
    }
    return false;
  }


  private static void writePropertiesFile(final String propname,
                                          final String arch)
    throws IOException
  {
    final File propfile = new File(propname);
    final OutputStream stream = new FileOutputStream(propfile);
    final PrintWriter writer = new PrintWriter(stream);
    writer.println("##############################################################################");
    writer.println("# Detected Architecture for build.xml");
    writer.println("# Automatically generated --- do not edit");
    writer.println("##############################################################################");
    writer.println();
    if (arch == null) {
      writer.println("java.os =");
      writer.println("java.arch =");
    } else {
      final int dotpos = arch.lastIndexOf('.');
      final String os = dotpos < 0 ? arch : arch.substring(0, dotpos);
      writer.println("java.os = " + os);
      writer.println("java.arch = " + arch);
    }
    writer.close();
  }


  //#########################################################################
  //# Inner Class DirectoryFilter
  private static class DirectoryFilter implements FilenameFilter
  {

    public boolean accept(final File dir, final String name)
    {
      final File subdir = new File(dir, name);
      return subdir.isDirectory();
    }

  }


  //#########################################################################
  //# Inner Class LibraryFilter
  private static class LibraryFilter implements FilenameFilter
  {

    public boolean accept(final File dir, final String name)
    {
      final File file = new File(dir, name);
      if (file.isDirectory()) {
        return true;
      } else {
        return getLibraryName(name) != null;
      }
    }

    private String getLibraryName(final String name)
    {
      final int dotpos = name.lastIndexOf('.');
      if (dotpos < 0) {
        return null;
      }
      final String ext = name.substring(dotpos + 1);
      if (ext.equalsIgnoreCase("dll") || ext.equals("dylib")) {
        return name.substring(0, dotpos);
      } else if (ext.equals("so") && name.startsWith("lib")) {
        return name.substring(3, dotpos);
      } else {
        return null;
      }
    }

  }


  //#########################################################################
  //# Class DirectoryFilter
  private static final FilenameFilter DIRECTORY_FILTER = new DirectoryFilter();
  private static final LibraryFilter LIBRARY_FILTER = new LibraryFilter();

}
