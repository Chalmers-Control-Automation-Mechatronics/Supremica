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

package net.sourceforge.waters.gui.dialog;

import java.text.ParseException;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.transfer.ProxyTransferable;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.ExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


/**
 * @author Carly Hona
 */

public class ParameterBindingEditorDialog
  extends AbstractBindingEditorDialog<SimpleIdentifierProxy>
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
    final SelectionOwner panel = getSelectionOwner();
    final Proxy anchor = panel.getSelectionAnchor();
    if (anchor == null) {
      mExistingBindings = null;
    } else if (anchor instanceof InstanceSubject) {
      final InstanceSubject inst = (InstanceSubject) anchor;
      mExistingBindings = inst.getBindingListModifiable();
    } else if (anchor instanceof ProxySubject) {
      final ProxySubject subject = (ProxySubject) anchor;
      final InstanceSubject inst =
        SubjectTools.getAncestor(subject, InstanceSubject.class);
      mExistingBindings = inst.getBindingListModifiable();
    } else {
      mExistingBindings = null;
    }
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
  ProxySubject createNewProxySubject(final IdentifierSubject ident,
                                     ExpressionSubject expr)
  {
    final String name = ident.toString();
    if (expr == null){
      expr = new PlainEventListSubject();
    }
    return new ParameterBindingSubject(name, expr);
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
  FormattedInputHandler<SimpleIdentifierProxy>
  createInputParser(final IdentifierProxy oldIdent,
                    final ExpressionParser parser)
  {
    final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) oldIdent;
    return new ParameterBindingInputHandler(simple, parser);
  }

  @Override
  IdentifierSubject getProxyIdentifier()
  {
    if (mBinding == null) {
      return null;
    } else {
      final String name = mBinding.getName();
      return new SimpleIdentifierSubject(name);
    }
  }

  @Override
  IdentifierSubject getProxyIdentifier(final ProxySubject template)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject) template;
    final String name = para.getName();
    return new SimpleIdentifierSubject(name);
  }

  @Override
  int getOperatorMask()
  {
    return Operator.TYPE_ANY;
  }

  @Override
  ProxyTransferable createTemplateTransferable()
  {
    final ParameterBindingSubject template =
      new ParameterBindingSubject("", new SimpleIdentifierSubject(""));
    return WatersDataFlavor.createTransferable(template);
  }

  @Override
  void setIdentifier(final ProxySubject template,
                     final IdentifierSubject ident)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject) template;
    final String name = ident.toString();
    para.setName(name);
  }

  @Override
  void setExpression(final ProxySubject template, final ExpressionSubject exp)
  {
    final ParameterBindingSubject para = (ParameterBindingSubject) template;
    para.setExpression(exp);
  }


  //#########################################################################
  //# Inner Class ParameterBindingInputHandler
  private class ParameterBindingInputHandler
    extends SimpleIdentifierInputHandler
  {
    //#######################################################################
    //# Constructor
    ParameterBindingInputHandler(final SimpleIdentifierProxy oldname,
                                 final ExpressionParser parser)
    {
      super(oldname, parser, true);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.FormattedInputParser
    @Override
    public SimpleIdentifierProxy parse(final String text)
      throws ParseException
    {
      final SimpleIdentifierProxy ident = super.parse(text);
      if (mExistingBindings != null && ident != getOldIdentifier()) {
        final String name = ident.getName();
        if (mExistingBindings.containsName(name)) {
          final InstanceSubject inst =
            SubjectTools.getAncestor(mExistingBindings, InstanceSubject.class);
          final String instName = inst.getName();
          final StringBuilder buffer = new StringBuilder("Instance '");
          buffer.append(instName);
          buffer.append("' already has a binding for '");
          buffer.append(name);
          buffer.append("'.");
          final String msg = buffer.toString();
          throw new ParseException(msg, 0);
        }
      }
      return ident;
    }
  }


  //#########################################################################
  //# Data Members
  private ParameterBindingSubject mBinding;
  private final IndexedListSubject<ParameterBindingSubject> mExistingBindings;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1483302008823600671L;

}
