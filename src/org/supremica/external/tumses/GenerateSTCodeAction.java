package org.supremica.external.tumses;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sourceforge.waters.subject.module.ModuleSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.actions.IDEActionInterface;

public class GenerateSTCodeAction
{
  private static final long serialVersionUID = 1L;
  private final static Logger logger = LogManager.getLogger(IDE.class);



  public static void GenerateSTCode(final IDEActionInterface ide, final ModuleSubject module, final EditorSynthesizerOptions options) {
    logger.info("Generating PLC Code using TUM external toolbox...");
    tryLabel:
    try {
      logger.info("\tChecking the current model file");
      final String modulename = module.getName(); // Module's name without extension

      // Retrieve the path to access the Wmod file to be passed to the converter
      String modulePath;
      URI moduleUri = module.getLocation(); // already returns null if the module (included in the JAR) is not a Wmod file
      if (moduleUri == null) {
        modulePath = null;
      } else {
        modulePath = moduleUri.getPath(); // returns null if in the JAR, returns the path otherwise
      }

      // modulePath is null if the module is included in the JAR -> Save the current module into a selected folder
      final DocumentContainerManager manager = ide.getActiveDocumentContainer().getIDE().getDocumentContainerManager();
      if (modulePath == null) { // modulePath is null if the module is included in the JAR
        // Save as ...
        logger.warn("The current model first needs to be saved as an external WMOD file. Please select the destination folder.");
        manager.saveActiveContainerAs();

        // Retrieve the new module's path (and name)
        moduleUri = module.getLocation();
        modulePath = moduleUri.getPath(); // retrieve the new module's path (and name)
      } else {
        manager.saveActiveContainer();
      }
      logger.debug("moduleUri   : " + moduleUri);
      logger.debug("modulePath  : " + modulePath);

      // Select the Output path to be passed to the converter
      String destFoldPath = modulePath;
      if (!options.getSavPLCCodeTUMBox()) { // adding the Output option
        logger.warn("Selection of the PLC code destination. Please select the output folder.");
        final String dialogTitle;
        if (options.getTypePLCCodeTUM()=="standalone") {
          dialogTitle = "Select the destination folder";
        } else if (options.getTypePLCCodeTUM()=="TwinCAT") {
          dialogTitle = "Select the TwinCAT project folder";
        } else {
          dialogTitle = "Title undefined"; // Should never be called
        }

        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File("/" + modulePath));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle(dialogTitle);
        final int returnVal = chooser.showOpenDialog(ide.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          destFoldPath = chooser.getSelectedFile().getAbsolutePath();
          destFoldPath = destFoldPath.replace("\\", "/");
        } else {
          logger.warn("\tSelection canceled! Saving in the module's folder:" + modulePath);
          destFoldPath = modulePath;
        }
      }
      logger.debug("destFoldPath: " + destFoldPath);

      // Check and clean up the paths
      if (modulePath.startsWith("//")) {
        logger.error("The external PLC code converter does not support network repositories!");
        break tryLabel;
      } else if (modulePath.startsWith("/")) {
        modulePath = modulePath.substring(1); // remove the first /
      }
      if (destFoldPath.startsWith("//")) {
        logger.error("The external PLC code converter does not support network repositories!");
        break tryLabel;
      } else if (destFoldPath.startsWith("/")) {
        destFoldPath = destFoldPath.substring(1); // remove the first /
      }

      // Building the command to pass to the external converter
      // TODO: Now that the number of command is growing it would make sense to create a dedicated function
      // If would be simpler to handle specific cases, such as -h which should directly return
      logger.debug("\tBuilding the command");
      //final ArrayList<String> command = new ArrayList<>(Arrays.asList("cmd", "/k", "start", "STCodeConverter.exe"));
      final ArrayList<String> command = new ArrayList<>(Arrays.asList("STCodeConverter.exe"));
      if (options.getTypePLCCodeTUM()=="standalone") { // Standalone (default) option, with filepath
        final List<String> addargs = Arrays.asList("-f", modulePath);
        command.addAll(addargs);
      } else if (options.getTypePLCCodeTUM()=="TwinCAT") { // TwinCAT option, with filepath
        final List<String> addargs = Arrays.asList("-tc", "-f", modulePath);
        command.addAll(addargs);
      } else { // Help option
        command.add("-h");
      }
      if (options.getPLCCodeTUMefaBox()) { // adding the EFA option
        final List<String> addargs = Arrays.asList("-e");
        command.addAll(addargs);
      }
      if (!options.getSavPLCCodeTUMBox()) { // adding the Output option
        final List<String> addargs = Arrays.asList("-o", destFoldPath);
        command.addAll(addargs);
      }
      logger.debug("Command built: " + command);

      // Building the external process and starting it
      logger.info("\tStarting the external process");
      final ProcessBuilder procbuilder = new ProcessBuilder(command);

      procbuilder.redirectErrorStream(true); // so we can ignore the error stream
      final Process proc = procbuilder.start();
      final InputStream out = proc.getInputStream();
      final OutputStream in = proc.getOutputStream();

      logger.warn("External process started! Please check the external console for output.");

      final byte[] buffer = new byte[4000];
      String returnStr = "";
      boolean returnSuccess = false;
      while (isAlive(proc)) {
        final int no = out.available();
        if (no > 0) {
          final int n = out.read(buffer, 0, Math.min(no, buffer.length));
          final String str = new String(buffer, 0, n);
          System.out.println(str);
          returnStr += str; // TODO: Should we retrieve all strings?
          if (str.contains("successfully")) {
            returnSuccess = true;
          }
        }

        final int ni = System.in.available();
        if (ni > 0) {
          final int n = System.in.read(buffer, 0, Math.min(ni, buffer.length));
          in.write(buffer, 0, n);
          in.flush();
        }

        try {
          Thread.sleep(10);
        }
        catch (final InterruptedException e) {
        }
      }

//      final int result = proc.exitValue(); // TODO: What is the difference between exitValue and waitFor?

      final int result = proc.waitFor();

      if (result == 0) {
        // TODO: Parse the outputstream to check if that was really successful, or if that was a successfully caught error!
        //       -> Need to agree on the message to be written by the STCodeConverter.exe
        //       -> See above, e.g. if (str.contains("successfully")) ...
        if (returnSuccess) {
          final String msg = "ST code generation process successfully completed!\r\n"
                           + "Check the console for more details.";
          logger.warn(msg);
          logger.info("Console output: \r\n" + returnStr);
          JOptionPane.showMessageDialog(ide.getFrame(), msg);
        } else {
          final String msg = "ST code generation process gracefully exited after an error!\r\n"
                           + "Check the console for more details.";
          logger.warn(msg);
          logger.warn("Console output: \r\n" + returnStr);
          JOptionPane.showMessageDialog(ide.getFrame(), msg);
        }
      } else {
        final String msg = "ST code generation process did not completed properly! Error code: " + Integer.toString(result);
        logger.error(msg);
        logger.error("Console output: \r\n" + returnStr);
        JOptionPane.showMessageDialog(ide.getFrame(), msg);
      }
    }
    catch (final Exception ex) {
      logger.error("Exception while generating PLC Code using TUM external toolbox");
      ex.printStackTrace();
    }
    finally{
      // TODO: finally of Generating PLC Code using TUM external toolbox
    }
  }

  public static boolean isAlive(final Process p) {
    try {
      p.exitValue();
      return false;
    }
    catch (final IllegalThreadStateException e) {
      return true;
    }
  }
}
