package org.supremica.external.processAlgebraPetriNet.gui;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;

import org.supremica.external.processAlgebraPetriNet.algorithms.*;

public class ToolBar extends JPanel {

    protected String newline = "\n";

    PPNGui guireferens;
    Document doc;
	ConverterSTEPtoPA convertersteptopa;
	ConverterPAtoAutomata converterpatoautomata;

    public ToolBar() {

		JToolBar toolBar = new JToolBar();
        addButtons(toolBar);

        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addButtons(JToolBar toolBar) {
        JButton button = null;

		//OPEN button
		button = new JButton("OPEN STEP MODEL");
		button.setToolTipText("Open STEP model");
		button.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(ToolBar.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File file = fc.getSelectedFile();
				String filnamn = file.getName();
				String path = file.getPath();

				String[] filNamnsFalt = {filnamn};

				// Load filename and SAX driver class
				boolean expandEntities = true;
				String filename = filNamnsFalt[0];
				String saxDriverClass = null;
					if (filNamnsFalt.length > 1) {
						if (filNamnsFalt[1].equalsIgnoreCase("false")) {
							expandEntities = false;
						}
						if (filNamnsFalt.length > 2) {
							saxDriverClass = filNamnsFalt[2];
						}
					}

				// Create an SAXBuilder
				try {
				SAXBuilder builder = null;

					if (saxDriverClass == null) {
						builder = new SAXBuilder();
					} else {
						builder = new SAXBuilder(saxDriverClass);
					}

				// Create an JDOM object
				builder.setExpandEntities(expandEntities);
				try
				{
					doc = builder.build(path);
				}
				catch (IOException ex)
				{
					System.err.println(ex.getMessage());
					return;
				}
				//Create a DocTree and updates the gui
				DOMOutputter outputter1 = new DOMOutputter();
				outputter1.output(doc);
				XMLTree tree = new XMLTree(outputter1.output(doc));
				guireferens.UppdateraTree(tree);

				// Convert the opened doc to a processalgebra doc
				convertersteptopa = new ConverterSTEPtoPA();
				convertersteptopa.convertSTEPtoPA(doc);
				Document PAdoc = convertersteptopa.getDoc();
				printPPNDoc(PAdoc);
				saveDoc(PAdoc);

				} catch (JDOMException sxe) {}
			}
		}
		});
		toolBar.add(button);

		toolBar.addSeparator();

		//Convert without open button
		button = new JButton("CONVERT TO AUTOMATA");
		button.setToolTipText("Convert to Automata");
		button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

			Document d = convertersteptopa.getDoc();

			// Convert to an automatadoc
			converterpatoautomata = new ConverterPAtoAutomata();
			converterpatoautomata.convertPAtoAutomata(d);
			Document automataDoc = converterpatoautomata.getDoc();
			printAutomataDoc(automataDoc);
			saveDoc(automataDoc);

		}
		});
		toolBar.add(button);

		toolBar.addSeparator();

		//Open PPN button
		button = new JButton("OPEN PPN MODEL");
		button.setToolTipText("Open PPN model");
		button.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

			//Create a file chooser
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(ToolBar.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File file = fc.getSelectedFile();
				String filnamn = file.getName();
				String path = file.getPath();

				String[] filNamnsFalt = {filnamn};

				// Load filename and SAX driver class
				boolean expandEntities = true;
				String filename = filNamnsFalt[0];
				String saxDriverClass = null;
					if (filNamnsFalt.length > 1) {
						if (filNamnsFalt[1].equalsIgnoreCase("false")) {
							expandEntities = false;
						}
						if (filNamnsFalt.length > 2) {
							saxDriverClass = filNamnsFalt[2];
						}
					}

				// Create an SAXBuilder
				try {
				SAXBuilder builder = null;
					if (saxDriverClass == null) {
						builder = new SAXBuilder();
					} else {
						builder = new SAXBuilder(saxDriverClass);
					}

				// Create an JDOM object
				builder.setExpandEntities(expandEntities);
				try
				{
					doc = builder.build(path);
				}
				catch (IOException ex)
				{
					System.err.println(ex.getMessage());
					return;
				}
				// Create a DocTree and updates the gui
				DOMOutputter outputter1 = new DOMOutputter();
				outputter1.output(doc);
				XMLTree tree = new XMLTree(outputter1.output(doc));
				guireferens.UppdateraPPNTree(tree);

				// Convert to an automatadoc
				converterpatoautomata = new ConverterPAtoAutomata();
				converterpatoautomata.convertPAtoAutomata(doc);
				Document automataDoc = converterpatoautomata.getDoc();
				printAutomataDoc(automataDoc);
				saveDoc(automataDoc);

				} catch (JDOMException sxe) {}
			}
		}
		});
		toolBar.add(button);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void sattRef(PPNGui guiref) {
			guireferens = guiref;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void printPPNDoc(Document doc) {
		try {
			DOMOutputter outputter1 = new DOMOutputter();
			outputter1.output(doc);
			XMLTree tree = new XMLTree(outputter1.output(doc));
			guireferens.UppdateraPPNTree(tree);
		} catch (JDOMException sxe) {}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void printAutomataDoc(Document doc) {
		try {
			DOMOutputter outputter1 = new DOMOutputter();
			outputter1.output(doc);
			XMLTree tree = new XMLTree(outputter1.output(doc));
			guireferens.UppdateraAutomataTree(tree);
		} catch (JDOMException sxe) {}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void printPNDoc(Document doc) {
		try {
			DOMOutputter outputter1 = new DOMOutputter();
			outputter1.output(doc);
			XMLTree tree = new XMLTree(outputter1.output(doc));
			guireferens.UppdateraPNTree(tree);
		} catch (JDOMException sxe) {}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void saveDoc(Document doc) {

		Document mppndoc = doc;
		String filnamn;
		String path = "knut";

		//Create a file chooser
		final JFileChooser f = new JFileChooser();
		int returnVal = f.showSaveDialog(ToolBar.this);


		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File file = f.getSelectedFile();
			filnamn = file.getName();
			path = file.getPath();
		}

		try {
			XMLOutputter outp = new XMLOutputter();
			outp.setFormat(org.jdom.output.Format.getPrettyFormat());
			//outp.setIndent("	");
			//outp.setNewlines(true);

			FileOutputStream filestream = new FileOutputStream(path);

			outp.output(mppndoc, filestream);
		}
		catch (FileNotFoundException e) {
			System.out.println("ingen fil");
		}
		catch (IOException sxe) {

		}
	}
}