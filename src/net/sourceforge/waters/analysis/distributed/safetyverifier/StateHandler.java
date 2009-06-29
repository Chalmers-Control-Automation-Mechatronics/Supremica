package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.io.Serializable;

/**
 * An interface for adding new states.
 * @author Sam Douglas
 */
public interface StateHandler extends Serializable
{
  /**
   * Adds a state. This method may block until any IO has
   * completed. Throwing an exception should indicate a failure
   * to add a state, which may have compromised the model 
   * verification.
   * @param state the state to add
   * @throws Exception if something goes wrong.
   */
  public void addState(StateTuple state) throws Exception;
}