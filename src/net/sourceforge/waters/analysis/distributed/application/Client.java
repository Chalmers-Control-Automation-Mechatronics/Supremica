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

	if (args.length > 3)
	  service = args[3];

	Registry registry = LocateRegistry.getRegistry(host, port);
	Server server = (Server) registry.lookup(service);
	
	Job job = new Job();
	job.setAttribute("name", "test-job");
	job.setAttribute("controller", controller);

	Job result = server.submitJob(job);

	if (result.getJobStatus() == JobStatus.COMPLETE)
	  {
	    System.out.println("Job finished successfully!");
	  }
	else if (result.getJobStatus() == JobStatus.EXCEPTION)
	  {
	    System.out.format("Job exception: %s\n", result.getException());
	  }

      }
    catch (Exception e)
      {
	System.err.println("Client exception:" + e);
	e.printStackTrace();
      }
  }
}