
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.gui.automataExplorer;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import javax.swing.*;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexFormHelper;
import org.supremica.properties.Config;

public class AutomataStateDisplayer
        extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private AutomataStateViewer stateViewer;
    @SuppressWarnings("unused")
	private Automata theAutomata;
    private JCheckBox isInitialBox = new JCheckBox("initial");
    private JCheckBox isAcceptingBox = new JCheckBox("accepting");
    private JCheckBox isForbiddenBox = new JCheckBox("forbidden");
    private JLabel stateCost = new JLabel();
    private JLabel stateId = new JLabel();
    private JLabel stateName = new JLabel();
    private AutomataSynchronizerHelper helper;
    
    public AutomataStateDisplayer(AutomataStateViewer stateViewer, AutomataSynchronizerHelper helper)
    {
        setLayout(new BorderLayout());
        
        this.stateViewer = stateViewer;
        this.theAutomata = helper.getAutomata();
        this.helper = helper;
        
        JLabel header = new JLabel("Current composite state");
        
        add(header, BorderLayout.NORTH);
        
        Box statusBox = new Box(BoxLayout.Y_AXIS);
        
        isInitialBox.setEnabled(false);
        isInitialBox.setBackground(Color.white);
        statusBox.add(isInitialBox);
        isAcceptingBox.setEnabled(false);
        isAcceptingBox.setBackground(Color.white);
        statusBox.add(isAcceptingBox);
        isForbiddenBox.setEnabled(false);
        isForbiddenBox.setBackground(Color.white);
        statusBox.add(isForbiddenBox);
        statusBox.add(stateCost);
        statusBox.add(stateId);
        statusBox.add(stateName);
        
        JScrollPane boxScroller = new JScrollPane(statusBox);
        
        add(boxScroller, BorderLayout.CENTER);
        
        JViewport vp = boxScroller.getViewport();
        
        vp.setBackground(Color.white);
    }
    
    public void setCurrState(int[] currState)
    {
        helper.addStatus(currState);
        
        if (!helper.getCoExecuter().isControllable())
            helper.setForbidden(currState, true);
        
        isInitialBox.setSelected(AutomataIndexFormHelper.isInitial(currState));
        isAcceptingBox.setSelected(AutomataIndexFormHelper.isAccepting(currState));
        isForbiddenBox.setSelected(AutomataIndexFormHelper.isForbidden(currState));
        
        StringBuffer stateNameBuffer = new StringBuffer();
        stateNameBuffer.append(helper.getIndexMap().getStateAt(0,currState[0]).getName());
        for (int i=1; i<helper.getAutomata().size(); i++)
        {
            stateNameBuffer.append(Config.GENERAL_STATE_SEPARATOR.get());
            stateNameBuffer.append(helper.getIndexMap().getStateAt(i,currState[i]).getName());
        }
        stateName.setText(stateNameBuffer.toString());
    }
}
