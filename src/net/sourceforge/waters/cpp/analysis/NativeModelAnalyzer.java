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

package net.sourceforge.waters.cpp.analysis;

import java.nio.ByteBuffer;
import java.util.List;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.LeafOptionPage;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>The abstract base class of all native model analysers.</P>
 *
 * @author Robi Malik
 */

public abstract class NativeModelAnalyzer
  extends AbstractModelAnalyzer
{

  //#########################################################################
  //# Static Initialisation
  static {
    System.loadLibrary("waters");
  }


  //#########################################################################
  //# Constructors
  public NativeModelAnalyzer(final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    this(null, factory, translator);
  }

  public NativeModelAnalyzer(final ProductDESProxy model,
                             final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    super(model, factory, translator);
    mNativeModelAnalyzer = null;
    mDetailedOutputEnabled = true;
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether the event enablement condition is to be compiled into
   * a branching program to speed up synchronous product computation.
   * This option is enabled by default.
   */
  public void setEventTreeEnabled(final boolean enable)
  {
    mEventTreeEnabled = enable;
  }

  /**
   * Returns whether the event enablement condition is compiled into
   * a branching program to speed up synchronous product computation.
   * @see #setEventTreeEnabled(boolean)
   */
  public boolean isEventTreeEnabled()
  {
    return mEventTreeEnabled;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public void setDetailedOutputEnabled(final boolean enable)
  {
    mDetailedOutputEnabled = enable;
  }

  @Override
  public boolean isDetailedOutputEnabled()
  {
    return mDetailedOutputEnabled;
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    // TODO loop events - in superclass ...
    db.append(options, NativeModelVerifierFactory.
                       OPTION_NativeModelAnalyzer_EventTreeEnabled);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalTransitionLimit);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(NativeModelVerifierFactory.
                     OPTION_NativeModelAnalyzer_EventTreeEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setEventTreeEnabled(boolOption.getBooleanValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setNodeLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setTransitionLimit(intOption.getIntValue());
    } else {
      super.setOption(option);
    }
  }

  @Override
  public NativeVerificationResult createAnalysisResult()
  {
    return new NativeVerificationResult(this);
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    if (getModel() == null) {
      throw new AnalysisConfigurationException("Input model is NULL!");
    } else {
      clearAnalysisResult();
      final long start = System.currentTimeMillis();
      try {
        final AnalysisResult result = runNativeAlgorithm();
        final long stop = System.currentTimeMillis();
        result.setRuntime(stop - start);
        setAnalysisResult(result);
        return result.isSatisfied();
      } catch (final AnalysisException exception) {
        final long stop = System.currentTimeMillis();
        final AnalysisResult result = createAnalysisResult();
        result.setException(exception);
        result.setRuntime(stop - start);
        setAnalysisResult(result);
        throw exception;
      }
    }
  }


  //#########################################################################
  //# Native Methods
  abstract AnalysisResult runNativeAlgorithm() throws AnalysisException;

  @Override
  public native void requestAbort();

  @Override
  public native void resetAbort();

  public static native long getPeakMemoryUsage();


  //#########################################################################
  //# Auxiliary Methods for Native Code
  public ByteBuffer getNativeModelAnalyzer()
  {
    return mNativeModelAnalyzer;
  }

  public void setNativeModelAnalyzer(final ByteBuffer buffer)
  {
    mNativeModelAnalyzer = buffer;
  }


  //#########################################################################
  //# Data Members
  private ByteBuffer mNativeModelAnalyzer;
  private boolean mDetailedOutputEnabled;
  private boolean mEventTreeEnabled = true;

}
