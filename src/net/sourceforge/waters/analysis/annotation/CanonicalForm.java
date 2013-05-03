package net.sourceforge.waters.analysis.annotation;



public class CanonicalForm
{
  /*private final ListBufferTransitionRelation mRelation;
  private final Map<TIntHashSet, TIntHashSet> mSetCache;
  private final TObjectIntHashMap<Tuple> mTupleCache;
  private final List<Tuple> mStates;
  private final List<TIntArrayList> mSuccessors;
  private final List<TIntHashSet[]> mPredeccessors;
  private final List<TIntHashSet> mainstates;
  private final TIntHashSet mFirstLC;
  private final TIntHashSet mBlocking;
  private final int mMarking;
  private int mExpanded;

  public CompareLessConflicting(final ListBufferTransitionRelation relation,
                                final int marking)
  {
    mRelation = relation;
    mSetCache = new HashMap<TIntHashSet, TIntHashSet>();
    mTupleCache = new TObjectIntHashMap<Tuple>();
    mStates = new ArrayList<Tuple>();
    mFirstLC = new TIntHashSet();
    mExpanded = 0;
    mMarking = marking;
    FindBlockingStates fbs = new FindBlockingStates(mRelation, mMarking);
    mBlocking = fbs.getBlockingStates();
    fbs = new FindBlockingStates(first, mMarking);
    mSuccessors = new ArrayList<TIntArrayList>();
    mPredeccessors = new ArrayList<TIntHashSet[]>();
  }

  public TIntHashSet calculateTauReachable(int state, ListBufferTransitionRelation trans)
  {
    TIntHashSet set = new TIntHashSet();
    set.add(state);
    return calculateTauReachable(set, trans);
  }

  public TIntHashSet calculateTauReachable(TIntHashSet set,
                                           ListBufferTransitionRelation trans)
  {
    TIntHashSet taureach = new TIntHashSet(set.toArray());
    TIntArrayList togo = new TIntArrayList(set.toArray());
    while (!togo.isEmpty()) {
      int state = togo.remove(togo.size() - 1);
      TransitionIterator ti = trans.createSuccessorsReadOnlyIterator(state,
                                                                     EventEncoding.TAU);
      while (ti.advance()) {
        if (taureach.add(ti.getCurrentTargetState())) {
          togo.add(ti.getCurrentTargetState());
        }
      }
    }

    return taureach;
  }

  public TIntHashSet calculateSuccessor(TIntHashSet set, int event,
                                        ListBufferTransitionRelation trans)
  {
    set = calculateTauReachable(set, trans);// this shouldn't be needed
    TIntHashSet succ = new TIntHashSet();
    TIntIterator it = set.iterator();
    while (it.hasNext()) {
      int s = it.next();
      if (s == -1) {return null;}
      if (event != trans.getNumberOfProperEvents()) {
        TransitionIterator ti = trans.createSuccessorsReadOnlyIterator(s, event);
        while (ti.advance()) {
          succ.add(ti.getCurrentTargetState());
        }
      } else {
        if (trans.isMarked(s, mMarking)) {
          succ.add(-1);
          return succ;
        }
      }
    }
    return calculateTauReachable(succ, trans);
  }

  private int getState(Tuple tup)
  {
    if (!mTupleCache.containsKey(tup)) {
      int state = mStates.size();
      mTupleCache.put(tup, state);
      mStates.add(tup);
      int[] sucs = new int[mFirstRelation.getNumberOfProperEvents() + 1];
      for (int i = 0; i < sucs.length; i++) {
        sucs[i] = -1;
      }
      mSuccessors.add(new TIntArrayList(sucs));
      mPredeccessors.add(new TIntHashSet[mFirstRelation.getNumberOfProperEvents() + 1]);
      if (tup.firstset.contains(-1)) {
        mFirstLC.add(state);
      }
      TIntIterator it = tup.secondset.iterator();
      while (it.hasNext()) {
        int num = it.next();
        if (mBlocking.contains(num)) {
          mFirstLC.add(state); break;
        }
      }
    }
    int state = mTupleCache.get(tup);
    return state;
  }

  public void expandStates()
  {
    for (;mExpanded < mStates.size(); mExpanded++) {
      int state = mExpanded;
      Tuple tup = mStates.get(state);
      if (tup.firstset.contains(-1) || tup.secondset.contains(-1)) {
        continue;
      }
      //System.out.println(tup);
      TIntIterator it = tup.firstset.iterator();
      while (it.hasNext()) {
        TIntHashSet f = new TIntHashSet();
        f.add(it.next());
        getState(new Tuple(f, tup.secondset));
      }
      for (int e = 0; e < mFirstRelation.getNumberOfProperEvents() + 1; e++) {
        if (e == EventEncoding.TAU) {continue;}
        TIntHashSet first = calculateSuccessor(tup.firstset, e, mRelation);
        TIntHashSet second = calculateSuccessor(tup.secondset, e, mRelation);
        int target = getState(new Tuple(first, second));
        mSuccessors.get(state).set(e, target);
        TIntHashSet preds = mPredeccessors.get(target)[e];
        if (preds == null) {
          preds = new TIntHashSet();
          TIntHashSet[] predsarr = mPredeccessors.get(target);
          predsarr[e] = preds;
        }
        preds.add(state);
      }
      TIntHashSet first = calculateSuccessor(tup.firstset, mMarking, mRelation);
      TIntHashSet second = calculateSuccessor(tup.secondset, mMarking, mRelation);
      int target = getState(new Tuple(first, second));
      mSuccessors.get(state).set(mMarking, target);
    }
  }

  public void calculateLCStates()
  {
    boolean modified = true;
    int LC = 0;
    while (modified) {
      System.out.println("LC: " + LC++ + " " + mFirstLC.size());
      modified = false;
      TIntArrayList makelc = new TIntArrayList();
      Set<Triple> MCTriples = new THashSet<Triple>();
      List<Triple> tobeexpanded = new ArrayList<Triple>();
      for (int s = 0; s < mStates.size(); s++) {
        //System.out.println(mStates.get(s));
        if (!mFirstLC.contains(s)) {
          makelc.add(s);
          Tuple state = mStates.get(s);
          TIntHashSet moreset = state.secondset;
          if (moreset.contains(-1)) {
            //System.out.println("MC:" + Arrays.toString(state.firstset.toArray()) + " : " + Arrays.toString(moreset.toArray()));
            Triple triple = new Triple(mStates.get(s), -1);
            MCTriples.add(triple);
            tobeexpanded.add(triple);
          }
        }
      }
      while (!tobeexpanded.isEmpty()) {
        Triple triple = tobeexpanded.remove(tobeexpanded.size() - 1);
        for (int e = 0; e < mFirstRelation.getNumberOfProperEvents() + 1; e++) {
          if (e == EventEncoding.TAU) {continue;}
          TIntHashSet preds = mPredeccessors.get(mTupleCache.get(triple.tuple))[e];
          if (preds == null) {continue;}
          TIntIterator it = preds.iterator();
          while (it.hasNext()) {
            int pred = it.next();
            if (mFirstLC.contains(pred)) {continue;}
            Tuple predtuple = mStates.get(pred);
            TIntHashSet moreset = predtuple.secondset;
            TIntIterator itstates = moreset.iterator();
            while (itstates.hasNext()) {
              int state = itstates.next();
              TIntHashSet newset = new TIntHashSet(); newset.add(state);
              TIntHashSet statesuccessors = calculateSuccessor(newset, e, mSecondRelation);
              if (statesuccessors.contains(triple.state)) {
                //System.out.println("MC:" + Arrays.toString(predtuple.firstset.toArray()) + " : " + Arrays.toString(moreset.toArray()) + ":" + state);
                Triple add = new Triple(mStates.get(pred), state);
                if (MCTriples.add(add)) {tobeexpanded.add(add);}
              }
            }
          }
        }
      }
      System.out.println("MC: " + MCTriples.size());
      LCPAIRS:
      for (int i = 0; i < makelc.size(); i++) {
        int state = makelc.get(i);
        Tuple tup = mStates.get(state);
        TIntIterator it2 = tup.secondset.iterator();
        while (it2.hasNext()) {
          int propstate = it2.next();
          Triple triple = new Triple(tup, propstate);
          if (!MCTriples.contains(triple)) {
            mFirstLC.add(state);
            modified = true;
          }
        }
      }
    }
  }

  public ListBufferTransitionRelation CreateCanon()
  {
    TIntHashSet second = new TIntHashSet();
    for (int s = 0; s < mRelation.getNumberOfStates(); s++) {
      if (mRelation.isInitial(s)) {
        second.add(s);
        continue;
      }
    }
    Tuple initial = new Tuple(new TIntHashSet(),
                              calculateTauReachable(second, mSecondRelation));
    // creates the subset construction paired with emptyset.
    getState(tuple);
    expandStates();
    // calculates the set of certain conflicts
    calculateLCStates();
    // calculate language as pre process
    // The first set is the certain conflicts
    TObjectIntHashMap<TIntHashSet> settoclass = new TObjectIntHashMap<TIntHashSet>();
    List<Set<TIntHashSet>> equivalentlanguage = languageEquivalent();
    List<Set<TIntHashSet>> equivalentconflict = new HashSet<Set<TIntHashSet>>();
    equivalentconflict.add(equivalentlanguage.get(0));
    for (int i = 1; i < equivalentlanguage.size(); i++) {
      Set<TIntHashSet> equiv = equivalentlanguage.get(0);
      while(!equiv.isEmpty()) {
        Set<TIntHashSet> confequiv = new HashSet<TIntHashSet>();
        Iterator it = equiv.iterator();
        TIntHashSet stateset1 = it.next();
        confequiv.add(stateset1);
        settoclass.set(stateset1, equivalentconflict.size() - 1);
        it.remove();
        while(it.hasNext()) {
          TIntHashSet stateset2 = it.next();
          if (isLessConflicting(new Tuple(stateset1, stateset2)) && isLessConflicting(new Tuple(stateset2, stateset1))) {
            confequiv.add(stateset2); it.remove();
            settoclass.set(stateset2, equivalentconflict.size() - 1);
          }
        }
      }
    }
  }

  public boolean isLessConflicting(Tuple tuple)
  {
    long time = System.currentTimeMillis();
    int initial = getState(tuple);
    // adds the certain conflict states to the calculation
    getState(new Tuple(new TIntHashSet(), tuple.secondset));
    expandStates();
    System.out.println("tuples: " + mStates.size());
    calculateLCStates();
    //System.out.println("LC:" + mFirstLC.size());
    TIntHashSet explored = new TIntHashSet();
    TIntArrayList toexplore = new TIntArrayList();
    explored.add(initial);
    toexplore.add(initial);
    while (!toexplore.isEmpty()) {
      //System.out.println(mStates.size());
      int s = toexplore.remove(toexplore.size() -1);
      Tuple state = mStates.get(s);
      if (state.firstset.isEmpty()) {continue;}
      if (state.firstset.size() > 1) {
        TIntIterator it = state.firstset.iterator();
        while (it.hasNext()) {
          TIntHashSet set = new TIntHashSet();
          set.add(it.next());
          if (explored.add(getState(new Tuple(set, state.secondset)))) {
            toexplore.add(getState(new Tuple(set, state.secondset)));
          }
        }
        continue;
      }
      //calculateLCStates();
      ////System.out.println("LC:" + mFirstLC.size());
      //System.out.println(state);
      if (mFirstLC.contains(getState(new Tuple(new TIntHashSet(), state.secondset)))) {continue;}
      if (!mFirstLC.contains(s)) {
        TIntArrayList states = new TIntArrayList();
        TIntHashSet visited2 = new TIntHashSet();
        visited2.add(s);
        states.add(s);
        while (!states.isEmpty()) {
          int snum = states.remove(0);
          state = mStates.get(snum);
          System.out.println("tuple: " + state);
          for (int e = 0; e < mSuccessors.get(snum).size(); e++) {
            if (e == EventEncoding.TAU) {continue;}
            if (mSuccessors.get(snum).get(e) != -1) {
              Tuple target = mStates.get(mSuccessors.get(snum).get(e));
              int tnum = mSuccessors.get(snum).get(e);
              System.out.println(state + " " + mFirstLC.contains(snum) + " -" + e + "-> " + target + " " + mFirstLC.contains(tnum));
              if (visited2.add(tnum) && !mFirstLC.contains(tnum)) {
                states.add(tnum);
              }
            }
          }
        }
        time -= System.currentTimeMillis();
        System.out.println("Time: " + time);
        return false;
      }
      TIntArrayList succs = mSuccessors.get(s);
      for (int e = 0; e < succs.size(); e++) {
        if (e == EventEncoding.TAU) {continue;}
        int suc = succs.get(e);
        if (suc == -1) {continue;}
        if (explored.add(suc)) {
          //System.out.println(state + " -" + e + "-> " + mStates.get(suc));
          toexplore.add(suc);
        }
      }
    }
    time -= System.currentTimeMillis();
    System.out.println("Time: " + time);
    return true;
  }

  public TIntHashSet getSet(TIntHashSet set)
  {
    TIntHashSet tset = mSetCache.get(set);
    if (tset == null) {
      tset = set;
      mSetCache.put(set, set);
    }
    return tset;
  }

  private abstract class View
  {
    abstract TIntHashSet getMoreSet(Tuple t);
    abstract TIntHashSet getLessSet(Tuple t);
    abstract ListBufferTransitionRelation getMoreRelation();
    abstract ListBufferTransitionRelation getLessRelation();
    abstract Tuple createTuple(TIntHashSet less, TIntHashSet more);
  }

  private class Triple
  {
    public final Tuple tuple;
    public final int state;

    public Triple(Tuple ptuple, int s)
    {
      tuple = ptuple;
      state = s;
    }

    public int hashCode()
    {
      return tuple.hashCode() * 13 + state;
    }

    public boolean equals(Object o)
    {
      Triple other = (Triple)o;
      return state == other.state && tuple.equals(other.tuple);
    }
  }

  public static void main(String[] args)
  {
    String modname = args[0];
    String lessconf = args[1];
    String moreconf = args[2];
    String markname = args[3];
    String tauname = args[4];
    try {
      final ProductDESProxy model = getCompiledDES(new File(modname), null);
      AutomatonProxy lprox = null;
      AutomatonProxy mprox = null;
      EventProxy mproxy = null;
      EventProxy tauproxy = null;
      for (AutomatonProxy aut : model.getAutomata()) {
        if (aut.getName().equals(lessconf)) {lprox = aut;}
        if (aut.getName().equals(moreconf)) {mprox = aut;}
      }
      for (EventProxy ev : model.getEvents()) {
        if (ev.getName().equals(markname)) {mproxy = ev;}
        if (ev.getName().equals(tauname)) {tauproxy = ev;}
      }
      EventEncoding ee = new EventEncoding(lprox,
                                ConflictKindTranslator.getInstance(), tauproxy);
      if (!lprox.getEvents().contains(mproxy)) {
        ee.addEvent(mproxy, ConflictKindTranslator.getInstance(), true);
      }
      ListBufferTransitionRelation lessbuff =
        new ListBufferTransitionRelation(lprox, ee,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      ListBufferTransitionRelation morebuff =
        new ListBufferTransitionRelation(mprox, ee,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      int mint = ee.getEventCode(mproxy);
      CompareLessConflicting clc = new CompareLessConflicting(lessbuff, morebuff, mint);
      System.out.println(lessbuff);
      System.out.println(morebuff);
      System.out.println("Is LC: " + clc.isLessConflicting());
    } catch(Throwable t) {
      t.printStackTrace();
    }
  }

  private class Tuple
  {
    public final TIntHashSet firstset;
    public final TIntHashSet secondset;

    public Tuple(TIntHashSet first, TIntHashSet second)
    {
      firstset = getSet(first);
      secondset = getSet(second);
      if (firstset == null || secondset == null) {
        throw new RuntimeException();
      }
    }

    public Tuple(TIntHashSet first, TIntHashSet second, boolean alreadycanon)
    {
      firstset = first;
      secondset = second;
    }

    public int hashCode()
    {
      return firstset.hashCode() * 13 + secondset.hashCode();
    }

    public boolean equals(Object o)
    {
      Tuple other = (Tuple)o;
      return firstset == other.firstset && secondset == other.secondset;
    }

    public String toString()
    {
      return Arrays.toString(firstset.toArray()) + " : " + Arrays.toString(secondset.toArray());
    }
  }

  private static ProductDESProxy getCompiledDES
    (final File filename,
     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final DocumentProxy doc = mDocumentManager.load(filename);
    if (doc instanceof ProductDESProxy) {
      return (ProductDESProxy) doc;
    } else if (doc instanceof ModuleProxy) {
      final ModuleProxy module = (ModuleProxy) doc;
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, mProductDESProxyFactory, module);
      return compiler.compile(bindings);
    } else {
      return null;
    }
  }

  public static ListBufferTransitionRelation
    mergeConflictEquivalent(ListBufferTransitionRelation aut, int marking)
  {
    CompareLessConflicting clc = new CompareLessConflicting(aut, aut, marking);
    TIntObjectHashMap<TIntArrayList> statetogroup = new TIntObjectHashMap<TIntArrayList>();
    List<TIntArrayList> values = new ArrayList<TIntArrayList>();
    for (int s1 = 0; s1 < aut.getNumberOfStates(); s1++) {
      if (statetogroup.containsKey(s1)) {continue;}
      TIntArrayList group = new TIntArrayList();
      group.add(s1);
      statetogroup.put(s1, group);
      values.add(group);
      for (int s2 = s1 + 1; s2 < aut.getNumberOfStates(); s2++) {
        if (statetogroup.containsKey(s2)) {continue;}
        if (clc.isLessConflicting(s1, s2) && clc.isLessConflicting(s2, s1)) {
          group.add(s2);
          statetogroup.put(s2, group);
        }
      }
      System.out.println("same:" + group.size());
    }
    List<int[]> partitions = new ArrayList<int[]>();
    System.out.println(aut.getNumberOfStates() + "vs" + values.size());
    for (TIntArrayList list : values) {
      partitions.add(list.toNativeArray());
    }
    aut.merge(partitions);
    return aut;
  }

  private static DocumentManager mDocumentManager = new DocumentManager();
  private static ProductDESProxyFactory mProductDESProxyFactory = ProductDESElementFactory.getInstance();

  static {
    final ModuleElementFactory mModuleFactory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    try {
      final JAXBModuleMarshaller modmarshaller =
        new JAXBModuleMarshaller(mModuleFactory, optable);
      mDocumentManager.registerUnmarshaller(modmarshaller);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }*/
}
