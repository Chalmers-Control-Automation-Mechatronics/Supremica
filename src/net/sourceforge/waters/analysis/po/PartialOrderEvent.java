package net.sourceforge.waters.analysis.po;

import java.util.BitSet;
import net.sourceforge.waters.model.des.EventProxy;


public class PartialOrderEvent
{
  public static int NUMAUTOMATA_;
  private final EventProxy event_;
  private BitSet[] enablings_;

  public PartialOrderEvent(final EventProxy event){
    event_ = event;
    enablings_ = new BitSet[NUMAUTOMATA_];
  }

  public EventProxy getEvent()
  {
    return event_;
  }

  public BitSet[] getEnablings()
  {
    return enablings_;
  }

  public void addEnabled(final int automatonIndex, final int eventIndex){
    enablings_[automatonIndex].set(eventIndex);
  }

  public void setEnablings(final BitSet[] bitSets)
  {
    enablings_ = bitSets;
  }

  public boolean eventEnablesUncontrollable(final int uncontrollableIndex,
                                            final int automatonIndex)
  {
    return enablings_[automatonIndex].get(uncontrollableIndex);
  }
}
