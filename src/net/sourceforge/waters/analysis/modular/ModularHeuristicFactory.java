//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.WatersRuntimeException;


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
                                       final Preference pref,
                                       final KindTranslator translator)
  {
    try {
      final Class<? extends ModularHeuristicFactory> myclass = getClass();
      final String packname = myclass.getPackage().getName();
      final String fullname = packname + "." + method.toString() + "Heuristic";
      final Class<?> lclass = myclass.getClassLoader().loadClass(fullname);
      @SuppressWarnings("unchecked")
      final Class<ModularHeuristic> heuclass =
        (Class<ModularHeuristic>) lclass;
      final Constructor<ModularHeuristic> constr =
        heuclass.getConstructor(KindTranslator.class, Preference.class);
      return constr.newInstance(translator, pref);
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
    NOPREF,
    PREFER_PLANT,
    PREFER_REAL_PLANT
  };

}
