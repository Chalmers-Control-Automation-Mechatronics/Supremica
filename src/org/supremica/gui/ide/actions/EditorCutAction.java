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
import java.awt.Toolkit;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.IDE;
import javax.swing.ImageIcon;

public class EditorCutAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public EditorCutAction(List<IDEAction> actionList)
	{
		super(actionList);

		setEditorActiveRequired(true);

		putValue(Action.NAME, "Cut");
		putValue(Action.SHORT_DESCRIPTION, "Cut");
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
    putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
  {
    ControlledSurface surface = ide.getActiveEditorWindowInterface()
                                   .getControlledSurface();
    Collection<ProxySubject> selected = surface.getSelected();
    IndexedSetSubject<NodeSubject> nodes =
      new IndexedHashSetSubject<NodeSubject>();
    Collection<EdgeSubject> edges = new ArrayList<EdgeSubject>();
    for (ProxySubject s : selected) {
      if (s instanceof NodeSubject) {
        nodes.add((NodeSubject) s.clone());
      } else if (s instanceof EdgeSubject) {
        edges.add((EdgeSubject) s);
      }
    }
    if (!nodes.isEmpty()) {
      GraphContainer cont = new GraphContainer(nodes, edges);
      ObjectTransfer trans = new ObjectTransfer(cont);
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans,
                                                                   surface);
    }
    surface.doDeleteSelected();
  }
}
