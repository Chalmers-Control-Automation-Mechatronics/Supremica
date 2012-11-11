//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters HISC
//# PACKAGE: net.sourceforge.waters.analysis.hisc
//# CLASS:   HISCCPInterfaceConsistencyChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.sourceforge.waters.analysis.annotation.TRConflictPreorderChecker;
import net.sourceforge.waters.analysis.compositional.CompositionalSimplificationResult;
import net.sourceforge.waters.analysis.compositional.CompositionalSimplifier;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

public class HISCCPInterfaceConsistencyChecker extends AbstractModelVerifier
{

  //#########################################################################
  //# Constructors
  public HISCCPInterfaceConsistencyChecker(final ProductDESProxyFactory factory)
  {
    this(factory, ConflictKindTranslator.getInstance());
  }

  public HISCCPInterfaceConsistencyChecker(final ProductDESProxyFactory factory,
                                           final KindTranslator translator)
  {
    this(null, factory, translator);
  }

  public HISCCPInterfaceConsistencyChecker(final ProductDESProxy model,
                                           final ProductDESProxyFactory factory,
                                           final KindTranslator translator)
  {
    super(model, factory, translator);
    mSimplifier = new CompositionalSimplifier(factory);
    mSynchronousProductBuilder =
      new MonolithicSynchronousProductBuilder(factory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
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
    mSimplifier.setKindTranslator(translator);
    mSynchronousProductBuilder.setKindTranslator(translator);
    if (mConfiguredMarking == null) {
      final ProductDESProxy des = getModel();
      mUsedMarking = AbstractConflictChecker.getMarkingProposition(des);
    } else {
      mUsedMarking = mConfiguredMarking;
    }
    mSimplifier.setConfiguredDefaultMarking(mUsedMarking);
    final Collection<EventProxy> props = Collections.singletonList(mUsedMarking);
    mSynchronousProductBuilder.setPropositions(props);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();

      final ProductDESProxy des = getModel();
      final Collection<EventProxy> events = des.getEvents();
      final Collection<EventProxy> interfaceEvents = new ArrayList<EventProxy>();
      final KindTranslator translator = getKindTranslator();
      final EventEncoding eventEnc = new EventEncoding();
      for (final EventProxy event : events) {
        final Map<String,String> attribs = event.getAttributes();
        if (translator.getEventKind(event) == EventKind.PROPOSITION) {
          // nothing
        } else if (HISCAttributeFactory.getEventType(attribs) ==
                   HISCAttributeFactory.EventType.DEFAULT) {
          eventEnc.addSilentEvent(event);
        } else {
          interfaceEvents.add(event);
          eventEnc.addEvent(event, translator, true);
        }
      }
      final int markingID = eventEnc.addEvent(mUsedMarking, translator, true);

      final Collection<AutomatonProxy> automata = des.getAutomata();
      final Collection<AutomatonProxy> interfaces =
        new ArrayList<AutomatonProxy>();
      Collection<AutomatonProxy> subsystem =
        new ArrayList<AutomatonProxy>(automata.size());
      for (final AutomatonProxy aut : automata) {
        final Map<String,String> attribs = aut.getAttributes();
        if (HISCAttributeFactory.isInterface(attribs)) {
          interfaces.add(aut);
        } else {
          subsystem.add(aut);
        }
      }

      final ProductDESProxyFactory factory = getFactory();
      final String name = des.getName();
      final ProductDESProxy interfaceDES =
        AutomatonTools.createProductDESProxy(name + ":iface",
                                             interfaces, factory);
      ProductDESProxy subsystemDES =
        AutomatonTools.createProductDESProxy(name + ":subsystem",
                                             subsystem, factory);
      if (mSimplifier != null) {
        mSimplifier.setModel(subsystemDES);
        mSimplifier.setPreservedEvents(interfaceEvents);
        mSimplifier.run();
        final CompositionalSimplificationResult result =
          mSimplifier.getAnalysisResult();
        subsystemDES = result.getComputedProductDES();
        subsystem = subsystemDES.getAutomata();
        for (final EventProxy event : subsystemDES.getEvents()) {
          if (eventEnc.getEventCode(event) < 0) {
            eventEnc.addSilentEvent(event);
          }
        }
      }

      final ListBufferTransitionRelation interfaceRel =
        createTransitionRelation(interfaceDES, eventEnc);
      final ListBufferTransitionRelation subsystemRel =
        createTransitionRelation(subsystemDES, eventEnc);
      final TRConflictPreorderChecker checker =
        new TRConflictPreorderChecker(subsystemRel, interfaceRel, markingID);
      final boolean lc = checker.isLessConflicting();
      final VerificationResult result = getAnalysisResult();
      result.setSatisfied(lc);
      return lc;
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    mUsedMarking = null;
    super.tearDown();
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
    return new ListBufferTransitionRelation(aut, eventEnc, config);
  }


  //#########################################################################
  //# Data Members
  private EventProxy mConfiguredMarking;
  private EventProxy mUsedMarking;

  private CompositionalSimplifier mSimplifier;
  private final SynchronousProductBuilder mSynchronousProductBuilder;

}
