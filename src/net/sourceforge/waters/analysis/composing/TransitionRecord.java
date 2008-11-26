package net.sourceforge.waters.analysis.composing;

import java.util.Set;
import java.util.HashSet;

import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;

public class TransitionRecord {
	
	public TransitionRecord(AutomatonProxy automaton,
	                        Set<StatePair> transitions){
		mAutomaton = automaton;
		mTransitions = new HashSet<StatePair>(transitions);
	}
	
	public AutomatonProxy getAutomaton() {
	  return mAutomaton;
	}
	
	public Set<StatePair> getTrans() {
	  return mTransitions;
	}
	
	public boolean equals(Object obj){
		if(obj != null && obj.getClass() == getClass()){
		  TransitionRecord temp = (TransitionRecord)obj;
		  if (mAutomaton == temp.getAutomaton()
		    &&mTransitions.equals(temp.getTrans())) {
		    return true;
			} else return false;
		}else {
		   return false;
		 }
	}
	
	public int hashCode(){
		return mAutomaton.hashCode()+5*mTransitions.hashCode();
	}
	
	private AutomatonProxy mAutomaton;
	private Set<StatePair> mTransitions;
}
