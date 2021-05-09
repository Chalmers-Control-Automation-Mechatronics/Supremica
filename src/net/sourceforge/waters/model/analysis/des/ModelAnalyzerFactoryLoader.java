//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
  Disabled("net.sourceforge.waters.model.analysis.des.DisabledModelAnalyzerFactory") {
    @Override
    public String toString() { return "(disabled)"; }
  },
  BDD("net.sourceforge.waters.analysis.bdd.BDDModelVerifierFactory"),
  Compositional("net.sourceforge.waters.analysis.compositional.CompositionalModelAnalyzerFactory"),
  GNB("net.sourceforge.waters.analysis.gnonblocking.GNBModelVerifierFactory"),
  Modular("net.sourceforge.waters.analysis.modular.ModularModelVerifierFactory"),
  Monolithic("net.sourceforge.waters.analysis.monolithic.MonolithicModelAnalyzerFactory"),
  Native("net.sourceforge.waters.cpp.analysis.NativeModelVerifierFactory"),
  PartialOrder("net.sourceforge.waters.analysis.po.PartialOrderModelVerifierFactory"),
  Supremica("org.supremica.automata.waters.SupremicaModelAnalyzerFactory"),
  TRCompositional("net.sourceforge.waters.analysis.trcomp.TRCompositionalModelAnalyzerFactory"),
  TRMonolithic("net.sourceforge.waters.analysis.monolithic.TRMonolithicModelAnalyzerFactory");


  //#########################################################################
  //# Constructor
  private ModelAnalyzerFactoryLoader(final String classname)
  {
    mClassName = classname;
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
  //# Data Members
  private String mClassName;


  //#########################################################################
  //# Class Constants
  public static final ModelAnalyzerFactoryLoader DEFAULT = Monolithic;

}
