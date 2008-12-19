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
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * A compiler-internal representation the transition relation for the EFA
 * variables associated with a given event. This is basically a collection
 * of {@link EFAVariableTransitionRelation} items, each representing the
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
    return mParts.hashCode();
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

  SubsumptionKind subsumptionTest(final EFAVariableTransitionRelation rel)
  {
    if (mIsEmpty) {
      return rel.mIsEmpty ? SubsumptionKind.EQUALS : SubsumptionKind.SUBSUMES;
    } else if (rel.mIsEmpty) {
      return SubsumptionKind.SUBSUMED_BY;
    } else {
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts1 = mParts;
      final Map<EFAVariable,EFAVariableTransitionRelationPart> parts2 =
        rel.mParts;
      int visited = 0;
      SubsumptionKind result;
      if (parts1.size() >= parts2.size()) {
        result = SubsumptionKind.EQUALS;
        for (final Map.Entry<EFAVariable,EFAVariableTransitionRelationPart>
               entry : parts1.entrySet()) {
          final EFAVariable var = entry.getKey();
          final EFAVariableTransitionRelationPart part1 = entry.getValue();
          final EFAVariableTransitionRelationPart part2 = parts2.get(var);
          if (part2 == null) {
            assert part1.isAllSelfloops();
            if (part1.getTransitions().size() < var.getRange().size()) {
              result = SubsumptionKind.SUBSUMES;
            }
          } else {
            visited++;
            final SubsumptionKind kind = part1.subsumptionTest(part2);
            result = result.combine(kind);
            if (result == SubsumptionKind.INTERSECTS) {
              return SubsumptionKind.INTERSECTS;
            }
          }
        }
        if (visited < parts2.size()) {
          for (final Map.Entry<EFAVariable,EFAVariableTransitionRelationPart>
                 entry : parts2.entrySet()) {
            final EFAVariable var = entry.getKey();
            if (parts1.get(var) == null) {
              final EFAVariableTransitionRelationPart part2 = entry.getValue();
              assert part2.isAllSelfloops();
              if (part2.getTransitions().size() < var.getRange().size()) {
                result = result.combine(SubsumptionKind.SUBSUMED_BY);
                if (result == SubsumptionKind.INTERSECTS) {
                  return SubsumptionKind.INTERSECTS;
                }
              }
              if (++visited == parts2.size()) {
                break;
              }
            }
          }
        }
      } else {
        result = rel.subsumptionTest(this);
        if (result == SubsumptionKind.SUBSUMES) {
          result = SubsumptionKind.SUBSUMED_BY;
        }
      }
      return result;
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
      final Map<EFAVariable,EFAVariableTransitionRelationPart> map =
        new HashMap<EFAVariable,EFAVariableTransitionRelationPart>(size);
      vars.addAll(parts1.keySet());
      vars.addAll(parts2.keySet());
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

}
