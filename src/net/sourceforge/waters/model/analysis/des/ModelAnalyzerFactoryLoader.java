//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis.des
//# CLASS:   ModelAnalyzerFactoryLoader
//###########################################################################
//# $Id$
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
  BDD("net.sourceforge.waters.analysis.bdd.BDDModelVerifierFactory"),
  Compositional("net.sourceforge.waters.analysis.compositional.CompositionalModelAnalyzerFactory"),
  EnabledEvents("net.sourceforge.waters.analysis.compositional.EnabledEventsCompositionalModelVerifierFactory"),
  GNB("net.sourceforge.waters.analysis.gnonblocking.GNBModelVerifierFactory"),
  Modular("net.sourceforge.waters.analysis.modular.ModularModelVerifierFactory"),
  Monolithic("net.sourceforge.waters.analysis.monolithic.MonolithicModelAnalyzerFactory"),
  Native("net.sourceforge.waters.cpp.analysis.NativeModelVerifierFactory"),
  PartialOrder("net.sourceforge.waters.analysis.po.PartialOrderModelVerifierFactory"),
  Projecting("net.sourceforge.waters.analysis.modular.ProjectingModelVerifierFactory");


  //#########################################################################
  //# Constructor
  private ModelAnalyzerFactoryLoader(final String classname)
  {
    mClassName = classname;
  }


  //#########################################################################
  //# Constructor
  public ModelAnalyzerFactory getModelAnalyzerFactory()
    throws ClassNotFoundException
  {
    try {
      final ClassLoader loader = getClass().getClassLoader();
      final Class<?> clazz = loader.loadClass(mClassName);
      final Method method = clazz.getMethod("getInstance");
      return (ModelAnalyzerFactory) method.invoke(null);
    } catch (final SecurityException exception) {
      throw wrap(exception);
    } catch (final NoSuchMethodException exception) {
      throw wrap(exception);
    } catch (final IllegalAccessException exception) {
      throw wrap(exception);
    } catch (final InvocationTargetException exception) {
      throw wrap(exception);
    } catch (final ClassCastException exception) {
      throw wrap(exception);
    } catch (final UnsatisfiedLinkError exception) {
      throw wrap(exception);
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

}
