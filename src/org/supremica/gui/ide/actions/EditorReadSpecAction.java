//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   AnalyzerSynthesizerAction
//###########################################################################
//# $Id: AnalyzerSynthesizerAction.java 4750 2009-09-01 00:33:54Z robi $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.JFileChooser;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

public class EditorReadSpecAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.createLogger(IDE.class);
    HashMap<String,HashSet<String>> event2guard;


    public EditorReadSpecAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(true);

        putValue(Action.NAME, "Read specification from file...");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SHORT_DESCRIPTION, "Read specification from file in form of guards");
//        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/synthesize16.gif")));
    }

    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }


    public void doAction()
    {
        final ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();
        event2guard = new HashMap<String, HashSet<String>>();

        if(module.getComponentList().size() > 0)
        {
            final JFileChooser chooser = new JFileChooser();
            final int returnVal = chooser.showOpenDialog(ide.getFrame());

            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                final File file = chooser.getSelectedFile();
                FileReader fis = null;
                BufferedReader bis = null;
                try
                {
                   fis = new FileReader(file);
                   bis = new BufferedReader(fis);
                   String text = null;
                   while((text = bis.readLine()) != null)
                   {                        
                        final StringTokenizer st = new StringTokenizer(text, "\t");
                        final String event = st.nextToken();
                        final String guard = st.nextToken();
                        if(event2guard.containsKey(event))
                        {
                            event2guard.get(event).add(guard);
                        }
                        else
                        {
                            final HashSet<String> temp = new HashSet<String>();
                            temp.add(guard);
                            event2guard.put(event,temp);
                        }
                   }

                    fis.close();
                    bis.close();

                }
                catch (final FileNotFoundException e){}
                catch (final IOException e) { e.printStackTrace();}

                addGuardsToAutomata(module);
            }
        }
        else
            logger.error("No model exists! Please open a model.");

    }

    public void addGuardsToAutomata(final ModuleSubject module)
    {
        //Add the guard to the automata
        final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
        final ExpressionParser parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());

        HashSet<String> currGuards = new HashSet<String>();
            for(final AbstractSubject simSubj: module.getComponentListModifiable())
            {
                if(simSubj instanceof SimpleComponentSubject)
                {
                    for(final EdgeSubject ep:((SimpleComponentSubject)simSubj).getGraph().getEdgesModifiable())
                    {
//                        for(String currEvent:event2guard.keySet())
//                        {
                        SimpleExpressionSubject ses = null;
                        SimpleExpressionSubject ses1 = null;
                        @SuppressWarnings("unused")
                        SimpleExpressionSubject ses2 = null;
                        //&& simSubj.getKind().name().equals("SPEC")
                        String guard = "";


                        final String currEvent = ep.getLabelBlock().getEventList().iterator().next().toString();

                        if(event2guard.containsKey(currEvent))
                            currGuards = event2guard.get(currEvent);
                        else
                            currGuards = new HashSet<String>();

                        String currGuard="";
                        try
                        {
                            for(final String g:currGuards)
                            {
                                guard += (g+"&");
                            }
                            currGuard="";
                            if(!ep.getGuardActionBlock().getGuardsModifiable().isEmpty())
                            {
                                ses1 = ep.getGuardActionBlock().getGuardsModifiable().iterator().next().clone();
                                currGuard = ses1.toString();
                            }
                            if(currGuard.length() == 0 && guard.length() >0)
                                guard = guard.substring(0, guard.length()-1);
                            String finalGuard = guard+currGuard;
                            if(finalGuard.length() == 0)
                                finalGuard = "1";
                            if(guard.length() == 0)
                                guard = "1";
//                            System.out.println("final:"+finalGuard);

                            ses = (SimpleExpressionSubject)(parser.parse(finalGuard,Operator.TYPE_BOOLEAN));
                            //The following line cocerns the new guards that will be attached to the automata with a DIFFERENT COLOR!

                            if(guard.endsWith("&"))
                                guard = guard.substring(0, guard.length()-1);
                            ses2 = (SimpleExpressionSubject)(parser.parse(guard,Operator.TYPE_BOOLEAN));
                        }
                        catch(final ParseException pe)
                        {
                            System.err.println(pe);
                            logger.error("Some of the guards could not be parsed and attached to the automata: It is likely that there exists some 'strange' characters in some variables or values!");
                            break;
                        }
                        if(!ep.getGuardActionBlock().getGuardsModifiable().isEmpty())
                        {
                            ep.getGuardActionBlock().getGuardsModifiable().remove(0);
                            ep.getGuardActionBlock().getGuardsModifiable().add(ses);
                            //For color purposes
//                            if(!ses1.toString().equals("1"))
//                                ep.getGuardActionBlock().getGuardsModifiable().add(ses1);
//                            if(!ses2.toString().equals("1"))
//                                ep.getGuardActionBlock().getGuardsModifiable().add(ses2);
                        }
                        else
                        {
                            if(!ses.toString().equals("1"))
                                ep.getGuardActionBlock().getGuardsModifiable().add(ses);
                            //For color purposes
//                            if(!ses2.toString().equals("1"))
//                                ep.getGuardActionBlock().getGuardsModifiable().add(ses2);
                        }
                    }
                }
            }
        }
//    }

}
