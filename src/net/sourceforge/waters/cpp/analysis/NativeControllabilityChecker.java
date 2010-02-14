//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * <P>A monolithic controllability checker implementation, written in C++.</P>
 *
 * @author Robi Malik
 */

public class NativeControllabilityChecker
  extends NativeSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public NativeControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeControllabilityChecker(final ProductDESProxy model,
                                      final ProductDESProxyFactory factory)
  {
    super(model, factory, ControllabilityKindTranslator.getInstance());
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.cpp.analysis.NativeModelVerifier
  public String getTraceName()
  {
    return getModel().getName() + "-uncontrollable";
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier
  public String getTraceComment(final EventProxy event,
                                final AutomatonProxy aut,
                                final StateProxy state)
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("The model '");
    buffer.append(getModel().getName());
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
