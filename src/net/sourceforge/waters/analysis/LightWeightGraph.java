package net.sourceforge.waters.analysis;

import gnu.trove.TIntArrayList;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.plain.base.NamedElement;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class LightWeightGraph
  extends NamedElement
  implements AutomatonProxy
{
  private static final long serialVersionUID = 1L;

  private final int[][][] mTransitions;
  private final EventProxy[] mEvents;
  private final boolean[] mMarked;
  private final EventProxy mMark;
  private final ProductDESProxyFactory mFactory;
  private final int mStateNum;
  private Set<EventProxy> mEventSet;
  private Set<StateProxy> mStateSet;
  private Collection<TransitionProxy> mTransitionSet;
  
  public LightWeightGraph(String name, int[][] transitions, int statenum,
                          EventProxy[] events, int[] marked, EventProxy mark,
                          ProductDESProxyFactory factory)
  {
    super(name);
    mStateNum = statenum;
    mTransitions = null; //transitions;
    mEvents = events;
    mMarked = new boolean[mStateNum];
    for (int i = 0; i < marked.length; i++) {mMarked[marked[i]] = true;}
    mMark = mark;
    mFactory = factory;
  }
  
  public LightWeightGraph(String name, int[][] transitions, int statenum,
                          EventProxy[] events, boolean[] marked, EventProxy mark,
                          ProductDESProxyFactory factory)
  {
    super(name);
    mStateNum = statenum;
    mTransitions = null; //transitions;
    mEvents = events;
    mMarked = marked;
    mMark = mark;
    mFactory = factory;
  }
  
  public LightWeightGraph(AutomatonProxy graph, ProductDESProxyFactory factory,
                          EventProxy mark)
  {
    super(graph.getName());
    mStateNum = graph.getStates().size();
    StateProxy[] states = new StateProxy[mStateNum];
    states = graph.getStates().toArray(states);
    mEvents = graph.getEvents().toArray(new EventProxy[0]);
    Arrays.sort(mEvents);
    mMarked = new boolean[mStateNum];
    mTransitions = new int[mEvents.length][mStateNum][];
    mMark = mark;
    TObjectIntHashMap<StateProxy> statemap = 
      new TObjectIntHashMap<StateProxy>(mStateNum);
    TObjectIntHashMap<EventProxy> eventmap = 
      new TObjectIntHashMap<EventProxy>(mEvents.length);
    // puts initial state at the front
    for (int i = 0; i < states.length; i++) {
      if (states[i].isInitial()) {
        StateProxy t = states[0]; states[0] = states[i]; states[i] = t; break;
      }
    }
    // sets up the state map and marked states
    for (int i = 0; i < states.length; i++) {
      statemap.put(states[i], i);
      if (states[i].getPropositions().contains(mMark)) {mMarked[i] = true;}
    }
    // set up the event map
    for (int i = 0; i < mEvents.length; i++) {eventmap.put(mEvents[i], i);}
    TIntArrayList[][] temptransitions = new TIntArrayList[mEvents.length][mStateNum];
    for (TransitionProxy t : graph.getTransitions()) {
      int source = statemap.get(t.getSource());
      int target = statemap.get(t.getTarget());
      int event = eventmap.get(t.getEvent());
      TIntArrayList targs = temptransitions[event][source];
      if (targs == null) {
        targs = new TIntArrayList(); temptransitions[event][source] = targs;
      }
      targs.add(target);
    }
    for (int i = 0; i < temptransitions.length; i++) {
      for (int j = 0; j < temptransitions[i].length; j++) {
        TIntArrayList t = temptransitions[i][j];
        if (t == null) {continue;}
        mTransitions[i][j] = t.toNativeArray();
      }
    }
    mFactory = factory;
  }
  
  public LightWeightGraph clone()
  {
    assert(false);
    return null;
    //return new LightWeightGraph(getName(), null /*mTransitions*/, mStateNum, mEvents,
    //                            mMarked, mMark, mFactory);
  }
  
  public Set<EventProxy> getEvents()
  {
    if (mEventSet == null) {
      mEventSet = new HashSet<EventProxy>(Arrays.asList(mEvents));
    }
    return mEventSet;
  }
  
  /*private int[][] compress(int[][] transitions)
  {
    int[] active = new int[mStateNum];
    TObjectIntHashMap<int[]> map = new TObjectIntHashMap<int[]>(ArrayHash.ARRAYHASH);
    for (int i = 0; i < transitions.length; i++) {
      int count = map.get(transitions[i]);
      if (count == 0) {
        active[transitions[i][0]]++;
      }
      count++;
      map.put(transitions[i], count);
    }
    final int[][][] newtransitions = new int[mStateNum][][];
    for (int i = 0; i < active.length; i++) {
      newtransitions[i] = new int[active[i]][];
      active[i] = 0; // now use this to count which one we are up to
    }
    map.forEachEntry(new TObjectIntProcedure() {
      public boolean execute(int[] arr, int val)
      {
        int source = arr[0];
        int event = active[source];
        active[source]++;
        
        return true;
      }
    })
  }*/
  
  private void setup()
  {
    System.out.println("Setup");
    StateProxy[] states = new StateProxy[mStateNum];
    int j = 0;
    boolean[] marked = mMarked;
    for (int i = 0; i < states.length; i++) {
      if (marked[i]) {
        j++;
      }
      states[i] = new MemStateProxy(i, mMark);
    }
    mStateSet = new HashSet<StateProxy>(Arrays.asList(states));
    mTransitionSet = new ArrayList<TransitionProxy>(mTransitions.length);
    for (int i = 0; i < mTransitions.length; i++) {
      EventProxy e = mEvents[i];
      for (j = 0; j < mTransitions[i].length; j++) {
        StateProxy s = states[j];
        if (mTransitions[i][j] != null) {
          for (int k = 0; k < mTransitions[i][j].length; k++) {
            StateProxy t = states[mTransitions[i][j][k]];
            mTransitionSet.add(mFactory.createTransitionProxy(s, e, t));
          }
        }
      }
    }
  }
  
  public Set<StateProxy> getStates()
  {
    if (mStateSet == null) {
      setup();
    }
    return mStateSet;
  }
  
  public Collection<TransitionProxy> getTransitions()
  {
    if (mTransitionSet == null) {
      setup();
    }
    return mTransitionSet;
  }
  
  public ComponentKind getKind()
  {
    return ComponentKind.SPEC;
  }
  
  public int[][] getLightTransitions()
  {
    return new int[0][0];
    //return mTransitions;
  }
  
  public boolean[] getMarkedStates()
  {
    return mMarked;
  }
  
  public EventProxy getMarked()
  {
    return mMark;
  }
  
  public EventProxy[] getLightEvents()
  {
    return mEvents;
  }
  
  public int getStateNum()
  {
    return mStateNum;
  }
  
  public int hashCode()
  {
    return System.identityHashCode(this);
  }
  
  public boolean equals(Object o)
  {
    return System.identityHashCode(this) == System.identityHashCode(o);
  }
  
  private static class MemStateProxy
    implements StateProxy
  {
    private final int mName;
    private final EventProxy mEvent;
    
    public MemStateProxy(int name, EventProxy event)
    {
      mName = name;
      mEvent = event;
    }
    
    public Collection<EventProxy> getPropositions()
    {
      if (mEvent == null) {
        return Collections.emptySet();
      } else {
        return Collections.singleton(mEvent);
      }
    }
    
    public boolean isInitial()
    {
      return mName == 0;
    }
    
    public MemStateProxy clone()
    {
      return new MemStateProxy(mName, mEvent);
    }
    
    public String getName()
    {
      return Integer.toString(mName);
    }
    
    @SuppressWarnings("unused")
	public boolean refequals(Object o)
    {
      if (o instanceof NamedProxy) {
        return refequals((NamedProxy) o);
      }
      return false;
    }
    
    public boolean refequals(NamedProxy o)
    {
      if (o instanceof MemStateProxy) {
        final MemStateProxy s = (MemStateProxy) o;
        return s.mName == mName;
      } else {
        return false;
      }
    }
    
    public int refHashCode()
    {
      return mName;
    }
    
    public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
    {
      final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
      return desvisitor.visitStateProxy(this);
    }

    public Class<StateProxy> getProxyInterface()
    {
      return StateProxy.class;
    }

    public boolean equalsByContents(final Proxy partner)
    {
      if (partner != null &&
          partner.getProxyInterface() == getProxyInterface()) {
        final StateProxy state = (StateProxy) partner;
        return (getName().equals(state.getName())) &&
               (isInitial() == state.isInitial()) &&
               state.getPropositions().isEmpty();
      } else {
        return false;
      }
    }
    
    public boolean equalsWithGeometry(Proxy o)
    {
      return equalsByContents(o);
    }
    
    public int hashCodeByContents()
    {
      return refHashCode();
    }
    
    public int hashCodeWithGeometry()
    {
      return refHashCode();
    }
    
    public int compareTo(NamedProxy n)
    {
      return n.getName().compareTo(getName());
    }
    
    public String toString()
    {
      return "S:" + mName;
    }
  }
  
  public Object acceptVisitor(ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitAutomatonProxy(this);
  }

  public Class<AutomatonProxy> getProxyInterface()
  {
    return AutomatonProxy.class;
  }

}
