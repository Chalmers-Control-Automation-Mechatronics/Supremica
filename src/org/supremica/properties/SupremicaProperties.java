//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.properties
//# CLASS:   SupremicaProperties
//###########################################################################
//# $Id$
//###########################################################################

/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

package org.supremica.properties;

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

import org.supremica.util.BDD.Options;


/**
 * Properties for Supremica. All properties are added in the Config class.
 *
 * @author Knut &Aring;kesson
 */

public final class SupremicaProperties
{
  private SupremicaProperties()
  {
  }

  public static String getProperties()
  {
    final StringBuilder sb = new StringBuilder();
    for (final Property property : Property.getAllProperties()) {
      sb.append("# ").append(property.getComment()).append("\n");
      sb.append(property.toString()).append("\n\n");
    }
    return sb.toString();

  }

  public static void loadProperties(final File theFile)
    throws FileNotFoundException, IOException
  {
    propertyFile = theFile; // this is the file we load properties from, it should also be the one to save to
    updateProperties(propertyFile);
  }

  /**
   * Load properties from file.
   */
  private static void updateProperties(final File propertyFile)
    throws FileNotFoundException, IOException
  {
    final Properties propertiesFromFile = buildProperties(propertyFile);
    for (final Enumeration<?> e = propertiesFromFile.keys(); e
      .hasMoreElements();) {
      final String newKey = (String) e.nextElement();
      final String newValue = propertiesFromFile.getProperty(newKey);

      final Property orgProperty = Property.getProperty(newKey);
      if (orgProperty == null) {
        System.err.println("Unknown property: " + newKey);
      } else {
        try {
          orgProperty.set(newValue);
        } catch (final IllegalArgumentException ex) {
          System.err.println("Invalid argument to key: " + newKey);
        }
      }
    }

    // Update values in BDD.Options based on the current Config.
    updateBDDOptions(false);
  }

  /**
   * Saves all properties to the configuration file after two seconds, unless
   * this method is called again before. If the method is called before two
   * seconds have elapsed, the save is delayed for two seconds again. This is
   * used to avoid frequent file access when properties are changed rapidly,
   * e.g., while resizing a window.
   */
  public static void savePropertiesLater()
  {
    if (propertyFile != null) {
      if (mSaverThread == null) {
        mSaverThread = new SaverThread();
        mSaverThread.start();
      } else {
        mSaverThread.waitLonger();
      }
    }
  }

  public static void saveProperties() throws IOException
  {
    SupremicaProperties.saveProperties(false);
  }

  public static void saveProperties(final boolean saveAll) throws IOException
  {
    if (propertyFile != null) {
      saveProperties(propertyFile, saveAll);
    } else {
      System.err
        .println("No configuration file to write to, was not specified (by -p) on startup");
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
    // Update config from the current values in BDD.Options
    // (WHY!!? IT SHOULD BE THE OTHER WAY AROUND OR THEY ARE LOST?! /hguo)
    //updateBDDOptions(true);    // first sync from BDD options
    updateBDDOptions(false); // Send the new Config values to BDD.Options

    try (final OutputStream os = new FileOutputStream(propertyFile)) {
      final BufferedWriter writer =
        new BufferedWriter(new OutputStreamWriter(os, "8859_1"));
      writer.write("# Supremica configuration file\n");
      writer.write("# Created: " + new Date().toString() + "\n\n");

      for (final Property property : Property.getAllProperties()) {
        if (saveAll || property.currentValueDifferentFromDefaultValue()) {
          writer.append("# " + property.getComment() + "\n");
          final String value = property.getAsString();
          final String escaped = getEscapedString(value, false);
          writer.append(property.getPropertyType() + "." + property.getKey() +
                        " " + escaped + "\n\n");
        }
      }

      writer.flush();
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

  /*
   * The problem is that we got two copies of BDD Options. This will make sure
   * they are both updated
   *
   * TO DO: Rewrite the Option code in the BDD Package to support the new
   * style property handling.
   */
  public static void updateBDDOptions(final boolean from_Options)
  {
    if (from_Options) {
      // Options -> Config
      Config.BDD_ORDER_ALGO
        .set(Options.ORDERING_ALGORITHM_NAMES[Options.ordering_algorithm]);
      Config.BDD_DEBUG_ON.set(Options.debug_on);
      Config.BDD_PROFILE_ON.set(Options.profile_on);
    } else {
      // Config -> Options
      Options.ordering_algorithm = indexOf(Config.BDD_ORDER_ALGO.get(),
                                           Options.ORDERING_ALGORITHM_NAMES);
      Options.debug_on = Config.BDD_DEBUG_ON.get();
      Options.profile_on = Config.BDD_PROFILE_ON.get();
    }
  }

  /**
   * Returns the index of object in objects. For the BDD options.
   */
  private static int indexOf(final Object object, final Object[] objects)
  {
    for (int i = 0; i < objects.length; i++) {
      if (object.equals(objects[i]))
        return i;
    }
    return -1;
  }


  //#########################################################################
  //# Formatting
  // ALL OF THIS IS COMING FROM THE JAVA SDK CODE (Properties.java)
  private static String getEscapedString(final String theString,
                                         final boolean escapeSpace)
  {
    final int len = theString.length();
    final StringBuilder outBuffer = new StringBuilder(len * 2);

    for (int x = 0; x < len; x++) {
      final char aChar = theString.charAt(x);

      switch (aChar) {

      case ' ':
        if ((x == 0) || escapeSpace) {
          outBuffer.append('\\');
        }

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
          if (specialSaveChars.indexOf(aChar) != -1) {
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
    return hexDigit[(nibble & 0xF)];
  }

  /** A table of hex digits */
  private static final char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6',
                                          '7', '8', '9', 'A', 'B', 'C', 'D',
                                          'E', 'F'};
  private static final String specialSaveChars = "=: \t\r\n\f#!";


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
  //# Static Variables
  private static File propertyFile = null; // Set by loadProperties
  private static SaverThread mSaverThread;


  //#########################################################################
  //# Class Constants
  private static final long SAVE_DELAY = 2000;
  @SuppressWarnings("unused")
  private final static Config CONFIG = Config.getInstance();


  //#########################################################################
  //# Static Initialiser
  static {
    // Update values in BDD.Options based on Config.
    updateBDDOptions(false);
  }

}
