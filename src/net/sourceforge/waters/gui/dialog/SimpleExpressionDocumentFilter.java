//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
//# CLASS:   SimpleExpressionDocumentFilter
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.dialog;

import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.gui.util.CharacterDocumentFilter;
import net.sourceforge.waters.model.expr.ExpressionParser;


/**
 * A {@link DocumentFilter} implementation that can be linked to
 * Swing text fields in order to restrict the set of possible
 * characters to those allowed in Waters expressions.
 *
 * @author Robi Malik
 */

class SimpleExpressionDocumentFilter
  extends CharacterDocumentFilter
{

  //#########################################################################
  //# Constructor
  SimpleExpressionDocumentFilter(final ExpressionParser parser)
  {
    mExpressionParser = parser;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.CharacterDocumentFilter
  @Override
  protected boolean isAllowedCharacter(final char ch)
  {
    return mExpressionParser.isExpressionCharacter(ch);
  }


  //#########################################################################
  //# Data Members
  private final ExpressionParser mExpressionParser;

}
