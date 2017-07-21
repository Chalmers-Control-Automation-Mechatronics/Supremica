//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.module.ModuleProxy;

import org.supremica.automata.IO.EFAToNuSMV;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


/**
 *
 * @author voronov
 */
public class AnalyzerSMVAction extends IDEAction {
    private static final long serialVersionUID = -5526955419215441122L;
    private static Logger logger = LoggerFactory.createLogger(AnalyzerSMVAction.class);

    public AnalyzerSMVAction(final List<IDEAction> actionList){
        super(actionList);

        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Verify nonblocking using NuSMV");
        putValue(Action.SHORT_DESCRIPTION, "Verify nonblocking using NuSMV");
    }

    @Override
    public void doAction() {
        final DocumentProxy doc = ide.getActiveDocumentContainer().getDocument();
        if(doc instanceof ModuleProxy){
            final ModuleProxy m = (ModuleProxy) doc;

            try {
                final File f = File.createTempFile("supremica-NuSmv-" + m.getName(), ".smv");
                final PrintWriter pw = new PrintWriter(f);

                (new EFAToNuSMV()).print(m, pw, Arrays.asList(new EFAToNuSMV.SpecPrinterNonBlocking()));
                pw.flush();
                printFileToLog(f);
                runNuSMV(f);
            } catch (final IOException ex) {
                logger.error("IO error occurred while executing NuSMV module: " + ex.getMessage(), ex);
            }
        } else {
            logger.error("Document is not a Module (it is " + doc.getClass().toString() + ")");
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        doAction();
    }


    private void printFileToLog(final File f)
      throws FileNotFoundException, IOException
    {
      final BufferedReader br = new BufferedReader(new FileReader(f));
      try {
        int line = 1;
        logger.info("File for NuSMV: ");
        while(br.ready()){
            logger.info("" + line + " " + br.readLine());
            line++;
        }
        logger.info("End of file for NuSMV.");
      } finally {
        br.close();
      }
    }

    private void runNuSMV(final File inputFile) throws IOException{
        final String[] commands  = new String[]{
            "c:\\Progs\\NuSMV-2.4.3\\2.4.3\\bin\\NuSMV.exe ",
            inputFile.getAbsolutePath()};

        final java.lang.ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        final Process p = pb.start();
        final InputStream is = p.getInputStream();
        final BufferedReader br = new BufferedReader(new InputStreamReader(is));

        runMainOutputLoop(br, p);
    }

    /**
     * Main loop.
     * Stop if process is terminated and there is nothing to read.
     * If process is terminated and there is something to read - read it.
     * If process is not terminated and there is something to read - read it.
     * If process is not terminated and there is nothing to read - wait (retry).
     */
    private void runMainOutputLoop(final BufferedReader br, final Process p) throws IOException{
        boolean term = false;
        while(!term){
            boolean alive = true;
            try{
                p.exitValue();
                alive = false;
            } catch(final IllegalThreadStateException e) {
                // expected, do nothing
            }
            final boolean ready = br.ready();

            if(ready){
                 while(br.ready())
                    logger.info(br.readLine());
            } else {
                term = !alive; // terminate the loop if the process is not alive
            }
        }
    }

}
