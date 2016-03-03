
package org.supremica.gui.ide.actions;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.automata.algorithms.IISCT.EFAPartialEvaluator;
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
public class EditorEFAPEAction
 extends IDEAction
{

  public EditorEFAPEAction(final List<IDEAction> actionList)
  {
    super(actionList);

    setEditorActiveRequired(true);

    putValue(Action.NAME, "Partial Evaluation");
    putValue(Action.SHORT_DESCRIPTION, "Partial Evaluation");
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
      final ModuleSubject module = ide.getActiveDocumentContainer()
       .getEditorPanel().getModuleSubject();
      if (module.getComponentList().isEmpty()) {
        return;
      }
      System.err.println("Start compiling ...");
      final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
      compiler.setMarkingVariablEFAEnable(false);
      final SimpleEFASystem sys = compiler.compile();
      System.err.println("Finish compiling ...");

      final List<SimpleEFAComponent> compList = new ArrayList<>();
      final List<? extends Proxy> currentSelection =
       ide.getActiveDocumentContainer().getEditorPanel()
       .getComponentsPanel().getCurrentSelection();

      final THashSet<String> components = new THashSet<>();
      for (final Proxy item : currentSelection) {
        if (item instanceof SimpleComponentSubject) {
          components.add(((SimpleComponentSubject) item).getName());
        }
      }

      if (components.isEmpty()) {
        compList.addAll(sys.getComponents());
      } else if (components.size() > 0) {
        for (final SimpleEFAComponent comp : sys.getComponents()) {
          if (components.contains(comp.getName())) {
            compList.add(comp);
          }
        }
      }
      if (!compList.isEmpty()) {
        System.err.println("Start evaluating ...");
        final SimpleEFAVariableContext context = sys.getVariableContext();
        final EFAPartialEvaluator pe = new EFAPartialEvaluator(context, sys.getEventEncoding());
        pe.init(compList);
        final boolean success = pe.evaluate();
        System.err.println("Finish evaluating ...");
        if (success) {
          final Collection<SimpleEFAComponent> residuals = pe.getResidualComponents();
          final Collection<SimpleEFAVariable> vars = pe.getEvaluatedVariables();
          for (final SimpleEFAVariable var : vars) {
            System.err.println("Evaluated: " + var.getName());
          }
          for (final SimpleEFAComponent res : residuals) {
            sys.addComponent(res);
          }
        }
      }

      System.err.println("Start importing ...");
      mHelper = new SimpleEFAHelper();
      final ModuleSubject system = (ModuleSubject) mHelper.getModuleProxy(sys);
      final ModuleWindowInterface root = (ModuleWindowInterface) ide.getIDE().
       getActiveDocumentContainer().getActivePanel();
      mHelper.importToIDE(root, system, module);
      System.err.println("Finish importing ...");
    } catch (AnalysisException | EvalException | IOException |
     UnsupportedFlavorException ex) {
      java.util.logging.Logger.getLogger(EditorTransitionProjectionAction.class
       .getName()).
       log(Level.SEVERE, null, ex);
    }
  }

  //#########################################################################
  //# Class Constants
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.createLogger(IDE.class);
  private static final long serialVersionUID = 1008047793917621873L;
  private SimpleEFAHelper mHelper;

}