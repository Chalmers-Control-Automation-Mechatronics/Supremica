package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class ExportToCIFAction extends WatersAction {

  public ExportToCIFAction(final IDE ide) {
    super(ide);
    putValue(Action.NAME, "Export to CIF");
    putValue(Action.SHORT_DESCRIPTION, "Convert the current Supremica model to a CIF script natively");
  }

  // Create a lightweight Logger object for the Lua script to use
  // This matches the 'log:info(str, level)' method the script expects
  public static class LuaLogger {
      public void info(final String message, final int level) {
          System.out.println("[CIF Export] " + message);
      }
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final ModuleContainer container = getActiveModuleContainer();

    if (container == null || container.getDocument() == null) {
        JOptionPane.showMessageDialog(null,
            "No active model found. Please open a model first.",
            "Conversion Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

        // 1. Initialize the internal Java Script Engine
        final ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("luaj");

        // Fallback in case the engine is registered under a different name
        if (engine == null) {
            engine = factory.getEngineByName("lua");
        }

        if (engine == null) {
            JOptionPane.showMessageDialog(null,
                "Internal Lua engine not found. Ensure LuaJ libraries are on the Build Path.",
                "Engine Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Inject the Java objects directly into the Lua environment
        engine.put("IDE_GLOBAL", getIDE());
        engine.put("LOG_GLOBAL", new LuaLogger());

        java.io.InputStream luaStream = null;

        // 3. Load the Lua script from Eclipse 'src/scripts/' folder
        // final java.io.InputStream luaStream = getClass().getResourceAsStream("/scripts/Supremica2CIF.lua");
        try {
          luaStream = new java.io.FileInputStream("scripts/Supremica2CIFInternship2.lua");
        } catch (final java.io.FileNotFoundException ex) {
          ex.printStackTrace();
        }
        if (luaStream == null) {
            JOptionPane.showMessageDialog(null,
                "Could not find the embedded conversion script inside /scripts/.",
                "Configuration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. Execute the script natively inside Supremica
        //final Reader reader = new InputStreamReader(luaStream);
        //engine.eval(reader);
        try {
          final Reader reader = new InputStreamReader(luaStream);
          engine.eval(reader);
          reader.close();
        } catch (final Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null,
            "Error during internal export: " + ex.getMessage(),
            "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
  }

  private static final long serialVersionUID = 1L;
}
