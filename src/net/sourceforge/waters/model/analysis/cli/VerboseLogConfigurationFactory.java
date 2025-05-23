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

package net.sourceforge.waters.model.analysis.cli;

import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class VerboseLogConfigurationFactory
  extends ConfigurationFactory
  {
  //#######################################################################
  //# Constructor
  public VerboseLogConfigurationFactory(final Level verbosity)
  {
    mVerbosity = verbosity;
  }

  //#######################################################################
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

  //#######################################################################
  //# Creating the Configuration
  private Configuration createConfiguration(final String name,
                                            final ConfigurationBuilder<BuiltConfiguration> builder)
  {
    builder.setConfigurationName(name);
    builder.setStatusLevel(Level.WARN);
    final AppenderComponentBuilder appenderBuilder =
      builder.newAppender("stdout", "CONSOLE").
      addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
    appenderBuilder.add(builder.newLayout("PatternLayout").
                        addAttribute("pattern", "%-5level %msg%n"));
    builder.add(appenderBuilder);
    builder.add(builder.newRootLogger(mVerbosity).
                add(builder.newAppenderRef("stdout")));
    return builder.build();
  }

  //#######################################################################
  //# Data Members
  private final Level mVerbosity;
}
