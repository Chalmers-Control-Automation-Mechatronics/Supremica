//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   MonolithicControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * @author Robi Malik
 */

public class MonolithicControllabilityChecker
  extends MonolithicSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public MonolithicControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public MonolithicControllabilityChecker(final ProductDESProxy model,
                                          final ProductDESProxyFactory factory)
  {
    super(model, ControllabilityKindTranslator.getInstance(), factory);
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
    buffer.append("' is not controllable: specification ");
    buffer.append(aut.getName());
    buffer.append(" disables the uncontrollable event ");
    buffer.append(event.getName());
    buffer.append(" in state ");
    buffer.append(state.getName());
    buffer.append(", but it is possible according to the plant model.");
    return buffer.toString();
  }

}
