//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.model.analysis;

import java.util.ListIterator;

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
    throws AnalysisConfigurationException
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

  protected void failUnsupportedAnalyzerClass(final Object analyzer)
  {
    fail(ProxyTools.getShortClassName(analyzer) +
         " does not support secondary verifier!");
  }


  //#######################################################################
  //# Parsing
  @Override
  public void parse(final ListIterator<String> iter)
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
  public void configureCompiler(final ModuleCompiler compiler)
  {
    mSecondaryFactory.configure(compiler);
  }


  //#########################################################################
  //# Data Members
  private ModelAnalyzerFactory mSecondaryFactory;

}
