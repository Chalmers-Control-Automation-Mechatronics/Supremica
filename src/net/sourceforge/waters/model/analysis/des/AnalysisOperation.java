//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

/**
 *
 * @author Benjamin Wheeler
 */
public abstract class AnalysisOperation
{

  private AnalysisOperation(final String optionMapName,
                            final String analysisName,
                            final String failureDescription,
                            final String successDescription)
  {
    mOptionMapName = optionMapName;
    mAnalysisName = analysisName;
    mFailureDescription = failureDescription;
    mSuccessDescription = successDescription;
  }

  public String getOptionMapName()
  {
    return mOptionMapName;
  }

  public String getOptionMapPrefix()
  {
    return mOptionMapName.replace('/', '.');
  }

  public String getAnalysisName()
  {
    return mAnalysisName;
  }

  public String getFailureDescription()
  {
    return mFailureDescription;
  }

  public String getSuccessDescription()
  {
    return mSuccessDescription;
  }

  public abstract ModelAnalyzer createModelAnalyzer
    (ModelAnalyzerFactory factory, ProductDESProxyFactory desFactory)
      throws AnalysisConfigurationException;

  private final String mOptionMapName;
  private final String mAnalysisName;
  private final String mFailureDescription;
  private final String mSuccessDescription;

  public static final AnalysisOperation CONFLICT_CHECK =
    new AnalysisOperation("waters.analysis/conflict", "Conflict",
                          "is nonblocking", "is blocking")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createConflictChecker(desFactory);
    }
  };

  public static final AnalysisOperation CONTROLLABILITY_CHECK =
    new AnalysisOperation("waters.analysis/controllability", "Controllability",
                          "is not controllable", "is controllable")
  {
    @Override
    public final ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createControllabilityChecker(desFactory);
    }
  };

  public static final AnalysisOperation CONTROL_LOOP_CHECK =
    new AnalysisOperation("waters.analysis/loop", "Control Loop",
                          "has a control loop", "is control-loop free")
  {
    @Override
    public final ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createControlLoopChecker(desFactory);
    }
  };

  public static final AnalysisOperation DEADLOCK_CHECK =
    new AnalysisOperation("waters.analysis/deadlock", "Deadlock Check",
                          "has a deadlock loop", "is deadlock-loop free")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createDeadlockChecker(desFactory);
    }
  };

  public static final AnalysisOperation LANGUAGE_INCLUSION_CHECK =
    new AnalysisOperation("waters.analysis/languageinclusion", "Language Inclusion",
                          "does not satisfy Language Inclusion",
                          "satisfies Language Inclusion")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createLanguageInclusionChecker(desFactory);
    }
  };

  public static final AnalysisOperation STATE_COUNTER =
    new AnalysisOperation("waters.analysis/statecount", "State Counter",
                          null, null)
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createStateCounter(desFactory);
    }
  };

  public static final AnalysisOperation SYNCHRONOUS_PRODUCT =
    new AnalysisOperation("waters.analysis/syncprod", "Synchronize",
                          null, null)
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createSynchronousProductBuilder(desFactory);
    }
  };

  public static final AnalysisOperation SUPERVISOR_SYNTHESIZER =
    new AnalysisOperation("waters.analysis/synthesis", "Synthesize",
                          null, null)
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createSupervisorSynthesizer(desFactory);
    }
  };

}
