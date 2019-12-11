//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.options;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.sourceforge.waters.analysis.abstraction.StepSimplifierFactory;
import net.sourceforge.waters.analysis.trcomp.ChainSimplifierFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.WatersRuntimeException;

import org.supremica.automata.waters.SupremicaSimplifierFactory;


/**
 * <P>A collection of available options.</P>
 *
 * <P>An option map acts as a database that maps string identifiers to
 * {@link Option} objects. For persistent storage of options values,
 * the option map is first initialised with all available options,
 * typically at program start-up. The initialised option map is then
 * passed to {@link Configurable} objects, which select the options
 * stored in it.</P>
 *
 * @author Robi Malik
 */
public enum OptionMap
{

  ConflictCheck
    ("conflictchecker", 0),
  ControllabilityCheck
    ("conflictchecker", 0),
  ControlLoop
    ("conflictchecker", 0),
  DeadlockCheck
    ("conflictchecker", 0),
  LanguageInclusion
    ("conflictchecker", 0),
  StateCounter
    ("conflictchecker", 0),
  SynchronousProduct
    ("conflictchecker", 0),
  Synthesis
    ("conflictchecker", 0),

  Simplifier
    ("simplifier",
     StepSimplifierFactory.class.getName(),
     SupremicaSimplifierFactory.class.getName(),
     ChainSimplifierFactory.class.getName());

  //#########################################################################
  //# Constructors
  private OptionMap(final String prefix) {
    mMap = new HashMap<>();
    //TODO Read
  }

  private OptionMap(final String prefix, final String... classNames) {
    this(prefix);
    for (final String className : classNames) {
      registerOptions(className);
    }
  }

  private OptionMap(final String prefix, final int n) {
    this(prefix);
    for (final ModelAnalyzerFactoryLoader loader :
      ModelAnalyzerFactoryLoader.values()) {
     try {
       final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();
       //TODO Check for analyzer
       //final ModelAnalyzer analyzer = createAnalyzer(factory);
       //if (analyzer != null) {
       factory.registerOptions(this);
       //  mAnalyzerComboBox.addItem(loader);
       //}
     } catch (NoClassDefFoundError |
              ClassNotFoundException |
              UnsatisfiedLinkError exception) {
       // skip this factory
     }
    }
  }

  //#########################################################################
  //# Simple Access
  public Option<?> get(final String id)
  {
    return mMap.get(id);
  }

  public void add(final Option<?> param)
  {
    final String id = param.getID();
    mMap.put(id, param);
  }


  //#########################################################################
  //# Manipulating Option Lists
  public void append(final List<Option<?>> list, final String id)
  {
    final Option<?> option = get(id);
    assert option != null;
    list.add(option);
  }

  public void prepend(final List<Option<?>> list, final String id)
  {
    final Option<?> option = get(id);
    assert option != null;
    list.add(0, option);
  }

  public boolean remove(final List<Option<?>> list, final String id)
  {
    final Iterator<Option<?>> iter = list.iterator();
    while (iter.hasNext()) {
      final Option<?> option = iter.next();
      if (option.hasID(id)) {
        iter.remove();
        return true;
      }
    }
    return false;
  }

  public Set<String> getOptionNames() {
    return mMap.entrySet()
      .stream()
      .map(e -> e.getKey())
      .collect(Collectors.toSet());
  }

  public void registerOptions(final String className) {
    try {
      final ClassLoader loader = getClass().getClassLoader();
      final Class<?> clazz = loader.loadClass(className);
      final Method method = clazz.getMethod("getInstance");
      final Object factory = method.invoke(null);
      final Method methodRegister = clazz.getMethod("registerOptions", OptionMap.class);
      methodRegister.invoke(factory, this);
    } catch (final SecurityException |
             NoSuchMethodException |
             IllegalAccessException |
             InvocationTargetException |
             ClassCastException |
             UnsatisfiedLinkError |
             ClassNotFoundException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Data Members
  private final Map<String,Option<?>> mMap;

}
