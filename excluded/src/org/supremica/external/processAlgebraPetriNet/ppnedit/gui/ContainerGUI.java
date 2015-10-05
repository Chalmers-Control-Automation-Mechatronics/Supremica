package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;
import java.awt.event.*;

class ContainerGUI 
			extends JDesktopPane
						implements ActionListener {

	protected JMenu jmFile = new JMenu("File");
	protected JMenu jmEdit = new JMenu("Edit");
	protected JMenu jmRelationType = new JMenu("Relation Type");
	protected JMenu jmOptions = new JMenu("Options");
	protected JMenu jmWindows = new JMenu("Windows");
	
	protected JMenuItem jmiSave = new JMenuItem("Save...");
	protected JMenuItem jmiSaveROP = new JMenuItem("Save ROP");
	
	protected JMenuItem jmiCut = new JMenuItem("Cut");
	protected JMenuItem jmiCopy = new JMenuItem("Copy");
	protected JMenuItem jmiPaste = new JMenuItem("Paste");
	protected JMenuItem jmiDelete = new JMenuItem("Delete");
	protected JMenuItem jmAutoPositioning = new JMenuItem("Auto Positioning");
	
	protected JCheckBoxMenuItem jmiMultiMode =
		new JCheckBoxMenuItem("Multi Mode");

	protected ButtonGroup bgWindow = new ButtonGroup();
	protected ButtonGroup bgRelationType = new ButtonGroup();
	
	protected int numOfNewSheetToDay = 0;

	public ContainerGUI() {
		super();

		final JMenuItem jmiNew = new JMenuItem("New");
		final JMenuItem jmiOpen = new JMenuItem("Open...");
		final JMenuItem jmiClose = new JMenuItem("Close");
		final JMenuItem jmiExit = new JMenuItem("Exit");

		jmiSave.setEnabled(true);
		jmiSaveROP.setEnabled(false);
		
		jmiNew.addActionListener(this);
		jmiOpen.addActionListener(this);
		jmiClose.addActionListener(this);
		jmiSave.addActionListener(this);
		jmiSaveROP.addActionListener(this);
		jmiExit.addActionListener(this);

		jmFile.add(jmiNew);
		jmFile.add(jmiOpen);
		jmFile.add(jmiClose);

		jmFile.addSeparator();

		jmFile.add(jmiSave);
		jmFile.add(jmiSaveROP);

		jmFile.addSeparator();

		jmFile.add(jmiExit);

		final JMenuItem jmiNewResource = new JMenuItem("New Resource");
		final JMenuItem jmiNewOperation = new JMenuItem("New Operation");
		final JMenu jmNewRelationType = new JMenu("New Relation Type");
		final JMenuItem jmiNewSequence = new JMenuItem("New Sequence");
		final JMenuItem jmiNewAlternative = new JMenuItem("New Alternative");
		final JMenuItem jmiNewParallel = new JMenuItem("New Parallel");

		jmiNewSequence.addActionListener(this);
		jmiNewAlternative.addActionListener(this);
		jmiNewParallel.addActionListener(this);

		jmNewRelationType.add(jmiNewSequence);
		jmNewRelationType.add(jmiNewAlternative);
		jmNewRelationType.add(jmiNewParallel);

		final JMenuItem jmiInsertResource = new JMenuItem("Insert Resource...");

		jmRelationType.setEnabled(false);

		final JRadioButtonMenuItem jrbmiSequence = new JRadioButtonMenuItem(
				"Sequence");
		final JRadioButtonMenuItem jrbmiAlternative = new JRadioButtonMenuItem(
				"Alternative");
		final JRadioButtonMenuItem jrbmiParallel = new JRadioButtonMenuItem(
				"Parallel");

		jrbmiSequence.addActionListener(this);
		jrbmiAlternative.addActionListener(this);
		jrbmiParallel.addActionListener(this);

		jmRelationType.add(jrbmiSequence);
		jmRelationType.add(jrbmiAlternative);
		jmRelationType.add(jrbmiParallel);
		bgRelationType.add(jrbmiSequence);
		bgRelationType.add(jrbmiAlternative);
		bgRelationType.add(jrbmiParallel);

		jmiCut.setEnabled(false);
		jmiCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));

		jmiCopy.setEnabled(false);
		jmiCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));

		jmiPaste.setEnabled(false);
		jmiPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK));

		jmiDelete.setEnabled(false);
		jmiDelete.setAccelerator(KeyStroke.getKeyStroke("DELETE"));

		jmiNewResource.addActionListener(this);
		jmiNewOperation.addActionListener(this);
		jmiInsertResource.addActionListener(this);
		jmiCut.addActionListener(this);
		jmiCopy.addActionListener(this);
		jmiPaste.addActionListener(this);
		jmiDelete.addActionListener(this);

		jmEdit.add(jmiNewResource);
		jmEdit.add(jmiNewOperation);
		jmEdit.add(jmNewRelationType);

		jmEdit.addSeparator();

		jmEdit.add(jmiInsertResource);

		jmEdit.addSeparator();

		jmEdit.add(jmiCut);
		jmEdit.add(jmiCopy);
		jmEdit.add(jmiPaste);

		jmEdit.addSeparator();

		jmEdit.add(jmiDelete);

		jmEdit.addSeparator();

		jmEdit.add(jmRelationType);

		final JMenuItem jmiAutoPositioning = new JMenuItem("Auto Positioning");
		jmiAutoPositioning.addActionListener(this);
		final JMenu jmView = new JMenu("View");
		jmiMultiMode.addActionListener(this);
		jmiMultiMode.setState(true);
		jmView.add(jmiMultiMode);
		jmOptions.add(jmiAutoPositioning);
		jmOptions.add(jmView);
		jmOptions.addSeparator();

		final JMenuItem jmiOrganizeAll = new JMenuItem("Organize All");
		jmiOrganizeAll.addActionListener(this);
		jmWindows.add(jmiOrganizeAll);
		jmWindows.addSeparator();
	}

	public JMenu getFileMenu() {
		return jmFile;
	}

	public JMenu getEditMenu() {
		return jmEdit;
	}

	public JMenu getOptionsMenu() {
		return jmOptions;
	}

	public JMenu getWindowsMenu() {
		return jmWindows;
	}

	// ovveride in EventAction
	public void actionPerformed(final ActionEvent e) {}
}
