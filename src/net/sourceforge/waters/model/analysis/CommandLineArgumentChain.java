//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineArgumentChain
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Iterator;

import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ControlLoopChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A command line argument specifying a second {@link ModelAnalyzerFactory}.
 * The <CODE>-chain</CODE> command line argument is followed by the class name
 * of a {@link ModelAnalyzerFactory}. It stops all command line argument
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
  extends CommandLineArgumentEnum<ModelAnalyzerFactoryLoader>
{

  //#########################################################################
  //# Constructors
  protected CommandLineArgumentChain()
  {
    super("-chain", "Specify secondary model verifier factory and arguments",
          ModelAnalyzerFactoryLoader.class);
  }


  //#######################################################################
  //# Simple Access
  @Override
  protected String getArgumentTemplate()
  {
    return "<factory>";
  }

  protected ModelAnalyzerFactory getSecondaryFactory()
  {
    return mSecondaryFactory;
  }

  //#######################################################################
  //# Configuring the Secondary Verifier
  protected ModelAnalyzer createSecondaryAnalyzer(final ModelAnalyzer analyzer)
  {
    final ProductDESProxyFactory desFactory = analyzer.getFactory();
    final ModelVerifier secondaryAnalyzer;
    if (analyzer instanceof ConflictChecker) {
      secondaryAnalyzer =
        mSecondaryFactory.createConflictChecker(desFactory);
    } else if (analyzer instanceof ControllabilityChecker) {
      secondaryAnalyzer =
        mSecondaryFactory.createControllabilityChecker(desFactory);
    } else if (analyzer instanceof ControlLoopChecker) {
      secondaryAnalyzer =
        mSecondaryFactory.createControlLoopChecker(desFactory);
    } else if (analyzer instanceof LanguageInclusionChecker) {
      secondaryAnalyzer =
        mSecondaryFactory.createLanguageInclusionChecker(desFactory);
    } else {
      failUnsupportedAnalyzerClass(analyzer);
      return null;
    }
    mSecondaryFactory.configure(secondaryAnalyzer);
    return secondaryAnalyzer;
  }

  protected void failUnsupportedAnalyzerClass(final ModelAnalyzer analyzer)
  {
    fail(ProxyTools.getShortClassName(analyzer) +
         " does not support secondary verifier!");
  }


  //#######################################################################
  //# Parsing
  @Override
  public void parse(final Iterator<String> iter)
  {
    super.parse(iter);
    final ModelAnalyzerFactoryLoader loader = getValue();
    final String factoryName = loader.toString();
    try {
      mSecondaryFactory = loader.getModelAnalyzerFactory();
    } catch (final ClassNotFoundException exception) {
      fail("Can't load factory " + factoryName + "!");
    }
    mSecondaryFactory.parse(iter);
  }

  @Override
  public void configure(final ModuleCompiler compiler)
  {
    mSecondaryFactory.configure(compiler);
  }


  //#########################################################################
  //# Data Members
  private ModelAnalyzerFactory mSecondaryFactory;

}
