//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   MonolithicLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * @author Robi Malik
 */

public class MonolithicLanguageInclusionChecker
  extends MonolithicSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public MonolithicLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public MonolithicLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model, LanguageInclusionKindTranslator.getInstance(), factory);
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
