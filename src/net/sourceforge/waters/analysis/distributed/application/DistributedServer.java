package net.sourceforge.waters.analysis.distributed.application;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.List;
import java.util.ArrayList;

public class DistributedServer 
  implements Server
{
  public DistributedServer()
  {
    super();
    mNodes = new ArrayList<Node>();
  }

  public String hello()
  {
    return "Hello world!";
  }

  public void registerNode(Node node) 
  {
    mNodes.add(node);
  }

  /**
   * Create an instance of the named controller class.
   * This must be a valid Controller. It must have a 
   * constructor that takes no arguments.
   * @param name the full name (package + class) to create.
   * @return new instance of the controller.
   * @throws ClassNotFoundException if the class loader could not
   *                                find the class, or if it is not
   *                                a Controller.
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
      throw new ClassNotFoundException
	(name + " is not a valid Controller class");

    Controller controller = (Controller) c.newInstance();
    return controller;
  }

  public Job submitJob(Job job)
  {
    String controller_name = (String)job.getAttribute("controller");

    System.out.println ("Processing job "+ job.getAttribute("name"));
    System.out.format ("Using controller: %s\n",
		       controller_name);
    
    //Create a controller for the job.
    Controller control = null;
    try
      {
	control = createController(controller_name);
      }
    catch (Exception e)
      {
	job.setAttribute("exception", e);
	return job;
      }

    assert(control != null);
    
    control.run();

    //This isn't really right, but works for testing
    //the controller.
    job.setAttribute
      ("result", 
       control.getState() == ControllerState.COMPLETED);

    if (control.getState() == ControllerState.EXCEPTION)
      job.setAttribute("exception", control.getException());
    
    return job;
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