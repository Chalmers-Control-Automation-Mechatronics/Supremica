
package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * Editor class of the Transition Projection method.
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

public class EditorTransitionProjectionAction
                extends IDEAction
{
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.createLogger(IDE.class);

    public EditorTransitionProjectionAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(true);

        putValue(Action.NAME, "Transition Projection");
        putValue(Action.SHORT_DESCRIPTION, "Abstraction using transition projection");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/TranProj16.gif")));
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        doAction();
    }

    @Override
    public void doAction() {
        // Moved to Supremica/Java-1.7 branch due to Java 1.6 and 1.7 compatibility issue
    }
}
