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
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.ExpressionSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


public class ParameterBindingEditorDialog extends AbstractBindingEditorDialog
{

  //#########################################################################
  //# Constructors
  public ParameterBindingEditorDialog(final ModuleWindowInterface root)
  {
    this(root, null);
  }

  public ParameterBindingEditorDialog(final ModuleWindowInterface root,
                                      final ParameterBindingSubject binding)
  {
    super(root);
    mRoot = root;
    mBinding = binding;
    if (binding == null) {
      setTitle("Creating new Parameter Binding");
    } else {
      setTitle("Editing Parameter Binding");
    }
    run();
  }

  public SelectionOwner getSelectionOwner()
  {
    return mRoot.getComponentsPanel();
  }

  public ProxySubject getProxySubject()
  {
    return mBinding;
  }

  public void setProxySubject(final ProxySubject template)
  {
    mBinding = (ParameterBindingSubject)template;
  }

  public ProxySubject createNewProxySubject(final Object id,
                                            final ExpressionSubject exp)
  {
    return new ParameterBindingSubject((String) id, exp);
  }

  public ExpressionSubject getExpression()
  {
    return mBinding.getExpression();
  }

  public ExpressionSubject getExpression(final ProxySubject template)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject)template;
    return para.getExpression();
  }

  public String getName()
  {
    if(mBinding == null){
      return null;
    }
    return mBinding.getName();
  }

  public String getName(final ProxySubject template)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject)template;
    return para.getName();
  }

  public int getOperatorMask()
  {
    return Operator.TYPE_ANY;
  }

  public ProxySubject createTemplate()
  {
    return new ParameterBindingSubject("", new SimpleIdentifierSubject(""));
  }

  public ProxySubject getClone()
  {
    return mBinding.clone();
  }

  public void setIdentifier(final ProxySubject template, final Object id)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject)template;
    para.setName((String)id);
  }

  public void setExpression(final ProxySubject template, final ExpressionSubject exp)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject)template;
    para.setExpression(exp);
  }

  public Object getInput(final SimpleExpressionCell name)
  {
    return name.getText();
  }


  //#########################################################################
  //# Data Members
  // Swing components
  private final ModuleWindowInterface mRoot;
  private ParameterBindingSubject mBinding;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
