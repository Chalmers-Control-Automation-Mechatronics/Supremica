//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
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

	private static final String TRUE_GUARD = "1";

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

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }


    @Override
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
        logger.debug("Add the guard to the automata");

        final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
        final ExpressionParser parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());

        HashSet<String> currGuards = new HashSet<String>();
        for(final AbstractSubject simSubj: module.getComponentListModifiable())
        {
                if(simSubj instanceof SimpleComponentSubject)
                {
					final StringBuilder guardB = new StringBuilder();
					final StringBuilder currGuardB = new StringBuilder();
					final StringBuilder finalGuardB = new StringBuilder();

                    for(final EdgeSubject ep:((SimpleComponentSubject)simSubj).getGraph().getEdgesModifiable())
                    {
//                        for(String currEvent:event2guard.keySet())
//                        {
                        SimpleExpressionSubject ses = null;
                        SimpleExpressionSubject ses1 = null;
                        final String currEvent = ep.getLabelBlock().getEventIdentifierList().iterator().next().toString();

                        if(event2guard.containsKey(currEvent))
                            currGuards = event2guard.get(currEvent);
                        else
                            currGuards = new HashSet<String>();

                       // String currGuard = "";
                        try
                        {
                            for(final String g:currGuards)
                            {
                                // guard += (g + "&");
								guardB.append(g).append('&'); //MF
                            }
                            // currGuard = "";
                            if(!ep.getGuardActionBlock().getGuardsModifiable().isEmpty())
                            {
                                ses1 = ep.getGuardActionBlock().getGuardsModifiable().iterator().next().clone();
                                // currGuard = ses1.toString();
								currGuardB.append(ses1.toString()); //MF
                            }

                            // if(currGuard.length() == 0 && guard.length() > 0)
							//	guard = guard.substring(0, guard.length()-1);
							if(currGuardB.length() == 0 && guardB.length() > 0)
								guardB.setLength(guardB.length()-1); //MF

                            // String finalGuard = guard + currGuard;
							finalGuardB.append(guardB).append(currGuardB); //MF

                            // if(finalGuard.length() == 0)
                            //     finalGuard = "1";
							if(finalGuardB.length() == 0) finalGuardB.append(TRUE_GUARD); //MF
                            // if(guard.length() == 0)
                            //    guard = "1";
							if(guardB.length() == 0) guardB.append(TRUE_GUARD); //MF
//                            System.out.println("final:"+finalGuard);

							// Replace any ".curr" by "_curr" -- and we assuem it occurs max once per guard
							final String CURR = ".curr";	// This is problematic, "." is interpreted as a namespace delimiter
							final String FIXX = "_curr";
							final int SIZEOF_FIXX = FIXX.length();

							int curr_index = finalGuardB.indexOf(CURR);
							if(curr_index != -1) finalGuardB.replace(curr_index, curr_index+SIZEOF_FIXX, FIXX);
							curr_index = guardB.indexOf(CURR);
							if(curr_index != -1) guardB.replace(curr_index, curr_index+SIZEOF_FIXX, FIXX);

                            ses = (SimpleExpressionSubject)(parser.parse(finalGuardB.toString(), Operator.TYPE_BOOLEAN));
                            //The following line cocerns the new guards that will be attached to the automata with a DIFFERENT COLOR!

                            // if(guard.endsWith("&")) // then remove that last "&"
                            //    guard = guard.substring(0, guard.length()-1);
							if(guardB.charAt(guardB.length()-1) == '&') guardB.setLength(guardB.length()-1); //MF

                            // ses2 = (SimpleExpressionSubject)(parser.parse(guardB.toString(), Operator.TYPE_BOOLEAN));
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

						// Clear the striong builders for next iteration
						guardB.setLength(0);
						currGuardB.setLength(0);
						finalGuardB.setLength(0);
                    }
                }
        }
	}


}





