//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ForeachEventProxy
//###########################################################################
//# $Id: ForeachEventProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.ForeachEventType;
import net.sourceforge.waters.xsd.module.ForeachType;


/**
 * <P>A foreach construct for module events.</P>
 *
 * <P>A foreach event construct that occurs in an <I>event list</I> ({@link
 * EventListProxy}), which may occur on a graph's edge, in an alias
 * definition, or in the actual parameter of an instnce. The entries in its
 * body can be of the following types.</P>
 *
 * <UL>
 * <LI>{@link net.sourceforge.waters.model.expr.SimpleIdentifierProxy}</LI>
 * <LI>{@link net.sourceforge.waters.model.expr.IndexedIdentifierProxy}</LI>
 * <LI>{@link ForeachEventProxy}</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public class ForeachEventProxy extends ForeachProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new foreach event construct without guard and an empty body.
   * @param  dummy       The name of the dummy variable.
   * @param  range       The range of iteration.
   */
  public ForeachEventProxy(final String dummy,
			   final SimpleExpressionProxy range)
  {
    super(dummy, range);
  }

  /**
   * Creates a new foreach event construct with an empty body.
   * @param  dummy       The name of the dummy variable.
   * @param  range       The range of iteration.
   * @param  guard       The guard expression.
   */
  public ForeachEventProxy(final String dummy,
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
  ForeachEventProxy(final ForeachEventType foreach,
		    final ForeachProxyFactory factory)
    throws ModelException
  {
    super(foreach, factory);
  }


  //#########################################################################
  //# Overrides for abstract base class ForeachProxy
  void storeBodyElement(final ForeachType foreach, final ElementType body)
  {
    final ForeachEventType foreachevent = (ForeachEventType) foreach;
    final EventListType events = (EventListType) body;
    foreachevent.setEventList(events);
  }

}
