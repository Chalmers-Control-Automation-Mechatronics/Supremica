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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ShutdownApplication
{
  public static void main(String[] args)
  {
    try
      {
	if (args.length == 0)
	  {
	    System.err.println("Usage: ShutdownApplication HOST [PORT]");
	    System.exit(1);
	  }

	String host = args[0];
	int port = 23232;

	if (args.length > 1) 
	  port = Integer.parseInt(args[1]);

	String service = DistributedServer.DEFAULT_SERVICE_NAME;

	Registry registry = LocateRegistry.getRegistry(host, port);
	Server server = (Server) registry.lookup(service);
	
	//Test the water. Assume that if ping succeeds, then 
	//the following shutdown will work and we can ignore
	//any RemoteExceptions that occur. It would be nicer to
	//start a remote shutdown that waits for stuff to finish.
	try
	  {
	    server.ping();
	  }
	catch (Exception e)
	  {
	    System.err.println("Server ping failed. Shutdown probably won't work");
	    e.printStackTrace();
	  }

	try
	  {
	    server.shutdown();
	  }
	catch (Exception e)
	  {
	    //Do nothing
	  }

      }
    catch (Exception e)
      {
	System.err.println("Shutdown Client exception:" + e);
	e.printStackTrace();
      }
  }
}
