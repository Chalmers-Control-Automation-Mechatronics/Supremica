package net.sourceforge.waters.analysis.distributed;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AnalysisNode extends Remote
{
  public String nodeHello() throws RemoteException;
}