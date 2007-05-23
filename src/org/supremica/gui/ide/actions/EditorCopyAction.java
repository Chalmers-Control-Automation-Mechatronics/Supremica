//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   EditorCopyAction
//###########################################################################
//# $Id: EditorCopyAction.java,v 1.6 2007-05-23 15:47:29 flordal Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.transfer.GraphContainer;
import net.sourceforge.waters.gui.transfer.ObjectTransfer;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public class EditorCopyAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public EditorCopyAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        
        putValue(Action.NAME, "Copy");
        putValue(Action.SHORT_DESCRIPTION, "Copy");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Copy16.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        final ControlledSurface surface =
            ide.getActiveEditorWindowInterface().getControlledSurface();
        final Collection<ProxySubject> selected = surface.getSelected();
        final Collection<NodeSubject> nodes = new LinkedList<NodeSubject>();
        final Collection<EdgeSubject> edges = new LinkedList<EdgeSubject>();
        for (final ProxySubject item : selected)
        {
            if (item instanceof NodeSubject)
            {
                nodes.add((NodeSubject) item);
            }
            else if (item instanceof EdgeSubject)
            {
                edges.add((EdgeSubject) item);
            }
        }
        if (!nodes.isEmpty())
        {
            GraphContainer cont = new GraphContainer(nodes, edges);
            ObjectTransfer trans = new ObjectTransfer(cont);
            Toolkit.getDefaultToolkit().getSystemClipboard().
                setContents(trans, surface);
        }
    }
}
