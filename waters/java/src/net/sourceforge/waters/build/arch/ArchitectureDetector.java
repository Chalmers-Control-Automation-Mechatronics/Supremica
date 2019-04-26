//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
 * The name of the first subdirectory containing a library that can be
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
 * <LI>If no loadable library can be found, the <CODE>java.arch</CODE>
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
      if (arch == null) {
        System.err.println("Could not find any loadable library.");
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
        if (name.equals("libBisimulationEquivalence.so")) {
          continue;
        }
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
      writer.println("native.host.arch =");
    } else {
      writer.println("native.host.arch = " + arch);
    }
    writer.close();
  }


  //#########################################################################
  //# Inner Class DirectoryFilter
  private static class DirectoryFilter implements FilenameFilter
  {

    @Override
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

    @Override
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
