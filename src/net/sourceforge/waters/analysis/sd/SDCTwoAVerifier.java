//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters SD Analysis
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDCTwoAVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A model verifier to check SD Controllability Property II.a.</P>
 *
 * <P>The check is done by creating a test automaton and modifying plant
 * automata for each prohibitable event in the model, and passing these
 * models to a language inclusion checker.</P>
 *
 * <P><STRONG>Reference.</STRONG> Mahvash Baloch. A compositional approach for
 * verifying sampled-data supervisory control. M.Sc. Thesis, Dept. of
 * Computing and Software, McMaster University, March 2012.</P>
 *
 * @see SDCTwoPropertyBuilder
 * @see LanguageInclusionChecker
 *
 * @author Mahvash Baloch , Robi Malik
 */


//#########################################################################
//# Constructors
public class SDCTwoAVerifier extends AbstractSDLanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public SDCTwoAVerifier(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public SDCTwoAVerifier(final LanguageInclusionChecker checker,
                         final ProductDESProxyFactory factory)
  {
    super(checker, factory);
  }

  public SDCTwoAVerifier(final LanguageInclusionChecker checker,
                         final ProductDESProxy model,
                         final ProductDESProxyFactory factory)
  {
    super(checker, model, factory);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether the check is to be performed using separate models.
   * If set to <CODE>true</CODE> separate language inclusion checks are
   * carried out for each prohibitable event, otherwise a single language
   * inclusion check is carried out to check for all prohibitable events
   * simultaneously.
   */
  public void setSplitting(final boolean splitting)
  {
    mSplitting = splitting;
  }

  /**
   * Returns whether the check is performed using separate models.
   * @see #setSplitting(boolean) setSplitting()
   */
  public boolean isSplitting()
  {
    return mSplitting;
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    setUp();
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> oldAutomata = model.getAutomata();
    final int numaut = oldAutomata.size();
    if (numaut == 0) {
      return setSatisfiedResult();
    }
    final SDCTwoPropertyBuilder builder =
      new SDCTwoPropertyBuilder(model, getFactory());
    try {
      final Collection<EventProxy> hibs = builder.getSDCTwoAEvents();
      if (mSplitting) {
        for (final EventProxy hib : hibs) {
          logHibEvent(hib, hibs);
          final ProductDESProxy convertedModel =
            builder.createSDCTwoAModel(hib);
          if (!runChecker(convertedModel)) {
            mFailedProhibitable = hib;
            return false;
          }
        }
      } else {
        final ProductDESProxy convertedModel = builder.createSDCTwoAModel(hibs);
        return runChecker(convertedModel);
      }
    } finally {
      tearDown();
    }
    return setSatisfiedResult();
  }

  public EventProxy getFailedProhibitable()
  {
    return mFailedProhibitable;
  }


  //#########################################################################
  //# Data Members
  private boolean mSplitting = true;
  private EventProxy mFailedProhibitable;

}