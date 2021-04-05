//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * <P>A command line tool to determine the OS and architecture the VM is
 * running on. This is used by the main ant build script to choose which
 * dynamic libraries should be copied into the distribution directory.</P>
 *
 * <P>USAGE:<BR>
 * &nbsp;&nbsp;<CODE>java
 *   net.sourceforge.waters.build.arch.ArchitectureDetector
 *   &lt;<I>library&nbsp;path</I>&gt; &lt;<I>search&nbsp;path</I>&gt;
 *   &lt;<I>property&nbsp;file&gt;</I></CODE></P>
 *
 * <P>Examines each subdirectory of the given directory
 * &lt;<I>search&nbsp;path</I>&gt; to see whether it contains a loadable
 * library named <CODE>waters</CODE> for some operating system. If a
 * candidate file is found, it is linked or copied (under a different name)
 * to the directory identified as &lt;<I>library&nbsp;path</I>&gt;, which must
 * be equal to the <CODE>java.library.path</CODE> property. Then it is
 * attempted to load the file as a dynamic library into the VM.
 * The name of the first subdirectory where this succeeds is placed as a
 * property in the file &lt;<I>property file</I>&gt;, which will contain a
 * line like this:<BR>
 * &nbsp;&nbsp;<CODE>native.host.arch = &lt;subdir&gt;</CODE></P>
 *
 * <P><I>Notes.</I></P>
 * <UL>
 * <LI>The program attempts to load dynamic libraries matching the following
 * patterns: <CODE>*.dll</CODE>, <CODE>lib*.so</CODE>,
 * <CODE>*.dylib</CODE>.</LI>
 * <LI>If no loadable library can be found, the <CODE>native.host.arch</CODE>
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
    if (args.length != 3) {
      usage();
    }

    final File searchPath = new File(args[1]);
    final File[] subDirs = searchPath.listFiles(DIRECTORY_FILTER);
    if (subDirs == null) {
      usage();
    }

    final String libPathName = args[0];
    final File libPath = new File(libPathName);
    if (!libPath.exists()) {
      libPath.mkdir();
    } else if (!libPath.isDirectory()) {
      usage();
    }

    try {
      String arch = null;
      for (final File subDir : subDirs) {
        if (subDir.equals(libPath)) {
          continue;
        } else if (containsLoadableLibrary(subDir.getName(),
                                           subDir, libPathName)) {
          arch = subDir.getName();
          System.out.println("Found architecture: " + arch);
          break;
        }
      }
      if (arch == null) {
        System.err.println("Could not find loadable library.");
      }
      writePropertiesFile(args[2], arch);

    } catch (final Throwable exception) {
      System.err.print("ERROR: ");
      System.err.print(exception.getClass().getSimpleName());
      System.err.println("caught in main.");
      exception.printStackTrace();
    } finally {
      deleteDirectory(libPath);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private static void usage()
  {
    System.err.print("USAGE: java ");
    System.err.print(ArchitectureDetector.class.getName());
    System.err.println("<library path> <search path> <property file>");
    System.exit(1);
  }

  private static boolean containsLoadableLibrary(final String arch,
                                                 final File subDir,
                                                 final String libPathName)
    throws IOException, SecurityException
  {
    final File[] libFiles = subDir.listFiles(LIBRARY_FILTER);
    if (libFiles != null) {
      for (final File libFile : libFiles) {
        if (libFile.isDirectory()) {
          if (containsLoadableLibrary(arch, libFile, libPathName)) {
            return true;
          }
        } else {
          final String libFileName = libFile.getName();
          final int dotPos = libFileName.indexOf('.');
          final String base = libFileName.substring(0, dotPos);
          final String ext = libFileName.substring(dotPos);
          final String libFileNameWithArch = base + '.' + arch + ext;
          final Path libPathWithArch =
            Paths.get(libPathName, libFileNameWithArch);
          final Path libPath = libFile.toPath();
          if (!Files.exists(libPathWithArch)) {
            try {
              Files.createLink(libPathWithArch, libPath);
            } catch (IOException | SecurityException |
                     UnsupportedOperationException exception) {
              // never mind---try to copy
            }
          }
          if (!Files.exists(libPathWithArch)) {
            try {
              Files.copy(libPath, libPathWithArch);
            } catch (final IOException exception) {
              System.err.println("Error copying library: " + libPath);
              System.err.println(exception.getMessage());
              throw exception;
            }
          }
          try {
            final String loadableName = LIBRARY_NAME + '.' + arch;
            System.loadLibrary(loadableName);
            return true;
          } catch (final UnsatisfiedLinkError error) {
            final String msg = error.getMessage();
            if (msg.contains("Can't find dependent libraries")) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private static boolean deleteDirectory(final File directoryToBeDeleted)
  {
    final File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (final File file : allContents) {
        deleteDirectory(file);
      }
    }
    return directoryToBeDeleted.delete();
  }

  private static void writePropertiesFile(final String propFileName,
                                          final String arch)
    throws IOException
  {
    try {
      final File propFile = new File(propFileName);
      final OutputStream stream = new FileOutputStream(propFile);
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
    } catch (final IOException exception) {
      System.err.println("Error writing properties file: " + propFileName);
      System.err.println(exception.getMessage());
      throw exception;
    }
  }


  //#########################################################################
  //# Inner Class DirectoryFilter
  private static class DirectoryFilter implements FilenameFilter
  {
    //#######################################################################
    //# Interface java.io.FilenameFilter
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
    //#######################################################################
    //# Interface java.io.FilenameFilter
    @Override
    public boolean accept(final File dir, final String name)
    {
      final File file = new File(dir, name);
      if (file.isDirectory()) {
        return true;
      }
      final int dotPos = name.lastIndexOf('.');
      if (dotPos < 0) {
        return false;
      }
      final String ext = name.substring(dotPos + 1);
      if (ext.equalsIgnoreCase("dll") || ext.equals("dylib")) {
        final String base = name.substring(0, dotPos);
        return base.equals(LIBRARY_NAME);
      } else if (ext.equals("so") && name.startsWith("lib")) {
        final String base = name.substring(3, dotPos);
        return base.equals(LIBRARY_NAME);
      } else {
        return false;
      }
    }
  }


  //#########################################################################
  //# Class Constants
  private static final String LIBRARY_NAME = "waters";
  private static final FilenameFilter DIRECTORY_FILTER =
    new DirectoryFilter();
  private static final FilenameFilter LIBRARY_FILTER =
    new LibraryFilter();

}
