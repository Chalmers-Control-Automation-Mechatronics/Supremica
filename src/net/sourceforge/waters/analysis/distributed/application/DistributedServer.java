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
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;


public class DistributedServer 
  implements Server
{
  public DistributedServer()
  {
    super();
    mNodes = new ArrayList<Node>();

    Thread pruner = new NodePruner();
    pruner.setDaemon(true);
    pruner.start();
  }

  public void ping()
  {
    //Pong
    return;
  }

  public void registerNode(Node node) 
  {
    System.out.format("Node %s connected", node);
    addNode(node);
  }

  private ControllerID createControllerID(Controller c)
  {
    return new ControllerID(java.util.UUID.randomUUID().toString());
  }

  private synchronized Collection<Node> selectNodes(int count)
  {
    Set<Node> nodes = new HashSet<Node>();

    //Duplicate and shuffle the list of nodes. This way the
    //distribution of work should be more even amongst nodes
    List<Node> shuffled = new ArrayList<Node>(mNodes);
    Collections.shuffle(shuffled);

    for (Node n : shuffled)
      {
	if (nodes.size() >= count)
	  break;

	//Attempt to ping the node. Avoid giving out 
	//nodes that do not respond to pings.
	try
	  {
	    n.ping();
	    nodes.add(n);
	  }
	catch (RemoteException e)
	  {
	    continue;
	  }
      }

    return nodes;
  }

  /**
   * Create an instance of the named controller class.
   * This must be a valid Controller. It must have a 
   * constructor that takes no arguments.
   * @param name the full name (package + class) to create.
   * @return new instance of the controller.
   * @throws ClassNotFoundException if the class loader could not
   *                                find the class.
   * @throws ClassCastException if the specified class does not implement
   *                                the {@link Controller} interface.
   * @throws IllegalAccessException if the class is not accessible.
   * @throws InstantiationException if the class cannot be instantiated.
   */
  private Controller createController(String name) 
    throws 
    ClassNotFoundException, 
    IllegalAccessException, 
    InstantiationException

  {
    Class<?> c = Class.forName(name);

    //The loaded class /must/ implement the Controller 
    //interface.
    if (!Controller.class.isAssignableFrom(c))
      throw new ClassCastException
	(name + " is not a valid Controller class");

    Controller controller = (Controller) c.newInstance();
    return controller;
  }


  public JobResult submitJob(Job job)
  {
    JobResult result = new JobResult();

    try
      {
	if (job.getController() == null)
	  throw new IllegalArgumentException("Job does not contain a controller attribute");

	String controller_name = job.getController();
	
	//If the job doesn't specify how many nodes it wants
	//then we just give out a default number
	int preferred_nodes = 10;
	if (job.getNodeCount() != null)
	  {
	    preferred_nodes = job.getNodeCount();
	  }

	System.out.format ("Using controller: %s\n",
			   controller_name);

	//Create a controller for the job.
	Controller control = createController(controller_name);
	
	Collection<Node> nodes = selectNodes(preferred_nodes);
	System.out.println ("Processing job "+ job.get("name"));		
	System.out.format("Preferred %d, and got %d nodes\n",
			  preferred_nodes, nodes.size());


	ControllerID id = createControllerID(control);
	control.setControllerID(id);
	control.setJob(job);
	control.setNodes(nodes);

	control.run();

	//Clean up after the controller.
	for (Node n : nodes)
	  {
	    try
	      {
		n.cleanup(id);
	      }
	    catch (Exception e)
	      {
		e.printStackTrace();
	      }
	  }

	result = control.getResult();
	if (result == null)
	  result = new JobResult();
	
	if (control.getState() == ControllerState.COMPLETED)
	  {
	    result = control.getResult();
	    return result;
	  }
	else if (control.getState() == ControllerState.EXCEPTION)
	  throw control.getException();
	else
	  throw new IllegalStateException("Controller ended in bad state!");
      }
    catch (Exception e)
      {
	result.setException(e);
	return result;
      }
  }

  private class NodePruner extends Thread
  {
    public void run()
    {
      while (true)
	{
	  try
	    {
	      Thread.sleep(10000);
	      pruneNodes();
	    }
	  catch (InterruptedException e)
	    {}
	}
    }
  }

  /**
   * Removes nodes that are no longer alive.
   */
  private synchronized void pruneNodes()
  {
    Iterator<Node> it = mNodes.iterator();
    while (it.hasNext())
      {
	Node n = it.next();
	try
	  {
	    n.ping();
	  }
	catch (RemoteException e)
	  {
	    System.err.format("Pruning node %s\n", n);
	    it.remove();
	  }
      }
  }

  private synchronized void addNode(Node n)
  {
    mNodes.add(n);
  }

  public synchronized void shutdown()
  {
    for (Node n : mNodes)
      {
	
	//Try a ping first... nodes that respond to pings should 
	//shutdown properly. Shutting down means the RMI call probably 
	//won't return happily, so this gives some idea of nodes that may
	//be borked.
	try
	  {
	    n.ping();
	  }
	catch (Exception e)
	  {
	    System.err.println("Node failed ping, proper shutdown seems unlikely."); 
	  }

	try
	  {
	    n.shutdown();
	  }
	catch (Exception e)
	  {
	    //Maybe a better way to shutdown servers is necessary.
	  }
      }

    System.exit(0);
  }

  private final List<Node> mNodes;


  //#########################################################################
  //# Main code
  public static void main(String[] args)
  { 
    try
      {
	Server server = new DistributedServer();
	Server stub = 
	  (Server) UnicastRemoteObject.exportObject(server, 0);
	Registry registry = LocateRegistry.createRegistry(DEFAULT_PORT);
	registry.rebind(DEFAULT_SERVICE_NAME, stub);
      }
    catch (Exception e)
      {
	System.err.println("Server exception:");
	e.printStackTrace();
      }
  }

  public static final String DEFAULT_SERVICE_NAME = "waters-analysis-server";
  public static final int DEFAULT_PORT = 23232;
}
