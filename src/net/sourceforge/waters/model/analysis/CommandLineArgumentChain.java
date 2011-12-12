//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentChain
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import net.sourceforge.waters.model.compiler.ModuleCompiler;


/**
 * <P>A command line argument specifying a second {@link ModelVerifierFactory}.
 * The <CODE>-chain</CODE> command line argument is followed by the class name
 * of a {@link ModelVerifierFactory}. It stops all command line argument
 * processing by the current model verifier factory and hands over to the
 * secondary factory.</P>
 *
 * <P>This abstract class needs to be further subclassed to obtain a secondary
 * model verifier from the factory and configure the primary model verifier
 * to use it.</P>
 *
 * @author Robi Malik
 */

public abstract class CommandLineArgumentChain
  extends CommandLineArgument
{

  //#########################################################################
  //# Constructors
  protected CommandLineArgumentChain()
  {
    super("-chain", "Specify secondary model verifier factory and arguments");
  }


  //#######################################################################
  //# Simple Access
  protected String getArgumentTemplate()
  {
    return "<factory>";
  }

  protected ModelVerifierFactory getSecondaryFactory()
  {
    return mSecondaryFactory;
  }


  //#######################################################################
  //# Parsing
  @Override
  protected void parse(final Iterator<String> iter)
  {
    if (iter.hasNext()) {
      final String factoryname = iter.next();
      try {
        final ClassLoader loader = getClass().getClassLoader();
        final Class<?> fclazz = loader.loadClass(factoryname);
        final Method getinst = fclazz.getMethod("getInstance");
        mSecondaryFactory = (ModelVerifierFactory) getinst.invoke(null);
      } catch (final ClassNotFoundException exception) {
        fail("Can't find factory " + factoryname + "!");
      } catch (final SecurityException exception) {
        fail("Invalid factory " + factoryname + "!");
      } catch (final NoSuchMethodException exception) {
        fail("Invalid factory " + factoryname + "!");
      } catch (final IllegalAccessException exception) {
        fail("Invalid factory " + factoryname + "!");
      } catch (final InvocationTargetException exception) {
        fail("Invalid factory " + factoryname + "!");
      }
      iter.remove();
      setUsed(true);
      mSecondaryFactory.parse(iter);
    } else {
      failMissingValue();
    }
  }

  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    mSecondaryFactory.configure(compiler);
  }


  //#########################################################################
  //# Data Members
  private ModelVerifierFactory mSecondaryFactory;

}
