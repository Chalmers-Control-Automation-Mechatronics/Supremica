//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularHeuristicFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.sourceforge.waters.model.analysis.CommandLineArgument;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.unchecked.Casting;


/**
 * The central access point to obtain a {@link ModularHeuristic} that
 * defines the method for automata selection by the modular controllability
 * or modular language inclusion check algorithms.
 *
 * @author Robi Malik
 */

public class ModularHeuristicFactory {

  //#########################################################################
  //# Singleton Pattern
  public static ModularHeuristicFactory getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static final class SingletonHolder {
    private static final ModularHeuristicFactory theInstance =
      new ModularHeuristicFactory();
  }

  private ModularHeuristicFactory()
  {
  }


  //#########################################################################
  //# Creation of Heuristics
  public ModularHeuristic getHeuristic(final Method method,
				       final Preference pref)
  {
    try {
      final Class<? extends ModularHeuristicFactory> myclass = getClass();
      final String packname = myclass.getPackage().getName();
      final String fullname = packname + "." + method.toString() + "Heuristic";
      final Class<?> lclass = myclass.getClassLoader().loadClass(fullname);
      final Class<ModularHeuristic> heuclass = Casting.toClass(lclass);
      final Constructor<ModularHeuristic> constr =
	heuclass.getConstructor(Preference.class);
      return constr.newInstance(pref);
    } catch (final ClassNotFoundException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final IllegalAccessException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final InstantiationException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final InvocationTargetException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final NoSuchMethodException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Command Line Support
  public static CommandLineArgument getMethodArgument()
  {
    return new MethodArgument();
  }

  public static CommandLineArgument getPreferenceArgument()
  {
    return new PreferenceArgument();
  }


  //#########################################################################
  //# Inner Class MethodArgument
  private static class MethodArgument
    extends CommandLineArgumentEnum<Method>
  {
    //#######################################################################
    //# Constructor
    private MethodArgument()
    {
      super("-heuristic", "Modular heuristic", Method.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void assign(final ModelVerifier verifier)
    {
      final AbstractModularSafetyVerifier modular =
        (AbstractModularSafetyVerifier) verifier;
      final Method method = getValue();
      modular.setHeuristicMethod(method);
    }
  }

    
  //#########################################################################
  //# Inner Class PreferenceArgument
  private static class PreferenceArgument
    extends CommandLineArgumentEnum<Preference>
  {
    //#######################################################################
    //# Constructor
    private PreferenceArgument()
    {
      super("-preference",
            "Preference of modular heuristic",
            Preference.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void assign(final ModelVerifier verifier)
    {
      final AbstractModularSafetyVerifier modular =
        (AbstractModularSafetyVerifier) verifier;
      final Preference preference = getValue();
      modular.setHeuristicPreference(preference);
    }

  }

    
  //#########################################################################
  //# Enumeration Types
  public enum Method {
    All,                            
    EarlyNotAccept,
    LateNotAccept, 
    MaxCommonEvents, 
    MaxCommonUncontrollableEvents,
    MaxStates, 
    MinEvents,
    MinNewEvents,
    MinStates,
    MinTransitions,
    One,
    RelMaxCommonEvents
  };

  public enum Preference {
    NOPREF,
    PREFER_PLANT,
    PREFER_REAL_PLANT
  };

}
