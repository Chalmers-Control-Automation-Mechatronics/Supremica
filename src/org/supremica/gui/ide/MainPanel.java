//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   MainPanel
//###########################################################################
//# $Id: MainPanel.java,v 1.20 2006-10-12 20:47:03 flordal Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

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
    
    public void showComment(String comment)
    {
        //Create the text pane and configure it.
        JTextPane textPane = new JTextPane();
        AbstractDocument doc;
        textPane.setCaretPosition(0);
        textPane.setMargin(new Insets(5,5,5,5));
        StyledDocument styledDoc = textPane.getStyledDocument();
        if (styledDoc instanceof AbstractDocument)
        {
            doc = (AbstractDocument)styledDoc;       
        }
        else
        {
            logger.error("Text pane's document isn't an AbstractDocument!");
            return;
        }
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(200, 200));
        // Add comment to document
        try
        {
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            //StyleConstants.setFontFamily(attributes, "SansSerif");
            StyleConstants.setFontSize(attributes, 12);
            //StyleConstants.setBold(attributes, true);
            doc.insertString(doc.getLength(), comment, attributes);
        }
        catch (BadLocationException ex)
        {
            logger.error(ex);
            return;
        }
        
        final EditorPanel editorPanel =
            moduleContainer.getEditorPanel();
        editorPanel.setRightComponent
            (textPane);
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
