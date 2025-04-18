//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.dialog;

import net.sourceforge.waters.gui.analyzer.AutomataTableModel;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;


/**
 * An input parser for automaton names, for use with a
 * {@link javax.swing.JFormattedTextField JFormattedTextField}. This parser
 * allows entry of structured identifiers, and checks in addition whether an
 * entered name is already used by a component in a given module context.
 *
 * @see SimpleExpressionInputCell
 * @author Robi Malik
 */

public class AutomatonNameInputHandler extends IdentifierInputHandler
{


  //#########################################################################
  //# Constructor
  public AutomatonNameInputHandler(final IdentifierProxy oldname,
                                   final WatersAnalyzerPanel panel,
                                   final ExpressionParser parser,
                                   final boolean nameChange,
                                   final boolean nullAllowed)
  {
    super(oldname, parser, nullAllowed);
    mModel = panel.getAutomataTableModel();
    mNameChange = nameChange;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.FormattedInputParser
  @Override
  public IdentifierProxy parse(final String text)
    throws java.text.ParseException
  {
    try {
      final IdentifierProxy ident = super.parse(text);
      if (!mNameChange) {
        mModel.checkNewAutomatonName(ident);
      } else {
        final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
        if (!eq.equals(getOldIdentifier(), ident)) {
          mModel.checkNewAutomatonName(ident);
        }
      }
      return ident;
    } catch (final ParseException exception) {
      throw exception.getJavaException();
    }
  }


  //#######################################################################
  //# Data Members
  private final AutomataTableModel mModel;
  private final boolean mNameChange;

}
