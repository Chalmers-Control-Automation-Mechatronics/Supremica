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

package net.sourceforge.waters.gui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * @author Robi Malik
 */

public abstract class WatersVerificationDialog extends WatersAnalyzeDialog
{

  //#########################################################################
  //# Constructor
  public WatersVerificationDialog(final IDE owner,
                            final ProductDESProxy des)
  {
    super(owner, des);
  }


  //#########################################################################
  //# Simple Access
  protected ModelVerifier getModelVerifier()
  {
    return (ModelVerifier) getModelAnalyzer();
  }


  //#########################################################################
  //# Abstract Methods
  protected abstract String getFailureDescription();

  protected abstract String getSuccessDescription();


  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog
  @Override
  protected String getFailureText()
  {
    final ModelVerifier verifier = getModelVerifier();
    final CounterExampleProxy counterExample = verifier.getCounterExample();
    String text = null;
    if (counterExample != null) {
      text = counterExample.getComment();
    }
    if (text == null || text.length() == 0) {
      final ProductDESProxy des = verifier.getModel();
      final String name = des.getName();
      text = "Model " + name + " " + getFailureDescription() + ".";
    }
    return text;
  }

  @Override
  protected String getSuccessText()
  {
    final ModelAnalyzer analyzer = getModelAnalyzer();
    final ProductDESProxy des = analyzer.getModel();
    final String name = des.getName();
    return "Model " + name + " " + getSuccessDescription() + ".";
  }

  @Override
  public void fail()
  {
    super.fail();
    final ModelVerifier modelVerifier = getModelVerifier();
    final CounterExampleProxy counterexample =
      modelVerifier.getCounterExample();
    if (counterexample != null) {
      final JButton traceButton = new JButton("Show Trace");
      traceButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          WatersVerificationDialog.this.dispose();
          final IDE ide = getIDE();
          final ModuleContainer container =
            (ModuleContainer) ide.getActiveDocumentContainer();
          container.switchToTraceMode(counterexample);
          final String comment = counterexample.getComment();
          if (comment != null && comment.length() > 0) {
            final Logger logger = LogManager.getLogger();
            logger.info(comment);
          }
        }
      });
      addButton(traceButton);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 9160337468090484051L;

}
