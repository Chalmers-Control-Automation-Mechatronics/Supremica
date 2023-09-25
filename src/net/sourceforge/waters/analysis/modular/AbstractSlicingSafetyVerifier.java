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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.ChainedAnalyzerOption;
import net.sourceforge.waters.model.options.Option;


/**
 * <P>A modular controllability or language inclusion check algorithm that
 * uses one uncontrollable event at a time.</P>
 *
 * <P><I>Reference:</I><BR>
 * Bertil A. Brandin, Robi Malik, Petra Malik. Incremental verification
 * and synthesis of discrete-event systems guided by counter-examples.
 * IEEE Transactions on Control Systems Technology,
 * <STRONG>12</STRONG>&nbsp;(3), 387&ndash;401, 2004.</P>
 *
 * @author Simon Ware, Robi Malik
 */

public abstract class AbstractSlicingSafetyVerifier
  extends AbstractSafetyVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractSlicingSafetyVerifier(final ProductDESProxy model,
                                       final KindTranslator translator,
                                       final SafetyDiagnostics diag,
                                       final ProductDESProxyFactory factory,
                                       final SafetyVerifier nested)
  {
    super(model, translator, diag, factory);
    mNestedVerifier = nested;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(SlicingModelVerifierFactory.
                     OPTION_SlicingControllabilityChecker_Chain) ||
        option.hasID(SlicingModelVerifierFactory.
                     OPTION_SlicingLanguageInclusionChecker_Chain)) {
      try {
        final ChainedAnalyzerOption chain = (ChainedAnalyzerOption) option;
        final ProductDESProxyFactory factory = getFactory();
        mNestedVerifier =
          (SafetyVerifier) chain.createAndConfigureModelAnalyzer(factory);
      } catch (final AnalysisConfigurationException exception) {
        throw new WatersRuntimeException(exception);
      }
    } else {
      super.setOption(option);
    }
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return mNestedVerifier.supportsNondeterminism();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mNestedVerifier != null) {
      mNestedVerifier.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mNestedVerifier != null) {
      mNestedVerifier.resetAbort();
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ProductDESProxy des = getModel();
    mNestedVerifier.setModel(des);
    mNestedVerifier.setNodeLimit(getNodeLimit());
    mNestedVerifier.setTransitionLimit(getTransitionLimit());
    mNestedVerifier.setCounterExampleEnabled(isCounterExampleEnabled());
  }

  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();

      final KindTranslator translator = getKindTranslator();
      final ProductDESProxy des = getModel();
      final Collection<EventProxy> events = des.getEvents();
      final int numEvents = events.size();
      final List<EventProxy> uncontrollables = new ArrayList<>(numEvents);
      for (final EventProxy event : events) {
        if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
          uncontrollables.add(event);
        }
      }
      Collections.sort(uncontrollables);

      mStates = 0;
      for (final EventProxy event : uncontrollables) {
        checkAbort();
        mNestedVerifier.setKindTranslator(new KindTranslator()
        {
          @Override
          public EventKind getEventKind(final EventProxy e)
          {
            if (e == event) {
              return EventKind.UNCONTROLLABLE;
            } else if (e == KindTranslator.INIT) {
              return translator.getEventKind(e);
            } else {
              final EventKind kind = translator.getEventKind(e);
              if (kind == EventKind.UNCONTROLLABLE) {
                return EventKind.CONTROLLABLE;
              } else {
                return kind;  // CONTROLLABLE or PROPOSITION or null
              }
            }
          }

          @Override
          public ComponentKind getComponentKind(final AutomatonProxy a)
          {
            return translator.getComponentKind(a);
          }
        });
        mNestedVerifier.setNodeLimit(getNodeLimit()/* - mStates*/);
        if (!mNestedVerifier.run()) {
          mStates += mNestedVerifier.getAnalysisResult().getTotalNumberOfStates();
          return setFailedResult(mNestedVerifier.getCounterExample());
        }
        mStates += mNestedVerifier.getAnalysisResult().getTotalNumberOfStates();
      }
      return setSatisfiedResult();

    } catch (final OutOfMemoryError error) {
      System.gc();
      final AnalysisException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final AnalysisException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
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
  private SafetyVerifier mNestedVerifier;
  private int mStates;

}
