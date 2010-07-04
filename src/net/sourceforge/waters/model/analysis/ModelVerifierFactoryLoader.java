//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelVerifierFactoryLoader
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * An enumeration of available model verifier factories.
 * This is used in combination with Supremica properties to enable users
 * to choose a model verifier factory through the IDE configuration.
 *
 * @author Robi Malik
 */

public enum ModelVerifierFactoryLoader
{
  Monolithic("net.sourceforge.waters.analysis.monolithic.MonolithicModelVerifierFactory"),
  Modular("net.sourceforge.waters.analysis.modular.ModularModelVerifierFactory"),
  Native("net.sourceforge.waters.cpp.analysis.NativeModelVerifierFactory");


  //#########################################################################
  //# Constructor
  private ModelVerifierFactoryLoader(final String classname)
  {
    mClassName = classname;
  }


  //#########################################################################
  //# Constructor
  public ModelVerifierFactory getModelVerifierFactory()
    throws ClassNotFoundException
  {
    try {
      final ClassLoader loader = getClass().getClassLoader();
      final Class<?> clazz = loader.loadClass(mClassName);
      final Method method = clazz.getMethod("getInstance");
      return (ModelVerifierFactory) method.invoke(null);
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
