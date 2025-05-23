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

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionRelation;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

/**
 * An implementation of the {@link AbstractEFATransitionRelation}.
 *
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAComponent
 extends AbstractEFATransitionRelation<Integer>
{

  public SimpleEFAComponent(final String name,
                            final int[] variables,
                            final SimpleEFAVariableContext varContext,
                            final SimpleEFAStateEncoding stateEncoding,
                            final SimpleEFALabelEncoding labelEncoding,
                            final ListBufferTransitionRelation rel,
                            final int[] blockedEvents,
                            final ComponentKind kind)
  {
    super(rel, labelEncoding, null);
    super.setName(name);
    mIdentifier = SimpleEFAHelper.getSimpleIdentifierSubject(name);
    mStateEncoding = stateEncoding;
    mVariables = new TIntArrayList(variables);
    mVarContext = varContext;
    mStateVars = findStateVariables(mStateEncoding.getInitialStateId());
    mBlockedEvents = new TIntArrayList(blockedEvents);
    mKind = kind != null ? kind : ComponentKind.PLANT;
    rel.setKind(mKind);
    mIsStructurallyDeterministic = rel.isDeterministic();
  }

  public SimpleEFAComponent(final String name,
                            final int[] variables,
                            final SimpleEFAVariableContext varContext,
                            final SimpleEFAStateEncoding stateEncoding,
                            final SimpleEFALabelEncoding labels,
                            final ListBufferTransitionRelation rel)
  {
    this(name, variables, varContext, stateEncoding, labels, rel, null, null);
  }

  public SimpleEFAEventEncoding getEventEncoding()
  {
    return getTransitionLabelEncoding().getEventEncoding();
  }

  @Override
  public SimpleEFALabelEncoding getTransitionLabelEncoding()
  {
    return (SimpleEFALabelEncoding) super.getTransitionLabelEncoding();
  }

  public void addVariable(final SimpleEFAVariable variable)
  {
    variable.addTransitionRelation(this);
  }

  public void removeVariable(final SimpleEFAVariable variable)
  {
    variable.removeTransitionRelation(this);
  }

  /**
   * Registers this transition relation by adding its reference to all its
   * variables and events.
   */
  public void register() throws AnalysisException
  {
    final TIntHashSet vars = new TIntHashSet(mUnprimedVars);
    vars.addAll(mPrimedVars);
    if (!vars.isEmpty() && (mVariables.size() != vars.size())) {
      vars.removeAll(mVariables);
      throw new AnalysisException(
       "SimpleEFAComponent > register() > Inconsistency in variable set is detected > " + vars);
    }

    for (final int var : mUnprimedVars.toArray()) {
      mVarContext.getVariable(var).addTransitionRelation(this);
      mVarContext.getVariable(var).addVisitor(this);
    }

    for (final int var : mPrimedVars.toArray()) {
      mVarContext.getVariable(var).addTransitionRelation(this);
      mVarContext.getVariable(var).addModifier(this);
    }

    for (final int var : mStateVars.toArray()) {
      mVarContext.getVariable(var).addUseInState(this);
    }

    final SimpleEFAEventEncoding eventEncoding = getEventEncoding();
    for (final int e : getEvents()) {
      final SimpleEFAEventDecl event = eventEncoding.getEventDecl(e);
      event.addComponent(this);
      eventEncoding.resetEventStatus(event);
    }
  }

  /**
   * Deregisters this transition relation by removing its reference from all its
   * variables and events.
   */
  public void dispose()
  {
    try {
      for (final int var : mUnprimedVars.toArray()) {
        mVarContext.getVariable(var).removeTransitionRelation(this);
        mVarContext.getVariable(var).removeVisitor(this);
      }

      for (final int var : mPrimedVars.toArray()) {
        mVarContext.getVariable(var).removeTransitionRelation(this);
        mVarContext.getVariable(var).removeModifier(this);
      }

      for (final int var : mStateVars.toArray()) {
        mVarContext.getVariable(var).removeUseInState(this);
      }

      final SimpleEFAEventEncoding eventEncoding = getEventEncoding();
      for (final int e : getEvents()) {
        final SimpleEFAEventDecl event = eventEncoding.getEventDecl(e);
        event.removeComponent(this);
        eventEncoding.resetEventStatus(event);
      }
    } catch (final Exception ex) {
    } finally {
      mVariables = null;
      mUnprimedVars = null;
      mPrimedVars = null;
      mStateVars = null;
      mStateEncoding = null;
    }
  }

  public int[] getEvents()
  {
    return getTransitionLabelEncoding().getEventListExceptTau();
  }

  public List<Integer> getTransitionLabels()
  {
    return getTransitionLabelEncoding().getTransitionLabels();
  }

  public int[] getBlockedEvents()
  {
    return mBlockedEvents.toArray();
  }

  public void setBlockedEvents(final TIntArrayList blocked)
  {
    mBlockedEvents = blocked;
  }

  public SimpleEFAStateEncoding getStateEncoding()
  {
    return mStateEncoding;
  }

  public void setStateEncoding(final SimpleEFAStateEncoding encoding)
  {
    mStateEncoding = encoding;
  }

  public SimpleNodeProxy getInitialState()
  {
    return mStateEncoding.getInitialState();
  }

  public void setStructurallyDeterministic(final boolean deterministic)
  {
    mIsStructurallyDeterministic = deterministic;
  }

  public boolean isStructurallyDeterministic()
  {
    return mIsStructurallyDeterministic;
  }

  public boolean isStructurallyNonBlocking()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    for (int source = 0; source < rel.getNumberOfStates(); source++) {
      if (rel.isReachable(source)
       && rel.isDeadlockState(source, SimpleEFAHelper.DEFAULT_MARKING_ID)) {
        return false;
      }
    }
    return true;
  }

  public void setIsEFA(final boolean isEFA)
  {
    mIsEFA = isEFA;
  }

  public boolean isEFA()
  {
    return mIsEFA;
  }

  public void addSystem(final SimpleEFASystem system)
  {
    if (mSystems == null) {
      mSystems = new ArrayList<>();
    }
    mSystems.add(system);
  }

  public void removeSystem(final SimpleEFASystem system)
  {
    if (mSystems != null) {
      mSystems.remove(system);
    }
  }

  public List<SimpleEFASystem> getSystems()
  {
    if (mSystems != null) {
      return mSystems;
    } else {
      return Collections.emptyList();
    }
  }

  public ComponentKind getKind()
  {
    return mKind;
  }

  public void setKind(final ComponentKind kind)
  {
    mKind = kind;
    getTransitionRelation().setKind(kind);
  }

  public IdentifierProxy getIdentifier()
  {
    return mIdentifier;
  }

  @Override
  public String getName()
  {
    return mIdentifier.toString();
  }

  public boolean hasMarkedState()
  {
    return mStateEncoding.hasMarkedState();
  }

  public boolean hasForbiddenState()
  {
    return mStateEncoding.hasForbbidenState();
  }

  public int getNumberOfEvents()
  {
    return getTransitionLabelEncoding().getEventSize();
  }

  public int getNumberOfStates()
  {
    return mStateEncoding.size();
  }

  public SimpleEFAVariableContext getVariableContext()
  {
    return mVarContext;
  }

  public void setUnprimeVariables(final int[] uvars)
  {
    mUnprimedVars = new TIntArrayList(uvars);
  }

  public void setPrimeVariables(final int[] pvars)
  {
    mPrimedVars = new TIntArrayList(pvars);
  }

  public void setStateVariables(final int[] svars)
  {
    mStateVars = new TIntArrayList(svars);
  }

  public void setStateVariables(final int stateId)
  {
    mStateVars = findStateVariables(stateId);
  }

  public TIntArrayList getUnprimeVariables()
  {
    return mUnprimedVars;
  }

  public TIntArrayList getPrimeVariables()
  {
    return mPrimedVars;
  }

  public TIntArrayList getStateVariables()
  {
    return mStateVars;
  }

  public TIntArrayList getVariables()
  {
    return mVariables;
  }

  private TIntArrayList findStateVariables(final int stateId)
  {
    return mVarContext.getVariablesId(SimpleEFAHelper.getStateVariables(mStateEncoding.getAttribute(
     stateId, SimpleEFAHelper.DEFAULT_STATEVALUE_KEY), mVarContext.getVariables()));
  }

  //#########################################################################
  //# Data Members
  public static final Comparator<SimpleEFAComponent> KindComparator
   = new Comparator<SimpleEFAComponent>()
   {
     @Override
     public int compare(final SimpleEFAComponent e1, final SimpleEFAComponent e2)
     {
       if (e1.getKind() == ComponentKind.PLANT && e2.getKind() == ComponentKind.SPEC) {
         return -1;
       }
       if (e1.getKind() == ComponentKind.SPEC && e2.getKind() == ComponentKind.PLANT) {
         return 1;
       }
       return 0;
     }
   };

  private SimpleEFAStateEncoding mStateEncoding;
  private TIntArrayList mVariables;
  private TIntArrayList mBlockedEvents;
  private List<SimpleEFASystem> mSystems;
  private TIntArrayList mUnprimedVars;
  private TIntArrayList mPrimedVars;
  private TIntArrayList mStateVars;
  private boolean mIsStructurallyDeterministic;
  private boolean mIsEFA = true;
  private final IdentifierProxy mIdentifier;
  private ComponentKind mKind;
  private final SimpleEFAVariableContext mVarContext;
}
