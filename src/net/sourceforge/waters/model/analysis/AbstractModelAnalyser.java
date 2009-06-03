//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelAnalyser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstract base class that can be used for all model analyser
 * implementations. This class simply provides access to model and
 * factory members needed by virtually any model analyser implementation.
 *
 * @author Robi Malik
 */

public abstract class AbstractModelAnalyser implements ModelAnalyser
{

  //#########################################################################
  //# Constructors
  public AbstractModelAnalyser(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public AbstractModelAnalyser(final ProductDESProxy model,
			       final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mModel = model;
    mNodeLimit = Integer.MAX_VALUE;
    mTransitionLimit = Integer.MAX_VALUE;
  }


  //#########################################################################
  //# Simple Access Methods
  public ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  public ProductDESProxy getModel()
  {
    return mModel;
  }

  public void setModel(final ProductDESProxy model)
  {
    mModel = model;
    clearAnalysisResult();
  }

  public void setModel(final AutomatonProxy aut)
  {
    ProductDESProxy des = createProductDESProxy(aut, mFactory);
    setModel(des);
  }

  public void setNodeLimit(final int limit)
  {
    mNodeLimit = limit;
  }

  public int getNodeLimit()
  {
    return mNodeLimit;
  }

  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }


  //#########################################################################
  //# Auxiliary Static Methods
  public static ProductDESProxy createProductDESProxy
    (final AutomatonProxy aut, final ProductDESProxyFactory factory)
  {
    final String name = aut.getName();
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<AutomatonProxy> automata = Collections.singletonList(aut);
    return factory.createProductDESProxy(name, events, automata);
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private ProductDESProxy mModel;
  private int mNodeLimit;
  private int mTransitionLimit;

}
