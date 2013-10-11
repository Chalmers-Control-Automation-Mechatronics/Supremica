//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFATransitionLabel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionLabel;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;

/**
 * An implementation of {@link AbstractEFATransitionLabel}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFATransitionLabel
 extends AbstractEFATransitionLabel
{

  public SimpleEFATransitionLabel(final SimpleEFAEventDecl event,
                                  final ConstraintList constraint)
  {
    super(constraint, event);
  }

  public SimpleEFATransitionLabel(final SimpleEFAEventDecl event)
  {
    super(event);
  }
  
  @Override
  public String toString(){
    final StringBuilder events = new StringBuilder();
    events.append("{");
    events.append(getEvent().toString());
    events.append(" : ");
    events.append(getConstraint().toString());
    events.append("}");
    return events.toString();
  }
}
