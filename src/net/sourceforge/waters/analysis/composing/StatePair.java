package net.sourceforge.waters.analysis.composing;

import java.lang.Object;

import net.sourceforge.waters.model.des.StateProxy;

public class StatePair extends Object{
	
	public StatePair(){
	}
	
	public StatePair(StateProxy source,
	                 StateProxy target){
		mSource = source;
		mTarget = target;
	}
	
	public StateProxy getSource() {
	  return mSource;
	}
	
	public StateProxy getTarget() {
	  return mTarget;
	}
	
	public boolean equals(Object obj){
		if(obj != null && obj.getClass() == getClass()){
		  StatePair temp = (StatePair)obj;
		  if (mSource == temp.getSource()
		    &&mTarget == temp.getTarget()) {
		    return true;
			} else return false;
		}else {
		   return false;
		 }
	}
	
	public int hashCode(){
		return (mSource.getName()+mTarget.getName()).hashCode();
	}
	
	private StateProxy mSource;
	private StateProxy mTarget;
}
