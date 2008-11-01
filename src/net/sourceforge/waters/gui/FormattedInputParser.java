//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   FormattedInputParser
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import net.sourceforge.waters.model.expr.ParseException;

import javax.swing.text.DocumentFilter;



public interface FormattedInputParser
{
  public Object parse(final String text) throws ParseException;
  public DocumentFilter getDocumentFilter();
}