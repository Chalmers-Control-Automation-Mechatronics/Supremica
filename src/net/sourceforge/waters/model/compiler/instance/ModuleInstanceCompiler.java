//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.analysis.hisc.HISCAttributeFactory;
import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.EvalAbortException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SingleBindingContext;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoCloner;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.MultiEvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

import org.xml.sax.SAXException;


/**
 * <P>A compiler tool to expand bindings, foreach blocks, and instantiations
 * in a module.</P>
 *
 * <P>The instance compiler accepts a module ({@link ModuleProxy}) as input
 * and produces another module as output. It expands all aliases, foreach
 * constructs, and instantiations. Event arrays as well as component and
 * variable arrays are enumerated explicitly. Variable components are
 * preserved in the output, with all guards and actions simplified by
 * substituting values obtained from aliasing or instantiation.</P>
 *
 * <P>It is ensured that the resultant module only contains
 * objects of the following types:</P>
 * <UL>
 * <LI>{@link EventDeclProxy}, where only simple events are defined,
 *     i.e., the list of ranges is guaranteed to be empty;</LI>
 * <LI>{@link SimpleComponentProxy};</LI>
 * <LI>{@link VariableComponentProxy}.</LI>
 * </UL>
 *
 * <P>The instance compiler is used as the first stage of the {@link
 * ModuleCompiler} to translate a {@link ModuleProxy} into a {@link
 * ProductDESProxy}, but it can also be used as a stand-alone component
 * to remove foreach blocks and instantiation from a module. To do so,
 * the user first creates an instance of this class and configures it as
 * needed. Then compilation is started using the {@link #compile()} or
 * {@link #compile(List)} method, which returns the instantiated module.
 * The following code can be used to instantiate a given <CODE>module</CODE>.</P>
 *
 * <P>
 * <CODE>try {</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link ModuleProxyFactory} factory =
 *   {@link ModuleElementFactory}.{@link ModuleElementFactory#getInstance()
 *   getInstance}();</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link OperatorTable} optable =
 *   {@link CompilerOperatorTable}.{@link CompilerOperatorTable#getInstance()
 *   getInstance}();</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link ProxyUnmarshaller}&lt;{@link ModuleProxy}&gt; unmarshaller =
 *   new {@link JAXBModuleMarshaller#JAXBModuleMarshaller(ModuleProxyFactory,OperatorTable)
 *   JAXBModuleMarshaller}(factory, optable);</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link DocumentManager} manager =
 *   new {@link DocumentManager#DocumentManager() DocumentManager}();</CODE><BR>
 * <CODE>&nbsp;&nbsp;manager.{@link DocumentManager#registerUnmarshaller(ProxyUnmarshaller)
 *   registerUnmarshaller}(unmarshaller);</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link ModuleInstanceCompiler} compiler =
 *   new {@link ModuleInstanceCompiler#ModuleInstanceCompiler(DocumentManager,
 *   ModuleProxyFactory, ModuleProxy) ModuleInstanceCompiler}(manager, factory,
 *   module);</CODE><BR>
 * <CODE>&nbsp;&nbsp;// </CODE>configure compiler here if needed ...<BR>
 * <CODE>&nbsp;&nbsp;{@link ModuleProxy} instantiatedModule =
 *   compiler.{@link #compile()};</CODE><BR>
 * <CODE>&nbsp;&nbsp;// </CODE>instantiation successful, result in
 *   <CODE>instantiatedModule</CODE> ...<BR>
 * <CODE>} catch ({@link EvalException} exception) {</CODE><BR>
 * <CODE>&nbsp;&nbsp;// </CODE>module has errors ...<BR>
 * <CODE>} catch ({@link JAXBException} | {@link SAXException} exception) {</CODE><BR>
 * <CODE>&nbsp;&nbsp;// </CODE>error setting up XML parsers - should not happen ...<BR>
 * <CODE>}</CODE></P>
 *
 * @author Robi Malik, Roger Su
 */

public class ModuleInstanceCompiler extends DefaultModuleProxyVisitor
                                    implements Abortable
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new module instance compiler for the given module.
   * @param manager  The document manager used to load additional modules
   *                 referred to by the module being being compiled.
   * @param factory  The factory used to create the output module.
   * @param module   The module to be compiled. This module is passed by
   *                 reference into the compiler.
   */
  public ModuleInstanceCompiler(final DocumentManager manager,
                                final ModuleProxyFactory factory,
                                final ModuleProxy module)
  {
    this(manager, factory, new CompilationInfo(), module);
  }

  /**
   * Creates a new module instance compiler for the given module.
   * @param manager  The document manager used to load additional modules
   *                 referred to by the module being being compiled.
   * @param factory  The factory used to create the output module.
   * @param compilationInfo  Information structure used to track error
   *                 locations and to record how components in the output
   *                 module are linked to the input module. This is passed
   *                 in by the {@link ModuleCompiler} to track associations
   *                 across several stages of compilations.
   * @param module   The module to be compiled. This module is passed by
   *                 reference into the compiler.
   */
  public ModuleInstanceCompiler(final DocumentManager manager,
                                final ModuleProxyFactory factory,
                                final CompilationInfo compilationInfo,
                                final ModuleProxy module)
  {
    mDocumentManager = manager;
    mFactory = factory;
    mCompilationInfo = compilationInfo;
    mCloner = new SourceInfoCloner(factory, compilationInfo);
    mOperatorTable = CompilerOperatorTable.getInstance();
    mEquality = new ModuleEqualityVisitor(false);
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(mFactory, mCompilationInfo,
                                   mOperatorTable);
    mPrimeSearcher = new PrimeSearcher();
    mNameCompiler = new NameCompiler();
    mIndexAdder = new IndexAdder();
    mNameSpaceVariablesContext = new NameSpaceVariablesContext();
    mInputModule = module;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }


  //#########################################################################
  //# Invocation
  /**
   * Instantiates the input module using default values for all parameter
   * bindings.
   * @return The instantiated module.
   * @throws EvalException to indicate any syntactical or semantical
   *         errors encountered during compilation.
   */
  public ModuleProxy compile()
    throws EvalException
  {
    return compile(null);
  }

  /**
   * Instantiates the input module using the given parameter bindings.
   * @param  bindings  List of parameter bindings to supply values for
   *                   parameters specified in the module. A module's
   *                   event declarations ({@link EventDeclProxy}) and
   *                   constant alias declarations ({@link ConstantAliasProxy})
   *                   may be declared as parameters, in which case the
   *                   bindings can be used to supply values to replace these
   *                   parameters.
   * @return The instantiated module.
   * @throws EvalException to indicate any syntactical or semantical
   *         errors encountered during compilation.
   */
  public ModuleProxy compile(final List<ParameterBindingProxy> bindings)
    throws EvalException
  {
    try {
      mHasEFAElements = false;
      mContext = new ModuleBindingContext(mInputModule);
      mNameSpace = new CompiledNameSpace();
      mCompiledEvents = new TreeSet<>();
      mCompiledComponents = new LinkedList<>();
      if (bindings != null) {
        mParameterMap = new TreeMap<>();
        visitCollection(bindings);
      }
      visitModuleProxy(mInputModule);
      final String name = mInputModule.getName();
      final String comment = mInputModule.getComment();
      return mFactory.createModuleProxy(name, comment, null, null,
                                        mCompiledEvents, null,
                                        mCompiledComponents);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof EvalException) {
        throw (EvalException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      mContext = null;
      mNameSpace = null;
      mCompiledEvents = null;
      mCompiledComponents = null;
      mParameterMap = null;
    }
  }

  /**
   * Returns whether the last instantiation run has found any variables or
   * guard/action blocks in the input module.
   */
  public boolean getHasEFAElements()
  {
    return mHasEFAElements;
  }


  //##########################################################################
  //# Configuration
  /**
   * Returns whether compiler optimisation is enabled.
   * @see #setOptimizationEnabled(boolean)
   */
  public boolean isOptimizationEnabled()
  {
    return mIsOptimizationEnabled;
  }

  /**
   * Enables or disabled compiler optimisation.
   * If enabled, the compiler may perform several optimisation steps to
   * remove selfloops and unused events or automata from the output.
   * This option is enabled by default.
   */
  public void setOptimizationEnabled(final boolean enable)
  {
    mIsOptimizationEnabled = enable;
  }

  /**
   * Gets the set of enabled property names.
   * @see #setEnabledPropertyNames(Collection)
   */
  public Collection<String> getEnabledPropertyNames()
  {
    return mEnabledPropertyNames;
  }

  /**
   * Sets the set of enabled property names.
   * If non-null, the compiler will check all property components to be
   * generated (automata of type {@link ComponentKind#PROPERTY}) against
   * the names in this collection, and suppress any that are not listed.
   * The default for this option is <CODE>null</CODE>, which means to
   * include all properties in the output.
   */
  public void setEnabledPropertyNames(final Collection<String> names)
  {
    mEnabledPropertyNames = names;
  }

  /**
   * Gets the set of enabled proposition names.
   * @see #setEnabledPropositionNames(Collection)
   */
  public Collection<String> getEnabledPropositionNames()
  {
    return mEnabledPropositionNames;
  }

  /**
   * If non-null, the compiler will check all proposition events to be
   * generated (events of type {@link EventKind#PROPOSITION}) against
   * the names in this collection, and suppress any that are not listed.
   * The default for this option is <CODE>null</CODE>, which means to
   * include all propositions in the output.
   */
  public void setEnabledPropositionNames(final Collection<String> names)
  {
    mEnabledPropositionNames = names;
  }

  /**
   * Gets the current setting for partial compilation of HISC subsystems.
   * @see #setHISCCompileMode(HISCCompileMode)
   */
  public HISCCompileMode getHISCCompileMode()
  {
    return mHISCCompileMode;
  }

  /**
   * Configures the compiler for partial compilation of HISC subsystems.
   * The default for this option is {@link HISCCompileMode#NOT_HISC},
   * which means that all components of a module hierarchy are compiled as a
   * standard flat discrete event system.
   * @see HISCCompileMode
   */
  public void setHISCCompileMode(final HISCCompileMode mode)
  {
    mHISCCompileMode = mode;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Object visitCollection(final Collection<? extends Proxy> collection)
    throws VisitorException
  {
    boolean hasExceptions = false;
    for (final Proxy proxy : collection) {
      try {
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          hasExceptions = true;
          mCompilationInfo.raiseInVisitor((EvalException) cause);
        } else {
          throw exception;
        }
      }
    }
    if (hasExceptions) {
      throw wrap(mCompilationInfo.getExceptions());
    }
    return null;
  }

  @Override
  public SimpleExpressionProxy visitConstantAliasProxy
    (final ConstantAliasProxy alias)
    throws VisitorException
  {
    try {
      checkAbort();
      final IdentifierProxy ident = alias.getIdentifier();
      final ScopeKind scope = alias.getScope();
      final CompiledParameterBinding binding =
        getParameterBinding(ident, scope);
      final SimpleExpressionProxy value;
      if (binding == null) {
        final SimpleExpressionProxy defaultExpr =
          (SimpleExpressionProxy) alias.getExpression();
        value = mSimpleExpressionCompiler.eval(defaultExpr, mContext);
      } else {
        value = binding.getSimpleValue();
      }
      final ModuleBindingContext context = mContext.getModuleBindingContext();
      context.insertBinding(ident, value);
      return value;
    } catch (final EvalException exception) {
      exception.provideLocation(alias);
      throw wrap(exception);
    }
  }

  @Override
  public EdgeProxy visitEdgeProxy(final EdgeProxy edge)
    throws VisitorException
  {
    try {
      mCurrentEdge = edge;
      final LabelBlockProxy labels0 = edge.getLabelBlock();
      final CompiledEventList events = visitLabelBlockProxy
        (labels0, EventKindMask.TYPEMASK_EVENT);
      final GuardActionBlockProxy ga0 = edge.getGuardActionBlock();
      GuardActionBlockProxy ga1 = null;
      if (ga0 != null) {
        ga1 = visitGuardActionBlockProxy(ga0);
        final List<SimpleExpressionProxy> guards = ga1.getGuards();
        final List<BinaryExpressionProxy> actions = ga1.getActions();
        if (guards.isEmpty()) {
          if (actions.isEmpty()) {
            ga1 = null;
          }
        } else {
          final Iterator<SimpleExpressionProxy> iter = guards.iterator();
          final SimpleExpressionProxy guard = iter.next();
          if (!iter.hasNext() &&
              mSimpleExpressionCompiler.isAtomicValue(guard, mContext)) {
            final boolean value =
              mSimpleExpressionCompiler.getBooleanValue(guard);
            if (!value) {
              addAdditionalBlockedEvents(events);
              return null;
            } else if (actions.isEmpty()) {
              ga1 = null;
            }
          }
        }
      }
      mHasEFAElements |= ga1 != null;
      final NodeProxy source0 = edge.getSource();
      final NodeProxy source1 = mNodeMap.get(source0);
      final NodeProxy target0 = edge.getTarget();
      final NodeProxy target1 = mNodeMap.get(target0);
      final LabelBlockProxy labels1 = createLabelBlock(events);
      final EdgeProxy compiled = mFactory.createEdgeProxy
        (source1, target1, labels1, ga1, null, null, null);
      mCompilationInfo.add(compiled, edge);
      mCurrentEdges.add(compiled);
      return compiled;
    } catch (final EvalException exception) {
      throw wrap(exception);
    } finally {
      mCurrentEdge = null;
    }
  }

  @Override
  public CompiledEvent visitEventAliasProxy(final EventAliasProxy alias)
    throws VisitorException
  {
    try {
      final IdentifierProxy ident = alias.getIdentifier();
      final IdentifierProxy compiledname = mNameCompiler.compileName(ident);
      final ExpressionProxy expr = alias.getExpression();
      final Object value = expr.acceptVisitor(this);
      if (!(value instanceof CompiledEvent)) {
        throw new TypeMismatchException(expr, "event");
      }
      final CompiledEvent event = (CompiledEvent) value;
      mNameSpace.addEvent(compiledname, event);
      return event;
    } catch (final EvalException exception) {
      exception.provideLocation(alias);
      throw wrap(exception);
    }
  }

  @Override
  public CompiledEvent visitEventDeclProxy(final EventDeclProxy decl)
    throws VisitorException
  {
    try {
      checkAbort();
      final IdentifierProxy ident = decl.getIdentifier();
      final ScopeKind scope = decl.getScope();
      final CompiledParameterBinding binding =
        getParameterBinding(ident, scope);
      final List<SimpleExpressionProxy> declRanges = decl.getRanges();
      CompiledEvent event = binding == null ? null : binding.getEventValue();
      if (event == null) {
        // Declare a new event.
        final int numranges = declRanges.size();
        final List<CompiledRange> ranges =
          new ArrayList<CompiledRange>(numranges);
        for (final SimpleExpressionProxy expr : declRanges) {
          final SimpleExpressionProxy value =
            mSimpleExpressionCompiler.eval(expr, mContext);
          final CompiledRange range =
            mSimpleExpressionCompiler.getRangeValue(value);
          ranges.add(range);
        }
        final CompiledEventDecl entry =
          new CompiledEventDecl(mNameSpace, decl, ranges);
        event = entry.getCompiledEvent();
        if (!mIsOptimizationEnabled) {
          final Iterable<SingleEventOutput> outputs =
            new EventOutputIterable(event, mCompilationInfo);
          for (final SingleEventOutput output : outputs) {
            final CompiledSingleEvent single = output.getEvent();
            createEventDecl(single);
          }
        }
      } else {
        // Use the event through parameter binding.
        final EventKind kind = decl.getKind();
        final int mask = event.getKindMask();
        if (!EventKindMask.isAssignable(kind, mask) ||
            decl.isObservable() && !event.isObservable()) {
          throw new EventKindException(decl, event);
        }
        final Iterator<SimpleExpressionProxy> declIter = declRanges.iterator();
        final List<CompiledRange> eventRanges = event.getIndexRanges();
        final Iterator<CompiledRange> eventIter = eventRanges.iterator();
        int index = 0;
        while (declIter.hasNext()) {
          if (!eventIter.hasNext()) {
            throw new EventKindException(decl, event, index);
          }
          final SimpleExpressionProxy expr = declIter.next();
          final SimpleExpressionProxy value =
            mSimpleExpressionCompiler.eval(expr, mContext);
          final CompiledRange declRange =
            mSimpleExpressionCompiler.getRangeValue(value);
          final CompiledRange eventRange = eventIter.next();
          if (!declRange.equals(eventRange)) {
            throw new EventKindException(decl, event, index, declRange);
          }
          index++;
        }
      }
      mNameSpace.addEvent(ident, event);
      return event;
    } catch (final EvalException exception) {
      exception.provideLocation(decl);
      throw wrap(exception);
    }
  }

  @Override
  public CompiledEventList visitEventListExpressionProxy
    (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy, EventKindMask.TYPEMASK_ANY);
  }

  public CompiledEventList visitEventListExpressionProxy
    (final EventListExpressionProxy proxy, final int mask)
    throws VisitorException
  {
    try {
      mCurrentEventList = new CompiledEventList(mask);
      final List<Proxy> list = proxy.getEventIdentifierList();
      visitCollection(list);
      return mCurrentEventList;
    } finally {
      mCurrentEventList = null;
    }
  }

  @Override
  public Object visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
  {
    try {
      final BindingContext root = mContext;
      final String name = foreach.getName();
      final List<Proxy> body = foreach.getBody();
      final SimpleExpressionProxy range = foreach.getRange();
      final SimpleExpressionProxy rvalue =
        mSimpleExpressionCompiler.eval(range, mContext);
      final CompiledRange crange =
        mSimpleExpressionCompiler.getRangeValue(rvalue);
      for (final SimpleExpressionProxy item : crange.getValues()) {
        try {
          mContext = new SingleBindingContext(mFactory, name, item, root);
          final SimpleExpressionProxy guard = foreach.getGuard();
          if (guard == null) {
            visitCollection(body);
          } else {
            final SimpleExpressionProxy gvalue =
              mSimpleExpressionCompiler.eval(guard, mContext);
            final boolean gboolean =
              mSimpleExpressionCompiler.getBooleanValue(gvalue);
            if (gboolean) {
              visitCollection(body);
            }
          }
        } finally {
          mContext = root;
        }
      }
      return null;
    } catch (final EvalException exception) {
      throw wrap(exception);
    }
  }

  @Override
  public GraphProxy visitGraphProxy(final GraphProxy graph)
    throws VisitorException
  {
    try {
      final boolean deterministic = graph.isDeterministic();
      final LabelBlockProxy blocked0 = graph.getBlockedEvents();
      if (blocked0 != null) {
        mCurrentBlockedEvents = visitLabelBlockProxy(blocked0);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      final int numnodes = nodes.size();
      mCurrentNodes = new ArrayList<>(numnodes);
      mNodeMap = new HashMap<>(numnodes);
      visitCollection(nodes);
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      mCurrentAlphabet = new ProxyAccessorHashSet<>(eq);
      final Collection<EdgeProxy> edges = graph.getEdges();
      final int numedges = edges.size();
      mCurrentEdges = new ArrayList<>(numedges);
      visitCollection(edges);
      final LabelBlockProxy blocked1 =
        mCurrentBlockedEvents == null ? null :
        createLabelBlock(mCurrentBlockedEvents);
      final GraphProxy compiled = mFactory.createGraphProxy
        (deterministic, blocked1, mCurrentNodes, mCurrentEdges);
      mCompilationInfo.add(compiled, graph);
      return compiled;
    } finally {
      mCurrentNodes = null;
      mNodeMap = null;
      mCurrentAlphabet = null;
      mCurrentBlockedEvents = null;
      mCurrentEdges = null;
    }
  }

  @Override
  public GroupNodeProxy visitGroupNodeProxy(final GroupNodeProxy group)
    throws VisitorException
  {
    final String name = group.getName();
    final PlainEventListProxy props0 = group.getPropositions();
    final CompiledEventList event = visitEventListExpressionProxy
      (props0, EventKindMask.TYPEMASK_PROPOSITION);
    final PlainEventListProxy props1 = createPlainEventList(event);
    final Map<String,String> attribs0 = group.getAttributes();
    final Map<String,String> attribs1 = new HashMap<String,String>(attribs0);
    final Set<NodeProxy> children0 = group.getImmediateChildNodes();
    final int numchildren = children0.size();
    final List<NodeProxy> children1 = new ArrayList<NodeProxy>(numchildren);
    for (final NodeProxy child0 : children0) {
      final NodeProxy child1 = mNodeMap.get(child0);
      if (child1 == null) {
        return null;
      }
      children1.add(child1);
    }
    final GroupNodeProxy compiled =
      mFactory.createGroupNodeProxy(name, props1, attribs1, children1, null);
    mNodeMap.put(group, compiled);
    mCurrentNodes.add(compiled);
    mCompilationInfo.add(compiled, group);
    return compiled;
  }

  @Override
  public GuardActionBlockProxy visitGuardActionBlockProxy
    (final GuardActionBlockProxy ga)
    throws VisitorException
  {
    try
    {
      final List<SimpleExpressionProxy> oldguards = ga.getGuards();
      final int numguards = oldguards.size();
      final List<SimpleExpressionProxy> newguards =
                             new ArrayList<SimpleExpressionProxy>(numguards);

      for (final SimpleExpressionProxy oldguard : oldguards)
      {
        checkAbort();

        if (mPrimeSearcher.containsPrime(oldguard))
        {
          /* Guards with primes are not simplified
           * because they are needed to determine
           * the variable alphabet.
           */
          final SimpleExpressionProxy newguard =
                          (SimpleExpressionProxy) mCloner.getClone(oldguard);
          mCompilationInfo.add(newguard, oldguard);
          newguards.add(newguard);
        }

        else
        {
          final SimpleExpressionProxy newguard =
                                    mSimpleExpressionCompiler.simplify
                                      (oldguard, mNameSpaceVariablesContext);
          if (!mSimpleExpressionCompiler.isAtomicValue(newguard, mContext)) {
            newguards.add(newguard);
          } else if (mSimpleExpressionCompiler.getBooleanValue(newguard)) {
            // True guards are not added.
          } else {
            // If a guard is false, no need for any other guards.
            newguards.clear();
            newguards.add(newguard);
            break;
          }
        }
      }

      final List<BinaryExpressionProxy> oldactions = ga.getActions();
      final int numactions = oldactions.size();
      final List<BinaryExpressionProxy> newactions =
                            new ArrayList<BinaryExpressionProxy>(numactions);

      for (final BinaryExpressionProxy oldaction : oldactions)
      {
        checkAbort();
        final SimpleExpressionProxy newaction =
                                  mSimpleExpressionCompiler.simplify
                                    (oldaction, mNameSpaceVariablesContext);

        if (newaction instanceof BinaryExpressionProxy)
        {
          final BinaryExpressionProxy newbinary =
                                            (BinaryExpressionProxy) newaction;
          newactions.add(newbinary);
        } else {
          throw new TypeMismatchException(oldaction, "ACTION");
        }
      }

      final GuardActionBlockProxy newga =
          mFactory.createGuardActionBlockProxy(newguards, newactions, null);
      mCompilationInfo.add(newga, ga);
      return newga;
    } catch (final EvalException exception) {
      throw wrap(exception);
    }
  }

  @Override
  public Object visitIdentifierProxy(final IdentifierProxy ident)
    throws VisitorException
  {
    try {
      checkAbort();
      // First evaluate all indices,
      final IdentifierProxy newident = mNameCompiler.compileName(ident, false);
      // Then perform a search. The location depends on the context.
      final CompiledEvent event;
      if (mCurrentEventList == null) {
        final SimpleExpressionProxy value =
          mSimpleExpressionCompiler.simplify(newident, mContext);
        if (mSimpleExpressionCompiler.isAtomicValue(value, mContext)) {
          return value;
        }
        final IdentifierProxy ivalue =
          mSimpleExpressionCompiler.getIdentifierValue(value);
        event = mNameSpace.findEvent(ivalue);
      } else {
        event = mNameSpace.findEvent(newident);
        if (isDisabledProposition(event)) {
          return null;
        }
      }
      final SourceInfo info = mCompilationInfo.getSourceInfo(ident);
      final Proxy source = (info == null) ? ident : info.getSourceObject();
      final SourceInfo einfo =
        mCompilationInfo.createSourceInfo(source, mContext);
      final CompiledEvent occ = new CompiledEventOccurrence(event, einfo);
      if (mCurrentEventList != null) {
        mCurrentEventList.addEvent(occ);
      }
      if (mCurrentAlphabet != null) {
        mCurrentAlphabet.addProxy(newident);
      }
      return occ;
    } catch (final EvalException exception) {
      exception.provideLocation(ident);
      throw wrap(exception);
    }
  }

  @Override
  public Object visitInstanceProxy(final InstanceProxy inst)
    throws VisitorException
  {
    // Instance components are only processed in the second pass.
    if (m1stPass) {
      return null;
    }
    checkAbort();
    final BindingContext oldContext = mContext;
    final CompiledNameSpace oldNameSpace = mNameSpace;
    final MultiEvalException oldExceptions = mCompilationInfo.getExceptions();
    switch (mHISCCompileMode) {
    case HISC_LOW:
      return null;
    case HISC_HIGH:
      mHISCCompileMode = HISCCompileMode.HISC_LOW;
      break;
    default:
      break;
    }

    SourceInfo info = null;
    try {
      info = mCompilationInfo.pushParentSourceInfo(inst, mContext);
      final IdentifierProxy ident = inst.getIdentifier();
      final IdentifierProxy suffix = mNameCompiler.compileName(ident);
      final IdentifierProxy fullname =
        mNameSpace.getPrefixedIdentifier(suffix, mFactory);
      mCompilationInfo.add(fullname, ident);
      final List<ParameterBindingProxy> bindings = inst.getBindingList();
      mParameterMap = new TreeMap<String,CompiledParameterBinding>();
      visitCollection(bindings);

      try {
        final ModuleBindingContext root = mContext.getModuleBindingContext();
        final URI uri = root.getModule().getLocation();
        final String filename = inst.getModuleName();
        final ModuleProxy module =
          mDocumentManager.load(uri, filename, ModuleProxy.class);
        mContext = new ModuleBindingContext(module, fullname, info);
        mNameSpace = new CompiledNameSpace(suffix, mNameSpace);
        mCompilationInfo.setExceptions(new MultiEvalException());
        return visitModuleProxy(module);

      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException &&
          !(cause instanceof EvalAbortException)) {
          mCompilationInfo.setExceptions(oldExceptions);
          final EvalException evalCause = (EvalException) cause;
          for (final EvalException ex : evalCause.getAll()) {
            final InstantiationException next =
              new InstantiationException(ex, inst);
            mCompilationInfo.raiseInVisitor(next);
          }
          throw wrap(mCompilationInfo.getExceptions());
        } else {
          throw exception;
        }

      } catch (final IOException | WatersUnmarshalException exception) {
        final InstantiationException next =
          new InstantiationException(exception, inst);
        throw wrap(next);

      } finally {
        mContext = oldContext;
        mNameSpace = oldNameSpace;
        mCompilationInfo.setExceptions(oldExceptions);
        mParameterMap = null;
        if (mHISCCompileMode == HISCCompileMode.HISC_LOW) {
          mHISCCompileMode = HISCCompileMode.HISC_HIGH;
        }
      }
    } finally {
      if (info != null) {
        mCompilationInfo.popParentSourceInfo();
      }
    }
  }

  @Override
  public CompiledEventList visitLabelBlockProxy(final LabelBlockProxy block)
    throws VisitorException
  {
    return visitLabelBlockProxy(block, EventKindMask.TYPEMASK_ANY);
  }

  public CompiledEventList visitLabelBlockProxy
    (final LabelBlockProxy block, final int mask)
    throws VisitorException
  {
    final List<Proxy> list = block.getEventIdentifierList();
    if (list.isEmpty()) {
      final EmptyLabelBlockException exception =
        new EmptyLabelBlockException(block, mCurrentEdge, mCurrentComponent);
      throw wrap(exception);
    } else {
      return visitEventListExpressionProxy(block, mask);
    }
  }

  @Override
  public Object visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    final List<Proxy> parameters = new LinkedList<Proxy>();
    final List<Proxy> nonparameters = new LinkedList<Proxy>();

    for (final ConstantAliasProxy alias : module.getConstantAliasList())
    {
      if (alias.getScope() == ScopeKind.LOCAL)
        nonparameters.add(alias);
      else
        parameters.add(alias);
    }

    for (final EventDeclProxy decl : module.getEventDeclList())
    {
      if (decl.getScope() == ScopeKind.LOCAL)
        nonparameters.add(decl);
      else
        parameters.add(decl);
    }

    visitCollection(parameters);

    if (mParameterMap != null && !mParameterMap.isEmpty())
    {
      // Throw an exception when not all paremeters passed into the module
      // have been consumed, unless compiling in top-level context.
      final CompiledParameterBinding entry =
                                    mParameterMap.values().iterator().next();
      final ParameterBindingProxy binding = entry.getBinding();
      final String name = binding.getName();
      final UndefinedIdentifierException exception =
                new UndefinedIdentifierException(name, "parameter", binding);
      throw wrap(exception);
    }
    mParameterMap = null;

    visitCollection(nonparameters);

    final List<Proxy> aliases = module.getEventAliasList();
    visitCollection(aliases);

    /* All of the variable components are processed before the
     * simple components and instances. Since a 'foreach' component
     * can be of either type, its order is not important.
     */
    final List<Proxy> components = module.getComponentList();
    m1stPass = true;
    visitCollection(components);
    m1stPass = false;
    visitCollection(components);

    return null;
  }

  @Override
  public CompiledParameterBinding visitParameterBindingProxy
    (final ParameterBindingProxy binding)
    throws VisitorException
  {
    final String name = binding.getName();
    final ExpressionProxy expr = binding.getExpression();
    final Object value = expr.acceptVisitor(this);
    final CompiledParameterBinding compiled =
      new CompiledParameterBinding(binding, value);
    mParameterMap.put(name, compiled);
    return compiled;
  }

  @Override
  public SimpleComponentProxy visitSimpleComponentProxy
    (final SimpleComponentProxy comp)
    throws VisitorException
  {
    // Simple components are only processed in the second pass.
    if (m1stPass) return null;

    try {
      mCurrentComponent = comp;
      final IdentifierProxy ident = comp.getIdentifier();
      final IdentifierProxy suffix = mNameCompiler.compileName(ident);
      final IdentifierProxy fullname =
        mNameSpace.getPrefixedIdentifier(suffix, mFactory);
      ComponentKind kind = comp.getKind();
      Map<String,String> attribs = comp.getAttributes();
      if (mHISCCompileMode == HISCCompileMode.HISC_LOW) {
        if (HISCAttributeFactory.isInterface(attribs)) {
          attribs = null;
          kind = ComponentKind.PLANT;
        } else {
          return null;
        }
      }
      if (isDisabledProperty(kind, fullname)) {
        return null;
      }
      final GraphProxy graph = comp.getGraph();
      final GraphProxy newgraph = visitGraphProxy(graph);
      final SimpleComponentProxy newComp =
        mFactory.createSimpleComponentProxy(fullname, kind, newgraph, attribs);
      mNameSpace.addComponent(suffix, newComp);
      mCompiledComponents.add(newComp);
      // TODO Fixing bug: add missing context
      mCompilationInfo.add(newComp, comp, mContext);
      return newComp;
    } catch (final EvalException exception) {
      exception.provideLocation(comp);
      throw wrap(exception);
    } finally {
      mCurrentComponent = null;
    }
  }

  @Override
  public SimpleExpressionProxy visitSimpleExpressionProxy
    (final SimpleExpressionProxy expr)
    throws VisitorException
  {
    try {
      return mSimpleExpressionCompiler.eval(expr, mContext);
    } catch (final EvalException exception) {
      throw wrap(exception);
    }
  }

  @Override
  public SimpleNodeProxy visitSimpleNodeProxy(final SimpleNodeProxy node)
    throws VisitorException
  {
    checkAbort();
    final String name = node.getName();
    final boolean initial = node.isInitial();
    final PlainEventListProxy props0 = node.getPropositions();
    final CompiledEventList event = visitEventListExpressionProxy
      (props0, EventKindMask.TYPEMASK_PROPOSITION);
    final PlainEventListProxy props1 = createPlainEventList(event);
    final Map<String,String> attribs0 = node.getAttributes();
    final Map<String,String> attribs1 = new HashMap<String,String>(attribs0);
    final SimpleNodeProxy compiled =
      mFactory.createSimpleNodeProxy(name, props1, attribs1,
                                     initial, null, null, null);
    mNodeMap.put(node, compiled);
    mCurrentNodes.add(compiled);
    mCompilationInfo.add(compiled, node);
    return compiled;
  }

  @Override
  public VariableComponentProxy visitVariableComponentProxy
    (final VariableComponentProxy var)
    throws VisitorException
  {
    // Variable components are processed in the first pass.
    if (!m1stPass) return null;

    try {
      checkAbort();
      if (mHISCCompileMode == HISCCompileMode.HISC_LOW) {
        return null;
      }
      mHasEFAElements = true;
      final IdentifierProxy ident = var.getIdentifier();
      final IdentifierProxy suffix = mNameCompiler.compileName(ident);
      final IdentifierProxy fullname =
        mNameSpace.getPrefixedIdentifier(suffix, mFactory);
      mCompilationInfo.add(fullname, ident);
      final BindingContext context = new SinglePrefixingContext(suffix);
      final SimpleExpressionProxy expr = var.getType();
      final SimpleExpressionProxy value =
        mSimpleExpressionCompiler.eval(expr, mContext);
      mSimpleExpressionCompiler.getRangeValue(value);
      final SimpleExpressionProxy oldinit = var.getInitialStatePredicate();
      final SimpleExpressionProxy newinit =
        mSimpleExpressionCompiler.simplify(oldinit, context);
      final List<VariableMarkingProxy> oldmarkings = var.getVariableMarkings();
      final List<VariableMarkingProxy> newmarkings =
        new LinkedList<VariableMarkingProxy>();
      for (final VariableMarkingProxy oldmarking : oldmarkings) {
        final IdentifierProxy prop = oldmarking.getProposition();
        final CompiledEvent events =
          (CompiledEvent) visitIdentifierProxy(prop);
        final SimpleExpressionProxy oldpred = oldmarking.getPredicate();
        final SimpleExpressionProxy newpred =
          mSimpleExpressionCompiler.simplify(oldpred, context);
        final Iterable<SingleEventOutput> outputs =
          new EventOutputIterable(events, mCompilationInfo);
        for (final SingleEventOutput output : outputs) {
          final CompiledSingleEvent event = output.getEvent();
          if (event.getKind() != EventKind.PROPOSITION) {
            final int mask = event.getKindMask();
            final EventKindException exception =
              new EventKindException(event, mask);
            exception.provideLocation(prop);
            throw exception;
          }
          final IdentifierProxy newident = createSingleEvent(output);
          final VariableMarkingProxy newmarking =
            mFactory.createVariableMarkingProxy(newident, newpred);
          newmarkings.add(newmarking);
        }
      }
      final VariableComponentProxy newvar =
        mFactory.createVariableComponentProxy(fullname, value,
                                              newinit, newmarkings);
      mNameSpace.addComponent(suffix, newvar);
      mCompiledComponents.add(newvar);
      mCompilationInfo.add(newvar, var);
      return newvar;
    } catch (final EvalException exception) {
      exception.provideLocation(var);
      throw wrap(exception);
    }
  }


  //#########################################################################
  //# Aborting
  private void checkAbort() throws VisitorException
  {
    if (mIsAborting) {
      final EvalAbortException exception = new EvalAbortException();
      throw new VisitorException(exception);
    }
  }


  //#########################################################################
  //# Specific Evaluation Methods
  private CompiledParameterBinding getParameterBinding
    (final IdentifierProxy ident, final ScopeKind scope)
    throws UndefinedIdentifierException
  {
    if (mParameterMap == null || scope == ScopeKind.LOCAL) {
      return null;
    }
    final CompiledParameterBinding binding;
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      binding = mParameterMap.remove(name);
    } else {
      binding = null;
    }
    if (binding == null && scope == ScopeKind.REQUIRED_PARAMETER) {
      final String paramname = ident.toString();
      throw new UndefinedIdentifierException
        (paramname, "required parameter", null);
    } else {
      return binding;
    }
  }

  private LabelBlockProxy createLabelBlock(final CompiledEventList events)
  {
    final List<IdentifierProxy> elist = new LinkedList<IdentifierProxy>();
    createEventList(events, elist);
    return mFactory.createLabelBlockProxy(elist, null);
  }

  private PlainEventListProxy createPlainEventList
    (final CompiledEventList events)
  {
    final List<IdentifierProxy> elist = new LinkedList<IdentifierProxy>();
    createEventList(events, elist);
    return mFactory.createPlainEventListProxy(elist);
  }

  private void createEventList(final CompiledEventList events,
                               final List<IdentifierProxy> elist)
  {
    final Iterable<SingleEventOutput> outputs =
      new EventOutputIterable(events, mCompilationInfo);
    for (final SingleEventOutput output : outputs) {
      final IdentifierProxy ident = createSingleEvent(output);
      elist.add(ident);
    }
  }

  private IdentifierProxy createSingleEvent(final SingleEventOutput output)
  {
    final CompiledSingleEvent event = output.getEvent();
    IdentifierProxy ident = event.getIdentifier();
    if (ident == null) {
      createEventDecl(event);
      ident = event.getIdentifier();
    }
    final IdentifierProxy iclone = ident.clone();
    mCompilationInfo.add(iclone, output.getSourceInfo());
    return iclone;
  }

  private EventDeclProxy createEventDecl(final CompiledSingleEvent event)
  {
    final CompiledEventDecl cdecl = event.getCompiledEventDecl();
    final EventDeclProxy edecl = cdecl.getEventDeclProxy();
    final IdentifierProxy base = edecl.getIdentifier();
    final List<SimpleExpressionProxy> indices = event.getIndexes();
    final IdentifierProxy suffix = mIndexAdder.addIndexes(base, indices);
    final CompiledNameSpace namespace = cdecl.getNameSpace();
    final IdentifierProxy ident =
      namespace.getPrefixedIdentifier(suffix, mFactory);
    mCompilationInfo.add(ident, base);
    final EventKind kind = edecl.getKind();
    final boolean observable = edecl.isObservable();
    Map<String,String> attribs = edecl.getAttributes();
    if (mHISCCompileMode == HISCCompileMode.HISC_HIGH &&
        edecl.getScope() != ScopeKind.LOCAL &&
        HISCAttributeFactory.getEventType(attribs) !=
        HISCAttributeFactory.EventType.DEFAULT) {
      attribs = new HashMap<String,String>(attribs);
      HISCAttributeFactory.setParameter(attribs, true);
    }
    final EventDeclProxy decl = mFactory.createEventDeclProxy
      (ident, kind, observable, ScopeKind.LOCAL, null, null, attribs);
    mCompiledEvents.add(decl);
    mCompilationInfo.add(decl, edecl);
    event.setIdentifier(ident);
    return decl;
  }

  private void addAdditionalBlockedEvents(final CompiledEvent event)
    throws EventKindException
  {
    if (mCurrentBlockedEvents == null) {
      mCurrentBlockedEvents = new CompiledEventList();
    }
    mCurrentBlockedEvents.addEvent(event);
  }

  private boolean isDisabledProposition(final CompiledEvent event)
  {
    if (mEnabledPropositionNames == null) {
      return false;
    } else if (event instanceof CompiledSingleEvent) {
      final CompiledSingleEvent single = (CompiledSingleEvent) event;
      return
        single.getKind() == EventKind.PROPOSITION &&
        !mEnabledPropositionNames.contains(single.toString());
    } else {
      return false;
    }
  }

  private boolean isDisabledProperty(final ComponentKind kind,
                                     final IdentifierProxy ident)
  {
    if (mEnabledPropertyNames == null) {
      return false;
    } else if (kind == ComponentKind.PROPERTY) {
      final String name = ident.toString();
      return !mEnabledPropertyNames.contains(name);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Inner Class: PrimeSearcher
  private class PrimeSearcher extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private boolean containsPrime(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      return (Boolean) expr.acceptVisitor(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      final SimpleExpressionProxy rhs = expr.getRight();
      return containsPrime(lhs) || containsPrime(rhs);
    }

    @Override
    public Boolean visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      final List<SimpleExpressionProxy> indices = ident.getIndexes();
      for (final SimpleExpressionProxy index : indices) {
        if (containsPrime(index)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Boolean visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return containsPrime(base) || containsPrime(comp);
    }

    @Override
    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

    @Override
    public Boolean visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final UnaryOperator op = expr.getOperator();
      if (op == mOperatorTable.getNextOperator()) {
        return true;
      } else {
        final SimpleExpressionProxy subterm = expr.getSubTerm();
        return containsPrime(subterm);
      }
    }
  }


  //#########################################################################
  //# Inner Class: NameCompiler
  private class NameCompiler extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private IdentifierProxy compileName(final IdentifierProxy ident)
      throws VisitorException
    {
      return compileName(ident, true);
    }

    private IdentifierProxy compileName(final IdentifierProxy ident,
                                        final boolean cloning)
      throws VisitorException
    {
      mIsCloning = cloning;
      return (IdentifierProxy) ident.acceptVisitor(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public IndexedIdentifierProxy visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final String name = ident.getName();
        final List<SimpleExpressionProxy> indices = ident.getIndexes();
        final List<SimpleExpressionProxy> values =
          new ArrayList<SimpleExpressionProxy>(indices.size());
        boolean cloning = mIsCloning;
        for (final SimpleExpressionProxy index : indices) {
          final SimpleExpressionProxy value =
            mSimpleExpressionCompiler.eval(index, mContext);
          values.add(value);
          cloning |= !mEquality.equals(index, value);
        }
        if (cloning) {
          final IndexedIdentifierProxy copy =
            mFactory.createIndexedIdentifierProxy(name, values);
          mCompilationInfo.add(copy, ident);
          return copy;
        } else {
          return ident;
        }
      } catch (final EvalException exception) {
        exception.provideLocation(ident);
        throw wrap(exception);
      }
    }

    @Override
    public QualifiedIdentifierProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base0 = ident.getBaseIdentifier();
      final IdentifierProxy base1 =
        (IdentifierProxy) base0.acceptVisitor(this);
      final IdentifierProxy comp0 = ident.getComponentIdentifier();
      final IdentifierProxy comp1 =
        (IdentifierProxy) comp0.acceptVisitor(this);
      if (mIsCloning ||
          !mEquality.equals(base0, base1) ||
          !mEquality.equals(comp0, comp1)) {
        final QualifiedIdentifierProxy copy =
          mFactory.createQualifiedIdentifierProxy(base1, comp1);
        mCompilationInfo.add(copy, ident);
        return copy;
      } else {
        return ident;
      }
    }

    @Override
    public SimpleIdentifierProxy visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      if (mIsCloning) {
        return (SimpleIdentifierProxy) mCloner.getClone(ident);
      } else {
        return ident;
      }
    }

    //#######################################################################
    //# Data Members
    private boolean mIsCloning;
  }


  //#########################################################################
  //# Inner Class: IndexAdder
  private class IndexAdder extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private IdentifierProxy addIndexes
      (final IdentifierProxy ident,
       final List<SimpleExpressionProxy> indices)
    {
      try {
        if (indices.isEmpty()) {
          return ident;
        } else {
          mIndexes = indices;
          return (IdentifierProxy) ident.acceptVisitor(this);
        }
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public IndexedIdentifierProxy visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
    {
      final String name = ident.getName();
      final List<SimpleExpressionProxy> indexes0 = ident.getIndexes();
      final int numindexes = indexes0.size() + mIndexes.size();
      final List<SimpleExpressionProxy> allindexes =
        new ArrayList<SimpleExpressionProxy>(numindexes);
      allindexes.addAll(indexes0);
      allindexes.addAll(mIndexes);
      return mFactory.createIndexedIdentifierProxy(name, mIndexes);
    }

    @Override
    public QualifiedIdentifierProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      final IdentifierProxy comp0 = ident.getComponentIdentifier();
      final IdentifierProxy comp1 =
        (IdentifierProxy) comp0.acceptVisitor(this);
      return mFactory.createQualifiedIdentifierProxy(base, comp1);
    }

    @Override
    public IndexedIdentifierProxy visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      final String name = ident.getName();
      return mFactory.createIndexedIdentifierProxy(name, mIndexes);
    }

    //#######################################################################
    //# Data Members
    private List<SimpleExpressionProxy> mIndexes;
  }


  //#########################################################################
  //# Inner Class: NameSpaceVariablesContext
  private class NameSpaceVariablesContext implements BindingContext
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
    @Override
    public SimpleExpressionProxy getBoundExpression
      (final SimpleExpressionProxy expr)
    {
      final SimpleExpressionProxy bound = mContext.getBoundExpression(expr);
      if (bound != null) {
        return bound;
      }
      if (expr instanceof IdentifierProxy) {
        final IdentifierProxy ident = (IdentifierProxy) expr;
        final IdentifiedProxy comp = mNameSpace.getComponent(ident);
        if (comp != null) {
          return comp.getIdentifier();
        }
      }
      return null;
    }

    @Override
    public boolean isEnumAtom(final IdentifierProxy ident)
    {
      return mContext.isEnumAtom(ident);
    }

    @Override
    public ModuleBindingContext getModuleBindingContext()
    {
      return mContext.getModuleBindingContext();
    }
  }


  //#########################################################################
  //# Inner Class: SinglePrefixingContext
  private class SinglePrefixingContext implements BindingContext
  {
    //#######################################################################
    //# Constructor
    private SinglePrefixingContext(final IdentifierProxy suffix)
    {
      mSuffix = suffix;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
    @Override
    public SimpleExpressionProxy getBoundExpression
      (final SimpleExpressionProxy expr)
    {
      if (mEquality.equals(expr, mSuffix)) {
        return mNameSpace.getPrefixedIdentifier(mSuffix, mFactory);
      } else {
        return mContext.getBoundExpression(expr);
      }
    }

    @Override
    public boolean isEnumAtom(final IdentifierProxy ident)
    {
      if (mEquality.equals(ident, mSuffix)) {
        return false;
      } else {
        return mContext.isEnumAtom(ident);
      }
    }

    @Override
    public ModuleBindingContext getModuleBindingContext()
    {
      return mContext.getModuleBindingContext();
    }

    //#######################################################################
    //# Data Member
    private final IdentifierProxy mSuffix;
  }


  //#########################################################################
  //# Data Members

  //  Utilities:
  private final DocumentManager mDocumentManager;
  private final ModuleProxyFactory mFactory;
  private final CompilationInfo mCompilationInfo;
  private final ModuleProxyCloner mCloner;
  private final CompilerOperatorTable mOperatorTable;
  private final ModuleEqualityVisitor mEquality;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final PrimeSearcher mPrimeSearcher;
  private final NameCompiler mNameCompiler;
  private final IndexAdder mIndexAdder;
  private final NameSpaceVariablesContext mNameSpaceVariablesContext;
  private final ModuleProxy mInputModule;

  //  Configurations:
  private boolean mIsOptimizationEnabled = true;
  private Collection<String> mEnabledPropertyNames = null;
  private Collection<String> mEnabledPropositionNames = null;
  private HISCCompileMode mHISCCompileMode = HISCCompileMode.NOT_HISC;

  //  Module Information:
  private boolean mHasEFAElements;
  private boolean m1stPass; // Used in the method visitModuleProxy().

  private BindingContext mContext;
  private CompiledNameSpace mNameSpace;
  private Collection<EventDeclProxy> mCompiledEvents;
  private Collection<Proxy> mCompiledComponents;
  private Map<String,CompiledParameterBinding> mParameterMap;

  private SimpleComponentProxy mCurrentComponent;
  private ProxyAccessorSet<IdentifierProxy> mCurrentAlphabet;
  private CompiledEventList mCurrentBlockedEvents;
  private List<NodeProxy> mCurrentNodes;
  private List<EdgeProxy> mCurrentEdges;
  private Map<NodeProxy,NodeProxy> mNodeMap;

  private EdgeProxy mCurrentEdge;
  private CompiledEventList mCurrentEventList;

  //  Aborting:
  private boolean mIsAborting;
}
