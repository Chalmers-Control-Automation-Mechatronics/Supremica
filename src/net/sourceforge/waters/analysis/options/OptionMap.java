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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierCreator;
import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierFactory;
import net.sourceforge.waters.analysis.abstraction.StepSimplifierFactory;
import net.sourceforge.waters.analysis.trcomp.ChainSimplifierFactory;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


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
 * @author Robi Malik, Benjamin Wheeler
 */
public enum OptionMap
{

  ConflictCheck
    ("waters.analysis/conflict", AnalysisOperation.CONFLICT_CHECK),
  ControllabilityCheck
    ("waters.analysis/controllability", AnalysisOperation.CONTROLLABILITY_CHECK),
  ControlLoop
    ("waters.analysis/loop", AnalysisOperation.CONTROL_LOOP_CHECK),
  DeadlockCheck
    ("waters.analysis/deadlock", AnalysisOperation.DEADLOCK_CHECK),
  LanguageInclusion
    ("waters.analysis/languageinclusion", AnalysisOperation.LANGUAGE_INCLUSION_CHECK),
  StateCounter
    ("waters.analysis/statecount", AnalysisOperation.STATE_COUNTER),
  SynchronousProduct
    ("waters.analysis/syncprod", AnalysisOperation.SYNCHRONOUS_PRODUCT),
  Synthesis
    ("waters.analysis/synthesis", AnalysisOperation.SUPERVISOR_SYNTHESIZER),

  Simplifier
    ("waters.analysis/simplification",
     0L,
     StepSimplifierFactory.class.getName(),
     "org.supremica.automata.waters.SupremicaSimplifierFactory",
     ChainSimplifierFactory.class.getName());


  //#########################################################################
  //# Constructors
  private OptionMap(final String identifier)
  {

    mMap = new HashMap<>();
    mIdentifier = identifier;
    mTopOptionSubset = new OptionSubset(true);
    //TODO Read
  }

  private OptionMap(final String identifier, final String... classNames)
  {
    this(identifier);
    for (final String className : classNames) {
      registerOptions(className);
    }
  }

  private OptionMap(final String identifier,
                    final long dummy,
                    final String... classNames) {
    this(identifier);

    mTopOptionSubset.setTitle("Family");

    for (final String className : classNames) {
      try {
        final ClassLoader loader = getClass().getClassLoader();
        final Class<?> clazz = loader.loadClass(className);
        final Method method = clazz.getMethod("getInstance");
        final AutomatonSimplifierFactory factory =
          (AutomatonSimplifierFactory) method.invoke(null);
        factory.registerOptions(this);

        final OptionSubset factorySubset = new OptionSubset();
        mTopOptionSubset.addSubset(factory, factorySubset);
        factorySubset.setTitle("Simplifier");
        for (final AutomatonSimplifierCreator creator :
            factory.getSimplifierCreators()) {
          final OptionSubset simplifierSubset = new OptionSubset();
          factorySubset.addSubset(creator, simplifierSubset);
          simplifierSubset.setDescription(creator.getDescription());
          for (final Option<?> option : creator.getOptions(this)) {
            simplifierSubset.getOptionNames().add(option.getID());
          }
          final AutomatonBuilder builder = creator.createBuilder
            (ProductDESElementFactory.getInstance());
          for (final Option<?> option : builder.getOptions(this)) {
            simplifierSubset.getOptionNames().add(option.getID());
          }
        }
        factorySubset.buildSelectedObjectOption(null);//TODO Load
      } catch (NoClassDefFoundError |
               ClassNotFoundException |
               UnsatisfiedLinkError |
               NoSuchMethodException |
               SecurityException |
               IllegalAccessException |
               IllegalArgumentException |
               InvocationTargetException exception) {
        // skip this factory
      }
      mTopOptionSubset.buildSelectedObjectOption(null);//TODO Load
    }

  }

  private OptionMap(final String identifier, final AnalysisOperation operation) {
    this(identifier);

    mTopOptionSubset.setTitle("Algorithm");

    for (final ModelAnalyzerFactoryLoader loader :
      ModelAnalyzerFactoryLoader.values()) {
      try {
        final ModelAnalyzerFactory factory = loader.getModelAnalyzerFactory();

        final ProductDESProxyFactory desFactory =
          ProductDESElementFactory.getInstance();
        final ModelAnalyzer analyzer =
          operation.createModelAnalyzer(factory, desFactory);

        if (analyzer != null) {
          final OptionSubset algorithmSubset = new OptionSubset();
          mTopOptionSubset.addSubset(loader, algorithmSubset);
          factory.registerOptions(this);
          for (final Option<?> option : analyzer.getOptions(this)) {
            algorithmSubset.getOptionNames().add(option.getID());
          }
        }
      } catch (ClassNotFoundException |
               AnalysisConfigurationException exception) {
        // skip this factory
      }
    }
    mTopOptionSubset.buildSelectedObjectOption(null);
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

  public String getIdentifier() {
    return mIdentifier;
  }

  public String getPrefix() {
    return mIdentifier.replace('/', '.');
  }

  public boolean hasSubsets() {
    return mTopOptionSubset.hasSubsets();
  }

  public OptionSubset getTopOptionSubset() {
    return mTopOptionSubset;
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
      final Method methodRegister = clazz.getMethod("registerOptions",
                                                    OptionMap.class);
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

  public static OptionMap getOptionMap(final String prefix) {
    for (final OptionMap map : OptionMap.values()) {
      if (map.getPrefix().equals(prefix)) return map;
    }
    return null;
  }

  public static class OptionSubset
  {

    private OptionSubset(final boolean root)
    {
      this();
      if (root) mSelectorKey = "subsetselector";
    }

    private OptionSubset()
    {
      mTitle = "";
      mSubsetMap = new HashMap<>();
      mSubsetKeys = new LinkedList<>();
      mOptionNames = new LinkedList<>();
    }

    public List<Object> getSubsetKeys()
    {
      return mSubsetKeys;
    }

    public OptionSubset getSubset(final Object key)
    {
      return mSubsetMap.get(key);
    }

    public List<String> getOptionNames()
    {
      return mOptionNames;
    }

    public boolean hasSubsets()
    {
      return mSubsetMap.size() != 0;
    }

    public void setTitle(final String title)
    {
      mTitle = title;
    }

    public String getTitle() {
      return mTitle;
    }

    public void setDescription(final String description)
    {
      mDescription = description;
    }

    public String getDescription()
    {
      return mDescription;
    }

    public void setSelectorKey(final String key)
    {
      mSelectorKey = key;
    }

    public String getSelectorKey()
    {
      return mSelectorKey;
    }

    private void addSubset(final Object key, final OptionSubset subset)
    {
      addSubset(key, subset, v -> v.toString());
    }

    private void addSubset(final Object key, final OptionSubset subset,
                           final Function<Object, String> keyTranslator)
    {
      mSubsetMap.put(key, subset);
      mSubsetKeys.add(key);
      final String selectorKey = getSelectorKey() + '.' +
        keyTranslator.apply(key);
      subset.setSelectorKey(selectorKey);
    }

    private void buildSelectedObjectOption(final Object defaultKey)
    {

      final EnumFactory<Object> enumFactory =
        new ListedEnumFactory<Object>() {
        {
          for (final Object key : mSubsetKeys) {
            register(key, key == defaultKey);
          }
        }
      };

      final String id = getSelectorKey();
      mSelectedObjectOption = new EnumOption<Object>(id,
        null, null, null,
        enumFactory);
    }

    public void setSelected(final Object value)
    {
      if (mSelectedObjectOption == null) return;
      mSelectedObjectOption.setValue(value);
    }

    public Object getSelected() {
      return mSelectedObjectOption.getValue();
    }

    private final Map<Object, OptionSubset> mSubsetMap;
    private final List<Object> mSubsetKeys;
    private final List<String> mOptionNames;
    private String mTitle;
    private String mDescription;
    private String mSelectorKey;
    private EnumOption<Object> mSelectedObjectOption;

  }


  //#########################################################################
  //# Data Members
  private final Map<String, Option<?>> mMap;
  private final String mIdentifier;

  private final OptionSubset mTopOptionSubset;



}
