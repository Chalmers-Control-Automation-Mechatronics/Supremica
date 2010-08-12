//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   DeterministicStateADT
//###########################################################################
//# $Id: Projection2.java 5752 2010-06-04 04:53:20Z craig $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Collection;
import net.sourceforge.waters.model.des.StateProxy;

public class CollectionDeterministic
{

public CollectionDeterministic(final int initialSize)
{
  mDetStates = new ArrayList<DeterministicState>(initialSize);
}

public void insert(final int index, final DeterministicState d){
  if(!mDetStates.contains(d)){
    mDetStates.add(index, d);
  }
}

public void remove(final DeterministicState d){
  mDetStates.remove(d);
}

public int member(final DeterministicState d){
 return mDetStates.indexOf(d);
}

public DeterministicState getState(final DeterministicState d){
  DeterministicState result = null;
  for(final DeterministicState state : mDetStates){
    if(state.compare(d) == 0){
      result = state;
    }
  }
  return result;
}

public int getStateName(final StateProxy s){
  int result = -1;
  for(final DeterministicState state : mDetStates){
    if(state.getProxy() == s){
      return result = mDetStates.indexOf(state);
    }
  }
  return result;
}

public int getName(final DeterministicState d){
  return mDetStates.indexOf(d);
}

public Collection<StateProxy> getAllStateProxy(){
  final ArrayList<StateProxy> result = new ArrayList<StateProxy>();
  for(final DeterministicState state : mDetStates){
    result.add(state.getProxy());
  }
  return result;
}


public void setStateProxy(final DeterministicState d){
  d.setProxy(getName(d));
}

public DeterministicState takeState(){
  final DeterministicState result = mDetStates.get(mDetStates.size()-1);
  remove(result);

  return result;
}

public int getSize(){
  return mDetStates.size();
}

  private final ArrayList<DeterministicState> mDetStates;
}
