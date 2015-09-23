//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
