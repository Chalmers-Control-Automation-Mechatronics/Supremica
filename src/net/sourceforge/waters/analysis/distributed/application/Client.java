package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client
{
  public static void main(String[] args)
  {
    try
      {
	String host = args[0];
	int port = Integer.parseInt(args[1]);
	String service = DistributedServer.DEFAULT_SERVICE_NAME;

	if (args.length > 2)
	  service = args[2];

	Registry registry = LocateRegistry.getRegistry(host, port);
	Server server = (Server) registry.lookup(service);
	
	
	Job job = new Job();
	job.setAttribute("name", "test-job");

	Job result = server.submitJob(job);

	System.out.format("Result: %s\n", result.getAttribute("result"));

      }
    catch (Exception e)
      {
	System.err.println("Client exception:" + e);
	e.printStackTrace();
      }
  }
}