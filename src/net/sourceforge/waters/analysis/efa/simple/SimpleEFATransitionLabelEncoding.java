//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFATransitionLabelEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionLabelEncoding;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;

/**
 * An implementation of {@link AbstractEFATransitionLabelEncoding}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFATransitionLabelEncoding
 extends AbstractEFATransitionLabelEncoding<SimpleEFATransitionLabel>
{

  public SimpleEFATransitionLabelEncoding()
  {
    this(DEFAULT_SIZE);
  }

  public SimpleEFATransitionLabelEncoding(final int size)
  {
    super(size);
    final ConstraintList trueg = new ConstraintList();
    final EFAHelper helper = new EFAHelper();
    final SimpleEFAEventDecl event = new SimpleEFAEventDecl(helper.getTAUDecl());
    final SimpleEFATransitionLabel label =
     new SimpleEFATransitionLabel(event, trueg);
    createTransitionLabelId(label);
  }

  public SimpleEFATransitionLabelEncoding(
   final SimpleEFATransitionLabelEncoding encoding)
  {
    super(encoding);
  }
  
  @Override
  public String toString(){
    if (super.isEmpty()){
      return "[]";
    }
    final StringBuilder events = new StringBuilder();
    final String sep = " <> ";
    events.append("[");
    for (final SimpleEFATransitionLabel label : getTransitionLabels()){
      final String out = Integer.toString(getTransitionLabelId(label))
                   + " -> "
                   + label.toString()
                   + sep;
      events.append(out);
    }
    events.delete(events.length() - sep.length(), events.length());    
    events.append("]");
    return events.toString();
  }
  
}
