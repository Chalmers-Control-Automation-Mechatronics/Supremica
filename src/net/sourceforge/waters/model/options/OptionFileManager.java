//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
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

package net.sourceforge.waters.model.options;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;


/**
 * Static methods to read and write options to/from property files.
 *
 * @author Robi Malik
 */

public final class OptionFileManager
{

  //#########################################################################
  //# Dummy Constructor
  private OptionFileManager()
  {
  }


  //#########################################################################
  //# Loading
  /**
   * Loads options from properties file into given option page.
   * Also records this option page and file to be used when saving.
   */
  public static void loadProperties(final OptionPage root,
                                    final File file)
    throws IOException
  {
    mRoot = root;
    mPropertyFile = file; // this is the file we load properties from, it should also be the one to save to
    final Properties properties = loadProperties(file);
    updateProperties(mRoot, properties);
  }

  /**
   * Loads properties from file.
   * Also applies legacy option transformations.
   */
  public static Properties loadProperties(final File file)
    throws IOException
  {
    final FileInputStream stream = new FileInputStream(file);
    try {
      final Properties properties = new Properties();
      properties.load(new BufferedInputStream(stream));
      LegacyOption.transformProperties(properties);
      return properties;
    } finally {
      stream.close();
    }
  }

  /**
   * Updates option page based on properties map.
   * Warns about options that appear in the map but are not defined.
   */
  public static void updateProperties(final OptionPage root,
                                      final Properties properties)
  {
    try {
      mReadingPropertyFile = true;
      root.loadProperties(properties);
      warnAboutUnused(properties);
    } finally {
      mReadingPropertyFile = false;
    }
  }

  private static void warnAboutUnused(final Properties properties)
  {
    final int size = properties.size();
    if (size == 0) {
      return;
    } else if (size < 5) {
      final List<String> names = new ArrayList<>(size);
      for (final Object key : properties.keySet()) {
        final String name = (String) key;
        names.add(name);
      }
      Collections.sort(names);
      for (final String name : names) {
        LogManager.getLogger().warn("Unkown property {} ignored.", name);
      }
    } else {
      LogManager.getLogger().warn("{} unknown properties ignored.", size);
    }
  }


  //#########################################################################
  //# Saving
  /**
   * Saves all properties to the configuration file after two seconds, unless
   * this method is called again before. If the method is called before two
   * seconds have elapsed, the save is delayed for two seconds again. This is
   * used to avoid frequent file access when properties are changed rapidly,
   * e.g., while resizing a window.
   */
  public static void savePropertiesLater()
  {
    if (mPropertyFile != null && !mReadingPropertyFile) {
      final SaverThread thread = mSaverThread;
      if (thread == null) {
        mSaverThread = new SaverThread();
        mSaverThread.start();
      } else {
        thread.waitLonger();
      }
    }
  }

  /**
   * Completes any pending save to the properties file. This method
   * should be called prior to calling System.exit() to ensure that
   * any queued save operation ({@link #savePropertiesLater()} is
   * executed before exiting.
   */
  public static void savePropertiesOnExit()
  {
    if (mPropertyFile != null) {
      final SaverThread thread = mSaverThread;
      if (thread != null) {
        thread.interrupt();
        try {
          thread.join();
        } catch (final InterruptedException exception) {
          // should not be interrupted
        }
      }
    }
  }

  public static void saveProperties() throws IOException
  {
    saveProperties(false);
  }

  public static void saveProperties(final boolean saveAll)
    throws IOException
  {
    if (mPropertyFile != null) {
      saveProperties(mRoot, mPropertyFile, saveAll);
    } else {
      LogManager.getLogger().error
        ("No configuration file to write to, was not specified (by -p) on startup");
    }
  }

  /**
   * Save options to a configuration file.
   * @param  root    The option page containing all options to be saved.
   * @param  propertyFile  The name of the properties file to be written.
   * @param  saveAll If this is <CODE>true</CODE> all persistent properties
   *                 are saved, otherwise only properties with values
   *                 different from the default are saved.
   */
  public static void saveProperties(final OptionPage root,
                                    final File propertyFile,
                                    final boolean saveAll)
    throws IOException
  {
    final OutputStream stream = new FileOutputStream(propertyFile);
    try {
      final BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(stream, "8859_1"));
      writer.write("# Waters configuration file\n");
      writer.write("# Created: " + new Date().toString() + "\n\n");
      root.saveProperties(writer, saveAll);
      writer.flush();
    } finally {
      stream.close();
    }
  }


  //#########################################################################
  //# Inner Class SaverThread
  private static class SaverThread extends Thread
  {
    //#######################################################################
    //# Constructor
    private SaverThread()
    {
      waitLonger();
    }

    //#######################################################################
    //# Invocation
    private void waitLonger()
    {
      mSaveTime = System.currentTimeMillis() + SAVE_DELAY;
    }

    //#######################################################################
    //# Interface java.lang.Runnable
    @Override
    public void run()
    {
      long current = System.currentTimeMillis();
      while (current < mSaveTime && !isInterrupted()) {
        try {
          Thread.sleep(mSaveTime - current);
          current = System.currentTimeMillis();
        } catch (final InterruptedException exception) {
          break;
        }
      }
      mSaverThread = null;
      try {
        saveProperties(false);
      } catch (final IOException exception) {
        // Could not save --- never mind ...
      }
    }

    //#######################################################################
    //# Data Members
    private long mSaveTime;
  }


  //#########################################################################
  //# XML Formatting
  //# This code is originally from the Java SDK Code (Properties.java)
  static String getEscapedString(final String theString)
  {
    final int len = theString.length();
    final StringBuilder outBuffer = new StringBuilder(len * 2);
    for (int x = 0; x < len; x++) {
      final char aChar = theString.charAt(x);
      switch (aChar) {
      case ' ':
        outBuffer.append('\\');
        outBuffer.append(' ');
        break;
      case '\\':
        outBuffer.append('\\');
        outBuffer.append('\\');
        break;
      case '\t':
        outBuffer.append('\\');
        outBuffer.append('t');
        break;
      case '\n':
        outBuffer.append('\\');
        outBuffer.append('n');
        break;
      case '\r':
        outBuffer.append('\\');
        outBuffer.append('r');
        break;
      case '\f':
        outBuffer.append('\\');
        outBuffer.append('f');
        break;
      default:
        if ((aChar < 0x0020) || (aChar > 0x007e)) {
          outBuffer.append('\\');
          outBuffer.append('u');
          outBuffer.append(toHex((aChar >> 12) & 0xF));
          outBuffer.append(toHex((aChar >> 8) & 0xF));
          outBuffer.append(toHex((aChar >> 4) & 0xF));
          outBuffer.append(toHex(aChar & 0xF));
        } else {
          if (SPECIAL_SAVE_CHARS.indexOf(aChar) != -1) {
            outBuffer.append('\\');
          }
          outBuffer.append(aChar);
        }
      }
    }
    return outBuffer.toString();
  }

  private static char toHex(final int nibble)
  {
    return HEX_DIGIT[(nibble & 0xF)];
  }


  //#########################################################################
  //# Static Variables
  private static OptionPage mRoot = null;   // Set by loadProperties()
  private static File mPropertyFile = null; // Set by loadProperties()
  private static boolean mReadingPropertyFile = false;
  private static SaverThread mSaverThread = null;


  //#########################################################################
  //# Class Constants
  private static final long SAVE_DELAY = 2000;

  private static final char[] HEX_DIGIT = {'0', '1', '2', '3', '4', '5', '6',
                                           '7', '8', '9', 'A', 'B', 'C', 'D',
                                           'E', 'F'};
  private static final String SPECIAL_SAVE_CHARS = "=: \t\r\n\f#!";

}
