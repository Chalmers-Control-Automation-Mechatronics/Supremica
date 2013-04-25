package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;

public class FindBlockingStates
{
  public FindBlockingStates(final ListBufferTransitionRelation automaton,
                            final int marking)
  {
    mAutomaton = automaton;
    mBlockingStates = new TIntHashSet();
    if (marking != -1) {
      final TIntArrayList reaches = new TIntArrayList();
      for (int s = 0; s < mAutomaton.getNumberOfStates(); s++) {
        if (!mAutomaton.isMarked(s, marking)) {
          mBlockingStates.add(s);
        } else {
          reaches.add(s);
        }
      }
      mAutomaton.reconfigure(ListBufferTransitionRelation.CONFIG_ALL);
      while (!reaches.isEmpty()) {
        final int state = reaches.removeAt(reaches.size() - 1);
        final TransitionIterator ti = mAutomaton.createPredecessorsReadOnlyIterator(state);
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

  private final ListBufferTransitionRelation mAutomaton;
  private final TIntHashSet mBlockingStates;
}

