//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   DesktopEditAction
//###########################################################################
//# $Id$
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
