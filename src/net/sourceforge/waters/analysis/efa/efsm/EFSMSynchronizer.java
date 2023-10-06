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

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.EFASimplifierStatistics;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * A synchronous product builder to compose two EFSMs without any
 * shared events.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class EFSMSynchronizer extends AbstractEFSMAlgorithm
{

  //#########################################################################
  //# Constructors
  public EFSMSynchronizer(final ModuleProxyFactory factory)
  {
    createStatistics(true);
    mFactory = factory;
  }


  //#########################################################################
  //# Configuration
  public void setSourceInfoEnabled(final boolean enabled)
  {
    mSourceInfoEnabled = enabled;
  }


  //#########################################################################
  //# Invocation
  public EFSMTransitionRelation synchronize(final EFSMTransitionRelation efsmTR1,
                                            final EFSMTransitionRelation efsmTR2)
    throws AnalysisException
  {
    final long start = System.currentTimeMillis();
    final EFASimplifierStatistics statistics = getStatistics();
    statistics.recordStart(efsmTR1, efsmTR2);
    EFSMTransitionRelation synchResult = null;
    try{
      final ListBufferTransitionRelation rel1 = efsmTR1.getTransitionRelation();
      final ListBufferTransitionRelation rel2 = efsmTR2.getTransitionRelation();
      rel1.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      rel2.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final int reachableStates1 = rel1.getNumberOfReachableStates();
      final int reachableStates2 = rel2.getNumberOfReachableStates();
      if ((double) reachableStates1 * (double) reachableStates2 >
          Integer.MAX_VALUE) {
        throw new OverflowException(OverflowKind.STATE, Integer.MAX_VALUE);
      }
      final int finalStateNum = reachableStates1 * reachableStates2;
      final List<SimpleNodeProxy> nodeList1 = efsmTR1.getNodeList();
      final List<SimpleNodeProxy> nodeList2 = efsmTR2.getNodeList();
      final List<SimpleNodeProxy> nodeList;
      if (mSourceInfoEnabled && nodeList1 != null &&  nodeList2 != null) {
        nodeList = new ArrayList<SimpleNodeProxy>(finalStateNum);
      } else {
        nodeList = null;
      }
      final EFSMEventEncoding eventEncoding1 = efsmTR1.getEventEncoding();
      final EFSMEventEncoding eventEncoding2 = efsmTR2.getEventEncoding();
      final int eventNum = eventEncoding1.size() + eventEncoding2.size();
      final EFSMEventEncoding synchEventEncoding =
        new EFSMEventEncoding(eventNum);
      final int[] eventMap1 = new int[eventEncoding1.size()];
      for (int e = 0; e < eventEncoding1.size(); e++) {
        final ConstraintList update = eventEncoding1.getUpdate(e);
        eventMap1[e] = synchEventEncoding.createEventId(update);
      }
      final int[] eventMap2 = new int[eventEncoding2.size()];
      for (int e = 0; e < eventEncoding2.size(); e++) {
        final ConstraintList update = eventEncoding2.getUpdate(e);
        eventMap2[e] = synchEventEncoding.createEventId(update);
      }
      final int[] stateMap1 = new int[rel1.getNumberOfStates()];
      int currentState1 = 0;
      for (int s1 = 0; s1 < rel1.getNumberOfStates(); s1++) {
        if (rel1.isReachable(s1)) {
          stateMap1[s1] = currentState1;
          currentState1++;
        } else {
          stateMap1[s1] = -1;
        }
      }
      checkAbort();
      int currentState2 = 0;
      final int[] stateMap2 = new int[rel2.getNumberOfStates()];
      for (int s2 = 0; s2 < rel2.getNumberOfStates(); s2++) {
        if (rel2.isReachable(s2)) {
          stateMap2[s2] = currentState2;
          currentState2++;
        } else {
          stateMap2[s2] = -1;
        }
      }
      checkAbort();
      int prop = 0;
      final int prop1 = rel1.getNumberOfPropositions();
      final int prop2 = rel2.getNumberOfPropositions();
      if (prop1 + prop2 > 0) {
        prop = 1;
      }
      final String name = "{" + rel1.getName() + "||" + rel2.getName() +"}";
      final ComponentKind kind ;
      if (rel1.getKind() == ComponentKind.SPEC &&
          rel2.getKind() == ComponentKind.SPEC) {
        kind = ComponentKind.SPEC;
      } else {
        kind = ComponentKind.PLANT;
      }
      final ListBufferTransitionRelation synchRel =
        new ListBufferTransitionRelation(name, kind,
                                         synchEventEncoding.size(),
                                         prop, finalStateNum,
                                         rel1.getConfiguration());
      for (int e1 = EventEncoding.NONTAU; e1 < eventMap1.length; e1++) {
        final int e = eventMap1[e1];
        final byte status = rel1.getProperEventStatus(e1);
        synchRel.setProperEventStatus(e, status);
      }
      for (int e2 = EventEncoding.NONTAU; e2 < eventMap2.length; e2++) {
        final int e = eventMap2[e2];
        final byte status = rel2.getProperEventStatus(e2);
        synchRel.setProperEventStatus(e, status);
      }
      int code = 0;
      final TransitionIterator iter1 = rel1.createSuccessorsReadOnlyIterator();
      final TransitionIterator iter2 = rel2.createSuccessorsReadOnlyIterator();
      for (int s1 = 0; s1 < rel1.getNumberOfStates(); s1++) {
        final int code1 = stateMap1[s1];
        if (code1 >= 0) {
          for (int s2 = 0; s2 < rel2.getNumberOfStates(); s2++) {
            final int code2 = stateMap2[s2];
            if (code2 >= 0) {
              if (rel1.isInitial(s1) && rel2.isInitial(s2)) {
                synchRel.setInitial(code, true);
              }
              if (prop > 0) {
                if ((prop1 == 0 || rel1.isMarked(s1, 0)) &&
                    (prop2 == 0 || rel2.isMarked(s2, 0))) {
                  synchRel.setMarked(code, 0, true);
                }
              }
              if (nodeList != null) {
                final SimpleNodeProxy node1 = nodeList1.get(s1);
                final String name1 = node1.getName();
                final SimpleNodeProxy node2 = nodeList2.get(s2);
                final String name2 = node2.getName();
                final String nodeName = name1 + ":" + name2;
                final SimpleNodeProxy node =
                  mFactory.createSimpleNodeProxy(nodeName);
                nodeList.add(node);
                checkAbort();
              }
              iter1.resetState(s1);
              while (iter1.advance()) {
                final int event = eventMap1[iter1.getCurrentEvent()];
                final int target1 = stateMap1[iter1.getCurrentTargetState()];
                final int target = reachableStates2 * target1 + code2;
                synchRel.addTransition(code, event, target);
                checkAbort();
              }
              iter2.resetState(s2);
              while (iter2.advance()) {
                final int event = eventMap2[iter2.getCurrentEvent()];
                final int target2 = stateMap2[iter2.getCurrentTargetState()];
                final int target = reachableStates2 * code1 + target2;
                synchRel.addTransition(code, event, target);
                checkAbort();
              }
              code++;
            }
          }
        }
      }
      final Collection<EFSMVariable> variables1 = efsmTR1.getVariables();
      final Collection<EFSMVariable> variables2 = efsmTR2.getVariables();
      final Collection<EFSMVariable> variables =
        new THashSet<>(variables1.size() + variables2.size());
      variables.addAll(variables1);
      variables.addAll(variables2);
      synchResult = new EFSMTransitionRelation(synchRel, synchEventEncoding,
                                               variables, nodeList);
      return synchResult;
    } finally {
      final long stop = System.currentTimeMillis();
      final long difftime = stop - start;
      recordRunTime(difftime);
      if (synchResult != null) {
        statistics.recordFinish(synchResult, true);
      } else {
        statistics.recordOverflow();
      }
      tearDown();
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private boolean mSourceInfoEnabled;

}
