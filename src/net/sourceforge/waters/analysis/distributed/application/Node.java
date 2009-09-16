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

  /**
   * A factory method to create workers given a class name.
   * The class name should be the full name for a class, and the
   * class must be assignable to the Worker interface.
   * 
   * Exceptions will be thrown if the worker cannot be created... 
   * somehow through the remote exception
   *
   * @param id Controller id, used for resource management.
   * @param classname name of the class to load.
   * @param cb error callback to use, null for no callback.
   * @return remote instance of a worker. 
   * @throws IllegalArgumentException if the controller ID or classname
   *                                  is null.
   */
  public Worker createWorker(ControllerID id, String classname, ErrorCallback cb) 
    throws 
    ClassNotFoundException,
    IllegalAccessException,
    IllegalArgumentException,
    InstantiationException,
    RemoteException;


  public void cleanup(ControllerID id) throws RemoteException;

  public void shutdown() throws RemoteException;
}