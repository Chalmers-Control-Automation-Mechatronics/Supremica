//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   AliasProxy
//###########################################################################
//# $Id: AliasProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.xsd.module.IdentifiedType;


/**
 * <P>An alias definition used to assign a simple name to a commonly used
 * expression.</P>
 *
 * <P>Aliases are used in to separate ways in modules.</P>
 * <UL>
 * <LI>To define named constants representing integers or ranges. Such
 *     aliases are of type {@link ConstantAliasProxy} and occur in the
 *     <I>constant alias list</I> of a module.</LI>
 * <LI>To give names to groups of events that are used in several different
 *     edges or graphs of a module. Such aliases are of type {@link
 *     EventAliasProxy} and occur in the <I>event alias list</I> of a
 *     module.</LI>
 * </UL>
 *
 * <P>Each alias has a name and a value bound to the name. The name can be
 * an arbitrary identifier. In particular, it can be indexed, as aliases
 * can also be defined iteratively in a <I>foreach</I> construct ({@link
 * ForeachEventAliasProxy}). The value is an arbitrary expression, its type
 * can vary depending on the particular kind of alias.</P>
 *
 * @see ModuleProxy
 *
 * @author Robi Malik
 */

public abstract class AliasProxy extends IdentifiedElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an alias.
   * @param  ident       The name for the new alias.
   */
  AliasProxy(final IdentifierProxy ident)
  {
    super(ident);
  }

  /**
   * Creates an alias from a parsed XML structure.
   * This method is for internal use only and should not be called
   * directly.
   * @param  alias       The parsed XML structure of the new element.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  AliasProxy(final IdentifiedType alias)
    throws ModelException
  {
    super(alias);
  }


  //#########################################################################
  //# Getters and Setters
  public abstract ExpressionProxy getExpression();
  public abstract void setExpression(final ExpressionProxy proxy);


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    getIdentifier().pprint(printer);
    printer.print(" = ");
    getExpression().pprint(printer);
  }

}
