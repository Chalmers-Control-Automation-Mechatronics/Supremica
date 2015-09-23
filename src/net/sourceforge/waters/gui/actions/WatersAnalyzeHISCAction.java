//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.gui.compiler.CompilationDialog;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * An abstract class of analysis actions for HISC properties.
 * These actions require the module to be compiled separately with
 * HISC settings.
 *
 * @author Robi Malik
 */

public abstract class WatersAnalyzeHISCAction
  extends WatersAnalyzeAction
{

  //#########################################################################
  //# Constructor
  protected WatersAnalyzeHISCAction(final IDE ide)
  {
    super(ide);
  }


  //#########################################################################
  //# Interface java.awt.ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    // TODO: Make this run in the background like normal compilation.
    final IDE ide = getIDE();
    final ModuleContainer container = getActiveModuleContainer();
    final ModuleProxy module = container.getModule();
    final DocumentManager manager = ide.getDocumentManager();
    final ProductDESProxyFactory desfactory =
      ProductDESElementFactory.getInstance();
    final ModuleCompiler compiler =
      new ModuleCompiler(manager, desfactory, module);
    compiler.setSourceInfoEnabled(false);
    compiler.setMultiExceptionsEnabled(true);
    compiler.setOptimizationEnabled(true);
    compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
    final List<String> accepting =
      Collections.singletonList(EventDeclProxy.DEFAULT_MARKING_NAME);
    compiler.setEnabledPropositionNames(accepting);
    try {
      final ProductDESProxy des = compiler.compile();
      compilationSucceeded(des);
    } catch (final EvalException exception) {
      final CompilationDialog dialog = new CompilationDialog(ide, null);
      dialog.setEvalException(exception, getVerb());
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
