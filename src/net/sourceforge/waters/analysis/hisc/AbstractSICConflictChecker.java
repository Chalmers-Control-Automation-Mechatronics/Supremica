//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   AbstractSICConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstract base class for model verifier to check SIC or LDIC Properties
 * of HISC models that are based on a conflict check.
 *
 * The abstract base class provides the common configuration option to
 * configure the underlying conflict checker needed for different SIC
 * verification tasks.
 *
 * @author Robi Malik
 */

abstract public class AbstractSICConflictChecker
  extends AbstractConflictChecker
{

  //#########################################################################
  //# Constructors
  public AbstractSICConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public AbstractSICConflictChecker(final ConflictChecker checker,
                                    final ProductDESProxyFactory factory)
  {
    this(checker, null, factory);
  }

  public AbstractSICConflictChecker(final ConflictChecker checker,
                                    final ProductDESProxy model,
                                    final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mChecker = checker;
  }


  //#########################################################################
  //# Configuration
  public ConflictChecker getConflictChecker()
  {
    return mChecker;
  }

  public void setConflictChecker(final ConflictChecker checker)
  {
    mChecker = checker;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    mChecker.requestAbort();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return mChecker.supportsNondeterminism();
  }


  //#########################################################################
  //# Data Members
  private ConflictChecker mChecker;

}
