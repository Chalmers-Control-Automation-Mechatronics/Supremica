package net.sourceforge.waters.analysis.distributed;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AnalysisServer extends Remote
{
  public String hello() throws RemoteException;
  public void registerNode(AnalysisNode node) throws RemoteException;
} 