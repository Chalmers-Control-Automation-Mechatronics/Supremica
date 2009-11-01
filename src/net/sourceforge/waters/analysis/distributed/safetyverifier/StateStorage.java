package net.sourceforge.waters.analysis.distributed.safetyverifier;

/**
 * State storage abstracts the state queue, observed set and
 * storage of state tuples.
 */
public interface StateStorage
{
  /**
   * Add a state tuple to the storage. This will add it to the queue
   * to process if necessary.
   * @param state State tuple to add
   */
  public void addState(StateTuple state);

  /**
   * Gets the next state from the queue.
   * @return The next state tuple, or null if the queue is empty.
   */
  public StateTuple getNextState();

  /**
   * Gets the number of unprocessed state tuples waiting 
   * in the queue.
   * @return Number of unprocessed state tuples
   */
  public int getUnprocessedStateCount();

  /**
   * Gets the depth value associated with the state.
   * @param state The state tuple to get depth for
   * @return The depth of the requested state.
   */
  public int getStateDepth(StateTuple state);

  /**
   * Gets the total state count.
   */
  public int getStateCount();

  /**
   * Gets the number of processed states.
   */
  public int getProcessedStateCount();

  /**
   * Checks if the state storage contains this state.
   */
  public boolean containsState(StateTuple state);
}