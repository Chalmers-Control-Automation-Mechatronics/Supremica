//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.gui
//# CLASS:   ExecutionDialog
//###########################################################################
//# $Id$
//###########################################################################

/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.setProgress
 */
package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.supremica.automata.algorithms.Stoppable;


public final class ExecutionDialog
    extends JDialog
    implements ActionListener, Runnable
{
    private static final long serialVersionUID = 1L;
    private List<Stoppable> threadsToStop;
    private JPanel contentPanel = null;
    
    /** The header of the operation. */
    private JLabel operationHeader = null;
    /** The subheader of the operation */
    private JLabel operationSubheader = null;
    
    private JPanel infoPanel = null;
    private JPanel progressPanel = null;
    private JLabel infoValue = null;
    private JProgressBar progressBar = null;
    private JPanel currCenterPanel = null;
    private JButton stopButton = null;
    
    private int progressValue = -1;
    private int value = -1;
    
    private ExecutionDialogMode currentMode = null;
    private ExecutionDialogMode newMode = null;
    @SuppressWarnings("unused")
	private int nbrOfFoundStates = -1;
    
    private void Init(String title)
    {
        setTitle(title);
        setSize(new Dimension(250, 120));
        setResizable(false);
        
        // Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        
        JPanel operationPanel = new JPanel(new GridLayout(2, 1));
        operationHeader = new JLabel();
        operationHeader.setHorizontalAlignment(JLabel.LEFT);
        operationPanel.add(operationHeader);
        operationSubheader = new JLabel();
        operationSubheader.setHorizontalAlignment(JLabel.CENTER);
        operationPanel.add(operationSubheader);
        
        // We have two panels that we switch between, infoPanel and progressPanel
        
        // The infoPanel
        infoPanel = new JPanel();
        infoValue = new JLabel();
        infoPanel.add(infoValue, BorderLayout.CENTER);
        
        // The progressPanel
        progressPanel = new JPanel();
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        // And there is a button
        JPanel buttonPanel = new JPanel();
        stopButton = new JButton("Abort");
        stopButton.addActionListener(this);
        buttonPanel.add(stopButton);
        
        // And all is shown in one panel, the contentPanel
        contentPanel = (JPanel) getContentPane();
        contentPanel.add(operationPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Hit it!
        setMode(ExecutionDialogMode.UNINITIALIZED);
        setVisible(true);
    }
    
    /**
     * Creates dialog box for canceling the Stoppable classes in the supplied List.
     * @see Stoppable
     */
    public ExecutionDialog(Frame frame, String title, List<Stoppable> threadsToStop)
    {
        super(frame);
        
        setVisible(false);
        
        this.threadsToStop = threadsToStop;
        
        Init(title);
    }
    
    // -- MF -- Special case when you've got only one thread to watch
    public ExecutionDialog(Frame frame, String title, Stoppable threadToStop)
    {
        this(frame, title, new ArrayList<Stoppable>());
        
        addThreadToStop(threadToStop);
    }
    
    public void addThreadToStop(Stoppable threadToStop)
    {
        threadsToStop.add(threadToStop);
    }
    
    /**
     * Sets the mode of the dialog.
     */
    public void setMode(final ExecutionDialogMode mode)
    {
        newMode = mode;
        updateMode();
    }
    
    /**
     * Changes the subheader to the supplied string.
     */
    public void setSubheader(String string)
    {
        operationSubheader.setText(string);
    }
    
    /**
     * This must be called before changing mode to a progressMode.
     */
    public void initProgressBar(int min, int max)
    {
        // progressMin = min;
        // progressMax = max;
        progressBar.setMinimum(min);
        progressBar.setMaximum(max);
        
        this.progressValue = 0;
        
        update();
    }
    
    /**
     * Sets value of progress bar. The value is shown as % of completion
     * (with respect to the initialized min and max ).
     */
    public void setProgress(int progressValue)
    {
        this.progressValue = progressValue;
        
        update();
    }
    
    public void setValue(int value)
    {
        this.value = value;
        
        update();
    }
    
    private void update()
    {
        java.awt.EventQueue.invokeLater(this);
    }
    
    private void updateMode()
    {
        // Should we replace the "value panel"
        if (currCenterPanel != null)
        {
            contentPanel.remove(currCenterPanel);
        }
        
        update();
    }
    
    public void run()
    {
        // Update labels
		if (newMode != currentMode) {
			currentMode = newMode;
            if (currentMode == ExecutionDialogMode.HIDE) {
                dispose();                
                return;
            }
			setVisible(true);
            // Should we replace the "value panel"
            if (currCenterPanel != null)
            {
                contentPanel.remove(currCenterPanel);
            }
            
            // Update the dialog with the current mode
            operationHeader.setText(currentMode.getId());
            operationSubheader.setText(currentMode.getText());
            
            if (currentMode.showValue())
            {
                contentPanel.add(infoPanel, BorderLayout.CENTER);
                
                currCenterPanel = infoPanel;
            }
            else if (currentMode.showProgress())
            {
                contentPanel.add(progressPanel, BorderLayout.CENTER);
                
                currCenterPanel = progressPanel;
            }
        }
        
        // Update labels
        boolean showValues = currentMode.showValue();
        boolean showProgress = currentMode.showProgress();
        
        if (showValues)
        {
            // Don't show negative values in the dialog
            if (value >= 0)
            {
                infoValue.setText(String.valueOf(value));
            }
            else
            {
                infoValue.setText("");
            }
        }
        else if (showProgress)
        {
            progressBar.setValue(progressValue);
            
            //progressBar.setString(String.valueOf(Math.round(progressBar.getPercentComplete()*1000)/10.0) + "%");
            progressBar.setString(String.valueOf(Math.round(progressBar.getPercentComplete() * 100)) + "%");
        }
    }
    
    public void stopAllThreads()
    {
        for (Iterator<Stoppable> exIt = threadsToStop.iterator(); exIt.hasNext(); )
        {
            Stoppable threadToStop = exIt.next();
            if (!threadToStop.isStopped())
                threadToStop.requestStop();
        }
    }
    
    public void actionPerformed(ActionEvent event)
    {
        Object source = event.getSource();
        
        if (source == stopButton)
        {
            if (threadsToStop != null)
            {
                stopAllThreads();
                
                threadsToStop = null;    // Helping the garbage collector...
            }
            
            setMode(ExecutionDialogMode.HIDE);
        }
        else
        {
            System.err.println("Error in ExecutionDialog, unknown event occurred.");
        }
    }
}
