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

import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * <P>A common superclass for the modular controllability ({@link
 * ModularControllabilityChecker}) and language inclusion ({@link
 * ModularLanguageInclusionChecker}) checkers.</P>
 *
 * <P>Based on {@link AbstractModularVerifier}, this class only adds the
 * the details needed to handle safety counterexamples ({@link
 * SafetyCounterExampleProxy}).</P>
 *
 * @author Simon Ware, Robi Malik
 */

abstract class AbstractModularSafetyVerifier
  extends AbstractModularVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractModularSafetyVerifier(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final KindTranslator translator,
                                       final SafetyVerifier mono)
  {
    super(model, factory, translator, mono);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.SafetyVerifier
  @Override
  public SafetyCounterExampleProxy getCounterExample()
  {
    return (SafetyCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.modular.AbstractModularVerifier
  @Override
  protected SafetyCounterExampleProxy createExtendedCounterexample
    (final CounterExampleProxy counter,
     final Collection<AutomatonProxy> newAutomata,
     final List<TraceProxy> newTraces)
  {
    assert newTraces.size() == 1;
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String name = getDiagnostics().getTraceName(des);
    final String comment = counter.getComment();
    final TraceProxy trace = newTraces.get(0);
    return factory.createSafetyCounterExampleProxy(name, comment, null, des,
                                                   newAutomata, trace);
  }

}
