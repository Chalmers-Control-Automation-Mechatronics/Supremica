/********************* FileChooser.java *******************/
/* Trying the FileChooser.lua script as Java code. There is
 * a bug in either LuaJava or in Java itself that prevents
 * the simple call fc:getSelectedFile():getName(), or variants
 * of it, from working; an exception is thrown instead
 ** It works fine from Java, though
 */
package Lupremica;

import java.io.File;
import javax.swing.JFileChooser;
import org.supremica.gui.ide.IDE;
import org.apache.logging.log4j.Logger;

public class FileChooser
{

    public FileChooser(final IDE ide)
    {
		final Logger log = ide.getTheLog();

		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Lupremica file chooser");
		final int retval = fc.showOpenDialog(null);
		if(retval == JFileChooser.APPROVE_OPTION)
		{
			log.info("File: " + fc.getSelectedFile().getName());
		}
		else
		{
			log.info("User cancelled");
		}
    }
}