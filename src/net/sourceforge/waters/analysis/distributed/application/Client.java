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

	String controller = args[2];
	  

	Registry registry = LocateRegistry.getRegistry(host, port);
	Server server = (Server) registry.lookup(service);
	
	Job job = new Job();
	job.setName("test-job");
	job.setController(controller);

	if (args.length > 3)
	  {
	    Integer nodes = Integer.parseInt(args[3]);
	    job.setNodeCount(nodes);
	  }

	JobResult result = server.submitJob(job);

	if (result.getException() != null)
	  System.out.format("Job exception: %s\n", result.getException());

      }
    catch (Exception e)
      {
	System.err.println("Client exception:" + e);
	e.printStackTrace();
      }
  }
}