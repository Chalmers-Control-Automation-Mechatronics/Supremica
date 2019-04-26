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

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.PrintWriter;
import java.util.Formatter;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;

/**
 * @author Robi Malik
 */
public class EventDecisionTree
{

  //#########################################################################
  //# Constructors
  public EventDecisionTree(final List<AutomatonProxy> automata,
                           final EventEncoding encoding)
  {
    mAutomata = automata;
    mEventEncoding = encoding;
    mDecisionTree = new TIntArrayList();
  }


  //#########################################################################
  //# Code Generation
  int getNextLineNumber()
  {
    return mDecisionTree.size();
  }

  void appendCase(final int aut)
  {
    appendRaw(OPCODE_CASE | aut);
  }

  void appendIfDisabled(final int aut, final int event, final int task)
  {
    appendRaw(OPCODE_IFNN | aut);
    appendRaw(event);
    appendRaw(task);
  }

  void appendExecute(final int event)
  {
    appendRaw(OPCODE_EXEC | event);
  }

  void appendGoto(final int task)
  {
    assert (task & OPCODE_MASK) == 0;
    appendRaw(OPCODE_GOTO | task);
  }

  void appendRaw(final int value)
  {
    mDecisionTree.add(value);
  }

  public void renumber(final TIntIntHashMap lineNumberMap)
  {
    final int lines = mDecisionTree.size();
    int pos = 0;
    while (pos < lines) {
      final int line = pos;
      final int code = mDecisionTree.get(pos++);
      final int opcode = code & OPCODE_MASK;
      final int operand = code & OPERAND_MASK;
      switch (opcode) {
      case OPCODE_CASE:
      {
        final AutomatonProxy aut = mAutomata.get(operand);
        for (@SuppressWarnings("unused") final StateProxy state : aut.getStates()) {
          final int l0 = mDecisionTree.get(pos);
          final int l1 = lineNumberMap.get(l0);
          mDecisionTree.set(pos++, l1);
        }
        break;
      }
      case OPCODE_IFNN:
      {
        pos++;
        final int l0 = mDecisionTree.get(pos);
        final int l1 = lineNumberMap.get(l0);
        mDecisionTree.set(pos++, l1);
        break;
      }
      case OPCODE_EXEC:
        break;  // nothing to be replaced here
      case OPCODE_GOTO:
      {
        final int l1 = lineNumberMap.get(operand);
        mDecisionTree.set(line, OPCODE_GOTO | l1);
        break;
      }
      default:
        throw new IllegalStateException
          ("Unexpected opcode " + opcode +
           " in " + ProxyTools.getShortClassName(this) + "!");
      }
    }
  }

  //#########################################################################
  //# Printing
  public void dump(final PrintWriter writer)
  {
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(writer);
    final int lines = mDecisionTree.size();
    final int digits = (int) Math.log10(lines) + 1;
    final String format0 = "%" + digits + "d: ";
    final String formatCASE = format0 + "CASE %s ";
    final String formatIFNN = format0 + "IFNN %s %s GOTO %d";
    final String formatEXEC = format0 + "EXEC %s";
    final String formatGOTO = format0 + "GOTO %d";
    int pos = 0;
    while (pos < lines) {
      final int line = pos;
      final int code = mDecisionTree.get(pos++);
      final int opcode = code & OPCODE_MASK;
      final int operand = code & OPERAND_MASK;
      switch (opcode) {
      case OPCODE_CASE:
      {
        final AutomatonProxy aut = mAutomata.get(operand);
        final String name = aut.getName();
        formatter.format(formatCASE, line, name);
        boolean first = true;
        for (@SuppressWarnings("unused") final StateProxy state : aut.getStates()) {
          if (first) {
            first = false;
          } else {
            writer.print(',');
          }
          final int l = mDecisionTree.get(pos++);
          writer.print(l);
        }
        break;
      }
      case OPCODE_IFNN:
      {
        final AutomatonProxy aut = mAutomata.get(operand);
        final String autName = aut.getName();
        final int e = mDecisionTree.get(pos++);
        final EventProxy event = mEventEncoding.getProperEvent(e);
        final String eventName = event.getName();
        final int l = mDecisionTree.get(pos++);
        formatter.format(formatIFNN, line, autName, eventName, l);
        break;
      }
      case OPCODE_EXEC:
      {
        final EventProxy event = mEventEncoding.getProperEvent(operand);
        final String name = event == null ? "NULL" : event.getName();
        formatter.format(formatEXEC, line, name);
        break;
      }
      case OPCODE_GOTO:
      {
        formatter.format(formatGOTO, line, operand);
        break;
      }
      default:
        throw new IllegalStateException
          ("Unexpected opcode " + opcode +
           " in " + ProxyTools.getShortClassName(this) + "!");
      }
      writer.println();
      writer.flush();
    }
  }

  public double getAverageNumberOfSteps(final EventProbabilityProvider probabilities)
  {
    final TIntDoubleHashMap resultCache =
      new TIntDoubleHashMap(mDecisionTree.size(), 0.5f, -1, -1);
    return getAverageNumberOfSteps(0, probabilities, resultCache);
  }

  //#########################################################################
  //# Auxiliary Methods
  public double getAverageNumberOfSteps
    (final int lineNo,
     final EventProbabilityProvider probabilities,
     final TIntDoubleHashMap resultCache)
  {
    final double cachedResult = resultCache.get(lineNo);
    if (cachedResult >= 0) {
      return cachedResult;
    }
    double result = 0.0;
    if (lineNo < mDecisionTree.size()) {
      final int code = mDecisionTree.get(lineNo);
      final int opcode = code & OPCODE_MASK;
      final int operand = code & OPERAND_MASK;
      switch (opcode) {
      case OPCODE_CASE:
      {
        final AutomatonProxy aut = mAutomata.get(operand);
        final int numStates = aut.getStates().size();
        for (int pos = lineNo + 1; pos <= lineNo + numStates; pos++) {
          final int l = mDecisionTree.get(pos);
          result += getAverageNumberOfSteps(l, probabilities, resultCache);
        }
        result = 1.0 + result / numStates;
        break;
      }
      case OPCODE_IFNN:
      {
        final int e = mDecisionTree.get(lineNo + 1);
        final double prob = probabilities.getProbability(operand, e);
        final int l = mDecisionTree.get(lineNo + 2);
        result = 1.0 +
          prob * getAverageNumberOfSteps(lineNo + 3, probabilities, resultCache) +
          (1.0 - prob) * getAverageNumberOfSteps(l, probabilities, resultCache);

        break;
      }
      case OPCODE_EXEC:
      {
        result = 1.0 +
          getAverageNumberOfSteps(lineNo + 1, probabilities, resultCache);
        break;
      }
      case OPCODE_GOTO:
      {
        result = 1.0 +
          getAverageNumberOfSteps(operand, probabilities, resultCache);
        break;
      }
      default:
        throw new IllegalStateException
          ("Unexpected opcode " + opcode +
           " in " + ProxyTools.getShortClassName(this) + "!");
      }
    }
    resultCache.put(lineNo, result);
    return result;
  }


  //#########################################################################
  //# Data Members
  private final List<AutomatonProxy> mAutomata;
  private final EventEncoding mEventEncoding;
  private final TIntArrayList mDecisionTree;


  //#########################################################################
  //# Class Constants
  private static final int OPCODE_SHIFT = 30;
  private static final int OPERAND_MASK = (1 << OPCODE_SHIFT) - 1;
  private static final int OPCODE_MASK = ~OPERAND_MASK;

  private static final int OPCODE_CASE = 0x00 << OPCODE_SHIFT;
  private static final int OPCODE_IFNN = 0x01 << OPCODE_SHIFT;
  private static final int OPCODE_EXEC = 0x02 << OPCODE_SHIFT;
  private static final int OPCODE_GOTO = 0x03 << OPCODE_SHIFT;

}
