package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import org.supremica.log.*;
import org.supremica.automata.Project;
import org.supremica.gui.TestCasesDialog;
import org.supremica.gui.VisualProjectFactory;
import org.supremica.automata.templates.TemplateItem;


/**
 * A new action
 */
public class ExamplesStaticAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.createLogger(Actions.class);

    /**
     * Constructor.
     */
    public ExamplesStaticAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(false);

        putValue(Action.NAME, "Static examples");
        putValue(Action.SHORT_DESCRIPTION, "Static examples");
        //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
    }

    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    public void doAction()
    {

    }

    /**
     * The code that is run when the action is invoked.
     */
    public void doAction(TemplateItem item)
    {

        Project project;

        try
        {
            project = item.createInstance(new VisualProjectFactory());
            if (!project.isDeterministic())
            {
                logger.warn("Nondeterministic automaton loaded. Some algorithms are not guaranteed to work.");
            }

            try
            {
				// Compile into Waters module
				ProductDESImporter importer = new ProductDESImporter(ModuleSubjectFactory.getInstance());
				ModuleSubject module = (ModuleSubject) importer.importModule(project);

				// Add as a new module (see OpenFileAction)
				ide.getIDE().installContainer(module);
                //int nbrOfAddedAutomata = gui.addProject(project);

                //gui.info("Successfully added " + nbrOfAddedAutomata + " automata.");
            }
            catch (Exception excp)
            {
                logger.error("Error adding automata ", excp);
                logger.debug(excp.getStackTrace());

                return;
            }

            // logger.debug("ActionMan.fileNewFromTemplate");
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(ide.getIDE(), "Error while creating the template!", "Alert", JOptionPane.ERROR_MESSAGE);
            logger.debug(ex.getStackTrace());
        }
    }
}



