package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.rmi.Remote;
import java.rmi.RemoteException;

interface PredecessorCallback extends Remote
{
  /**
   * Takes a predecessor state for consideration in a counterexample.
   * This method returns the distance for the current best state the
   * controller knows about. This can be used workers to filter the
   * predecessors it returns.
   * @param original The state the predecessor is for
   * @param predecessor the predecessor state being given.
   * @param event the index of the event 
   * @return The current best distance.
   */
  public int takePredecessor(StateTuple original, StateTuple predecessor, int event) throws RemoteException;
}