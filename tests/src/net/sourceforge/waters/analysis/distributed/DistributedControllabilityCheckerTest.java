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

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.waters.analysis.distributed.application.DistributedNode;
import net.sourceforge.waters.analysis.distributed.application.DistributedServer;
import net.sourceforge.waters.analysis.distributed.application.Server;
import net.sourceforge.waters.model.analysis.AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
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







