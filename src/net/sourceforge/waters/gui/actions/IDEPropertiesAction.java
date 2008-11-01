//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDEPropertiesAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.EditorEditEdgeDialog;
import net.sourceforge.waters.gui.EventEditorDialog;
import net.sourceforge.waters.gui.ForeachComponentEditorDialog;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.SimpleComponentEditorDialog;
import net.sourceforge.waters.gui.VariableEditorDialog;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ForeachComponentSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;

import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'properties' menu buttons.</P>
 *
 * <P>This action pops up a dialog box to edit the currently focussed
 * item, if that item is of a supported type. To support this action,
 * components including editable items must implement the {@link
 * SelectionOwner} interface and return the item to be edited through
 * their {@link SelectionOwner#getSelectionAnchor() getSelectionAnchor()}
 * method.</P>
 *
 * @author Robi Malik
 */

public class IDEPropertiesAction
  extends WatersAction
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
  public void actionPerformed(final ActionEvent event)
  {
    final Proxy proxy = getActionArgument();
    mVisitor.editProperties(proxy);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      updateEnabledStatus();
    }
  }


  //#########################################################################
  //# Auxilary Methods
  private void updateEnabledStatus()
  {
    final Proxy proxy = getActionArgument();
    final boolean enabled = proxy != null && mVisitor.canEditProperties(proxy);
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
    extends AbstractModuleProxyVisitor
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
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Boolean visitProxy(final Proxy proxy)
    {
      return false;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public Boolean visitEdgeProxy(final EdgeProxy edge)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final EdgeSubject subject = (EdgeSubject) edge;
        EditorEditEdgeDialog.showDialog(subject, root);
      }
      return true;
    }

    public Boolean visitEventDeclProxy(final EventDeclProxy decl)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final EventDeclSubject subject = (EventDeclSubject) decl;
        new EventEditorDialog(root, subject);
      }
      return true;
    }

    public Boolean visitForeachComponentProxy
      (final ForeachComponentProxy comp)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final ForeachComponentSubject subject = (ForeachComponentSubject) comp;
        new ForeachComponentEditorDialog(root, subject);
      }
      return true;
    }

    public Boolean visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
    {
      if (mDoEdit) {
        final ModuleWindowInterface root = getActiveModuleWindowInterface();
        final SimpleComponentSubject subject = (SimpleComponentSubject) comp;
        new SimpleComponentEditorDialog(root, subject);
      }
      return true;
    }

    public Boolean visitVariableComponentProxy
      (final VariableComponentProxy var)
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

}
