//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControlLoopChecker
//###########################################################################
//# $Id: ControlLoopChecker.java,v 1.4 2006-08-20 22:51:38 yip1 Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.lang.Math;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.LoopTraceProxy;


/**
 * <P>An implementation of a control loop checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does a control loop check,
 * and finds whether the given model is control loop free.
 *
 * @see ModelChecker
 *
 * @author Peter Yunil Park
 */

public class ControlLoopChecker extends ModelChecker
{
    /** a sentinel that states if the model is control loop free. */
    private boolean mControlLoopFree;
    
    /** a list of automata in the model */
    private ArrayList<AutomatonProxy> mAutomataList;
    
    /** number of automata in the model */
    private int mNumAutomata;
    
    /** a list of events in the model */
    private ArrayList<EventProxy> mEventList;
    
    /** number of event in the model */
    private int mNumEvent;
    
    /** a list of transitions in the model */
    private ArrayList<ArrayList<TransitionProxy>> mTransitionList;
    
    /** a map of state tuple in synchronized model */
    private Map<ArrayList<Integer>, boolean[]> mGlobalStateMap;
    
    /** a list of unvisited state tuple. */
    private List<ArrayList<Integer>> mUnvisitedList;
    
    /** it holds the initial state tuple of the model. */
    private ArrayList<Integer> mInitialStateTuple;
    
    /** for tracing counterexample: it holds the root state of the control loop. */
    private ArrayList<Integer> mRootStateTuple;
    
    /** global event map: true is controllable, false is uncontrollable */
    private boolean[] mGlobalEventMap;
    
    /** a map for event availabilities */
    private static ArrayList<byte[]> mEventListMap;
    
    /** a map for available transitions */
    private static ArrayList<int[][]> mTransitionListMap;

    
    //#########################################################################
    //# Constructor
    /**
     * Creates a new control loop checker to check a particular model.
     * @param  model   The model to be checked by this control loop checker.
     * @param  factory Factory used for trace construction.
     */
    public ControlLoopChecker(final ProductDESProxy model,
			      final ProductDESProxyFactory factory)
    {
	super(model, factory);
	
	final ProductDESProxy des = getModel();
	
	mControlLoopFree = true;
	mAutomataList = new ArrayList<AutomatonProxy>();
	mEventList = new ArrayList<EventProxy>();

	mTransitionList = new ArrayList<ArrayList<TransitionProxy>>();
	
	// create Automaton list
	for(final AutomatonProxy aProxy: des.getAutomata()){
	    mAutomataList.add(aProxy);
	}
	
	// create Event list
	for(final EventProxy eProxy: des.getEvents()){
	    mEventList.add(eProxy);
	}

	// create Transition list
	for(final AutomatonProxy aProxy: mAutomataList){
	    final ArrayList<TransitionProxy> tmpTran = new ArrayList<TransitionProxy>();
	    for(final TransitionProxy tProxy: aProxy.getTransitions()){
		tmpTran.add(tProxy);
	    }
	    mTransitionList.add(tmpTran);
	}

	// get number of automata
	mNumAutomata = mAutomataList.size();

	// get number of events
	mNumEvent = mEventList.size();
	
        // create global event map
        mGlobalEventMap = new boolean[mNumEvent];
        for(int i = 0; i < mNumEvent; i++){
            if(mEventList.get(i).getKind() == EventKind.CONTROLLABLE){
                mGlobalEventMap[i] = true;
            }
        }
        
	// create maps
	mEventListMap = new ArrayList<byte[]>();
	mTransitionListMap = new ArrayList<int[][]>();
        
	// initialize maps
	byte[] currEvents;
	int[][] currTrans;

	Set<StateProxy> stateSet;
	ArrayList<StateProxy> stateList;

	int counter = 0;
	for(final AutomatonProxy aProxy: mAutomataList){
	    
	    stateSet = aProxy.getStates();
	    stateList = new ArrayList<StateProxy>(stateSet);
	    
	    currEvents = new byte[mNumEvent];
	    for(EventProxy eProxy: aProxy.getEvents()){
		if(eProxy.getKind() == EventKind.CONTROLLABLE){
		    currEvents[mEventList.indexOf(eProxy)] = 2;
		}
		else{
		    currEvents[mEventList.indexOf(eProxy)] = 1;
		}
	    }
	    mEventListMap.add(currEvents);
	    
	    int stateSize = stateList.size(); // number of states in current automaton
	    currTrans = new int[stateSize][mNumEvent];
	    for(int i = 0; i < stateSize; i++){
		for(int j = 0; j < mNumEvent; j++){
		    currTrans[i][j] = -1;
		}
	    }
	    for(TransitionProxy tProxy: mTransitionList.get(counter)){
		currTrans[stateList.indexOf(tProxy.getSource())][mEventList.indexOf(tProxy.getEvent())] 
		    = stateList.indexOf(tProxy.getTarget());
	    }
	    mTransitionListMap.add(currTrans);

	    counter++;
	}
	
	// initialise state tuple list
	mGlobalStateMap = new HashMap<ArrayList<Integer>, boolean[]>();
	mUnvisitedList = new LinkedList<ArrayList<Integer>>();
	
	mInitialStateTuple = new ArrayList<Integer>();
	
	// create initial state tuple
	for(final AutomatonProxy aProxy: mAutomataList){
	    int i = 0;
	    for(final StateProxy sProxy: aProxy.getStates()){
		if(sProxy.isInitial() == true){
		    mInitialStateTuple.add(i);
		    break;
		}
		i++;
	    }
	}
    }
    
    
    //#########################################################################
    //# Invocation
    /**
     * Runs this control loop checker.
     * This method starts the model checking process on the model given
     * as parameter to the constructor of this object. On termination,
     * the result of checking the property is known and can be queried
     * using the {@link #getResult()} and {@link #getCounterExample()}
     * methods.
     * @return <CODE>true</CODE> if the model is control loop free, or
     *         <CODE>false</CODE> if it is not.
     *         The same value can be queried using the {@link #getResult()}
     *         method.
     */
    public boolean run()
    {
        ///////////////////////////////////////////////////////////////////////////////
        ///////////////// GETTING RUNTIME OF THE CONTROL LOOP CHECKER /////////////////
        ///////////////////////////////////////////////////////////////////////////////
	long startTime = System.currentTimeMillis();

	boolean result = getResult();

        long endTime = System.currentTimeMillis();
        System.out.println("    Total Time (in milliSeconds): " + (endTime - startTime));
        ///////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////

	return result;
        
        // return getResult();
    }
    
    //#########################################################################
    //# Simple Access Methods
    /**
     * Gets the result of control loop checking.
     * @return <CODE>true</CODE> if the model was found to be control loop free,
     *         <CODE>false</CODE> otherwise.
     */
    public boolean getResult()
    {	
	// insert initial state tuple to global state and state list
	boolean[] initSp = new boolean[2];
	
	mGlobalStateMap.put(mInitialStateTuple, initSp);
	mUnvisitedList.add(mInitialStateTuple);
	
	while(!mUnvisitedList.isEmpty()){
	    final ArrayList<Integer> currTuple = mUnvisitedList.get(0);
	    final boolean sp[] = mGlobalStateMap.get(currTuple);
	    if(sp != null){
		if(sp[0] == false){
		    visit(currTuple);
		}
	    }
	    mUnvisitedList.remove(0);
	}
	
	return mControlLoopFree;
    }

    /**
     * This method visits each state tuple in the synchronized product.
     * If it tries to visit state tuple that has been visited before, it detects a loop.
     * @param state current state tuple property
     */
    public void visit(ArrayList<Integer> currTuple)
    {
	boolean[] currSp = mGlobalStateMap.get(currTuple);
	currSp[0] = true;

	for(int i = 0; i < mNumEvent; i++){
	    ArrayList<Integer> nextTuple = eventAvailable(currTuple, i, false);
	    
	    if(nextTuple != null){
                if(mGlobalEventMap[i]){
		    boolean[] nextSp;
		    
		    if(mGlobalStateMap.get(nextTuple) == null){
			nextSp = new boolean[2];
			mGlobalStateMap.put(nextTuple, nextSp);
			mUnvisitedList.add(nextTuple);
			visit(nextTuple);
			if(!mControlLoopFree){
			    return;
			}
		    }
		    else{
			nextSp = mGlobalStateMap.get(nextTuple);
			if(nextSp[0] == false){
			    visit(nextTuple);
			    if(!mControlLoopFree){
				return;
			    }
			}
		    }
		    
		    if(nextSp[1] == false){
			if(nextSp[0] == true){ // control loop detected here
			    if(mControlLoopFree){
				mControlLoopFree = false;
				mRootStateTuple = currTuple;
			    }
			    return;
			}
		    }
		}
		else{ // UNCONTROLLABLE
		    if(mGlobalStateMap.get(nextTuple) == null){
			boolean nextSp[] = new boolean[2];
			mGlobalStateMap.put(nextTuple, nextSp);
			mUnvisitedList.add(nextTuple);
		    }
		}
	    }
	}
	
	currSp[1] = true;
    }
    
    /**
     * Gets a counterexample if the model was found to be not control loop free.
     * representing a control loop error trace. A control loop error
     * trace is a nonempty sequence of events such that a trace has 
     * a strongly connected component of controllable events.
     * @return A trace object representing the counterexample.
     *         The returned trace is constructed for the input product DES
     *         of this control loop checker and shares its automata and
     *         event objects.
     * @throws IllegalStateException if this method is called before
     *         model checking has completed, i.e., before {@link #run()}
     *         has been called, or model checking has found that the
     *         property is satisfied and there is no counterexample.
     */
    public LoopTraceProxy getCounterExample()
    {
	final ProductDESProxyFactory factory = getFactory();
	final ProductDESProxy des = getModel();
	final String desname = des.getName();
	final String tracename = desname + ":has a control loop";
	final List<EventProxy> tracelist = new LinkedList<EventProxy>();
	
	/* FIND COUNTEREXAMPLE TRACE HERE */
	/* Counterexample = The shortest path from mInitialStateTuple to mRootStateTuple 
	                  + The shortest path from mRootStateTuple to mRootStateTuple */
	
	// find a shortest path from mRootStateTuple to mRootStateTuple: only for controllable events
	final Set<TransitionProperty> loopStates = new HashSet<TransitionProperty>();

        List<ArrayList<Integer>> list = new LinkedList<ArrayList<Integer>>();
        Set<ArrayList<Integer>> set = new HashSet<ArrayList<Integer>>();
        
	List<Integer> indexList = new ArrayList<Integer>();
        
        ArrayList<Integer> currTuple = new ArrayList<Integer>();
        
        int lastEvent = -1;
	
	list.add(mRootStateTuple);
	indexList.add(0);
	int indexSize = 0;
	
	loop:
	while(true){
	    indexSize = indexList.size();
	    for(int i = (indexSize==1)?0:(indexList.get(indexSize-2)+1); i <= indexList.get(indexSize-1); i++){
		currTuple = list.get(i);
		
                for(int j = 0; j < mNumEvent; j++){
                    if(mGlobalEventMap[j]){
			ArrayList<Integer> nextTuple = eventAvailable(currTuple, j, true);
			
			if(nextTuple != null){
			    if(nextTuple.equals(mRootStateTuple)){
                                lastEvent = j;
				break loop;
			    }
			    
			    if(set.add(nextTuple)){
				list.add(nextTuple);
			    }
			}
		    }
		}
	    }
	    
	    if(list.size() != (indexList.get(indexSize-1)+1)){
		indexList.add(list.size()-1);
	    }
	    else{
		break;
	    }
	}

	if(indexList.size() == 1){ // single cycle loop
	    loopStates.add(new TransitionProperty(mRootStateTuple, mRootStateTuple, lastEvent));
	}
	else{
	    loopStates.add(new TransitionProperty(currTuple, mRootStateTuple, lastEvent));
            ArrayList<Integer> target = currTuple;

	    for(int i = indexList.size()-2; i >= 0; i--){
		int start = (indexList.get(i)==0)?0:indexList.get(i-1)+1;
		int end = indexList.get(i);

		next:
		for(int j = start; j <= end; j++){
                    ArrayList<Integer> curr = list.get(j);
		    
                    for(int k = 0; k < mNumEvent; k++){
                        if(mGlobalEventMap[k]){
			    ArrayList<Integer> next = eventAvailable(curr, k, true);
                            
			    if(next != null){
				if(next.equals(target)){
				    loopStates.add(new TransitionProperty(curr, next, k));
				    target = curr;
				    break next;
				}
			    }
			}
		    }
		}
	    }
	}
	
	// find a shortest path from mInitialStateTuple to mRootStateTuple: for both controllable events and uncontrollable events
	// if mInitialStateTuple != mRootStateTuple
	if(!mInitialStateTuple.equals(mRootStateTuple)){
	    list = new LinkedList<ArrayList<Integer>>();
	    set = new HashSet<ArrayList<Integer>>();
	    indexList = new ArrayList<Integer>();
	    lastEvent = -1;
	    
	    list.add(mInitialStateTuple);
	    indexList.add(0);
	    indexSize = 0;

	    loop2:
	    while(true){
		indexSize = indexList.size();
		for(int i = (indexSize==1)?0:(indexList.get(indexSize-2)+1); i <= indexList.get(indexSize-1); i++){
		    currTuple = list.get(i);
		    
                    for(int j = 0; j < mNumEvent; j++){
			final ArrayList<Integer> nextTuple = eventAvailable(currTuple, j, false);
                        
			if(nextTuple != null){
			    if(isInLoop(nextTuple, loopStates)){
                                tracelist.add(0, mEventList.get(j));
				mRootStateTuple = nextTuple;
				break loop2;
			    }
			    
			    if(set.add(nextTuple)){
				list.add(nextTuple);
			    }
			}
		    }
		}
	    
		if(list.size() != (indexList.get(indexSize-1)+1)){
		    indexList.add(list.size()-1);
		}
		else{
		    break;
		}
	    }
	    
	    // Add to tracelist here
	    ArrayList<Integer> target = currTuple;
	    
	    for(int i = indexList.size()-2; i >= 0; i--){
		int start = (indexList.get(i)==0)?0:indexList.get(i-1)+1;
		int end = indexList.get(i);

		next2:
		for(int j = start; j <= end; j++){
		    ArrayList<Integer> curr = list.get(j);
		    
                    for(int k = 0; k < mNumEvent; k++){
                        ArrayList<Integer> next = eventAvailable(curr, k, false);
                        
			if(next != null){
			    if(next.equals(target)){
				tracelist.add(0, mEventList.get(k));
				target = curr;
				break next2;
			    }
			}
		    }
		}
	    }
	}

	int loopIndex = tracelist.size() + 1;
	
	while(loopStates.size() > 0){
	    for(final TransitionProperty tp: loopStates){
		if(mRootStateTuple.equals(tp.getSourceTuple())){
		    tracelist.add(mEventList.get(tp.getEvent()));
		    mRootStateTuple = tp.getTargetTuple();
		    loopStates.remove(tp);
		    break;
		}
	    }
	}
	
	final LoopTraceProxy trace = factory.createLoopTraceProxy(tracename, des, tracelist, loopIndex);
	return trace;
    }

    /**
     * It checks event is available from current state tuple.
     * @param currTuple current state tuple
     * @param event current event index
     * @param onlyControllable true if system needs to find only controllable events
     * @return return null if event is not available from current event, or return next state tuple
     */
    public ArrayList<Integer> eventAvailable(ArrayList<Integer> currTuple, int event, boolean onlyControllable)
    {
	ArrayList<Integer> nextTuple = new ArrayList<Integer>(mNumAutomata);
	
	for(int i = 0; i < mNumAutomata; i++){
	    int nextState = currTuple.get(i);
	    boolean eventExist = false;
	    boolean transitionExist = false;
	    
	    if(onlyControllable){ // 2: controllable events
		if(mEventListMap.get(i)[event] == 2){
		    eventExist = true;
		}
	    }
	    else{ // 1: uncontrollable events & 2: controllable events
		if(mEventListMap.get(i)[event] > 0){
		    eventExist = true;
		}
	    }
	    
	    if(eventExist){
		int temp = mTransitionListMap.get(i)[currTuple.get(i)][event];
		if(temp > -1){
		    nextState = temp;
		    transitionExist = true;
		}
	    }
	    
	    if(!eventExist){ // if event does not exist: nextTuple[i] = currTuple[i]
		nextTuple.add(currTuple.get(i));
	    }
	    else if(transitionExist){ // else if exists transition: nextTuple[i] = getNextTransition(currTuple[i])
		nextTuple.add(nextState);
	    }
	    else{ // else: event is not available
		return null;
	    }
	}
	
	return nextTuple;
    }
    
    /**
     * It checks current state tuple is in the control loop
     * @param currTuple current state tuple
     * @param loopStates transitions in the control loop
     * @return return true if state tuple is in the loop, false otherwise
     */
    public boolean isInLoop(ArrayList<Integer> currTuple, Set<TransitionProperty> loopStates)
	{
	for(final TransitionProperty tp: loopStates){
	    if(currTuple.equals(tp.getSourceTuple())){
		return true;
	    }
	}

	return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////// For encoding and decoding ///////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /**
     * It returns a number of bits used for encoding current automaton
     * @param automaton current automaton
     */
    public int getBitLength(AutomatonProxy automaton)
    {
	int states = automaton.getStates().size();
	int bits = 0;
	states -= 1;

	while(states > 0){
	    states >>= 1;
	    bits++;
	}

	return (bits == 0)?1:bits;
    }
}
