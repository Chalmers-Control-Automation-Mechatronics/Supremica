//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.cpp.analysis;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ControlLoopChecker;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A monolithic control-loop check implementation, written in C++.
 * The native control-loop checker implements the standard {@link
 * ControlLoopChecker} interface and determines whether or not a given input
 * model is <I>control-loop free</I>.</P>
 *
 * @author Robi Malik
 */

public class NativeControlLoopChecker
  extends NativeModelVerifier
  implements ControlLoopChecker
{

  //#########################################################################
  //# Constructors
  public NativeControlLoopChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeControlLoopChecker(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory)
  {
    super(model, factory, ControllabilityKindTranslator.getInstance());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ControlLoopChecker
  @Override
  public LoopTraceProxy getCounterExample()
  {
    return (LoopTraceProxy) super.getCounterExample();
  }

  @Override
  public Collection<EventProxy> getNonLoopEvents()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Native Methods
  @Override
  native VerificationResult runNativeAlgorithm();

  @Override
  public String getTraceName()
  {
    return getModel().getName() + "-loop";
  }

}
