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

package net.sourceforge.waters.analysis.compositional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
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

  void checkAbort()
    throws AnalysisAbortException, OverflowException
  {
    mAnalyzer.checkAbort();
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
    final StringBuilder buffer = new StringBuilder();
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
