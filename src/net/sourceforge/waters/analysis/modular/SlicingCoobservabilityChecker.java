//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import java.util.List;

import net.sourceforge.waters.analysis.coobs.CoobservabilityAttributeFactory;
import net.sourceforge.waters.analysis.coobs.CoobservabilitySignature;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.des.CoobservabilityChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.CoobservabilityCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.ChainedAnalyzerOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.PositiveIntOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>The modular coobservability check algorithm,
 * implemented based on {@link AbstractModularVerifier}.</P>
 *
 * <P><I>Reference:</I><BR>
 * Huailiang Liu, Ryan Leduc, Robi Malik, S. Laurie Ricker.
 * Incremental verification of co-observability in discrete-event systems.
 * 2014 American Control Conference (ACC'14), 5446&ndash;5452, 2014.</P>
 *
 * @author Robi Malik
 */

public class SlicingCoobservabilityChecker
  extends AbstractModelVerifier
  implements CoobservabilityChecker
{

  //#########################################################################
  //# Constructors
  public SlicingCoobservabilityChecker(final ProductDESProxyFactory factory,
                                       final CoobservabilityChecker nested)
  {
    this(null, factory, nested);
  }

  public SlicingCoobservabilityChecker(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final CoobservabilityChecker nested)
  {
    super(model, factory, ControllabilityKindTranslator.getInstance());
    mNestedVerifier = nested;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.analysis.des.CoobservabilityChecker
  @Override
  public void setDefaultSiteName(final String name)
  {
    mDefaultSiteName = "".equals(name) ? null : name;
  }

  @Override
  public String getDefaultSiteName()
  {
    return mDefaultSiteName == null ? "" : mDefaultSiteName;
  }

  @Override
  public void setSignature(final CoobservabilitySignature sig)
  {
    mSignature = sig;
  }

  @Override
  public CoobservabilityCounterExampleProxy getCounterExample()
  {
    return (CoobservabilityCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalTransitionLimit);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_CoobservabilityChecker_DefaultSite);
    db.append(options, SlicingModelVerifierFactory.
                       OPTION_SlicingCoobservabilityChecker_Chain);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_CoobservabilityChecker_DefaultSite)) {
      final String value = (String) option.getValue();
      setDefaultSiteName(value);
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setNodeLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setTransitionLimit(intOption.getIntValue());
    } else if (option.hasID(SlicingModelVerifierFactory.
                            OPTION_SlicingCoobservabilityChecker_Chain)) {
      try {
        final ChainedAnalyzerOption chain = (ChainedAnalyzerOption) option;
        final ProductDESProxyFactory factory = getFactory();
        mNestedVerifier =
          (CoobservabilityChecker) chain.createAndConfigureModelAnalyzer(factory);
      } catch (final AnalysisConfigurationException exception) {
        throw new WatersRuntimeException(exception);
      }
      super.setOption(option);
    }
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return mNestedVerifier.supportsNondeterminism();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mNestedVerifier != null) {
      mNestedVerifier.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mNestedVerifier != null) {
      mNestedVerifier.resetAbort();
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ProductDESProxy des = getModel();
    mNestedVerifier.setModel(des);
    mNestedVerifier.setNodeLimit(getNodeLimit());
    mNestedVerifier.setTransitionLimit(getTransitionLimit());
    mNestedVerifier.setCounterExampleEnabled(isCounterExampleEnabled());
    if (mSignature == null) {
      final KindTranslator translator = getKindTranslator();
      mSignature =
        new CoobservabilitySignature(des, translator, mDefaultSiteName);
    }
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final Logger logger = LogManager.getLogger();

      CoobservabilitySignature.SiteSet sites = mSignature.findMinimalSiteSet();
      while (sites != null) {
        checkAbort();
        logger.debug("Checking site set {} ...", sites);
        final CoobservabilitySignature sig =
          new CoobservabilitySignature(mSignature, sites);
        mNestedVerifier.setSignature(sig);
        if (!mNestedVerifier.run()) {
          final CoobservabilityCounterExampleProxy counter =
            mNestedVerifier.getCounterExample();
          return setFailedResult(counter);
        }
        mSignature.removeCoveredEvents(sites);
        sites = mSignature.findMinimalSiteSet();
      }

      return setSatisfiedResult();
    } catch (final OutOfMemoryError error) {
      System.gc();
      final AnalysisException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final AnalysisException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSignature = null;
  }


  //#########################################################################
  //# Data Members
  private String mDefaultSiteName =
    CoobservabilityAttributeFactory.DEFAULT_SITE_NAME;
  private CoobservabilityChecker mNestedVerifier;
  private CoobservabilitySignature mSignature;

}
