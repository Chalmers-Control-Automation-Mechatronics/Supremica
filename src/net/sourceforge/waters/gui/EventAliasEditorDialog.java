//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventAliasEditorDialog
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.ExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


public class EventAliasEditorDialog extends AbstractBindingEditorDialog
{

  //#########################################################################
  //# Constructors
  public EventAliasEditorDialog(final ModuleWindowInterface root)
  {
    this(root, null);
  }

  public EventAliasEditorDialog(final ModuleWindowInterface root,
                                final EventAliasSubject alias)
  {
    super(root);
    mAlias = alias;
    if (alias == null) {
      setTitle("Creating new Event Alias");
    } else {
      setTitle("Editing Event Alias");
    }
    initialize();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.AbstractBindingEditorDialog
  @Override
  SelectionOwner getSelectionOwner()
  {
    final ModuleWindowInterface root = getRoot();
    return root.getEventAliasesPanel();
  }

  @Override
  ProxySubject getProxySubject()
  {
    return mAlias;
  }

  @Override
  void setProxySubject(final ProxySubject template)
  {
    mAlias = (EventAliasSubject) template;
  }

  @Override
  ProxySubject createNewProxySubject(final Object id,
                                     ExpressionSubject exp)
  {
    if(exp == null){
      exp = new PlainEventListSubject();
    }
    return new EventAliasSubject((IdentifierProxy) id, exp);
  }

  @Override
  ExpressionSubject getExpression()
  {
    return mAlias.getExpression();
  }

  @Override
  ExpressionSubject getExpression(final ProxySubject template)
  {
    final EventAliasSubject temp = (EventAliasSubject) template;
    return temp.getExpression();
  }

  @Override
  String getProxyName()
  {
    if (mAlias == null) {
      return null;
    }
    return mAlias.getName();
  }

  @Override
  String getProxyName(final ProxySubject template)
  {
    final EventAliasSubject temp = (EventAliasSubject) template;
    return temp.getName();
  }

  @Override
  int getOperatorMask()
  {
    return Operator.TYPE_NAME;
  }

  @Override
  ProxySubject createTemplate()
  {
    return new EventAliasSubject(new SimpleIdentifierSubject(""),
                                 new SimpleIdentifierSubject(""));
  }

  @Override
  void setIdentifier(final ProxySubject template, final Object id)
  {
    final EventAliasSubject temp = (EventAliasSubject) template;
    temp.setIdentifier((IdentifierSubject) id);
  }

  @Override
  void setExpression(final ProxySubject template, final ExpressionSubject exp)
  {
    final EventAliasSubject temp = (EventAliasSubject) template;
    temp.setExpression(exp);
  }

  @Override
  Object getInput(final SimpleExpressionCell name)
  {
    return name.getValue();
  }


  //#########################################################################
  //# Data Members
  private EventAliasSubject mAlias;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
