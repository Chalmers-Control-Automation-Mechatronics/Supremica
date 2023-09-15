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

package net.sourceforge.waters.model.analysis.des;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * An enumeration of available model verifier factories.
 * This is used in combination with Supremica properties to enable users
 * to choose a model verifier factory through the IDE configuration dialog
 * as well as the command line tool.
 *
 * @author Robi Malik
 */

public enum ModelAnalyzerFactoryLoader
{
  Disabled("net.sourceforge.waters.model.analysis.des.DisabledModelAnalyzerFactory", "-disabled") {
    @Override
    public String toString() { return "(disabled)"; }
  },
  BDD("net.sourceforge.waters.analysis.bdd.BDDModelVerifierFactory", "-bdd"),
  Compositional("net.sourceforge.waters.analysis.compositional.CompositionalModelAnalyzerFactory", "-comp"),
  GNB("net.sourceforge.waters.analysis.gnonblocking.GNBModelVerifierFactory", "-gnb"),
  Modular("net.sourceforge.waters.analysis.modular.ModularModelVerifierFactory", "-mod"),
  Monolithic("net.sourceforge.waters.analysis.monolithic.MonolithicModelAnalyzerFactory", "-mono"),
  Native("net.sourceforge.waters.cpp.analysis.NativeModelVerifierFactory", "-native"),
  PartialOrder("net.sourceforge.waters.analysis.po.PartialOrderModelVerifierFactory", "-po"),
  Supremica("org.supremica.automata.waters.SupremicaModelAnalyzerFactory", "-sup"),
  TRCompositional("net.sourceforge.waters.analysis.trcomp.TRCompositionalModelAnalyzerFactory", "-trcomp"),
  TRMonolithic("net.sourceforge.waters.analysis.monolithic.TRMonolithicModelAnalyzerFactory", "-trmono");


  //#########################################################################
  //# Constructor
  private ModelAnalyzerFactoryLoader(final String classname,
                                     final String consoleName)
  {
    mClassName = classname;
    mConsoleName = consoleName;
  }


  //#########################################################################
  //# Simple Access
  public String getConsoleName()
  {
    return mConsoleName;
  }


  //#########################################################################
  //# Invocation
  public ModelAnalyzerFactory getModelAnalyzerFactory()
    throws ClassNotFoundException
  {
    try {
      final ClassLoader loader = getClass().getClassLoader();
      final Class<?> clazz = loader.loadClass(mClassName);
      final Method method = clazz.getMethod("getInstance");
      return (ModelAnalyzerFactory) method.invoke(null);
    } catch (final SecurityException |
             NoSuchMethodException |
             IllegalAccessException |
             InvocationTargetException |
             ClassCastException |
             UnsatisfiedLinkError exception) {
      throw wrap(exception);
    }
  }

  public boolean isLoadable()
  {
    try {
      getModelAnalyzerFactory();
      return true;
    } catch (final ClassNotFoundException exception) {
      return false;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private ClassNotFoundException wrap(final Throwable exception)
  {
    final String msg = exception.getMessage();
    final ClassNotFoundException rethrow = new ClassNotFoundException(msg);
    rethrow.initCause(exception);
    return rethrow;
  }


  //#########################################################################
  //# Creating Enum Factories
  public static EnumFactory<ModelAnalyzerFactoryLoader> getEnumFactory()
  {
    return ModelAnalyzerFactoryLoaderEnumFactory.INSTANCE;
  }

  public static EnumFactory<ModelAnalyzerFactoryLoader> createEnumFactory
    (final AnalysisOperation operation, final boolean canBeDisabled)
  {
    return new ModelAnalyzerFactoryLoaderEnumFactory(operation, canBeDisabled);
  }

  public static EnumFactory<ModelAnalyzerFactoryLoader> createEnumFactory
    (final List<ModelAnalyzerFactoryLoader> loaders,
     final ModelAnalyzerFactoryLoader suppressedLoader)
  {
    return new ModelAnalyzerFactoryLoaderEnumFactory(loaders, suppressedLoader);
  }


  private static class ModelAnalyzerFactoryLoaderEnumFactory
    extends ListedEnumFactory<ModelAnalyzerFactoryLoader>
  {
    //#######################################################################
    //# Constructors
    private ModelAnalyzerFactoryLoaderEnumFactory()
    {
      for (final ModelAnalyzerFactoryLoader loader :
           ModelAnalyzerFactoryLoader.values()) {
        if (loader.getConsoleName() != null) {
          register(loader, loader == Monolithic);
        }
      }
    }

    private ModelAnalyzerFactoryLoaderEnumFactory
      (final AnalysisOperation operation, final boolean canBeDisabled)
    {
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      for (final ModelAnalyzerFactoryLoader loader :
           ModelAnalyzerFactoryLoader.values()) {
        if (loader == Disabled && canBeDisabled) {
          register(loader);
        } else {
          try {
            final ModelAnalyzerFactory factory =
              loader.getModelAnalyzerFactory();
            final ModelAnalyzer analyzer =
              operation.createModelAnalyzer(factory, desFactory);
            if (analyzer != null) {
              register(loader);
            }
          } catch (ClassNotFoundException |
                   AnalysisConfigurationException |
                   UnsatisfiedLinkError |
                   NoClassDefFoundError exception) {
            // skip this factory
          }
        }
      }
      findDefault();
    }

    private ModelAnalyzerFactoryLoaderEnumFactory
      (final List<ModelAnalyzerFactoryLoader> loaders,
       final ModelAnalyzerFactoryLoader suppressedLoader)
    {
      for (final ModelAnalyzerFactoryLoader loader : loaders) {
        if (loader != suppressedLoader) {
          register(loader);
        }
      }
      findDefault();
    }

    private void findDefault()
    {
      for (final ModelAnalyzerFactoryLoader loader : getEnumConstants()) {
        switch (loader) {
        case Disabled:
        case Native:
          setDefaultValue(loader);
          return;
        case Monolithic:
          setDefaultValue(Monolithic);
          break;
        default:
          break;
        }
      }
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.EnumFactory<AnalysisOperation>
    @Override
    public ModelAnalyzerFactoryLoader getEnumValue(final String name)
    {
      final ModelAnalyzerFactoryLoader loader = super.getEnumValue(name);
      if (loader != null) {
        return loader;
      } else {
        return getEnumValueFallback(name);
      }
    }

    @Override
    public String getConsoleName(final ModelAnalyzerFactoryLoader loader)
    {
      return loader.getConsoleName();
    }

    @Override
    public boolean isDisplayedInConsole(final ModelAnalyzerFactoryLoader loader)
    {
      return loader != Disabled;
    }

    //#######################################################################
    //# Singleton Instance
    private static ModelAnalyzerFactoryLoaderEnumFactory INSTANCE =
      new ModelAnalyzerFactoryLoaderEnumFactory();
  }


  //#########################################################################
  //# Data Members
  private String mClassName;
  private String mConsoleName;

}
