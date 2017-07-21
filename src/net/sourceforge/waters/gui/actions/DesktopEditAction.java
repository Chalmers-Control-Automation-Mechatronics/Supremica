//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public class DesktopEditAction extends WatersDesktopAction
{

  //#########################################################################
  //# Constructor
  DesktopEditAction(final IDE ide, final AutomatonProxy autoToEdit)
  {
    super(ide);
    mAutomaton = autoToEdit;
    final SimpleComponentSubject comp = getSimpleComponent();
    String name = null;
    if (comp != null) {
      final ComponentKind kind = comp.getKind();
      final String kindName = ModuleContext.getComponentKindToolTip(kind);
      final String compName = comp.getName();
      if (compName.length() <= 32) {
        name = kindName + " " + compName;
      }
    }
    if (name != null) {
      putValue(Action.NAME, "Edit " + name);
    } else {
      putValue(Action.NAME, "Edit Automaton");
    }
    putValue(Action.SHORT_DESCRIPTION, "Open this automaton in the editor");
    setEnabled(comp != null);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent e)
  {
    final SimpleComponentSubject comp = getSimpleComponent();
    if (comp != null) {
      final IDE ide = getIDE();
      try {
        final DocumentContainer docContainer = ide.getActiveDocumentContainer();
        final ModuleContainer modContainer = (ModuleContainer) docContainer;
        modContainer.showEditor(comp);
      } catch (final GeometryAbsentException exception) {
        final String msg = exception.getMessage(comp);
        ide.error(msg);
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleComponentSubject getSimpleComponent()
  {
    final IDE ide = getIDE();
    final DocumentContainer docContainer = ide.getActiveDocumentContainer();
    final ModuleContainer modContainer = (ModuleContainer) docContainer;
    final SourceInfo info = modContainer.getSourceInfoMap().get(mAutomaton);
    if (info != null) {
      final Proxy source = info.getSourceObject();
      if (source instanceof SimpleComponentSubject) {
        return (SimpleComponentSubject) source;
      }
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1644229513613033199L;

}
