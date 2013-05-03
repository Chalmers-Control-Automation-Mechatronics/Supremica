package net.sourceforge.waters.analysis.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

public class EFSMSynchronization
{
  //#########################################################################
  //# Constructors
  public EFSMSynchronization(final ModuleProxyFactory factory)
  {
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
  throws OverflowException
  {
    final ListBufferTransitionRelation rel1 = efsmTR1.getTransitionRelation();
    final ListBufferTransitionRelation rel2 = efsmTR2.getTransitionRelation();
    final int reachableStates1 = rel1.getNumberOfReachableStates();
    final int reachableStates2 = rel2.getNumberOfReachableStates();
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
    final EFSMEventEncoding synchEventEncoding  = new EFSMEventEncoding(eventNum);
    final int[] eventMap1 = new int[eventEncoding1.size()];
    for (int i=0; i< eventEncoding1.size(); i++) {
      final ConstraintList update = eventEncoding1.getUpdate(i);
      eventMap1[i] = synchEventEncoding.createEventId(update);
    }
    final int[] eventMap2 = new int[eventEncoding2.size()];
    for (int i=0; i< eventEncoding2.size(); i++) {
      final ConstraintList update = eventEncoding2.getUpdate(i);
      eventMap2[i] = synchEventEncoding.createEventId(update);
    }
    final int[] stateMap1 = new int[rel1.getNumberOfStates()];
    int currentState1 = 0;
    for (int s1=0; s1<rel1.getNumberOfStates(); s1++ ){
      if (rel1.isReachable(s1)) {
        stateMap1[s1] = currentState1;
        currentState1++;
      } else {
        stateMap1[s1] = -1;
      }
    }
    int currentState2 = 0;
    final int[] stateMap2 = new int[rel2.getNumberOfStates()];
    for (int s2=0; s2<rel2.getNumberOfStates(); s2++ ){
      if (rel2.isReachable(s2)) {
        stateMap2[s2] = currentState2;
        currentState2++;
      } else {
        stateMap2[s2] = -1;
      }
    }
    int prop = 0;
    final int prop1 = rel1.getNumberOfPropositions();
    final int prop2 = rel2.getNumberOfPropositions();
    if (prop1 + prop2 > 0) {
      prop = 1;
    }
    ComponentKind kind ;
    if(rel1.getKind() == ComponentKind.SPEC && rel2.getKind() ==
      ComponentKind.SPEC) {
      kind = ComponentKind.SPEC;
    } else {
      kind = ComponentKind.PLANT;
    }
    final ListBufferTransitionRelation synchRel =
      new ListBufferTransitionRelation("{" + rel1.getName() + "||" +
        rel2.getName() +"}", kind,
        synchEventEncoding.size(),
        prop, finalStateNum,
        rel1.getConfiguration());
    int code = 0;
    final TransitionIterator iter1 = rel1.createSuccessorsReadOnlyIterator();
    final TransitionIterator iter2 = rel2.createSuccessorsReadOnlyIterator();
    for (int s1=0; s1<rel1.getNumberOfStates(); s1++ ){
      final int code1 = stateMap1[s1];
      if (code1 >= 0) {
        for (int s2=0; s2<rel2.getNumberOfStates(); s2++ ){
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
              final String name = name1 + ":" +name2;
              final SimpleNodeProxy node = mFactory.createSimpleNodeProxy(name);
              nodeList.add(node);
            }
            iter1.resetState(s1);
            while (iter1.advance()) {
              final int event= eventMap1[iter1.getCurrentEvent()];
              final int target1 = stateMap1[iter1.getCurrentTargetState()];
              final int target = reachableStates2 * target1 + code2;
              synchRel.addTransition(code, event, target);
            }
            iter2.resetState(s2);
            while (iter2.advance()) {
              final int event= eventMap2[iter2.getCurrentEvent()];
              final int target2 = stateMap2[iter2.getCurrentTargetState()];
              final int target = reachableStates2 * code1 + target2;
              synchRel.addTransition(code, event, target);
            }
            code++;
          }
        }
      }
    }
    final Collection<EFSMVariable> variables1 = efsmTR1.getVariables();
    final Collection<EFSMVariable> variables2 = efsmTR2.getVariables();
    final Collection<EFSMVariable> variables = new THashSet<EFSMVariable>
    (variables1.size() + variables2.size());
    variables.addAll(variables1);
    variables.addAll(variables2);

    return new EFSMTransitionRelation(synchRel, synchEventEncoding,
                                      variables,nodeList);
  }

  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private boolean mSourceInfoEnabled;
}
