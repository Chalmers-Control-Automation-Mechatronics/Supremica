package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.List;
import java.util.ArrayList;

public class DistributedServer 
  implements Server
{
  public DistributedServer()
  {
    super();
    mNodes = new ArrayList<Node>();
  }

  public String hello()
  {
    return "Hello world!";
  }

  public void registerNode(Node node) 
  {
    mNodes.add(node);
  }

  public Job submitJob(Job job)
  {
    System.out.println ("Processing job "+ job.getAttribute("name"));
    job.setAttribute("result", true);
    return job;
  }
  
  private final List<Node> mNodes;
  

  //#########################################################################
  //# Main code
  public static void main(String[] args)
  { 
    String name = DEFAULT_SERVICE_NAME;

    try
      {
	Server server = new DistributedServer();
	Server stub = 
	  (Server) UnicastRemoteObject.exportObject(server, 0);
	Registry registry = LocateRegistry.createRegistry(23232);
	registry.rebind(DEFAULT_SERVICE_NAME, stub);
      }
    catch (Exception e)
      {
	System.err.println("Server exception:");
	e.printStackTrace();
      }
  }

  public final static String DEFAULT_SERVICE_NAME = "waters-analysis-server";
}