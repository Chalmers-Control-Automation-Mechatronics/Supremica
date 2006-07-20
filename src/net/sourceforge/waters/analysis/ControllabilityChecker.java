//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.java,v 1.2 2006-07-20 02:28:36 robi Exp $
//###########################################################################

//Name: Jinjian Shi
//ID  : 1010049

package net.sourceforge.waters.analysis;

import java.math.BigInteger;
import java.io.PrintStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.BitSet;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A dummy implementation of a controllability checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is controllable.</P>
 *
 * @see ModelChecker
 *
 * @author Robi Malik
 */

public class ControllabilityChecker extends ModelChecker
{
  //
  private static ProductDESProxy model;                 
  
  //
	private static Set<AutomatonProxy> automatonSet;
	private static Set<BigInteger>     systemSet;   // Future changable
	private static ArrayList<AutomatonProxy> plantList;
	private static ArrayList<AutomatonProxy> specList;
	
	//Transition map
	private static ArrayList<HashMap<Pair,StateProxy>> plantTransitionMap;
	private static ArrayList<HashMap<Pair,StateProxy>> specTransitionMap;
	
	//Level states storage
	private static ArrayList<Integer> indexList;
	private static BlockedArrayList<BigInteger> stateList;
	
	//For encoding/decoding
  private static ArrayList<ArrayList<StateProxy>> codingList;	
	
	//Temporate Variables
	private boolean found;
	private int i;
	private int automatonSize;
	private Integer a;
	private BigInteger bi;
	
	private StateProxy sp;
	private StateProxy temp;
	private EventProxy ep;
	private EventProxy errorEvent;
	private ArrayList<StateProxy> codes;
	private ArrayList<StateProxy> systemState;
	private ArrayList<StateProxy> successor;
	private HashMap<Pair,StateProxy> hm;
	private Pair pair;
	
	private static long startTime;
	private static long endTime;
	
  //#########################################################################
  //# Constructors
  /**
   * Creates a new controllability checker to check a particular model.
   * @param  model   The model to be checked by this controllability checker.
   * @param  factory Factory used for trace construction.
   */
  public ControllabilityChecker(final ProductDESProxy model,
				final ProductDESProxyFactory factory)
  {
    super(model, factory);
    this.model = model;
    
    automatonSet = this.getModel().getAutomata();
    systemSet = new HashSet<BigInteger>();
    plantList = new ArrayList<AutomatonProxy>();
    specList = new ArrayList<AutomatonProxy>();
		plantTransitionMap = new ArrayList<HashMap<Pair,StateProxy>>();
		specTransitionMap = new ArrayList<HashMap<Pair,StateProxy>>();
    
    indexList = new ArrayList<Integer>();
    stateList = new BlockedArrayList<BigInteger>(BigInteger.class);
    systemState = new ArrayList<StateProxy>();
    hm = new HashMap<Pair,StateProxy>();
		pair = new Pair();
		
    codes = new ArrayList<StateProxy>();
    codingList= new ArrayList<ArrayList<StateProxy>>();		
		bi = BigInteger.valueOf(0);
		
		startTime = 0;
		endTime = 0;
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this controllability checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * the result of checking the property is known and can be queried
   * using the {@link #getResult()} and {@link #getCounterExample()}
   * methods.
   * Presently, this is a dummy implementation that nothing but always
   * returns <CODE>true</CODE>.
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.
   *         The same value can be queried using the {@link #getResult()}
   *         method.
   */
  public boolean run()
  {
		boolean result = this.getResult();
    System.out.println("Total Time (in milliSeconds): "+(endTime-startTime));		
		return result;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the result of controllability checking.
   * @return <CODE>true</CODE> if the model was found to be controllable,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException in all cases, because this method is
   *         not yet implemented.
   */
  public boolean getResult() 
  {
		Set<StateProxy> stateSet;
		ArrayList<ArrayList<StateProxy>> specCodingList = new ArrayList<ArrayList<StateProxy>>();
		ArrayList<StateProxy> specSystemState = new ArrayList<StateProxy>();
		
		//Separate the automatons by kind
    for (AutomatonProxy ap : automatonSet) {
      //Get all states
    	stateSet = ap.getStates();
    	//Encoding states to binary values
    	codes = new ArrayList<StateProxy>(stateSet);   	
    	
    	//Find initial state
    	StateProxy initialState = null;
    	for (StateProxy sp : stateSet) {
    		if (sp.isInitial()) {initialState = sp; break;}
    	}    	
    	if (ap.getKind() == ComponentKind.PLANT) {
    		plantList.add(ap);
    		codingList.add(codes);
    		systemState.add(initialState);
				for (TransitionProxy tp : ap.getTransitions()) {
					pair = new Pair(tp.getSource(),tp.getEvent());
					hm.put(pair,tp.getTarget());
				}
				plantTransitionMap.add(hm);
    	}
    	else if (ap.getKind() == ComponentKind.SPEC){
    	  specList.add(ap);
    	  specCodingList.add(codes);
    	  specSystemState.add(initialState);
				for (TransitionProxy tp : ap.getTransitions()) {
					pair = new Pair(tp.getSource(),tp.getEvent());
					hm.put(pair,tp.getTarget());
				}
				specTransitionMap.add(hm);
      }
    }
    codingList.addAll(specCodingList);
    automatonSize = codingList.size();
    systemState.addAll(specSystemState);
		System.out.println("Start ...............");
		startTime = System.currentTimeMillis();
    return this.isControllable(systemState);
  }
  
  /**   
   * @parameter HashMap<AutomatonProxy, StateProxy> stateSet
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.   
   */  
  private boolean isControllable(ArrayList<StateProxy> sState){
	  
    boolean transitionPossible = true;
    boolean enabled            = true;
    boolean controllable       = false;
    
    systemSet.add(encode(sState));
    stateList.add(encode(sState));
    indexList.add(stateList.size()-1);
    
    int j,k = 0 ;
    int indexSize = 0 ;
    
    AutomatonProxy ap ;
    AutomatonProxy as ;
    
		while(true){
			//System.out.println("Next Level ");
			//For each current state in the current level, check its controllability
			indexSize = indexList.size();
			for (j=(indexSize==1)?0:(indexList.get(indexSize-2)+1);j<=indexList.get(indexSize-1);j++){
			  systemState = decode(stateList.get(j));
				//System.out.println("Next State ");
				//printState(systemState);				
				for (EventProxy event : model.getEvents()) {
				  // Retrieve all enabled events
					successor = new ArrayList<StateProxy>(systemState);
					//System.out.println("Next Event "+event.getName());
				  enabled = true;
					transitionPossible = true;
				  for (i=0; i<plantList.size();i++) {
				    ap = plantList.get(i);
				    if (ap.getEvents().contains(event)){
				      transitionPossible = false;
							pair = new Pair(systemState.get(i),event);
							temp = plantTransitionMap.get(i).get(pair);
				      if (temp != null){
								transitionPossible = true;
								successor.set(i,temp);							
							}
						}
						if (!transitionPossible) {
							enabled = false;
							break;
						}
					}
					if (!enabled) {
						//System.out.println(event.getName()+" is disabled by"+plantList.get(i).getName());
						enabled = false;
						continue;
					}
					
					// Check controllability of current state				  
				  if (event.getKind() == EventKind.UNCONTROLLABLE) {
				    for (i=0; i<specList.size();i++) {
				    	ap = specList.get(i);
				    	if (ap.getEvents().contains(event)){
				    	  controllable = false;
								pair = new Pair(systemState.get(i+plantList.size()),event);
								temp = specTransitionMap.get(i).get(pair);
								if (temp != null){
									controllable = true;
									successor.set(i+plantList.size(),temp);							
								}				    	  
				    	  if (!controllable) {			    	    
									System.out.println(systemSet.size());
									errorEvent = event;
									endTime = System.currentTimeMillis();
				    	    return false;
				    	  }
				    	}
				  	}
				  } else {					
				  	for (k=0;k<specList.size();k++){
							as = specList.get(k);
							transitionPossible = true;
				    	if (as.getEvents().contains(event)){
				      	transitionPossible = false;
								pair = new Pair(systemState.get(k+plantList.size()),event);
								temp = specTransitionMap.get(k).get(pair);
				      	if (temp != null){
									transitionPossible = true;
									successor.set(k+plantList.size(),temp);			      	  
				      	}
				      	if (!transitionPossible) {
									//System.out.println("Spec "+as.getName()+" disabled "+event.getName());												
									break;
								}
				    	}	
				  	}								
				  	if (!transitionPossible) {
				    	enabled = false;								
							//System.out.println(event.getName()+" is disabled");
				    	continue;
				  	}		
				  }	  
				  				  
					//System.out.println("Event "+event+" generate ");
					//printState(successor);
				 	// Encode the new system state and put it into stateList
				 	bi = encode(successor);
				 	if (!systemSet.contains(bi)) {
				    stateList.add(bi);
				    systemSet.add(bi);
				  }
				}
      }
      // If stateList has added a new state, update indexList at the last loop of current level
			if (stateList.size()!=(indexList.get(indexSize-1)+1)) {
				indexList.add(stateList.size()-1);
			}
			else break;			
		}		
		System.out.println(systemSet.size());
		endTime = System.currentTimeMillis();
		return true;
	}  	
  
	//Encoding
  private BigInteger encode(ArrayList<StateProxy> sState){
		bi = BigInteger.valueOf(0);
		for (i = 0; i<automatonSize; i++){
		  codes = codingList.get(i);
			a = codes.indexOf(sState.get(i));
			bi = bi.shiftLeft(BigInteger.valueOf(codes.size()).bitLength()).add(BigInteger.valueOf(a));
		}
		return bi;
	}
	//Decoding
	private ArrayList<StateProxy> decode(BigInteger bi){
		systemState = new ArrayList<StateProxy>(systemState);
		for (i=automatonSize-1;i>-1;i--){
		  codes = codingList.get(i);
			a = bi.and(BigInteger.valueOf(
			  (int)Math.pow(2,BigInteger.valueOf(codes.size()).bitLength())-1)).intValue();
			bi = bi.shiftRight(BigInteger.valueOf(codes.size()).bitLength());
			systemState.set(i, codingList.get(i).get(a));
		}
		return systemState;
	}
	
	private void printStateList(){
	  for (BigInteger bi : stateList){
	    systemState = decode(bi);
	    printState(systemState);
	  }
	}
	
	private void printState(ArrayList<StateProxy> systemState){
	  for (StateProxy sp : systemState) {
		  System.out.print(sp.getName());
		}
		System.out.println("\n");
	}
	
  /**
   * Gets a counterexample if the model was found to be not controllable.
   * representing a controllability error trace. A controllability error
   * trace is a nonempty sequence of events such that all except the last
   * event in the list can be executed by the model. The last event in list
   * is an uncontrollable event that is possible in all plant automata, but
   * not in all specification automata present in the model. Thus, the last
   * step demonstrates why the model is not controllable.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this controllability checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  public SafetyTraceProxy getCounterExample()
  {
    // The following creates a trace that consists of all the events in
    // the input model.
    // This code is only here to demonstrate the use of the interfaces.
    // IT DOES NOT GIVE A CORRECT TRACE!
		final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String tracename = desname + ":uncontrollable";   
    final List<EventProxy> tracelist = new LinkedList<EventProxy>();
		
		boolean transitionPossible;
    boolean enabled;
		
		int j,k = 0 ;
		int indexSize = indexList.size();    
    
    AutomatonProxy ap ;
    AutomatonProxy as ;
		ArrayList<StateProxy> errorState;		
		
		tracelist.add(0,errorEvent);
		
		while(true){			
			errorState = new ArrayList<StateProxy>(systemState);			
			indexList.remove(--indexSize);
			if(indexList.size()==0) break;				
			for (j=(indexSize==1)?0:(indexList.get(indexSize-2)+1);j<=indexList.get(indexSize-1);j++){
			  systemState = decode(stateList.get(j));
				//System.out.println("Next State ");
				//printState(systemState);				
				for (EventProxy event : model.getEvents()) {
				  // Retrieve all enabled events
					successor = new ArrayList<StateProxy>(systemState);
					//System.out.println("Next Event "+event.getName());
				  enabled = true;
					transitionPossible = true;
				  for (i=0; i<plantList.size();i++) {
				    ap = plantList.get(i);
				    if (ap.getEvents().contains(event)){
				      transitionPossible = false;
							pair = new Pair(systemState.get(i),event);
							temp = plantTransitionMap.get(i).get(pair);
				      if (temp != null){
								transitionPossible = true;
								successor.set(i,temp);								
							}
						}
						if (!transitionPossible) {
							enabled = false;
							break;
						}
					}
					if (!enabled) {
						//System.out.println(event.getName()+" is disabled by"+plantList.get(i).getName());
						enabled = false;
						continue;
					}
															
				  for (k=0;k<specList.size();k++){
						as = specList.get(k);
						transitionPossible = true;
			    	if (as.getEvents().contains(event)){
			      	transitionPossible = false;
							pair = new Pair(systemState.get(k+plantList.size()),event);
							temp = specTransitionMap.get(k).get(pair);
				     	if (temp != null){
								transitionPossible = true;
								successor.set(k+plantList.size(),temp);			      	  
				     	}
			      	if (!transitionPossible) {
								//System.out.println("Spec "+as.getName()+" disabled "+event.getName());												
								break;
							}
						}	
				  }								
				 	if (!transitionPossible) {
				   	enabled = false;								
						//System.out.println(event.getName()+" is disabled");
			    	continue;
			  	}				  
					if(successor.equals(errorState)){
						tracelist.add(0,event);
						break;
					}
				}
				if(successor.equals(errorState)) break;			
			}
		}
    final SafetyTraceProxy trace =
      factory.createSafetyTraceProxy(tracename, des, tracelist);
    return trace;
  }

}
