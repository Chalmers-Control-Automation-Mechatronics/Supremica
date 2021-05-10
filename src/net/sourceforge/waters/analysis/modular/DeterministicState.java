//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;

public class DeterministicState
{
  public DeterministicState(final Set<Integer> states)
  {
    mSetStates = new int[states.size()];
    int i = 0;
    for (final int state : states) {
      mSetStates[i++] = state;
    }
    Arrays.sort(mSetStates);
    mProxy = null;
    mName = -1;
  }

  @Override
  public int hashCode(){
    return Arrays.hashCode(mSetStates);
  }

  @Override
  public boolean equals(final Object o){
    if(o != null && o.getClass() == getClass()){
      final DeterministicState detState = (DeterministicState) o;
      return Arrays.equals(mSetStates, detState.mSetStates);
    } else {
      return false;
    }
  }

  public int size(){
    return mSetStates.length;
  }

  public int getState(final int state){
    return mSetStates[state];
  }

  public int[] getSetState(){
    return mSetStates;
  }

  public StateProxy getProxy(){
    return mProxy;
  }
  public void setProxy(final int name){
    mProxy = new MemStateProxy(name);
  }

  public static DeterministicState merge(final DeterministicState d, final DeterministicState s){
    if(d == null){
      return s;
    }
    if(s == null){
      return d;
    }
    final HashSet<Integer> set = new HashSet<Integer>();
    for(final int i : d.mSetStates){
      set.add(i);
    }
    for(final int i : s.mSetStates){
      set.add(i);
    }
    return new DeterministicState(set);
  }

  public int getName()
  {
    return mName;
  }

  public void setName(final int name){
    mName = name;
  }

  private static class MemStateProxy
  implements StateProxy
  {
  private final int mProxyName;

  public MemStateProxy(final int name)
  {
    mProxyName = name;
  }

  public Collection<EventProxy> getPropositions()
  {
    return Collections.emptySet();
  }

  public boolean isInitial()
  {
    return mProxyName == 0;
  }

  public MemStateProxy clone()
  {
    return new MemStateProxy(mProxyName);
  }

  public String getName()
  {
    return Integer.toString(mProxyName);
  }

  @SuppressWarnings("unused")
  public boolean refequals(final Object o)
  {
    if (o instanceof NamedProxy) {
      return refequals((NamedProxy) o);
    }
    return false;
  }

  public boolean refequals(final NamedProxy o)
  {
    if (o instanceof MemStateProxy) {
      final MemStateProxy s = (MemStateProxy) o;
      return s.mProxyName == mProxyName;
    } else {
      return false;
    }
  }

  public int refHashCode()
  {
    return mProxyName;
  }

  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor =
  (ProductDESProxyVisitor) visitor;
    return desvisitor.visitStateProxy(this);
  }

  public Class<StateProxy> getProxyInterface()
  {
    return StateProxy.class;
  }

  public int compareTo(final NamedProxy n)
  {
    return n.getName().compareTo(getName());
  }
  }


  private final int[] mSetStates;
  private StateProxy mProxy;
  private int mName;

}
