package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote
{
  public String nodeHello() throws RemoteException;
}