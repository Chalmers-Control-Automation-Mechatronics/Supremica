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

import java.io.IOException;
import java.io.Writer;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A configurable parameter to specify a chained model analyser.
 *
 * @author Robi Malik
 */

public class ChainedAnalyzerOption
  extends EnumOption<ModelAnalyzerFactoryLoader>
{

  //#########################################################################
  //# Constructors
  public ChainedAnalyzerOption
    (final String id,
     final String shortName,
     final String description,
     final AnalysisOptionPage db,
     final ModelAnalyzerFactoryLoader parentLoader,
     final String... chainSuppressions)
  {
    super(id, shortName, description, "-chain",
          createEnumFactory(db, parentLoader));
    mOptionPage = new ChainedAnalyzerOptionPage(db, this, chainSuppressions);
    mParentLoader = parentLoader;
  }

  private ChainedAnalyzerOption(final ChainedAnalyzerOption template,
                                final ChainedAnalyzerOptionPage parentPage)
  {
    super(template, createEnumFactory(parentPage, template.mParentLoader));
    mOptionPage =
      new ChainedAnalyzerOptionPage(parentPage, this, template.mOptionPage);
    mParentLoader = template.mParentLoader;
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public ChainedAnalyzerOption clone()
  {
    throw new IllegalStateException
      ("ChainedAnalyzerOption cannot be cloned directly.");
    // Use clone(ChainedAnalyzerOptionPage) instead
  }

  @Override
  public ChainedAnalyzerOption clone(final ChainedAnalyzerOptionPage newParent)
  {
    return new ChainedAnalyzerOption(this, newParent);
  }


  //#########################################################################
  //# Simple Access
  public ChainedAnalyzerOptionPage getOptionPage()
  {
    return mOptionPage;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.options.Option<ModelAnalyzerFactoryLoader>
  @Override
  public OptionEditor<ModelAnalyzerFactoryLoader>
  createEditor(final OptionContext context)
  {
    return context.createChainedAnalyzerEditor(this);
  }

  @Override
  public void save(final Writer writer,
                   final LeafOptionPage page,
                   final boolean saveAll)
    throws IOException
  {
    super.save(writer, page, saveAll);
    mOptionPage.saveProperties(writer, saveAll);
  }


  //#########################################################################
  //# Specific Access
  public ModelAnalyzer createAndConfigureModelAnalyzer
    (final ProductDESProxyFactory desFactory)
    throws AnalysisConfigurationException
  {
    final ModelAnalyzerFactoryLoader loader = getValue();
    try {
      final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
      final AnalysisOperation operation = mOptionPage.getAnalysisOperation();
      final ModelAnalyzer analyzer =
        operation.createModelAnalyzer(factory, desFactory);
      for (final Option<?> option : analyzer.getOptions(mOptionPage)) {
        analyzer.setOption(option);
      }
      return analyzer;
    } catch (final ClassNotFoundException exception) {
      throw new AnalysisConfigurationException
        ("Could not create chained analyzer for " + loader.toString() +
         " factory.", exception);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private static EnumFactory<ModelAnalyzerFactoryLoader>
  createEnumFactory(final SelectorLeafOptionPage<ModelAnalyzerFactoryLoader> parentPage,
                    final ModelAnalyzerFactoryLoader parentLoader)
  {
    final EnumOption<ModelAnalyzerFactoryLoader> selector =
      parentPage.getCurrentPageSelectorOption();
    final ListedEnumFactory<ModelAnalyzerFactoryLoader> result =
      new ListedEnumFactory<>();
    for (final ModelAnalyzerFactoryLoader loader :
         selector.getEnumConstants()) {
      if (loader != ModelAnalyzerFactoryLoader.Disabled &&
          loader != parentLoader) {
        result.register(loader, loader == ModelAnalyzerFactoryLoader.DEFAULT);
      }
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final ChainedAnalyzerOptionPage mOptionPage;
  private final ModelAnalyzerFactoryLoader mParentLoader;

}
