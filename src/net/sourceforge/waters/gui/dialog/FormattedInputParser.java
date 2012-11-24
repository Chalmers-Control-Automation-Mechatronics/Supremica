//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
///# CLASS:   FormattedInputParser
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.dialog;

import net.sourceforge.waters.model.expr.ParseException;

import javax.swing.text.DocumentFilter;


/**
 * @author Robi Malik
 */

public interface FormattedInputParser
{
  public Object parse(final String text) throws ParseException;
  public DocumentFilter getDocumentFilter();
}