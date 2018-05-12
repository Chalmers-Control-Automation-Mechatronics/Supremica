package net.sourceforge.waters.analysis.comp552;

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


/**
 * A LOG4J configuration that ensures minimal logging output for the
 * COMP552 assignments.
 *
 * @author Robi Malik
 */

class QuietLogConfigurationFactory
  extends ConfigurationFactory
{

  //#######################################################################
  //# Initialisation
  static void install()
  {
    final ConfigurationFactory factory =
      new QuietLogConfigurationFactory();
    ConfigurationFactory.setConfigurationFactory(factory);
  }


  //#######################################################################
  //# Constructor
  QuietLogConfigurationFactory()
  {
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
    builder.add(builder.newRootLogger(Level.WARN).
                add(builder.newAppenderRef("stdout")));
    return builder.build();
  }

}
