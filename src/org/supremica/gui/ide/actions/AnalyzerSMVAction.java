/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
     * main loop
     * stop if process is terminated and there is nothing to read
     * if process is terminated and there is something to read - read it
     * if process is not terminated and there is somethign to read - read it
     * if process is not terminated and there is nothing to read - wait (retry)

     * @param r
     * @param p
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
