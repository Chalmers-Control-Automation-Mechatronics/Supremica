//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
//# CLASS:   ParameterBindingEditorDialog
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.dialog;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.transfer.ProxyTransferable;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
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
  FormattedInputParser createInputParser(final IdentifierProxy oldIdent,
                                         final ExpressionParser parser)
  {
    final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) oldIdent;
    return new ParameterBindingInputParser(simple, parser);
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
  //# Inner Class ParameterBindingInputParser
  private class ParameterBindingInputParser
  extends SimpleIdentifierInputParser
  {
    //#######################################################################
    //# Constructor
    ParameterBindingInputParser(final SimpleIdentifierProxy oldname,
                                final ExpressionParser parser)
    {
      super(oldname, parser);
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
          final StringBuffer buffer = new StringBuffer("Instance '");
          buffer.append(instName);
          buffer.append("' already has a binding for '");
          buffer.append(name);
          buffer.append("'!");
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
  private static final long serialVersionUID = 1L;

}
