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

package net.sourceforge.waters.model.options;

import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * <P>An option page to configure a chained model analyser.</P>
 *
 * @author Robi Malik
 */

public class ChainedAnalyzerOptionPage
  extends SelectorLeafOptionPage<ModelAnalyzerFactoryLoader>
{

  //#########################################################################
  //# Constructors
  public ChainedAnalyzerOptionPage
    (final AnalysisOptionPage parent,
     final EnumOption<ModelAnalyzerFactoryLoader> selector,
     final String... overridden)
  {
    super(parent.getPrefix() + "." + selector.getID(),
          selector.getShortName());
    mParent = parent;
    mOperation = parent.getAnalysisOperation();
    mAlgorithmOption = selector;
    mOverriddenIDs = new THashSet<>(overridden.length);
    for (final String id : overridden) {
      mOverriddenIDs.add(id);
    }
  }

  public ChainedAnalyzerOptionPage
    (final ChainedAnalyzerOptionPage parent,
     final EnumOption<ModelAnalyzerFactoryLoader> selector,
     final ChainedAnalyzerOptionPage template)
  {
    super(parent.getPrefix() + "." + selector.getID(),
          selector.getShortName());
    mParent = parent;
    mOperation = parent.getAnalysisOperation();
    mAlgorithmOption = selector;
    mOverriddenIDs = template.mOverriddenIDs;
  }


  //#########################################################################
  //# Simple Access
  public SelectorLeafOptionPage<ModelAnalyzerFactoryLoader> getParent()
  {
    return mParent;
  }

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
    try {
      final AnalysisOperation operation = getAnalysisOperation();
      final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final ModelAnalyzer analyzer =
        operation.createModelAnalyzer(factory, desFactory);
      final List<Option<?>> analyzerOptions = analyzer.getOptions(this);
      options.addAll(analyzerOptions);
    } catch (final ClassNotFoundException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final AnalysisConfigurationException exception) {
      // can't construct analyser - then no options
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.LeafOptionPage
  @Override
  public String getShortDescription()
  {
    final AnalysisOperation operation = getAnalysisOperation();
    return operation.getShortAnalysisName();
  }

  @Override
  public Option<?> get(final String id)
  {
    final Option<?> option = super.get(id);
    if (option != null) {
      return option;
    } else if (mOverriddenIDs.contains(id)) {
      return null;
    }
    final Option<?> template = mParent.get(id);
    if (template == null) {
      return null;
    }
    final Option<?> cloned = template.clone(this);
    register(cloned);
    return cloned;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.options.OptionPage
  @Override
  public void saveProperties(final Writer writer, final boolean saveAll)
    throws IOException
  {
    for (final Option<?> option : getCurrentOptions()) {
      if (option.isPersistent()) {
        option.save(writer, this, saveAll);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final SelectorLeafOptionPage<ModelAnalyzerFactoryLoader> mParent;
  private final AnalysisOperation mOperation;
  private final EnumOption<ModelAnalyzerFactoryLoader> mAlgorithmOption;
  private final Set<String> mOverriddenIDs;

}
