//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import gnu.trove.THashSet;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A default implementation of the {@link ModelVerifierFactory} interface.
 * This class is extended for different flavours of model checking
 * algorithms.
 *
 * @author Robi Malik
 */

public abstract class AbstractModelVerifierFactory
  implements ModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  protected AbstractModelVerifierFactory()
  {
    mArgumentMap = new HashMap<String,CommandLineArgument>(16);
    mArgumentList = new LinkedList<CommandLineArgument>();
  }


  //#########################################################################
  //# Configuration
  protected void addArguments()
  {
    addArgument(new EndArgument());
    addArgument(new HelpArgument());
    addArgument(new HISCArgument());
    addArgument(new LimitArgument());
    addArgument(new MarkingArgument());
    addArgument(new NoOptimisationArgument());
    addArgument(new PreMarkingArgument());
    addArgument(new PropertyArgument());
    addArgument(new TransitionLimitArgument());
  }

  protected void addArgument(final CommandLineArgument argument)
  {
    final String name = argument.getName();
    mArgumentMap.put(name, argument);
    mArgumentList.add(argument);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public ConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    throw createUnsupportedOperationException("conflict");
  }

  @Override
  public ControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    throw createUnsupportedOperationException("controllability");
  }

  @Override
  public ControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    throw createUnsupportedOperationException("control-loop");
  }

  @Override
  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    throw createUnsupportedOperationException("language inclusion");
  }


  @Override
  public void parse(final Iterator<String> iter)
  {
    mArgumentMap.clear();
    mArgumentList.clear();
    addArguments();
    while (iter.hasNext()) {
      final String name = iter.next();
      final CommandLineArgument arg = mArgumentMap.get(name);
      if (arg != null) {
        iter.remove();
        arg.parse(iter);
      } else if (name.startsWith("-")) {
        System.err.println("Unsupported option " + name +
                           ". Try -help to see available options.");
        System.exit(1);
      }
    }
    checkRequiredArguments();
  }

  @Override
  public void configure(final ModelVerifier verifier)
  {
    for (final CommandLineArgument arg : mArgumentList) {
      if (arg.isUsed()) {
        arg.configure(verifier);
      }
    }
  }

  @Override
  public void configure(final ModuleCompiler compiler)
  {
    for (final CommandLineArgument arg : mArgumentList) {
      if (arg.isUsed()) {
        arg.configure(compiler);
      }
    }
  }

  @Override
  public void postConfigure(final ModelVerifier checker)
  throws AnalysisException
  {
    for (final CommandLineArgument arg : mArgumentList) {
      if (arg.isUsed()) {
        arg.postConfigure(checker);
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private UnsupportedOperationException createUnsupportedOperationException
    (final String checkname)
  {
    final String clsname = getClass().getName();
    final int dotpos = clsname.lastIndexOf('.');
    final String msg =
      clsname.substring(dotpos + 1) + " does not support " +
      checkname + " check!";
    return new UnsupportedOperationException(msg);
  }

  private void checkRequiredArguments()
  {
    for (final CommandLineArgument arg : mArgumentList) {
      if (arg.isRequired() && !arg.isUsed()) {
        final String clsname = getClass().getName();
        final int dotpos = clsname.lastIndexOf('.');
        final String msg =
          "Required argument " + arg.getName() + " for " +
          clsname.substring(dotpos + 1) + " not specified!";
        CommandLineArgument.fail(msg);
      }
    }
  }


  //#########################################################################
  //# Inner Class EndArgument
  private class EndArgument extends CommandLineArgument
  {
    //#######################################################################
    //# Constructors
    private EndArgument()
    {
      super("--", "Treat remaining arguments as file names");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void parse(final Iterator<String> iter)
    {
      while (iter.hasNext()) {
        iter.next();
      }
    }
  }


  //#########################################################################
  //# Inner Class HelpArgument
  private class HelpArgument extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructors
    private HelpArgument()
    {
      super("-help", "Print this message");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      if (getValue()) {
        final String name =
          ProxyTools.getShortClassName(AbstractModelVerifierFactory.this);
        System.err.println
          (name + " supports the following command line options:");
        final List<CommandLineArgument> args =
          new ArrayList<CommandLineArgument>(mArgumentMap.values());
        Collections.sort(args);
        for (final CommandLineArgument arg : args) {
          arg.dump(System.err, verifier);
        }
        System.exit(0);
      }
    }
  }


  //#########################################################################
  //# Inner Class HISCArgument
  private static class HISCArgument
    extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructors
    private HISCArgument()
    {
      super("-hisc",
            "Compile as HISC module, only including interfaces of low levels");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModuleCompiler compiler)
    {
      if (getValue()) {
        compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
        compiler.setEnabledPropertyNames(null);
      }
    }
  }


  //#########################################################################
  //# Inner Class LimitArgument
  private static class LimitArgument extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructors
    private LimitArgument()
    {
      super("-limit",
            "Maximum number of states/nodes explored");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      verifier.setNodeLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class MarkingArgument
  private static class MarkingArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private MarkingArgument()
    {
      super("-marking",
            "Name of marking proposition for conflict check");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      if (!(verifier instanceof ConflictChecker)) {
        fail("Command line option " + getName() +
             " is only supported for conflict check!");
      }
    }

    @Override
    protected void configure(final ModuleCompiler compiler)
    {
      final String name = getValue();
      final Collection<String> current = compiler.getEnabledPropertyNames();
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
      compiler.setEnabledPropositionNames(props);
    }

    @Override
    protected void postConfigure(final ModelVerifier verifier)
    throws EventNotFoundException
    {
      final ConflictChecker cchecker = (ConflictChecker) verifier;
      final ProductDESProxy model = cchecker.getModel();
      final String markingname = getValue();
      if (markingname != null) {
        final EventProxy marking =
          AbstractConflictChecker.getMarkingProposition(model, markingname);
        cchecker.setConfiguredDefaultMarking(marking);
      }
    }
  }


  //#########################################################################
  //# Inner Class NoOptimisationArgument
  private static class NoOptimisationArgument
    extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructors
    private NoOptimisationArgument()
    {
      super("-noopt", "Disable compiler optimisation");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModuleCompiler compiler)
    {
      final boolean opt = !getValue();
      compiler.setOptimizationEnabled(opt);
    }
  }


  //#########################################################################
  //# Inner Class PreMarkingArgument
  private static class PreMarkingArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private PreMarkingArgument()
    {
      super("-premarking",
            "Name of precondition marking proposition\n" +
            "for generalised conflict check");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      if (!(verifier instanceof ConflictChecker)) {
        fail("Command line option " + getName() +
             " is only supported for conflict check!");
      }
    }

    @Override
    protected void configure(final ModuleCompiler compiler)
    {
      final String name = getValue();
      final Collection<String> current = compiler.getEnabledPropertyNames();
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
      compiler.setEnabledPropositionNames(props);
    }

    @Override
    protected void postConfigure(final ModelVerifier verifier)
    throws EventNotFoundException
    {
      final ConflictChecker cchecker = (ConflictChecker) verifier;
      final ProductDESProxy model = cchecker.getModel();
      final String markingname = getValue();
      if (markingname != null) {
        final EventProxy marking =
          AbstractConflictChecker.getMarkingProposition(model, markingname);
        cchecker.setConfiguredPreconditionMarking(marking);
      }
    }
  }


  //#########################################################################
  //# Inner Class PropertyArgument
  private class PropertyArgument
    extends CommandLineArgumentStringList
  {
    //#######################################################################
    //# Constructors
    private PropertyArgument()
    {
      super("-property",
            "Property for language inclusion check\n" +
            "(can be used more than once)");
      setUsed(true);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final Collection<String> props = getValues();
      if (verifier instanceof LanguageInclusionChecker) {
        if (props.isEmpty()) {
          setUsed(false);
        } else {
          final LanguageInclusionChecker lchecker =
            (LanguageInclusionChecker) verifier;
          final Collection<String> names = getValues();
          final PropertyKindTranslator translator =
            new PropertyKindTranslator(names);
          lchecker.setKindTranslator(translator);
        }
      } else {
        if (!props.isEmpty()) {
          fail("Command line option " + getName() +
               " is only supported for language inclusion!");
        }
      }
    }

    @Override
    protected void configure(final ModuleCompiler compiler)
    {
      final Collection<String> props = getValues();
      compiler.setEnabledPropertyNames(props);
    }


    //#######################################################################
    //# Printing
    @Override
    protected void dump(final PrintStream stream,
                        final ModelVerifier verifier)
    {
      if (verifier instanceof LanguageInclusionChecker) {
        super.dump(stream, verifier);
      }
    }
  }


  //#########################################################################
  //# Inner Class TransitionLimitArgument
  private static class TransitionLimitArgument
    extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructors
    private TransitionLimitArgument()
    {
      super("-tlimit",
            "Maximum number of transitions stored");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      verifier.setTransitionLimit(limit);
    }
  }


  //#########################################################################
  //# Inner Class PropertyKindTranslator
  private static class PropertyKindTranslator
    implements KindTranslator, Serializable
  {
    //#######################################################################
    //# Constructor
    PropertyKindTranslator(final Collection<String> names)
    {
      mUsedPropertyNames = new THashSet<String>(names);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      final ComponentKind kind = aut.getKind();
      switch (kind) {
      case PLANT:
      case SPEC:
        return ComponentKind.PLANT;
      case PROPERTY:
        final String name = aut.getName();
        if (mUsedPropertyNames.contains(name)) {
          return ComponentKind.SPEC;
        } else {
          return kind;
        }
      default:
        return kind;
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      final EventKind kind = event.getKind();
      switch (kind) {
      case CONTROLLABLE:
      case UNCONTROLLABLE:
        return EventKind.UNCONTROLLABLE;
      default:
        return kind;
      }
    }

    //#######################################################################
    //# Data Members
    private final Collection<String> mUsedPropertyNames;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Data Members
  private final Map<String,CommandLineArgument> mArgumentMap;
  private final List<CommandLineArgument> mArgumentList;

}
