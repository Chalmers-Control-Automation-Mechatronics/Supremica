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
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.des.StateProxy;

public class CollectionDeterministic
{

public CollectionDeterministic(final int initialSize)
{
  mDetStates = new HashMap<DeterministicState, DeterministicState>(initialSize);
}

public boolean insert(final DeterministicState d, final int name){
  if(!mDetStates.containsKey(d))
  {
    d.setProxy(name);
    mDetStates.put(d, d);
    return true;
  }
  return false;
}

public void remove(final DeterministicState d){
  mDetStates.remove(d);
}

public boolean member(final DeterministicState d){
 return mDetStates.containsKey(d);
}

public int getName(final DeterministicState d){
  return mDetStates.get(d).getName();
}

public DeterministicState getState (final int name){
  return mDetStates.get(name);
}

public Collection<StateProxy> getAllStateProxy(){
  final ArrayList<StateProxy> result = new ArrayList<StateProxy>();
  for(final DeterministicState state : mDetStates.values()){
    result.add(state.getProxy());
  }
  return result;
}

public StateProxy getStateProxy(final DeterministicState d){
  if(mDetStates.containsKey(d)){
    return mDetStates.get(d).getProxy();
  }
  return null;

}

public int getSize(){
  return mDetStates.size();
}

  private final Map<DeterministicState, DeterministicState> mDetStates;
}
