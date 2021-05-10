//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.comp552;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.Collection;
import java.util.LinkedList;
import java.util.PropertyPermission;


class TeachingSecurityManager extends SecurityManager
{

  //#########################################################################
  //# Constructor
  TeachingSecurityManager()
    throws IOException
  {
    mReadableDirectories = new LinkedList<String>();
    mWriteableDirectories = new LinkedList<String>();
    mAllowedLibraries = new LinkedList<String>();
    mEnabled = false;
    mClosed = false;
  }


  //#########################################################################
  //# Configuration
  void addReadOnlyDirectory(final String filename)
    throws IOException
  {
    final File file = new File(filename);
    addReadOnlyDirectory(file);
  }

  void addReadOnlyDirectory(final File file)
    throws IOException
  {
    if (!mClosed) {
      final String path = file.getCanonicalPath();
      mReadableDirectories.add(path);
    }
  }

  void addReadWriteDirectory(final String filename)
    throws IOException
  {
    final File file = new File(filename);
    addReadWriteDirectory(file);
  }

  void addReadWriteDirectory(final File file)
    throws IOException
  {
    if (!mClosed) {
      final String path = file.getCanonicalPath();
      mReadableDirectories.add(path);
      mWriteableDirectories.add(path);
    }
  }

  void addLibrary(final String libname)
  {
    if (!mClosed) {
      mAllowedLibraries.add(libname);
    }
  }

  void setEnabled(final boolean enabled)
  {
    mEnabled = enabled;
  }

  void close()
  {
    mClosed = true;
  }


  //#########################################################################
  //# Overrides for Base Class java.lang.SecurityManager
  @Override
  public void checkAccept(final String host, final int port)
  {
    throw new SecurityException
      ("Network access disabled!\n" +
       "(Attempted to accept socket connection from " +
       host + ", port " + port + ".)");
  }

  @Override
  public void checkConnect(final String host, final int port)
  {
    throw new SecurityException
      ("Network access disabled!\n" +
       "(Attempted to open socket connection to " +
       host + ", port " + port + ".)");
  }

  @Override
  public void checkConnect(final String host, final int port,
                           final Object context)
  {
    checkConnect(host, port);
  }

  @Override
  public void checkCreateClassLoader()
  {
  }

  @Override
  public void checkExit(final int status)
  {
    if (mEnabled) {
      throw new SecurityException("System.exit() disabled!");
    }
  }

  @Override
  public void checkLink(final String libname)
  {
    if (mEnabled && !mAllowedLibraries.contains(libname)) {
      throw new SecurityException
        ("Native code libraries disabled!\n" +
         "(Attempted to load library '" + libname + "'.)");
    }
  }

  @Override
  public void checkListen(final int port)
  {
    throw new SecurityException
      ("Network access disabled!\n" +
       "(Attempted to listen on port " + port + ".)");
  }

  @Override
  public void checkPermission(final Permission perm)
  {
    final String name = perm.getName();
    if (perm instanceof FilePermission) {
      final FilePermission fileperm = (FilePermission) perm;
      final String actionlist = fileperm.getActions();
      final String[] actions = actionlist.split(",");
      for (final String action : actions) {
        if (action.equals("read")) {
          if (mEnabled &&
              !isAccessible(name, mReadableDirectories) &&
              !isAllowedLibrary(name)) {
            super.checkPermission(perm);
          }
        } else if (action.equals("write") || action.equals("delete")) {
          if (!isAccessible(name, mWriteableDirectories)) {
            super.checkPermission(perm);
          }
        } else {
          super.checkPermission(perm);
        }
      }
    } else if (perm instanceof PropertyPermission) {
      final PropertyPermission propperm = (PropertyPermission) perm;
      final String actionlist = propperm.getActions();
      final String[] actions = actionlist.split(",");
      for (final String action : actions) {
        if (action.equals("read")) {
          if (name.equals("*") ||
              name.equals("java.*") ||
              name.equals("java.library.*") ||
              name.equals("java.library.path") ||
              name.equals("user.*") ||
              name.equals("user.home") ||
              name.equals("user.name")) {
            super.checkPermission(perm);
          }
        } else if (action.equals("write")) {
          if (name.equals("sun.font.fontmanager")) {
            // With Java 1.7, needed by AWT for loading rectangle ???
          } else {
            super.checkPermission(perm);
          }
        }
      }
    } else if (perm instanceof ReflectPermission) {
      // allow :-(
    } else if (perm instanceof RuntimePermission) {
      // needed for Java 1.8 lambda expressions?
    } else if (mEnabled) {
      super.checkPermission(perm);
    }
  }

  @Override
  public void checkPackageAccess(final String pack)
  {
  }

  @Override
  public void checkSetFactory()
  {
    throw new SecurityException
      ("Network access disabled!\n(Attempted set socket a factory.)");
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean isAccessible(final String filename,
                               final Collection<String> dirs)
  {
    try {
      final String path = new File(filename).getCanonicalPath();
      for (final String dir : dirs) {
        if (path.startsWith(dir)) {
          return true;
        }
      }
      return false;
    } catch (final IOException exception) {
      throw new SecurityException(exception);
    }
  }

  private boolean isAllowedLibrary(final String filename)
  {
    final File file = new File(filename);
    final String tail = file.getName();
    if (tail.startsWith("lib") && tail.endsWith(".so")) {
      final int start = 3;
      final int end = tail.length() - 3;
      final String libname = tail.substring(start, end);
      return mAllowedLibraries.contains(libname);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Data Members
  private final Collection<String> mReadableDirectories;
  private final Collection<String> mWriteableDirectories;
  private final Collection<String> mAllowedLibraries;
  private boolean mEnabled;
  private boolean mClosed;

}
