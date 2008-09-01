/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.supremica.automata.Automata;
import org.supremica.automata.SAT.SATAutomata;

/**
 *
 * @author voronov
 */
public class AnalyzerSatAction extends IDEAction {
    private static final long serialVersionUID = 6904520117256488890L;

    public AnalyzerSatAction(List<IDEAction> actionList){
        super(actionList);
        
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Verify SAT");
        putValue(Action.SHORT_DESCRIPTION, "Run controllability verification by SAT (with induction)");
    }
    
    @Override
    public void doAction() {
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(ide.getFrame(), 1, true, false, true, true))
        {
            return;
        }
        
        try {
            boolean isControllable;
            isControllable = (new SATAutomata(selectedAutomata)).isControllableByInduction();
            JOptionPane.showMessageDialog(null, "The system is "+(isControllable?"":"un")+"controllable.");
        } catch (org.sat4j.specs.TimeoutException e){
            JOptionPane.showMessageDialog(null, "Timed out.");
        }
        
    }

    public void actionPerformed(ActionEvent e) {
        doAction();
    }

}
