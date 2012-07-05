
package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.algorithms.TransitionProjection.AutomataTransitionProjection;
import org.supremica.automata.algorithms.TransitionProjection.TPDialogOption;
import org.supremica.gui.TPDialog;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * Editor class of the Transition Projection method.
 * 
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
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
        final ExtendedAutomata exAutomata = new ExtendedAutomata(module);

        final int nbrOfComponents = module.getComponentList().size();
        if(nbrOfComponents == 0){
            logger.error("There is no model in the editor. Please create or open one and then run again");
            return;
        }        
        
        List<? extends Proxy> currentSelection = ide.getActiveDocumentContainer().getEditorPanel().getComponentsPanel().getCurrentSelection();
        
        HashSet<String> components = new HashSet<String>();
        for(Proxy item : currentSelection)
            if(item instanceof SimpleComponentSubject)
                components.add(((SimpleComponentSubject) item).getName());
        
        TPDialog dialog = new TPDialog(ide.getFrame(), true, components.isEmpty());
        dialog.setLocationRelativeTo(ide.getIDE());
        dialog.setVisible(true);
        
        if(dialog.isProjectSelected()){
            if(dialog.getProjectionOption() == TPDialogOption.ALLEFAS_SELECTED){
                for(ExtendedAutomaton efa : exAutomata){
                    components.add(efa.getName());
                }
            }
            
            boolean auto = (dialog.getEventOption() == TPDialogOption.AUTOMATIC_SELECTED)?true:false;
            AutomataTransitionProjection TP = new AutomataTransitionProjection(exAutomata, auto);
            List<ExtendedAutomaton> prjs = new ArrayList<ExtendedAutomaton>();
            
            if(dialog.getEventOption() == TPDialogOption.LOCALEVENT_SELECTED){
                HashSet<EventDeclProxy> localEvents = new HashSet<EventDeclProxy>();
                String locals = dialog.getLocalText();
                if(!locals.isEmpty()){
                    String[] events = locals.split(",");
                    for(String ev : events){
                        if(!ev.isEmpty()){
                            for(EventDeclProxy e:exAutomata.getUnionAlphabet()){
                                if(ev.equals(e.getName())){
                                    localEvents.add(e);
                                }
                            }
                        }
                    }
                    if(!localEvents.isEmpty())
                        TP.setLocalEvents(localEvents);
                }
                
            } else if(dialog.getEventOption() == TPDialogOption.SHAREDEVENT_SELECTED){
                HashSet<EventDeclProxy> sharedEvents = new HashSet<EventDeclProxy>();
                String shared = dialog.getShareText();
                if(!shared.isEmpty()){
                    String[] events = shared.split(",");
                    for(String ev : events){
                        if(!ev.isEmpty()){
                            for(EventDeclProxy e:exAutomata.getUnionAlphabet()){
                                if(ev.equals(e.getName())){
                                    sharedEvents.add(e);
                                }
                            }
                        }
                    }
                    if(!sharedEvents.isEmpty())
                        TP.setSharedEvents(sharedEvents);
                }
            }        
            
            int nbrOriNodes = 0;
            int nbrPrjNodes = 0;
            int nbrOriTrans = 0;
            int nbrPrjTrans = 0;
            long elapsed = 0;
            if(dialog.showResult())
                logger.info("Transition Projection start");
            
            for(String efa : components){
                ExtendedAutomaton oriEFA = exAutomata.getExtendedAutomaton(efa);
                nbrOriNodes += oriEFA.getNodes().size();
                nbrOriTrans += oriEFA.getTransitions().size();
                if(dialog.showResult())
                    logger.info("Projecting " + efa + " ...");
                ExtendedAutomaton prjEFA = TP.projectEFA(efa);
                if(dialog.showResult())
                    logger.info("Projection finish in " + TP.getTimer());
                String name = oriEFA.getName();
                if(dialog.getNamingOption() == TPDialogOption.SUFFIXNAME_SELECTED)
                    name += dialog.getSuffixName();
                else if (dialog.getNamingOption() == TPDialogOption.PREFIXNAME_SELECTED)
                    name = dialog.getPrefixName() + name;
                
                prjEFA.setName(name);
                prjs.add(prjEFA);
                elapsed += TP.getElapsedTime();
                nbrPrjNodes += prjEFA.getNodes().size();
                nbrPrjTrans += prjEFA.getTransitions().size();
            }
            
            if(dialog.removeOriginalEFAs()){
                for(String comp : components){
                    ExtendedAutomaton efa = exAutomata.getExtendedAutomaton(comp);
                    exAutomata.getModule().getComponentListModifiable().remove(efa.getComponent());
                }
            }
                
            for(ExtendedAutomaton efa : prjs)
                exAutomata.addAutomaton(efa);
            
            if(dialog.showResult()){
                HashSet<EventDeclProxy> events = new HashSet<EventDeclProxy>();
                for(ExtendedAutomaton efa : prjs)
                    events.addAll(efa.getAlphabet());
                
                @SuppressWarnings("unchecked")
                HashSet<EventDeclProxy> locEvents = (HashSet<EventDeclProxy>) ExtendedAutomaton.setDifference(exAutomata.getUnionAlphabet(), events);
                String l = "{";
                for (Iterator<EventDeclProxy> it = locEvents.iterator(); it.hasNext();) {
                    EventDeclProxy e = it.next();
                    if(it.hasNext())
                        l += e.getName() + ", ";
                    else
                        l += e.getName();
                }
                l += "}";

                logger.info("\n -----------------------"
                        + "\n Projection Result"
                        + "\n -----------------------"
                        + "\n Nbr original nodes: " + nbrOriNodes 
                        + "\n Nbr original transitions: " + nbrOriTrans
                        + "\n Nbr TP nodes: " + nbrPrjNodes 
                        + "\n Nbr TP transitions: " + nbrPrjTrans
                        + "\n Local events: " + l
                        + "\n Total computation time: " + elapsed/1000F + " seconds");                
            }
        }
    }
}
