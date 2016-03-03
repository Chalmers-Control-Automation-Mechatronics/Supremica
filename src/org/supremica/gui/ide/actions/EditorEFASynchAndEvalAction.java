
package org.supremica.gui.ide.actions;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.automata.algorithms.IISCT.EFAPartialEvaluator;
import org.supremica.automata.algorithms.IISCT.EFASynchronizer;
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
public class EditorEFASynchAndEvalAction
 extends IDEAction
{

  public EditorEFASynchAndEvalAction(final List<IDEAction> actionList)
  {
    super(actionList);

    setEditorActiveRequired(true);

    putValue(Action.NAME, "EFA Synch & Eval");
    putValue(Action.SHORT_DESCRIPTION, "EFA Synch & Eval");
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
      mHelper = new SimpleEFAHelper();
      final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
      compiler.setMarkingVariablEFAEnable(false);
       final SimpleEFASystem sys = compiler.compile();

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
      } else if (components.size() > 1) {
        for (final SimpleEFAComponent comp : sys.getComponents()) {
          if (components.contains(comp.getName())) {
            compList.add(comp);
          }
        }
      }
      if (!compList.isEmpty()) {

        final long currTime = System.currentTimeMillis();
        final SimpleEFAVariableContext context = sys.getVariableContext();
        final EFAPartialEvaluator pe = new EFAPartialEvaluator(context, sys.getEventEncoding());
        final EFASynchronizer synch = new EFASynchronizer();
        SimpleEFAComponent residual = null;
        for (final Iterator<SimpleEFAComponent> it = compList.iterator(); it.hasNext();) {
          final SimpleEFAComponent com = it.next();
          synch.init(com);
          synch.addComponent(residual);
          synch.synchronize();
          final SimpleEFAComponent syn = synch.getSynchronizedEFA();
          if (!syn.equals(com)) {
            sys.disposeComponent(com);
            sys.disposeComponent(residual);
          } else {
            residual = com;
            continue;
          }
          pe.init(syn);
          if (pe.evaluate()) {
            residual = pe.getResidualComponents().iterator().next();
            sys.disposeComponent(syn);
          } else {
            residual = syn;
          }
          sys.addComponent(residual);
        }

        final long elapsed = System.currentTimeMillis() - currTime;
        System.err.println("----------------------------------");
        System.err.println("Time: " + elapsed + "ms (" + elapsed / 1000 + "s)");
        System.err.println("No. Synch. Locs: " + residual.getNumberOfStates());
        System.err.println("No. Synch. Events: " + residual.getNumberOfEvents());
        System.err.println("No. Synch. Prime: " + residual.getPrimeVariables().size());
        System.err.println("No. Synch. Unprime: " + residual.getUnprimeVariables().size());
        System.err.println("----------------------------------");
      }

      System.err.println("Start importing ...");
      final ModuleSubject system = (ModuleSubject) mHelper.getModuleProxy(sys);
      final ModuleWindowInterface root = (ModuleWindowInterface) ide.getIDE().
       getActiveDocumentContainer().getActivePanel();
      mHelper.importToIDE(root, system, module);
      System.err.println("Finish importing ...");
    } catch (EvalException | IOException | OverflowException |
     UnsupportedFlavorException ex) {
      logger.error(ex);
    } catch (final AnalysisException ex) {
      java.util.logging.Logger.getLogger(EditorEFASynchAndEvalAction.class
       .getName())
       .log(Level.SEVERE, null, ex);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final Logger logger = LoggerFactory.createLogger(IDE.class);
  private static final long serialVersionUID = -4108158304486885027L;

  private SimpleEFAHelper mHelper;

}
