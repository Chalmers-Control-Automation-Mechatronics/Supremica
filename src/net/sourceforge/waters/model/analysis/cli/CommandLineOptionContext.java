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

package net.sourceforge.waters.model.analysis.cli;

import java.awt.Color;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.sourceforge.waters.analysis.options.AggregatorOptionPage;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.ChainOption;
import net.sourceforge.waters.analysis.options.ColorOption;
import net.sourceforge.waters.analysis.options.ComponentKindOption;
import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.DoubleOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.LeafOptionPage;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionContext;
import net.sourceforge.waters.analysis.options.OptionEditor;
import net.sourceforge.waters.analysis.options.OptionPageEditor;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.analysis.options.SelectorLeafOptionPage;
import net.sourceforge.waters.analysis.options.SimpleLeafOptionPage;
import net.sourceforge.waters.analysis.options.StringListOption;
import net.sourceforge.waters.analysis.options.StringOption;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

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
    mArgumentSources = new Stack<ArgumentSource>();
    mConfigurables = new Stack<Configurable>();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.options.OptionContext
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
  public OptionEditor<ComponentKind> createComponentKindEditor(final ComponentKindOption option)
  {
    return null;
  }

  @Override
  public <E> OptionEditor<E> createEnumEditor(final EnumOption<E> option)
  {
    return new EnumCommandLineArgument<E>(option);
  }

  @Override
  public OptionEditor<Set<EventProxy>> createEventSetEditor(final EventSetOption option)
  {
    return null;
  }

  @Override
  public OptionEditor<File> createFileEditor(final FileOption option)
  {
    return new FileCommandLineArgument(option);
  }

  @Override
  public OptionEditor<Integer> createPositiveIntEditor(final PositiveIntOption option)
  {
    return new PositiveIntCommandLineArgument(option);
  }

  @Override
  public OptionEditor<Double> createDoubleEditor(final DoubleOption option)
  {
    return new DoubleCommandLineArgument(option);
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
  public OptionEditor<Color> createColorEditor(final ColorOption option)
  {
    return null;
  }

  @Override
  public OptionEditor<ModelAnalyzerFactoryLoader> createChainEditor(final ChainOption option)
  {
    return new ChainCommandLineArgument(this, option);
  }

  @Override
  public OptionPageEditor<SimpleLeafOptionPage> createSimpleLeafOptionPageEditor(final SimpleLeafOptionPage page)
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
  public OptionPageEditor<AggregatorOptionPage> createAggregatorOptionPageEditor(final AggregatorOptionPage page)
  {
    return null;
  }


  //#########################################################################
  //# Specific Access
  public void setProductDES(final ProductDESProxy des)
    throws AnalysisException
  {
    mDES = des;
    for (final CommandLineArgument<?> arg : mArgumentMap.values()) {
      arg.updateContext(this);
    }
  }

  public void addArgument(final CommandLineArgument<?> argument)
  {
    for (final String name : argument.getKeys()) {
      mArgumentMap.put(name, argument);
    }
    mArgumentList.add(argument);
  }

  public void parse(final ListIterator<String> iter)
    throws AnalysisException
  {
    mArgumentMap.clear();
    mArgumentList.clear();
    final LeafOptionPage page = new SimpleLeafOptionPage(null, null) {
      @Override
      public List<Option<?>> getOptions()
      {
        return null;
      }
    };
    for (final ArgumentSource argumentSource : mArgumentSources) {
      for (final Configurable configurable : mConfigurables) {
        argumentSource.addArguments(this, configurable, page);
      }
    }
    while (iter.hasNext()) {
      final String name = iter.next();
      final CommandLineArgument<?> arg = mArgumentMap.get(name);
      if (arg != null) {
        arg.parse(this, mConfigurables, iter);
      } else if (name.startsWith("-")) {
        System.err.println("Unsupported option " + name +
                           ". Try -help to see available options.");
        System.exit(1);
      }
    }
    checkRequiredArguments();
  }

  public void generateArgumentsFromOptions(final LeafOptionPage page,
                                           final Configurable source,
                                           final String... requiredOptions)
  {
    final List<Option<?>> options = source.getOptions(page);
    for (final Option<?> option : options) {
      final CommandLineArgument<?> arg =
        (CommandLineArgument<?>) option.createEditor(this);
      if (arg == null) {
        continue;
      }
      for (final String id : requiredOptions) {
        if (option.getID().equals(id)) {
          arg.setRequired(true);
          break;
        }
      }
      addArgument(arg);
    }
  }

  public Map<String, CommandLineArgument<?>> getArgumentMap()
  {
    return mArgumentMap;
  }

  public void addArgumentSource(final ArgumentSource source)
  {
    mArgumentSources.push(source);
  }

  public Stack<ArgumentSource> getArgumentSources()
  {
    return mArgumentSources;
  }

  public void addConfigurable(final Configurable configurable)
  {
    mConfigurables.push(configurable);
  }

  public Stack<Configurable> getConfigurables()
  {
    return mConfigurables;
  }

  public void showHelpMessage(final PrintStream stream)
  {
    final List<String> keys = new ArrayList<>(mArgumentMap.keySet());
    Collections.sort(keys);
    for (final String key : keys) {
      final CommandLineArgument<?> arg = mArgumentMap.get(key);
      if (arg.isPrimaryKey(key)) {
        arg.dump(stream);
      }
    }
  }

  public void configure(final Configurable configurable)
  {
    for (final CommandLineArgument<?> arg : mArgumentList) {
      if (arg.isUsed()) {
        configurable.setOption(arg.getOption());
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void checkRequiredArguments()
  {
    for (final CommandLineArgument<?> arg : mArgumentMap.values()) {
      if (arg.isRequired() && !arg.isUsed()) {
        final String msg ="Required argument " + arg.getCommandLineCode() + " not specified!";
        CommandLineArgument.fail(msg);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final Map<String, CommandLineArgument<?>> mArgumentMap =
    new HashMap<>(64);
  private final List<CommandLineArgument<?>> mArgumentList =
    new ArrayList<>(64);

  private final Stack<ArgumentSource> mArgumentSources;
  private final Stack<Configurable> mConfigurables;

  private ProductDESProxy mDES;

}
