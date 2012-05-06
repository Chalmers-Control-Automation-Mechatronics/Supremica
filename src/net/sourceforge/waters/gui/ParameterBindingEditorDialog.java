//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ParameterBindingEditorDialog
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.ExpressionSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
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
    mBinding = binding;
    if (binding == null) {
      setTitle("Creating new Parameter Binding");
    } else {
      setTitle("Editing Parameter Binding");
    }
    initialize();
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.AbstractBindingEditorDialog
  @Override
  SelectionOwner getSelectionOwner()
  {
    final ModuleWindowInterface root = getRoot();
    return root.getComponentsPanel();
  }

  @Override
  ProxySubject getProxySubject()
  {
    return mBinding;
  }

  @Override
  void setProxySubject(final ProxySubject template)
  {
    mBinding = (ParameterBindingSubject) template;
  }

  @Override
  ProxySubject createNewProxySubject(final Object id,
                                     ExpressionSubject exp)
  {
    if(exp == null){
      exp = new PlainEventListSubject();
    }
    return new ParameterBindingSubject((String) id, exp);
  }

  @Override
  ExpressionSubject getExpression()
  {
    return mBinding.getExpression();
  }

  @Override
  ExpressionSubject getExpression(final ProxySubject template)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject) template;
    return para.getExpression();
  }

  @Override
  String getProxyName()
  {
    if (mBinding == null) {
      return null;
    }
    return mBinding.getName();
  }

  @Override
  String getProxyName(final ProxySubject template)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject) template;
    return para.getName();
  }

  @Override
  int getOperatorMask()
  {
    return Operator.TYPE_ANY;
  }

  @Override
  ProxySubject createTemplate()
  {
    return new ParameterBindingSubject("", new SimpleIdentifierSubject(""));
  }

  @Override
  void setIdentifier(final ProxySubject template, final Object id)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject) template;
    para.setName((String) id);
  }

  @Override
  void setExpression(final ProxySubject template, final ExpressionSubject exp)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject) template;
    para.setExpression(exp);
  }

  @Override
  Object getInput(final SimpleExpressionCell name)
  {
    return name.getText();
  }


  //#########################################################################
  //# Data Members
  private ParameterBindingSubject mBinding;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
