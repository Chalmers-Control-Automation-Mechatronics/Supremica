//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   FormattedInputParser
//###########################################################################
//# $Id: FormattedInputParser.java,v 1.1 2006-08-08 23:59:21 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import net.sourceforge.waters.model.expr.ParseException;

import javax.swing.text.DocumentFilter;



public interface FormattedInputParser
{
  public Object parse(final String text) throws ParseException;
  public DocumentFilter getDocumentFilter();
}