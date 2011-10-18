//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   DefaultAutomatonResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * The standard implementation of the {@link AutomatonResult} interface.
 * The default automaton result provides read/write access to all the data
 * provided by the interface.
 *
 * @author Robi Malik
 */

public class DefaultAutomatonResult
  extends DefaultProxyResult<AutomatonProxy>
  implements AutomatonResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an automaton result representing an incomplete run.
   */
  public DefaultAutomatonResult()
  {
  }


  //#########################################################################
  //# Simple Access Methods
  public AutomatonProxy getComputedAutomaton()
  {
    return getComputedProxy();
  }

  public void setComputedAutomaton(final AutomatonProxy aut)
  {
    setComputedProxy(aut);
  }

}
