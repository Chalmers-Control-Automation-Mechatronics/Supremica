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

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JFrame;

import net.sourceforge.waters.gui.compiler.CompilationObserver;
import net.sourceforge.waters.gui.dialog.AnalyzeDialog;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


/**
 * @author Andrew Holland, Robi Malik
 */

public abstract class WatersAnalyzeAction
  extends WatersAction
  implements SupremicaPropertyChangeListener, CompilationObserver
{

  //#########################################################################
  //# Constructor
  protected WatersAnalyzeAction(final IDE ide)
  {
    super(ide);
    ide.attach(this);
    putValue(Action.NAME, getCheckName() + " check");
    putValue(Action.SHORT_DESCRIPTION,
             "Check for " + getCheckName() + " issues");
    Config.GUI_ANALYZER_USED_FACTORY.addPropertyChangeListener(this);
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
    final AnalyzerDialog dialog = new AnalyzerDialog(ide, compiledDES, getModelVerifier(), ide);
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
  public void propertyChanged(final SupremicaPropertyChangeEvent event)
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
    final ModelVerifier verifier = getModelVerifier();
    setEnabled(verifier != null);
  }


  //#########################################################################
  //# Factory Access
  ModelAnalyzerFactory getModelVerifierFactory()
    throws ClassNotFoundException
  {
    final ModelAnalyzerFactoryLoader loader =
      Config.GUI_ANALYZER_USED_FACTORY.get();
    return loader.getModelAnalyzerFactory();
  }

  ModelVerifier getModelVerifier()
  {
    try {
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final ModelAnalyzerFactory vFactory = getModelVerifierFactory();
      final ModelVerifier verifier = getModelVerifier(vFactory, desFactory);
      vFactory.configureFromOptions(verifier);
      return verifier;
    } catch (final NoClassDefFoundError |
                   ClassNotFoundException |
                   UnsupportedOperationException |
                   UnsatisfiedLinkError |
                   AnalysisConfigurationException exception) {
      return null;
    }
  }


  //#########################################################################
  // # Abstract Methods
  protected abstract String getCheckName();
  protected abstract String getFailureDescription();
  protected abstract String getSuccessDescription();
  protected abstract ModelVerifier getModelVerifier
    (ModelAnalyzerFactory factory, ProductDESProxyFactory desFactory)
    throws AnalysisConfigurationException;


//#########################################################################
  //# Inner Class AnalyzerDialog
  private class AnalyzerDialog extends AnalyzeDialog
  {

    public AnalyzerDialog(final JFrame owner, final ProductDESProxy des,
                          final ModelAnalyzer Verifier, final IDE ide)
    {
      super(owner, des, Verifier, ide);
    }

    @Override
    protected String getCheckName()
    {
      // TODO Auto-generated method stub
      return WatersAnalyzeAction.this.getCheckName();
    }

    @Override
    protected String getFailureDescription()
    {
      // TODO Auto-generated method stub
      return WatersAnalyzeAction.this.getFailureDescription();
    }

    @Override
    protected String getSuccessDescription()
    {
      // TODO Auto-generated method stub
      return WatersAnalyzeAction.this.getSuccessDescription();
    }

    @Override
    protected ModelAnalyzer getModelVerifier(final ModelAnalyzerFactory factory,
                                             final ProductDESProxyFactory desFactory)
      throws AnalysisConfigurationException
    {
      return WatersAnalyzeAction.this.getModelVerifier(factory, desFactory);
    }
    private static final long serialVersionUID = -3797986885054648213L;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3797986885054648213L;

}
