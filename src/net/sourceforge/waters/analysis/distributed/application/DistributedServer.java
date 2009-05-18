package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


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
    Class c = Class.forName(name);

    //The loaded class /must/ implement the Controller 
    //interface.
    if (!Controller.class.isAssignableFrom(c))
      throw new ClassCastException
	(name + " is not a valid Controller class");

    Controller controller = (Controller) c.newInstance();
    return controller;
  }


  public Job submitJob(Job job)
  {
    Job result = job.clone();

    try
      {
	if (!job.containsAttribute("controller"))
	  throw new IllegalArgumentException("Job does not contain a controller attribute");

	String controller_name = (String)job.getAttribute("controller");
	
	System.out.println ("Processing job "+ job.getAttribute("name"));
	System.out.format ("Using controller: %s\n",
			   controller_name);
	
	//Create a controller for the job.
	Controller control = createController(controller_name);
	
	control.run();
	
	if (control.getState() == ControllerState.COMPLETED)
	  {
	    result.setComplete();
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
      }
	
    return result;
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
  
  private final List<Node> mNodes;
  

  //#########################################################################
  //# Main code
  public static void main(String[] args)
  { 
    String name = DEFAULT_SERVICE_NAME;

    try
      {
	Server server = new DistributedServer();
	Server stub = 
	  (Server) UnicastRemoteObject.exportObject(server, 23232);
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