package org.supremica.external.avocades.common;

import java.util.LinkedList;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;

import org.supremica.automata.ExtendedAutomaton;

public class EFA extends ExtendedAutomaton{

	/**
    *
    * list of states to store added state
    * in ExtendedAutomata
    *
    */
   private final LinkedList<String> states;

   /**
    * list of events to store added event
    * in ExtendedAutomaton
    */
   private final LinkedList<String> events;

   private final Module module;

   @SuppressWarnings("deprecation")
   public EFA(final String name, final Module m){
	   super(name,m,true);

	   module = m;

	   states = new LinkedList<String>();
	   events = new LinkedList<String>();
   }

   @Override
  public void addTransition(final String from, final String to,
		   String event, final String guard, String action ){

	   this.addEvent(event);

	   //event, guard and action must ends whit ;
	   if(event.length() > 0 && !event.endsWith(";")){
		   event = event.concat(";");
	   }

	   if(action.length() > 0 && !action.endsWith(";")){
		   action = action.concat(";");
	   }

	   //super
	   super.addTransition(from,to,event,guard,action);
   }

   @Override
  public NodeProxy addState(final String name){
	   return this.addState(name, false, false);
   }

   public void addInitialState(final String name){
	   this.addState(name, false, true);
   }

   public void addInitialState(final String name, final boolean accepting){
	   this.addState(name, accepting, true);
   }

   public void addAcceptingState(final String name){
	   this.addState(name, true, false);
   }

   /**
    *
    */
   public NodeProxy addState(final String name, final boolean accepting, final boolean initial){
	 //check in data
       if(name == null){
           return null;
       }else if(name.length() == 0){
    	   return null;
       }

       //check if we already added this state
       if(states.contains(name)){
    	   return null;
       }
       //add new state
       final NodeProxy state = super.addState(name, accepting, initial,false);
       states.add(name);
       return state;
   }

   /**
    * add event String event to automata (module).
    * if e already exist or null do nothing.
    *
    * @param event events separated whit ; tex. e1;e2;
    */
   protected void addEvent(final String event){

       //check in data
       if(event == null ){
           return;
       }else if(event.length() == 0){
    	   return;
       }

       //parse event
       //events are separated whit ; tex event1;event2
       final String[] es = event.split(";");

       //add new event to automata
       for(int i = 0; i < es.length; i++){
    	   if(!eventExist(es[i])){
    		   module.addEvent(es[i]);
    		   events.add(es[i]);
    	   }
       }
   }

   @Override
  public EventDeclProxy addEvent(final String event, final String kind){

       //check in data
       if(event == null ){
           return null;
       }else if(event.length() == 0){
    	   return null;
       }

       //parse event
       //events are separated whit ; tex event1;event2
       final String[] es = event.split(";");

       //add new event to automata
       for(int i = 0; i < es.length; i++){
    	   if(!eventExist(es[i])){
    		   module.addEvent(es[i], kind);
    		   events.add(es[i]);
    	   }
       }
       return null;
   }


   /**
    * returns the name of next state
    * returned by
    * @return next state.
    */
   public String nextState(){
       if(states == null){
           return "s"+0;
       }
       return "s"+states.size();
   }

   /**
    * return last state in list
    * @return last state in list
    */
   public String lastState(){
       return states.getLast();
   }

   /**
    * Add a new unique state and return its name
    * as a String.
    * @return a new state with a unique name.
    */
   public String newUniqueState(){
       final String s = nextState();
       addState(s);
       return s;
   }

   /**
    * return true if state already in list of states
    * @param state
    * @return true if state exists.
    */
   public boolean stateExist(final String state){
       return states.contains(state);
   }

   /**
    * return true if event already in list of events
    */
   public boolean eventExist(final String event){
       return events.contains(event);
   }

   public Module getModule(){
	   return module;
   }
}
