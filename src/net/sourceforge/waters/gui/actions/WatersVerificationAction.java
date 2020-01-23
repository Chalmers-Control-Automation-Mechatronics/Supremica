//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.analysis.options.AnalysisOptionPage;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionChangeEvent;
import net.sourceforge.waters.analysis.options.OptionChangeListener;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.gui.compiler.CompilationObserver;
import net.sourceforge.waters.gui.dialog.WatersVerificationDialog;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * @author Andrew Holland, Robi Malik, Benjamin Wheeler
 */

public abstract class WatersVerificationAction
  extends WatersAction
  implements OptionChangeListener, CompilationObserver
{

  //#########################################################################
  //# Constructor
  protected WatersVerificationAction(final IDE ide)
  {
    this(ide, null);
  }

  protected WatersVerificationAction(final IDE ide,
                                     final AnalysisOperation operation)
  {
    super(ide);
    mOperation = operation;
    ide.attach(this);
    putValue(Action.NAME, getCheckName() + " check");
    putValue(Action.SHORT_DESCRIPTION,
             "Check for " + getCheckName() + " issues");
    final EnumOption<ModelAnalyzerFactoryLoader> option = getSelectorOption();
    if (option != null) {
      option.addPropertyChangeListener(this);
    }
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    final ModuleContainer container = getActiveModuleContainer();
    container.compile(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.compiler.CompilationObserver
  @Override
  public void compilationSucceeded(final ProductDESProxy compiledDES)
  {
    final IDE ide = getIDE();
    @SuppressWarnings("unused")
    final VerificationDialog dialog =
      new VerificationDialog(ide, compiledDES, createAndConfigureModelVerifier());
  }

  @Override
  public String getVerb()
  {
    return "verified";
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface org.supremica.properties.SupremicaPropertyChangeListener
  @Override
  public void optionChanged(final OptionChangeEvent event)
  {
    updateEnabledStatus();
  }


  //#########################################################################
  //# Enablement
  void updateEnabledStatus()
  {
    final ModuleContainer container = getActiveModuleContainer();
    if (container == null) {
      setEnabled(false);
      return;
    }
    final ModelAnalyzer analyzer = createModelVerifier();
    setEnabled(analyzer != null);
  }


  //#########################################################################
  //# Factory Access
  protected String getCheckName()
  {
    return mOperation.getAnalysisName();
  }

  protected String getFailureDescription()
  {
    return mOperation.getFailureDescription();
  }

  protected String getSuccessDescription()
  {
    return mOperation.getSuccessDescription();
  }

  ModelAnalyzerFactory getModelAnalyzerFactory()
    throws ClassNotFoundException
  {
    final EnumOption<ModelAnalyzerFactoryLoader> option = getSelectorOption();
    if (option == null) {
      return null;
    } else {
      final ModelAnalyzerFactoryLoader loader = option.getValue();
      return loader.getModelAnalyzerFactory();
    }
  }

  ModelVerifier createModelVerifier()
  {
    final ProductDESProxyFactory factory = ProductDESElementFactory.getInstance();
    return createModelVerifier(factory);
  }

  ModelVerifier createModelVerifier(final ProductDESProxyFactory desFactory)
  {
    try {
      final ModelAnalyzerFactory vFactory = getModelAnalyzerFactory();
      if (vFactory == null) {
        return null;
      } else {
        final ModelVerifier verifier =
          (ModelVerifier) mOperation.createModelAnalyzer(vFactory, desFactory);
        // TODO Remove this configureFromOptions()
        vFactory.configureFromOptions(verifier);
        return verifier;
      }
    } catch (final ClassNotFoundException |
                   AnalysisConfigurationException exception) {
      return null;
    }
  }

  ModelVerifier createAndConfigureModelVerifier()
  {
    final ProductDESProxyFactory factory = ProductDESElementFactory.getInstance();
    return createAndConfigureModelVerifier(factory);
  }

  protected ModelVerifier createAndConfigureModelVerifier
    (final ProductDESProxyFactory desFactory)
  {
    final ModelVerifier verifier = createModelVerifier(desFactory);
    if (verifier == null) {
      return null;
    }
    final String prefix = mOperation.getOptionPagePrefix();
    final OptionPage map = OptionPage.getOptionPage(prefix);
    if (map != null) {
      for (final Option<?> option : verifier.getOptions(map)) {
        if (option.isPersistent()) {
          verifier.setOption(option);
        }
      }
    }
    return verifier;
  }


  //#########################################################################
  //# Auxiliary Methods
  @SuppressWarnings("unchecked")
  private EnumOption<ModelAnalyzerFactoryLoader> getSelectorOption()
  {
    if (mOperation == null) {
      return null;
    } else {
      final String optionPagePrefix =
        mOperation.getOptionPagePrefix();
      final AnalysisOptionPage page =
        (AnalysisOptionPage) OptionPage.getOptionPage(optionPagePrefix);
      return (EnumOption<ModelAnalyzerFactoryLoader>) page.getTopSelectorOption();
    }
  }


  //#########################################################################
  //# Inner Class VerificationDialog
  private class VerificationDialog extends WatersVerificationDialog
  {
    //#######################################################################
    //# Constructor
    public VerificationDialog(final IDE owner,
                              final ProductDESProxy des,
                              final ModelAnalyzer Verifier)
    {
      super(owner, des);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.dialog.WatersVerifyDialog
    @Override
    protected String getFailureDescription()
    {
      return WatersVerificationAction.this.getFailureDescription();
    }

    @Override
    protected String getSuccessDescription()
    {
      return WatersVerificationAction.this.getSuccessDescription();
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog
    @Override
    protected String getAnalysisName()
    {
      return WatersVerificationAction.this.getCheckName() + " check";
    }

    @Override
    protected ModelAnalyzer createAndConfigureModelAnalyzer()
    {
      return WatersVerificationAction.this.createAndConfigureModelVerifier();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -3327360941008729348L;
  }

  private final AnalysisOperation mOperation;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3797986885054648213L;

}
