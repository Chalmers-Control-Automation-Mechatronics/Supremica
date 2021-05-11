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

package net.sourceforge.waters.analysis.options;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * A configurable parameter to specify a chained model analyser.
 *
 * @author Robi Malik
 */

public class ChainedAnalyzerOption extends EnumOption<ChainedAnalyzerFactory>
{

  //#########################################################################
  //# Constructor
  public ChainedAnalyzerOption(final String id,
                               final String shortName,
                               final String description,
                               final String commandLineOption,
                               final AnalysisOptionPage parentPage,
                               final ModelAnalyzerFactoryLoader parentLoader,
                               final Collection<String> overridden)
  {
    super(id, shortName, description, commandLineOption,
          createEnumFactory(parentPage, parentLoader));
    mOverriddenIDs = new THashSet<>(overridden);
    for (final ChainedAnalyzerFactory value : getEnumConstants()) {
      final OptionPage page = createChainedOptionPage(parentPage, value);
      value.setOptionPage(page);
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.options.Option<ModelAnalyzer>
  @Override
  public OptionEditor<ChainedAnalyzerFactory>
  createEditor(final OptionContext context)
  {
    // TODO Auto-generated method stub
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static EnumFactory<ChainedAnalyzerFactory>
  createEnumFactory(final AnalysisOptionPage parentPage,
                    final ModelAnalyzerFactoryLoader parentLoader)
  {
    final AnalysisOperation operation = parentPage.getAnalysisOperation();
    final EnumOption<ModelAnalyzerFactoryLoader> selector =
      parentPage.getTopSelectorOption();
    final ListedEnumFactory<ChainedAnalyzerFactory> result =
      new ListedEnumFactory<>();
    boolean first = true;
    for (final ModelAnalyzerFactoryLoader loader : selector.getEnumConstants()) {
      if (loader != ModelAnalyzerFactoryLoader.Disabled &&
          loader != parentLoader) {
        final ChainedAnalyzerFactory enumConstant =
          new ChainedAnalyzerFactory(operation, loader);
        final boolean isDefault =
          first || loader == ModelAnalyzerFactoryLoader.DEFAULT;
        result.register(enumConstant, isDefault);
        first = false;
      }
    }
    return result;
  }

  private OptionPage createChainedOptionPage
    (final OptionPage parent,
     final ChainedAnalyzerFactory value)
  {
    final String name = value.getName();
    final String prefix = parent.getPrefix() + "." + name;
    return new ChainedOptionPage(prefix, name);
  }


  //#########################################################################
  //# Inner Class ChainedOptionPage
  private class ChainedOptionPage extends LeafOptionPage
  {
    //#######################################################################
    //# Constructor
    public ChainedOptionPage(final String prefix, final String name)
    {
      super(prefix, name);
      try {
        final ChainedAnalyzerFactory cFactory = getValue();
        final ModelAnalyzerFactory aFactory =
          cFactory.getModelAnalyzerFactory();
        aFactory.registerOptions(this);
      } catch (final ClassNotFoundException exception) {
        // OK, no options then ...
      }
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.analysis.options.OptionPage
    @Override
    public void register(final Option<?> option)
    {
      final String id = option.getID();
      if (!mOverriddenIDs.contains(id)) {
        super.register(option);
      }
    }

    @Override
    public OptionPageEditor<ChainedOptionPage>
    createEditor(final OptionContext context)
    {
      // TODO Auto-generated method stub
      return null;
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.analysis.options.LeafOptionPage
    @Override
    public List<Option<?>> getOptions()
    {
      try {
        final ChainedAnalyzerFactory cFactory = getValue();
        final ModelAnalyzerFactory aFactory =
          cFactory.getModelAnalyzerFactory();
        final AnalysisOperation operation = cFactory.getAnalysisOperation();
        final ProductDESProxyFactory desFactory =
          ProductDESElementFactory.getInstance();
        final ModelAnalyzer analyzer =
          operation.createModelAnalyzer(aFactory, desFactory);
        assert analyzer != null;
        return analyzer.getOptions(this);
      } catch (AnalysisConfigurationException |
               ClassNotFoundException exception) {
        return Collections.emptyList();
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final Set<String> mOverriddenIDs;

}
