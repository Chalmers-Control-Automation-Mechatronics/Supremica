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

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ControlLoopChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A configurable parameter to specify a secondary model analyser factory.
 *
 * @author Benjamin Wheeler
 */

public class ChainOption extends EnumOption<ModelAnalyzerFactoryLoader>
{
  //#########################################################################
  //# Constructors
  public ChainOption(final String id,
                     final String shortName,
                     final String description,
                     final String commandLineOption)
  {
   super(id, shortName, description, commandLineOption,
         ModelAnalyzerFactoryLoader.values(),
         ModelAnalyzerFactoryLoader.DEFAULT);
  }


  //#########################################################################
  //# Type-specific Access
  public void setSecondaryFactory(final ModelAnalyzerFactory secondaryFactory)
  {
    mSecondaryFactory = secondaryFactory;
  }

  public ModelAnalyzerFactory getSecondaryFactory()
  {
    return mSecondaryFactory;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.Option
  @Override
  public OptionEditor<ModelAnalyzerFactoryLoader> createEditor(final OptionContext context)
  {
    return context.createChainEditor(this);
  }


  //#######################################################################
  //# Configuring the Secondary Verifier
  public ModelAnalyzer createSecondaryAnalyzer(final ModelAnalyzer analyzer)
  {
    try {
      final ModelAnalyzerFactory secondaryFactory = getValue().getModelAnalyzerFactory();
      final ProductDESProxyFactory desFactory = analyzer.getFactory();
      final ModelVerifier secondaryAnalyzer;
      if (analyzer instanceof ConflictChecker) {
        secondaryAnalyzer =
          secondaryFactory.createConflictChecker(desFactory);
      } else if (analyzer instanceof ControllabilityChecker) {
        secondaryAnalyzer =
          secondaryFactory.createControllabilityChecker(desFactory);
      } else if (analyzer instanceof ControlLoopChecker) {
        secondaryAnalyzer =
          secondaryFactory.createControlLoopChecker(desFactory);
      } else if (analyzer instanceof LanguageInclusionChecker) {
        secondaryAnalyzer =
          secondaryFactory.createLanguageInclusionChecker(desFactory);
      } else {
        failUnsupportedAnalyzerClass(analyzer);
        return null;
      }
      return secondaryAnalyzer;
    } catch (final ClassNotFoundException exception) {
      fail("Secondary factory not found!");
      return null;
    } catch (final AnalysisConfigurationException exception) {
      fail("Could not create an analyzer!");
      return null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void failUnsupportedAnalyzerClass(final Object analyzer)
  {
    fail(ProxyTools.getShortClassName(analyzer) +
         " does not support secondary verifier!");
  }

  public static void fail(final String msg)
  {
    System.err.println(msg);
    System.exit(1);
  }


  //#########################################################################
  //# Data Members
  private ModelAnalyzerFactory mSecondaryFactory;

}
