//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.java,v 1.6 2006-08-25 03:16:54 js173 Exp $
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
 * <P>An implementation of a controllability checker.</P>
 *
 * <P>The {@link #run()} method of this model checker will check
 * the controllability of models.</P>
 *
 * @see ModelChecker
 *
 * @author Jinjian Shi
 */

public class ControllabilityChecker extends ModelChecker
{
  //
  private ProductDESProxy model;                 
  
  //
	private Set<AutomatonProxy> automatonSet;	
	
	//Transition map
	private ArrayList<int[][]> plantTransitionMap;
	private ArrayList<int[][]> specTransitionMap;
	
	//Level states storage
	private ArrayList<Integer> indexList;
	private BlockedArrayList<StateTuple> stateList;
	
	//For encoding/decoding
	private ArrayList<StateProxy> codes;
  private ArrayList<ArrayList<StateProxy>> codingList;
  private byte[] aneventCodingList;	
	private ArrayList<EventProxy> eventCodingList;
	private ArrayList<byte[]> plantEventList;
	private ArrayList<byte[]> specEventList;	
	private int[] bitlengthList;	
	private int[] maskList;
	private int[] codePosition;
	private StateTuple stateTuple;	

	//Size
	private int automatonSize;
	private int eventSize;
	private int plantSize;	
	private int stSize;		
	
	//For computing successor and counterexample
	private int[] systemState;
	private int[] successor;
	private Integer errorEvent;	
	
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
    stateList = new BlockedArrayList<StateTuple>(StateTuple.class);        
		
    codes = new ArrayList<StateProxy>();
    codingList = new ArrayList<ArrayList<StateProxy>>();	
		eventCodingList = new ArrayList<EventProxy>(model.getEvents());		
		plantEventList = new ArrayList<byte[]>();
		specEventList = new ArrayList<byte[]>();		
		
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
   */
  public boolean getResult() 
  {		
		Set<StateProxy> stateSet;
		ArrayList<ArrayList<StateProxy>> specCodingList = new ArrayList<ArrayList<StateProxy>>();
		int i,j,k = 0;		
		int ck = 0;	
		int bl = 0;
		int mask = 0;
		int codeLength = 0;
		int cp = 0;
		
		eventSize = eventCodingList.size();
		automatonSize = automatonSet.size();
		bitlengthList = new int[automatonSize];
		maskList = new int[automatonSize];	
		codePosition = new int[automatonSize];	
		
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
			//Encoding transitions to binary values
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
			mask = (1 << bl)-1;			
    	
    	//Find initial state
    	StateProxy initialState = null;
    	for (StateProxy sp : stateSet) {
    		if (sp.isInitial()) {initialState = sp; break;}
    	} 
    	//Store all the information by automaton type   	
    	if (ap.getKind() == ComponentKind.PLANT) {    		
    		codingList.add(codes);    		
    		systemState[ck] = codes.indexOf(initialState);
				plantEventList.add(aneventCodingList);				
				plantTransitionMap.add(atransition);
				bitlengthList[ck] = bl;
				maskList[ck] = mask;
				ck++;
    	}
    	else if (ap.getKind() == ComponentKind.SPEC){    	  
    	  specCodingList.add(codes);     	    	  
    	  systemState[k+plantSize] = codes.indexOf(initialState);
				specEventList.add(aneventCodingList);				
				specTransitionMap.add(atransition);
				bitlengthList[k+plantSize] = bl;
				maskList[k+plantSize] = mask;
				k++;
      }      
    }	
    //Combine the plant coding list and spec coding list together	
    codingList.addAll(specCodingList); 
    
    //Set the codePosition list
    for (i=0;i<automatonSize;i++) {
			codeLength += bitlengthList[i];
			if (codeLength <= 32){
			  codePosition[i] = cp;
			} else {
				codeLength = bitlengthList[i];
				cp++;
				codePosition[i] = cp;
			} 
		}
		stSize = cp+1;		
		System.out.println("\nStart ...............");		
		startTime = System.currentTimeMillis();
		return this.isControllable(systemState);
  }
  
  /** 
   * Check the controllability of the model with a parameter of  
   * initial synchronous product. 
   * @parameter sState The initial synchronous product of the model
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.   
   */  
  private boolean isControllable(int[] sState){
	  
		Set<StateTuple> systemSet = new HashSet<StateTuple>(); // Future changable			
   
    boolean enabled = true;
   
    //Add the initial synchronous product in systemSet and stateList
		successor = new int[automatonSize];
		stateTuple = new StateTuple(stSize);		
		encode(sState,stateTuple);		
    systemSet.add(stateTuple);
    stateList.add(stateTuple);
    indexList.add(stateList.size()-1);    
    
    int indexSize = 0;
		int eventSize = eventCodingList.size();   
		int i,j,k,temp;
    
		while(true){			
			//For each current state in the current level, check its controllability			
			indexSize = indexList.size();
			for (j=(indexSize==1)?0:(indexList.get(indexSize-2)+1);j<=indexList.get(indexSize-1);j++){				
			  decode(stateList.get(j),systemState);						
				for (int e=0;e<eventSize;e++) {
				  // Retrieve all enabled events									
				  enabled = true;					
				  for (i=0; i<plantSize;i++) {						
				    if (plantEventList.get(i)[e] == 1){				     				
							temp = plantTransitionMap.get(i)[systemState[i]][e];	
							if (temp == -1) {
								enabled = false;
								break;
							}						
				      else if (temp > -1){								
								successor[i] = temp;	
								continue;						
							}
						}
						successor[i] = systemState[i];										
					}
					if (!enabled) {						
						continue;
					}					
				
					// Check controllability of current state				  
				  if (eventCodingList.get(e).getKind() == EventKind.UNCONTROLLABLE) {						
				    for (i=0; i<automatonSize-plantSize;i++) {													
				    	if (specEventList.get(i)[e] == 1){				    	 					
								temp = specTransitionMap.get(i)[systemState[i+plantSize]][e];
								if (temp == -1) {			    	    
									System.out.println(systemSet.size());
									errorEvent = e;									
									endTime = System.currentTimeMillis();
				    	    return false;
				    	  }
								if (temp > -1){									
									successor[i+plantSize] = temp;	
									continue;								
								}				    	  
				    	}
				    	successor[i+plantSize] = systemState[i+plantSize];								
				  	}
				  } else {					
				  	for (k=0;k<automatonSize-plantSize;k++){						
				    	if (specEventList.get(k)[e] == 1) {				      	
								temp = specTransitionMap.get(k)[systemState[k+plantSize]][e];
								if (temp == -1) {	
								  enabled = false;																				
									break;
								}
				      	if (temp > -1){									
									successor[k+plantSize] = temp;	
									continue;									
				      	}				      	
				    	}	
				    	successor[k+plantSize] = systemState[k+plantSize];						
				  	}								
				  	if (!enabled) {				    	 
				    	continue;
				  	}		
				  }					  				  
					
				 	// Encode the new system state and put it into stateList
					stateTuple = new StateTuple(stSize);
				 	encode(successor,stateTuple);				
							
				 	if (systemSet.add(stateTuple)) {							
						stateList.add(stateTuple);						
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
  
  //#########################################################################
  //# Encoding
  /** 
   * Encode the synchronous product into StateTuple
   * @parameter sState  The state to be encoded   
   * @parameter sTuple The encoded StateTuple     
   */ 
  private void encode(int[] sState,StateTuple sTuple){
  	int i;		
		int k = 0;
		int result = 0;
		for (i = 0; i<automatonSize; i++) {
			if (codePosition[i] == k) {
				result <<= bitlengthList[i];				
				result |= sState[i];
			}
			else {
				sTuple.set(k,result);				
				result = sState[i];				
				k++;
			}		 
			if (i == automatonSize-1) {
				sTuple.set(k,result);
			}
		}	
	}
	
  //#########################################################################
  //# Decoding
	/** 
   * Decode the StateTuple  
   * @parameter sTuple The StateTuple to be decoded
   * @parameter state  The decoded state   
   */ 
	private void decode(StateTuple sTuple,int[] state){	
		int i,result;	
		int k = codePosition[automatonSize-1];		
		int temp = sTuple.get(k);		
		for (i=automatonSize-1;i>-1;i--){	
			if (codePosition[i] == k) {
				result = temp;
				result &= maskList[i];
				state[i] = result;				
				temp >>= bitlengthList[i];
			}	
			else if (codePosition[i] < k) {
				k--;
			  temp = sTuple.get(k);
			  result = temp;
				result &= maskList[i];
				state[i] = result;
				temp >>= bitlengthList[i];
			} 						
		}		
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
		final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String tracename = desname + ":uncontrollable";   
    final List<EventProxy> tracelist = new LinkedList<EventProxy>();
		
		boolean enabled;
			
		boolean found = false;
		int i,j,k,temp;	
		int indexSize = indexList.size();     
    
		int[] errorState = new int[automatonSize];		
		
		tracelist.add(0,eventCodingList.get(errorEvent));			
		
		while(true){
			for (i = 0;i<automatonSize;i++) {			
				errorState[i] = systemState[i];	
			}			
			indexList.remove(--indexSize);
			if(indexList.size()==0) break;
			//Backward search the previous level states, compute their
			//successors and compare them with the error state				
			for (j=(indexSize==1)?0:(indexList.get(indexSize-2)+1);j<=indexList.get(indexSize-1);j++){				
			  decode(stateList.get(j),systemState);						
				for (int e=0;e<eventSize;e++) {				 								
				  enabled = true;					
				  for (i=0; i<plantSize;i++) {						
				    if (plantEventList.get(i)[e] == 1){				     				
							temp = plantTransitionMap.get(i)[systemState[i]][e];	
							if (temp == -1) {
								enabled = false;
								break;
							}						
				      else if (temp > -1){								
								successor[i] = temp;	
								continue;						
							}
						}
						successor[i] = systemState[i];										
					}
					if (!enabled) {						
						continue;
					}
									
				  for (k=0;k<automatonSize-plantSize;k++){						
				   	if (specEventList.get(k)[e] == 1) {				      	
							temp = specTransitionMap.get(k)[systemState[k+plantSize]][e];
							if (temp == -1) {	
							  enabled = false;																				
								break;
							}
				     	if (temp > -1){									
								successor[k+plantSize] = temp;	
								continue;									
				     	}				      	
				   	}	
				   	successor[k+plantSize] = systemState[k+plantSize];						
				 	}								
				 	if (!enabled) {				    	 
				   	continue;
				 	}				  
 			 
					if(Arrays.equals(successor,errorState)){					
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
