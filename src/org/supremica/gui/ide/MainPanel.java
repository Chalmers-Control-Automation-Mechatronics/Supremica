//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   MainPanel
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import net.sourceforge.waters.gui.EditorColor;

import org.supremica.gui.WhiteScrollPane;


public abstract class MainPanel
    extends JPanel
{

    //######################################################################
    //# Constructor
    public MainPanel(final String name)
    {
        mName = name;

        setPreferredSize(IDEDimensions.mainPanelPreferredSize);
        setMinimumSize(IDEDimensions.mainPanelMinimumSize);

        final GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.BOTH;

        mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        mSplitPane.setOneTouchExpandable(false);
        mSplitPane.setOneTouchExpandable(false);
        mSplitPane.setDividerLocation(0.2);
        mSplitPane.setResizeWeight(0.0);
        gridbag.setConstraints(mSplitPane, constraints);
        add(mSplitPane);
    }


    //######################################################################
    //#
    public String getName()
    {
        return mName;
    }

    protected void setLeftComponent(final JComponent newComponent)
    {
        mSplitPane.setLeftComponent(newComponent);
    }

    protected boolean setRightComponent(final JComponent newComponent)
    {
        JComponent oldComponent = getRightComponent();
        if (oldComponent != newComponent)
        {
            JScrollPane emptyRightPanel = getEmptyRightPanel();
            int dividerLocation = mSplitPane.getDividerLocation();
            Dimension oldSize = emptyRightPanel.getSize();

            if (oldComponent != null)
            {
                mSplitPane.remove(oldComponent);
                oldSize = oldComponent.getSize();
            }

            if (newComponent == null || newComponent == getEmptyRightPanel())
            {
                emptyRightPanel.setPreferredSize(oldSize);
                mSplitPane.setRightComponent(emptyRightPanel);
            }
            else
            {
                newComponent.setPreferredSize(oldSize);
                mSplitPane.setRightComponent(newComponent);
            }
            mSplitPane.setDividerLocation(dividerLocation);
            validate();
            return true;
        } else {
            return false;
        }
    }

    public JComponent getRightComponent()
    {
        return (JComponent) mSplitPane.getRightComponent();
    }

    public JScrollPane getEmptyRightPanel()
    {
        return mEmptyRightPanel;
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
    private final JSplitPane mSplitPane;

    private final EmptyRightPanel mEmptyRightPanel = new EmptyRightPanel();
    private final String mName;


    //######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

}
