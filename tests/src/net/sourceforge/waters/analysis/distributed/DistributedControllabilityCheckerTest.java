//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.distributed
//# CLASS:   DistributedControllabilityCheckerTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.distributed;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.distributed.application.
  DistributedNode;
import net.sourceforge.waters.analysis.distributed.application.
  DistributedServer;
import net.sourceforge.waters.analysis.distributed.application.Node;
import net.sourceforge.waters.analysis.distributed.application.Server;
import net.sourceforge.waters.model.analysis.
  AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class DistributedControllabilityCheckerTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    TestSuite testSuite =
      new TestSuite(DistributedControllabilityCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ControllabilityChecker
    createModelVerifier(final ProductDESProxyFactory factory)
  {
    final DistributedControllabilityChecker checker =
      new DistributedControllabilityChecker(factory);
    checker.setHostname(HOSTNAME);
    return checker;
  }


  //#########################################################################
  //# Class Constants
  private static final String HOSTNAME = "localhost";
  private static final int NUM_NODES = 3;

  static {
    try {
      final String name = DistributedServer.DEFAULT_SERVICE_NAME;
      final int port = DistributedServer.DEFAULT_PORT;
      final Registry registry = LocateRegistry.createRegistry(port);
      final Server server = new DistributedServer();
      final Server stub = (Server) UnicastRemoteObject.exportObject(server, 0);
      registry.bind(name, stub);
      for (int i = 0; i < NUM_NODES; i++) {
        final DistributedNode node = new DistributedNode(HOSTNAME, port, name);
        node.start();
      }
    } catch (final AlreadyBoundException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final RemoteException exception) {
      throw new WatersRuntimeException(exception);
    }
  }
  
}