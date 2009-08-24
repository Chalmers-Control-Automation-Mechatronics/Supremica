package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote
{
  /**
   * A simple method that can be used to check if the
   * server is alive.
   */
  public void ping() throws RemoteException;

  public void registerNode(Node node) throws RemoteException;

  /**
   * Submit a job for processing. This remote method will not return
   * until the job is finished, or an error occurs.
   *
   * This method will clone (shallow copy) the original job and return it
   * with the result set. This detail is irrelevant as the parameter and result
   * is serialised for the RMI call. 
   *
   * @param job The job description
   * @return A job containing the original job, and results.
   */
  public JobResult submitJob(Job job) throws RemoteException;

  public void shutdown() throws RemoteException;
} 