//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelVerifierFactory
//###########################################################################
//# $Id: AbstractModelVerifierFactory.java,v 1.1 2006-11-28 04:28:33 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A factory interface for all types of model verifiers.
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
    addArgument(new LimitArgument());
    addArgument(new PropArgument());
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
  public List<String> loadArguments(final ModelVerifier verifier)
  {
    if (mArgumentList != null && mArgumentMap != null) {
      final List<String> filenames = new LinkedList<String>();
      final Iterator<String> iter = mArgumentList.iterator();
      while (iter.hasNext()) {
        final String name = iter.next();
        final CommandLineArgument arg = mArgumentMap.get(name);
        if (arg != null) {
          arg.parse(iter);
          arg.assign(verifier);
        } else if (name.equals("--")) {
          while (iter.hasNext()) {
            final String nextname = iter.next();
            filenames.add(nextname);
          }
        } else {
          filenames.add(name);
        }
      }
      return filenames;
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Inner Class LimitArgument
  private static class LimitArgument extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructors
    LimitArgument()
    {
      super("-limit");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void assign(final ModelVerifier verifier)
    {
      final int limit = getValue();
      verifier.setNodeLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class PropArgument
  private static class PropArgument
    extends CommandLineArgumentString
    implements KindTranslator
  {
    //#######################################################################
    //# Constructors
    PropArgument()
    {
      super("-property");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void assign(final ModelVerifier verifier)
    {
      if (verifier instanceof LanguageInclusionChecker) {
        final LanguageInclusionChecker checker =
          (LanguageInclusionChecker) verifier;
        final String name = getValue();
        if (mNames == null) {
          mNames = new HashSet<String>();
          checker.setKindTranslator(this);
        }
        mNames.add(name);
      } else {
        throw new IllegalArgumentException
          ("Command line option " + getName() +
           " is only supported for language inclusion!");
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      final ComponentKind kind = aut.getKind();
      switch (kind) {
      case PLANT:
      case SPEC:
      case SUPERVISOR:
        return ComponentKind.PLANT;
      case PROPERTY:
        final String name = aut.getName();
        if (mNames.contains(name)) {
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
    //# Data Members
    private Set<String> mNames;
  }


  //#########################################################################
  //# Data Members
  private final List<String> mArgumentList;
  private final Map<String,CommandLineArgument> mArgumentMap;

}
