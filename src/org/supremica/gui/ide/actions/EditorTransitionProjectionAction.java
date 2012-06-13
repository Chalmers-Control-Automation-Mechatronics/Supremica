/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.ImageIcon;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.algorithms.TP.*;
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
        final ExtendedAutomata exAutomata = new ExtendedAutomata(module);
//        TPDialog dialog = new TPDialog();
//        dialog.show();
        TPDialog dialog = new TPDialog(ide.getFrame());
        dialog.show();
        if(dialog.isTPSelected()){
            final int nbrOfComponents = module.getComponentList().size();
            if(nbrOfComponents == 0){
                logger.error("There is no EFA to abstract. Create one in editor panel and run again");
                return;
            }
            logger.info("Transition Projections is running ...");
            AutomataTP TP = new AutomataTP(exAutomata);
            if(dialog.isLocalSelected()){
                String localText = dialog.getLocalText().trim();
                HashSet<EventDeclProxy> locEvents = new HashSet<EventDeclProxy>();
                HashSet<String> events = new HashSet<String>();
                if(localText == null || localText.isEmpty()){
                    TP.compute();
                } else if(localText.contains(",")){
                    String[] split = localText.split(",");
                    for(String t:split)
                        if(!t.isEmpty())
                            events.add(t);

                    for(EventDeclProxy e:exAutomata.getUnionAlphabet())
                        if(events.contains(e.getName()))
                            locEvents.add(e);

                    TP.setLocalEvents(locEvents);
                    TP.compute();

                } else {
                    for(EventDeclProxy ee:exAutomata.getUnionAlphabet())
                        if(localText.equals(ee.getName()))
                            locEvents.add(ee);

                    if(locEvents.isEmpty()){
                        TP.compute();
                    } else {
                        TP.setLocalEvents(locEvents);
                        TP.compute();
                    }
                }                
            } else if (dialog.isShareSelected()){
                String textShare = dialog.getShareText().trim();
                HashSet<EventDeclProxy> shareEvents = new HashSet<EventDeclProxy>();
                HashSet<String> events = new HashSet<String>();
                if(textShare == null || textShare.isEmpty()){
                    TP.compute();
                } else if(textShare.contains(",")){
                    String[] split = textShare.split(",");
                    for(String t:split)
                        if(!t.isEmpty())
                            events.add(t);

                    for(EventDeclProxy e:exAutomata.getUnionAlphabet())
                        if(events.contains(e.getName()))
                            shareEvents.add(e);
                    
                    TP.setSharedEvents(shareEvents);
                    TP.compute();

                } else {
                    for(EventDeclProxy ee:exAutomata.getUnionAlphabet())
                        if(textShare.equals(ee.getName()))
                            shareEvents.add(ee);

                    if(shareEvents.isEmpty()){
                        TP.compute();
                    } else {
                        TP.setSharedEvents(shareEvents);
                        TP.compute();
                    }
                }                
            } else {
                TP.compute();
            }
            
            int nbrOriNodes = 0;
            int nbrObsNodes = 0;
            int nbrOriTrans = 0;
            int nbrObsTrans = 0;

            for(ExtendedAutomaton efa:exAutomata){
                nbrOriNodes += efa.getNodes().size();
                nbrOriTrans += efa.getTransitions().size();
            }

            for(ExtendedAutomaton efa:exAutomata)
                if(efa.getName().contains("_QUO")){
                    nbrObsNodes += efa.getNodes().size();
                    nbrObsTrans += efa.getTransitions().size();
                }
            String t = "{";
            for (Iterator<EventDeclProxy> it = TP.getAllLocalEvents().iterator(); it.hasNext();) {
                EventDeclProxy e = it.next();
                if(it.hasNext())
                    t += e.getName() + ", ";
                else
                    t += e.getName();
            }
            t += "}";

            logger.info("\n Transition Projection"
                    + "\n -----------------------"
                    + "\n Nbr original nodes: " + nbrOriNodes + "\n Nbr original transitions: " + nbrOriTrans
                    + "\n Nbr TP nodes: " + nbrObsNodes + "\n Nbr TP transitions: " + nbrObsTrans
                    + "\n Local events: " + t
                    + "\n Computation time: " + TP.getTimer());
            logger.info("Transition projection end");
        }
        
        if(dialog.isImportSelected()){
            File[] files = dialog.getSelectedFiles();
            if(files != null){
                for(File file : files){
                    try {
                        ADSConverter ads = new ADSConverter(exAutomata);
                        ExtendedAutomaton convert = ads.convert(file.toURI());
                        exAutomata.addAutomaton(convert);
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(EditorTransitionProjectionAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
