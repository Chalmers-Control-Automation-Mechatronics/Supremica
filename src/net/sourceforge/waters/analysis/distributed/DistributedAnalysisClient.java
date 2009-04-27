package net.sourceforge.waters.analysis.distributed;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DistributedAnalysisClient
{
  public static void main(String[] args)
  {
    try
      {
	String host = args[0];
	int port = Integer.parseInt(args[1]);
	String service = DistributedAnalysisServer.DEFAULT_SERVICE_NAME;

	if (args.length > 2)
	  service = args[2];

	Registry registry = LocateRegistry.getRegistry(host, port);
	AnalysisServer server = (AnalysisServer) registry.lookup(service);
	
	for (int i = 0; i < 10; i++)
	  System.out.println(server.hello());
      }
    catch (Exception e)
      {
	System.err.println("Distributed analysis client exception:");
	e.printStackTrace();
      }
  }
}