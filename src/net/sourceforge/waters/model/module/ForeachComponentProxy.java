//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ForeachComponentProxy
//###########################################################################
//# $Id: ForeachComponentProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ComponentListType;
import net.sourceforge.waters.xsd.module.ForeachComponentType;
import net.sourceforge.waters.xsd.module.ForeachType;


/**
 * <P>A foreach construct for module components.</P>
 *
 * <P>A foreach component construct that occurs in the <I>component list</I>
 * of a module. The entries in its body can be of the following types.</P>
 *
 * <UL>
 * <LI>{@link SimpleComponentProxy}</LI>
 * <LI>{@link InstanceProxy}</LI>
 * <LI>{@link ForeachComponentProxy}</LI>
 * </UL>
 *
 * @see ModuleProxy#getComponentList()
 *
 * @author Robi Malik
 */

public class ForeachComponentProxy extends ForeachProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new foreach component construct without guard and an empty body.
   * @param  dummy       The name of the dummy variable.
   * @param  range       The range of iteration.
   */
  public ForeachComponentProxy(final String dummy,
			       final SimpleExpressionProxy range)
  {
    super(dummy, range);
  }

  /**
   * Creates a new foreach component construct with an empty body.
   * @param  dummy       The name of the dummy variable.
   * @param  range       The range of iteration.
   * @param  guard       The guard expression.
   */
  public ForeachComponentProxy(final String dummy,
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
  ForeachComponentProxy(final ForeachComponentType foreach,
			final ForeachProxyFactory factory)
    throws ModelException
  {
    super(foreach, factory);
  }


  //#########################################################################
  //# Overrides for abstract base class ForeachProxy
  void storeBodyElement(final ForeachType foreach, final ElementType body)
  {
    final ForeachComponentType foreachcomp = (ForeachComponentType) foreach;
    final ComponentListType comps = (ComponentListType) body;
    foreachcomp.setComponentList(comps);
  }

}
