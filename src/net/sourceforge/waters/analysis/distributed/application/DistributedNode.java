package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DistributedNode
  implements Node
{
  public DistributedNode()
  {
    super();
  }

  public String nodeHello()
  {
    return "Hello from Mr. Node";
  }

  public static void main(String[] args)
  {
    try
      {
	//Create an analysis node object.
	Node realNode = new DistributedNode();
	Node node = 
	  (Node) UnicastRemoteObject.exportObject(realNode, 0);

	if (args.length < 1)
	  {
	    System.err.format("DistributedNode: No server host specified\n");
	    System.exit(1);
	  }

	String host = args[0];

	int port = DistributedServer.DEFAULT_PORT;
	if (args.length > 1)
	  port = Integer.parseInt(args[1]);

	String service = DistributedServer.DEFAULT_SERVICE_NAME;
	if (args.length > 2)
	  service = args[2];

	Registry registry = LocateRegistry.getRegistry(host, port);
	Server server = (Server) registry.lookup(service);

	server.registerNode(node);

      }
    catch (Exception e)
      {
	System.err.println("Node exception:");
	e.printStackTrace();
      }
    
  }
}