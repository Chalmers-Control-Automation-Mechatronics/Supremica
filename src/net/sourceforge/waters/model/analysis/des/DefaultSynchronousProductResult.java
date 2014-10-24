//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis.des
//# CLASS:   DefaultSynchronousProductResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;



/**
 * The standard implementation of the {@link SynchronousProductResult}
 * interface. The default synchronous product result provides read/write
 * access to all the data provided by the interface.
 *
 * @author Robi Malik
 */

public class DefaultSynchronousProductResult
  extends DefaultAutomatonResult
  implements SynchronousProductResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an automaton result representing an incomplete run.
   */
  public DefaultSynchronousProductResult()
  {
  }


  //#########################################################################
  //# Simple Access Methods
  @Override
  public SynchronousProductStateMap getStateMap()
  {
    return mStateMap;
  }


  @Override
  public void setStateMap(final SynchronousProductStateMap map)
  {
    mStateMap = map;
  }


  //#########################################################################
  //# Data Members
  private SynchronousProductStateMap mStateMap;

}
