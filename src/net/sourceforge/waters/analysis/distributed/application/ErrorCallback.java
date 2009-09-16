package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface for providing remote error callbacks for 
 * workers.
 *
 * @author Sam Douglas
 */
public interface ErrorCallback extends Remote
{
  /**
   * Handle an error that occurs. This shouldn't really 
   * throw anything, except a remote exception. It's purpose
   * is to handle errors and exceptions... not make more!
   * @param workerid A string to uniquely identify the worker.
   * @param worker A remote proxy for the worker
   * @param throwable to handle. This is supposed to handle
   *        errors too.
   * @throws RemoteException if it is a remote callback and 
   *                         the RMI call fails.
   */
  public void handle(String workerid, Worker worker, Throwable throwable) throws RemoteException;
}