//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   AliasBindingContext
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.context;

import net.sourceforge.waters.model.module.IdentifierProxy;
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
 * indicating the location of the elementary event label within the
 * compiled alias declaration and with an <CODE>AliasBindingContext</CODE>
 * as context, which contains another source information record ({@link
 * SourceInfo}). That context's source information record indicates the
 * location of the alias symbol that was replaced, which may be an
 * occurrence on actual edge, or in the body of another alias
 * declaration.</P>
 *
 * <P>Thus, the source object of a compiled transition's source information
 * record ({@link SourceInfo}) does not necessarily indicate a location in
 * a graph edge of the original model. To get a location within a graph, the
 * source information's {@link SourceInfo#getGraphSourceInfo()
 * getGraphSourceInfo()} method can be used.</P>
 *
 * @see BindingContext
 * @see SourceInfo
 * @author Robi Malik
 */

public class AliasBindingContext implements BindingContext
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new alias binding context.
   * @param  info   The source information of the alias declaration
   *                that is being replaced.
   * @param  parent The parent binding context with additional bindings
   *                for the aliased symbol before its replacement.
   */
  public AliasBindingContext(final SourceInfo info,
                             final BindingContext parent)
  {
    mAliasSource = info;
    mParent = parent;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.BindingContext
  public SimpleExpressionProxy getBoundExpression(final IdentifierProxy ident)
  {
    return mParent.getBoundExpression(ident);
  }

  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    return mParent.isEnumAtom(ident);
  }

  public ModuleBindingContext getModuleBindingContext()
  {
    return mParent.getModuleBindingContext();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the source information record from where the alias has been
   * instantiated.
   * @return A {@link SourceInfo} record pointing to the {@link
   *         net.sourceforge.waters.model.module.SimpleExpressionProxy
   *         SimpleExpressionProxy} of the alias identifier that
   *         was replaced.
   */
  public SourceInfo getAliasSource()
  {
    return mAliasSource;
  }

  /**
   * Gets the parent binding context of this alias binding.
   * The parent context may provide additional bindings
   * for the aliased symbol after its replacement.
   */
  public BindingContext getParent()
  {
    return mParent;
  }


  //#########################################################################
  //# Data Members
  private final SourceInfo mAliasSource;
  private final BindingContext mParent;

}
