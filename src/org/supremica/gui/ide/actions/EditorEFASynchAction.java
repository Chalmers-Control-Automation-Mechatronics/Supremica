
package org.supremica.gui.ide.actions;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

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
@SuppressWarnings("deprecation")
public class EditorEFASynchAction
 extends IDEAction
{

  public EditorEFASynchAction(final List<IDEAction> actionList)
  {
    super(actionList);

    setEditorActiveRequired(true);

    putValue(Action.NAME, "EFA Synchronization");
    putValue(Action.SHORT_DESCRIPTION, "EFA Synchronization");
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
      final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
      final SimpleEFASystem sys = compiler.compile();

      final List<SimpleEFAComponent> compList = new ArrayList<>();
      final List<? extends Proxy> currentSelection
       = ide.getActiveDocumentContainer().getEditorPanel()
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
      mHelper = new SimpleEFAHelper();
      final List<ComponentProxy> list = new ArrayList<>();
      if (!compList.isEmpty()) {
        final long currTime = System.currentTimeMillis();
        final EFASynchronizer synch = new EFASynchronizer(EFASynchronizer.MODE_IISCT);
        synch.init(compList);
        synch.synchronize();
        final SimpleEFAComponent result = synch.getSynchronizedEFA();
        final long elapsed = System.currentTimeMillis() - currTime;
        System.err.println("----------------------------------");
        System.err.println("Time: " + elapsed + "ms (" + elapsed / 1000 + "s)");
        System.err.println("No. Synch. Locs: " + result.getNumberOfStates());
        System.err.println("No. Synch. Events: " + result.getNumberOfEvents());
        System.err.println("----------------------------------");
        System.err.println("Finish synchronizing ...");
        list.add(mHelper.getSimpleComponentProxy(result));
      }

      System.err.println("Start importing ...");
      final ModuleWindowInterface root = (ModuleWindowInterface) ide.getIDE().
       getActiveDocumentContainer().getActivePanel();
      mHelper.importToIDE(root, module, list);
      System.err.println("Finish importing ...");
    } catch (AnalysisException | EvalException | IOException | UnsupportedFlavorException ex) {
      logger.error(ex);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final Logger logger = LoggerFactory.createLogger(IDE.class);
  private static final long serialVersionUID = -4108158304486885027L;
  private SimpleEFAHelper mHelper;

}
