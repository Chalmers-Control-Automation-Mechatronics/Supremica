/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.algorithms.TP.AutomataTP;
import org.supremica.automata.algorithms.TP.AutomatonObserver;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 *
 * @author shoaei
 */
public class EditorTransitionProjectionAction 
                extends IDEAction
{
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.createLogger(IDE.class);

    public EditorTransitionProjectionAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(true);

        putValue(Action.NAME, "Transition Projection");
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SHORT_DESCRIPTION, "Abstraction using transition projection");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/TranProj16.gif")));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        doAction();
    }

    @Override
    public void doAction() {
        final ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();

        final int nbrOfComponents = module.getComponentList().size();
        if(nbrOfComponents == 0){
            logger.error("There is no EFA to abstract. Create one in Editor panel and run again.");
            return;
        }
        logger.info("Transition projection running ... ");
        String s = "";
        final ExtendedAutomata exAutomata = new ExtendedAutomata(module);
        AutomatonObserver observer = new AutomatonObserver(exAutomata);
        s += "\n All shared event: " + observer.getSharedEvents() 
                + "\n All local events: " + observer.getLocalEvents()
                + "\n +++++++++++++++++";

        for(ExtendedAutomaton efa:exAutomata){
            s += ("\n EFA: " + efa.getName()
                    + "\n Shared event: " + observer.getSharedEvents(efa)
                    + "\n LocalEvents events: " + observer.getLocalEvents(efa));
            HashSet<HashSet<NodeProxy>> congruence = observer.getCongruence(efa);
            for(HashSet<NodeProxy> p : congruence){
                s += "\n Coset members: {";
                for(NodeProxy st : p)
                    s += " " + st.getName() + " ";
                s += "}";
            }
            s += "\n Observer calculated in [" 
                    + observer.getObserverTimer() 
                    + "] within [" + observer.getNrIterations() 
                    + "] iterations. \n ----------------------------";
        }
        logger.info(s);
        logger.info("Transition projection end");
    }
}
