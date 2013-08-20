//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFATransitionLabelEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

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
    super();
  }

  public SimpleEFATransitionLabelEncoding(final int size)
  {
    super(size);
    final ConstraintList trueg = new ConstraintList();
    final SimpleEFAEventDecl[] event = new SimpleEFAEventDecl[1];
    event[0] = SimpleEFAEventDecl.getSimpleTAU(null);
    final SimpleEFATransitionLabel label = new SimpleEFATransitionLabel(trueg,
     event);
    createTransitionLabelId(label);
  }

  public SimpleEFATransitionLabelEncoding(
   final SimpleEFATransitionLabelEncoding encoding)
  {
    super(encoding);
  }
  
}
