//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ForeachEventAliasProxy
//###########################################################################
//# $Id: ForeachEventAliasProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.EventAliasListType;
import net.sourceforge.waters.xsd.module.ForeachEventAliasType;
import net.sourceforge.waters.xsd.module.ForeachType;


/**
 * <P>A foreach construct for module event aliases.</P>
 *
 * <P>A foreach event alias construct that occurs in the <I>event alias
 * list</I> of a module. The entries in its body can be of the following
 * types.</P>
 *
 * <UL>
 * <LI>{@link EventAliasProxy}</LI>
 * <LI>{@link ForeachEventAliasProxy}</LI>
 * </UL>
 *
 * @see ModuleProxy#getEventAliasList()
 *
 * @author Robi Malik
 */

public class ForeachEventAliasProxy extends ForeachProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new foreach event alias construct without guard and
   * an empty body.
   * @param  dummy       The name of the dummy variable.
   * @param  range       The range of iteration.
   */
  public ForeachEventAliasProxy(final String dummy,
				final SimpleExpressionProxy range)
  {
    super(dummy, range);
  }

  /**
   * Creates a new foreach event alias construct with an empty body.
   * @param  dummy       The name of the dummy variable.
   * @param  range       The range of iteration.
   * @param  guard       The guard expression.
   */
  public ForeachEventAliasProxy(final String dummy,
				final SimpleExpressionProxy range,
				final SimpleExpressionProxy guard)
  {
    super(dummy, range, guard);
  }

  /**
   * Creates a foreach construct from a parsed XML structure.
   * @param  foreach     The parsed XML structure of the new construct.
   * @param  factory     The factory to be used to create body elements.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  ForeachEventAliasProxy(final ForeachEventAliasType foreach,
			 final ForeachProxyFactory factory)
    throws ModelException
  {
    super(foreach, factory);
  }

  //#########################################################################
  //# Overrides for abstract base class ForeachProxy
  void storeBodyElement(final ForeachType foreach, final ElementType body)
  {
    final ForeachEventAliasType foreachalias =
      (ForeachEventAliasType) foreach;
    final EventAliasListType aliases = (EventAliasListType) body;
    foreachalias.setEventAliasList(aliases);
  }

}
