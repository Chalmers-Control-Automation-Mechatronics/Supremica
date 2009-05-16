package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote
{
  /**
   * An empty method that can be used to check if the 
   * node is alive.
   */
  public void ping() throws RemoteException;
}