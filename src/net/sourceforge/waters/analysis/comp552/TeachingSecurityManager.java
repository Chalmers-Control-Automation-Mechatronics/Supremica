//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   TeachingSecurityManager
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.LoggingPermission;
import java.security.Permission;
import java.security.SecurityPermission;


class TeachingSecurityManager extends SecurityManager
{

  //#########################################################################
  //# Constructor
  TeachingSecurityManager()
    throws IOException
  {
    mReadableDirectories = new LinkedList<String>();
    mWriteableDirectories = new LinkedList<String>();
    mEnabled = false;
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
    final String path = file.getCanonicalPath();
    mReadableDirectories.add(path);
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
    final String path = file.getCanonicalPath();
    mReadableDirectories.add(path);
    mWriteableDirectories.add(path);
  }

  void setEnabled(final boolean enabled)
  {
    mEnabled = enabled;
  }


  //#########################################################################
  //# Overrides for Base Class java.lang.SecurityManager
  public void checkAccept(final String host, final int port)
  {
    throw new SecurityException
      ("Network access disabled!\n" +
       "(Attempted to accept socket connection from " +
       host + ", port " + port + ".)");
  }

  public void checkConnect(final String host, final int port)
  {
    throw new SecurityException
      ("Network access disabled!\n" +
       "(Attempted to open socket connection to " +
       host + ", port " + port + ".)");
  }

  public void checkConnect(final String host, final int port,
                           final Object context)
  {
    checkConnect(host, port);
  }

  public void checkCreateClassLoader()   
  {
  }

  public void checkExec(final String cmd)
  {
    throw new SecurityException
      ("System commands disabled!\n(Attempted to execute '" + cmd + "'.)");
  }

  public void checkExit(final int status)
  {
    if (mEnabled) {
      throw new SecurityException("System.exit() disabled!");
    }
  }

  public void checkLink(final String lib)
  {
    if (mEnabled) {
      throw new SecurityException
	("Native code libraries disabled!\n" +
	 "(Attempted to load library '" + lib + "'.)");
    }
  }

  public void checkListen(final int port)
  {
    throw new SecurityException
      ("Network access disabled!\n" +
       "(Attempted to listen on port " + port + ".)");
  }

  public void checkMemberAccess(final Class<?> clazz, final int which)
  {
  }

  public void checkPermission(final Permission perm)
  {
    final String name = perm.getName();
    if (mEnabled) {
      if (perm instanceof FilePermission) {
        final FilePermission fileperm = (FilePermission) perm;
        final String actionlist = fileperm.getActions();
        final String[] actions = actionlist.split(",");
        for (final String action : actions) {
          if (action.equals("read")) {
            if (!isAccessible(name, mReadableDirectories)) {
              throw new SecurityException
                ("File access disabled!\n" +
                 "(Attempted to read '" + name + "'.)");
            }
          } else if (action.equals("write") || actions.equals("delete")) {
            if (!isAccessible(name, mWriteableDirectories)) {
              throw new SecurityException
                ("File access disabled!\n(Attempted to " + action +
                 " '" + name + "'.)");
            }
          } else {
            throw new SecurityException
              ("System commands disabled!\n" +
		 "(Attempted to execute '" + name + "'.)");
          }
        }
      } else {
        super.checkPermission(perm);
      }
    }
  }

  public void checkPackageAccess(final String pack)
  {
  }

  public void checkPropertyAccess(final String key)
  {
    if (mEnabled) {
      super.checkPropertyAccess(key);
    }
  }

  public void checkSetFactory()
  {
    throw new SecurityException
      ("Network access disabled!\n(Attempted set socket a factory.)");
  }

  public void checkSystemClipboardAccess()
  {
    throw new SecurityException("Clipboard access disabled!");
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


  //#########################################################################
  //# Data Members 
  private final Collection<String> mReadableDirectories;
  private final Collection<String> mWriteableDirectories;
  private boolean mEnabled;

}