//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ComponentNameInputParser
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;


class ComponentNameInputParser
  implements FormattedInputParser
{

  //#########################################################################
  //# Constructor
  ComponentNameInputParser(final IdentifierProxy oldname,
			   final ModuleContext context,
			   final ExpressionParser parser)
  {
    mOldIdentifier = oldname;
    mOldName = oldname.toString();
    mModuleContext = context;
    mExpressionParser = parser;
    mDocumentFilter = new SimpleExpressionDocumentFilter(parser);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.FormattedInputParser
  public IdentifierProxy parse(final String text)
    throws ParseException
  {
    if (text.equals(mOldName)) {
      return mOldIdentifier;
    } 
    final IdentifierProxy ident = mExpressionParser.parseIdentifier(text);
    if (mOldIdentifier.equalsWithGeometry(ident)) {
      return mOldIdentifier;
    }
    mModuleContext.checkNewComponentName(ident);
    return ident;
  }

  public DocumentFilter getDocumentFilter()
  {
    return mDocumentFilter;
  }


  //#######################################################################
  //# Data Members
  private final IdentifierProxy mOldIdentifier;
  private final String mOldName;
  private final ModuleContext mModuleContext;
  private final ExpressionParser mExpressionParser;
  private final DocumentFilter mDocumentFilter;

}
