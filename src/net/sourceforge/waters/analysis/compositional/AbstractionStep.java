//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractionStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * @author Robi Malik
 */
abstract class AbstractionStep
{

  //#######################################################################
  //# Constructors
  AbstractionStep(final AbstractCompositionalModelAnalyzer analyzer,
                  final List<AutomatonProxy> results,
                  final List<AutomatonProxy> originals)
  {
    mAnalyzer = analyzer;
    mResultAutomata = results;
    mOriginalAutomata = originals;
  }

  AbstractionStep(final AbstractCompositionalModelAnalyzer analyzer,
                  final AutomatonProxy result,
                  final Collection<AutomatonProxy> originals)
  {
    this(analyzer,
         Collections.singletonList(result),
         new ArrayList<AutomatonProxy>(originals));
  }

  AbstractionStep(final AbstractCompositionalModelAnalyzer analyzer,
                  final AutomatonProxy result,
                  final AutomatonProxy original)
  {
    this(analyzer,
         Collections.singletonList(result),
         Collections.singletonList(original));
  }


  //#######################################################################
  //# Simple Access
  List<AutomatonProxy> getResultAutomata()
  {
    return mResultAutomata;
  }

  AutomatonProxy getResultAutomaton()
  {
    if (mResultAutomata.size() == 1) {
      return mResultAutomata.iterator().next();
    } else {
      throw new IllegalStateException
        ("Attempting to get a single result automaton from " +
         ProxyTools.getShortClassName(this) + " with " +
         mResultAutomata.size() + " result automata!");
    }
  }

  List<AutomatonProxy> getOriginalAutomata()
  {
    return mOriginalAutomata;
  }

  AutomatonProxy getOriginalAutomaton()
  {
    if (mOriginalAutomata.size() == 1) {
      return mOriginalAutomata.iterator().next();
    } else {
      throw new IllegalStateException
        ("Attempting to get a single input automaton from " +
         ProxyTools.getShortClassName(this) + " with " +
         mOriginalAutomata.size() + " input automata!");
    }
  }

  void addAutomatonPair(final AutomatonProxy result,
                        final AutomatonProxy original)
  {
    mResultAutomata.add(result);
    mOriginalAutomata.add(original);
  }


  //#######################################################################
  //# Accessing the Analyser
  AbstractCompositionalModelAnalyzer getAnalyzer()
  {
    return mAnalyzer;
  }

  ProductDESProxyFactory getFactory()
  {
    return mAnalyzer.getFactory();
  }

  EventProxy getUsedDefaultMarking()
  {
    return mAnalyzer.getUsedDefaultMarking();
  }

  EventProxy getUsedPreconditionMarking()
  {
    return mAnalyzer.getUsedPreconditionMarking();
  }


  //#######################################################################
  //# Trace Computation
  /**
   * Converts the given trace on the result of this rule application
   * to a trace on the original automaton before abstraction.
   * Assumes that a saturated trace is being passed.
   */
  List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> steps)
    throws AnalysisException
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) +
       " does not support trace expansion!");
  }

  //#######################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(ProxyTools.getShortClassName(this));
    buffer.append("\nOriginal: ");
    boolean first = true;
    for (final AutomatonProxy aut : mOriginalAutomata) {
      if (first) {
        first = false;
      } else {
        buffer.append("; ");
      }
      buffer.append(aut.getName());
    }
    buffer.append("\nResult: ");
    first = true;
    for (final AutomatonProxy aut : mResultAutomata) {
      if (first) {
        first = false;
      } else {
        buffer.append("; ");
      }
      buffer.append(aut.getName());
    }
    return buffer.toString();
  }

  //#######################################################################
  //# Data Members
  private final AbstractCompositionalModelAnalyzer mAnalyzer;
  private final List<AutomatonProxy> mResultAutomata;
  private final List<AutomatonProxy> mOriginalAutomata;

}