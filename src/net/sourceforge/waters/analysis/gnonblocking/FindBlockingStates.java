package net.sourceforge.waters.analysis.gnonblocking;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntArrayList;
import net.sourceforge.waters.analysis.tr.TransitionIterator;

public class FindBlockingStates
{
  public FindBlockingStates(ListBufferTransitionRelation automaton,
                            int marking)
  {
    mAutomaton = automaton;
    mBlockingStates = new TIntHashSet();
    if (marking != -1) {
      TIntArrayList reaches = new TIntArrayList();
      for (int s = 0; s < mAutomaton.getNumberOfStates(); s++) {
        if (!mAutomaton.isMarked(s, marking)) {
          mBlockingStates.add(s);
        } else {
          reaches.add(s);
        }
      }
      mAutomaton.reconfigure(ListBufferTransitionRelation.CONFIG_ALL);
      while (!reaches.isEmpty()) {
        int state = reaches.remove(reaches.size() - 1);
        TransitionIterator ti = mAutomaton.createPredecessorsReadOnlyIterator(state);
        while (ti.advance()) {
          if (mBlockingStates.remove(ti.getCurrentSourceState())) {
            reaches.add(ti.getCurrentSourceState());
          }
        }
      }
    }
  }
  
  public TIntHashSet getBlockingStates()
  {
    return mBlockingStates;
  }
  
  private ListBufferTransitionRelation mAutomaton;
  private TIntHashSet mBlockingStates;
}
