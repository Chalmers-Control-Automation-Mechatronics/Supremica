package net.sourceforge.waters.analysis.po;

import java.util.BitSet;


public class PartialOrderEvent
{
  private final int eventIndex_;
  private final BitSet[] enablings_;
  private final BitSet[] disablings_;

  public PartialOrderEvent(final int eventIndex,final int numAutomata, final int numPlants){
    eventIndex_ = eventIndex;
    enablings_ = new BitSet[numPlants];
    disablings_ = new BitSet[numAutomata - numPlants];
  }

  public int getEvent()
  {
    return eventIndex_;
  }

  public BitSet[] getEnablings()
  {
    return enablings_;
  }

  public BitSet[] getDisablings()
  {
    return disablings_;
  }

  public void addEnabled(final int automatonIndex, final int eventIndex,
                         final boolean enable){
    if (enable && automatonIndex < enablings_.length) {
      enablings_[automatonIndex].set(eventIndex);
    }
    else if (!enable && automatonIndex >= enablings_.length){
      disablings_[automatonIndex - enablings_.length].set(eventIndex);
    }
  }



  public boolean eventEnablesUncontrollable(final int uncontrollableIndex,
                                            final int automatonIndex,
                                            final boolean enable)
  {
    return enable ? enablings_[automatonIndex].get(uncontrollableIndex) :
                    disablings_[automatonIndex - enablings_.length].get(uncontrollableIndex);
  }

}
