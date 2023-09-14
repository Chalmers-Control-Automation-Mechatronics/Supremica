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

package net.sourceforge.waters.analysis.modular;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.coobs.CoobservabilityAttributeFactory;
import net.sourceforge.waters.analysis.coobs.CoobservabilityDiagnostics;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CoobservabilityCounterExampleProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;


/**
 * <P>The modular coobservability check algorithm,
 * implemented based on {@link AbstractModularVerifier}.</P>
 *
 * <P><I>Reference:</I><BR>
 * Huailiang Liu, Ryan Leduc, Robi Malik, S. Laurie Ricker.
 * Incremental verification of co-observability in discrete-event systems.
 * 2014 American Control Conference (ACC'14), 5446&ndash;5452, 2014.</P>
 *
 * @author Robi Malik
 */

public class ModularCoobservabilityChecker
  extends AbstractModularVerifier
  implements CoobservabilityChecker
{

  //#########################################################################
  //# Constructors
  public ModularCoobservabilityChecker(final ProductDESProxyFactory factory,
                                       final CoobservabilityChecker mono)
  {
    this(null, factory, mono);
  }

  public ModularCoobservabilityChecker(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final CoobservabilityChecker mono)
  {
    super(model, factory, ControllabilityKindTranslator.getInstance(), mono);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.analysis.des.CoobservabilityChecker
  @Override
  public void setDefaultSiteName(final String name)
  {
    mDefaultSiteName = "".equals(name) ? null : name;
  }

  @Override
  public String getDefaultSiteName()
  {
    return mDefaultSiteName == null ? "" : mDefaultSiteName;
  }

  @Override
  public CoobservabilityCounterExampleProxy getCounterExample()
  {
    return (CoobservabilityCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_CoobservabilityChecker_DefaultSite);
    db.append(options, ModularModelVerifierFactory.
                       OPTION_ModularCoobservabilityChecker_Chain);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_CoobservabilityChecker_DefaultSite)) {
      final String value = (String) option.getValue();
      setDefaultSiteName(value);
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.modular.AbstractModularVerifier
  @Override
  public CoobservabilityChecker getMonolithicVerifier()
  {
    return (CoobservabilityChecker) super.getMonolithicVerifier();
  }

  @Override
  public void setUp()
    throws AnalysisException
  {
    super.setUp();
    final CoobservabilityChecker mono = getMonolithicVerifier();
    mono.setDefaultSiteName(mDefaultSiteName);
  }

  @Override
  protected CoobservabilityCounterExampleProxy createExtendedCounterexample
    (final CounterExampleProxy counter,
     final Collection<AutomatonProxy> newAutomata,
     final List<TraceProxy> newTraces)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String name = CoobservabilityDiagnostics.getDefaultTraceName(des);
    final String comment = counter.getComment();
    return factory.createCoobservabilityCounterExampleProxy
      (name, comment, null, des, newAutomata, newTraces);
  }


  //#########################################################################
  //# Data Members
  private String mDefaultSiteName =
    CoobservabilityAttributeFactory.DEFAULT_SITE_NAME;

}
