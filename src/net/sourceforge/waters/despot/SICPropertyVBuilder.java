package net.sourceforge.waters.despot;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


public interface SICPropertyVBuilder
{
  /**
   * Builds a model for a given answer event.
   * @param answerNm The name of the answer event.
   * @return
   */
  public ProductDESProxy getModel(String answerNm);

  public AutomatonProxy changeInterface(AutomatonProxy aut);

  public AutomatonProxy changeLowlevel(AutomatonProxy aut);

  public AutomatonProxy createT();

  //may later return something different
  public ConflictTraceProxy getCounterExample();
}
