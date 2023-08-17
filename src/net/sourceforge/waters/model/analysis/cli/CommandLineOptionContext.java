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

import java.awt.Color;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.options.AggregatorOptionPage;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.ChainedAnalyzerOption;
import net.sourceforge.waters.model.options.ColorOption;
import net.sourceforge.waters.model.options.ComponentKindOption;
import net.sourceforge.waters.model.options.Configurable;
import net.sourceforge.waters.model.options.DoubleOption;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.EventSetOption;
import net.sourceforge.waters.model.options.FileOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.MemoryOption;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.OptionContext;
import net.sourceforge.waters.model.options.OptionEditor;
import net.sourceforge.waters.model.options.OptionPageEditor;
import net.sourceforge.waters.model.options.ParameterBindingListOption;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.model.options.PropositionOption;
import net.sourceforge.waters.model.options.SelectorLeafOptionPage;
import net.sourceforge.waters.model.options.SimpleLeafOptionPage;
import net.sourceforge.waters.model.options.StringListOption;
import net.sourceforge.waters.model.options.StringOption;


/**
 *
 * @author Benjamin Wheeler
 */

public class CommandLineOptionContext implements OptionContext
{

  //#########################################################################
  //# Constructors
  public CommandLineOptionContext()
  {
    mDES = null;
    mArgumentMap = new HashMap<>(64);
    mArgumentSources = new LinkedList<>();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.OptionContext
  @Override
  public ProductDESProxy getProductDES()
  {
    return mDES;
  }

  @Override
  public OptionEditor<Boolean> createBooleanEditor(final BooleanOption option)
  {
    return new BooleanCommandLineArgument(option);
  }

  @Override
  public OptionEditor<ModelAnalyzerFactoryLoader>
  createChainedAnalyzerEditor(final ChainedAnalyzerOption option)
  {
    return new ChainedAnalyzerCommandLineArgument(option);
  }

  @Override
  public OptionEditor<Color> createColorEditor(final ColorOption option)
  {
    return null;
  }

  @Override
  public OptionEditor<ComponentKind>
  createComponentKindEditor(final ComponentKindOption option)
  {
    return null;
  }

  @Override
  public OptionEditor<Double> createDoubleEditor(final DoubleOption option)
  {
    return new DoubleCommandLineArgument(option);
  }

  @Override
  public <E> OptionEditor<E> createEnumEditor(final EnumOption<E> option)
  {
    return new EnumCommandLineArgument<E>(option);
  }

  @Override
  public OptionEditor<Set<EventProxy>>
  createEventSetEditor(final EventSetOption option)
  {
    return null;
  }

  @Override
  public OptionEditor<File> createFileEditor(final FileOption option)
  {
    return new FileCommandLineArgument(option);
  }

  @Override
  public OptionEditor<List<ParameterBindingProxy>>
  createParameterBindingListEditor(final ParameterBindingListOption option)
  {
    return new ParameterBindingListCommandLineArgument(option);
  }

  @Override
  public OptionEditor<Integer>
  createPositiveIntEditor(final PositiveIntOption option)
  {
    return new PositiveIntCommandLineArgument(option);
  }

  @Override
  public OptionEditor<String> createMemoryOptionEditor
    (final MemoryOption option)
  {
    return null;
  }

  @Override
  public OptionEditor<EventProxy> createPropositionEditor
    (final PropositionOption option)
  {
    return new PropositionCommandLineArgument(option);
  }

  @Override
  public OptionEditor<String> createStringEditor(final StringOption option)
  {
    return new StringCommandLineArgument(option);
  }

  @Override
  public OptionEditor<List<String>>
  createStringListEditor(final StringListOption option)
  {
    return new StringListCommandLineArgument(option);
  }

  @Override
  public OptionPageEditor<SimpleLeafOptionPage>
  createSimpleLeafOptionPageEditor(final SimpleLeafOptionPage page)
  {
    return null;
  }

  @Override
  public <S> OptionPageEditor<SelectorLeafOptionPage<S>>
  createSelectorLeafOptionPageEditor(final SelectorLeafOptionPage<S> page)
  {
    return null;
  }

  @Override
  public OptionPageEditor<AggregatorOptionPage>
  createAggregatorOptionPageEditor(final AggregatorOptionPage page)
  {
    return null;
  }


  //#########################################################################
  //# Specific Access
  public LeafOptionPage createCommandLineToolOptionPage()
  {
    return new CommandLineToolOptionPage();
  }

  public void registerArguments(final LeafOptionPage page,
                                final Configurable configurable)
  {
    registerArguments(page, configurable, false);
  }

  public void registerArguments(final LeafOptionPage page,
                                final Configurable configurable,
                                final boolean includeSpecials)
  {
    final ArgumentSource source = new ArgumentSource(page, configurable);
    mArgumentSources.add(0, source);
    if (includeSpecials) {
      source.addSpecialArgument(new EndArgument());
      source.addSpecialArgument(new HelpArgument());
      source.addSpecialArgument(new VersionArgument());
    }
  }

  public void parse(final ListIterator<String> iter)
    throws AnalysisException
  {
    while (iter.hasNext()) {
      final String name = iter.next();
      final CommandLineArgument arg = mArgumentMap.get(name);
      if (arg != null) {
        arg.parse(this, iter);
      } else if (name.startsWith("-")) {
        System.err.println("Unsupported option " + name +
                           ". Try -help to see available options.");
        ExitException.testFriendlyExit(1);
      }
    }
  }

  public void setProductDES(final ProductDESProxy des)
    throws AnalysisException
  {
    mDES = des;
    for (final ArgumentSource source : mArgumentSources) {
      source.updateContext();
    }
  }

  public void configure(final Configurable configurable)
    throws AnalysisException
  {
    for (final ArgumentSource source : mArgumentSources) {
      source.configure(configurable);
    }
  }

  public void showHelpMessage(final PrintStream stream)
  {
    for (final ArgumentSource source : mArgumentSources) {
      source.showHelpMessage(stream);
    }
    final LeafOptionPage page = new JavaOptionPage();
    final ArgumentSource source = new ArgumentSource(page);
    source.showHelpMessage(stream);
  }


  //#########################################################################
  //# Auxiliary Methods
  private <T> OptionCommandLineArgument<T>
  registerOption(final Option<T> option)
  {
    final OptionCommandLineArgument<T> arg =
      (OptionCommandLineArgument<T>) option.createEditor(this);
    if (arg != null) {
      registerArgument(arg);
    }
    return arg;
  }

  private void registerArgument(final CommandLineArgument arg)
  {
    for (final String key : arg.getKeys()) {
      mArgumentMap.put(key, arg);
    }
  }


  //#########################################################################
  //# Inner Class ArgumentSource
  private class ArgumentSource
  {
    //#######################################################################
    //# Constructor
    private ArgumentSource(final LeafOptionPage page,
                           final Configurable configurable)
    {
      mTitle = ProxyTools.getShortClassName(configurable);
      final List<Option<?>> options = configurable.getOptions(page);
      mArguments = new ArrayList<>(options.size());
      for (final Option<?> option : options) {
        final OptionCommandLineArgument<?> arg = registerOption(option);
        if (arg != null) {
          mArguments.add(arg);
        }
      }
    }

    private ArgumentSource(final LeafOptionPage page)
    {
      mTitle = page.getTitle();
      final Collection<Option<?>> options =  page.getRegisteredOptions();
      mArguments = new ArrayList<>(options.size());
      for (final Option<?> option : options) {
        final OptionCommandLineArgument<?> arg = registerOption(option);
        if (arg != null) {
          mArguments.add(arg);
        }
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private void updateContext()
      throws AnalysisException
    {
      for (final CommandLineArgument arg : mArguments) {
        arg.updateContext(CommandLineOptionContext.this);
      }
    }

    private void configure(final Configurable configurable)
      throws AnalysisException
    {
      for (final CommandLineArgument arg : mArguments) {
        arg.configure(configurable);
      }
    }

    private void showHelpMessage(final PrintStream stream)
    {
      stream.print(mTitle);
      stream.println(" supports the following options:");
      final List<CommandLineArgument> args = new ArrayList<>(mArguments);
      Collections.sort(args);
      for (final CommandLineArgument arg : args) {
        arg.dump(stream);
      }
    }

    private void addSpecialArgument(final CommandLineArgument arg)
    {
      mArguments.add(arg);
      registerArgument(arg);
    }

    //#######################################################################
    //# Data Members
    private final String mTitle;
    private final List<CommandLineArgument> mArguments;
  }


  //#########################################################################
  //# Inner Class EndArgument
  private class EndArgument extends CommandLineArgument
  {
    //#########################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.cli.CommandLineArgument
    @Override
    public String getCommandLineCode()
    {
      return "--";
    }

    @Override
    public String getDescription()
    {
      return "Treat remaining arguments as file names";
    }

    @Override
    public void parse(final CommandLineOptionContext context,
                      final ListIterator<String> iter)
    {
      iter.remove();
      while (iter.hasNext()) {
        iter.next();
      }
      setUsed(true);
    }
  }


  //#########################################################################
  //# Inner Class HelpArgument
  private class HelpArgument extends CommandLineArgument
  {
    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.cli.CommandLineArgument
    @Override
    public String getCommandLineCode()
    {
      return "-help";
    }

    @Override
    public String getDescription()
    {
      return "Print this message";
    }

    @Override
    public void parse(final CommandLineOptionContext context,
                      final ListIterator<String> iter)
    {
      showHelpMessage(System.out);
      ExitException.testFriendlyExit(0);
    }
  }


  //#########################################################################
  //# Inner Class VersionArgument
  private class VersionArgument extends CommandLineArgument
  {
    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.cli.CommandLineArgument
    @Override
    public String getCommandLineCode()
    {
      return "-version";
    }

    @Override
    public String getDescription()
    {
      return "Show version information and exit";
    }

    @Override
    public void parse(final CommandLineOptionContext context,
                      final ListIterator<String> iter)
    {
      Version.printConsoleInfo(System.out);
      ExitException.testFriendlyExit(0);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineToolOptionPage
  private class CommandLineToolOptionPage
    extends SimpleLeafOptionPage
  {
    //#######################################################################
    //# Constructor
    public CommandLineToolOptionPage()
    {
      super("cli", "Command Line Tool");
      register(new FileOption(OPTION_CommandLineTool_Csv, null,
                              "Save statistics in CSV file", "-csv",
                              FileOption.Type.OUTPUT_FILE));
      register(new FileOption(OPTION_CommandLineTool_Properties, null,
                              "Read options from properties file", "-p",
                              FileOption.Type.INPUT_FILE));
      register(new BooleanOption(OPTION_CommandLineTool_Quiet, null,
                                 "Suppress all log output",
                                 "+quiet|+q", false));
      register(new BooleanOption(OPTION_CommandLineTool_Stats, null,
                                 "Print statistics", "+stats", false));
      register(new PositiveIntOption(OPTION_CommandLineTool_Timeout, null,
                                     "Maximum allowed runtime in seconds",
                                     "-timeout"));
      register(new BooleanOption(OPTION_CommandLineTool_Verbose, null,
                                 "Verbose log output",
                                 "+verbose|+v", false));
      register(new FileOption(OPTION_CommandLineTool_Xml, null,
                              "Save output data (counterexample or " +
                              "supervisor) to XML file", "-o",
                              FileOption.Type.OUTPUT_FILE));
    }
  }


  //#########################################################################
  //# Inner Class JavaOptionPage
  private class JavaOptionPage
    extends SimpleLeafOptionPage
  {
    //#######################################################################
    //# Constructor
    public JavaOptionPage()
    {
      super("java", "Java VM");
      register(new BooleanOption(OPTION_Java_Assertions, null,
                                 "Enable assertions for debugging",
                                 "+ea|+enableassertions", false));
      register(new BooleanOption(OPTION_Java_Heap, null,
                                 "Maximum heap memory, e.g., -Xmx2048m or -Xmx4g",
                                 "+Xmx<memory>", false));
      register(new BooleanOption(OPTION_Java_Stack, null,
                                 "Maximum stack memory, e.g., -Xms1024m",
                                 "+Xms<memory>", false));
    }
  }


  //#########################################################################
  //# Data Members
  private final Map<String,CommandLineArgument> mArgumentMap;
  private final List<ArgumentSource> mArgumentSources;

  private ProductDESProxy mDES;


  //#########################################################################
  //# Class Constants
  public static final String OPTION_CommandLineTool_Csv =
    "CommandLineTool.Csv";
  public static final String OPTION_CommandLineTool_Properties =
    "CommandLineTool.Properties";
  public static final String OPTION_CommandLineTool_Quiet =
    "CommandLineTool.Quiet";
  public static final String OPTION_CommandLineTool_Stats =
    "CommandLineTool.Stats";
  public static final String OPTION_CommandLineTool_Timeout =
    "CommandLineTool.Timeout";
  public static final String OPTION_CommandLineTool_Verbose =
    "CommandLineTool.Verbose";
  public static final String OPTION_CommandLineTool_Xml =
    "CommandLineTool.Xml";

  public static final String OPTION_Java_Assertions = "Java.Assertions";
  public static final String OPTION_Java_Heap = "Java.Heap";
  public static final String OPTION_Java_Stack = "Java.Stack";

}
