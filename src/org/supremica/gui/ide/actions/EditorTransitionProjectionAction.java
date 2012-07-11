
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
    public void actionPerformed(final ActionEvent e) {
        doAction();
    }

    @Override
    public void doAction() {
        final ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();
        final ExtendedAutomata exAutomata = new ExtendedAutomata(module);
        final HashSet<EventDeclProxy> uniAlphabet = new HashSet<EventDeclProxy>();
        for(final ExtendedAutomaton efa:exAutomata)
            uniAlphabet.addAll(efa.getAlphabet());

        final int nbrOfComponents = module.getComponentList().size();
        if(nbrOfComponents == 0){
            logger.error("There is no component in the editor. Please create or open one and then run it again");
            return;
        }

        final List<? extends Proxy> currentSelection = ide.getActiveDocumentContainer().getEditorPanel().getComponentsPanel().getCurrentSelection();

        final HashSet<String> components = new HashSet<String>();
        for(final Proxy item : currentSelection)
            if(item instanceof SimpleComponentSubject)
                components.add(((SimpleComponentSubject) item).getName());

        final TPDialog dialog = new TPDialog(ide.getFrame(), true, components.isEmpty());
        dialog.setLocationRelativeTo(ide.getIDE());
        dialog.setVisible(true);

        if(dialog.isProjectSelected()){
            if(dialog.getProjectionOption() == TPDialogOption.ALLEFAS_SELECTED){
                for(final ExtendedAutomaton efa : exAutomata){
                    components.add(efa.getName());
                }
            }

            final boolean auto = (dialog.getEventOption() == TPDialogOption.AUTOMATIC_SELECTED)?true:false;
            final AutomataTransitionProjection TP = new AutomataTransitionProjection(exAutomata, auto);
            final List<ExtendedAutomaton> prjs = new ArrayList<ExtendedAutomaton>();

            if(dialog.getEventOption() == TPDialogOption.LOCALEVENT_SELECTED){
                final HashSet<EventDeclProxy> localEvents = new HashSet<EventDeclProxy>();
                final String locals = dialog.getLocalText();
                if(!locals.isEmpty()){
                    final String[] events = locals.split(",");
                    for(final String ev : events){
                        if(!ev.isEmpty()){
                            for(final EventDeclProxy e:exAutomata.getUnionAlphabet()){
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
                final HashSet<EventDeclProxy> sharedEvents = new HashSet<EventDeclProxy>();
                final String shared = dialog.getShareText();
                if(!shared.isEmpty()){
                    final String[] events = shared.split(",");
                    for(final String ev : events){
                        if(!ev.isEmpty()){
                            for(final EventDeclProxy e:exAutomata.getUnionAlphabet()){
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
            long elapsed = 0;
            if(dialog.showResult())
                logger.info("Transition Projection start");

            final HashSet<String> projectedEFAs = new HashSet<String>();
            for(final String efa : components){
                final ExtendedAutomaton oriEFA = exAutomata.getExtendedAutomaton(efa);
                if(oriEFA.isNondeterministic() != null){
                    logger.error("EFA '" + efa +"' is nondeterministic and therefore it is skipped");
                    continue;
                }

                nbrOriNodes += oriEFA.getNodes().size();

                final ExtendedAutomaton prjEFA = TP.projectEFA(efa);

                if(prjEFA.getAlphabet().size() == oriEFA.getAlphabet().size()
                        && prjEFA.getNodes().size() == oriEFA.getNodes().size()
                        && prjEFA.getTransitions().size() == oriEFA.getTransitions().size()){
                    logger.info("The projected EFA for '"+ efa +"' is the same as the original one so keeping the original EFA");
                    elapsed += TP.getElapsedTime();
                    nbrPrjNodes += prjEFA.getNodes().size();                    
                    continue;
                }
                
                if(prjEFA.isNondeterministic() != null){
                    logger.info("The projected EFA for '"+ efa +"' is nondeterministic so keeping the original EFA");
                    elapsed += TP.getElapsedTime();
                    nbrPrjNodes += prjEFA.getNodes().size();                    
                    continue;
                }

                projectedEFAs.add(efa);

                if(dialog.showResult())
                    logger.info("Projecting <" + efa + "> finished in " + TP.getTimer());

                String name = oriEFA.getName();
                if(dialog.getNamingOption() == TPDialogOption.SUFFIXNAME_SELECTED)
                    name += dialog.getSuffixName();
                else if (dialog.getNamingOption() == TPDialogOption.PREFIXNAME_SELECTED)
                    name = dialog.getPrefixName() + name;

                prjEFA.setName(name);
                prjs.add(prjEFA);
                elapsed += TP.getElapsedTime();
                nbrPrjNodes += prjEFA.getNodes().size();
            }

            if(dialog.removeOriginalEFAs()){
                for(final String comp : projectedEFAs){
                    final ExtendedAutomaton efa = exAutomata.getExtendedAutomaton(comp);
                    exAutomata.getModule().getComponentListModifiable().remove(efa.getComponent());
                }
            }

            for(final ExtendedAutomaton efa : prjs){
                exAutomata.addAutomaton(efa);
            }

            if(dialog.showResult()){
                final HashSet<EventDeclProxy> uniPrjAlphabet = new HashSet<EventDeclProxy>();
                for(final ExtendedAutomaton efa : prjs)
                    uniPrjAlphabet.addAll(efa.getAlphabet());
                
                final HashSet<EventDeclProxy> locEvents = new HashSet<EventDeclProxy>();
                
                if(!(uniPrjAlphabet.isEmpty() || uniPrjAlphabet.size() == uniAlphabet.size())){
                    @SuppressWarnings("unchecked")
                    final HashSet<EventDeclProxy> temp = (HashSet<EventDeclProxy>) ExtendedAutomaton.setMinus(uniAlphabet, uniPrjAlphabet);        
                    locEvents.addAll(temp);
                }
                
                String l = "{";
                for (final Iterator<EventDeclProxy> it = locEvents.iterator(); it.hasNext();) {
                    final EventDeclProxy e = it.next();
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
                        + "\n Nbr TP nodes: " + nbrPrjNodes
                        + "\n Local events: " + l
                        + "\n Total computation time: " + elapsed/1000F + " seconds");
            } else {
                if(prjs.size() == 1){
                    logger.info("One EFA was projected in "+ elapsed/1000F + " seconds");
                } else if(prjs.size() > 1){
                    logger.info(prjs.size() + " EFAs were projected in "+ elapsed/1000F + " seconds");
                }
            }
        }
    }
}
