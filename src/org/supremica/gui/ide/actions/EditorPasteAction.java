package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.ModuleContainer;
import java.util.List;
import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import java.util.Collection;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.IndexedHashSetSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import java.util.ArrayList;
import net.sourceforge.waters.gui.transfer.GraphContainer;
import net.sourceforge.waters.gui.transfer.ObjectTransfer;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
//import net.sourceforge.waters.gui.command.CopyGraphCommand;
import net.sourceforge.waters.gui.command.Command;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.FlavorEvent;

public class EditorPasteAction
    extends IDEAction
    implements FlavorListener
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private static DataFlavor COPYGRAPH = 
      new DataFlavor(GraphContainer.class, GraphContainer.class.getName());
    private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit()
                                                      .getSystemClipboard();
    
    public EditorPasteAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue(Action.NAME, "Paste");
        putValue(Action.SHORT_DESCRIPTION, "Paste Graph");
        
        setEnabled(CLIPBOARD.isDataFlavorAvailable(COPYGRAPH));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
      /*
      if (CLIPBOARD.isDataFlavorAvailable(COPYGRAPH)) {
        try {
          ControlledSurface surface = ide.getActiveEditorWindowInterface()
                                         .getControlledSurface();
          GraphContainer cont = (GraphContainer) CLIPBOARD.getData(COPYGRAPH);
          System.out.println("create");
          Command command = new CopyGraphCommand(surface.getGraph(), cont,
                                                 surface.getCurrentPoint());
          System.out.println("created");
          ide.getActiveEditorWindowInterface().getUndoInterface()
                                              .executeCommand(command);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      */
    }
    
    public void flavorsChanged(FlavorEvent e)
    {
      setEnabled(CLIPBOARD.isDataFlavorAvailable(COPYGRAPH));
    }
}
