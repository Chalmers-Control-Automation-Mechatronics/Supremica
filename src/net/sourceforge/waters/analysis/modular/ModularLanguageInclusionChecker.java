//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>The modular language inclusion check algorithm.</P>
 *
 * <P>The modular language inclusion checker is a simple wrapper to
 * split a model with several properties into several models with only
 * one property each. Each model is checked individually by another safety
 * verifier. If one check fails, language inclusion is found to be not
 * satisfied, otherwise it is satisfied.</P>
 *
 * <P>The model verifier checking the individual properties can be
 * configured. It typically is a {@link ModularControllabilityChecker}
 * or {@link ProjectingControllabilityChecker}. The modular language
 * inclusion checker is only useful for models with more than one
 * property; if there is only one property, it will delegate the
 * complete task to the secondary model verifier.</P>
 *
 * @author Simon Ware
 */

public class ModularLanguageInclusionChecker
  extends AbstractModularSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructor
  public ModularLanguageInclusionChecker(final ProductDESProxyFactory factory,
                                         final SafetyVerifier checker)
  {
    this(null, factory, checker);
  }

  public ModularLanguageInclusionChecker(final ProductDESProxy model,
                                         final ProductDESProxyFactory factory,
                                         final SafetyVerifier checker)
  {
    super(model, factory);
    setKindTranslator(LanguageInclusionKindTranslator.getInstance());
    mChecker = checker;
    mStates = 0;
    setNodeLimit(10000000);
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws AnalysisException
  {
    setUp();
    mStates = 0;
    final List<AutomatonProxy> properties = new ArrayList<AutomatonProxy>();
    final Set<AutomatonProxy> automata =
      new HashSet<AutomatonProxy>(getModel().getAutomata().size());
    final KindTranslator translator = getKindTranslator();
    for (final AutomatonProxy automaton : getModel().getAutomata()) {
      switch (translator.getComponentKind(automaton)) {
      case PLANT:
        automata.add(automaton);
        break;
      case SPEC:
        properties.add(automaton);
        break;
      default:
        break;
      }
    }
    Collections.sort(properties, new AutomatonComparator());
    for (final AutomatonProxy p : properties) {
      automata.add(p);
      final ProductDESProxy model =
        getFactory().createProductDESProxy("prop", getModel().getEvents(),
                                           automata);
      mChecker.setModel(model);
      final KindTranslator chain =
        new ChainKindTranslator(translator, properties);
      mChecker.setKindTranslator(chain);
      mChecker.setNodeLimit(getNodeLimit() - mStates);
      if (!mChecker.run()) {
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        return setFailedResult(mChecker.getCounterExample(), p);
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      automata.remove(p);
    }
    setSatisfiedResult();
    return true;
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(mStates);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return mChecker.supportsNondeterminism();
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean setFailedResult(final TraceProxy counterexample,
                                  final AutomatonProxy property)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String propname = property.getName();
    final String cleanedname = propname.replaceAll(":", "-");
    final String tracename = desname + '-' + cleanedname;
    final Collection<AutomatonProxy> automata = counterexample.getAutomata();
    final List<TraceStepProxy> steps = counterexample.getTraceSteps();
    final SafetyTraceProxy wrapper =
      factory.createSafetyTraceProxy(tracename, null, null,
                                     des, automata, steps);
    return setFailedResult(wrapper);
  }


  //#########################################################################
  //# Inner Class AutomatonComparator
  private final static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }


  //#########################################################################
  //# Inner Class ChainKindTranslator
  private static class ChainKindTranslator implements KindTranslator
  {

    //#######################################################################
    //# Constructor
    private ChainKindTranslator(final KindTranslator master,
                                final Collection<AutomatonProxy> properties)
    {
      mMaster = master;
      mProperties = new THashSet<AutomatonProxy>(properties);
    }

    //#######################################################################
    //# Inner Class ChainKindTranslator
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (mProperties.contains(aut)) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

    public EventKind getEventKind(final EventProxy event)
    {
      final EventKind kind = mMaster.getEventKind(event);
      if (kind == EventKind.CONTROLLABLE) {
        return EventKind.UNCONTROLLABLE;
      } else {
        return kind;
      }
    }

    //#######################################################################
    //# Data Members
    private final KindTranslator mMaster;
    private final Collection<AutomatonProxy> mProperties;

  }


  //#########################################################################
  //# Data Members
  private final SafetyVerifier mChecker;
  private int mStates;

}
