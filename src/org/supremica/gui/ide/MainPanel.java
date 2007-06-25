//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   MainPanel
//###########################################################################
//# $Id: MainPanel.java,v 1.33 2007-06-25 20:18:48 robi Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.gui.ide.actions.IDEAction;

import org.supremica.log.*;

import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.ide.actions.Actions;

abstract class MainPanel
    extends JPanel
{

    //######################################################################
    //# Constructor
    public MainPanel(String name)
    {
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
    //#
    public String getName()
    {
        return name;
    }

    protected GridBagConstraints getGridBagConstraints()
    {
        return constraints;
    }

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
                emptyRightPanel.setPreferredSize(oldSize);
                splitPanelHorizontal.setRightComponent(emptyRightPanel);
            }
            else
            {
                newComponent.setPreferredSize(oldSize);
                splitPanelHorizontal.setRightComponent(newComponent);
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


    //######################################################################
    //# Data Members
    private GridBagConstraints constraints = new GridBagConstraints();
    private EmptyRightPanel emptyRightPanel = new EmptyRightPanel();
    private String name;
    protected JSplitPane splitPanelHorizontal;

}
