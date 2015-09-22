//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
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

import gnu.trove.set.hash.THashSet;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.EditorPanel;
import org.supremica.gui.ide.IDE;


/**
 * The action used to send an automaton from the analyser to the editor.
 *
 * @author Hugo Flordal, Robi Malik
 */

public class AnalyzerSendToEditorAction extends IDEAction
{

  //#########################################################################
  //# Constructor
  public AnalyzerSendToEditorAction(final List<IDEAction> actionList)
  {
    super(actionList);
    setEditorActiveRequired(false);
    setAnalyzerActiveRequired(true);
    putValue(Action.NAME, "To editor");
    putValue(Action.SHORT_DESCRIPTION, "Send selected automata to editor");
    putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
    //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
    putValue(Action.SMALL_ICON,
             new ImageIcon(IDE.class.getResource
                             ("/icons/supremica/toEditor16.gif")));
  }


  //#########################################################################
  //# Invocation
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    doAction();
  }

  @Override
  public void doAction()
  {
    if (ide.getActiveDocumentContainer().getEditorPanel() != null) {
      final DocumentContainer container = ide.getActiveDocumentContainer();
      final EditorPanel panel = container.getEditorPanel();
      final ModuleContext context = panel.getModuleContext();
      final ModuleSubject module = (ModuleSubject) container.getDocument();
      final ListSubject<AbstractSubject> compTarget =
        module.getComponentListModifiable();
      int compInsPos = compTarget.size();
      final Automata selectedAutomata =
          container.getAnalyzerPanel().getSelectedAutomata();
      final ProductDESImporter importer =
          new ProductDESImporter(ModuleSubjectFactory.getInstance());
      final List<InsertInfo> components = new LinkedList<InsertInfo>();
      final List<InsertInfo> decls = new LinkedList<InsertInfo>();
      final Collection<String> addedNames = new THashSet<String>();
      boolean problem = false;
      for (final Automaton aut : selectedAutomata) {
        // Try to import automaton ...
        try {
          final SimpleComponentProxy comp = importer.importComponent(aut);
          final IdentifierProxy ident = comp.getIdentifier();
          context.checkNewComponentName(ident);
          final ListInsertPosition inspos =
            new ListInsertPosition(compTarget, compInsPos++);
          final InsertInfo info = new InsertInfo(comp, inspos);
          components.add(info);
        } catch (final ParseException exception) {
          ide.getIDE().error("Could not add automaton " + aut.getName() +
                             " to editor: " + exception.getMessage());
          continue;
        }
        // Import new event declarations if needed ...
        for (final EventProxy event : aut.getEvents()) {
          final String name = event.getName();
          problem |= name.contains(".");
          if (context.getEventDecl(name) == null && addedNames.add(name)) {
            try {
              final EventDeclProxy decl = importer.importEventDecl(event);
              final IdentifierProxy ident = decl.getIdentifier();
              if (ident instanceof SimpleIdentifierProxy) {
                final InsertInfo info = new InsertInfo(decl);
                decls.add(info);
              }
            } catch (final ParseException exception) {
              ide.getIDE().error("Could not add event " + event.getName() +
                                 " to editor: " + exception.getMessage());
              continue;
            }
          }
        }
      }
      if (!components.isEmpty()) {
        // Create command ...
        final Command cmd;
        final SelectionOwner componentsPanel = panel.getComponentsPanel();
        final InsertCommand insertComponents =
            new InsertCommand(components, componentsPanel, null);
        insertComponents.setUpdatesSelection(false);
        if (decls.isEmpty()) {
          cmd = insertComponents;
        } else {
          final SelectionOwner eventsPanel = panel.getEventsPanel();
          final InsertCommand insertEvents =
              new InsertCommand(decls, eventsPanel, null);
          insertEvents.setUpdatesSelection(false);
          final String name = insertComponents.getName();
          final CompoundCommand compound = new CompoundCommand(name);
          compound.addCommand(insertComponents);
          compound.addCommand(insertEvents);
          compound.end();
          cmd = compound;
        }
        final UndoInterface undoer = panel.getUndoInterface();
        if (undoer == null) {
          // If there is no undo interface, just add them ...
          cmd.execute();
        } else {
          // Otherwise register the command ...
          undoer.executeCommand(cmd);
        }
      }
      if (problem) {
        ide.getIDE().warn
          ("There is a problem in the back-translation of parametrised events.");
      }
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}





