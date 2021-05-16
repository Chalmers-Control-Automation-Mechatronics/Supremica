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


package net.sourceforge.waters.analysis.options;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import net.sourceforge.waters.model.expr.ParseException;

import org.apache.logging.log4j.LogManager;


public final class OptionFileManager
{

  //#########################################################################
  //# Dummy Constructor
  private OptionFileManager()
  {
  }

  //#########################################################################
  //# Loading
  public static void loadProperties(final File theFile)
    throws IOException
  {
    mPropertyFile = theFile; // this is the file we load properties from, it should also be the one to save to
    updateProperties(mPropertyFile);
  }

  /**
   * Load properties from file.
   */
  private static void updateProperties(final File propertyFile)
    throws IOException
  {
    final Properties propertiesFromFile = buildProperties(propertyFile);
    try {
      mReadingPropertyFile = true;
      for (final Enumeration<?> e = propertiesFromFile.keys();
        e.hasMoreElements();) {
        final String legacyKey = (String) e.nextElement();
        final String legacyValue = propertiesFromFile.getProperty(legacyKey);
        final LegacyOption legacyOption = LegacyOption.get(legacyKey);
        final String key;
        final String value;
        if (legacyOption == null) {
          key = legacyKey;
          value = legacyValue;
        } else {
          value = legacyOption.getReplacementValue(legacyValue);
          if (value == null) {
            continue;
          }
          key = legacyOption.getReplacementName();
        }
        int index = 0;
        while (index != -1) {
          final String prefix = key.substring(0, index);
          final LeafOptionPage page = OptionPage.getLeafOptionPage(prefix);
          if (page != null) {
            final String suffix = key.substring(index + 1);
            final Option<?> option = page.get(suffix);
            if (option != null) {
              try {
                option.set(value);
                break;
              } catch (final ParseException exception) {
                System.err.println("Invalid argument to property: " + key);
                break;
              }
            }
          }
          index = key.indexOf('.', index + 1);
        }
        if (index == -1) {
          System.err.println("Unknown property: " + key);
        }
      }
    } finally {
      mReadingPropertyFile = false;
    }
  }

  private static Properties buildProperties(final File theFile)
    throws FileNotFoundException, IOException
  {
    final FileInputStream theStream = new FileInputStream(theFile);
    return buildProperties(new BufferedInputStream(theStream));
  }

  private static Properties buildProperties(final InputStream inStream)
    throws IOException
  {
    final Properties newProperties = new Properties();
    newProperties.load(inStream);
    return newProperties;
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

  public static void saveProperties(final boolean saveAll) throws IOException
  {
    if (mPropertyFile != null) {
      saveProperties(mPropertyFile, saveAll);
    } else {
      LogManager.getLogger().error
        ("No configuration file to write to, was not specified (by -p) on startup");
    }
  }

  /**
   * Save the property list to the configuration file.
   *
   * @param propertyFile
   *          is the name of the config file
   * @param saveAll
   *          if this is true all mutable properties are saved to file
   *          otherwise only those properties that values different from the
   *          default value is saved.
   */
  private static void saveProperties(final File propertyFile,
                                     final boolean saveAll)
    throws FileNotFoundException, IOException
  {
    try (final OutputStream os = new FileOutputStream(propertyFile)) {
      final BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(os, "8859_1"));
      writer.write("# Waters configuration file\n");
      writer.write("# Created: " + new Date().toString() + "\n\n");
      OptionPage.TOP_LEVEL_AGGREGATOR.saveProperties(writer, saveAll);
      writer.flush();
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
      while (current < mSaveTime) {
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
  private static File mPropertyFile = null; // Set by loadProperties
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
