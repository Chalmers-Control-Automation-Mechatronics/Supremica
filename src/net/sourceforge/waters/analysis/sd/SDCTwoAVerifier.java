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
