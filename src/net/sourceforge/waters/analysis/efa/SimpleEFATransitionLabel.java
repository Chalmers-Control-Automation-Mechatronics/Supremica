//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFATransitionLabel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import net.sourceforge.waters.model.compiler.constraint.ConstraintList;

/**
 * An implementation of {@link AbstractEFATransitionLabel}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFATransitionLabel
 extends AbstractEFATransitionLabel
{

  public SimpleEFATransitionLabel(final ConstraintList constraint,
                                  final SimpleEFAEventDecl... event)
  {
    super(constraint, event);
  }

  public SimpleEFATransitionLabel(final SimpleEFAEventDecl... event)
  {
    super(event);
  }
  
  @Override
  public String toString(){
    final StringBuffer events = new StringBuffer();
    events.append("{");
    for (final SimpleEFAEventDecl e : getEvents()){
      events.append(e.toString());
      events.append(",");
    }
    events.delete(events.length() - 1, events.length());
    events.append("}");
    return events.toString() + " : " + getConstraint().toString();
  }
}
