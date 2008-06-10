//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   SourceInfo
//###########################################################################
//# $Id: SourceInfo.java,v 1.2 2008-06-10 08:30:42 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.compiler;


import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A link between the input and the output of the {@link
 * ModuleCompiler}.</P>
 *
 * <P>Each item in the compiler output is associated with a source
 * information record that enables the user to trace from which item in the
 * original module the compiled item was produced, and which binding were
 * used. More specifically, the items of a {@link
 * net.sourceforge.waters.model.des.ProductDESProxy ProductDESProxy} are
 * associated with <CODE>SourceInfo</CODE> records as follows.</P>
 *
 * <UL>
 * <LI>Each {@link net.sourceforge.waters.model.des.AutomatonProxy
 *     AutomatonProxy} object has a record pointing to the {@link
 *     net.sourceforge.waters.model.module.SimpleComponentProxy
 *     SimpleComponentProxy} or {@link
 *     net.sourceforge.waters.model.module.VariableComponentProxy
 *     VariableComponentProxy} object from which it was produced.</LI>
 * <LI>Each {@link net.sourceforge.waters.model.des.StateProxy
 *     StateProxy} object has a record pointing to the {@link
 *     net.sourceforge.waters.model.module.SimpleNodeProxy SimpleNodeProxy}
 *     object from which it was produced.</LI>
 * <LI>Each {@link net.sourceforge.waters.model.des.TransitionProxy
 *     TransitionProxy} object has a record pointing to a {@link
 *     net.sourceforge.waters.model.module.SimpleExpressionProxy
 *     SimpleExpressionProxy} object in the {@link
 *     net.sourceforge.waters.model.module.LabelBlockProxy LabelBlockProxy}
 *     of the edge from which the transition was produced.</LI>
 * <LI>Each {@link net.sourceforge.waters.model.des.EventProxy
 *     EventProxy} object has a record pointing to the {@link
 *     net.sourceforge.waters.model.module.EventDeclProxy EventDeclProxy}
 *     object from which it was produced.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public class SourceInfo
{

  //#########################################################################
  //# Constructor
  SourceInfo(final Proxy proxy, final BindingContext context)
  {
    mSourceObject = proxy;
    mBindingContext = context;
  }


  //#########################################################################
  //# Simple Access
  public Proxy getSourceObject()
  {
    return mSourceObject;
  }

  public BindingContext getBindingContext()
  {
    return mBindingContext;
  }


  //#########################################################################
  //# Data Members
  private final Proxy mSourceObject;
  private final BindingContext mBindingContext;

}

