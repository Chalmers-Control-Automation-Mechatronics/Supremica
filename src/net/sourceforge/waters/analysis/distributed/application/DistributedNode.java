package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DistributedNode
  implements Node
{
  public DistributedNode(String host, int port, String service)
  {
    super();
    mServerHost = host;
    mServerPort = port;
    mServerService = service;
  }

  /**
   * Export this node as a remote object.
   */
  public void start() throws RemoteException
  {
    mNodeProxy = (Node) UnicastRemoteObject.exportObject(this, 0);
    Thread t = new ConnectionThread();
    t.setDaemon(true);
    t.start();
  }

  private class ConnectionThread extends Thread
  {
    public void run()
    {
      int failcount = 0;

      while (true)
	{
	  assert(mNodeProxy != null);
	  
	  //Need to get a server proxy object.
	  if (mServerProxy == null)
	    {
	      try
		{
		  System.out.format("Attempting to get server proxy\n");
		  mServerProxy = getServerProxy();
		  mServerProxy.registerNode(mNodeProxy);
		}
	      catch (Exception e)
		{
		  mServerProxy = null;
		}
	    }

	  //Try to ping the server
	  if (mServerProxy != null)
	    {
	      try
		{
		  System.out.format("Pinging server\n");
		  mServerProxy.ping();
		}
	      catch (RemoteException e)
		{
		  //Ping failed.
		  System.out.format("Ping failed!\n");
		  mServerProxy = null;
		}
	    }

	  //Wait a bit
	  dosleep(2000);
	  
	}
    }

    private void dosleep(long millis)
    {
      try
	{
	  Thread.sleep(millis);
	}
      catch (InterruptedException e)
	{
	}
    }
  }

  private Server getServerProxy() throws Exception
  {
    Registry registry = LocateRegistry.getRegistry(mServerHost, mServerPort);
    Server server = (Server) registry.lookup(mServerService);
    return server;
  }

  public void ping()
  {
    return;
  }
						
  private final String mServerHost;
  private final int mServerPort;
  private final String mServerService;
  private Server mServerProxy = null;
  private Node mNodeProxy = null;

  //########################Main code###########################

  public static void main(String[] args)
  {
    try
      {
	
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

	DistributedNode node = new DistributedNode(host, port, service);
	node.start();

      }
    catch (Exception e)
      {
	System.err.println("Node exception:");
	e.printStackTrace();
      }
    
  }
}