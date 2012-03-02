//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDActivityLoopChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.modular.ModularControlLoopChecker;
import net.sourceforge.waters.model.analysis.ControlLoopChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 *      This class Checks for Activity Loops in an automata
        @author Mahvash Baloch
 */

public class SDActivityLoopChecker
  extends ModularControlLoopChecker
  implements ControlLoopChecker

{

  //#########################################################################
  //# Constructors
  public SDActivityLoopChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public SDActivityLoopChecker(final ProductDESProxy model,
                                    final ProductDESProxyFactory factory)
  {
    super(model,
          SDActivityLoopKindTranslator.getInstance(),
                    factory);
  }

}
