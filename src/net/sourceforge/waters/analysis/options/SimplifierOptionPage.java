//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierCreator;
import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierFactory;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

/**
 *
 * @author Benjamin Wheeler
 */
public class SimplifierOptionPage extends SelectorLeafOptionPage
{

  protected SimplifierOptionPage(final String prefix, final String title,
                                 final String...classNames)
  {
    super(prefix, title);
    mCreatorOptionMap = new HashMap<>();
    mCreatorOptions = new LinkedList<>();
    final List<AutomatonSimplifierFactory> factories =
      getFamilies(classNames);
    mFamilyOption = new SimplifierFamilyOption(prefix+".FamilySelector",
                                               factories, null);//TODO
    add(mFamilyOption);
    for (final AutomatonSimplifierFactory factory : factories) {
      final List<AutomatonSimplifierCreator> creators =
        factory.getSimplifierCreators();
      final SimplifierCreatorOption creatorOption =
        new SimplifierCreatorOption(prefix+".SimplifierSelector."+factory,
                                    creators, null);//TODO
      addCreatorOption(creatorOption, factory);
    }
  }


  private List<AutomatonSimplifierFactory>
    getFamilies(final String... classNames)
  {

    final List<AutomatonSimplifierFactory> families = new LinkedList<>();

    for (final String className : classNames) {
      try {
        final ClassLoader loader = getClass().getClassLoader();
        final Class<?> clazz = loader.loadClass(className);
        final Method method = clazz.getMethod("getInstance");
        final AutomatonSimplifierFactory factory =
          (AutomatonSimplifierFactory) method.invoke(null);
        factory.registerOptions(this);
        families.add(factory);
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
    }

    return families;

  }

  private void addOptions(final List<Option<?>> options,
                          final AutomatonSimplifierCreator creator)
  {
    for (final Option<?> option : creator.getOptions(this)) {
      //Get the option instance known by this OptionPage
      final Option<?> optionInstance = get(option.getID());
      options.add(optionInstance);
    }
    final AutomatonBuilder builder = creator.createBuilder
      (ProductDESElementFactory.getInstance());
    for (final Option<?> option : builder.getOptions(this)) {
      //Get the option instance known by this OptionPage
      final Option<?> optionInstance = get(option.getID());
      options.add(optionInstance);
    }
  }

  private void addCreatorOption(final SimplifierCreatorOption creatorOption,
                                final AutomatonSimplifierFactory factory)
  {
    mCreatorOptionMap.put(factory, creatorOption);
    mCreatorOptions.add(creatorOption);
    add(creatorOption);
  }

  @Override
  public List<Option<?>> getOptionsForSelector
    (final SelectorOption<?> selectorOption, final Object key)
  {
    final List<Option<?>> options = new LinkedList<>();
    addOptions(options, (AutomatonSimplifierCreator) key);
    options.add(get(AutomatonSimplifierFactory.
                    OPTION_AutomatonSimplifierFactory_KeepOriginal));
    return options;
  }


  @Override
  public SelectorOption<?> getTopSelectorOption()
  {
    return mFamilyOption;
  }

  public List<SelectorOption<?>> getSubSelectors
    (final SelectorOption<?> selectorOption)
  {
    if (selectorOption == mFamilyOption) return mCreatorOptions;
    else return null;
  }

  @Override
  public SelectorOption<?> getSubSelector
    (final SelectorOption<?> selectorOption, final Object key)
  {
    if (selectorOption == mFamilyOption
      && key instanceof AutomatonSimplifierFactory) {
      return mCreatorOptionMap.get(key);
    }
    else return null;
  }

  private final SimplifierFamilyOption mFamilyOption;
  private final Map<AutomatonSimplifierFactory, SimplifierCreatorOption>
    mCreatorOptionMap;
  private final List<SelectorOption<?>> mCreatorOptions;

}
