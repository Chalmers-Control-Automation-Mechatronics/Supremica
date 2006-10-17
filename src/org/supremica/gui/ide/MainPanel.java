//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   MainPanel
//###########################################################################
//# $Id: MainPanel.java,v 1.22 2006-10-17 23:31:07 flordal Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import net.sourceforge.waters.gui.EditorColor;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.log.*;

import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.ide.actions.Actions;


abstract class MainPanel
    extends JPanel
    implements ModuleWindowInterface
{
    private static Logger logger = LoggerFactory.createLogger(MainPanel.class);
    
    private IDEToolBar thisToolBar = null;
    private IDEToolBar currParentToolBar = null;
    
    private GridBagConstraints constraints = new GridBagConstraints();
    
    private EmptyRightPanel emptyRightPanel = new EmptyRightPanel();
    
    private ModuleContainer moduleContainer;
    private String name;
    
    protected JSplitPane splitPanelHorizontal;
    
    public MainPanel(ModuleContainer moduleContainer, String name)
    {
        this.moduleContainer = moduleContainer;
        this.name = name;
        
        setPreferredSize(IDEDimensions.mainPanelPreferredSize);
        setMinimumSize(IDEDimensions.mainPanelMinimumSize);
        
        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);
        
        constraints.gridy = 0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
    }
    
    
    //######################################################################
    //# Interface net.sourceforge.waters.gui.ModuleWindowInterface
    public UndoInterface getUndoInterface()
    {
        return moduleContainer;
    }
    
    public ModuleSubject getModuleSubject()
    {
        return moduleContainer.getModule();
    }
    
    public ExpressionParser getExpressionParser()
    {
        return moduleContainer.getExpressionParser();
    }
    
    public EventKind guessEventKind(final IdentifierProxy ident)
    {
        return moduleContainer.guessEventKind(ident);
    }
    
    public Frame getRootWindow()
    {
        return (Frame) getTopLevelAncestor();
    }
    
    public EditorWindowInterface showEditor(SimpleComponentSubject component)
    {
        final EditorPanel editorPanel =
            moduleContainer.getEditorPanel();
        if (component != null)
        {
            editorPanel.setRightComponent
                (moduleContainer.getComponentEditorPanel(component));
        }
        return editorPanel.getActiveEditorWindowInterface();
    }
    
    public void showComment()
    {
        String name = getModuleSubject().getName();
        String comment = getModuleSubject().getComment();
        
        try
        {
            // Add stuff to a panel
            JPanel commentPanel = new JPanel();
            commentPanel.setLayout(new BorderLayout());
            // Create title
            //JTextPane titlePane = new JTextPane();
            JTextField titlePane = new JTextField(name);
            titlePane.setFont(new Font(null, Font.BOLD, 14));
            //StyledDocument titleDoc = titlePane.getStyledDocument();
            //titleDoc.insertString(titleDoc.getLength(), name, null);
            titlePane.setBorder(new EmptyBorder(0,0,0,0));
            titlePane.setBorder(new LineBorder(commentPanel.getBackground())); // WTF!? Doesn't work?
            //titlePane.setBorder(new LineBorder(EditorColor.INVISIBLE));
            titlePane.setBackground(commentPanel.getBackground()); // WTF!? Doesn't work?
            //titlePane.setBackground(EditorColor.INVISIBLE);
            commentPanel.add(BorderLayout.NORTH, titlePane);
            //Create the comment text
            JTextPane commentPane = new JTextPane();
            commentPane.setFont(new Font(null, Font.PLAIN, 12));
            StyledDocument commentDoc = commentPane.getStyledDocument();
            commentDoc.insertString(commentDoc.getLength(), comment, null);
            commentPane.setBackground(commentPanel.getBackground());
            commentPanel.add(BorderLayout.CENTER, commentPane);

            final EditorPanel editorPanel = moduleContainer.getEditorPanel();
            editorPanel.setRightComponent(commentPanel);
        }
        catch (BadLocationException ex)
        {
            //JOptionPane.showMessageDialog(ide.getFrame(), "Bad comment in module.", "Bad comment", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //######################################################################
    //#
    public String getName()
    {
        return name;
    }
    
    public Actions getActions()
    {
        return moduleContainer.getActions();
    }
    
    protected GridBagConstraints getGridBagConstraints()
    {
        return constraints;
    }
    
    public abstract void addToolBarEntries(IDEToolBar toolbar);
    
    public abstract void disablePanel();
    
    public abstract void enablePanel();
    
    public void setRightComponent(JComponent newComponent)
    {
        JComponent oldComponent = getRightComponent();
        if (oldComponent != newComponent)
        {
            JScrollPane emptyRightPanel = getEmptyRightPanel();
            int dividerLocation = splitPanelHorizontal.getDividerLocation();
            Dimension oldSize = emptyRightPanel.getSize();
            
            if (oldComponent != null)
            {
                splitPanelHorizontal.remove(oldComponent);
                oldSize = oldComponent.getSize();
            }
            
            if (newComponent == null || newComponent == getEmptyRightPanel())
            {
                // emptyRightPanel.setPreferredScrollableViewportSize(oldSize);
                emptyRightPanel.setPreferredSize(oldSize);
                splitPanelHorizontal.setRightComponent(emptyRightPanel);
                disablePanel();
            }
            else
            {
                //				newComponent.setPreferredScrollableViewportSize(oldSize);
                newComponent.setPreferredSize(oldSize);
                splitPanelHorizontal.setRightComponent(newComponent);
                enablePanel();
            }
            splitPanelHorizontal.setDividerLocation(dividerLocation);
        }
        validate();
    }
    
    public JComponent getRightComponent()
    {
        return (JComponent)splitPanelHorizontal.getRightComponent();
    }
    
    public JScrollPane getEmptyRightPanel()
    {
        return emptyRightPanel;
    }
    
    public JToolBar getToolBar(JToolBar parentToolBar)
    {
        if (parentToolBar instanceof IDEToolBar)
        {
            if (parentToolBar == currParentToolBar)
            {
                return thisToolBar;
            }
            thisToolBar = new IDEToolBar((IDEToolBar)parentToolBar);
            
            addToolBarEntries(thisToolBar);
            
            currParentToolBar = (IDEToolBar)parentToolBar;
            return thisToolBar;
        }
        return null;
    }
    
    public void actionPerformed(ActionEvent e)
    {
    }
    
    class EmptyRightPanel
        extends WhiteScrollPane
    {
        private static final long serialVersionUID = 1L;
        
        public EmptyRightPanel()
        {
            setPreferredSize(IDEDimensions.rightEmptyPreferredSize);
            setMinimumSize(IDEDimensions.rightEmptyMinimumSize);
        }
    }
}
