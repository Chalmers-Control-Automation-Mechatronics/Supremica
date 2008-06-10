//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   AliasBindingContext
//###########################################################################
//# $Id: AliasBindingContext.java,v 1.1 2008-06-10 18:56:29 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.compiler;


import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A binding context constructed when compiling an alias.</P>
 *
 * <P>A alias binding context contains a reference to another source
 * information record ({@link SourceInfo}), identifying a location
 * within a compiled alias declaration.</P>
 *
 * <P>When an alias symbol in a label block is compiled, each generated
 * transition has a source information record ({@link SourceInfo})
 * indicating the location of the alias symbol in the label and with an
 * <CODE>BindingContext</CODE> as context, which contains another source
 * information record ({@link SourceInfo}). The context's source
 * information record indicates a symbol within the event list of the
 * corresponding alias declaration.</P>
 *
 * @see BindingContext, SourceInfo
 * @author Robi Malik
 */

public class AliasBindingContext implements BindingContext
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new alias binding context.
   * @param  info   The source information within the alias declaration
   *                that is being compiled.
   * @param  parent The parent binding context with additional bindings
   *                for the aliased symbol before its replacement.
   */
  AliasBindingContext(final SourceInfo info,
                      final BindingContext parent)
  {
    mAliasSource = info;
    mParent = parent;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.BindingContext
  public SimpleExpressionProxy getBoundExpression(final String name)
  {
    return mParent.getBoundExpression(name);
  }

  public ModuleBindingContext getModuleBindingContext()
  {
    return mParent.getModuleBindingContext();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the source information record from which the alias has been
   * instantiated.
   * @return A {@link SourceInfo} record pointing to the {@link
   *         net.sourceforge.waters.model.module.SimpleExpressionProxy
   *         SimpleExpressionProxy} within the compiled alias
   *         expression.
   */
  SourceInfo getAliasSource()
  {
    return mAliasSource;
  }

  /**
   * Gets the parent binding context of this alias binding.
   * The parent context may provide additional bindings
   * for the aliased symbol before its replacement.
   */
  BindingContext getParent()
  {
    return mParent;
  }


  //#########################################################################
  //# Data Members
  private final SourceInfo mAliasSource;
  private final BindingContext mParent;

}

