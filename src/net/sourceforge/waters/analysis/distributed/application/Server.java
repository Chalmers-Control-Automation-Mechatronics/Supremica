package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote
{
  public String hello() throws RemoteException;
  public void registerNode(Node node) throws RemoteException;

  /**
   * Submit a job for processing. This remote method will not return
   * until the job is finished, or an error occurs.
   *
   * This method will return a job based on the original job object,
   * but because it is serialised (and jobs are not remote objects) 
   * it will not necessarily be the same job object as the input.
   *
   * @param job The job description
   * @return A job containing the original job, and results.
   */
  public Job submitJob(Job job) throws RemoteException;
} 