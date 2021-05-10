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

package net.sourceforge.waters.junit;

import java.io.File;
import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;


/**
 * <P>LOG4J2 Configuration factory for Waters tests.</P>
 *
 * <P>This replaces the default log configuration by a configuration that uses
 * a log file. The log output of all verbosity levels is put in a file
 * called <CODE>log4j.log</CODE> in the test output directory. For example,
 * the output for {@link net.sourceforge.waters.analysis.bdd.BDDDeadlockCheckerTest}
 * is stored under the checkout directory of Waters in the file
 * <CODE>logs/results/analysis/bdd/BDDDeadlockCheckerTest/log4j.log</CODE>.</P>
 *
 * @author Robi Malik
 */

class WatersLogConfigurationFactory extends ConfigurationFactory
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new configuration factory.
   * @param  dir   The directory to contain the log file. Typically obtained
   *               by {@link AbstractWatersTest#getOutputDirectory()}.
   */
  WatersLogConfigurationFactory(final File dir)
  {
    final File file = new File(dir, LOG_FILE_NAME);
    if (file.lastModified() + DAY < System.currentTimeMillis()) {
      file.delete();
    }
    mFileName = file.getAbsolutePath();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns the full path name of the log file.
   */
  String getFileName()
  {
    return mFileName;
  }


  //#########################################################################
  //# Overrides for org.apache.logging.log4j.core.config.ConfigurationFactory
  @Override
  public Configuration getConfiguration(final LoggerContext loggerContext,
                                        final ConfigurationSource source)
  {
    return getConfiguration(loggerContext, source.toString(), null);
  }

  @Override
  public Configuration getConfiguration(final LoggerContext loggerContext,
                                        final String name,
                                        final URI configLocation)
  {
    final ConfigurationBuilder<BuiltConfiguration> builder =
      newConfigurationBuilder();
    return createConfiguration(name, builder);
  }

  @Override
  protected String[] getSupportedTypes()
  {
    return new String[] {"*"};
  }


  //#########################################################################
  //# Creating the Configuration
  private Configuration createConfiguration(final String name,
                                            final ConfigurationBuilder<BuiltConfiguration> builder)
  {
    builder.setConfigurationName(name);
    builder.setStatusLevel(Level.WARN);
    final AppenderComponentBuilder appenderBuilder =
      builder.newAppender("file", "FILE").
      addAttribute("fileName", mFileName);
    appenderBuilder.add(builder.newLayout("PatternLayout").
                        addAttribute("pattern", "%-5level %msg%n%throwable"));
    builder.add(appenderBuilder);
    builder.add(builder.newRootLogger(Level.ALL).add(builder.newAppenderRef("file")));
    return builder.build();
  }


  //#########################################################################
  //# Data Members
  private final String mFileName;


  //#########################################################################
  //# Class Constants
  private static final String LOG_FILE_NAME = "log4j.log";
  private static final long DAY = 24 * 60 * 60 * 1000L;

}
