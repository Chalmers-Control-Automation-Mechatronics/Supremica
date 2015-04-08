//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   TRSynchronousProductBuilder
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A Java implementation of the monolithic synchronous product algorithm,
 * based on {@link ListBufferTransitionRelation} as automaton representation.
 *
 * @author Robi Malik
 */

public class TRSynchronousProductBuilder
  extends TRAbstractSynchronousProductBuilder
{

  //#########################################################################
  //# Constructors
  public TRSynchronousProductBuilder()
  {
  }

  public TRSynchronousProductBuilder(final ProductDESProxy model)
  {
    super(model);
  }

  public TRSynchronousProductBuilder
    (final ProductDESProxy model,
     final KindTranslator translator)
  {
    super(model, translator);
  }

}

