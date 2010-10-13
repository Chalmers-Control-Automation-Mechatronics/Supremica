//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   SDPlantCompletenessDiagnostics
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.io.Serializable;

import net.sourceforge.waters.model.analysis.SafetyDiagnostics;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * <P>A diagnostics generator used for plant completeness checking.</P>
 *
 * @author Robi Malik
 */

public class SDPlantCompletenessDiagnostics
  implements SafetyDiagnostics, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static SDPlantCompletenessDiagnostics getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final SDPlantCompletenessDiagnostics theInstance =
      new SDPlantCompletenessDiagnostics();
  }

  private SDPlantCompletenessDiagnostics()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyDiagnostics
  public String getTraceName(final ProductDESProxy des)
  {
    final String desname = des.getName();
    return desname + "-incomplete";
  }

  public String getTraceComment(final ProductDESProxy des,
                                final EventProxy event,
                                final AutomatonProxy aut,
                                final StateProxy state)
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("The model '");
    buffer.append(des.getName());
    buffer.append("' is not complete because plant ");
    buffer.append(aut.getName());
    buffer.append(" disables the controllable event ");
    buffer.append(event.getName());
    buffer.append(" in state ");
    buffer.append(state.getName());
    buffer.append(", but it is possible according to the specification.");
    return buffer.toString();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
