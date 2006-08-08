//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControlLoopChecker
//###########################################################################
//# $Id: ControlLoopChecker.java,v 1.1 2006-08-08 22:32:37 yip1 Exp $
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

    /** a list of automata in the model. */
    private ArrayList<AutomatonProxy> mAutomataList;

    /** a list of events in the model. */
    private ArrayList<EventProxy> mEventList;
    
    /** */
    private Map<ArrayList<StateProxy>, StateProperty> mGlobalStateMap;

    /** a list of unvisited state tuple. */
    private List<ArrayList<StateProxy>> mUnvisitedList;

    /** it holds the initial state tuple of the model. */
    private ArrayList<StateProxy> mInitialStateTuple;

    /** for tracing counterexample: it holds the root state of the control loop. */
    private ArrayList<StateProxy> mRootStateTuple;


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
     * Presently, this is a dummy implementation that nothing but always
     * returns <CODE>true</CODE>.
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
	// long startTime = System.currentTimeMillis();

	boolean result = getResult();

        // long endTime = System.currentTimeMillis();
        // System.out.println("    Total Time (in milliSeconds): " + (endTime - startTime));
        ///////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////

	return result;
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
	ProductDESProxy des = getModel();

	mControlLoopFree = true;
	mAutomataList = new ArrayList<AutomatonProxy>();
	mEventList = new ArrayList<EventProxy>();

	mGlobalStateMap = new HashMap<ArrayList<StateProxy>, StateProperty>();
	mUnvisitedList = new LinkedList<ArrayList<StateProxy>>();

	mInitialStateTuple = new ArrayList<StateProxy>();
	
	// create Automaton list
	for(final AutomatonProxy aProxy: des.getAutomata()){
	    mAutomataList.add(aProxy);
	}

	// create Event list
	for(final EventProxy eProxy: des.getEvents()){
	    mEventList.add(eProxy);
	}

	// create initial state tuple
	for(final AutomatonProxy aProxy: mAutomataList){
	    for(final StateProxy sProxy: aProxy.getStates()){
		if(sProxy.isInitial() == true){
		    mInitialStateTuple.add(sProxy);
		    break;
		}
	    }
	}
	
	// insert initial state tuple to global state and state list
	final StateProperty initSp = new StateProperty(mInitialStateTuple);
	mGlobalStateMap.put(mInitialStateTuple, initSp);
	mUnvisitedList.add(mInitialStateTuple);

	while(!mUnvisitedList.isEmpty()){
	    final ArrayList<StateProxy> currTuple = mUnvisitedList.get(0);

	    final StateProperty sp = mGlobalStateMap.get(currTuple);
	    if(sp != null){
		if(sp.getVisited() == false){
		    visit(currTuple);
		}
	    }
	    
	    mUnvisitedList.remove(0);
	}

	return mControlLoopFree;
    }

    /**
     * Description:
     * @param state current state tuple property
     */
    public void visit(ArrayList<StateProxy> currTuple)
    {
	final StateProperty currSp = mGlobalStateMap.get(currTuple);
	currSp.setInComponent(false);
	currSp.setVisited(true);

	for(EventProxy eProxy: mEventList){
	    ArrayList<StateProxy> nextTuple = new ArrayList<StateProxy>();
	    boolean eventAvailable = true;
	    
	    for(int i = 0; i < mAutomataList.size(); i++){
		StateProxy nextState = currTuple.get(i);
		boolean eventExist = false;
		boolean transitionExist = false;
		eventAvailable = true;

		/////////////////////////////////////////////////////////////////////////
		// need to hashmap or lookup events in automaton to make search faster //
		/////////////////////////////////////////////////////////////////////////
		for(EventProxy e: mAutomataList.get(i).getEvents()){ // Find event exists
		    if(e == eProxy){
			eventExist = true;
			break;
		    }
		}

		if(eventExist){
		    for(TransitionProxy t: mAutomataList.get(i).getTransitions()){ // Find transition exists
			if(t.getSource() == currTuple.get(i) && t.getEvent() == eProxy){
			    nextState = t.getTarget();
			    transitionExist = true;
			    break;
			}
		    }
		}
		//////////////////////////////////////////////////////////////////////////

		if(!eventExist){ // if event does not exist: nextTuple[i] = currTuple[i]
		    nextTuple.add(currTuple.get(i));
		}
		else if(transitionExist){ // else if exists transition: nextTuple[i] = getNextTransition(currTuple[i])
		    nextTuple.add(nextState);
		}
		else{ // else: event is not available
		    eventAvailable = false;
		    break;
		}
	    }

	    if(eventAvailable){
		if(eProxy.getKind() == EventKind.CONTROLLABLE){
		    final StateProperty nextSp;
		    
		    if(mGlobalStateMap.get(nextTuple) == null){
			nextSp = new StateProperty(nextTuple);
			mGlobalStateMap.put(nextTuple, nextSp);
			mUnvisitedList.add(nextTuple);
			visit(nextTuple);
			if(!mControlLoopFree){
			    return;
			}
		    }
		    else{
			nextSp = mGlobalStateMap.get(nextTuple);
			if(!nextSp.getVisited()){
			    visit(nextTuple);
			    if(!mControlLoopFree){
				return;
			    }
			}
		    }
		    
		    if(!nextSp.getInComponent()){
			if(nextSp.getVisited()){ // control loop detected here.
			    if(mControlLoopFree){
				mControlLoopFree = false;
				mRootStateTuple = currSp.getNode();
				// mRootStateTuple = nextSp.getNode();
			    }
			    return;
			}
		    }
		}
		else{ // UNCONTROLLABLE
		    if(mGlobalStateMap.get(nextTuple) == null){
			final StateProperty nextSp = new StateProperty(nextTuple);
			mGlobalStateMap.put(nextTuple, nextSp);
			mUnvisitedList.add(nextTuple);
		    }
		}
	    }
	}

	currSp.setInComponent(true);
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
	final Collection<EventProxy> events = des.getEvents();
	final List<EventProxy> tracelist = new LinkedList<EventProxy>();

	// find controllable events
	final List<EventProxy> conEvents = new ArrayList<EventProxy>();
	for(EventProxy e: events){
	    if(e.getKind() == EventKind.CONTROLLABLE){
		conEvents.add(e);
	    }
	}

	/* FIND COUNTEREXAMPLE TRACE HERE */
	/* Counterexample = The shortest path from mInitialStateTuple to mRootStateTuple 
	                  + The shortest path from mRootStateTuple to mRootStateTuple */

	// find a shortest path from mRootStateTuple to mRootStateTuple: only for controllable events
	final Set<TransitionProperty> loopStates = new HashSet<TransitionProperty>();

	List<ArrayList<StateProxy>> list = new LinkedList<ArrayList<StateProxy>>();
	Set<ArrayList<StateProxy>> set = new HashSet<ArrayList<StateProxy>>();
	List<Integer> indexList = new ArrayList<Integer>();
	ArrayList<StateProxy> currTuple = new ArrayList<StateProxy>();
	EventProxy lastEvent = null;
	
	list.add(mRootStateTuple);
	indexList.add(0);
	int indexSize = 0;
	
	loop:
	while(true){
	    indexSize = indexList.size();
	    for(int i = (indexSize==1)?0:(indexList.get(indexSize-2)+1); i <= indexList.get(indexSize-1); i++){
		currTuple = list.get(i);
		
		for(EventProxy eProxy: conEvents){
		    ArrayList<StateProxy> nextTuple = new ArrayList<StateProxy>();
		    boolean eventAvailable = true;
		    
		    for(int j = 0; j < mAutomataList.size(); j++){
			StateProxy nextState = currTuple.get(j);
			boolean eventExist = false;
			boolean transitionExist = false;
			eventAvailable = true;
			
			for(EventProxy e: mAutomataList.get(j).getEvents()){ // Find event exists
			    if(e == eProxy){
				eventExist = true;
				break;
			    }
			}
			
			if(eventExist){
			    for(TransitionProxy t: mAutomataList.get(j).getTransitions()){ // Find transition exists
				if(t.getSource() == currTuple.get(j) && t.getEvent() == eProxy){
				    nextState = t.getTarget();
				    transitionExist = true;
				    break;
				}
			    }
			}
			
			if(!eventExist){ // if event does not exist: nextTuple[i] = currTuple[i]
			    nextTuple.add(currTuple.get(j));
			}
			else if(transitionExist){ // else if exists transition: nextTuple[i] = getNextTransition(currTuple[i])
			    nextTuple.add(nextState);
			}
			else{ // else: event is not available
			    eventAvailable = false;
			    break;
			}
		    }
		    
		    if(eventAvailable){
			if(nextTuple.equals(mRootStateTuple)){
			    // lastTuple = currTuple;
			    lastEvent = eProxy;
			    break loop;
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

	if(indexList.size() == 1){ // single cycle loop
	    loopStates.add(new TransitionProperty(mRootStateTuple, mRootStateTuple, lastEvent));
	}
	else{
	    loopStates.add(new TransitionProperty(currTuple, mRootStateTuple, lastEvent));
	    ArrayList<StateProxy> target = currTuple;

	    for(int i = indexList.size()-2; i >= 0; i--){
		int start = (indexList.get(i)==0)?0:indexList.get(i-1)+1;
		int end = indexList.get(i);

		next:
		for(int j = start; j <= end; j++){
		    ArrayList<StateProxy> curr = list.get(j);
		    
		    for(EventProxy eProxy: conEvents){
			ArrayList<StateProxy> next = new ArrayList<StateProxy>();
			boolean eventAvailable = true;
		    
			for(int k = 0; k < mAutomataList.size(); k++){
			    StateProxy nextState = curr.get(k);
			    boolean eventExist = false;
			    boolean transitionExist = false;
			    eventAvailable = true;
			    
			    for(EventProxy e: mAutomataList.get(k).getEvents()){ // Find event exists
				if(e == eProxy){
				    eventExist = true;
				    break;
				}
			    }
			    
			    if(eventExist){
				for(TransitionProxy t: mAutomataList.get(k).getTransitions()){ // Find transition exists
				    if(t.getSource() == curr.get(k) && t.getEvent() == eProxy){
					nextState = t.getTarget();
					transitionExist = true;
					break;
				    }
				}
			    }
			    
			    if(!eventExist){ // if event does not exist: nextTuple[i] = currTuple[i]
				next.add(curr.get(k));
			    }
			    else if(transitionExist){ // else if exists transition: nextTuple[i] = getNextTransition(currTuple[i])
				next.add(nextState);
			    }
			    else{ // else: event is not available
				eventAvailable = false;
				break;
			    }
			}
			
			if(eventAvailable){
			    if(next.equals(target)){
				loopStates.add(new TransitionProperty(curr, next, eProxy));
				target = curr;
				break next;
			    }
			}
		    }
		}
	    }
	}
	
	// find a shortest path from mInitialStateTuple to mRootStateTuple: for both controllable events and uncontrollable events
	// if mInitialStateTuple != mRootStateTuple
	if(!mInitialStateTuple.equals(mRootStateTuple)){
	    list = new LinkedList<ArrayList<StateProxy>>();
	    set = new HashSet<ArrayList<StateProxy>>();
	    indexList = new ArrayList<Integer>();
	    lastEvent = null;
	    
	    list.add(mInitialStateTuple);
	    indexList.add(0);
	    indexSize = 0;

	    loop2:
	    while(true){
		indexSize = indexList.size();
		for(int i = (indexSize==1)?0:(indexList.get(indexSize-2)+1); i <= indexList.get(indexSize-1); i++){
		    currTuple = list.get(i);
		    
		    for(EventProxy eProxy: mEventList){
			final ArrayList<StateProxy> nextTuple = new ArrayList<StateProxy>();
			boolean eventAvailable = true;
			
			for(int j = 0; j < mAutomataList.size(); j++){
			    StateProxy nextState = currTuple.get(j);
			    boolean eventExist = false;
			    boolean transitionExist = false;
			    eventAvailable = true;
			    
			    for(EventProxy e: mAutomataList.get(j).getEvents()){ // Find event exists
				if(e == eProxy){
				    eventExist = true;
				    break;
				}
			    }
			    
			    if(eventExist){
				for(TransitionProxy t: mAutomataList.get(j).getTransitions()){ // Find transition exists
				    if(t.getSource() == currTuple.get(j) && t.getEvent() == eProxy){
					nextState = t.getTarget();
					transitionExist = true;
					break;
				    }
				}
			    }
			    
			    if(!eventExist){ // if event does not exist: nextTuple[i] = currTuple[i]
				nextTuple.add(currTuple.get(j));
			    }
			    else if(transitionExist){ // else if exists transition: nextTuple[i] = getNextTransition(currTuple[i])
				nextTuple.add(nextState);
			    }
			    else{ // else: event is not available
				eventAvailable = false;
				break;
			    }
			}
			
			if(eventAvailable){
			    if(inLoop(nextTuple, loopStates)){
				// lastTuple = currTuple;
				tracelist.add(0, eProxy);
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
	    ArrayList<StateProxy> target = currTuple;
	    
	    for(int i = indexList.size()-2; i >= 0; i--){
		int start = (indexList.get(i)==0)?0:indexList.get(i-1)+1;
		int end = indexList.get(i);

		next2:
		for(int j = start; j <= end; j++){
		    ArrayList<StateProxy> curr = list.get(j);
		    
		    for(EventProxy eProxy: mEventList){
			ArrayList<StateProxy> next = new ArrayList<StateProxy>();
			boolean eventAvailable = true;
		    
			for(int k = 0; k < mAutomataList.size(); k++){
			    StateProxy nextState = curr.get(k);
			    boolean eventExist = false;
			    boolean transitionExist = false;
			    eventAvailable = true;
			    
			    for(EventProxy e: mAutomataList.get(k).getEvents()){ // Find event exists
				if(e == eProxy){
				    eventExist = true;
				    break;
				}
			    }
			    
			    if(eventExist){
				for(TransitionProxy t: mAutomataList.get(k).getTransitions()){ // Find transition exists
				    if(t.getSource() == curr.get(k) && t.getEvent() == eProxy){
					nextState = t.getTarget();
					transitionExist = true;
					break;
				    }
				}
			    }
			    
			    if(!eventExist){ // if event does not exist: nextTuple[i] = currTuple[i]
				next.add(curr.get(k));
			    }
			    else if(transitionExist){ // else if exists transition: nextTuple[i] = getNextTransition(currTuple[i])
				next.add(nextState);
			    }
			    else{ // else: event is not available
				eventAvailable = false;
				break;
			    }
			}
			
			if(eventAvailable){
			    if(next.equals(target)){
				tracelist.add(0, eProxy);
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
	    for(TransitionProperty tp: loopStates){
		if(mRootStateTuple.equals(tp.getSourceTuple())){
		    tracelist.add(tp.getEvent());
		    mRootStateTuple = tp.getTargetTuple();
		    loopStates.remove(tp);
		    break;
		}
	    }
	}
	
	final LoopTraceProxy trace = factory.createLoopTraceProxy(tracename, des, tracelist, loopIndex);
	return trace;
    }

    public boolean inLoop(ArrayList<StateProxy> currTuple, Set<TransitionProperty> loopStates)
    {
	for(TransitionProperty tp: loopStates){
	    if(currTuple.equals(tp.getSourceTuple())){
		return true;
	    }
	}

	return false;
    }
}
