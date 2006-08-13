package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.VerificationDialog;
import org.supremica.gui.AutomataVerificationWorker;
import java.util.List;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.algorithms.minimization.*;

import org.supremica.log.*;
import javax.swing.JOptionPane;

public class AnalyzerVerifierAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.createLogger(IDE.class);

	public AnalyzerVerifierAction(List<IDEAction> actionList)
	{
		super(actionList);

		setAnalyzerActiveRequired(true);

		putValue(Action.NAME, "Verify");
		putValue(Action.SHORT_DESCRIPTION, "Verify");
//		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Copy16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		// Retrieve the selected automata and make a sanity check
		Automata selectedAutomata = ide.getSelectedAutomata();
		if (!selectedAutomata.sanityCheck(ide.getFrame(), 1, true, false, true, true))
		{
			return;
		}

		// Get the current options and allow the user to change them...
		VerificationOptions vOptions = new VerificationOptions();
		MinimizationOptions mOptions = MinimizationOptions.getDefaultVerificationOptions();
		VerificationDialog verificationDialog = new VerificationDialog(ide.getIDE(), vOptions, mOptions);
		verificationDialog.show();
		if (!vOptions.getDialogOK())
		{
			return;
		}
		if (vOptions.getVerificationType() == VerificationType.LANGUAGEINCLUSION)
		{
			vOptions.setInclusionAutomata(ide.getUnselectedAutomata());
		}
		SynchronizationOptions sOptions = SynchronizationOptions.getDefaultVerificationOptions();

		// Work!
		AutomataVerificationWorker worker = new AutomataVerificationWorker(ide.getIDE(), selectedAutomata,
																		   vOptions, sOptions, mOptions);
	}

}
