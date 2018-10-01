//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.javabdd.BDD;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
// import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.BDD.EFA.BDDExtendedSynthesizer;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.Guard.BDDExtendedGuardGenerator;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;

public class EditorGenerateTextLabelAction extends IDEAction
{

    //#########################################################################
    //# Constructor
    public EditorGenerateTextLabelAction(final List<IDEAction> actionList)
    {
        super(actionList);
        setEditorActiveRequired(true);

        final String actName =
          "Recompute Guards (and Actions) Text label";
        final String description = "Recompute the Text label of Guards";

        putValue(Action.NAME, actName);
        putValue(Action.SHORT_DESCRIPTION, description);
    }

    //#########################################################################
    //# Overridden methods
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    @Override
    public void doAction()
    {
      ///////////////////////////////////////////////////
	  // TODO: * Do the same for actions?              //
	  //       * Add undo operation. How?              //
	  //       * Automatic Save as?                    //
	  //       * Remove unused imports                 //
	  //       * Option: Guards and/or Actions         //
	  //       * Option: Replace all / only if missing //
	  ///////////////////////////////////////////////////
	  final ModuleSubject moduleSubject =
        ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();
      for (final AbstractSubject absSubject
				  : moduleSubject.getComponentListModifiable()) {
        if (absSubject instanceof SimpleComponentSubject) {
		  final SimpleComponentSubject componentSubject = (SimpleComponentSubject) absSubject;
		  final GraphSubject graphSubject = componentSubject.getGraph();
		  for (final EdgeSubject edgeSubject
					  : graphSubject.getEdgesModifiable()) {
		    final GuardActionBlockSubject guardActionBlockSubject = edgeSubject.getGuardActionBlock();
		    for (final SimpleExpressionSubject guardSubject
						: guardActionBlockSubject.getGuardsModifiable()) {
			  String newPlainText = guardSubject.toString();
			  guardSubject.setPlainText(newPlainText);
		    }
		  }
		}
	  }
    }
	
	//#########################################################################
    //# Class Constants
    private final Logger logger = LogManager.getLogger(IDE.class);
    private final long serialVersionUID = 1L;
 }