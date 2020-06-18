//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.gui.logging;

import java.io.File;
import java.io.Serializable;

import net.sourceforge.waters.analysis.options.OptionChangeEvent;
import net.sourceforge.waters.analysis.options.OptionChangeListener;
import net.sourceforge.waters.model.base.ProxyTools;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import org.supremica.gui.ide.IDE;
import org.supremica.properties.Config;


/**
 * <P>A LOG4J custom appender to support logging in the graphical user
 * interface.</P>
 *
 * <P>When the {@link IDE} is started, the {@link IDELogConfigurationFactory}
 * is invoked and associates this appender is associated with the root logger.
 * It then receives all log events and relays them to different log streams.</P>
 *
 * <P>Before the IDE window is up and running, only errors and warnings are
 * logged to the console (stderr). Once the IDE window is visible on screen,
 * the IDE appender is reconfigured based on Supremica's configuration.
 * The default behaviour is to display errors, warnings, and info messages
 * in the graphical user interface ({@link LogPanel}). With different options,
 * the IDE appender can also send log messages to the console (stderr) or
 * to a log file.</P>
 *
 * @author Robi Malik
 */

@Plugin(name = "IDEAppender", category = "Core", elementType = "appender", printObject = true)
public class IDEAppender
  extends AbstractAppender
  implements OptionChangeListener
{

  //#########################################################################
  //# Factory Methods
  @PluginFactory
  public static IDEAppender createAppender
    (@PluginAttribute("name") String name,
     @PluginElement("Layout") Layout<? extends Serializable> layout,
     @PluginElement("Filter") final Filter filter,
     @PluginAttribute("otherAttribute") final String otherAttribute)
  {
    if (name == null) {
      name = ProxyTools.getShortClassName(IDEAppender.class);
    }
    if (layout == null) {
      final PatternLayout.Builder layoutBuilder = PatternLayout.newBuilder();
      layoutBuilder.withPattern("%-5level %msg%n");
      layout = layoutBuilder.build();
    }
    INSTANCE = new IDEAppender(name, filter, layout, true);
    return INSTANCE;
  }

  public static void configure(final LogPanel panel)
  {
    INSTANCE.register(panel);
    INSTANCE.updateLogFile();
    INSTANCE.registerPropertyChangeListener();
  }


  //#########################################################################
  //# Constructor
  IDEAppender(final String name,
              final Filter filter,
              final Layout<? extends Serializable> layout,
              final boolean ignoreExceptions)
  {
    super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    final ConsoleAppender.Builder<?> builder = ConsoleAppender.newBuilder();
    builder.setName("stderr");
    builder.setLayout(layout);
    builder.setIgnoreExceptions(ignoreExceptions);
    builder.setTarget(ConsoleAppender.Target.SYSTEM_ERR);
    mStdErrAppender = builder.build();
  }


  //#########################################################################
  //# Configuration
  private void register(final LogPanel panel)
  {
    final PatternLayout.Builder layoutBuilder = PatternLayout.newBuilder();
    layoutBuilder.withPattern("%msg");
    final Layout<String> layout = layoutBuilder.build();
    final boolean ignore = ignoreExceptions();
    mLogPanelAppender = panel.createAppender("logPanel", layout, ignore);
    mLogPanelAppender.start();
 }

  private void updateLogFile()
  {
    if (mFileAppender != null) {
      mFileAppender.stop();
      mFileAppender = null;
    }
    final File file = Config.LOG_FILE.getValue();
    if (file != null) {
      final FileAppender.Builder<?> builder = FileAppender.newBuilder();
      builder.setName("logFile");
      builder.withFileName(file.toString());
      final PatternLayout.Builder layoutBuilder = PatternLayout.newBuilder();
      layoutBuilder.withPattern("%-5level [%d] %msg%ex%n");
      final Layout<String> layout = layoutBuilder.build();
      builder.setLayout(layout);
      final boolean ignore = ignoreExceptions();
      builder.setIgnoreExceptions(ignore);
      mFileAppender = builder.build();
      mFileAppender.start();
    }
  }

  private void registerPropertyChangeListener()
  {
    Config.LOG_FILE.addPropertyChangeListener(this);
  }


  //#########################################################################
  //# Interface org.apache.log4j.core.Appender
  @Override
  public void append(final LogEvent event)
  {
    final Level level = event.getLevel();
    if (mLogPanelAppender == null) {
      if (Level.WARN.isLessSpecificThan(level)) {
        mStdErrAppender.append(event);
      }
    } else {
      final boolean logToGui =
        Config.LOG_GUI_VERBOSITY.getValue().isLessSpecificThan(level);
      if (logToGui) {
        mLogPanelAppender.append(event);
      }
      if (Config.LOG_CONSOLE_VERBOSITY.getValue().isLessSpecificThan(level) &&
          !(Config.GENERAL_REDIRECT_STDERR.getValue() && logToGui)) {
        mStdErrAppender.append(event);
      }
      if (mFileAppender != null &&
        Config.LOG_FILE_VERBOSITY.getValue().isLessSpecificThan(level)) {
        mFileAppender.append(event);
      }
    }
  }


  //#########################################################################
  //# Interface org.supremica.properties.SupremicaPropertyChangeListener
  @Override
  public void optionChanged(final OptionChangeEvent event)
  {
    updateLogFile();
  }


  //#########################################################################
  //# Data Members
  private final Appender mStdErrAppender;
  private Appender mLogPanelAppender;
  private FileAppender mFileAppender;


  //#########################################################################
  //# Class Constants
  private static IDEAppender INSTANCE;

}
