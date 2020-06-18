//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.compiler;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.FlagOption;
import net.sourceforge.waters.analysis.options.LeafOptionPage;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.StringListOption;
import net.sourceforge.waters.analysis.options.StringOption;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.cli.ArgumentSource;
import net.sourceforge.waters.model.analysis.cli.CommandLineOptionContext;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.SourceInfoCloner;
import net.sourceforge.waters.model.compiler.efa.EFACompiler;
import net.sourceforge.waters.model.compiler.efsm.EFSMCompiler;
import net.sourceforge.waters.model.compiler.efsm.EFSMNormaliser;
import net.sourceforge.waters.model.compiler.graph.ModuleGraphCompiler;
import net.sourceforge.waters.model.compiler.groupnode.GroupNodeCompiler;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.MultiEvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.xml.sax.SAXException;


/**
 * <P>The main tool to translate module structures into plain finite-state
 * machines.</P>
 *
 * <P>The module compiler takes as input a {@link ModuleProxy} object,
 * which may contain guards and instantiation, and converts it into a
 * {@link ProductDESProxy} object, which is a collection of traditional
 * DES automata. Compilation is a necessary first step to pass module
 * structures to many analysis algorithms that take as input a {@link
 * ProductDESProxy}.</P>
 *
 * <P>To use a module compiler, the user first creates an instance of this
 * class, and configures it as needed. Then compilation is started
 * using the {@link #compile()} or {@link #compile(List)} method, which
 * returns the resultant product DES. The following code is used to
 * compile a given <CODE>module</CODE>.</P>
 *
 * <P>
 * <CODE>try {</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link ModuleProxyFactory} modFactory =
 *   {@link ModuleElementFactory}.{@link ModuleElementFactory#getInstance()
 *   getInstance}();</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link OperatorTable} optable =
 *   {@link CompilerOperatorTable}.{@link CompilerOperatorTable#getInstance()
 *   getInstance}();</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link ProxyUnmarshaller}&lt;{@link ModuleProxy}&gt; unmarshaller =
 *   new {@link SAXModuleMarshaller#SAXModuleMarshaller(ModuleProxyFactory,OperatorTable)
 *   SAXModuleMarshaller}(modFactory, optable);</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link DocumentManager} manager =
 *   new {@link DocumentManager#DocumentManager() DocumentManager}();</CODE><BR>
 * <CODE>&nbsp;&nbsp;manager.{@link DocumentManager#registerUnmarshaller(ProxyUnmarshaller)
 *   registerUnmarshaller}(unmarshaller);</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link ProductDESProxyFactory} desFactory =
 *   {@link ProductDESElementFactory}.{@link ProductDESElementFactory#getInstance()
 *   getInstance}();</CODE><BR>
 * <CODE>&nbsp;&nbsp;{@link ModuleCompiler} compiler =
 *   new {@link ModuleCompiler#ModuleCompiler(DocumentManager, ProductDESProxyFactory,
 *   ModuleProxy) ModuleCompiler}(manager, desFactory, module);</CODE><BR>
 * <CODE>&nbsp;&nbsp;// </CODE>configure compiler here if needed ...<BR>
 * <CODE>&nbsp;&nbsp;{@link ProductDESProxy} des =
 *   compiler.{@link #compile()};</CODE><BR>
 * <CODE>&nbsp;&nbsp;// </CODE>compilation successful, result in
 *   <CODE>des</CODE> ...<BR>
 * <CODE>} catch ({@link EvalException} exception) {</CODE><BR>
 * <CODE>&nbsp;&nbsp;// </CODE>module has errors ...<BR>
 * <CODE>} catch ({@link SAXException} | {@link ParserConfigurationException} exception) {</CODE><BR>
 * <CODE>&nbsp;&nbsp;// </CODE>error setting up XML parsers - should not happen ...<BR>
 * <CODE>}</CODE></P>
 *
 * <P><STRONG>Algorithm:</STRONG></P>
 * <P>The input module is transformed to a product DES in the following five
 * steps.</P>
 * <DL>
 * <DT>{@link ModuleInstanceCompiler}</DT>
 * <DD>Expands parameters bindings and foreach blocks, and loads and
 * instantiates referenced modules.</DD>
 * <DT>{@link GroupNodeCompiler}</DT>
 * <DD>Expands transitions linked to group nodes into separate transitions.</DD>
 * <DT>{@link EFSMNormaliser}</DT>
 * <DD>Combines and renames events from different EFSMs, ensures that each
 * event has a unique guard/action block throughout the module. (Only used
 * if variables and guard/action blocks are present, and normalisation
 * semantics is enabled by the {@link #setNormalizationEnabled(boolean)}
 * method.)</DD>
 * <DT>{@link EFACompiler}</DT>
 * <DD>Creates variable automata and expands guard/action blocks into
 * separate events. (Only used if variables and guard/action blocks are
 * present.)</DD>
 * <DT>{@link ModuleGraphCompiler}</DT>
 * <DD>Replaces the objects in the module hierarchy by the corresponding
 * {@link ProductDESProxy} structure.</DD>
 * </DL>
 *
 * @author Robi Malik
 */
public class ModuleCompiler extends AbortableCompiler
  implements Configurable, ArgumentSource
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new module compiler for the given module.
   * @param manager  The document manager used to load additional modules
   *                 referred to by the module being being compiled.
   * @param factory  The factory used to create the output product DES.
   * @param module   The module to be compiled. This module is passed by
   *                 reference into the compiler.
   */
  public ModuleCompiler(final DocumentManager manager,
                        final ProductDESProxyFactory factory,
                        final ModuleProxy module)
  {
    mDocumentManager = manager;
    mFactory = factory;
    mInputModule = module;
    mCompilationInfoIsDirty = true;
    mCompilationInfo = new CompilationInfo();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the module to be compiled by this compiler.
   */
  public ModuleProxy getInputModule()
  {
    return mInputModule;
  }

  /**
   * Sets a new input module for the compiler. This method replaces the
   * current input module and invalidates information from a previous
   * compilation.
   * @param module  The module to be compiled.
   * @param clone   Whether the input module is passed by reference or needs
   *                cloning. If <CODE>true</CODE>, the compiler creates a copy
   *                of the given module in order to compile the copy, enabling
   *                the caller to make changes to the original while the copy
   *                is being compiler. If <CODE>false</CODE> the given module
   *                reference is compiled directly, and it is the caller's
   *                responsibility to ensure that it remains unchanged while
   *                being compiled.
   */
  public void setInputModule(final ModuleProxy module, final boolean clone)
  {
    if (clone) {
      mCompilationInfo = new CompilationInfo();
      mCompilationInfoIsDirty = false;
      final ModuleProxyFactory modfactory =
        ModuleElementFactory.getInstance();
      final SourceInfoCloner cloner =
        new SourceInfoCloner(modfactory, mCompilationInfo);
      mInputModule = (ModuleProxy) cloner.getClone(module);
      mInputModule.setLocation(module.getLocation());
    } else {
      mCompilationInfoIsDirty = true;
      mInputModule = module;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mActiveAbortable != null) {
      mActiveAbortable.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mActiveAbortable != null) {
      mActiveAbortable.resetAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Configurable
  @Override
  public List<Option<?>> getOptions(final OptionPage page)
  {
    final List<Option<?>> options = new LinkedList<>();
    page.append(options, AbstractModelAnalyzerFactory.
                OPTION_AbstractModelAnalyzerFactory_NoOptimisation);
    page.append(options, AbstractModelAnalyzerFactory.
                OPTION_AbstractModelAnalyzerFactory_HISCModule);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_AbstractModelAnalyzerFactory_NoOptimisation)) {
      setOptimizationEnabled(false);
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_AbstractModelAnalyzerFactory_HISCModule)) {
      setHISCCompileMode(HISCCompileMode.HISC_HIGH);
      setEnabledPropertyNames(null);
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_LanguageInclusionChecker_Property)) {
      final StringListOption opt = (StringListOption) option;
      final Collection<String> props = opt.getValue();
      setEnabledPropertyNames(props);
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ConflictChecker_ConfiguredDefaultMarkingString)) {
      //Default Marking
      final StringOption opt = (StringOption) option;
      final String name = opt.getValue();
      final Collection<String> current = getEnabledPropertyNames();
      final Collection<String> props;
      if (current == null || current.isEmpty()) {
        props = Collections.singletonList(name);
      } else if (current.contains(EventDeclProxy.DEFAULT_MARKING_NAME)) {
        final int size = current.size();
        if (size == 1) {
          props = Collections.singletonList(name);
        } else {
          props = new ArrayList<String>(size);
          for (final String prop : current) {
            if (!prop.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
              props.add(prop);
            }
          }
          props.add(name);
        }
      } else {
        final int size = current.size() + 1;
        props = new ArrayList<String>(size);
        props.addAll(current);
        props.add(name);
      }
      setEnabledPropositionNames(props);
      //End of default marking
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ConflictChecker_ConfiguredDefaultMarkingString)) {
      //Pre Marking
      final StringOption opt = (StringOption) option;
      final String name = opt.getValue();
      final Collection<String> current = getEnabledPropertyNames();
      final Collection<String> props;
      if (current == null || current.isEmpty()) {
        props = new ArrayList<String>(2);
        props.add(EventDeclProxy.DEFAULT_MARKING_NAME);
      } else {
        final int size = current.size() + 1;
        props = new ArrayList<String>(size);
        props.addAll(current);
      }
      props.add(name);
      setEnabledPropositionNames(props);
      //End of pre marking
    }
  }

  public void registerOptions(final OptionPage page) {
    page.add(new FlagOption
           (AbstractModelAnalyzerFactory.
             OPTION_AbstractModelAnalyzerFactory_NoOptimisation, null,
            "Disable compiler optimisation",
            "-noopt"));
    page.add(new FlagOption
           (AbstractModelAnalyzerFactory.
             OPTION_AbstractModelAnalyzerFactory_HISCModule, null,
            "Compile as HISC module, "
             + "only including interfaces of low levels",
            "-hisc"));
  }

  @Override
  public void addArguments(final CommandLineOptionContext context,
                           final Configurable configurable, final LeafOptionPage page)
  {
    if (configurable == this) {
      registerOptions(page);
      context.generateArgumentsFromOptions(page, configurable);
    }
  }


  //#########################################################################
  //# Invocation
  /**
   * Compiles the input module using default values for all parameter
   * bindings.
   * @return The product DES resulting from compilation.
   * @throws EvalException to indicate any syntactical or semantical
   *         errors encountered during compilation.
   *         Using the {@link #setMultiExceptionsEnabled(boolean)
   *         setMultiExceptionsEnabled()} method, the compiler may be
   *         configured to collect multiple error messages in a single
   *         exception ({@link MultiEvalException}), or to stop with an
   *         exception when the first error is encountered.
   */
  public ProductDESProxy compile()
    throws EvalException
  {
    return compile(null);
  }

  /**
   * Compiles the input module using the given parameter bindings.
   * @param  bindings  List of parameter bindings to supply values for
   *                   parameters specified in the module. A module's
   *                   event declarations ({@link EventDeclProxy}) and
   *                   constant alias declarations ({@link ConstantAliasProxy})
   *                   may be declared as parameters, in which case the
   *                   bindings can be used to supply values to replace these
   *                   parameters.
   * @return The product DES resulting from compilation.
   * @throws EvalException to indicate any syntactical or semantical
   *         errors encountered during compilation.
   *         Using the {@link #setMultiExceptionsEnabled(boolean)
   *         setMultiExceptionsEnabled()} method, the compiler may be
   *         configured to collect multiple error messages in a single
   *         exception ({@link MultiEvalException}), or to stop with an
   *         exception when the first error is encountered.
   */
  public ProductDESProxy compile(final List<ParameterBindingProxy> bindings)
    throws EvalException
  {
    try
    {
      if (mCompilationInfoIsDirty) {
        mCompilationInfo = new CompilationInfo(mSourceInfoEnabled,
                                               mMultiExceptionsEnabled);
      }
      final ModuleProxyFactory modFactory = ModuleElementFactory.getInstance();

      // resolve instances
      ModuleInstanceCompiler instanceCompiler = new ModuleInstanceCompiler
        (mDocumentManager, modFactory, mCompilationInfo, mInputModule);
      instanceCompiler.setOptimizationEnabled(mOptimizationEnabled);
      instanceCompiler.setEnabledPropertyNames(mEnabledPropertyNames);
      instanceCompiler.setEnabledPropositionNames(mEnabledPropositionNames);
      instanceCompiler.setHISCCompileMode(mHISCCompileMode);
      checkAbort();
      mActiveAbortable = instanceCompiler;
      ModuleProxy intermediate = instanceCompiler.compile(bindings);
      final boolean efsm = instanceCompiler.getHasEFSMElements();
      mActiveAbortable = instanceCompiler = null;
      checkAbort();

      // simplify group nodes
      GroupNodeCompiler groupNodeCompiler =
        new GroupNodeCompiler(modFactory, mCompilationInfo, intermediate);
      mActiveAbortable = groupNodeCompiler;
      intermediate = groupNodeCompiler.compile();
      mActiveAbortable = groupNodeCompiler = null;
      checkAbort();

      if (efsm && mExpandingEFSMTransitions) {
        if (mNormalizationEnabled) {
          // perform normalisation
          EFSMNormaliser normaliser =
            new EFSMNormaliser(modFactory, mCompilationInfo, intermediate);
          normaliser.setUsesEventNameBuilder(true);
          normaliser.setCreatesGuardAutomaton(true);
          normaliser.setAutomatonVariablesEnabled(mAutomatonVariablesEnabled);
          mActiveAbortable = normaliser;
          intermediate = normaliser.compile();
          mActiveAbortable = normaliser = null;

          // compile normalised EFSM system
          EFSMCompiler efsmCompiler =
            new EFSMCompiler(modFactory, mCompilationInfo, intermediate);
          efsmCompiler.setOptimizationEnabled(mOptimizationEnabled);
          efsmCompiler.setAutomatonVariablesEnabled(mAutomatonVariablesEnabled);
          mActiveAbortable = efsmCompiler;
          intermediate = efsmCompiler.compile();
          mActiveAbortable = efsmCompiler = null;

        } else {
          // use old EFA compiler
          EFACompiler efaCompiler =
            new EFACompiler(modFactory, mCompilationInfo, intermediate);
          checkAbort();
          mActiveAbortable = efaCompiler;
          intermediate = efaCompiler.compile();
          mActiveAbortable = efaCompiler = null;
        }
      }

      // build product DES
      ModuleGraphCompiler graphCompiler =
        new ModuleGraphCompiler(mFactory, mCompilationInfo, intermediate);
      graphCompiler.setOptimizationEnabled(mOptimizationEnabled);
      checkAbort();
      mActiveAbortable = graphCompiler;
      final ProductDESProxy des = graphCompiler.compile();
      mActiveAbortable = graphCompiler = null;
      setLocation(des);
      return des;
    }

    catch (final EvalException exception) {
      mCompilationInfo.raise(exception);
      return null;
    } finally {
      tearDown();
      if (mCompilationInfo.hasExceptions()) {
        throw mCompilationInfo.getExceptions();
      }
    }
  }

  /**
   * Gets the source information resulting from the last compilation run.
   * @return  A map that assigns to objects in the output {@link
   *          ProductDESProxy} a source information record containing the
   *          object in the input {@link ModuleProxy} from which it was
   *          created, plus information about variable bindings.
   *          The generation of source information may be disabled by calling
   *          the {@link #setSourceInfoEnabled(boolean) setSourceInfoEnabled()}
   *          method, in which case this method returns <CODE>null</CODE>.
   */
  public Map<Object,SourceInfo> getSourceInfoMap()
  {
    return mCompilationInfo.getResultMap();
  }


  //#########################################################################
  //# Static Invocation
  /**
   * Creates a name for a compiled module with its parameters appended
   * in angle brackets.
   * @param  module   The module being compiled.
   * @param  bindings The list of parameter bindings, or <CODE>null</CODE>.
   * @return A name string such as <CODE>&quot;factory&lt;N=5&gt;&quot;</CODE>.
   */
  public static String getParametrizedName
    (final ModuleProxy module, final List<ParameterBindingProxy> bindings)
  {
    try {
      final String name = module.getName();
      if (bindings == null || bindings.isEmpty()) {
        return name;
      }
      final StringWriter writer = new StringWriter();
      writer.append(name);
      char sep = '<';
      for (final ParameterBindingProxy binding : bindings) {
        writer.append(sep);
        writer.append(binding.getName());
        writer.append('=');
        ProxyPrinter.printProxy(writer, binding.getExpression());
        sep = ',';
      }
      writer.append('>');
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Configuration
  /**
   * Returns whether compiler optimisation is enabled.
   * @see #setOptimizationEnabled(boolean)
   */
  public boolean isOptimizationEnabled()
  {
    return mOptimizationEnabled;
  }

  /**
   * Enables or disabled compiler optimisation.
   * If enabled, the compiler may perform several optimisation steps to
   * remove selfloops and unused events or automata from the output.
   * This option is enabled by default.
   */
  public void setOptimizationEnabled(final boolean enabled)
  {
    mOptimizationEnabled = enabled;
  }

  /**
   * Returns whether guard/action blocks are compiled.
   * @see #setExpandingEFATransitions(boolean)
   */
  public boolean isExpandingEFATransitions()
  {
    return mExpandingEFSMTransitions;
  }

  /**
   * Sets whether guard/action blocks are compiled.
   * If enabled, the compiler will compile guards and updates by replacing
   * them with events. If disabled, guard/action blocks will be ignored
   * (the {@link EFACompiler} will not be called).
   * This option is enabled by default.
   */
  public void setExpandingEFATransitions(final boolean expanding)
  {
    mExpandingEFSMTransitions = expanding;
  }

  /**
   * Returns whether normalisation semantics is used.
   * @see #setNormalizationEnabled(boolean)
   */
  public boolean isNormalizationEnabled()
  {
    return mNormalizationEnabled;
  }

  /**
   * <P>Sets whether normalisation semantics is used.</P>
   *
   * <P>If enabled, the compiler implements the semantics of a <I>normalised</I>
   * EFSM system, using a first step to ensure that each event has a unique
   * update associated with it.
   * With this semantics the set of variables changed by a transition is
   * determined after synchronous composition: variables that do not appear
   * next-state variable in a guard/action remain unchanged unless composed
   * with a transition in another EFSM that changes it.</P>
   *
   * <P>If disabled, an older semantics is used, where the set of variables
   * changed by a transition is determined from EFSM containing it.</P>
   *
   * <P>This option is disabled by default.</P>
   *
   * <P><I>Reference:</I><BR>
   * Sahar Mohajerani, Robi Malik, Martin Fabian. A framework for
   * compositional nonblocking verification of extended finite-state machines.
   * Discrete Event Dynamic Systems. September 2015.
   * DOI:&nbsp;<A HREF="http://link.springer.com/article/10.1007/s10626-015-0217-y">10.1007/s10626-015-0217-y</A></P>
   */
  public void setNormalizationEnabled(final boolean enabled)
  {
    mNormalizationEnabled = enabled;
  }

  /**
   * Returns whether automaton variables are enabled.
   * @see #setAutomatonVariablesEnabled(boolean)
   */
  public boolean isAutomatonVariablesEnabled()
  {
    return mAutomatonVariablesEnabled;
  }

  /**
   * <P>Sets whether automaton variables are enabled.</P>
   *
   * <P>If enabled, the EFSM compiler recognises the names of automata
   * (plant, spec, etc.) and their state names in guard expression to impose
   * additional constraints on transitions. It is then possible to specify
   * through a guard that a certain transition is only possible if a specific
   * automaton is in a specific state.</P>
   *
   * <P>If automaton variables are enabled, the compiler must perform
   * additional identifier checking. It is not allowed to use the same
   * name for a variable and an automaton, or for a variable/automaton and
   * a state. Duplicate identifiers are reported in case of such clashes.
   * The additional identifier checking can be avoided by disabling automaton
   * variables.</P>
   *
   * <P>Automaton variables are disabled by default. Automaton variables
   * only work in combination with the normalising compiler, so the option
   * has no effect if normalisation is disabled.</P>
   *
   * @see #setNormalizationEnabled(boolean)
   */
  public void setAutomatonVariablesEnabled(final boolean enabled)
  {
    mAutomatonVariablesEnabled = enabled;
  }

  /**
   * Returns whether the compiler generates source information.
   * @see #setSourceInfoEnabled(boolean)
   */
  public boolean isSourceInfoEnabled()
  {
    return mSourceInfoEnabled;
  }

  /**
   * <P>Sets whether the compiler generates source information.</P>
   *
   * <P>The source information records for each object in the output {@link
   * ProductDESProxy} the object in the input {@link ModuleProxy} from which
   * it was created, plus any variable bindings. It is used by the IDE to
   * display error locations, and to highlight states and transitions during
   * simulation.</P>
   *
   * <P>This option is disabled by default.</P>
   *
   * @see #getSourceInfoMap()
   */
  public void setSourceInfoEnabled(final boolean enable)
  {
    mSourceInfoEnabled = enable;
    mCompilationInfo.setSourceInfoEnabled(enable);
  }

  /**
   * Returns whether the compiler collects multiple error messages.
   * @see #setMultiExceptionsEnabled(boolean)
   */
  public boolean isMultiExceptionsEnabled()
  {
    return mMultiExceptionsEnabled;
  }

  /**
   * Sets whether the compiler collects multiple error messages.
   * If enabled, the compiler may continue after encountering an error.
   * All errors encountered are collected in a {@link MultiEvalException}
   * that is thrown at the end of compilation.
   * If disabled, the compiler stops with a single {@link EvalException}
   * on encountering the first error.
   * This option is disabled by default.
   */
  public void setMultiExceptionsEnabled(final boolean enable)
  {
    mMultiExceptionsEnabled = enable;
    mCompilationInfo.setMultiExceptionsEnabled(enable);
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
  //# Auxiliary Methods
  private void tearDown()
  {
    mCompilationInfoIsDirty = true;
    mActiveAbortable = null;
  }

  private void setLocation(final ProductDESProxy des)
  {
    final URI moduleLocation = mInputModule.getLocation();
    if (moduleLocation != null) {
      try {
        final ProxyMarshaller<ProductDESProxy> marshaller =
          mDocumentManager.findProxyMarshaller(ProductDESProxy.class);
        final String ext = marshaller.getDefaultExtension();
        final String name = mInputModule.getName();
        final URI desLocation = moduleLocation.resolve(name + ext);
        des.setLocation(desLocation);
      } catch (final IllegalArgumentException exception) { }
    }
  }


  //#########################################################################
  //# Data Members
  private final DocumentManager mDocumentManager;
  private final ProductDESProxyFactory mFactory;

  private ModuleProxy mInputModule;
  private CompilationInfo mCompilationInfo;
  private boolean mCompilationInfoIsDirty;
  private Abortable mActiveAbortable;

  private boolean mOptimizationEnabled = true;
  private boolean mExpandingEFSMTransitions = true;
  private boolean mNormalizationEnabled = true;
  private boolean mAutomatonVariablesEnabled = false;
  private boolean mSourceInfoEnabled = false;
  private boolean mMultiExceptionsEnabled = false;
  private Collection<String> mEnabledPropertyNames = null;
  private Collection<String> mEnabledPropositionNames = null;
  private HISCCompileMode mHISCCompileMode = HISCCompileMode.NOT_HISC;

}
