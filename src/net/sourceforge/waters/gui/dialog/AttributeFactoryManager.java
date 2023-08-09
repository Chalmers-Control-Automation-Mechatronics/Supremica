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

package net.sourceforge.waters.gui.dialog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.waters.analysis.coobs.CoobservabilityAttributeFactory;
import net.sourceforge.waters.analysis.diagnosis.DiagnosabilityAttributeFactory;
import net.sourceforge.waters.analysis.hisc.HISCAttributeFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.AttributeFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.OptionChangeEvent;
import net.sourceforge.waters.model.options.OptionChangeListener;
import net.sourceforge.waters.model.options.WatersOptionPages;

import org.supremica.automata.BDD.EFA.ForcibleEventAttributeFactory;
import org.supremica.automata.BDD.EFA.TimeInvariantAttributeFactory;
import org.supremica.gui.ide.DefaultAttributeFactory;
import org.supremica.properties.Config;


/**
 * <P>A utility class to determine relevant attributes for {@link Proxy}
 * objects of given type.</P>
 *
 * <P>The AttributeFactoryManager collect all relevant attribute factories
 * and keeps them up-to-date when options are changed. On request, it
 * then provides a list of possible attribute/value pairs for an object,
 * enabling to GUI to offer appropriate attribute editing facilities based
 * on the object to be edited and the current option settings.</P>
 *
 * @see AttributeFactory
 * @see AttributesPanel
 *
 * @author Robi Malik
 */

public class AttributeFactoryManager implements OptionChangeListener
{
  //#########################################################################
  //# Singleton Pattern
  public static AttributeFactoryManager getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private AttributeFactoryManager()
  {
    register();
    update();
  }

  private static class SingletonHolder {
    private static final AttributeFactoryManager INSTANCE =
      new AttributeFactoryManager();
  }


  //#########################################################################
  //# Attribute Access
  /**
   * Gets attribute information for a given {@link Proxy} interface.
   * @param   iface  The class or interface of an object whose
   *                 attributes are to be edited.
   * @return  A map that assigns all known attributes associated with
   *          the given interface to a list of known values.
   */
  public Map<String,List<String>> getAttributeInfo
    (final Class<? extends Proxy> iface)
  {
    Map<String,List<String>> info = mAttributeInfoMap.get(iface);
    if (info == null) {
      info = new TreeMap<>();
      for (final AttributeFactory factory : mAttributeFactories) {
        for (final String attrib : factory.getApplicableKeys(iface)) {
          final List<String> values = factory.getApplicableValues(attrib);
          info.put(attrib, values);
        }
      }
      mAttributeInfoMap.put(iface, info);
    }
    return info;
  }

  /**
   * Gets attribute information for a given {@link Proxy} interface.
   * This method calls {@link #getAttributeInfo(Class) getAttributeInfo()}
   * for a singleton instance of this class.
   * @param   iface  The class or interface of an object whose
   *                 attributes are to be edited.
   * @return  A map that assigns all known attributes associated with
   *          the given interface to a list of known values.
   */
  public static Map<String,List<String>> getGlobalAttributeInfo
    (final Class<? extends Proxy> iface)
  {
    final AttributeFactoryManager instance = getInstance();
    return instance.getAttributeInfo(iface);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.OptionChangeListener
  @Override
  public void optionChanged(final OptionChangeEvent event)
  {
    update();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void register()
  {
    final EnumOption<ModelAnalyzerFactoryLoader> coobservabilityOption =
      WatersOptionPages.COOBSERVABILITY.getTopSelectorOption();
    coobservabilityOption.addOptionChangeListener(this);
    final EnumOption<ModelAnalyzerFactoryLoader> diagnosabilityOption =
      WatersOptionPages.DIAGNOSABILITY.getTopSelectorOption();
    diagnosabilityOption.addOptionChangeListener(this);
    Config.GUI_ANALYZER_INCLUDE_HISC.addOptionChangeListener(this);
    Config.INCLUDE_RAS_SUPPORT.addOptionChangeListener(this);
  }

  private void update()
  {
    mAttributeFactories.clear();
    mAttributeInfoMap.clear();
    mAttributeFactories.add(DefaultAttributeFactory.getInstance());
    final EnumOption<ModelAnalyzerFactoryLoader> coobservabilityOption =
      WatersOptionPages.COOBSERVABILITY.getTopSelectorOption();
    if (coobservabilityOption.getValue() != ModelAnalyzerFactoryLoader.Disabled) {
      mAttributeFactories.add(CoobservabilityAttributeFactory.getInstance());
    }
    final EnumOption<ModelAnalyzerFactoryLoader> diagnosabilityOption =
      WatersOptionPages.DIAGNOSABILITY.getTopSelectorOption();
    if (diagnosabilityOption.getValue() != ModelAnalyzerFactoryLoader.Disabled) {
      mAttributeFactories.add(DiagnosabilityAttributeFactory.getInstance());
    }
    if (Config.GUI_ANALYZER_INCLUDE_HISC.getValue()) {
      mAttributeFactories.add(HISCAttributeFactory.getInstance());
    }
    // A condition could be added to check if the model contains any clocks
    if (Config.INCLUDE_RAS_SUPPORT.getValue()) {
      mAttributeFactories.add(TimeInvariantAttributeFactory.getInstance());
      mAttributeFactories.add(ForcibleEventAttributeFactory.getInstance());
    }
  }


  //#########################################################################
  //# Data Members
  private final List<AttributeFactory>
    mAttributeFactories = new LinkedList<>();
  private final Map<Class<? extends Proxy>,Map<String,List<String>>>
    mAttributeInfoMap = new HashMap<>();

}
