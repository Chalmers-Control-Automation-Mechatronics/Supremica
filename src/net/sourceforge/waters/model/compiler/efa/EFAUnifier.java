//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFACompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.AbortableCompiler;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.constraint.ConstraintPropagator;
import net.sourceforge.waters.model.compiler.context.CompiledEnumRange;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfoBuilder;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
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

public class EFAUnifier extends AbortableCompiler
{

  //#########################################################################
  //# Constructors
  public EFAUnifier(final ModuleProxyFactory factory,
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
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
  }


  //#########################################################################
  //# Invocation
  public ModuleProxy compile()
    throws EvalException
  {
    try {
      mRootContext = new EFAModuleContext(mInputModule);
      // Pass 1 ...
      final Pass1Visitor pass1 = new Pass1Visitor();
      mInputModule.acceptVisitor(pass1);
      // Pass 2 ...
      mPropagator =
        new ConstraintPropagator(mFactory, mOperatorTable, mRootContext);
      // Pass 3 ...
      return null;
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      mRootContext = null;
    }
  }


  //#########################################################################
  //# Configuration



  //#########################################################################
  //# Auxiliary Methods

  @SuppressWarnings("unused")
  private void addSourceInfo(final Proxy target, final Proxy source)
  {
    if (mSourceInfoBuilder != null) {
      mSourceInfoBuilder.add(target, source);
    }
  }


  //#########################################################################
  //# Inner Class Pass1Visitor
  /**
   * The visitor implementing the first pass of EFA compilation. It
   * initialises the variables map {@link #mRootContext} and associates
   * the identifier of each simple or variable component with a {@link
   * EFAVariable} object that contains the range of possible state values
   * of that component.
   */
  private class Pass1Visitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public List<SimpleIdentifierProxy> visitGraphProxy(final GraphProxy graph)
      throws VisitorException
    {
      try {
        final Collection<NodeProxy> nodes = graph.getNodes();
        final int size = nodes.size();
        mCurrentRange = new ArrayList<SimpleIdentifierProxy>(size);
        visitCollection(nodes);
        return mCurrentRange;
      } finally {
        mCurrentRange = null;
      }
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
    public Object visitNodeProxy(final NodeProxy node)
    {
      return null;
    }

    @Override
    public CompiledRange visitSimpleComponentProxy
      (final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final GraphProxy graph = comp.getGraph();
        final List<SimpleIdentifierProxy> list = visitGraphProxy(graph);
        final CompiledRange range = new CompiledEnumRange(list);
        mRootContext.createVariables(comp, range, mFactory, mOperatorTable);
        return range;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public IdentifierProxy visitSimpleNodeProxy(final SimpleNodeProxy node)
      throws VisitorException
    {
      try {
        checkAbortInVisitor();
        final String name = node.getName();
        final SimpleIdentifierProxy ident =
          mFactory.createSimpleIdentifierProxy(name);
        mRootContext.insertEnumAtom(ident);
        mCurrentRange.add(ident);
        return ident;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public CompiledRange visitVariableComponentProxy
      (final VariableComponentProxy var)
      throws VisitorException
    {
      try {
        final SimpleExpressionProxy expr = var.getType();
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.eval(expr, mRootContext);
        final CompiledRange range =
          mSimpleExpressionCompiler.getRangeValue(value);
        mRootContext.createVariables(var, range, mFactory, mOperatorTable);
        return range;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private List<SimpleIdentifierProxy> mCurrentRange;
  }

  //#########################################################################
  //# Inner Class
  @SuppressWarnings("unused")
  private class ModuleEventInfo
  {
    //#######################################################################
    //# Constructor
    private ModuleEventInfo(final IdentifierProxy eventName)
    {
      mEventName = eventName;
      mMap = new HashMap<>();
      mList = new ArrayList<>();
      mConstraintMap = new HashMap<>();
    }

    //#######################################################################
    //# Simple Access
    private void addUpdate(final SimpleComponentProxy automaton,
                           final ConstraintList update)
    {
      EFAEventInfo info = mMap.get(automaton);
      if (info == null) {
        info = new EFAEventInfo();
        mMap.put(automaton, info);
        mList.add(info);
      }
      info.addUpdate(update);
    }

    private void combineUpdates() throws EvalException
    {
      combineUpdates(new ConstraintList(), 0);
    }

    private void combineUpdates(final ConstraintList oldUpdate, final int index)
      throws EvalException
    {
      if (index < mList.size()) {
        final EFAEventInfo info = mList.get(index);
        for (final ConstraintList update : info) {
          final List<SimpleExpressionProxy> constraints =
            new ArrayList<>(oldUpdate.size()+update.size());
          constraints.addAll(oldUpdate.getConstraints());
          constraints.addAll(update.getConstraints());
          final ConstraintList newUpdate = new ConstraintList(constraints);
          combineUpdates(newUpdate, index + 1);
        }
      } else {
        mPropagator.init(oldUpdate);
        mPropagator.propagate();
        if (!mPropagator.isUnsatisfiable()) {
          final ConstraintList result = mPropagator.getAllConstraints();
          IdentifierProxy ident = mConstraintMap.get(result);
          if (ident == null){
            final String name =
              mEventName.toString() + ":" + mConstraintMap.size();
            ident = mFactory.createSimpleIdentifierProxy(name);
            mConstraintMap.put(result, ident);
          }
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final IdentifierProxy mEventName;
    private final Map<SimpleComponentProxy, EFAEventInfo> mMap;
    private final List<EFAEventInfo> mList;
    private final Map<ConstraintList, IdentifierProxy> mConstraintMap;
  }


  //#########################################################################
  //# Inner Class
  private static class EFAEventInfo implements Iterable<ConstraintList>
  {
    //#######################################################################
    //# Constructor
    private EFAEventInfo()
    {
      mUpdates = new ArrayList<>();
    }

    //#######################################################################
    //# Simple Access
    @SuppressWarnings("unused")
    private int getNumberOfUpdates()
    {
      return mUpdates.size();
    }

    private void addUpdate(final ConstraintList update)
    {
      mUpdates.add(update);
    }

    @SuppressWarnings("unused")
    private ConstraintList getUpdate(final int index)
    {
      return mUpdates.get(index);
    }

    @SuppressWarnings("unused")
    private int getIndexOfUpdate(final ConstraintList update)
    {
      return mUpdates.indexOf(update);
    }

    //#######################################################################
    //# Interface java.lang.Iterable<ConstraintList>
    @Override
    public Iterator<ConstraintList> iterator()
    {
      return mUpdates.iterator();
    }

    //#######################################################################
    //# Data Members
    private final List<ConstraintList> mUpdates;
  }

  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final SourceInfoBuilder mSourceInfoBuilder;
  private final CompilerOperatorTable mOperatorTable;
  @SuppressWarnings("unused")
  private final ConstraintList mTrueGuard;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final ModuleProxy mInputModule;

  private EFAModuleContext mRootContext;
  private ConstraintPropagator mPropagator;

}
