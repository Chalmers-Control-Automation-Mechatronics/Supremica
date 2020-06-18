//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.awt.event.KeyEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.dialog.AutomatonPropertiesDialog;
import net.sourceforge.waters.gui.dialog.ConstantAliasEditorDialog;
import net.sourceforge.waters.gui.dialog.EdgeEditorDialog;
import net.sourceforge.waters.gui.dialog.EventAliasEditorDialog;
import net.sourceforge.waters.gui.dialog.EventDeclEditorDialog;
import net.sourceforge.waters.gui.dialog.ForeachEditorDialog;
import net.sourceforge.waters.gui.dialog.InstanceEditorDialog;
import net.sourceforge.waters.gui.dialog.NodeEditorDialog;
import net.sourceforge.waters.gui.dialog.ParameterBindingEditorDialog;
import net.sourceforge.waters.gui.dialog.SimpleComponentEditorDialog;
import net.sourceforge.waters.gui.dialog.VariableEditorDialog;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.DefaultProductDESAndModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ConstantAliasSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.MainPanel;
import org.supremica.gui.ide.ModuleContainer;


/**
 * <P>
 * The action associated with the 'properties' menu buttons.
 * </P>
 *
 * <P>
 * This action pops up a dialog box to edit the currently focused item, if
 * that item is of a supported type. To support this action, components that
 * hold editable items must implement the {@link SelectionOwner} interface and
 * return the item to be edited through their
 * {@link SelectionOwner#getSelectionAnchor() getSelectionAnchor()} method.
 * </P>
 *
 * @author Robi Malik
 */

public class IDEPropertiesAction extends WatersAction
{

  //#########################################################################
  //# Constructors
  IDEPropertiesAction(final IDE ide)
  {
    this(ide, null);
  }

  IDEPropertiesAction(final IDE ide, final Proxy arg)
  {
    super(ide);
    mActionArgument = arg;
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
    mVisitor = new PropertiesVisitor();
    updateEnabledStatus();
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final Proxy proxy = getActionArgument();
    mVisitor.editProperties(proxy);
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      updateEnabledStatus();
    }
  }

  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    final Proxy proxy = getActionArgument();
    final boolean enabled =
      proxy != null && mVisitor.canEditProperties(proxy);
    setEnabled(enabled);
    if (enabled) {
      final String name = ProxyNamer.getItemClassName(proxy);
      final String lname = name.toLowerCase();
      putValue(Action.NAME, name + " Properties ...");
      putValue(Action.SHORT_DESCRIPTION, "Edit properties of this " + lname);
    } else {
      putValue(Action.NAME, "Properties ...");
      putValue(Action.SHORT_DESCRIPTION, "Edit properties of selected item");
    }
  }

  private Proxy getActionArgument()
  {
    if (mActionArgument != null) {
      return mActionArgument;
    } else {
      return getSelectionAnchor();
    }
  }


  //#########################################################################
  //# Inner Class PropertiesVisitor
  private class PropertiesVisitor
    extends DefaultProductDESAndModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private boolean canEditProperties(final Proxy proxy)
    {
      try {
        mDoEdit = false;
        return (Boolean) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    private void editProperties(final Proxy proxy)
    {
      try {
        mDoEdit = true;
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Boolean visitProxy(final Proxy proxy)
    {
      return false;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
    @Override
    public Boolean visitAutomatonProxy(final AutomatonProxy aut)
    {
      if (mDoEdit) {
        final ModuleContainer container = getActiveModuleContainer();
        if (container == null) {
          return false;
        }
        final MainPanel panel = container.getActivePanel();
        if (panel == null || !(panel instanceof WatersAnalyzerPanel)) {
          return false;
        }
        final WatersAnalyzerPanel analyzer = (WatersAnalyzerPanel) panel;
        new AutomatonPropertiesDialog(analyzer,
                                      aut);
      }
      return true;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitConstantAliasProxy(final ConstantAliasProxy decl)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final ConstantAliasSubject subject = (ConstantAliasSubject) decl;
        new ConstantAliasEditorDialog(root, subject);
      }
      return true;
    }

    @Override
    public Boolean visitEdgeProxy(final EdgeProxy edge)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final EdgeSubject subject = (EdgeSubject) edge;
        EdgeEditorDialog.showDialog(subject, root);
      }
      return true;
    }

    @Override
    public Boolean visitEventAliasProxy(final EventAliasProxy alias)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final EventAliasSubject subject = (EventAliasSubject) alias;
        new EventAliasEditorDialog(root, subject);
      }
      return true;
    }

    @Override
    public Boolean visitEventDeclProxy(final EventDeclProxy decl)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final EventDeclSubject subject = (EventDeclSubject) decl;
        new EventDeclEditorDialog(root, subject);
      }
      return true;
    }

    @Override
    public Boolean visitForeachProxy(final ForeachProxy foreach)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final ForeachSubject subject = (ForeachSubject) foreach;
        final FocusTracker tracker = getFocusTracker();
        final SelectionOwner panel = tracker.getWatersSelectionOwner();
        new ForeachEditorDialog(root, panel, subject);
      }
      return true;
    }

    @Override
    public Boolean visitInstanceProxy(final InstanceProxy instance)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final InstanceSubject subject = (InstanceSubject) instance;
        new InstanceEditorDialog(root, subject);
      }
      return true;
    }

    @Override
    public Boolean visitParameterBindingProxy(final ParameterBindingProxy binding)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final ParameterBindingSubject subject =
          (ParameterBindingSubject) binding;
        new ParameterBindingEditorDialog(root, subject);
      }
      return true;
    }

    @Override
    public Boolean visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final SimpleComponentSubject subject = (SimpleComponentSubject) comp;
        new SimpleComponentEditorDialog(root, subject);
      }
      return true;
    }

    @Override
    public Boolean visitNodeProxy(final NodeProxy node)
    {
      if (mDoEdit) {
        final IDE ide = getIDE();
        final SelectionOwner panel =
          ide.getFocusTracker().getWatersSelectionOwner();
        if (panel instanceof GraphEditorPanel) {
          final ModuleContainer root = getActiveModuleContainer();
          final NodeSubject subject = (NodeSubject) node;
          new NodeEditorDialog(root, panel, subject);
        }
      }
      return true;
    }

    @Override
    public Boolean visitVariableComponentProxy(final VariableComponentProxy var)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final VariableComponentSubject subject =
          (VariableComponentSubject) var;
        new VariableEditorDialog(root, subject);
      }
      return true;
    }

    //#######################################################################
    //# Data Members
    private boolean mDoEdit;
  }

  //#########################################################################
  //# Data Members
  private final Proxy mActionArgument;
  private final PropertiesVisitor mVisitor;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1213915459887289849L;

}
