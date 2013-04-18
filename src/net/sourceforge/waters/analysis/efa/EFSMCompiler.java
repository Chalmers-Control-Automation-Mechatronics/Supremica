//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.TObjectIntHashMap;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.compiler.efa.EFAGuardCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;


/**
 * <P>The second pass of the compiler.</P>
 *
 * <P>This compiler accepts a module ({@link ModuleProxy}) as input and
 * produces another module as output. It expands all guard/action blocks by
 * partitioning the events, and replaces all variables by simple
 * components. Event arrays, aliases, foreach constructs, and
 * instantiations are not allowed in the input; these should be expanded by
 * a previous call the the module instance compiler ({@link
 * net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler
 * ModuleInstanceCompiler}).</P>
 *
 * <P>The EFA compiler ensures that the resultant module only contains
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
 * <LI>Collect and normalise guards, and identify the event variable set
 *     for each event.<BR>
 *     The event variable set consists of the set of all variables whose
 *     value may change if an event occurs. It can be computed in two
 *     different ways, depending on the configuration.<BR>
 *     In <CODE>AUTOMATON_ALPHABET</CODE> mode, the event variable set of
 *     an event is the set of all the variables updated in some simple
 *     component using the event.<BR>
 *     In <CODE>EVENT_ALPHABET</CODE> mode, the event variable set of an
 *     event is the set of all the variables updated in some guard/action
 *     block whose edge includes the event.</LI>
 * <LI>Compute event partitionings.</LI>
 * <LI>Build output automata.</LI>
 * </OL>
 *
 * @author Robi Malik
 */

public class EFSMCompiler
{

  //#########################################################################
  //# Constructors
  public EFSMCompiler(final ModuleProxyFactory factory,
                      final SourceInfoBuilder builder,
                      final ModuleProxy module)
  {
    mFactory = factory;
    mSourceInfoBuilder = builder;
    mOperatorTable = CompilerOperatorTable.getInstance();
    mTrueGuard = new ConstraintList();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mOperatorTable);
    mInputModule = module;
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    mModuleContext = new ModuleBindingContext(module);
    mGlobalVariableMap =
      new ProxyAccessorHashMap<IdentifierProxy,EFSMVariable>(eq);
  }


  //#########################################################################
  //# Invocation
  public EFSMSystem compile()
    throws EvalException
  {
    try {
      // Pass 1 ...
      final Pass1Visitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      // Pass 2 ...
      final Pass2Visitor pass2 = new Pass2Visitor();
      mInputModule.acceptVisitor(pass2);
      // Pass 3 ...
      return null;
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    }
  }


  //#########################################################################
  //# Configuration




  //#########################################################################
  //# Auxiliary Methods


  //#########################################################################
  //# Inner Class Pass1Visitor
  private class Pass1Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private Pass1Visitor()
    {
      final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
      mGlobalEventsMap =
        new ProxyAccessorHashMap<IdentifierProxy,SimpleComponentProxy>(eq);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public List<SimpleIdentifierProxy> visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final LabelBlockProxy bEvents = graph.getBlockedEvents();
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitLabelBlockProxy(bEvents);
      visitCollection(edges);
      return null;
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final LabelBlockProxy label = edge.getLabelBlock();
      visitLabelBlockProxy(label);
      return null;
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy label)
      throws VisitorException
    {
      final List<Proxy> list = label.getEventIdentifierList();
      visitCollection(list);
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      final SimpleComponentProxy comp = mGlobalEventsMap.get(ident);
      if (comp == null) {
        mGlobalEventsMap.putByProxy(ident, mCurrentComponent);
      } else if (comp != mCurrentComponent) {
        final SharedEventException exception =
          new SharedEventException(ident, comp, mCurrentComponent);
        throw wrap(exception);
      }
      return null;
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      visitCollection(components);
      return null;
    }

    @Override
    public CompiledRange visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      mCurrentComponent = comp;
      final GraphProxy graph = comp.getGraph();
      visitGraphProxy(graph);
      return null;
    }

    //#######################################################################
    //# Data Members
    private final ProxyAccessorMap<IdentifierProxy,SimpleComponentProxy> mGlobalEventsMap;
    private SimpleComponentProxy mCurrentComponent;
  }


  //#########################################################################
  //# Inner Class Pass2Visitor
  /**
   * The visitor implementing the second pass of EFA compilation.
   */
  private class Pass2Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      visitCollection(components);
      return null;
    }

    @Override
    public EFSMVariable visitVariableComponentProxy
      (final VariableComponentProxy var)
    throws VisitorException
    {
      try {
        final SimpleExpressionProxy expr = var.getType();
        visitSimpleExpressionProxy(expr);
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(expr, null);
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(value);
        final EFSMVariable EFSMvar= new EFSMVariable(var, range);
        final IdentifierProxy ident = var.getIdentifier();
        mGlobalVariableMap.putByProxy(ident, EFSMvar);
        return EFSMvar;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitEnumSetExpressionProxy(final EnumSetExpressionProxy expr)
      throws VisitorException
    {
      try {
        for (final SimpleIdentifierProxy ident: expr.getItems()) {
          mModuleContext.insertEnumAtom(ident);
        }
        return null;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }
  }


  //#########################################################################
  //# Inner Class Pass3Visitor
  /**
   * The visitor implementing the second pass of EFA compilation.
   */
  @SuppressWarnings("unused")
  private class Pass3Visitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Constructor
    private Pass3Visitor()
    {
      mGuardCompiler = new EFAGuardCompiler(mFactory, mOperatorTable);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final List<Proxy> components = module.getComponentList();
      visitCollection(components);
      return null;
    }

    @Override
    public Object visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
    throws VisitorException
    {
      mNumberOfStates = 0;
      final GraphProxy graph = comp.getGraph();
      return visitGraphProxy(graph);
    }

    @Override
    public Object visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      final Collection<NodeProxy> nodes = graph.getNodes();
      visitCollection(nodes);
      final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
      final Collection<EdgeProxy> edges = graph.getEdges();
      mGuardActionBlockMap =
        new ProxyAccessorHashMap<GuardActionBlockProxy,ConstraintList>(eq,edges.size());
      mEventCodeMap =
        new TObjectIntHashMap<ConstraintList>(edges.size());
      visitCollection(edges);
      return null;
    }

    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final GuardActionBlockProxy update = edge.getGuardActionBlock();
      if (update!=null) {
        visitGuardActionBlockProxy(update);
      }
      return null;
    }

    @Override
    public Object visitGuardActionBlockProxy(final GuardActionBlockProxy update)
      throws VisitorException
    {
      try {
        final ConstraintList list = mGuardCompiler.getCompiledGuard(update);
        return null;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      mNumberOfStates++;
      return null;
    }

    //#######################################################################
    //# Data Members
    private int mNumberOfStates;
    private final EFAGuardCompiler mGuardCompiler;
  }

  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  @SuppressWarnings("unused")
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final CompilerOperatorTable mOperatorTable;
  @SuppressWarnings("unused")
  private final ConstraintList mTrueGuard;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final ModuleProxy mInputModule;

  private final ModuleBindingContext mModuleContext;
  private final ProxyAccessorMap<IdentifierProxy,EFSMVariable> mGlobalVariableMap;
  @SuppressWarnings("unused")
  private ProxyAccessorMap<GuardActionBlockProxy,ConstraintList> mGuardActionBlockMap;
  @SuppressWarnings("unused")
  private TObjectIntHashMap<ConstraintList> mEventCodeMap;
}
