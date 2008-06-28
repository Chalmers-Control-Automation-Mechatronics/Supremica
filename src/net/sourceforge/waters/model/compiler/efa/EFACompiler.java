//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFACompiler
//###########################################################################
//# $Id: EFACompiler.java,v 1.1 2008-06-28 02:01:49 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * <P>The second pass of the compiler.</P>
 *
 * <P>This compiler accepts a module ({@link ModuleProxy}) as input and
 * produces another module as output. It expands all guard/action blocks by
 * partitioning the events, and replaces all variables by simple
 * components. Event arrays, aliases, foreach constructs, and
 * instantiations are not allowed in the input; these should be expanded by
 * a previous call the the module instance compiler ({@link
 * net.sourceforge.waters.model.compiler.ModuleInstanceCompiler
 * ModuleInstanceCompiler}).</P>
 *
 * <P>Th EFA compiler ensures that the resultant module only contains
 * nodes of the following types.</P>
 * <UL>
 * <LI>{@link EventDeclProxy}, where only simple events are defined,
 *     i.e., the list of ranges is guaranteed to be empty;</LI>
 * <LI>{@link SimpleComponentProxy};</LI>
 * </UL>
 *
 * <P><STRONG>Algorithm</STRONG></P>
 *
 * <P>The EFA compiler proceeds in four passes.</P>
 *
 * <OL>
 * <LI>Identify all components (simple or variable) and their state
 *     space.</LI>
 * <LI>Identify the event variable set and the set of checked components for
 *     each event.
 *     <UL>
 *     <LI>The event variable set consists of the set of all variables whose
 *         value may change if an event occurs. It can be computed in two
 *         different ways, depending on the configuration.<BR>
 *         In <CODE>AUTOMATON_ALPHABET</CODE> mode, the event variable set
 *         of an event is the set of all the variables updated in some
 *         simple component using the event.<BR>
 *         In <CODE>EVENT_ALPHABET</CODE> mode, the event variable set
 *         of an event is the set of all the variables updated in some
 *         guard/action block whose edge includes the event.</LI>
 *     <LI>Independently of the event variable set, the set of checked
 *         components for an event consists of all the components
 *         (variable or simple) whose state value will be considered
 *         when partitioning the event.<BR>
 *         It contains all simple components using a given event, provided
 *         that the event is associated with at least two different
 *         guard/action blocks in that component. If all guard/action
 *         blocks in a component are equal, the associated state-update
 *         relation must be added to the state update relation of the
 *         event, and then the component will simply use all the event
 *         labels generated for the event.<BR>
 *         It also contains all the variables&nbsp;<I>x</I> in the event
 *         variable set, unless the following condition is satisfied. If in
 *         every automaton using the event, all the guards can be written
 *         as <CODE>a&nbsp;&amp;&nbsp;b(</CODE><I>x</I><CODE>)</CODE> where
 *         <CODE>a</CODE> does not depend on&nbsp;<I>x</I>, and
 *         <CODE>b(</CODE><I>x</I><CODE>)</CODE> is an expression that
 *         depends on no variables except&nbsp;<I>x</I>, an is the same for
 *         all guards associated with the given event in the automaton, then
 *         the variable&nbsp;<I>x</I> is not a checked component for that
 *         event. In this case the update operation for the
 *         variable&nbsp;<I>x</I> on the given event is the conjunction of
 *         all the terms <CODE>b(</CODE><I>x</I><CODE>)</CODE> and can be
 *         implemented separately.</LI>
 *     </UL>
 * <LI>Compute normalised state-update relations.<BR>
 *     For each event, iterate over all combinations of values of the
 *     checked components and compute the (nondeterministic set of)
 *     successor states. (Under the current syntax, where only action
 *     functions are permitted, the successor state sets can be represented
 *     as a Cartesian product of state sets of the checked components; this
 *     will change once arbitrary relations are allowed as guards.)<BR>
 *     Each combination of values with a nonempty set of successor states
 *     may warrant an event of its own. But some combinations may be
 *     merged. If, for some component&nbsp;<I>c</I>, there are two or more
 *     state combinations that agree on the current and successor states of
 *     all components except&nbsp;<I>c</I>, then these two or more state
 *     combinations can be represented by the same event label in the
 *     output.</LI>
 * <LI>Create partitioned events and build output automata.</LI>
 * </OL>
 *
 * @author Robi Malik
 */

public class EFACompiler extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  public EFACompiler(final ModuleProxyFactory factory,
                     final SourceInfoBuilder builder,
                     final ModuleProxy module)
  {                     
    mInputModule = module;
    mSourceInfoBuilder = builder;
    mFactory = factory;
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile()
    throws EvalException
  {
    try {
      return visitModuleProxy(mInputModule);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      ;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public EdgeProxy visitEdgeProxy(final EdgeProxy edge)
    throws VisitorException
  {
    return null;
  }

  public Object visitEventDeclProxy(final EventDeclProxy decl)
    throws VisitorException
  {
    return null;
  }

  public Object visitEventListExpressionProxy
    (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    return null;
  }

  public GraphProxy visitGraphProxy(final GraphProxy graph)
    throws VisitorException
  {
    return null;
  }

  public GroupNodeProxy visitGroupNodeProxy(final GroupNodeProxy group)
    throws VisitorException
  {
    return null;
  }

  public Object visitGuardActionBlockProxy(final GuardActionBlockProxy ga)
    throws VisitorException
  {
    return null;
  }

  public Object visitIdentifierProxy(final IdentifierProxy ident)
    throws VisitorException
  {
    return null;
  }

  public Object visitLabelBlockProxy(final LabelBlockProxy block)
    throws VisitorException
  {
    return null;
  }

  public ModuleProxy visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    return null;
  }

  public SimpleComponentProxy visitSimpleComponentProxy
    (final SimpleComponentProxy comp)
    throws VisitorException
  {
    return null;
  }

  public SimpleExpressionProxy visitSimpleExpressionProxy
    (final SimpleExpressionProxy expr)
    throws VisitorException
  {
    return null;
  }

  public SimpleNodeProxy visitSimpleNodeProxy(final SimpleNodeProxy node)
    throws VisitorException
  {
    return null;
  }

  public Object visitVariableComponentProxy(final VariableComponentProxy var)
    throws VisitorException
  {
    return null;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final ModuleProxy mInputModule;

}
