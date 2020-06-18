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

import net.sourceforge.waters.analysis.distributed.application.DistributedServer;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.FlagOption;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.StringOption;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
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
  public void registerOptions(final OptionPage db)
  {
    db.add(new StringOption
             (OPTION_DistributedModelVerifierFactory_Host,
              null,
              "Serer to submit job to",
              "-host", null));
    db.add(new PositiveIntOption
             (OPTION_DistributedModelVerifierFactory_Port,
              null,
              "Port to connect to the server with",
              "-port", DistributedServer.DEFAULT_PORT));
    db.add(new PositiveIntOption
             (OPTION_DistributedModelVerifierFactory_NodeCount,
              null,
              "Preferred number of nodes for the job",
              "-nodes"));
    db.add(new FileOption
             (OPTION_DistributedModelVerifierFactory_ResultsDump,
              null,
              "File to dump job result into",
              "-resultsdump"));
    db.add(new FlagOption
             (OPTION_DistributedModelVerifierFactory_Shutdown,
              null,
              "Shut down the distributed checker after verification",
              "-shutdown"));
    db.add(new PositiveIntOption
             (OPTION_DistributedModelVerifierFactory_Walltime,
              null,
              "Sets the time limit for the job",
              "-walltime"));
    db.add(new StringOption
             (OPTION_DistributedModelVerifierFactory_StateDistribution,
              null,
              "State distribution method to use",
              "-statedist", null));
  }

  @Override
  public void configure(final ModelAnalyzer analyzer)
    throws AnalysisConfigurationException
  {
    super.configure(analyzer);
    ((DistributedSafetyVerifier) analyzer).launchLocalServers();
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
  //# Data Members


  //#########################################################################
  //# Class Variables
  private static DistributedModelVerifierFactory theInstance = null;

  public static final String OPTION_DistributedModelVerifierFactory_Host =
    "DistributedModelVerifierFactory.Host";
  public static final String OPTION_DistributedModelVerifierFactory_Port =
    "DistributedModelVerifierFactory.Port";
  public static final String OPTION_DistributedModelVerifierFactory_NodeCount =
    "DistributedModelVerifierFactory.NodeCount";
  public static final String OPTION_DistributedModelVerifierFactory_ResultsDump =
    "DistributedModelVerifierFactory.ResultsDump";
  public static final String OPTION_DistributedModelVerifierFactory_Shutdown =
    "DistributedModelVerifierFactory.Shutdown";
  public static final String OPTION_DistributedModelVerifierFactory_Walltime =
    "DistributedModelVerifierFactory.Walltime";
  public static final String OPTION_DistributedModelVerifierFactory_StateDistribution =
    "DistributedModelVerifierFactory.StateDistribution";



}
