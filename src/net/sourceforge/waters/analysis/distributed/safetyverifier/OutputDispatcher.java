package net.sourceforge.waters.analysis.distributed.safetyverifier;

interface OutputDispatcher extends StateHandler
{
  /**
   * Adds a state to be dispatched. This might not happen
   * synchronously.
   * @param state The state to add
   */
  public void addState(StateTuple state) throws Exception;

  /**
   * Adds multiple states to be dispatched. This is given as a slice
   * of an array. It is expected that offset and length are sensible.
   * 
   * The array will not be used outside of this method call, and only
   * the supplied range will be read, so it is safe to pass a buffer 
   * to this method.
   *
   * @param states Array of states to add.
   * @param offset Index of first state to add
   * @param length Number of states to add from the array
   */
  public void addStates(StateTuple[] states, int offset, int length) throws Exception;

  /**
   * Shut down the state dispatcher. This hook is added so
   * that any threads can be terminated and resources freed.
   */
  public void shutdown();
}