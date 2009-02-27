//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableTransitionRelationPart
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * A compiler-internal representation of the set of all the transitions
 * associated with a given event in one particular variable automaton to be
 * generated. More than one partial transition ({@link
 * EFAVariableTransition}) may be associated to the same event, for
 * different values of the EFA variables.
 *
 * @author Robi Malik
 */

class EFAVariableTransitionRelationPart
  implements Comparable<EFAVariableTransitionRelationPart>
{

  //#########################################################################
  //# Factory Methods
  static EFAVariableTransitionRelationPart createComplementaryPart
    (final CompiledRange range, final EFAVariableTransitionRelationPart part)
  {
    assert part.isAllSelfloops();
    final List<? extends SimpleExpressionProxy> allvalues = range.getValues();
    final ProxyAccessorMap<SimpleExpressionProxy> sources = part.mSourceValues;
    final int size = allvalues.size() - sources.size();
    final EFAVariableTransitionRelationPart result =
      new EFAVariableTransitionRelationPart(size);
    for (final SimpleExpressionProxy value : allvalues) {
      if (!sources.containsProxy(value)) {
        result.addTransition(value, value);
      }
    }
    return result;      
  }


  //#########################################################################
  //# Constructors
  EFAVariableTransitionRelationPart()
  {
    mSourceValues =
      new ProxyAccessorHashMapByContents<SimpleExpressionProxy>();
    mTransitions = new TreeSet<EFAVariableTransition>();
    mIsAllSelfloops = true;
  }

  EFAVariableTransitionRelationPart(final int size)
  {
    mSourceValues =
      new ProxyAccessorHashMapByContents<SimpleExpressionProxy>(size);
    mTransitions = new TreeSet<EFAVariableTransition>();
    mIsAllSelfloops = true;
  }

  EFAVariableTransitionRelationPart
    (final Set<EFAVariableTransition> transitions)
  {
    this(transitions.size());
    addTransitions(transitions);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the number of transitions in this part of the transition relation.
   */
  int size()
  {
    return mTransitions.size();
  }

  boolean isEmpty()
  {
    return mTransitions.isEmpty();
  }

  boolean isAllSelfloops()
  {
    return mIsAllSelfloops;
  }

  Set<EFAVariableTransition> getTransitions()
  {
    return mTransitions;
  }

  void addTransition(final SimpleExpressionProxy source,
                     final SimpleExpressionProxy target)
  {
    final EFAVariableTransition trans =
      new EFAVariableTransition(source, target);
    addTransition(trans);
  }

  void addTransitions(final Collection<EFAVariableTransition> transitions)
  {
    for (final EFAVariableTransition trans : transitions) {
      addTransition(trans);
    }
  }

  void addTransition(final EFAVariableTransition trans)
  {
    final SimpleExpressionProxy source = trans.getSource();
    mSourceValues.addProxy(source);
    mTransitions.add(trans);
    mIsAllSelfloops &= trans.isSelfloop();
  }


  //#########################################################################
  //# Overrides for Base Class java.lang.Object
  public String toString()
  {
    try {
      final StringWriter writer = new StringWriter();
      final ProxyPrinter printer = new ModuleProxyPrinter(writer);
      pprint(printer);
      final StringBuffer buffer = writer.getBuffer();
      return buffer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final EFAVariableTransitionRelationPart part =
        (EFAVariableTransitionRelationPart) other;
      return mTransitions.equals(part.mTransitions);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mTransitions.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final EFAVariableTransitionRelationPart part)
  {
    final Iterator<EFAVariableTransition> iter1 = mTransitions.iterator();
    final Iterator<EFAVariableTransition> iter2 = part.mTransitions.iterator();
    EFAVariableTransition trans1 = iter1.hasNext() ? iter1.next() : null;
    EFAVariableTransition trans2 = iter2.hasNext() ? iter2.next() : null;
    while (trans1 != null && trans2 != null) {
      final int result = trans1.compareTo(trans2);
      if (result != 0) {
        return result;
      }
      trans1 = iter1.hasNext() ? iter1.next() : null;
      trans2 = iter2.hasNext() ? iter2.next() : null;
    }
    if (trans1 == null) {
      return trans2 == null ? 0 : -1;
    } else {
      return 1;
    }
  }


  //#########################################################################
  //# Subsumption Testing
  boolean isDisjoint(final EFAVariableTransitionRelationPart part)
  {
    final ProxyAccessorMap<SimpleExpressionProxy> sources1 = mSourceValues;
    final ProxyAccessorMap<SimpleExpressionProxy> sources2 =
      part.mSourceValues;
    if (sources1.size() < sources2.size()) {
      for (final ProxyAccessor<SimpleExpressionProxy> accessor :
             sources1.keySet()) {
        if (sources2.containsKey(accessor)) {
          return false;
        }
      }
      return true;
    } else {
      return part.isDisjoint(this);
    }
  }

  SubsumptionResult.Kind subsumptionTest
    (final EFAVariableTransitionRelationPart part)
  {
    if (part == null) {
      if (isAllSelfloops()) {
        return SubsumptionResult.Kind.SUBSUMES;
      } else {
        return SubsumptionResult.Kind.INTERSECTS;
      }
    } else {
      final Set<EFAVariableTransition> transitions1 = mTransitions;
      final Set<EFAVariableTransition> transitions2 = part.mTransitions;
      if (transitions1.size() <= transitions2.size()) {
        if (!transitions2.containsAll(transitions1)) {
          return SubsumptionResult.Kind.INTERSECTS;
        } else if (transitions1.size() == transitions2.size()) {
          return SubsumptionResult.Kind.EQUALS;
        } else {
          return SubsumptionResult.Kind.SUBSUMES;
        }
      } else {
        final SubsumptionResult.Kind kind = part.subsumptionTest(this);
        return SubsumptionResult.reverse(kind);
      }
    }
  }

  EFAVariableTransitionRelationPart intersection
    (final EFAVariableTransitionRelationPart part)
  {
    final Set<EFAVariableTransition> transitions1 = mTransitions;
    final Set<EFAVariableTransition> transitions2 = part.mTransitions;
    if (transitions1.size() <= transitions2.size()) {
      final Set<EFAVariableTransition> transitions =
        new HashSet<EFAVariableTransition>(transitions1.size());
      for (final EFAVariableTransition trans : transitions1) {
        if (transitions2.contains(trans)) {
          transitions.add(trans);
        }
      }
      if (transitions.size() == transitions1.size()) {
        return this;
      } else {
        return new EFAVariableTransitionRelationPart(transitions);
      }
    } else {
      return part.intersection(this);
    }
  }

  EFAVariableTransitionRelationPart difference
    (final EFAVariableTransitionRelationPart part)
  {
    final Set<EFAVariableTransition> transitions =
      new HashSet<EFAVariableTransition>(mTransitions.size());
    for (final EFAVariableTransition trans : mTransitions) {
      if (!part.mTransitions.contains(trans)) {
        transitions.add(trans);
      }
    }
    return new EFAVariableTransitionRelationPart(transitions);
  }


  //#########################################################################
  //# Auxliary Methods
  void pprint(final ProxyPrinter printer)
    throws IOException
  {
    printer.pprint("{");
    boolean first = true;
    for (final EFAVariableTransition trans : mTransitions) {
      if (first) {
        first = false;
      } else {
        printer.pprint(", ");
      }
      trans.pprint(printer);
    }
    printer.pprint("}");
  }


  //#########################################################################
  //# Data Members
  private final ProxyAccessorMap<SimpleExpressionProxy> mSourceValues;
  private final Set<EFAVariableTransition> mTransitions;
  private boolean mIsAllSelfloops;

}
