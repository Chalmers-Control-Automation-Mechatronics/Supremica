//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   DeterministicStateADT
//###########################################################################
//# $Id: Projection2.java 5752 2010-06-04 04:53:20Z craig $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;

public class DeterministicState
{
  public DeterministicState(final int size)
  {
    mSetStates = new int[size];
    mName = -1;
    mProxy = null;
  }

  public boolean insert(final int pos, final int state){
    if(Arrays.binarySearch(mSetStates, state) >= 0){
      mSetStates[pos] = state;
      return true;
    }
    return false;
  }

  public void clear()
  {
    mSetStates = new int[0];
  }

  public int compare(final DeterministicState d)
  {
    if(d.size() != this.size()){
      return 1;
    } else {
      for(int i=0; i <d.mSetStates.length; i++){
        if(!(this.mSetStates[i] == d.mSetStates[i])){
          return 1;
        }
      }
    }
    return 0;
  }

  public int size(){
    return mSetStates.length;
  }

  public DeterministicState copy(){
    final DeterministicState temp = new DeterministicState(mSetStates.length);
    temp.mName = this.mName;
    temp.mSetStates = this.mSetStates.clone();

    return temp;
  }

  public int getState(final int state){
    return mSetStates[state];
  }

  public void sortStates(){
    Arrays.sort(mSetStates);
  }


  public StateProxy getProxy(){
    return mProxy;
  }

  public void setProxy(final int name){
    mProxy = new MemStateProxy(name);
  }


  private static class MemStateProxy
  implements StateProxy
  {
  private final int mName;

  public MemStateProxy(final int name)
  {
    mName = name;
  }

  public Collection<EventProxy> getPropositions()
  {
    return Collections.emptySet();
  }

  public boolean isInitial()
  {
    return mName == 0;
  }

  public MemStateProxy clone()
  {
    return new MemStateProxy(mName);
  }

  public String getName()
  {
    return Integer.toString(mName);
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
      return s.mName == mName;
    } else {
      return false;
    }
  }

  public int refHashCode()
  {
    return mName;
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


  private int[] mSetStates;
  private int mName;
  private StateProxy mProxy;

}
