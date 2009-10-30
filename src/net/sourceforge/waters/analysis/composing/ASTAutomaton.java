package net.sourceforge.waters.analysis.composing;

import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


public class ASTAutomaton {

	public ASTAutomaton(AutomatonProxy automaton,
	                    Map<EventProxy,Set<EventProxy>> revents){
		mAutomaton = automaton;
		mRevents = revents;	
	}
	
	public AutomatonProxy getAutomaton(){
	  return mAutomaton;
	}
	
	public Map<EventProxy,Set<EventProxy>> getRevents(){
	  return mRevents;
	}
		
	public boolean equals(Object obj){
		if(obj != null && obj.getClass() == getClass()){
		  ASTAutomaton temp = (ASTAutomaton)obj;
			return   (mAutomaton.equals(temp.getAutomaton())
			       &&mRevents.equals(temp.getRevents()));
		}else {
		   return false;
		 }
	}
	
	public int hashCode(){
		return mAutomaton.hashCode()+5*mRevents.hashCode();
	}
	
	private AutomatonProxy mAutomaton;
	private Map<EventProxy,Set<EventProxy>> mRevents;
}
