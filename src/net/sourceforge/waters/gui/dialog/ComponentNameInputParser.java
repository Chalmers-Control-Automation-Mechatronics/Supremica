//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
//# CLASS:   ComponentNameInputParser
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.dialog;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;


/**
 * An input parser for automaton names, for use with a
 * {@link javax.swing.JFormattedTextField JFormattedTextField}. This parser
 * allows entry of structured identifiers, and checks in addition whether
 * an entered name is already used by a component in a given module context.
 *
 * @see SimpleExpressionCell
 * @author Robi Malik
 */

class ComponentNameInputParser
  extends IdentifierInputParser
{

  //#########################################################################
  //# Constructor
  ComponentNameInputParser(final IdentifierProxy oldname,
                           final ModuleContext context,
                           final ExpressionParser parser)
  {
    super(oldname, parser);
    mModuleContext = context;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.FormattedInputParser
  @Override
  public IdentifierProxy parse(final String text)
    throws ParseException
  {
    final IdentifierProxy ident = super.parse(text);
    if (ident != getOldIdentifier()) {
      mModuleContext.checkNewComponentName(ident);
    }
    return ident;
  }


  //#######################################################################
  //# Data Members
  private final ModuleContext mModuleContext;

}
