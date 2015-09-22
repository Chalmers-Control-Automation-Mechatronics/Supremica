//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.distributed;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import net.sourceforge.waters.analysis.distributed.application.DistributedNode;
import net.sourceforge.waters.analysis.distributed.application.DistributedServer;
import net.sourceforge.waters.analysis.distributed.application.Server;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * Factory to create distributed model verifiers.
 *
 * @author Sam Douglas
 */

public class DistributedModelVerifierFactory
  extends AbstractModelAnalyzerFactory
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


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory
  @Override
  protected void addArguments()
  {
    super.addArguments();
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
  @Override
  public DistributedControllabilityChecker
  createControllabilityChecker(final ProductDESProxyFactory factory)
  {
    return new DistributedControllabilityChecker(factory);
  }

  @Override
  public DistributedLanguageInclusionChecker
  createLanguageInclusionChecker(final ProductDESProxyFactory factory)
  {
    return new DistributedLanguageInclusionChecker(factory);
  }

  @Override
  public void configure(final ModelAnalyzer analyzer)
    throws AnalysisConfigurationException
  {
    super.configure(analyzer);
    launchLocalServers();
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

    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) analyzer;
      final String value = getValue();
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

    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) analyzer;
      final String value = getValue();
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

    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) analyzer;
      final int value = getValue();
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

    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) analyzer;
      final int value = getValue();
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

    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) analyzer;
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

    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) analyzer;
      dsv.setWalltimeLimit(getValue());
    }
  }


  @SuppressWarnings("unused")
  private static class ProcessingThreadCount extends CommandLineArgumentInteger
  {
    private ProcessingThreadCount()
    {
      super("-procthreads",
	    "Number of processing threads to run");
    }

    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) analyzer;
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

    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final DistributedSafetyVerifier dsv = (DistributedSafetyVerifier) analyzer;
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







