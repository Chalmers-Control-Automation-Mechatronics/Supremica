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
import java.util.HashSet;
import java.util.Set;

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
{

  //#########################################################################
  //# Constructors
  EFAVariableTransitionRelationPart()
  {
    mTransitions = new HashSet<EFAVariableTransition>();
  }

  EFAVariableTransitionRelationPart(final int size)
  {
    mTransitions = new HashSet<EFAVariableTransition>(size);
  }

  EFAVariableTransitionRelationPart
    (final Set<EFAVariableTransition> transitions)
  {
    mTransitions = transitions;
  }


  //#########################################################################
  //# Simple Access
  int size()
  {
    return mTransitions.size();
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
    mTransitions.add(trans);
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
  //# Subsumption Testing
  SubsumptionKind subsumptionTest(final EFAVariableTransitionRelationPart part)
  {
    if (size() <= part.size()) {
      boolean maybeDisjoint = true;
      boolean maySubsume = true;
      for (final EFAVariableTransition trans : mTransitions) {
        if (part.mTransitions.contains(trans)) {
          maybeDisjoint = false;
          if (!maySubsume) {
            return SubsumptionKind.INTERSECTS;
          }
        } else {
          maySubsume = false;
          if (!maybeDisjoint) {
            return SubsumptionKind.INTERSECTS;
          }
        }
      }
      if (maySubsume && size() == part.size()) {
        return SubsumptionKind.EQUALS;
      } else if (maybeDisjoint) {
        return SubsumptionKind.DISJOINT;
      } else  {
        assert maySubsume;
        return SubsumptionKind.SUBSUMES;
      }
    } else {
      final SubsumptionKind kind = part.subsumptionTest(this);
      if (kind == SubsumptionKind.SUBSUMES) {
        return SubsumptionKind.SUBSUMED_BY;
      } else {
        return kind;
      }
    }
  }

  EFAVariableTransitionRelationPart intersection
    (final EFAVariableTransitionRelationPart part)
  {
    if (size() <= part.size()) {
      final Set<EFAVariableTransition> transitions =
        new HashSet<EFAVariableTransition>(size());
      for (final EFAVariableTransition trans : mTransitions) {
        if (part.mTransitions.contains(trans)) {
          transitions.add(trans);
        }
      }
      return new EFAVariableTransitionRelationPart(transitions);
    } else {
      return part.intersection(this);
    }
  }

  EFAVariableTransitionRelationPart difference
    (final EFAVariableTransitionRelationPart part)
  {
    final Set<EFAVariableTransition> transitions =
      new HashSet<EFAVariableTransition>(size());
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
  private final Set<EFAVariableTransition> mTransitions;

}
