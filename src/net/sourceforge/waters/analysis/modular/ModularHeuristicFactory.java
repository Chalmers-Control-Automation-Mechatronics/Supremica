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
    PREFER_REAL_PLANT,
    PREFER_PLANT,
    NOPREF
  };

}
