package net.sourceforge.waters.analysis.po;
import java.util.BitSet;

public class PartialOrderEvent
{
  private PartialOrderEventStutteringKind mStutter_;
  private final int mEventIndex_;
  private final BitSet[] mEnablings_;
  private final BitSet[] mDisablings_;

  public PartialOrderEvent(final int eventIndex,final int numAutomata, final int numPlants, final int numEvents){
    mStutter_ = PartialOrderEventStutteringKind.STUTTERING;
    mEventIndex_ = eventIndex;
    mEnablings_ = new BitSet[numPlants];
    mDisablings_ = new BitSet[numAutomata - numPlants];
    for (int i = 0; i < numPlants; i++){
      mEnablings_[i] = new BitSet(numEvents);
    }
    for (int i = 0; i < numAutomata - numPlants; i++){
      mDisablings_[i] = new BitSet(numEvents);
    }
  }

  public int getEvent()
  {
    return mEventIndex_;
  }

  public BitSet[] getEnablings()
  {
    return mEnablings_;
  }

  public BitSet[] getDisablings()
  {
    return mDisablings_;
  }

  public PartialOrderEventStutteringKind getStutter()
  {
    return mStutter_;
  }

  public void setStutter(final PartialOrderEventStutteringKind kind)
  {
    mStutter_ = kind;
  }

  public void addEnabled(final int automatonIndex, final int eventIndex,
                         final boolean enable){
    if (enable && automatonIndex < mEnablings_.length) {
      mEnablings_[automatonIndex].set(eventIndex);
    }
    else if (!enable && automatonIndex >= mEnablings_.length){
      mDisablings_[automatonIndex - mEnablings_.length].set(eventIndex);
    }
  }

  public boolean eventEnablesUncontrollable(final int uncontrollableIndex,
                                            final int automatonIndex)
  {
    return mEnablings_[automatonIndex].get(uncontrollableIndex);

  }

  public boolean eventDisablesUncontrollable(final int uncontrollableIndex,
                                             final int automatonIndex)
  {
    return mDisablings_[automatonIndex - mEnablings_.length].get(uncontrollableIndex);
  }


}
