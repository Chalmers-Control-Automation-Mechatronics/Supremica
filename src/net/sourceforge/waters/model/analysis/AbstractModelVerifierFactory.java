//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
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
    mArgumentList = null;
    mArgumentMap = null;
  }

  protected AbstractModelVerifierFactory(final List<String> arglist)
  {
    mArgumentList = arglist;
    mArgumentMap = new HashMap<String,CommandLineArgument>(16);
    addArgument(new HelpArgument());
    addArgument(new LimitArgument());
    addArgument(new TransitionLimitArgument());
    addArgument(new MarkingArgument());
    addArgument(new PropertyArgument());
  }


  //#########################################################################
  //# Configuration
  protected void addArgument(final CommandLineArgument argument)
  {
    if (mArgumentMap != null) {
      final String name = argument.getName();
      mArgumentMap.put(name, argument);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    throw createUnsupportedOperationException("conflict");
  }

  public ControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    throw createUnsupportedOperationException("controllability");
  }

  public ControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    throw createUnsupportedOperationException("control-loop");
  }

  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    throw createUnsupportedOperationException("language inclusion");
  }

  public List<String> configure(final ModelVerifier verifier)
  {
    if (mArgumentList != null && mArgumentMap != null) {
      Collection<CommandLineArgument> used =
        new HashSet<CommandLineArgument>();
      final List<String> filenames = new LinkedList<String>();
      final Iterator<String> iter = mArgumentList.iterator();
      while (iter.hasNext()) {
        final String name = iter.next();
        final CommandLineArgument arg = mArgumentMap.get(name);
        if (arg != null) {
          used.add(arg);
          arg.parse(iter);
          arg.configure(verifier);
        } else if (name.equals("--")) {
          while (iter.hasNext()) {
            final String nextname = iter.next();
            filenames.add(nextname);
          }
        } else {
          filenames.add(name);
        }
      }
      checkRequiredArguments(used);
      return filenames;
    } else {
      return null;
    }
  }

  public void configure(final ModuleCompiler compiler)
  {
    if (mArgumentList != null && mArgumentMap != null) {
      Collection<CommandLineArgument> used =
        new HashSet<CommandLineArgument>();
      final Iterator<String> iter = mArgumentList.iterator();
      while (iter.hasNext()) {
        final String name = iter.next();
        final CommandLineArgument arg = mArgumentMap.get(name);
        if (arg != null) {
          used.add(arg);
          arg.parse(iter);
          arg.configure(compiler);
        } else if (name.equals("--")) {
          break;
        }
      }
      checkRequiredArguments(used);
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

  private void checkRequiredArguments
    (final Collection<CommandLineArgument> used)
  {
    for (final CommandLineArgument arg : mArgumentMap.values()) {
      if (arg.isRequired() && !used.contains(arg)) {
        final String clsname = getClass().getName();
        final int dotpos = clsname.lastIndexOf('.');
        final String msg =
          "Required argument " + arg.getName() + " for " +
          clsname.substring(dotpos + 1) + " not specified!";
        arg.fail(msg);
      }
    }
  }


  //#########################################################################
  //# Inner Class HelpArgument
  private class HelpArgument extends CommandLineArgument
  {
    //#######################################################################
    //# Constructors
    private HelpArgument()
    {
      super("-help",
            "Print this message");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void parse(final Iterator<String> iter)
    {
      final Class<?> clazz = AbstractModelVerifierFactory.this.getClass();
      final String fullname = clazz.getName();
      final int dotpos = fullname.lastIndexOf('.');
      final String name = fullname.substring(dotpos + 1);
      System.err.println
        (name + " supports the following command line options:");
      final List<CommandLineArgument> args =
        new ArrayList<CommandLineArgument>(mArgumentMap.values());
      Collections.sort(args);
      for (final CommandLineArgument arg : args) {
        arg.dump(System.err);
      }
      System.exit(0);
    }

    protected void configure(final ModelVerifier verifier)
    {
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
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      verifier.setNodeLimit(limit);
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
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      verifier.setTransitionLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class PropertyArgument
  private static class MarkingArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private MarkingArgument()
    {
      super("-marking",
            "Name of marking propsosition for conflict check");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModuleCompiler compiler)
    {
      final String name = getValue();
      final Collection<String> props = Collections.singletonList(name);
      compiler.setEnabledPropositionNames(props);
    }

    protected void configure(final ModelVerifier verifier)
    {
      if (verifier instanceof ConflictChecker) {
        final ConflictChecker checker = (ConflictChecker) verifier;
        final ProductDESProxy model = checker.getModel();
        final String name = getValue();
        final EventProxy event =
          AbstractConflictChecker.getMarkingProposition(model, name);
        checker.setMarkingProposition(event);
      } else {
        fail("Command line option " + getName() +
             " is only supported for conflict check!");
      }
    }
  }


  //#########################################################################
  //# Inner Class PropertyArgument
  private class PropertyArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private PropertyArgument()
    {
      super("-property",
            "Property for language inclusion check\n" +
            "(can be used more than once)");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModuleCompiler compiler)
    {
      Collection<String> props = compiler.getEnabledPropertyNames();
      if (props == null) {
        props = new LinkedList<String>();
        compiler.setEnabledPropertyNames(props);
      }
      final String name = getValue();
      props.add(name);
    }

    protected void configure(final ModelVerifier verifier)
    {
      if (verifier instanceof LanguageInclusionChecker) {
        final String name = getValue();
        final LanguageInclusionChecker lchecker =
          (LanguageInclusionChecker) verifier;
        final KindTranslator trans = lchecker.getKindTranslator();
        if (trans instanceof PropertyKindTranslator) {
          final PropertyKindTranslator ptrans = (PropertyKindTranslator) trans;
          ptrans.addPropertyName(name);
        } else {
          final PropertyKindTranslator ptrans =
            new PropertyKindTranslator(name);
          lchecker.setKindTranslator(ptrans);
        }
      } else {
        fail("Command line option " + getName() +
             " is only supported for language inclusion!");
      }
    }
  }


  //#########################################################################
  //# Inner Class PropertyKindTranslator
  private static class PropertyKindTranslator
    implements KindTranslator, Serializable
  {
    //#######################################################################
    //# Constructor
    PropertyKindTranslator(final String name)
    {
      mUsedPropertyNames = new HashSet<String>();
      mUsedPropertyNames.add(name);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
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
    //# Simple Access
    void addPropertyName(final String name)
    {
      mUsedPropertyNames.add(name);
    }

    //#######################################################################
    //# Data Members
    private final Collection<String> mUsedPropertyNames;
  }


  //#########################################################################
  //# Data Members
  private final List<String> mArgumentList;
  private final Map<String,CommandLineArgument> mArgumentMap;

}
