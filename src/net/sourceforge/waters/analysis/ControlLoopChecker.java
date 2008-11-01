//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControlLoopChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.LoopTraceProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * <P>An implementation of a control loop checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does a control loop check,
 * and finds whether the given model is control loop free.</P>
 *
 * @see ModelChecker
 *
 * @author Peter Yunil Park
 */

public class ControlLoopChecker extends ModelChecker
{
    /** Constant: number of bits for integer buffer */
    private static final int SIZE_INT = 32;
    
    /** Constant: size of global array list buffer for growing */
    private static final int SIZE_BUFFER = 1024;
    
    /** a sentinel that states if the model is control loop free. */
    private boolean mControlLoopFree;
    
    /** a list of automata in the model */
    private ArrayList<AutomatonProxy> mAutomataList;
    
    /** number of automata in the model */
    private int mNumAutomata;
    
    /** a list of events in the model */
    private ArrayList<EventProxy> mEventList;
    
    /** number of all events in the model */
    private int mNumEvent;

    /** number of controllable events in the model */
    private int mNumConEvent;
    
    /** a list of transitions in the model */
    private ArrayList<ArrayList<TransitionProxy>> mTransitionList;
    
    /** a map of state tuple in synchronized model */
    private StateHashSet mGlobalStateSet;
    
    /** a list of unvisited state tuple. */
    private List<EncodedStateTuple> mUnvisitedList;
    
    /** it holds the initial state tuple of the model. */
    private int mInitialStateTuple[];
    
    /** it holds the initial encoded state tuple of the model. */
    private EncodedStateTuple mEncodedInitialStateTuple;
    
    /** for tracing counterexample: it holds the root encoded state of the control loop. */
    private EncodedStateTuple mEncodedRootStateTuple;
    
    /** global event map: true is controllable, false is uncontrollable */
    private boolean mGlobalEventMap[];
    
    /** */
    private static int[][][] mMap;

    /** a global integer array to store current decoded integer state tuple */
    private int mCurrTuple[];
    
    /** a global encoded state tuple for storing current state tuple */
    private EncodedStateTuple mEncodedCurrTuple;
    
    /** a global state tuple for storing next state tuple */
    private int mNextTuple[];
    
     //#########################################################################
    //# Variables used for encoding/decoding
    /** a list contains number of bits needed for each automaton */
    private int mNumBits[];
    
    /** a list contains masks needed for each automaton */
    private int mNumBitsMasks[];
    
    /** a number of integers used to encode synchronized state */
    private int mNumInts;
    
    /** an index of first automaton in each integer buffer */
    private int mIndexAutomata[];


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
        for (final AutomatonProxy aProxy : des.getAutomata()) {
            final ComponentKind kind = aProxy.getKind();
            switch (kind) {
            case PLANT:
            case SPEC:
                mAutomataList.add(aProxy);
                break;
            default:
                break;
            }
        }
        
        // create Event list: order controllable first, uncontrollable last
	// list only controllable and uncontrollable events
        for(final EventProxy eProxy: des.getEvents()){
	    if(eProxy.getKind() == EventKind.CONTROLLABLE){
		mEventList.add(0, eProxy); // controllable event: put it in the beginning of the list
		mNumConEvent++;
	    }
	    else if(eProxy.getKind() == EventKind.UNCONTROLLABLE){
		mEventList.add(eProxy); // uncontrollable event: put it in the end of the list
	    }
        }
        
        // get number of automata
        mNumAutomata = mAutomataList.size();
        
        // get number of events
        mNumEvent = mEventList.size();

        // get number of controllable events
        
        // get encoding information
        mNumBits = new int[mNumAutomata];
        mNumBitsMasks = new int[mNumAutomata];
        
        mNumInts = 1;
        int totalBits = SIZE_INT;
        int counter = 0;
        for(final AutomatonProxy aProxy: mAutomataList){
            int bits = getBitLength(aProxy);
            mNumBits[counter] = bits;
            mNumBitsMasks[counter] = (1 << bits) - 1;
            if(totalBits >= bits){ // if current buffer can store this automaton
                totalBits -= bits;
            }
            else{
                mNumInts++;
                totalBits = SIZE_INT;
            }
            counter++;
        }
        
        // get index
        counter = 0;
        totalBits = SIZE_INT;
        mIndexAutomata = new int[mNumInts + 1];
        mIndexAutomata[0] = counter++;
        for(int i = 0; i < mNumAutomata; i++){
            if(totalBits >= mNumBits[i]){
                totalBits -= mNumBits[i];
            }
            else{
                mIndexAutomata[counter++] = i;
                totalBits = SIZE_INT;
            }
        }
        mIndexAutomata[mNumInts] = mNumAutomata;
        
        // create Transition list
        for(final AutomatonProxy aProxy: mAutomataList){
            final ArrayList<TransitionProxy> tmpTran = new ArrayList<TransitionProxy>();
            for(final TransitionProxy tProxy: aProxy.getTransitions()){
                tmpTran.add(tProxy);
            }
            mTransitionList.add(tmpTran);
        }
        
        // create global event map
        mGlobalEventMap = new boolean[mNumEvent];
        for(int i = 0; i < mNumEvent; i++){
            if(mEventList.get(i).getKind() == EventKind.CONTROLLABLE){
                mGlobalEventMap[i] = true;
            }
        }
        
        // create maps
	mMap = new int[mNumEvent][mNumAutomata][];
	for(int i = 0; i < mNumEvent; i++){
	    for(int j = 0; j < mNumAutomata; j++){
		mMap[i][j] = null;
	    }
	}
        
        Set<StateProxy> stateSet;
        ArrayList<StateProxy> stateList;
        
	// create map
	int countAutomata = 0;
	int countEvent = 0;
	for(final AutomatonProxy aProxy: mAutomataList){
	    int numState = aProxy.getStates().size();
	    
	    stateSet = aProxy.getStates();
	    stateList = new ArrayList<StateProxy>(stateSet);
	    
	    for(final EventProxy eProxy: mEventList){
                // find if the event is in current automaton
                Set<EventProxy> eventSet = aProxy.getEvents();
                boolean eventAvailable = false;
                for(final EventProxy e: eventSet){
                    if(e.equals(eProxy)){
                        eventAvailable = true;
                        break;
                    }
                }
                
                if(eventAvailable){
		    mMap[countEvent][countAutomata] = new int[numState];

		    for(int i = 0; i < numState; i++){
			mMap[countEvent][countAutomata][i] = -1;
		    }
                }
		
		countEvent++;
	    }
	    
            for(TransitionProxy tProxy: mTransitionList.get(countAutomata)){
                mMap[mEventList.indexOf(tProxy.getEvent())][countAutomata][stateList.indexOf(tProxy.getSource())]
                    = stateList.indexOf(tProxy.getTarget());
            }
	    
	    countEvent = 0;
	    countAutomata++;
	}
	
        // create initial state tuple
        mInitialStateTuple = new int[mNumAutomata];
        int i = 0;
        for(final AutomatonProxy aProxy: mAutomataList){
            int j = 0;
            for(final StateProxy sProxy: aProxy.getStates()){
                if(sProxy.isInitial() == true){
                    mInitialStateTuple[i] = j;
                    break;
                }
                j++;
            }
            i++;
        }
        
        // set a buffer for storing current state tuple
        mCurrTuple = new int[mNumAutomata];
        
        // set a buffer for storing next state tuple
        mNextTuple = new int[mNumAutomata];
        
        // set the initial state tuple
        mEncodedInitialStateTuple = new EncodedStateTuple(encoding(mInitialStateTuple));
        
        // initialise state tuple list
        mGlobalStateSet = new StateHashSet(SIZE_BUFFER);
        mUnvisitedList = new ArrayList<EncodedStateTuple>(SIZE_BUFFER);
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
        // TEMP: getting the runtime of the control loop checker
        ///////////////////////////////////////////////////////////////////////////////
        long startTime = System.currentTimeMillis();
        
        boolean result = getResult();
        
        long endTime = System.currentTimeMillis();
        System.out.println("\tTotal Time (in milliSeconds): " + (endTime - startTime));
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
        int counter = 0;
        
        // insert initial state tuple to global state and state list
        mGlobalStateSet.getOrAdd(mEncodedInitialStateTuple);
        mUnvisitedList.add(mEncodedInitialStateTuple);
        
        while(true){
            if(counter < mUnvisitedList.size()){
                mEncodedCurrTuple = mUnvisitedList.get(counter++);
                
                if(mEncodedCurrTuple.getVisited() == false){
                    visit(mEncodedCurrTuple);
                }
            }
            else{
                break;
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////////
        // TEMP: display number of states visited
        ///////////////////////////////////////////////////////////////////////////////
        System.out.println("\n\tNumber of states visited: " + mGlobalStateSet.size());
        ///////////////////////////////////////////////////////////////////////////////
        
        return mControlLoopFree;
    }
    
    /**
     * This method visits each state tuple in the synchronized product.
     * If it tries to visit state tuple that has been visited before,
     * it detects a loop.
     * @param encodedCurrTuple current state tuple property
     */
    public void visit(EncodedStateTuple encodedCurrTuple)
    {
        final int currTuple[] = new int[mNumAutomata]; // new memory allocation to store current state tuple
        encodedCurrTuple.setVisited(true);
        decoding(encodedCurrTuple.getCodes(), currTuple); // current state tuple now in currTuple
        
        for(int i = 0; i < mNumEvent; i++){ // for all events
            
            if(eventAvailable(currTuple, i)){
                if(mGlobalEventMap[i]){ // CONTROLLABLE
                    EncodedStateTuple encodedNextTuple = new EncodedStateTuple(encoding(mNextTuple));
                    
                    if(mGlobalStateSet.getOrAdd(encodedNextTuple) == null){
                        mUnvisitedList.add(encodedNextTuple);
                        visit(encodedNextTuple);
                        if(!mControlLoopFree){
                            return;
                        }
                    }
                    else{
                        encodedNextTuple = mGlobalStateSet.get(encodedNextTuple);
                        if(encodedNextTuple.getVisited() == false){
                            visit(encodedNextTuple);
                            if(!mControlLoopFree){
                                return;
                            }
                        }
                    }
                    
                    if(encodedNextTuple.getInComponent() == false){
                        if(encodedNextTuple.getVisited() == true){ // control loop detected here
                            if(mControlLoopFree){
                                mControlLoopFree = false;
                                mEncodedRootStateTuple = encodedCurrTuple;
                            }
                            return;
                        }
                    }
                }
                else{ // UNCONTROLLABLE
                    final EncodedStateTuple encodedNextTuple = new EncodedStateTuple(encoding(mNextTuple));
                    
                    if(mGlobalStateSet.getOrAdd(encodedNextTuple) == null){
                        mUnvisitedList.add(encodedNextTuple);
                    }
                }
            }
        }
        
        encodedCurrTuple.setInComponent(true);
    }
    
    /**
     * It checks event is available from current state tuple.
     * @param currTuple current state tuple
     * @param event current event index
     * @return return null if event is not available from current event, or return next state tuple
     */
    public boolean eventAvailable(int currTuple[], int event)
    {
	int temp;

	for(int i = 0; i < mNumAutomata; i++){
	    int map[] = mMap[event][i];
	    if(map != null){
		temp = map[currTuple[i]];
		if(temp > -1){ // next state exists
		    mNextTuple[i] = temp;
		}
		else{ // event is not available
		    return false;
		}
	    }
	    else{ // event is not in the automaton
		mNextTuple[i] = currTuple[i];
	    }
	}
	
	return true;
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
        
        //return null;
        
        /* FIND COUNTEREXAMPLE TRACE HERE */
        /* Counterexample = The shortest path from mInitialStateTuple to mRootStateTuple 
           + The shortest path from mRootStateTuple to mRootStateTuple */
        
        // find a shortest path from mRootStateTuple to mRootStateTuple: only for controllable events
        final Set<TransitionProperty> loopStates = new HashSet<TransitionProperty>();
        
        List<EncodedStateTuple> list = new LinkedList<EncodedStateTuple>();
        Set<EncodedStateTuple> set = new HashSet<EncodedStateTuple>();
        
        ArrayList<Integer> indexList = new ArrayList<Integer>();
        
        EncodedStateTuple encodedCurrTuple = new EncodedStateTuple(mNumInts);
        
        int lastEvent = -1;
        
        list.add(mEncodedRootStateTuple);
        indexList.add(0);
        int indexSize = 0;
        
        decoding(mEncodedRootStateTuple.getCodes(), mCurrTuple);
        int target[] = mCurrTuple; // target is mEncodedRootStateTuple (decoded)
        mCurrTuple = new int[mNumAutomata];
        
        loop:
        while(true){
            indexSize = indexList.size();
            for(int i = (indexSize==1)?0:(indexList.get(indexSize-2)+1); i <= indexList.get(indexSize-1); i++){
                encodedCurrTuple = list.get(i);
                decoding(encodedCurrTuple.getCodes(), mCurrTuple);
                for(int j = 0; j < mNumConEvent; j++){
                    if(mGlobalEventMap[j]){
                        if(eventAvailable(mCurrTuple, j)){
                            if(compare(mNextTuple, target)){
                                lastEvent = j;
                                break loop;
                            }
                            
                            EncodedStateTuple encodedNextTuple = new EncodedStateTuple(encoding(mNextTuple));
                            if(set.add(encodedNextTuple)){
                                list.add(encodedNextTuple);
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
            loopStates.add(new TransitionProperty(mEncodedRootStateTuple, mEncodedRootStateTuple, lastEvent));
        }
        else{
            loopStates.add(new TransitionProperty(encodedCurrTuple, mEncodedRootStateTuple, lastEvent));
            
            // swap memory location: mCurrTuple <-> target
            int tmp[] = mCurrTuple;
            mCurrTuple = target;
            target = tmp;
            
            for(int i = indexList.size()-2; i >= 0; i--){
                int start = (indexList.get(i)==0)?0:indexList.get(i-1)+1;
                int end = indexList.get(i);
                
                next:
                for(int j = start; j <= end; j++){
                    EncodedStateTuple encodedCurr = list.get(j);
                    decoding(encodedCurr.getCodes(), mCurrTuple);
                    
                    for(int k = 0; k < mNumConEvent; k++){
                        if(mGlobalEventMap[k]){
                            if(eventAvailable(mCurrTuple, k)){
                                EncodedStateTuple encodedNext = new EncodedStateTuple(encoding(mNextTuple));
                                if(compare(mNextTuple, target)){
                                    loopStates.add(new TransitionProperty(encodedCurr, encodedNext, k));
                                    
                                    // swap memory location: mCurrTuple <-> target
                                    tmp = mCurrTuple;
                                    mCurrTuple = target;
                                    target = tmp;
                                    
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
        if(!mEncodedInitialStateTuple.equals(mEncodedRootStateTuple)){
            list = new LinkedList<EncodedStateTuple>();
            set = new HashSet<EncodedStateTuple>();
            indexList = new ArrayList<Integer>();
            lastEvent = -1;
            
            list.add(mEncodedInitialStateTuple);
            indexList.add(0);
            indexSize = 0;
            
            loop2:
            while(true){
                indexSize = indexList.size();
                for(int i = (indexSize==1)?0:(indexList.get(indexSize-2)+1); i <= indexList.get(indexSize-1); i++){
                    encodedCurrTuple = list.get(i);
                    decoding(encodedCurrTuple.getCodes(), mCurrTuple);
                    
                    for(int j = 0; j < mNumEvent; j++){
                        if(eventAvailable(mCurrTuple, j)){
                            EncodedStateTuple encodedNextTuple = new EncodedStateTuple(encoding(mNextTuple));
                            if(isInLoop(encodedNextTuple, loopStates)){
                                tracelist.add(0, mEventList.get(j));
                                
                                // swap memory location: mCurrTuple <-> target
                                int tmp[] = mCurrTuple;
                                mCurrTuple = target;
                                target = tmp;
                                
                                mEncodedRootStateTuple = encodedNextTuple; // now change the root of the loop
                                
                                break loop2;
                            }
                            
                            if(set.add(encodedNextTuple)){
                                list.add(encodedNextTuple);
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
            for(int i = indexList.size()-2; i >= 0; i--){
                int start = (indexList.get(i)==0)?0:indexList.get(i-1)+1;
                int end = indexList.get(i);
                
                next2:
                for(int j = start; j <= end; j++){
                    EncodedStateTuple encodedCurr = list.get(j);
                    decoding(encodedCurr.getCodes(), mCurrTuple);
                    
                    for(int k = 0; k < mNumEvent; k++){
                        if(eventAvailable(mCurrTuple, k)){
                            if(compare(mNextTuple, target)){
                                tracelist.add(0, mEventList.get(k));
                                
                                // swap memory location: mCurrTuple <-> target
                                int tmp[] = mCurrTuple;
                                mCurrTuple = target;
                                target = tmp;
                                
                                break next2;
                            }
                        }
                    }
                }
            }
        }
        
        int loopIndex = tracelist.size();
        
        while(loopStates.size() > 0){
            for(final TransitionProperty tp: loopStates){
                if(compare(mEncodedRootStateTuple.getCodes(), tp.getSourceTuple().getCodes())){
                    tracelist.add(mEventList.get(tp.getEvent()));
                    mEncodedRootStateTuple = tp.getTargetTuple();
                    loopStates.remove(tp);
                    break;
                }
            }
        }
        
        final LoopTraceProxy trace = factory.createLoopTraceProxy(tracename, des, tracelist, loopIndex);
        return trace;
    }
    
    /**
     * It checks current state tuple is in the control loop
     * @param encodedCurrTuple encoded current state tuple
     * @param loopStates transitions in the control loop
     * @return return true if state tuple is in the loop, false otherwise
     */
    public boolean isInLoop(EncodedStateTuple encodedCurrTuple, Set<TransitionProperty> loopStates)
	{
        for(final TransitionProperty tp: loopStates){
            if(encodedCurrTuple.equals(tp.getSourceTuple())){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * It compares two state tuple.
     * @param tuple1 first tuple that will be compared with second tuple 
     * @param tuple2 second tuple
     * @return true if two tuple are equivalent, false otherwise.
     */
    public boolean compare(final int[] tuple1, final int[] tuple2)
    {
        for(int i = 0; i < tuple1.length; i++){
            if(tuple1[i] != tuple2[i]){
                return false;
            }
        }
        
        return true;
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
    
    /**
     * It will take a single state tuple as a parameter and encode it.
     * @param stateCodes state tuple that will be encoded
     * @return encoded state tuple
     */
    public int[] encoding(final int[] stateCodes)
    {
        int encoded[] = new int[mNumInts], i, j;
        
        for(i = 0; i < mNumInts; i++){
            for(j = mIndexAutomata[i]; j < mIndexAutomata[i+1]; j++){
                encoded[i] <<= mNumBits[j];
                encoded[i] |= stateCodes[j];
            }
        }
        
        return encoded;
    }
    
    /**
     * It will take an encoded state tuple as a parameter and decode it.
     * Decoded result will be contained in the second parameter
     * @param encodedStateCodes state tuple that will be decoded
     * @param currTuple the decoded state tuple will be stored here
     */
    public void decoding(final int[] encodedStateCodes, final int[] currTuple)
    {
        int tmp, mask, value, i, j;
        
        for(i = 0; i < mNumInts; i++){
            tmp = encodedStateCodes[i];
            for(j = mIndexAutomata[i+1]-1; j >= mIndexAutomata[i]; j--){
                mask = mNumBitsMasks[j];
                value = tmp & mask;
                currTuple[j] = value;
                tmp = tmp >> mNumBits[j];
            } 
        }
    }
}
