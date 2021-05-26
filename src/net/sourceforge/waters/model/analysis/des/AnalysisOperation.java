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

import java.util.Collections;
import java.util.LinkedList;

import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.analysis.hisc.SICProperty5Verifier;
import net.sourceforge.waters.analysis.hisc.SICProperty6Verifier;
import net.sourceforge.waters.analysis.monolithic.MonolithicNerodeEChecker;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.JavaEnumFactory;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.AnalysisOptionPage;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.WatersOptionPages;


/**
 * Enumeration of possible verification and other analysis operations
 * accessible though GUI or command line.
 *
 * @author Robi Malik, Benjamin Wheeler
 */

public enum AnalysisOperation
{
  //#########################################################################
  //# Standard Operations
  CONFLICT_CHECK("waters.analysis.conflict", "conflict", "-conf",
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

  CONTROLLABILITY_CHECK("waters.analysis.controllability",
                        "controllability", "-cont",
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

  CONTROL_LOOP_CHECK("waters.analysis.loop", "control loop", "-controlloop",
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

  DEADLOCK_CHECK("waters.analysis.deadlock", "deadlock", "-dl",
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

  DIAGNOSABILITY_CHECK("waters.analysis.diagnosability",
                       "diagnosability", "-diag",
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

  LANGUAGE_INCLUSION_CHECK("waters.analysis.languageinclusion",
                           "language inclusion", "-lang",
                           "does not satisfy language inclusion",
                           "satisfies language inclusion")
  {
    @Override
    public void preConfigure(final ModuleCompiler compiler)
    {
      compiler.setEnabledPropositionNames(Collections.emptyList());
      compiler.setEnabledPropertyNames(null);
    }

    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createLanguageInclusionChecker(desFactory);
    }
  },

  LOOP_CHECK("waters.analysis.loop", "loop", "-loop",
             "has a loop", "is free")
  {
    @Override
    public final ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createControlLoopChecker(desFactory);
    }
  },

  STATE_COUNT("waters.analysis.statecount", "state count", "-count")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createStateCounter(desFactory);
    }
  },

  SYNCHRONOUS_PRODUCT("waters.analysis.syncprod",
                      "Synchronization", "synchronous product", "-sync")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createSynchronousProductBuilder(desFactory);
    }
  },

  SYNTHESIS("waters.analysis.synthesis",
            "Synthesis", "supervisor synthesis", "-synth")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      return factory.createSupervisorSynthesizer(desFactory);
    }
  },


  //#########################################################################
  //# Hierarchical Interface-Based Supervisory Control (HISC) Verification
  SIC5("waters.analysis.conflict", "SIC property V", "-sic5",
       "does not satisfy SIC property V", "satisfies SIC property V")
  {
    @Override
    public void preConfigure(final ModuleCompiler compiler)
    {
      super.preConfigure(compiler);
      compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
    }

    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      final ConflictChecker conflictChecker =
        factory.createConflictChecker(desFactory);
      return new SICProperty5Verifier(conflictChecker, null, desFactory);
    }
  },

  SIC6("waters.analysis.conflict", "SIC property VI", "-sic6",
       "does not satisfy SIC property VI", "satisfies SIC property VI")
  {
    @Override
    public void preConfigure(final ModuleCompiler compiler)
    {
      super.preConfigure(compiler);
      compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
    }

    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
        throws AnalysisConfigurationException
    {
      final ConflictChecker conflictChecker =
        factory.createConflictChecker(desFactory);
      return new SICProperty6Verifier(conflictChecker, null, desFactory);
    }
  },


  //#########################################################################
  //# Sampled Data (SD) Verification
  SD_III2("waters.analysis.languageinclusion",
          "SD controllability iii.2", "-sd32",
          "does not satisfy SD controllability point iii.2",
          "satisfies SD controllability point iii.2")
  {
    @Override
    public ModelAnalyzer createModelAnalyzer
      (final ModelAnalyzerFactory factory, final ProductDESProxyFactory desFactory)
    {
      return new MonolithicNerodeEChecker(desFactory);
    }
  };


  //#########################################################################
  //# Constructor
  private AnalysisOperation(final String optionPagePrefix,
                            final String analysisName,
                            final String consoleName)
  {
    this(optionPagePrefix, analysisName, analysisName, consoleName);
  }

  private AnalysisOperation(final String optionPagePrefix,
                            final String shortAnalysisName,
                            final String longAnalysisName,
                            final String consoleName)
  {
    mOptionPagePrefix = optionPagePrefix;
    mShortAnalysisName = shortAnalysisName;
    mLongAnalysisName = longAnalysisName;
    mConsoleName = consoleName;
    mFailureDescription = mSuccessDescription = null;
  }

  private AnalysisOperation(final String optionPagePrefix,
                            final String analysisName,
                            final String consoleName,
                            final String failureDescription,
                            final String successDescription)
  {
    mOptionPagePrefix = optionPagePrefix;
    mLongAnalysisName = analysisName + " check";
    mShortAnalysisName = analysisName;
    mConsoleName = consoleName;
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

  public String getShortAnalysisName()
  {
    return mShortAnalysisName;
  }

  public String getShortWindowTitle()
  {
    return toTitle(mShortAnalysisName);
  }

  public String getLongAnalysisName()
  {
    return mLongAnalysisName;
  }

  public String getLongWindowTitle()
  {
    return toTitle(mLongAnalysisName);
  }

  public String getConsoleName()
  {
    return mConsoleName;
  }

  public String getFailureDescription()
  {
    return mFailureDescription;
  }

  public String getSuccessDescription()
  {
    return mSuccessDescription;
  }

  private static String toTitle(final String text)
  {
    final int len = text.length();
    final StringBuilder builder = new StringBuilder(len);
    boolean newWord = true;
    for (int i = 0; i < len; i++) {
      final char ch = text.charAt(i);
      if (newWord) {
        builder.append(Character.toTitleCase(ch));
        newWord = false;
      } else {
        builder.append(ch);
        newWord = (ch == ' ');
      }
    }
    return builder.toString();
  }


  //#########################################################################
  //# Configuring Model Analysers
  public void preConfigure(final ModuleCompiler compiler)
  {
    compiler.setEnabledPropertyNames(new LinkedList<>());
    compiler.setEnabledPropositionNames(new LinkedList<>());
  }

  public abstract ModelAnalyzer createModelAnalyzer
    (ModelAnalyzerFactory factory, ProductDESProxyFactory desFactory)
    throws AnalysisConfigurationException;

  public ModelAnalyzer createAndConfigureModelAnalyzer
    (final ProductDESProxyFactory desFactory)
    throws AnalysisConfigurationException
  {
    try {
      final AnalysisOptionPage page = getOptionPage();
      final EnumOption<ModelAnalyzerFactoryLoader> selector =
        page.getTopSelectorOption();
      final ModelAnalyzerFactoryLoader loader = selector.getValue();
      final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
      final ModelAnalyzer analyzer = createModelAnalyzer(factory, desFactory);
      for (final Option<?> option : analyzer.getOptions(page)) {
        analyzer.setOption(option);
      }
      return analyzer;
    } catch (final ClassNotFoundException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mShortAnalysisName;
  }


  //#########################################################################
  //# Enum Factory
  public static EnumFactory<AnalysisOperation> getEnumFactory()
  {
    return AnalysisOperationEnumFactory.INSTANCE;
  }


  private static class AnalysisOperationEnumFactory
    extends JavaEnumFactory<AnalysisOperation>
  {
    //#######################################################################
    //# Constructor
    private AnalysisOperationEnumFactory()
    {
      super(AnalysisOperation.class, CONTROLLABILITY_CHECK);
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.EnumFactory<AnalysisOperation>
    @Override
    public String getConsoleName(final AnalysisOperation operation)
    {
      return operation.getConsoleName();
    }

    @Override
    public boolean isDisplayedInConsole(final AnalysisOperation operation)
    {
      return operation != CONTROL_LOOP_CHECK && operation != SD_III2;
    }

    //#######################################################################
    //# Singleton Instance
    private static AnalysisOperationEnumFactory INSTANCE =
      new AnalysisOperationEnumFactory();
  }


  //#########################################################################
  //# Data Members
  private final String mOptionPagePrefix;
  private final String mShortAnalysisName;
  private final String mLongAnalysisName;
  private final String mFailureDescription;
  private final String mSuccessDescription;
  private final String mConsoleName;

}
