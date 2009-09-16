package net.sourceforge.waters.analysis.distributed.application;


import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import java.rmi.RemoteException;

/**
 * A test controller implementation.
 */
public class TestController extends AbstractController
{
  public TestController()
  {
  }

  protected void executeController() throws Exception
  {
    Collection<Node> nodes = getNodes();
    Job job = getJob();

    if (nodes == null)
      throw new IllegalStateException("No nodes were given");

    if (job == null)
      throw new IllegalStateException("No job was given");

    
    Node[] nodesArray = new Node[nodes.size()];
    Worker[] workers = new Worker[nodes.size()];
    

    int i = 0;
    for (Node n : nodes)
      {
	try
	  {
	    Worker w = n.createWorker(getControllerID(), "net.sourceforge.waters.analysis.distributed.application.TestWorker", null);
	    nodesArray[i] = n;
	    workers[i] = w;
	    i++;
	  }
	catch (Exception e)
	  {
	    //Assume workers will be cleaned up automatically.
	    throw e;
	  }
      }
  }
}