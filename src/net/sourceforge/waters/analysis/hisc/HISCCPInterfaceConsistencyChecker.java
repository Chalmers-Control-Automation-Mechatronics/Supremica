//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.hisc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.sourceforge.waters.analysis.annotation.ConflictPreorderResult;
import net.sourceforge.waters.analysis.annotation.TRConflictPreorderChecker;
import net.sourceforge.waters.analysis.compositional.CompositionalConflictChecker;
import net.sourceforge.waters.analysis.compositional.CompositionalSimplificationResult;
import net.sourceforge.waters.analysis.compositional.CompositionalSimplifier;
import net.sourceforge.waters.analysis.compositional.ConflictAbstractionProcedureFactory;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class HISCCPInterfaceConsistencyChecker extends AbstractModelVerifier
{

  //#########################################################################
  //# Constructors
  public HISCCPInterfaceConsistencyChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public HISCCPInterfaceConsistencyChecker(final ProductDESProxyFactory factory,
                                           final ConflictChecker checker,
                                           final CompositionalSimplifier simplifier)
  {
    this(null, factory, checker, simplifier);
  }

  public HISCCPInterfaceConsistencyChecker(final ProductDESProxy model,
                                           final ProductDESProxyFactory factory)
  {
    this(model, factory,
         new CompositionalConflictChecker(factory),
         new CompositionalSimplifier(factory,
                                     ConflictAbstractionProcedureFactory.OEQ));
  }

  public HISCCPInterfaceConsistencyChecker(final ProductDESProxy model,
                                           final ProductDESProxyFactory factory,
                                           final ConflictChecker checker,
                                           final CompositionalSimplifier simplifier)
  {
    super(model, factory, HISCConflictKindTranslator.getInstance());
    mReversed = false;
    mConflictChecker = checker;
    mSimplifier = simplifier;
    mSynchronousProductBuilder =
      new MonolithicSynchronousProductBuilder(factory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public void setModel(final ProductDESProxy model)
  {
    super.setModel(model);
    mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifier
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Specific Configuration
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredMarking = marking;
    mUsedMarking = null;
  }

  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredMarking;
  }

  /**
   * Sets whether the conflict preorder check should be reversed.
   * If set to <CODE>true</CODE>, it will be checked whether the interface
   * is less conflicting (instead of more conflicting) than the subsystem.
   * The reversed check has no real application, and is only used to
   * measure performance of the conflict preorder check. The default is
   * <CODE>false</CODE>.
   */
  public void setReversed(final boolean reverse)
  {
    mReversed = reverse;
  }

  /**
   * Returns whether the conflict preorder check is revered.
   * @see #setReversed(boolean) setReversed()
   */
  public boolean isReversed()
  {
    return mReversed;
  }

  public void setConflictChecker(final ConflictChecker checker)
  {
    mConflictChecker = checker;
  }

  public ConflictChecker getConflictChecker()
  {
    return mConflictChecker;
  }

  public void setSimplifier(final CompositionalSimplifier simplifier)
  {
    mSimplifier = simplifier;
  }

  public CompositionalSimplifier getSimplifier()
  {
    return mSimplifier;
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final KindTranslator translator = getKindTranslator();
    mConflictChecker.setKindTranslator(translator);
    mSimplifier.setKindTranslator(translator);
    mSynchronousProductBuilder.setKindTranslator(translator);
    if (mConfiguredMarking == null) {
      final ProductDESProxy des = getModel();
      mUsedMarking = AbstractConflictChecker.findMarkingProposition(des);
    } else {
      mUsedMarking = mConfiguredMarking;
    }
    mConflictChecker.setConfiguredDefaultMarking(mUsedMarking);
    mSimplifier.setConfiguredDefaultMarking(mUsedMarking);
    final Collection<EventProxy> props = Collections.singletonList(mUsedMarking);
    mSynchronousProductBuilder.setPropositions(props);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();

      // Collect interface events ...
      final ProductDESProxy des = getModel();
      final Collection<EventProxy> events = des.getEvents();
      final Collection<EventProxy> interfaceEvents = new ArrayList<>();
      final KindTranslator translator = getKindTranslator();
      final EventEncoding eventEnc = new EventEncoding();
      boolean hasInterface = false;
      for (final EventProxy event : events) {
        final Map<String,String> attribs = event.getAttributes();
        if (translator.getEventKind(event) == EventKind.PROPOSITION) {
          // nothing
        } else if (HISCAttributeFactory.isParameter(attribs)) {
          interfaceEvents.add(event);
          eventEnc.addEvent(event, translator, EventStatus.STATUS_NONE);
          hasInterface = true;
        } else {
          eventEnc.addSilentEvent(event);
        }
      }
      final int markingID =
        eventEnc.addEvent(mUsedMarking, translator, EventStatus.STATUS_NONE);
      checkAbort();

      // Separate into interface and subsystem automata ...
      final Collection<AutomatonProxy> automata = des.getAutomata();
      final Collection<AutomatonProxy> interfaces =
        new ArrayList<AutomatonProxy>();
      Collection<AutomatonProxy> subsystem =
        new ArrayList<AutomatonProxy>(automata.size());
      for (final AutomatonProxy aut : automata) {
        final Map<String,String> attribs = aut.getAttributes();
        if (HISCAttributeFactory.isInterface(attribs)) {
          interfaces.add(aut);
          hasInterface = true;
        } else if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
          subsystem.add(aut);
        }
      }
      checkAbort();

      // If there is no interface, just run a conflict check ...
      if (!hasInterface) {
        mConflictChecker.setModel(des);
        mConflictChecker.run();
        final VerificationResult result = mConflictChecker.getAnalysisResult();
        setAnalysisResult(result);
        return result.isSatisfied();
      }

      // If there is an interface, we must check the conflict preorder ...
      final HISCCPVerificationResult hiscResult =
        new HISCCPVerificationResult(this);
      setAnalysisResult(hiscResult);
      final ProductDESProxyFactory factory = getFactory();
      final String name = des.getName();
      final ProductDESProxy interfaceDES =
        AutomatonTools.createProductDESProxy(name + ":iface",
                                             interfaces, factory);
      ProductDESProxy subsystemDES =
        AutomatonTools.createProductDESProxy(name + ":subsystem",
                                             subsystem, factory);
      // Minimise the subsystem ...
      if (mSimplifier != null) {
        mSimplifier.setModel(subsystemDES);
        mSimplifier.setPreservedEvents(interfaceEvents);
        mSimplifier.run();
        final CompositionalSimplificationResult result =
          mSimplifier.getAnalysisResult();
        hiscResult.addSimplificationResult(result);
        subsystemDES = result.getComputedProductDES();
        subsystem = subsystemDES.getAutomata();
        for (final EventProxy event : subsystemDES.getEvents()) {
          if (eventEnc.getEventCode(event) < 0) {
            eventEnc.addSilentEvent(event);
          }
        }
      }
      checkAbort();

      // Compose the interface and subsystem to single transition relations ...
      final ListBufferTransitionRelation interfaceRel =
        createTransitionRelation(interfaceDES, eventEnc);
      final ListBufferTransitionRelation subsystemRel =
        createTransitionRelation(subsystemDES, eventEnc);

      // Check the conflict preorder ...
      if (!mReversed) {
        mConflictPreorderChecker =
          new TRConflictPreorderChecker(subsystemRel, interfaceRel, markingID);
      } else {
        mConflictPreorderChecker =
          new TRConflictPreorderChecker(interfaceRel, subsystemRel, markingID);
      }
      final boolean lc = mConflictPreorderChecker.isLessConflicting();
      hiscResult.setSatisfied(lc);
      final ConflictPreorderResult result =
        mConflictPreorderChecker.getAnalysisResult();
      hiscResult.addConflictPreorderResult(result);
      return lc;
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    mUsedMarking = null;
    mConflictPreorderChecker = null;
    super.tearDown();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    mConflictChecker.requestAbort();
    if (mSimplifier != null) {
      mSimplifier.requestAbort();
    }
    if (mConflictPreorderChecker != null) {
      mConflictPreorderChecker.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mConflictChecker.resetAbort();
    if (mSimplifier != null) {
      mSimplifier.resetAbort();
    }
    if (mConflictPreorderChecker != null) {
      mConflictPreorderChecker.resetAbort();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private ListBufferTransitionRelation createTransitionRelation
    (final ProductDESProxy des, final EventEncoding eventEnc)
    throws AnalysisException
  {
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final AutomatonProxy aut;
    if (automata.size() == 1) {
      aut = automata.iterator().next();
    } else {
      mSynchronousProductBuilder.setModel(des);
      mSynchronousProductBuilder.run();
      aut = mSynchronousProductBuilder.getComputedAutomaton();
    }
    final int config = TRConflictPreorderChecker.getPreferredInputConfiguration();
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut, eventEnc, config);
    checkAbort();
    return rel;
  }


  //#########################################################################
  //# Data Members
  private EventProxy mConfiguredMarking;
  private EventProxy mUsedMarking;
  private boolean mReversed;

  private ConflictChecker mConflictChecker;
  private CompositionalSimplifier mSimplifier;
  private final SynchronousProductBuilder mSynchronousProductBuilder;
  private TRConflictPreorderChecker mConflictPreorderChecker;

}
