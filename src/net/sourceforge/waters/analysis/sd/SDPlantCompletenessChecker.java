//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDPlantCompletenessChecker
//###########################################################################
//# $Id: MonolithicControllabilityChecker.java 5118 2010-01-17 06:28:22Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.analysis.monolithic.MonolithicSafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * @author Mahvash Baloch
 */

public class SDPlantCompletenessChecker
  extends MonolithicSafetyVerifier
  //implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public SDPlantCompletenessChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public SDPlantCompletenessChecker(final ProductDESProxy model,
                                          final ProductDESProxyFactory factory)
  {
    super(model, SDPlantCompletenessKindTranslator.getInstance(), factory);
  }


  //#########################################################################
  //# Auxiliary Methods
  String createTraceComment(final ProductDESProxy des,
                            final EventProxy event,
                            final AutomatonProxy aut,
                            final StateProxy state)
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("The model '");
    buffer.append(des.getName());
    buffer.append("' is not Complete because");
    buffer.append(aut.getName());
    buffer.append(" disables the controllable event ");
    buffer.append(event.getName());
    buffer.append(" in state ");
    buffer.append(state.getName());
    buffer.append(", but it is possible according to the Specicification.");
    return buffer.toString();
  }

}
