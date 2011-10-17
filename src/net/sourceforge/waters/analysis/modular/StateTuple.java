//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   DeterministicStateADT
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.Arrays;

public class StateTuple
{
  public StateTuple(final int[] states)
  {
    mSetStates = states;
    mName = -1;
  }

  @Override
  public int hashCode(){
    return Arrays.hashCode(mSetStates);
  }

  @Override
  public boolean equals(final Object o){
    if(o != null && o.getClass() == getClass()){
      final StateTuple detState = (StateTuple) o;
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

  public int[] getSetStates(){
    return mSetStates;
  }

  public int getName()
  {
    return mName;
  }

  public void setName(final int name)
  {

    mName = name;
  }

  private final int[] mSetStates;
  private int mName;
}
