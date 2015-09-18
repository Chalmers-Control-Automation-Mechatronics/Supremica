//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.config
//# CLASS:   Version
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Version information.
 * This class contains details loaded from a property file
 * <CODE>Version.properties</CODE> located in the same package as this
 * class. The information in that file is generated at compile time
 * by the main <CODE>build.xml</CODE> script and copied directly into
 * the JAR.
 *
 * @author Robi Malik
 */
public class Version
{

  //#########################################################################
  //# Singleton Pattern
  public static Version getInstance()
  {
    return INSTANCE;
  }

  private static final Version INSTANCE = new Version();

  private Version()
  {
    final Properties props = new Properties();
    Reader reader = null;
    try {
      final URL url = getClass().getResource("Version.properties");
      InputStream stream;
      stream = url.openStream();
      reader = new InputStreamReader(stream);
      props.load(reader);
    } catch (final IOException | IllegalArgumentException exception) {
      // Should not happen but checking for null below just in case ...
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException exception) {
          // Should not happen ...
        }
      }
    }
    final String title = props.getProperty("build.title");
    mTitle = title == null ? "Waters/Supremica IDE" : title;
    final String buildTime = props.getProperty("build.time");
    if (buildTime != null) {
      final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
      try {
        mBuildTime = format.parse(buildTime);
      } catch (final ParseException exception) {
        // Should not happen ...
      }
    }
    mOSType = props.getProperty("native.host.arch");
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return getTitle() + ", built " + getPrintableBuildTime();
  }


  //#########################################################################
  //# Simple Access
  public String getTitle()
  {
    return mTitle;
  }

  public Date getBuildTime()
  {
    return mBuildTime;
  }

  public String getPrintableBuildTime()
  {
    final DateFormat format =
      DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);
    return format.format(mBuildTime);
  }

  public String getOSType()
  {
    return mOSType;
  }


  //#########################################################################
  //# Environment Version Information
  public String getJavaVersionText()
  {
    String text = System.getProperty("java.version");
    final int wordSize = getWordSize();
    if (wordSize > 0) {
      text += " (" + wordSize + "-bit)";
    }
    return text;
  }

  public int getWordSize()
  {
    try {
      final String text = System.getProperty("sun.arch.data.model");
      return Integer.parseInt(text);
    } catch (final NumberFormatException exception) {
      return -1;
    }
  }

  public boolean checkOSType()
  {
    final String os = System.getProperty("os.name");
    final String part1;
    if (os.startsWith("Linux")) {
      part1 = "linux";
    } else if (os.startsWith("Windows")) {
      part1 = "win32";
    } else {
      return false;  // Unknown or unsupported OS
    }
    final String part2;
    switch (getWordSize()) {
    case 32:
      part2 = "x86";
      break;
    case 64:
      part2 = "amd64";
      break;
    default:
      return true;  // Word size unknown - can't be sure ...
    }
    return mOSType.equals(part1 + "." + part2);
  }


  //#########################################################################
  //# Data Members
  private final String mTitle;
  private Date mBuildTime;
  private final String mOSType;

}
