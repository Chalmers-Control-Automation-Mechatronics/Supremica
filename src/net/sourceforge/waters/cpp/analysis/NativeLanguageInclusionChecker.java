//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * <P>A monolithic language inclusion checker implementation, written in
 * C++.</P>
 *
 * @author Robi Malik
 */

public class NativeLanguageInclusionChecker
  extends NativeSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public NativeLanguageInclusionChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeLanguageInclusionChecker(final ProductDESProxy model,
                                        final ProductDESProxyFactory factory)
  {
    super(model, LanguageInclusionKindTranslator.getInstance(), factory);
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.cpp.analysis.NativeModelVerifier
  public String getTraceName()
  {
    return getModel().getName() + ":unsafe";
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
    buffer.append("' does not satisfy the language inclusion property ");
    buffer.append(aut.getName());
    buffer.append(": event ");
    buffer.append(event.getName());
    buffer.append(" may occur in state ");
    buffer.append(state.getName());
    buffer.append(", but the property disallows it.");
    return buffer.toString();
  }


}
