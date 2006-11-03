//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   EditorRunEmbedderAction
//###########################################################################
//# $Id: EditorRunEmbedderAction.java,v 1.6 2006-11-03 15:01:57 torda Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.springembedder.SpringAbortDialog;
import net.sourceforge.waters.gui.springembedder.SpringEmbedder;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.properties.Config;

/**
 * A new action
 */
public class EditorRunEmbedderAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public EditorRunEmbedderAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(false);

        putValue(Action.NAME, "Layout graph");
        putValue(Action.SHORT_DESCRIPTION, "Makes an automatic layout of the graph");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
    }

    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    public void doAction()
    {
		final ControlledSurface surface =
			ide.getIDE().getActiveEditorWindowInterface().
			getControlledSurface();
        final GraphSubject graph = surface.getGraph();
        if (graph != null) {
			SpringEmbedder.stopAll();
			try {
				final Frame root = (Frame) surface.getTopLevelAncestor();
				final SimpleComponentSubject comp =
					(SimpleComponentSubject) graph.getParent();
				final String name = comp == null ? "graph" : comp.getName();
				final long timeout =
					Config.GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT.get();
				final SpringEmbedder embedder = new SpringEmbedder(graph);
				final Thread thread = new Thread(embedder);
				final JDialog dialog =
					new SpringAbortDialog(root, name, embedder, timeout);
				dialog.setLocationRelativeTo(surface);
				dialog.setVisible(true);
				thread.start();
			} catch (final GeometryAbsentException exception) {
				JOptionPane.showMessageDialog(ide.getFrame(),
											  "Graph is not layoutable!",
											  "Alert",
											  JOptionPane.ERROR_MESSAGE);
			}
        }
    }
}
