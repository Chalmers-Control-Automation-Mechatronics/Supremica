//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.IDE;

public class SynthesisTestAction extends WatersAction
{
  private static final long serialVersionUID = 1L;

  //#########################################################################
  //# Constructor
  public SynthesisTestAction(final IDE ide)
  {
    super(ide);
    final String actName = "Synthesis Test";
    final String description = "Synthesize a monolithic supervisor";
    putValue(Action.NAME, actName);
    putValue(Action.SHORT_DESCRIPTION, description);
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_ANALYZER_SYNTH);
  }

  //#########################################################################
  //# Overridden methods
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    try {
      final IDE ide = getIDE();
      final ModuleSubject module =
        ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();
      final DocumentManager manager = ide.getDocumentManager();
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final ModuleCompiler compiler =
        new ModuleCompiler(manager, desFactory, module);
      compiler.setNormalizationEnabled(true);
      final ProductDESProxy des = compiler.compile();

      final SupervisorSynthesizer synthesizer =
        new MonolithicSynthesizer(desFactory);
      synthesizer.setModel(des);
      synthesizer.run();
      final ProductDESResult result = synthesizer.getAnalysisResult();
      if (!result.isSatisfied()) {
        System.out.println("Synthesis result is empty.");
        return;
      }
      final ProductDESProxy supervisor = result.getComputedProductDES();

      final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
      final ProductDESImporter importer = new ProductDESImporter(factory);
      for (final AutomatonProxy aut : supervisor.getAutomata()) {
        final SimpleComponentSubject comp =
          (SimpleComponentSubject) importer.importComponent(aut);
        module.getComponentListModifiable().add(comp);
      }
    } catch (final EvalException exception) {
      System.err.println("Error compiling module: " + exception.getMessage());
    } catch (final AnalysisException exception) {
      System.err.println("Error during synthesis: " + exception.getMessage());
    } catch (final ParseException exception) {
      System.err.println("Error when converting supervisor to module: " +
                         exception.getMessage());
    }

  }

}
