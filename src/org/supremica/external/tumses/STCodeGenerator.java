package org.supremica.external.tumses;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.supremica.gui.ide.actions.IDEActionInterface;

/**
 * @author Julien Provost
 */

public class STCodeGenerator
{
  private static final long serialVersionUID = 1L;
  private final static Logger logger = LogManager.getLogger();

  /**
   * Generates, from a WMOD file, IEC 61131-3 ST code using TUM external STCodeConverter executable
   * @param ide {@link IDEActionInterface}
   * @param module {@link ModuleSubject}
   * @param options {@link EditorSynthesizerOptions}
   */
  public static void GenerateSTCode(final IDEActionInterface ide, final ModuleSubject module, final EditorSynthesizerOptions options) {
    logger.info("Generating PLC Code using TUM external toolbox...");
    tryLabel:
    try {
      logger.debug("\tChecking if the current model file is saved locally as a WMOD file...");
      // Retrieve the path of the Wmod file to be passed to the converter
      // Always save the module locally (Save as / Save). Otherwise, last changes may not be saved in the Wmod file
      final DocumentContainerManager manager = ide.getActiveDocumentContainer().getIDE().getDocumentContainerManager();
      if (isLocalWmod(module)) {
        manager.saveActiveContainer();
      } else {
        int count = 0;
        final int maxcount = 5; // max 5 tries, to avoid an infinite loop
        while (!isLocalWmod(module)) {
          logger.warn("The current model first needs to be saved, locally, as a WMOD file. Please select the destination folder.");
          count += 1;
          if (count > maxcount) { // Abort after too many missed tries
            logger.error("Failed to save the current model, locally, as a WMOD file. ST code generation aborted.");
            break tryLabel;
          }
          manager.saveActiveContainerAs();
        }
      }
      // Retrieve the new module's file name and folder
      String modFilePath = module.getFileLocation().getPath();
      final String modFileFolder = module.getFileLocation().getParentFile().getPath();

      // If different, select the output path to be passed to the converter
      String outFolder = null;
      if (!options.getSavPLCCodeTUMBox()) {
        logger.info("Selection of the PLC code destination. Please select the output folder.");
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
        chooser.setCurrentDirectory(new File("/" + modFileFolder));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle(dialogTitle);
        final int returnVal = chooser.showOpenDialog(ide.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          outFolder = chooser.getSelectedFile().getAbsolutePath();
          outFolder = outFolder.replace("\\", "/");
        } else {
          logger.warn("\tSelection canceled! Saving in the current module's folder:" + modFileFolder);
        }
      }
      if (outFolder == null) {
        outFolder = modFileFolder;
      }

      // Clean up the paths
      if (modFilePath.startsWith("/")) {
        modFilePath = modFilePath.substring(1); // remove the first /
      }
      if (outFolder.startsWith("/")) {
        outFolder = outFolder.substring(1); // remove the first /
      }

      // Building the command to pass to the external converter
      // examples:
      // STCodeConverter.exe -tc -f modFilePath -o outFolder pouFilename(without extension)
      final ArrayList<String> command = new ArrayList<>(Arrays.asList("STCodeConverter.exe"));
      if (options.getTypePLCCodeTUM()=="standalone") { // Standalone (default) option, with module's file path
        final List<String> addargs = Arrays.asList("-f", modFilePath);
        command.addAll(addargs);
      } else if (options.getTypePLCCodeTUM()=="TwinCAT") { // TwinCAT option, with module's file path
        final List<String> addargs = Arrays.asList("-tc", "-f", modFilePath);
        command.addAll(addargs);
      } else { // Help option
        command.add("-h");
      }
      if (options.getPLCCodeTUMefaBox()) { // adding the EFA option
        final List<String> addargs = Arrays.asList("-e");
        command.addAll(addargs);
      }
      // NOTA: by default for our Praktikum we write the supervisor code into the file "main_control.TcPOU"
      // TODO: Add this information to the documentation
      if (options.getTypePLCCodeTUM()=="TwinCAT") { // TwinCAT option -> Specify the output file's folder and name
        final String pouFilename = options.getPouNameField();
        final List<String> addargs = Arrays.asList("-o", outFolder, pouFilename);
        command.addAll(addargs);
      }
      logger.trace("\tCommand built: " + command);

      // Building the external process and starting it
      logger.info("\tStarting the external process");
      final ProcessBuilder procbuilder = new ProcessBuilder(command);

      procbuilder.redirectErrorStream(true); // so we can ignore the error stream
      final Process proc = procbuilder.start();
      final InputStream out = proc.getInputStream();
      final OutputStream in = proc.getOutputStream();
      logger.warn("External process started!");

      final byte[] buffer = new byte[4000];
      String returnStr = "";
      while (isAlive(proc)) {
        final int no = out.available();
        if (no > 0) {
          final int n = out.read(buffer, 0, Math.min(no, buffer.length));
          final String str = new String(buffer, 0, n);
          System.out.println(str);
          returnStr += str; // TODO: Should we retrieve all strings?
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

      final int result = proc.exitValue();
      logger.debug("\tproc.exitValue(): " + Integer.toString(result));

      if (result == 0) {
        final String msg = "ST code generation process completed!\r\n"
                         + "Check the console for more details.";
        logger.warn(msg);
        logger.info("STCodeGenerator's console output: \r\n" + returnStr);
        JOptionPane.showMessageDialog(ide.getFrame(), msg,
                                      "ST code generation completed",
                                      JOptionPane.WARNING_MESSAGE);
      } else {
        final String msg = "ST code generation process did not completed properly!";
        logger.debug(msg + "\r\n\tjava.lang.Process error code: " + Integer.toString(result));
        logger.error("STCodeGenerator's console output: \r\n" + returnStr);
        JOptionPane.showMessageDialog(ide.getFrame(), msg,
                                      "Error during the ST code generation",
                                      JOptionPane.ERROR_MESSAGE);
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

  /**
   * Checks if the module is saved locally as a WMOD file
   * @param  module {@link ModuleSubject}
   * @return true if the module is saved locally and has the right extension
   */
  public static boolean isLocalWmod(final ModuleSubject module) {
    if (module.getLocation() == null) { // already returns null if the module (included in the JAR) is not a Waters Module
      return false;
    } else if (module.getLocation().getPath() == null) { // returns null if in the JAR, returns the path otherwise
      return false;
    } else if (!module.getLocation().getPath().endsWith(".wmod")){ // returns null is the file extension is not .wmod
      return false;
    } else if (module.getLocation().getPath().startsWith("//")) { // Network repositories are not supported
      return false;
    } else {
      return true;
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
