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

package net.sourceforge.waters.model.compiler.efa;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
  implements Comparable<EFAVariableTransitionRelation>
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
      mParts = new TreeMap<EFAVariable,EFAVariableTransitionRelationPart>();
    }
    mIsEmpty = empty;
  }

  EFAVariableTransitionRelation(final int size)
  {
    mParts = new TreeMap<EFAVariable,EFAVariableTransitionRelationPart>();
    mIsEmpty = false;
  }

  EFAVariableTransitionRelation
    (final Map<EFAVariable,EFAVariableTransitionRelationPart> parts)
  {
    for (final EFAVariableTransitionRelationPart part : parts.values()) {
      if (part.isEmpty()) {
        mIsEmpty = true;
        mParts = null;
        return;
      }
    }
    mIsEmpty = false;
    mParts = new TreeMap<EFAVariable,EFAVariableTransitionRelationPart>(parts);
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
   * Returns the set of variables affected by this event.
   * @return The set of all variables changed by this event, or
   *         on whose value the enablement of this event depends.
   *         This represents exactly the variable automata in whose
   *         alphabet the event is to appear.
   */
  Set<EFAVariable> getVariables()
  {
    if (mParts == null) {
      return Collections.emptySet();
    } else {
      return mParts.keySet();
    }
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
   *                  to the variable, or <CODE>null</CODE> to indicate an
   *                  explicitly unchanged variable.
   */
  void addPart(final EFAVariable var,
               final EFAVariableTransitionRelationPart part)
  {
    if (!mIsEmpty) {
      if (part != null && part.isEmpty()) {
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
  @Override
  public String toString()
  {
    try {
      final StringWriter writer = new StringWriter();
      final ProxyPrinter printer = new ModuleProxyPrinter(writer);
      pprint(printer);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  @Override
  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final EFAVariableTransitionRelation rel =
        (EFAVariableTransitionRelation) other;
      if (mIsEmpty) {
        return rel.mIsEmpty;
      } else {
        return !rel.mIsEmpty && mParts.equals(rel.mParts);
      }
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    return mIsEmpty ? -1 : mParts.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  @Override
  public int compareTo(final EFAVariableTransitionRelation rel)
  {
    if (mIsEmpty) {
      return rel.mIsEmpty ? 0 : -1;
    } else if (rel.mIsEmpty) {
      return 1;
    }
    final Iterator<EFAVariable> iter1 = mParts.keySet().iterator();
    final Iterator<EFAVariable> iter2 = rel.mParts.keySet().iterator();
    EFAVariable var1 = iter1.hasNext() ? iter1.next() : null;
    EFAVariable var2 = iter2.hasNext() ? iter2.next() : null;
    while (var1 != null && var2 != null) {
      int result = var1.compareTo(var2);
      if (result != 0) {
        return result;
      }
      final EFAVariableTransitionRelationPart part1 = mParts.get(var1);
      final EFAVariableTransitionRelationPart part2 = rel.mParts.get(var2);
      if (part1 == null) {
        if (part2 != null) {
          return -1;
        }
      } else if (part2 == null) {
        return 1;
      } else {
        result = part1.compareTo(part2);
        if (result != 0) {
          return result;
        }
      }
      var1 = iter1.hasNext() ? iter1.next() : null;
      var2 = iter2.hasNext() ? iter2.next() : null;
    }
    if (var1 == null) {
      return var2 == null ? 0 : -1;
    } else {
      return 1;
    }
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
            if (part1 != null && part1.isDisjoint(part2)) {
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
          if (part1 != null) {
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
        }
        if (visited < parts2.size()) {
          for (final Map.Entry<EFAVariable,EFAVariableTransitionRelationPart>
                 entry : parts2.entrySet()) {
            final EFAVariable var = entry.getKey();
            if (parts1.get(var) == null) {
              final EFAVariableTransitionRelationPart part2 = entry.getValue();
              if (part2 != null) {
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
        }
        return result;
      } else {
        final SubsumptionResult.Kind result = rel.subsumptionTest(this);
        return SubsumptionResult.reverse(result);
      }
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
            part2.complement(range);
          map.put(var, complement);
        } else if (part2 == null) {
          final EFAVariableTransitionRelationPart complement =
            part1.stripSelfloops();
          map.put(var, complement);
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
        if (part == null) {
          printer.pprint(" =");
        } else {
          part.pprint(printer);
        }
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
