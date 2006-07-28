//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.java,v 1.4 2006-07-28 08:02:49 js173 Exp $
//###########################################################################

//Name: Jinjian Shi
//ID  : 1010049

package net.sourceforge.waters.analysis;

import java.math.BigInteger;
import java.io.PrintStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.BitSet;
import java.util.Arrays;

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
	//private static AutomatonProxy[] automatonList;	
	
	//Transition map
	private static ArrayList<int[][]> plantTransitionMap;
	private static ArrayList<int[][]> specTransitionMap;
	
	//Level states storage
	private static ArrayList<Integer> indexList;
	private static BlockedArrayList<BigInteger> stateList;
	
	//For encoding/decoding
  private static ArrayList<ArrayList<StateProxy>> codingList;
	private static ArrayList<EventProxy> eventCodingList;
	private static ArrayList<byte[]> plantEventList;
	private static ArrayList<byte[]> specEventList;	
	private static int[] bitlengthList;
	private static BigInteger[] maskList;
	
	//Temporate Variables	
	private int i,j,k;
	private int automatonSize;
	private int eventSize;
	private int plantSize;
	private Integer a;
	private BigInteger bi;	
	
	private Integer temp;	
	private Integer errorEvent;
	private ArrayList<StateProxy> codes;
	private int[] systemState;
	private int[] successor;	
	private byte[] aneventCodingList;	
	
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
		plantTransitionMap = new ArrayList<int[][]>();
		specTransitionMap = new ArrayList<int[][]>();
		
    indexList = new ArrayList<Integer>();
    stateList = new BlockedArrayList<BigInteger>(BigInteger.class);        
		
    codes = new ArrayList<StateProxy>();
    codingList = new ArrayList<ArrayList<StateProxy>>();	
		eventCodingList = new ArrayList<EventProxy>(model.getEvents());		
		plantEventList = new ArrayList<byte[]>();
		specEventList = new ArrayList<byte[]>();
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
		int ck = 0;	
		int bl = 0;
		BigInteger mask = BigInteger.valueOf(0);
		
		eventSize = eventCodingList.size();
		automatonSize = automatonSet.size();
		bitlengthList = new int[automatonSize];
		maskList = new BigInteger[automatonSize];
		//automatonList = new AutomatonProxy[automatonSize];
		
		//Count Plant size
		for (AutomatonProxy ap : automatonSet) {
			if (ap.getKind() == ComponentKind.PLANT) {
				plantSize++;
			}
		}		
		
		systemState = new int[automatonSize];
		
		//Separate the automatons by kind
    for (AutomatonProxy ap : automatonSet) {
      //Get all states
    	stateSet = ap.getStates();
    	//Encoding states to binary values
    	codes = new ArrayList<StateProxy>(stateSet);
			//Encoding events to binary values	
			aneventCodingList = new byte[eventSize];			
			for (EventProxy evp : ap.getEvents()) {
				aneventCodingList[eventCodingList.indexOf(evp)] = 1;
			}
			//Encoding trantions to binary values
			int stateSize = codes.size();
			int[][] atransition = new int[stateSize][eventSize];
			for (i=0;i<stateSize;i++){
				for (j=0;j<eventSize;j++) {
					atransition[i][j] = -1;
				}
			}
			for (TransitionProxy tp : ap.getTransitions()) {
				atransition[codes.indexOf(tp.getSource())][eventCodingList.indexOf(tp.getEvent())]
					 =  codes.indexOf(tp.getTarget());					
			}
			//Compute bit length and mask
			bl = BigInteger.valueOf(stateSize).bitLength();
			mask = BigInteger.valueOf((1 << BigInteger.valueOf(codes.size()).bitLength())-1);
    	
    	//Find initial state
    	StateProxy initialState = null;
    	for (StateProxy sp : stateSet) {
    		if (sp.isInitial()) {initialState = sp; break;}
    	}    	
    	if (ap.getKind() == ComponentKind.PLANT) {    		
    		//automatonList[ck] = ap;
    		codingList.add(codes);    		
    		systemState[ck] = codes.indexOf(initialState);
				plantEventList.add(aneventCodingList);				
				plantTransitionMap.add(atransition);
				bitlengthList[ck] = bl;
				maskList[ck] = mask;
				ck++;
    	}
    	else if (ap.getKind() == ComponentKind.SPEC){
    	  //automatonList[ck+plantSize] = ap;
    	  specCodingList.add(codes);  
    	  //
    	  //System.out.println(k);
    	  //System.out.println(k+plantSize+" out of boundry "+(automatonSize-1)+" plant size: "+plantSize);  	  
    	  systemState[k+plantSize] = codes.indexOf(initialState);
				specEventList.add(aneventCodingList);				
				specTransitionMap.add(atransition);
				bitlengthList[k+plantSize] = bl;
				maskList[k+plantSize] = mask;
				k++;
      }      
    }		
    codingList.addAll(specCodingList);   
		System.out.println("\nStart ...............");		
		startTime = System.currentTimeMillis();
    return this.isControllable(systemState);
  }
  
  /**   
   * @parameter HashMap<AutomatonProxy, StateProxy> stateSet
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.   
   */  
  private boolean isControllable(int[] sState){
	  
		Set<BigInteger> systemSet = new HashSet<BigInteger>(); // Future changable
		
    boolean transitionPossible = true;
    boolean enabled            = true;
    boolean controllable       = false;
    
		successor = new int[automatonSize];
    systemSet.add(encode(sState));
    stateList.add(encode(sState));
    indexList.add(stateList.size()-1);    
    
    int indexSize = 0 ;
		int eventSize = eventCodingList.size();   
    
		while(true){			
			//For each current state in the current level, check its controllability			
			indexSize = indexList.size();
			for (j=(indexSize==1)?0:(indexList.get(indexSize-2)+1);j<=indexList.get(indexSize-1);j++){			
			  systemState = decode(stateList.get(j));					
				for (int e=0;e<eventSize;e++) {
				  // Retrieve all enabled events									
				  enabled = true;
					transitionPossible = true;
				  for (i=0; i<plantSize;i++) {
						successor[i] = systemState[i];
				    if (plantEventList.get(i)[e] == 1){							
				      transitionPossible = false;							
							temp = plantTransitionMap.get(i)[systemState[i]][e];							
				      if (temp > -1){
								transitionPossible = true;
								successor[i] = temp;							
							}
						}
						if (!transitionPossible) {
							enabled = false;
							break;
						}						
					}
					if (!enabled) {						
						continue;
					}					
				
					// Check controllability of current state				  
				  if (eventCodingList.get(e).getKind() == EventKind.UNCONTROLLABLE) {						
				    for (i=0; i<automatonSize-plantSize;i++) {
							successor[i+plantSize] = systemState[i+plantSize];							
				    	if (specEventList.get(i)[e] == 1){
				    	  controllable = false;								
								temp = specTransitionMap.get(i)[systemState[i+plantSize]][e];
								if (temp > -1){
									controllable = true;
									successor[i+plantSize] = temp;									
								}				    	  
				    	  if (!controllable) {			    	    
									System.out.println(systemSet.size());
									errorEvent = e;									
									endTime = System.currentTimeMillis();
				    	    return false;
				    	  }
				    	}							
				  	}
				  } else {					
				  	for (k=0;k<automatonSize-plantSize;k++){
							transitionPossible = true;
							successor[k+plantSize] = systemState[k+plantSize];
				    	if (specEventList.get(k)[e] == 1) {
				      	transitionPossible = false;
								temp = specTransitionMap.get(k)[systemState[k+plantSize]][e];
				      	if (temp > -1){
									transitionPossible = true;
									successor[k+plantSize] = temp;										
				      	}
				      	if (!transitionPossible) {																					
									break;
								}
				    	}							
				  	}								
				  	if (!transitionPossible) {
				    	enabled = false;
				    	continue;
				  	}		
				  }					  				  
					
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
  private BigInteger encode(int[] sState){
		bi = BigInteger.valueOf(0);
		for (i = 0; i<automatonSize; i++){
		  codes = codingList.get(i);
			a = sState[i];
			bi = bi.shiftLeft(bitlengthList[i]).add(BigInteger.valueOf(a));
		}
		return bi;
	}
	//Decoding
	private int[] decode(BigInteger bi){		
		for (i=automatonSize-1;i>-1;i--){
		  codes = codingList.get(i);
			a = bi.and(maskList[i]).intValue();
			bi = bi.shiftRight(bitlengthList[i]);
			systemState[i] = a;			
		}
		return systemState;
	}
	
	private void printStateList(){
	  for (BigInteger bi : stateList){
	    System.out.print(bi+" ");	    
	  }
		System.out.print("\n");
	}
	
	private void printState(int[] systemState){
	  for (Integer sp : systemState) {
		  System.out.print(sp+" ");
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
		
		boolean found = false;
		int indexSize = indexList.size();    
    
    AutomatonProxy ap ;
    AutomatonProxy as ;
		int[] errorState = new int[automatonSize];		
		
		tracelist.add(0,eventCodingList.get(errorEvent));	
		
		for (i = 0;i<automatonSize;i++) {			
			errorState[i] = systemState[i];	
		}				
		
		while(true){		
			indexList.remove(--indexSize);
			if(indexList.size()==0) break;				
			for (j=(indexSize==1)?0:(indexList.get(indexSize-2)+1);j<=indexList.get(indexSize-1);j++){			
			  systemState = decode(stateList.get(j));					
				for (int e=0;e<eventSize;e++) {
				  // Retrieve all enabled events									
				  enabled = true;
					transitionPossible = true;
				  for (i=0; i<plantSize;i++) {
						successor[i] = systemState[i];
				    if (plantEventList.get(i)[e] == 1){							
				      transitionPossible = false;							
							temp = plantTransitionMap.get(i)[systemState[i]][e];							
				      if (temp > -1){
								transitionPossible = true;
								successor[i] = temp;							
							}
						}
						if (!transitionPossible) {
							enabled = false;
							break;
						}						
					}
					if (!enabled) {						
						continue;
					}
										
				  for (k=0;k<automatonSize-plantSize;k++){
						transitionPossible = true;
						successor[k+plantSize] = systemState[k+plantSize];
				   	if (specEventList.get(k)[e] == 1) {
				     	transitionPossible = false;
							temp = specTransitionMap.get(k)[systemState[k+plantSize]][e];
				     	if (temp > -1){
								transitionPossible = true;
								successor[k+plantSize] = temp;										
				     	}
				     	if (!transitionPossible) {																					
								break;
							}
				    }							
				  }								
				 	if (!transitionPossible) {
				   	enabled = false;
				   	continue;
				 	}
				 	/*
				 	System.out.print("System State: ");		
				 	printState(systemState);				 
				 	System.out.print("generates Successor: ");		
				 	printState(successor);
				 	System.out.println("by Event: "+eventCodingList.get(e).getName()+"\n");	
				 		*/		 			 
					if(Arrays.equals(successor,errorState)){					
				 		for (i = 0;i<automatonSize;i++) {			
							errorState[i] = systemState[i];	
						}	
						found = true;  
						tracelist.add(0,eventCodingList.get(e));
						break;
					}					
				}				
				if(found) { 
					found = false;						
					break;	
				}		
			}
		}
    final SafetyTraceProxy trace =
      factory.createSafetyTraceProxy(tracename, des, tracelist);
    return trace;
  }
}
