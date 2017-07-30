//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

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
    mWorkerCleanup = new WorkerCleanup();
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


  public Worker createWorker(ControllerID id, String classname, ErrorCallback cb) 
    throws 
    ClassNotFoundException,
    IllegalAccessException,
    IllegalArgumentException,
    InstantiationException,
    UnsupportedOperationException,
    RemoteException
  {
    if (id == null)
      throw new IllegalArgumentException("Controller ID cannot be null");

    Class<?> c = Class.forName(classname);

    if (!Worker.class.isAssignableFrom(c))
      throw new ClassCastException
	(classname + " is not a valid Worker class");

    Worker w = (Worker) c.newInstance();
    mWorkerCleanup.registerWorker(id, (WorkerLocal)w);

    //Export the worker as a remote object.
    Worker stub = (Worker) UnicastRemoteObject.exportObject(w, 0);

    //Set the remote proxy for the worker.
    try
      {
	((WorkerLocal)w).setWorkerProxy(stub);
	((WorkerLocal)w).setErrorCallback(cb);
      }
    catch (Exception e)
      {
	throw new UnsupportedOperationException("Could not set worker proxy or error callback", e);
      }

    //Call the create method on the worker. If something goes wrong, chain
    //an unsupported operation exception.
    try
      {
	((WorkerLocal)w).created();
      }
    catch (Exception e)
      {
	throw new UnsupportedOperationException("Could not run created() method on worker", e);
      }

    return stub;
  }

  public void cleanup(ControllerID id) throws RemoteException
  {
    System.err.format("Cleaning up for %s\n", id);
    mWorkerCleanup.cleanup(id);

    //Probably unnecessary with modern JVMs, but hint that now is a
    //good time to run the garbage collector.
    System.gc();
  }

  public void shutdown()
  {
    //No warning necessary. This should kill all non-daemon mode threads?
    System.err.println("Shutting down node");
    System.exit(0);
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
		  System.err.format("Attempting to get server proxy\n");
		  mServerProxy = getServerProxy();
		  mServerProxy.registerNode(mNodeProxy);
		  failcount = 0;
		}
	      catch (Exception e)
		{
		  mServerProxy = null;
		  failcount++;
		}
	    }

	  //Try to ping the server
	  if (mServerProxy != null)
	    {
	      try
		{
		  System.err.format("Pinging server\n");
		  mServerProxy.ping();
		  failcount = 0;
		}
	      catch (RemoteException e)
		{
		  //Ping failed.
		  System.err.format("Ping failed!\n");
		  mServerProxy = null;
		}
	    }

	  if (failcount > SUICIDE_FAIL_COUNT)
	    {
	      //We are the Judean People's Front crack
	      //suicide squad! Suicide squad, attack!
	      System.err.format("DistributedNode: server seems gone, exiting\n");
	      System.exit(1);
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


  /**
   * The number of times a ping fails before the node gives up and exits.
   * This should be made a command line option.
   */
  private static final int SUICIDE_FAIL_COUNT = 20; 
  private final WorkerCleanup mWorkerCleanup;
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
