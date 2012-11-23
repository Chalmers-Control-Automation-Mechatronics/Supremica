//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
//# CLASS:   IdentifierInputParser
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.dialog;

import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.gui.FormattedInputParser;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;


/**
 * An input parser to support identifiers ({@link IdentifierProxy}), for use
 * with a {@link javax.swing.JFormattedTextField JFormattedTextField}. This
 * parser allows entry of arbitrary structured identifiers. No additional
 * checks are performed.
 *
 * @see SimpleExpressionCell
 * @author Robi Malik
 */

public class IdentifierInputParser
  implements FormattedInputParser
{

  //#########################################################################
  //# Constructor
  public IdentifierInputParser(final IdentifierProxy oldIdent,
                               final ExpressionParser parser)
  {
    mOldIdentifier = oldIdent;
    mOldName = oldIdent.toString();
    mEquality = ModuleEqualityVisitor.getInstance(true);
    mExpressionParser = parser;
    mDocumentFilter = new SimpleExpressionDocumentFilter(parser);
  }


  //#########################################################################
  //# Simple Access
  public IdentifierProxy getOldIdentifier()
  {
    return mOldIdentifier;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.FormattedInputParser
  @Override
  public IdentifierProxy parse(final String text)
    throws ParseException
  {
    if (text.equals(mOldName)) {
      return mOldIdentifier;
    }
    final IdentifierProxy ident = mExpressionParser.parseIdentifier(text);
    if (mEquality.equals(mOldIdentifier, ident)) {
      return mOldIdentifier;
    }
    return ident;
  }

  @Override
  public DocumentFilter getDocumentFilter()
  {
    return mDocumentFilter;
  }


  //#######################################################################
  //# Data Members
  private final IdentifierProxy mOldIdentifier;
  private final String mOldName;
  private final ModuleEqualityVisitor mEquality;
  private final ExpressionParser mExpressionParser;
  private final DocumentFilter mDocumentFilter;

}
