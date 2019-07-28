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

package net.sourceforge.waters.cpp.analysis;

import java.util.List;

import net.sourceforge.waters.analysis.options.BoolParameter;
import net.sourceforge.waters.analysis.options.IntParameter;
import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterIDs;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public abstract class NativeModelVerifier
  extends NativeModelAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public NativeModelVerifier(final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    this(null, factory, translator);
  }

  public NativeModelVerifier(final ProductDESProxy model,
                             final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelVerifier
  @Override
  public void setShortCounterExampleRequested(final boolean req)
  {
    mShortCounterExampleRequested = req;
  }

  @Override
  public boolean isShortCounterExampleRequested()
  {
    return mShortCounterExampleRequested;
  }

  @Override
  public void setCounterExampleEnabled(final boolean enable)
  {
    setDetailedOutputEnabled(enable);
  }

  @Override
  public boolean isCounterExampleEnabled()
  {
    return isDetailedOutputEnabled();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Parameter> getParameters()
  {
    final List<Parameter> list = super.getParameters();
    list.add(new BoolParameter(ParameterIDs.ModelVerifier_DetailedOutputEnabled) {
      @Override
      public void commitValue()
      {
        setDetailedOutputEnabled(getValue());
      }
    });
    list.add(new BoolParameter(ParameterIDs.ModelVerifier_ShortCounterExampleRequested) {
      @Override
      public void commitValue()
      {
        setShortCounterExampleRequested(getValue());
      }
    });
    list.add(new IntParameter(ParameterIDs.ModelAnalyzer_NodeLimit) {
      @Override
      public void commitValue() {
        setNodeLimit(getValue());
      }
    });
    list.add(new IntParameter(ParameterIDs.ModelAnalyzer_TransitionLimit) {
      @Override
      public void commitValue() {
        setTransitionLimit(getValue());
      }
    });
    list.add(new BoolParameter(ParameterIDs.NativeModelVerifier_EventTreeEnabled) {
      @Override
      public void commitValue()
      {
        setEventTreeEnabled(getValue());
      }
    });
    return list;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifier
  @Override
  public boolean isSatisfied()
  {
    final VerificationResult result = getAnalysisResult();
    if (result != null) {
      return result.isSatisfied();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  @Override
  public CounterExampleProxy getCounterExample()
  {
    if (isSatisfied()) {
      throw new IllegalStateException("No trace for satisfied property!");
    } else {
      final VerificationResult result = getAnalysisResult();
      return result.getCounterExample();
    }
  }

  @Override
  public VerificationResult getAnalysisResult()
  {
    return (VerificationResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Native Methods
  @Override
  abstract VerificationResult runNativeAlgorithm() throws AnalysisException;

  public abstract String getTraceName();


  //#########################################################################
  //# Data Members
  private boolean mShortCounterExampleRequested = false;

}
