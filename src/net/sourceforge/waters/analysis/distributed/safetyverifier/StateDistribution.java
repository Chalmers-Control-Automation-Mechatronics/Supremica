package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

/**
 * A mapping between states and handlers. This class provides an
 * intermediate mapping between handler ids (as strings) and the state
 * handler object. This makes it more convenient to change the handler
 * without affecting the mapping between states and handlers. This
 * could hinder performance as it is an indirect lookup via a map, so
 * when a hander is set, a protected template hook is called, which
 * subclasses can override to update a higher performance lookup table.
 *
 * This class is not synchronized.
 *
 * @author Sam Douglas
 */
public abstract class StateDistribution implements StateHandler, Serializable
{
  /**
   * Dispatches the given state to the appropriate handler.
   * @param state the state to dispatch
   * @throws Exception if something bad happens
   */
  public void addState(StateTuple state) throws Exception
  {
    lookupStateHandler(state).addState(state);
  }

  /**
   * Sets the hander for a handler ID. This can be used to add 
   * new handlers (with new ids) however it does not guarantee they
   * will be used in the state distribution.
   * @param handlerid the ID of the handler to update.
   * @param handler the object to handle states
   */
  public void setHandler(String handlerid, StateHandler handler)
  {
    mHandlers.put(handlerid, handler);
    handlersUpdated();
  }

  /**
   * Gets the state handler for an ID.
   * @param handlerid the handler id
   * @return the state handler, or null if undefined.
   */
  public StateHandler getHandler(String handlerid)
  {
    return mHandlers.get(handlerid);
  }

  /**
   * Finds a handler for the specified state.
   * @param state to get handler for.
   * @return a state handler.
   */
  protected abstract StateHandler lookupStateHandler(StateTuple state);

  /**
   * A template method to allow subclasses to update any 
   * additional mappings when the handlers are updated. 
   */
  protected void handlersUpdated()
  {
  }
  
  private final Map<String,StateHandler> mHandlers = new HashMap<String,StateHandler>();
}