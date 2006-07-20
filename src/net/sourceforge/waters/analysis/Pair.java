package net.sourceforge.waters.analysis;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;

import java.lang.Object;

public class Pair extends Object{
	
	public Pair(){
	}
	
	public Pair(StateProxy s,EventProxy e){
		state_ = s;
		event_ = e;
	}
	
	public StateProxy getState(){
		return state_;
	}
	
	public EventProxy getEvent(){		
		return event_;
	}
	
	public boolean equals(Object obj){
		if(obj instanceof Pair){
			return (state_==((Pair)obj).getState())&&(event_==((Pair)obj).getEvent());
		}
		return false;
	}
	
	public int hashCode(){
		return (state_.getName()+event_.getName()).hashCode();
	}
	
	private StateProxy state_;
	private EventProxy event_;
}
