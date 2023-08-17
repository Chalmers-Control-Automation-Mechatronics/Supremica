//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * <P>Version information.</P>
 *
 * <P>This class contains details loaded from a property file
 * <CODE>Version.properties</CODE> located in the same package as this
 * class. The information in that file is generated at compile time
 * by the main <CODE>build.xml</CODE> script and copied directly into
 * the JAR. The version number can be changed by through the file
 * <CODE>{<I>supremica</I>}/templates/Version.properties</CODE>.</P>
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

  public boolean checkOSType()
  {
    final String os = System.getProperty("os.name");
    final String part1 = getOSPrefix(os);
    if (part1 == null) {
      return false;
    }
    final String part2 = getOSSuffix();
    if (part2 == null) {
      return mOSType.startsWith(part1 + ".");
    } else {
      return mOSType.equals(part1 + "." + part2);
    }
  }

  public void appendUnsupportedOSExplanation(final StringBuilder builder)
  {
    final String os = System.getProperty("os.name");
    final String prefix = getOSPrefix(os);
    final String suffix = getOSSuffix();
    boolean canUse = false;
    if (prefix == null) {
      builder.append("Your operating system, ");
      builder.append(os);
      builder.append(", is not supported. ");
    } else if (!mOSType.startsWith(prefix + ".")) {
      builder.append("You are running a ");
      builder.append(getCommonOSName(mOSType));
      builder.append(" version of ");
      builder.append(mTitle);
      builder.append(" on a ");
      builder.append(getCommonOSName(os));
      builder.append(" system. ");
      canUse = true;
    } else if (suffix == null) {
      builder.append("The pre-compiled binaries could not be loaded correctly. ");
    } else if (!mOSType.endsWith("." + suffix)) {
      builder.append("You are running a ");
      builder.append(getExpectedWordSize());
      builder.append("-bit version of ");
      builder.append(mTitle);
      builder.append(" in a ");
      builder.append(getWordSize());
      builder.append("-bit Java virtual machine. ");
      canUse = true;
    }
    if (canUse) {
      builder.append("Please download a ");
      builder.append(getWordSize());
      builder.append("-bit ");
      builder.append(getCommonOSName(os));
      builder.append(" version instead.");
    } else {
      builder.append("You cannot use ");
      builder.append(mTitle);
      builder.append(" on this computer.");
    }
  }

  public static int getWordSize()
  {
    try {
      final String text = System.getProperty("sun.arch.data.model");
      return Integer.parseInt(text);
    } catch (final NumberFormatException exception) {
      return -1;
    }
  }

  public static void printConsoleInfo(final PrintStream stream)
  {
    final Version version = getInstance();
    stream.println(version.getTitle());
    stream.print("Built ");
    stream.println(version.getPrintableBuildTime());
    stream.print("Dynamic libraries ");
    final String osType = version.getOSType();
    if (osType == null) {
      stream.println("unavailable");
    } else {
      stream.print("compiled for ");
      stream.print(osType);
      if (version.checkOSType()) {
        try {
          System.loadLibrary("waters");
        } catch (final UnsatisfiedLinkError error) {
          stream.print(" - failed to load");
        }
      } else {
        stream.print(" - incompatible");
      }
      stream.println();
    }
    stream.print("Running in Java ");
    stream.println(version.getJavaVersionText());
    stream.print("Maximum available memory: ");
    stream.print(Runtime.getRuntime().maxMemory() / 0x100000L);
    stream.println(" MiB");
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getExpectedWordSize()
  {
    if (mOSType.endsWith(".x86")) {
      return 32;
    } else {
      return 64;
    }
  }

  private static String getOSPrefix(final String os)
  {
    if (os.startsWith("Linux")) {
      return "linux";
    } else if (os.startsWith("Windows")) {
      return "win32";
    } else {
      return null;
    }
  }

  private static String getOSSuffix()
  {
    switch (getWordSize()) {
    case 32:
      return "x86";
    case 64:
      return "amd64";
    default:
      return null;
    }
  }

  private static String getCommonOSName(final String os)
  {
    if (os.startsWith("Linux") || os.startsWith("linux")) {
      return "Linux";
    } else {
      return "Windows";
    }
  }


  //#########################################################################
  //# Data Members
  private final String mTitle;
  private Date mBuildTime;
  private final String mOSType;

}
