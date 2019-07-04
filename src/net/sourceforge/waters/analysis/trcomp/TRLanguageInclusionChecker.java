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

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>The compositional language inclusion check algorithm.</P>

 * <P>This is the front-end for the compositional language inclusion check
 * algorithm. The checker supports arbitrary input models with any
 * number of properties.</P>
 *
 * <P>The internal representation of automata is based on list buffer
 * transition relations through the {@link TRAutomatonProxy} class. Input
 * models that are not in this form are converted. Multi-property models
 * are verified by creating a sequence of one-property models that are
 * passed to {@link TRCompositionalOnePropertyChecker}.</P>
 *
 * <P><I>References:</I><BR>
 * Simon Ware, Robi Malik. The use of language projection for compositional
 * verification of discrete event systems. Proc. 9th International Workshop
 * on Discrete Event Systems (WODES'08), 322-327, G&ouml;teborg, Sweden,
 * 2008.</P>
 *
 * @author Robi Malik
 */

public class TRLanguageInclusionChecker
  extends AbstractTRDelegatingSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public TRLanguageInclusionChecker()
  {
    this(null);
  }

  public TRLanguageInclusionChecker(final ProductDESProxy model)
  {
    this(model,
         LanguageInclusionKindTranslator.getInstance(),
         LanguageInclusionDiagnostics.getInstance());
  }

  public TRLanguageInclusionChecker
    (final ProductDESProxy model,
     final KindTranslator translator,
     final SafetyDiagnostics diag)
  {
    super(model, translator, diag);
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final Logger logger = LogManager.getLogger();
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();
    int numPlants = 0;
    int numSpecs = 0;
    for (final AutomatonProxy aut : des.getAutomata()) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
        numPlants++;
        break;
      case SPEC:
        numSpecs++;
        break;
      default:
        break;
      }
    }
    if (numSpecs == 0) {
      logger.debug("Did not find any properties to check, returning TRUE.");
      setSatisfiedResult();
      return;
    }
    mPlants = new ArrayList<>(numPlants);
    mProperties = new ArrayList<>(numSpecs);
    for (final AutomatonProxy aut : des.getAutomata()) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
        mPlants.add(aut);
        break;
      case SPEC:
        mProperties.add(aut);
        break;
      default:
        break;
      }
    }
    final TRCompositionalOnePropertyChecker delegate = getDelegate();
    mOnePropertyKindTranslator = new OnePropertyKindTranslator(translator);
    delegate.setKindTranslator(mOnePropertyKindTranslator);
    delegate.setPreservingEncodings(isPreservingEncodings());
  }

  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      final VerificationResult result = getAnalysisResult();
      if (result.isFinished()) {
        return result.isSatisfied();
      }
      final Logger logger = LogManager.getLogger();
      final ProductDESProxyFactory factory = getFactory();
      final TRCompositionalOnePropertyChecker delegate = getDelegate();
      final ProductDESProxy des = getModel();
      final String name = des.getName();
      final Set<EventProxy> events = des.getEvents();
      final int numPlants = mPlants.size();
      final int numAutomata = numPlants + 1;
      final List<AutomatonProxy> automata = new ArrayList<>(numAutomata);
      automata.addAll(mPlants);
      for (final AutomatonProxy property : mProperties) {
        final String propName = property.getName();
        logger.debug("Checking property {} ...", propName);
        mOnePropertyKindTranslator.setProperty(property);
        final String comment =
          "Generated by + " + ProxyTools.getShortClassName(this) + " from " +
          name + " to check property " + propName + ".";
        automata.add(property);
        final ProductDESProxy propDES =
          factory.createProductDESProxy(name, comment, null, events, automata);
        delegate.setModel(propDES);
        delegate.run();
        final VerificationResult propResult = delegate.getAnalysisResult();
        result.merge(propResult);
        if (!propResult.isSatisfied()) {
          final TRSafetyTraceProxy trace =
            (TRSafetyTraceProxy) propResult.getCounterExample();
          trace.setProductDES(des);
          result.setCounterExample(trace);
          return false;
        }
        automata.remove(numPlants);
      }
      return setSatisfiedResult();
    } catch (final AnalysisException exception) {
      setExceptionResult(exception);
      throw exception;
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException exception = new OverflowException(error);
      setExceptionResult(exception);
      throw exception;
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      setExceptionResult(exception);
      throw exception;
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mPlants = mProperties = null;
    mOnePropertyKindTranslator = null;
  }


  //#########################################################################
  //# Inner Class OnePropertyKindTranslator
  private static class OnePropertyKindTranslator implements KindTranslator
  {
    //#######################################################################
    //# Constructor
    private OnePropertyKindTranslator(final KindTranslator parent)
    {
      mParent = parent;
    }

    //#######################################################################
    //# Simple Access
    private void setProperty(final AutomatonProxy property)
    {
      mProperty = property;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mProperty) {
        return ComponentKind.SPEC;
      } else {
        return mParent.getComponentKind(aut);
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      return mParent.getEventKind(event);
    }

    //#######################################################################
    //# Data Members
    private final KindTranslator mParent;
    private AutomatonProxy mProperty;
  }


  //#########################################################################
  //# Data Members
  private List<AutomatonProxy> mPlants;
  private List<AutomatonProxy> mProperties;
  private OnePropertyKindTranslator mOnePropertyKindTranslator;

}
