//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import net.sourceforge.waters.model.options.AnalysisOptionPage;
import net.sourceforge.waters.model.options.WatersOptionPages;

/**
 *
 * @author Benjamin Wheeler
 */
public enum AnalysisOperation
{
  //#########################################################################
  //# Enumeration
  CONFLICT_CHECK("ConflictChecker", "waters.analysis.conflict", "Conflict",
                 "is blocking", "is nonblocking")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createConflictChecker(desFactory);
    }
  },

  CONTROLLABILITY_CHECK("ControllabilityChecker",
                        "waters.analysis.controllability", "Controllability",
                        "is not controllable", "is controllable")
  {
    @Override
    public final ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createControllabilityChecker(desFactory);
    }
  },

  CONTROL_LOOP_CHECK("ControlLoopChecker", "waters.analysis.loop", "Control Loop",
                     "has a control loop", "is control-loop free")
  {
    @Override
    public final ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createControlLoopChecker(desFactory);
    }
  },

  DEADLOCK_CHECK("DeadlockChecker", "waters.analysis.deadlock", "Deadlock",
                 "has a deadlock", "is deadlock free")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createDeadlockChecker(desFactory);
    }
  },

  DIAGNOSABILITY_CHECK("DiagnosabilityChecker",
                       "waters.analysis.diagnosability", "Diagnosability",
                       "is not diagnosable", "is diagnosable")
  {
    @Override
    public DiagnosabilityChecker createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createDiagnosabilityChecker(desFactory);
    }
  },

  LANGUAGE_INCLUSION_CHECK("LanguageInclusionChecker",
                           "waters.analysis.languageinclusion",
                           "Language Inclusion",
                           "does not satisfy language inclusion",
                           "satisfies language inclusion")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createLanguageInclusionChecker(desFactory);
    }
  },

  STATE_COUNT("StateCounter", "waters.analysis.statecount", "State Count")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createStateCounter(desFactory);
    }
  },

  SYNCHRONOUS_PRODUCT("SynchrounousProductBuilder",
                      "waters.analysis.syncprod", "Synchronization")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createSynchronousProductBuilder(desFactory);
    }
  },

  SYNTHESIS("SupervsorSynthesizer", "waters.analysis.synthesis", "Synthesis")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createSupervisorSynthesizer(desFactory);
    }
  };


  //#########################################################################
  //# Constructor
  private AnalysisOperation(final String key,
                            final String optionPagePrefix,
                            final String analysisName)
  {
    this(key, optionPagePrefix, analysisName, null, null);
  }

  private AnalysisOperation(final String key,
                            final String optionPagePrefix,
                            final String analysisName,
                            final String failureDescription,
                            final String successDescription)
  {
    mKey = key;
    mOptionPagePrefix = optionPagePrefix;
    mAnalysisName = analysisName;
    mFailureDescription = failureDescription;
    mSuccessDescription = successDescription;
  }


  //#########################################################################
  //# Simple Access
  public String getOptionPagePrefix()
  {
    return mOptionPagePrefix;
  }

  public AnalysisOptionPage getOptionPage()
  {
    return (AnalysisOptionPage)
      WatersOptionPages.ANALYSIS.getLeafOptionPage(mOptionPagePrefix);
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


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mKey;
  }


  //#########################################################################
  //# Data Members
  private String mKey;
  private final String mOptionPagePrefix;
  private final String mAnalysisName;
  private final String mFailureDescription;
  private final String mSuccessDescription;

}
