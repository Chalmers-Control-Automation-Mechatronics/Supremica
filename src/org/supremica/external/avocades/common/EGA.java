package org.supremica.external.avocades.common;

/**
 *	Help class for building transitions. EGA stands for Event Guard and
 *	Action.
 *
 *	@author David Millares
 *
 */
public class EGA {
	
	//
	static final public String AND = " & ";
	static final public String OR = " | ";
	
	//
	private String event="", guard="", action="";
	
	//constructors
	public EGA(){
		init("","","");
	}
	public EGA(String event){
		init(event,"","");
	}
	
	private void init(String event, String guard, String action){
		setEvent(event);
		setGuard(guard);
		setAction(action);
	}
	
	/**
	 * 
	 * @param event
	 */
	public void setEvent(String event){
		if(event.endsWith(";")){
			event = event.substring(0,event.length()-1);
		}
		this.event = event;
	}
	
	//set guard
	public void setGuard(String guard){
		this.guard = guard;
	}
	
	//set action
	public void setAction(String action){
		this.action = action;
	}
	
	//add AND guard
	public void andGuard(String guard){
		if(guard.equals("")){
			return;
		}
		
		if(this.guard.length() == 0){
			setGuard(guard);
		}else{
			this.guard = this.guard + AND + guard;
		}
	}
	
	//add OR guard
	public void orGuard(String guard){
		if(guard.equals("")){
			return;
		}
		
		if(this.guard.length() == 0){
			setGuard(guard);
		}else{
			this.guard = this.guard + OR + guard;
		}
	}
	
	public void addEvent(String event){
		if(event.equals("")){
			return;
		}
		
		if(this.event.length() == 0){
			setEvent(event);
		}else{
			this.event = this.event + ";" + event;
		}
	}
	
	public void addAction(String action){
		if(action.equals("")){
			return;
		}
		
		if(this.action.length() == 0){
			setAction(action);
		}else{
			this.action = this.action + ";" + action;
		}
	}
	
	//get event
	public String getEvent(){
		return event;
	}
	
	//get guard
	public String getGuard(){
		return guard;
	}
	
	//get action
	public String getAction(){
		return action;
	}
}
