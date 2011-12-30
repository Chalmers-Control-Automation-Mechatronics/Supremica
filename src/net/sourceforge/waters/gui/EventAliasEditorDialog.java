//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ForeachComponentEditorDialog
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
    mRoot = root;
    mAlias = alias;
    if (alias == null) {
      setTitle("Creating new Event Alias");
    } else {
      setTitle("Editing Event Alias");
    }
    run();
  }


  public SelectionOwner getSelectionOwner(){
    return mRoot.getEventAliasesPanel();
  }

    public ProxySubject getProxySubject()
  {
    return mAlias;
  }

  public void setProxySubject(final ProxySubject template)
  {
    mAlias = (EventAliasSubject)template;
  }

  public ProxySubject createNewProxySubject(final Object id,
                                            final ExpressionSubject exp)
  {
    return new EventAliasSubject((IdentifierProxy) id, exp);
  }

  public ExpressionSubject getExpression()
  {
    return mAlias.getExpression();
  }

  public ExpressionSubject getExpression(final ProxySubject template)
  {
    final EventAliasSubject temp = (EventAliasSubject)template;
    return temp.getExpression();
  }

  public String getName()
  {
    if(mAlias == null){
      return null;
    }
    return mAlias.getName();
  }

  public String getName(final ProxySubject template)
  {
    final EventAliasSubject temp = (EventAliasSubject)template;
    return temp.getName();
  }

  public int getOperatorMask()
  {
    return Operator.TYPE_NAME;
  }

  public ProxySubject createTemplate()
  {
    return new EventAliasSubject(new SimpleIdentifierSubject(""),
                                 new SimpleIdentifierSubject(""));
  }

  public ProxySubject getClone()
  {
    return mAlias.clone();
  }

  public void setIdentifier(final ProxySubject template, final Object id)
  {
    final EventAliasSubject temp = (EventAliasSubject)template;
    temp.setIdentifier((IdentifierSubject) id);
  }

  public void setExpression(final ProxySubject template, final ExpressionSubject exp)
  {
    final EventAliasSubject temp = (EventAliasSubject)template;
    temp.setExpression(exp);
  }

  public Object getInput(final SimpleExpressionCell name)
  {
    return name.getValue();
  }

  //#########################################################################
  //# Data Members
  private final ModuleWindowInterface mRoot;
  private EventAliasSubject mAlias;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;






}
