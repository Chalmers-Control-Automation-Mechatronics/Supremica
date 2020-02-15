//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

/**
 *
 * @author Benjamin Wheeler
 */
public class AnalysisOptionPage extends SelectorLeafOptionPage
{

  public AnalysisOptionPage(final AnalysisOperation operation)
  {
    super(operation.getOptionPagePrefix(), operation.getAnalysisName());
    final List<ModelAnalyzerFactoryLoader> loaders = getLoaders(operation);
    mAlgorithmOption = new AnalysisAlgorithmOption
      (operation.getOptionPagePrefix()+".AlgorithmSelector", loaders, null);
    add(mAlgorithmOption);
    mOperation = operation;
  }

  public List<ModelAnalyzerFactoryLoader> getLoaders
    (final AnalysisOperation operation)
  {
    final List<ModelAnalyzerFactoryLoader> loaders = new LinkedList<>();

    for (final ModelAnalyzerFactoryLoader loader :
      ModelAnalyzerFactoryLoader.values()) {
      try {
        final ModelAnalyzerFactory factory =
          loader.getModelAnalyzerFactory();
        final ProductDESProxyFactory desFactory =
          ProductDESElementFactory.getInstance();
        final ModelAnalyzer analyzer =
          operation.createModelAnalyzer(factory, desFactory);

        if (analyzer != null) {
          loaders.add(loader);
          factory.registerOptions(this);
        }
      } catch (ClassNotFoundException |
               AnalysisConfigurationException |
               UnsatisfiedLinkError |
               NoClassDefFoundError exception) {
        // skip this factory
      }
    }

    return loaders;
  }

  @Override
  public List<Option<?>> getOptionsForSelector
    (final SelectorOption<?> selectorOption, final Object key)
  {
    final List<Option<?>> options = new LinkedList<>();
    addOptions(options, (ModelAnalyzerFactoryLoader) key);
    return options;
  }

  public void addOptions(final List<Option<?>> options,
                         final ModelAnalyzerFactoryLoader loader)
  {
    try {
      final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final ModelAnalyzer analyzer =
        mOperation.createModelAnalyzer(factory, desFactory);
      for (final Option<?> option : analyzer.getOptions(this)) {
        //Get the option instance known by this OptionPage
        final Option<?> optionInstance = get(option.getID());
        options.add(optionInstance);
      }
    } catch (AnalysisConfigurationException
      | ClassNotFoundException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    }
  }

  @Override
  public SelectorOption<?> getTopSelectorOption()
  {
    return mAlgorithmOption;
  }

  @Override
  public SelectorOption<?> getSubSelector
    (final SelectorOption<?> selectorOption, final Object key)
  {
    return null;
  }

  private final AnalysisAlgorithmOption mAlgorithmOption;
  private final AnalysisOperation mOperation;

}
