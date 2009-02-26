//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAVariableTransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * A compiler-internal representation the transition relation for the EFA
 * variables associated with a given event. This is basically a collection
 * of {@link EFAVariableTransitionRelationPart} items, each representing the
 * transitions for a different variable. The overall transition relation
 * then is the Cartesian product of the individual parts.
 *
 * @author Robi Malik
 */

class EFAVariableTransitionRelation
{

  //#########################################################################
  //# Constructors
  EFAVariableTransitionRelation()
  {
    this(false);
  }

  EFAVariableTransitionRelation(final boolean empty)
  {
    if (empty) {
      mParts = null;
    } else {
      mParts = new HashMap<EFAVariable,EFAVariableTransitionRelationPart>();
    }
    mIsEmpty = empty;
  }

  EFAVariableTransitionRelation(final int size)
  {
    mParts = new HashMap<EFAVariable,EFAVariableTransitionRelationPart>(size);
    mIsEmpty = false;
  }

  EFAVariableTransitionRelation
    (final Map<EFAVariable,EFAVariableTransitionRelationPart> parts)
  {
    mIsEmpty = false;
    for (final EFAVariableTransitionRelationPart part : parts.values()) {
      if (part.isEmpty()) {
        mIsEmpty = true;
        break;
      }
    }
    mParts = mIsEmpty ? null : parts;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the number of transitions in this transition relation.
   * This method calculates the total number of transitions needed to
   * represent this transition relation as the sum of the transitions
   * needed by each variable automaton. The number of synchronous product
   * transitions is not considered.
   */
  int size()
  {
    int result = 0;
    if (!mIsEmpty) {
      for (final EFAVariableTransitionRelationPart part : mParts.values()) {
        result += part.size();
      }
    }
    return result;
  }

  /**
   * Checks whether this transition relation is empty.
   * An empty transition relation means that the event associated with it
   * is never enabled. A transition relation is recognised as empty as soon
   * as one of its parts is empty.
   * @return <CODE>true</CODE> if this transition relation is known
   *                           to be empty.
   */
  boolean isEmpty()
  {
    return mIsEmpty;
  }

  /**
   * Returns the partial transition relation associated with a given
   * variable.
   * @return The partial transition relation associated with the given
   *         variable, or <CODE>null</CODE> to indicate that the variable
   *         is left unchanged by this transition.
   */ 
  EFAVariableTransitionRelationPart getPart(final EFAVariable var)
  {
    return mParts.get(var);
  }

  /**
   * Adds a new part to this transition relation.
   * @param var       The variable to which the new transition relation part
   *                  is to be associated. If the transition relation already
   *                  contains a partial relation for this variable, the 
   *                  behaviour is undefined.
   * @param part      The new partial transition relation to be associated
   *                  to the variable.
   */
  void addPart(final EFAVariable var,
               final EFAVariableTransitionRelationPart part)
  {
    if (!mIsEmpty) {
      if (part.isEmpty()) {
        mParts.clear();
        mIsEmpty = true;
      } else {
        mParts.put(var, part);
      }
    }
  }

  /**
   * Gets the formula associated with this transition relation.
   * Each transition relation needs to be associated with the constraint list
   * from which it was created. This information is used to create event names.
   * @return A constraint list, or <CODE>null</CODE> if no formula has been
   *         provided yet.
   */
  ConstraintList getFormula()
  {
    return mFormula;
  }

  /**
   * Provides a new formula for this transition relation.
   * This method overwrites the formula stored on the transition relation
   * if the new formula is shorter than the current one, or if there is
   * no formula stored yet.
   * @param  formula  The new formula.
   */
  void provideFormula(final ConstraintList formula)
  {
    if (mFormula == null || formula.size() < mFormula.size()) {
      mFormula = formula;
    }
  }

  /**
   * Provides a new formula for this transition relation by sharing
   * with another transition relation.
   * This method overwrites the formula stored on this transition relation
   * with the formula of the given transition relation if present and shorter
   * than the existing one.
   */
  void provideFormula(final EFAVariableTransitionRelation source)
  {
    final ConstraintList formula = source.getFormula();
    if (formula != null) {
      provideFormula(formula);
    }
  }

  int objectHashCode()
  {
    return super.hashCode();
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
      final EFAVariableTransitionRelation rel =
        (EFAVariableTransitionRelation) other;
      return mIsEmpty ? rel.mIsEmpty : mParts.equals(rel.mParts);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mIsEmpty ? 0 : mParts.hashCode();
  }


  //#########################################################################
  //# Subsumption Testing
  boolean isDisjoint(final EFAVariableTransitionRelation rel)
  {
    if (mIsEmpty || rel.mIsEmpty) {
      return true;
    } else {
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts1 = mParts;
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts2 =
        rel.mParts;
      if (parts1.size() <= parts2.size()) {
        for (final Map.Entry<EFAVariable,EFAVariableTransitionRelationPart>
               entry : parts1.entrySet()) {
          final EFAVariable var = entry.getKey();
          final EFAVariableTransitionRelationPart part2 = parts2.get(var);
          if (part2 != null) {
            final EFAVariableTransitionRelationPart part1 = entry.getValue();
            if (part1.isDisjoint(part2)) {
              return true;
            }
          }
        }
        return false;
      } else {
        return rel.isDisjoint(this);
      }
    }
  }

  SubsumptionResult.Kind subsumptionTest
    (final EFAVariableTransitionRelation rel)
  {
    if (mIsEmpty) {
      if (rel.mIsEmpty) {
        return SubsumptionResult.Kind.EQUALS;
      } else {
        return SubsumptionResult.Kind.SUBSUMES;
      }
    } else if (rel.mIsEmpty) {
      return SubsumptionResult.Kind.SUBSUMED_BY;
    } else {
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts1 = mParts;
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts2 =
        rel.mParts;
      int visited = 0;
      if (parts1.size() >= parts2.size()) {
        SubsumptionResult.Kind result = SubsumptionResult.Kind.EQUALS;
        for (final Map.Entry<EFAVariable,EFAVariableTransitionRelationPart>
               entry : parts1.entrySet()) {
          final EFAVariable var = entry.getKey();
          final EFAVariableTransitionRelationPart part1 = entry.getValue();
          final EFAVariableTransitionRelationPart part2 = parts2.get(var);
          final SubsumptionResult.Kind kind = part1.subsumptionTest(part2);
          result = SubsumptionResult.combine(result, kind);
          if (result == SubsumptionResult.Kind.INTERSECTS) {
            return SubsumptionResult.Kind.INTERSECTS;
          }
          if (part2 != null) {
            visited++;
          }
        }
        if (visited < parts2.size()) {
          for (final Map.Entry<EFAVariable,EFAVariableTransitionRelationPart>
                 entry : parts2.entrySet()) {
            final EFAVariable var = entry.getKey();
            if (parts1.get(var) == null) {
              final EFAVariableTransitionRelationPart part2 = entry.getValue();
              final SubsumptionResult.Kind rkind = part2.subsumptionTest(null);
              final SubsumptionResult.Kind kind =
                SubsumptionResult.reverse(rkind);
              result = SubsumptionResult.combine(result, kind);
              if (result == SubsumptionResult.Kind.INTERSECTS) {
                return SubsumptionResult.Kind.INTERSECTS;
              }
              if (++visited == parts2.size()) {
                break;
              }
            }
          }
        }
        return result;
      } else {
        final SubsumptionResult.Kind result = rel.subsumptionTest(this);
        return SubsumptionResult.reverse(result);
      }
    }
  }

  EFAVariableTransitionRelation intersection
    (final EFAVariableTransitionRelation rel)
  {
    if (mIsEmpty) {
      return this;
    } else if (rel.mIsEmpty) {
      return rel;
    } else {
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts1 = mParts;
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts2 =
        rel.mParts;
      final int size = parts1.size() + parts2.size();
      final Set<EFAVariable> vars = new HashSet<EFAVariable>(size);
      vars.addAll(parts1.keySet());
      vars.addAll(parts2.keySet());
      final int vsize = vars.size();
      final Map<EFAVariable,EFAVariableTransitionRelationPart> map =
        new HashMap<EFAVariable,EFAVariableTransitionRelationPart>(vsize);
      for (final EFAVariable var : vars) {
        final EFAVariableTransitionRelationPart part1 = parts1.get(var);
        final EFAVariableTransitionRelationPart part2 = parts2.get(var);
        if (part1 == null) {
          assert part2.isAllSelfloops();
          map.put(var, part2);
        } else if (part2 == null) {
          assert part1.isAllSelfloops();
          map.put(var, part1);
        } else {
          final EFAVariableTransitionRelationPart part =
            part1.intersection(part2);
          map.put(var, part);
        }
      }
      return new EFAVariableTransitionRelation(map);
    }
  }

  EFAVariableTransitionRelation difference
    (final EFAVariableTransitionRelation rel)
  {
    if (mIsEmpty || rel.mIsEmpty) {
      return this;
    } else {
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts1 = mParts;
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts2 =
        rel.mParts;
      final int size = parts1.size() + parts2.size();
      final Set<EFAVariable> vars = new HashSet<EFAVariable>(size);
      final Map<EFAVariable,EFAVariableTransitionRelationPart> map =
        new HashMap<EFAVariable,EFAVariableTransitionRelationPart>
          (parts2.size());
      vars.addAll(parts1.keySet());
      vars.addAll(parts2.keySet());
      for (final EFAVariable var : vars) {
        final EFAVariableTransitionRelationPart part1 = parts1.get(var);
        final EFAVariableTransitionRelationPart part2 = parts2.get(var);
        if (part1 == null) {
          final CompiledRange range = var.getRange();
          final EFAVariableTransitionRelationPart complement =
            EFAVariableTransitionRelationPart.createComplementaryPart
              (range, part2);
          map.put(var, complement);
        } else if (part2 == null) {
          assert part1.isAllSelfloops();
          return new EFAVariableTransitionRelation(true);
        } else {
          final EFAVariableTransitionRelationPart part =
            part1.difference(part2);
          map.put(var, part);
        }
      }
      return new EFAVariableTransitionRelation(map);
    }
  }


  //#########################################################################
  //# Printing
  void pprint(final ProxyPrinter printer)
    throws IOException
  {
    if (mIsEmpty) {
      printer.pprint("{*: 0}");
    } else {
      final List<EFAVariable> vars =
        new ArrayList<EFAVariable>(mParts.keySet());
      Collections.sort(vars);
      printer.pprint("{");
      boolean first = true;
      for (final EFAVariable var : vars) {
        if (first) {
          first = false;
        } else {
          printer.pprint("; ");
        }
        final SimpleExpressionProxy ident = var.getVariableName();
        final EFAVariableTransitionRelationPart part = mParts.get(var);
        printer.pprint(ident);
        printer.pprint(": ");
        part.pprint(printer);
      }
      printer.pprint("}");
    }
  }


  //#########################################################################
  //# Data Members
  private final Map<EFAVariable,EFAVariableTransitionRelationPart> mParts;
  private boolean mIsEmpty;
  private ConstraintList mFormula;

}
