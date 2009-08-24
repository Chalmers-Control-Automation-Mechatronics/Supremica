package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ShutdownApplication
{
  public static void main(String[] args)
  {
    try
      {
	if (args.length == 0)
	  {
	    System.err.println("Usage: ShutdownApplication HOST [PORT]");
	    System.exit(1);
	  }

	String host = args[0];
	int port = 23232;

	if (args.length > 1) 
	  port = Integer.parseInt(args[1]);

	String service = DistributedServer.DEFAULT_SERVICE_NAME;

	Registry registry = LocateRegistry.getRegistry(host, port);
	Server server = (Server) registry.lookup(service);
	
	//Test the water. Assume that if ping succeeds, then 
	//the following shutdown will work and we can ignore
	//any RemoteExceptions that occur. It would be nicer to
	//start a remote shutdown that waits for stuff to finish.
	try
	  {
	    server.ping();
	  }
	catch (Exception e)
	  {
	    System.err.println("Server ping failed. Shutdown probably won't work");
	    e.printStackTrace();
	  }

	try
	  {
	    server.shutdown();
	  }
	catch (Exception e)
	  {
	    //Do nothing
	  }

      }
    catch (Exception e)
      {
	System.err.println("Shutdown Client exception:" + e);
	e.printStackTrace();
      }
  }
}