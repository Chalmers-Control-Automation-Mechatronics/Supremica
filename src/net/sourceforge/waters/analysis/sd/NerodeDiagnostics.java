//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControllabilityKindTranslator
//###########################################################################
//# $Id: LanguageInclusionDiagnostics.java 5927 2010-09-24 04:54:47Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.io.Serializable;

import net.sourceforge.waters.model.analysis.SafetyDiagnostics;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A diagnostics generator used for language inclusion checking.</P>
 *
 * @author Robi Malik
 */

public class NerodeDiagnostics
  implements SafetyDiagnostics, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static NerodeDiagnostics getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final NerodeDiagnostics theInstance =
      new NerodeDiagnostics();
  }

  private NerodeDiagnostics()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyDiagnostics
  public String getTraceName(final ProductDESProxy des)
  {
    final String desname = des.getName();
    return desname + "-unsafe";
  }

  public String getTraceComment(final ProductDESProxy des,
                                final EventProxy event,
                                final AutomatonProxy aut,
                                final StateProxy state)
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("The model '");
    buffer.append(des.getName());
    buffer.append("' does not satisfy the Nerode Equivalence property '");

    if(event.getKind().equals(EventKind.PROPOSITION))
        {
      buffer.append(" one of the concurrent String in '");
      buffer.append(aut.getName());
      buffer.append("' leads to a marked State ");
      buffer.append(state.getName());
      buffer.append(" but the other one does not ");
        }
    else
      {buffer.append(aut.getName());
       buffer.append("' contains concurrent Strings which do not lead to Nerode equivalent States");
      }
       //buffer.append(" : event ");
    //buffer.append(event.getName());
    //buffer.append(" takes to state");
    //buffer.append(state.getName());
    //buffer.append(", but the other string does not");
    return buffer.toString();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
