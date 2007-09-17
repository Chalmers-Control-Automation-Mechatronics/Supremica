package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

import java.util.LinkedList;

import org.supremica.automata.ExtendedAutomaton;

public class EFA extends ExtendedAutomaton{
	
	/**
    *
    * list of states to store added state
    * in ExtendedAutomata
    *
    */
   private LinkedList<String> states;
   
   /**
    * list of events to store added event
    * in ExtendedAutomaton
    */
   private LinkedList<String> events;
   
   private Module module;

   public EFA(String name, Module m){
	   super(name,m,true);
	   
	   module = m;
	   
	   states = new LinkedList<String>();
	   events = new LinkedList<String>();
   }
   
   public void addTransition(String from, String to,
		   String event, String guard, String action ){
	   
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
   /**
    * Add new state String s to automaton.
    * if state s already present or null do
    * nothing.
    * 
    * First added state will be initial state
    * @param s
    */
   public void addState(String s){
       
       //check in data
       if(s == null){
           return;
       }else if(s.length() == 0){
    	   return;
       }
       
       //check if we already added this state
       if(states.contains(s)){
    	   return;
       }
 
       //add new state
       if(states.isEmpty()){
    	   //first state is always
    	   //initial state
    	   super.addInitialState(s);
       }else{
    	   super.addState(s);
       }
       
       states.add(s);
   }
   
   /**
    * add event String event to automata (module).
    * if e already exist or null do nothing.
    * 
    * @param event events separated whit ; tex. e1;e2;
    */
   protected void addEvent(String event){
       
       //check in data
       if(event == null ){
           return;
       }else if(event.length() == 0){
    	   return;
       }
       
       //parse event
       //events are separated whit ; tex event1;event2
       String[] es = event.split(";");
       
       //add new event to automata
       for(int i = 0; i < es.length; i++){
    	   module.addEvent(es[i]);
       }
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
       String s = nextState();
       addState(s);
       return s;
   }
   
   /**
    * return true if state already in list of states
    * @param state
    * @return true if state exists.
    */
   public boolean stateExist(String state){
       return states.contains(state);
   }
   
   /**
    * return true if event already in list of events
    * @param event
    * @return
    */
   public boolean eventExist(String event){
       return events.contains(event);
   }
   
   public Module getModule(){
	   return module;
   }
}
