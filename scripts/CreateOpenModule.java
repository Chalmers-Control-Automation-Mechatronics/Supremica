/**************** CreateOpenModule.java ********************/
/* First try on java code to create or open a new module
 * in Supremica. Meant to be run as external script
**//***** And it works! ************************************/
package Lupremica;

import java.io.File;
import java.util.List;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;

public class CreateOpenModule
{
	private final IDE ide;

    public void createModule()
    {
        // To create a new module (from org\supremica\gui\ide\actions\NewAction.java)
        // final IDE ide = getIDE();
        final DocumentContainerManager manager = ide.getDocumentContainerManager();
        final org.supremica.gui.ide.ModuleContainer container = manager.newModuleContainer();
    }
    public void openModule(java.io.File file)
    {
        // To open an existing *.wmod (from net.sourceforge.waters.gui.actions.IDEOpenAction.java)
        // final IDE ide = getIDE();
        final DocumentContainerManager cmanager =
                  ide.getDocumentContainerManager();
        cmanager.openContainer(file);
    }

	public CreateOpenModule(final IDE ide)
    {
		System.out.println("CreateOpenModule(final IDE ide), called");
		this.ide = ide;
		// this.createModule();
		this.openModule(new File("Z:/Supremica/examples/waters/tests/synthesis/transferline_1.wmod"));
	}
}