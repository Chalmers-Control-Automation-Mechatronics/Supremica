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

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityDiagnostics;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class OneUncontrollableChecker
  extends AbstractSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public OneUncontrollableChecker(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final ControllabilityChecker checker)
  {
    super(model,
          ControllabilityKindTranslator.getInstance(),
          ControllabilityDiagnostics.getInstance(),
          factory);
    mChecker = checker;
    mStates = 0;
    setNodeLimit(5000000);
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      mStates = 0;
      final List<EventProxy> uncontrollables = new ArrayList<EventProxy>();
      for (final EventProxy event : getModel().getEvents()) {
        if (getKindTranslator().getEventKind(event) ==
          EventKind.UNCONTROLLABLE) {
          uncontrollables.add(event);
        }
      }
      Collections.sort(uncontrollables);
      for (final EventProxy event : uncontrollables) {
        mChecker.setModel(getModel());
        mChecker.setKindTranslator(new KindTranslator()
        {
          @Override
          public EventKind getEventKind(final EventProxy e)
          {
            return e.equals(event) ? EventKind.UNCONTROLLABLE
              : EventKind.CONTROLLABLE;
          }

          @Override
          public ComponentKind getComponentKind(final AutomatonProxy a)
          {
            if (getKindTranslator().getComponentKind(a) == ComponentKind.SPEC) {
              if (!a.getEvents().contains(event)) {
                return ComponentKind.PLANT;
              }
            }
            return getKindTranslator().getComponentKind(a);
          }
        });
        mChecker.setNodeLimit(getNodeLimit()/* - mStates*/);
        if (!mChecker.run()) {
          mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
          return setFailedResult(mChecker.getCounterExample());
        }
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      }
      return setSatisfiedResult();
    } catch (final OutOfMemoryError error) {
      System.gc();
      final AnalysisException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(mStates);
  }


  //#########################################################################
  //# Data Members
  private final ControllabilityChecker mChecker;
  private int mStates;

}
