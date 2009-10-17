//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.distributed
//# CLASS:   DistributedModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.distributed;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.analysis.distributed.application.
  DistributedNode;
import net.sourceforge.waters.analysis.distributed.application.
  DistributedServer;
import net.sourceforge.waters.analysis.distributed.application.Node;
import net.sourceforge.waters.analysis.distributed.application.Server;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * Factory to create distributed model verifiers.
 *
 * @author Sam Douglas
 */

public class DistributedModelVerifierFactory
  extends AbstractModelVerifierFactory
{
  //####################################################################
  // The current implementation makes a cast to
  // DistributedSafetyVerifier when processing command line arguments. 
  // An interface should be created for any distributed model verifiers
  // that allow the host and port parameters to be specified.
  //####################################################################


  //#########################################################################
  //# Constructors
  private DistributedModelVerifierFactory()
  {
  }

  private DistributedModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(mHostArgument);
    addArgument(mPortArgument);
    addArgument(mNodeCountArgument);
    addArgument(new ResultsDumpArgument());
    addArgument(new ShutdownFlagArgument());
    addArgument(new WalltimeArgument());
    addArgument(new StateDistributionArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public DistributedControllabilityChecker 
  createControllabilityChecker(final ProductDESProxyFactory factory)
  {
    return new DistributedControllabilityChecker(factory);
  }

  public DistributedLanguageInclusionChecker
  createLanguageInclusionChecker(final ProductDESProxyFactory factory)
  {
    return new DistributedLanguageInclusionChecker(factory);
  }

  public List<String> configure(final ModelVerifier verifier)
  {
    final List<String> result = super.configure(verifier);
    launchLocalServers();
    return result;
  }


  //#########################################################################
  //# Factory Instantiation
  public static DistributedModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new DistributedModelVerifierFactory();
    }
    return theInstance;
  }

  public static DistributedModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new DistributedModelVerifierFactory(cmdline);
  }


  //#########################################################################
  //# Launching Local Servers
  private void launchLocalServers()
  {
    try {
      final String hostname = mHostArgument.getHostName(); 
      final int port = mPortArgument.getPort();
      if (hostname.equals("localhost")) {
	final String name = DistributedServer.DEFAULT_SERVICE_NAME;
	final Registry registry = LocateRegistry.createRegistry(port);
	final Server server = new DistributedServer();
	final Server stub =
	  (Server) UnicastRemoteObject.exportObject(server, 0);
	registry.bind(name, stub);
	final int numnodes = mNodeCountArgument.getNodeCount();
	for (int i = 0; i < numnodes; i++) {
	  final DistributedNode node =
	    new DistributedNode(hostname, port, name);
	  node.start();
	}
      }
    } catch (final AlreadyBoundException exception) {
      // Server already running - no problem ...
    } catch (final RemoteException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Command Line Arguments
  /**
   * Process the host-name command line argument. This
   * argument is required for the distributed checkers.
   */
  private static class HostArgument extends CommandLineArgumentString
  {
    private HostArgument()
    {
      super("-host",
	    "Server to submit job to", true);
    }

    private String getHostName()
    {
      return getValue();
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) verifier;
      String value = getValue();
      dsv.setHostname(value);
    }
  }


  private static class ResultsDumpArgument extends CommandLineArgumentString
  {
    private ResultsDumpArgument()
    {
      super("-resultsdump",
	    "File to dump job result into");
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = (DistributedSafetyVerifier)verifier;
      String value = getValue();
      dsv.setResultsDumpFile(new File(value));
    }
  }

  
  private static class PortArgument extends CommandLineArgumentInteger
  {
    private PortArgument()
    {
      super("-port",
	    "Port to connect to the server with",
	    DistributedServer.DEFAULT_PORT);
    }

    private int getPort()
    {
      return getValue();
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) verifier;
      int value = getValue();
      dsv.setPort(value);
    }
  }

  
  private static class NodeCountArgument extends CommandLineArgumentInteger
  {
    private NodeCountArgument()
    {
      super("-nodes",
	    "Preferred number of nodes for the job");
    }

    private int getNodeCount()
    {
      return getValue();
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) verifier;
      int value = getValue();
      dsv.setNodeCount(value);
    }
  }

  
  private static class ShutdownFlagArgument extends CommandLineArgumentFlag
  {
    private ShutdownFlagArgument()
    {
      super("-shutdown",
	    "Shut down the distributed checker after verification");
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) verifier;
      dsv.setShutdownAfter(true);
    }
  }

  
  private static class WalltimeArgument extends CommandLineArgumentInteger
  {
    private WalltimeArgument()
    {
      super("-walltime",
	    "Sets the time limit for the job.");
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) verifier;
      dsv.setWalltimeLimit(getValue());
    }
  }


  private static class ProcessingThreadCount extends CommandLineArgumentInteger
  {
    private ProcessingThreadCount()
    {
      super("-procthreads",
	    "Number of processing threads to run");
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) verifier;
      dsv.setProcessingThreadCount(getValue());
    }
  }


  private static class StateDistributionArgument
    extends CommandLineArgumentString
  {
    private StateDistributionArgument()
    {
      super("-statedist",
	    "State distribution method to use");
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) verifier;
      dsv.setStateDistribution(getValue());
    }
  }


  //#########################################################################
  //# Data Members
  private final NodeCountArgument mNodeCountArgument = new NodeCountArgument();
  private final HostArgument mHostArgument = new HostArgument();
  private final PortArgument mPortArgument = new PortArgument();


  //#########################################################################
  //# Class Variables
  private static DistributedModelVerifierFactory theInstance = null;

}