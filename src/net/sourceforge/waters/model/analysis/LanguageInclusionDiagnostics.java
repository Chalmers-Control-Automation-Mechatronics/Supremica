//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControllabilityKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.Serializable;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * <P>A diagnostics generator used for language inclusion checking.</P>
 *
 * @author Robi Malik
 */

public class LanguageInclusionDiagnostics
  implements SafetyDiagnostics, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static LanguageInclusionDiagnostics getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final LanguageInclusionDiagnostics theInstance =
      new LanguageInclusionDiagnostics();
  }

  private LanguageInclusionDiagnostics()
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
    buffer.append("' does not satisfy the language inclusion property '");
    buffer.append(aut.getName());
    buffer.append('\'');
    if (event == null) {
      buffer.append(", because this property has no initial state ");
      buffer.append("and therefore represents the empty language.");
    } else {
      buffer.append(": event ");
      buffer.append(event.getName());
      buffer.append(" may occur in state ");
      buffer.append(state.getName());
      buffer.append(", but the property disallows it.");
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
