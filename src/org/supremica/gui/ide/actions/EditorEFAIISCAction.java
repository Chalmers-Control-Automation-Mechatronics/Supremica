
package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.automata.algorithms.IISCT.IISCT;
import org.supremica.automata.algorithms.IISCT.SMTSolver.Z3Solver;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

import gnu.trove.set.hash.THashSet;

/**
 * Editor class of the Transition Projection method.
 * <p/>
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */
public class EditorEFAIISCAction
 extends IDEAction
{

  public EditorEFAIISCAction(final List<IDEAction> actionList)
  {
    super(actionList);

    setEditorActiveRequired(true);

    putValue(Action.NAME, "IISC");
    putValue(Action.SHORT_DESCRIPTION, "Incremental, Inductive Supervisory Control");
    putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/TranProj16.gif")));
  }

  @Override
  public void actionPerformed(final ActionEvent e)
  {
    doAction();
  }

  @Override
  public void doAction()
  {
    try {
      final ModuleSubject module
       = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();
      if (module.getComponentList().isEmpty()) {
        final Z3Solver z = new Z3Solver();
        z.run();
        return;
      }
      final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
      final SimpleEFASystem sys = compiler.compile();

      final List<SimpleEFAComponent> compList = new ArrayList<>();
      final List<? extends Proxy> currentSelection
       = ide.getActiveDocumentContainer().getEditorPanel().getComponentsPanel().getCurrentSelection();
      final THashSet<String> components = new THashSet<>();
      for (final Proxy item : currentSelection) {
        if (item instanceof SimpleComponentSubject) {
          components.add(((SimpleComponentSubject) item).getName());
        }
      }
      if (components.isEmpty()) {
        compList.addAll(sys.getComponents());
      } else if (components.size() > 1) {
        for (final SimpleEFAComponent comp : sys.getComponents()) {
          if (components.contains(comp.getName())) {
            compList.add(comp);
          }
        }
      }
      if (!compList.isEmpty()) {
        final IISCT iisct = new IISCT(sys);
        final long currTime = System.currentTimeMillis();
        iisct.run();
        final long elapsed = System.currentTimeMillis() - currTime;
        System.err.println("time: " + elapsed + "ms");
      }
    } catch (final Exception ex) {
       logger.error(ex);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final Logger logger = LoggerFactory.createLogger(IDE.class);
  private static final long serialVersionUID = -4108158304486885027L;
}
