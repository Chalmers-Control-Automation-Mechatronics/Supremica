
package org.supremica.gui.ide.actions;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.analysis.efa.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.SimpleEFASystem;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * Editor class of the Transition Projection method.
 * <p/>
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */
public class EditorHDSAction
 extends IDEAction
{

  public EditorHDSAction(final List<IDEAction> actionList)
  {
    super(actionList);

    setEditorActiveRequired(true);

    putValue(Action.NAME, "HDS");
    putValue(Action.SHORT_DESCRIPTION, "Hierarchical and Decentralized Supervisory Control");
//    putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource(
//     "/icons/TranProj16.gif")));
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
      mFactory = ModuleSubjectFactory.getInstance();
      mCloner = mFactory.getCloner();
      final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
      final SimpleEFASystem sys = compiler.compile();
      System.err.println("Finish compiling ...");

      System.err.println("Start importing ...");
      final ModuleSubject system = (ModuleSubject) sys.getModuleProxy(mFactory);
      importToIDE(system, module);
      System.err.println("Finish importing ...");
    } catch (EvalException | IOException | UnsupportedFlavorException ex) {
//      java.util.logging.Logger.getLogger(EditorTransitionProjectionAction.class
//       .getName()).
//       log(Level.SEVERE, null, ex);
      logger.error(ex);
    }
  }

  private Collection<EventDeclSubject> getEventSubject(final List<EventDeclProxy> list)
  {
    final int size = list.size();
    final Collection<EventDeclSubject> result = new ArrayList<>(size);
    for (final Proxy item : list) {
      result.add((EventDeclSubject) mCloner.getClone(item));
    }
    return result;
  }

  private void importToIDE(final ModuleProxy nModule, final ModuleSubject oModule)
   throws IOException, UnsupportedFlavorException
  {
    final List<Proxy> componentList = mCloner.getClonedList(nModule
     .getComponentList());

    oModule.getEventDeclListModifiable().clear();
    final Collection<EventDeclSubject> events =
     getEventSubject(nModule.getEventDeclList());
    oModule.getEventDeclListModifiable().addAll(events);

    final ModuleWindowInterface root = (ModuleWindowInterface) ide.getIDE().
     getActiveDocumentContainer().getActivePanel();
    final SelectionOwner panel = root.getComponentsPanel();

    final List<InsertInfo> oList = new LinkedList<>();
    final List<InsertInfo> deletionVictims = panel.getDeletionVictims(panel.
     getAllSelectableItems());
    oList.addAll(deletionVictims);
    final Command deleteCommand = new DeleteCommand(oList, panel);
    root.getUndoInterface().executeCommand(deleteCommand);
    final InstanceSubject template =
     new InstanceSubject(new SimpleIdentifierSubject(""), "");
    final Transferable transfer =
     WatersDataFlavor.createTransferable(template);
    final List<InsertInfo> tInserts = panel.getInsertInfo(transfer);
    final InsertInfo tInsert = tInserts.get(0);
    final Object position = tInsert.getInsertPosition();
    final List<InsertInfo> nList = new ArrayList<>();
    for (int i = componentList.size() - 1; i >= 0; i--) {
      final InsertInfo insert = new InsertInfo(componentList.get(i),
                                               position);
      nList.add(insert);
    }
    final Command insertCommand = new InsertCommand(nList, panel, root);
    root.getUndoInterface().executeCommand(insertCommand);
    panel.clearSelection(false);
  }

  private ModuleSubjectFactory mFactory;
  private ModuleProxyCloner mCloner;

  //#########################################################################
  //# Class Constants
  private static final Logger logger = LoggerFactory.createLogger(IDE.class);
  private static final long serialVersionUID = 5024850468101547938L;

}
