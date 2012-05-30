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
        final ExtendedAutomata exAutomata = new ExtendedAutomata(module);
        for(ExtendedAutomaton efa:exAutomata){
            String s = "";
            AutomatonObserver observer = new AutomatonObserver(efa);
            s += ("\n EFA: " + efa.getName()
                    + "\n Observable event: " + observer.getObservableEvents()
                    + "\n Unobservable events: " + observer.getUnobservableEvents());
            HashSet<HashSet<NodeProxy>> congruence = observer.getCongruence();
            for(HashSet<NodeProxy> p : congruence){
                s += "\n Coset members: {";
                for(NodeProxy st : p)
                    s += " " + st.getName() + " ";
                s += "}";
            }
            s += "\n Observer calculate in [" 
                    + observer.getObserverTimer() 
                    + "] and in [" + observer.getNrIterations() 
                    + "] iterations. \n ----------------------------";
            logger.info(s);
        }
        logger.info("Transition projection end");
    }
}
