package net.sourceforge.waters.analysis.distributed;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DistributedAnalysisServer 
  implements AnalysisServer
{
  public DistributedAnalysisServer()
  {
    super();
  }

  public String hello()
  {
    return "Hello world!";
  }

  public void registerNode(AnalysisNode node) 
  {
    try
      {
	System.err.format("Welcome to the family, %s.\n %s", node, node.nodeHello());
      }
    catch (Exception e)
      {
	System.err.println("Register node exception:");
	e.printStackTrace();
      }
  }

  public static void main(String[] args)
  { 
    String name = DEFAULT_SERVICE_NAME;

    try
      {
	AnalysisServer server = new DistributedAnalysisServer();
	AnalysisServer stub = 
	  (AnalysisServer) UnicastRemoteObject.exportObject(server, 0);
	Registry registry = LocateRegistry.createRegistry(23232);
	registry.rebind(DEFAULT_SERVICE_NAME, stub);
      }
    catch (Exception e)
      {
	System.err.println("Analysis server exception:");
	e.printStackTrace();
      }
  }

  public final static String DEFAULT_SERVICE_NAME = "waters-analysis-server";
}