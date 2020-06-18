//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sourceforge.waters.analysis.distributed.application.DistributedNode;
import net.sourceforge.waters.analysis.distributed.application.DistributedServer;
import net.sourceforge.waters.analysis.distributed.application.JobResult;
import net.sourceforge.waters.analysis.distributed.application.Server;
import net.sourceforge.waters.analysis.distributed.safetyverifier.SafetyVerificationJob;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.StringOption;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.SerializableKindTranslator;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESEqualityVisitor;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;


/**
 * Main entry point to distributed safety verifier implementation.
 *
 * @author Sam Douglas
 */
public class DistributedSafetyVerifier
  extends AbstractSafetyVerifier
  implements SafetyVerifier
{
  public DistributedSafetyVerifier(final KindTranslator translator,
                                   final SafetyDiagnostics diag,
                                   final ProductDESProxyFactory factory)
  {
    this(null, translator, diag, factory);
  }


  public DistributedSafetyVerifier(final ProductDESProxy model,
                                   final KindTranslator translator,
                                   final SafetyDiagnostics diag,
                                   final ProductDESProxyFactory factory)
  {
    super(model, translator, diag, factory);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    //These arguments should be mandatory.
    assert(getHostname() != null);
    assert(getPort() > 0);
    final String controller = "net.sourceforge.waters.analysis.distributed.safetyverifier.SafetyVerifierController";
    final ProductDESProxy model = getModel();
    final ProductDESProxyFactory factory = getFactory();
    final KindTranslator translator = getKindTranslator();
    try {
	final Server server = connectToServer(getHostname(), getPort());
	final SafetyVerificationJob job = new SafetyVerificationJob();
	job.setName("safety-" + UUID.randomUUID().toString());
	job.setController(controller);
	job.setNodeCount(getNodeCount());
	job.setModel(model);
	job.setProcessingThreadCount(getProcessingThreadCount());

	//The controller shouldn't care if there isn't a
	//walltime limit.
	if (getWalltimeLimit() >= 0)
	  {
	    job.setWalltimeLimit(getWalltimeLimit());
	  }

	//Set the kind translator for the job by serialising
	//the current translator, regardless of the type.
	final SerializableKindTranslator kx = new SerializableKindTranslator(translator, model);
	job.setKindTranslator(kx);

	job.setStateDistribution(getStateDistribution());

	//Submit the job to the server and wait for it to run.
	//Interpret the results as a verification job result.
	final VerificationJobResult result = new VerificationJobResult(server.submitJob(job));

	if (getShutdownAfter())
	  {
	    //This ping probes if the server is alive. This should give some indication
	    //of whether the shutdown will happen cleanly.
	    try
	      {
		server.ping();
	      }
	    catch (final RemoteException e)
	      {
		System.err.println("Ping to server failed. Chances of shutting down cleanly are slim");
	      }

	    try
	      {
		server.shutdown();
	      }
	    catch (final RemoteException e)
	      {
		//Ignore. Shutdown calls generally appear to fail because
		//the remote shutdown methods do a System.exit
		//It is possible the server isn't available though.
	      }
	  }


	try
	  {
	    dumpJobResult(result);
	  }
	catch (final IOException e)
	  {
	    //Chain an analysis exception...
	    throw new AnalysisException(e.getMessage(), e);
	  }

	//Check for exceptions that might have occurred
	if (result.getException() != null)
	  throw new AnalysisException(result.getException());


	final Boolean b = result.getResult();
      if (b == null) {
        throw new AnalysisException("Verification result was undefined!");
      } else if (b == true) {
        return setSatisfiedResult();
      } else {
        //Get a counterexample from the job result
        EventProxy[] trace = null;
        SafetyCounterExampleProxy counterexample = null;
        if (result.getTrace() != null) {
          trace = result.getTrace();
          //'Sanitise' the trace. This ensures the EventProxy
          //objects that are in the trace are the same objects
          //as the ones in the original model, as is expected
          //for Waters verifiers.
          final List<EventProxy> tracelist = sanitiseTrace(trace);
          counterexample =
            factory.createSafetyCounterExampleProxy(model, tracelist);
        }
        return setFailedResult(counterexample);
      }

      }
    catch (final Exception e)
      {
	throw new AnalysisException(e);
      }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }

  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, DistributedModelVerifierFactory.
                OPTION_DistributedModelVerifierFactory_Host);
    db.append(options, DistributedModelVerifierFactory.
              OPTION_DistributedModelVerifierFactory_Port);
    db.append(options, DistributedModelVerifierFactory.
              OPTION_DistributedModelVerifierFactory_NodeCount);
    db.append(options, DistributedModelVerifierFactory.
              OPTION_DistributedModelVerifierFactory_ResultsDump);
    db.append(options, DistributedModelVerifierFactory.
              OPTION_DistributedModelVerifierFactory_Shutdown);
    db.append(options, DistributedModelVerifierFactory.
              OPTION_DistributedModelVerifierFactory_Walltime);
    db.append(options, DistributedModelVerifierFactory.
              OPTION_DistributedModelVerifierFactory_StateDistribution);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(DistributedModelVerifierFactory.
                     OPTION_DistributedModelVerifierFactory_Host)) {
      final StringOption opt = (StringOption) option;
      setHostname(opt.getValue());
    } else if (option.hasID(DistributedModelVerifierFactory.
                            OPTION_DistributedModelVerifierFactory_Port)) {
      final PositiveIntOption opt = (PositiveIntOption) option;
      setPort(opt.getValue());
    } else if (option.hasID(DistributedModelVerifierFactory.
                            OPTION_DistributedModelVerifierFactory_NodeCount)) {
      final PositiveIntOption opt = (PositiveIntOption) option;
      setNodeCount(opt.getValue());
    } else if (option.hasID(DistributedModelVerifierFactory.
                            OPTION_DistributedModelVerifierFactory_ResultsDump)) {
      final FileOption opt = (FileOption) option;
      setResultsDumpFile(opt.getValue());
    } else if (option.hasID(DistributedModelVerifierFactory.
                            OPTION_DistributedModelVerifierFactory_Shutdown)) {
      setShutdownAfter(true);
    } else if (option.hasID(DistributedModelVerifierFactory.
                            OPTION_DistributedModelVerifierFactory_Walltime)) {
      final PositiveIntOption opt = (PositiveIntOption) option;
      setWalltimeLimit(opt.getValue());
    } else if (option.hasID(DistributedModelVerifierFactory.
                            OPTION_DistributedModelVerifierFactory_StateDistribution)) {
      final StringOption opt = (StringOption) option;
      setStateDistribution(opt.getValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private List<EventProxy> sanitiseTrace(final EventProxy[] trace) throws AnalysisException
  {
    final List<EventProxy> nt = new ArrayList<EventProxy>();

    for (final EventProxy ev : trace)
      {
	nt.add(sanitiseEvent(ev));
      }

    return nt;
  }


  /**
   * Returns the event proxy instance from the actual model that is
   * equal by contents to the supplied event proxy.
   * @param event event to sanitise
   * @return event considered equal from the model
   * @throws AnalysisException if no events were equal
   */
  private EventProxy sanitiseEvent(final EventProxy event) throws AnalysisException
  {
    final ProductDESEqualityVisitor eq =
      ProductDESEqualityVisitor.getInstance();
    for (final EventProxy ev : getModel().getEvents()) {
      if (eq.equals(ev, event)) {
        return ev;
      }
    }
    throw new AnalysisException("EventProxy could not be sanitised: not in model?");
  }

  private Server connectToServer(final String host, final int port) throws Exception
  {
    //This should probably be replaced with a common constants
    //class or something. For now this will do
    final String service = net.sourceforge.waters.analysis.distributed.application.DistributedServer.DEFAULT_SERVICE_NAME;

    final Registry registry = LocateRegistry.getRegistry(host, port);
    final Server server = (Server) registry.lookup(service);

    return server;
  }


  private void dumpJobResult(final JobResult result) throws IOException
  {
    final File dumpfile = getResultsDumpFile();
    if (dumpfile != null)
      {
	ObjectOutputStream oos = null;
	FileOutputStream fos = null;
	try
	  {
	    fos = new FileOutputStream(dumpfile);
	    oos = new ObjectOutputStream(fos);
	    oos.writeObject(result);
	    oos.close();
	    fos.close();
	  }
	catch (final IOException e)
	  {
	    if (oos != null)
	      oos.close();

	    if (fos != null)
	      fos.close();

	    throw e;
	  }
      }
  }

  //#########################################################################
  //# Launching Local Servers
  void launchLocalServers()
  {
    try {
      final String hostname = mHostname;
      final int port = mPort;
      if (hostname.equals("localhost")) {
    final String name = DistributedServer.DEFAULT_SERVICE_NAME;
    final Registry registry = LocateRegistry.createRegistry(port);
    final Server server = new DistributedServer();
    final Server stub =
      (Server) UnicastRemoteObject.exportObject(server, 0);
    registry.bind(name, stub);
    final int numnodes = mNodeCount;
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
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  @Override
  public void setKindTranslator(final KindTranslator translator)
  {
    super.setKindTranslator(translator);
    clearAnalysisResult();
  }

  @Override
  public SafetyCounterExampleProxy getCounterExample()
  {
    return super.getCounterExample();
  }


  //########################################################################
  //# Option methods
  public void setHostname(final String hostname)
  {
    mHostname = hostname;
  }

  public void setPort(final int port)
  {
    mPort = port;
  }

  public String getHostname()
  {
    return mHostname;
  }

  public int getPort()
  {
    return mPort;
  }

  public void setResultsDumpFile(final File dumpFile)
  {
    mDumpFile = dumpFile;
  }

  public File getResultsDumpFile()
  {
    return mDumpFile;
  }

  public void setNodeCount(final int count)
  {
    mNodeCount = count;
  }

  public int getNodeCount()
  {
    return mNodeCount;
  }

  public void setShutdownAfter(final boolean value)
  {
    mShutdownAfter = value;
  }

  public boolean getShutdownAfter()
  {
    return mShutdownAfter;
  }

  public void setWalltimeLimit(final int seconds)
  {
    mWalltimeLimit = seconds;
  }

  public int getWalltimeLimit()
  {
    return mWalltimeLimit;
  }

  public void setProcessingThreadCount(final int threads)
  {
    mProcessingThreads = threads;
  }

  public int getProcessingThreadCount()
  {
    return mProcessingThreads;
  }

  public void setStateDistribution(final String dist)
  {
    mStateDistribution = dist;
  }

  public String getStateDistribution()
  {
    return mStateDistribution;
  }

  private String mHostname = null;
  private int mPort = 23232;
  private int mNodeCount = 10;
  private File mDumpFile = null;
  private boolean mShutdownAfter = false;
  private int mWalltimeLimit = -1;
  private int mProcessingThreads = 2;
  private String mStateDistribution = "hash";
}
