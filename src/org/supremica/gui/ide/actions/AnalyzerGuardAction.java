/*
 * AnalyzerGuardAction.java
 *
 * Created on May 7, 2008, 3:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import org.supremica.automata.algorithms.Guard.*;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.GuardDialog;
import java.util.List;
import org.supremica.automata.*;
import org.supremica.log.*;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.gui.ide.EditorPanel;

/**
 *
 * @author Sajed
 */
public class AnalyzerGuardAction 
    extends IDEAction
{
    
    /** Creates a new instance of AnalyzerGuardAction */

    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.createLogger(IDE.class);
    
    public AnalyzerGuardAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setAnalyzerActiveRequired(true);
        setMinimumNumberOfSelectedComponents(2);
        
        putValue(Action.NAME, "Generate Guard...");
        putValue(Action.SHORT_DESCRIPTION, "Generate a guard expression for a given event");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/expression.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        // Retrieve the selected automata and make a sanity check
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
//        if (selectedAutomata.nbrOfAutomata() > 1)
//        {
//            System.err.println("Just one automton should be selected!");
/*             JOptionPane.showMessageDialog(ActionMan.getGui().getComponent(),
                    "Just one automton should be selected",
                    "Alert",    
                    JOptionPane.ERROR_MESSAGE);*/
//            return;
//        }
        
        // Get the current options
        GuardOptions guardOptions = new GuardOptions();
        
        // Start a dialog to allow the user changing the options
        GuardDialog guardDialog = new GuardDialog(ide.getFrame(), guardOptions);

        guardDialog.show();
/*        if (!guardOptions.getDialogOK())
        {
            return;
        }
 */       
 //       GuardGenerator gg = new GuardGenerator(selectedAutomata.getAutomatonAt(0),guardOptions.getExpressionType());
        EditorPanel editorPanel = ide.getActiveDocumentContainer().getEditorPanel();
        LabeledEvent sigma = new LabeledEvent(guardOptions.getEvent());
        BDDGuardGenerator bddgg;
        for(EventDeclSubject sigmaS:  editorPanel.getModuleSubject().getEventDeclListModifiable())
        {
            if(sigmaS.getKind() == EventKind.CONTROLLABLE || sigmaS.getKind() == EventKind.UNCONTROLLABLE)
            {
                System.out.println("Generating guard for event "+ sigmaS.getName()+"...");
                bddgg = new BDDGuardGenerator(selectedAutomata, sigmaS.getName(), guardOptions.getExpressionType());
            }
        }
        bddgg = new BDDGuardGenerator(selectedAutomata, sigma.getName(), guardOptions.getExpressionType());
        
        //Add the guard to the automata
        ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
        ExpressionParser parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
        
        SimpleComponentSubject tempSubj =null;
        HashSet<SimpleComponentSubject> subjects = new HashSet();



        for(AbstractSubject as: editorPanel.getModuleSubject().getComponentListModifiable())
        {
            
            tempSubj = ((SimpleComponentSubject)(as)).clone();
            tempSubj.setIdentifier(new SimpleIdentifierSubject (tempSubj.getName()+"_SUP"));
            subjects.add(tempSubj);
            
        }       
        
        boolean changed = false;
        for(SimpleComponentSubject simSubj:subjects)
        {
            changed = false;
            for(EdgeSubject ep:simSubj.getGraph().getEdgesModifiable())
            {
                SimpleExpressionSubject ses = null;
                if(ep.getLabelBlock().getEventList().iterator().next().toString().equals(sigma.getName()) && simSubj.getKind().name().equals("SPEC") && !bddgg.guardIsTRUE())
                {
                    try
                    {
                        ses = (SimpleExpressionSubject)(parser.parse(bddgg.getGuard(),Operator.TYPE_BOOLEAN));
                        
                        SimpleExpressionSubject variable = ((BinaryExpressionSubject)ses).getLeft();
                        VariableComponentSubject vcs = new  VariableComponentSubject(
                        new SimpleIdentifierSubject(variable.toString())
                        , parser.parse("{q0,q1,q2}",Operator.TYPE_RANGE)
                        ,true
                        ,parser.parse(variable.toString()+"==0",Operator.TYPE_BOOLEAN));
        
                        editorPanel.getModuleSubject().getComponentListModifiable().add(vcs);                        
                        
                    }
                    catch(ParseException pe)
                    {
                        System.out.println("Parse error!");
                        break;
                    }
                    GuardActionBlockSubject gab = new GuardActionBlockSubject();
                    ep.setGuardActionBlock(gab);
                    ep.getGuardActionBlock().getGuardsModifiable().add(ses);
                    changed = true;
                }
             }
            
             if(changed)
                editorPanel.addComponent(simSubj);
        }
        
    }
    
}

