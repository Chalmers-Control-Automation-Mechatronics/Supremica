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

package net.sourceforge.waters.model.options;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * <P>An option page to configure an analysis algorithm.</P>
 *
 * <P>The analysis option page is associated with a given analysis task,
 * e.g., conflict check. It consists of a single selector option to
 * choose a {@link ModelAnalyzerFactory}, which each choice leading
 * to the set of options to configure the algorithm provided by the
 * corresponding factory.</P>
 *
 * @author Benjamin Wheeler
 */

public class AnalysisOptionPage
  extends SelectorLeafOptionPage<ModelAnalyzerFactoryLoader>
{

  //#########################################################################
  //# Constructors
  public AnalysisOptionPage(final AnalysisOperation operation)
  {
    this(operation, false);
  }

  public AnalysisOptionPage(final AnalysisOperation operation,
                            final boolean canBeDisabled)
  {
    super(operation.getOptionPagePrefix(), operation.getShortAnalysisName());
    mOperation = operation;
    final EnumFactory<ModelAnalyzerFactoryLoader> enumFactory =
      ModelAnalyzerFactoryLoader.createEnumFactory(operation, canBeDisabled);
    mAlgorithmOption = new EnumOption<ModelAnalyzerFactoryLoader>
      ("Algorithm", "Algorithm", null, null, enumFactory);
    register(mAlgorithmOption);
    registerAnalyzerFactoryOptions();
  }


  //#########################################################################
  //# Simple Access
  public AnalysisOperation getAnalysisOperation()
  {
    return mOperation;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.options.SelectorLeafOptionPage
  @Override
  public EnumOption<ModelAnalyzerFactoryLoader> getTopSelectorOption()
  {
    return mAlgorithmOption;
  }

  @Override
  public void collectOptions(final Collection<Option<?>> options,
                             final ModelAnalyzerFactoryLoader loader)
  {
    if (loader != ModelAnalyzerFactoryLoader.Disabled) {
      try {
        final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
        final ProductDESProxyFactory desFactory =
          ProductDESElementFactory.getInstance();
        final ModelAnalyzer analyzer =
          mOperation.createModelAnalyzer(factory, desFactory);
        final List<Option<?>> analyzerOptions = analyzer.getOptions(this);
        options.addAll(analyzerOptions);
      } catch (AnalysisConfigurationException |
               ClassNotFoundException exception) {
        throw new WatersRuntimeException(exception);
      }
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.LeafOptionPage
  @Override
  public String getShortDescription()
  {
    return mOperation.getShortAnalysisName();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void registerAnalyzerFactoryOptions()
  {
    for (final ModelAnalyzerFactoryLoader loader :
         mAlgorithmOption.getEnumConstants()) {
      try {
        final ModelAnalyzerFactory factory =
          loader.getModelAnalyzerFactory();
        factory.registerOptions(this);
      } catch (ClassNotFoundException |
               UnsatisfiedLinkError |
               NoClassDefFoundError exception) {
        // caught already - should not happen
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final AnalysisOperation mOperation;
  private final EnumOption<ModelAnalyzerFactoryLoader> mAlgorithmOption;

}
