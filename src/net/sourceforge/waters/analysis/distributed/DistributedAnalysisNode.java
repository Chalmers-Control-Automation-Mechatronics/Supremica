package net.sourceforge.waters.analysis.distributed;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DistributedAnalysisNode
  implements AnalysisNode
{
  public DistributedAnalysisNode()
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
	AnalysisNode realNode = new DistributedAnalysisNode();
	AnalysisNode node = 
	  (AnalysisNode) UnicastRemoteObject.exportObject(realNode, 0);

	String host = args[0];
	int port = Integer.parseInt(args[1]);
	String service = DistributedAnalysisServer.DEFAULT_SERVICE_NAME;
	if (args.length > 2)
	  service = args[2];

	Registry registry = LocateRegistry.getRegistry(host, port);
	AnalysisServer server = (AnalysisServer) registry.lookup(service);

	server.registerNode(node);

      }
    catch (Exception e)
      {
	System.err.println("Analysis node exception:");
	e.printStackTrace();
      }
    
  }
}