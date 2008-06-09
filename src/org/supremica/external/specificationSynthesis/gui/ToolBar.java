package org.supremica.external.specificationSynthesis.gui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.*;

import java.io.*;
import javax.xml.parsers.*;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.xml.parsers.*;

import org.xml.sax.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;

/*
import org.xml.sax.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;*/

import org.supremica.external.avocades.specificationsynthesis.*;


public class ToolBar extends JPanel {

	protected String newline = "\n";

	    Gui guireferens;
	    Document doc;
		ConverterILtoAutomata conv;

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
		button = new JButton("OPEN EOP MODEL");
		button.setToolTipText("Open EOP model");
		button.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			System.out.println("EOP");

			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(ToolBar.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
/*A
				File file = fc.getSelectedFile();
				String inputFileName = file.getName();
				String[] fileNameField = {inputFileName};

				// Load filename and SAX driver class

				String filename = fileNameField[0];

				boolean expandEntities = true;
				String saxDriverClass = null;

				if (fileNameField.length > 1) {
					if (fileNameField[1].equalsIgnoreCase("false")) {
						expandEntities = false;
					}
					if (fileNameField.length > 2) {
						saxDriverClass = fileNameField[2];
					}
				} //if(fileNameField.length > 1)


				// Create a SAXBuilder
				String path = file.getPath(); // Path to the choosen file
A*/
				//System.out.println("Open " + path);
				//try {
				/*ASAXBuilder builder = null; // In org.jdom.input
					if (saxDriverClass == null) {
						builder = new SAXBuilder();
						System.out.println("No saxDriverClass given");
					}
					else {
						builder = new SAXBuilder(saxDriverClass);
					}

				// Create an JDOM object
				// This sets whether or not to expand entities during the build.
				// A true means to expand entities as normal content. A false means
				// to leave entities unexpanded as EntityRef objects. The default is true.
				builder.setExpandEntities(expandEntities);A*/
				/*Aif(expandEntities)
					System.out.println("Expand entities.");
				else
					System.out.println("Don´t expand entities.");

				// Builds a JDOM document from an xml-file.
				try {
					doc = builder.build(file); // In org.jdom.input
				}
				catch (JDOMException ex)
				{
					System.err.println(ex.getMessage());
					return;
				}
				catch (IOException ex)
				{
					System.err.println(ex.getMessage());
					return;
				}A*/

				//Document d = convertersteptopa.getDoc();

				// Convert to an automatadoc
				//ConverterILtoAutomata conv = new ConverterILtoAutomata();

				//conv.convertILToAutomata(doc);

				//Document ILDoc = conv.getDoc();
				//printAutomataDoc(automataDoc);
				//saveDoc(ILDoc);

				//InterlocksToAutomata ILA = new InterlocksToAutomata();
				//System.out.println("Observe that the automaton representation of the supervisor must be such that the states of the operation models are unique, and separated by a dot in the supervisor states");
				//Document IL = ILA.extractRestrictions(doc);
				//System.out.println("Gui: Efter extract");
				//saveDoc(IL);

				//} catch (JDOMException sxe) {}
			} //if (returnVal ==...
		}

		});

		toolBar.add(button);

		toolBar.addSeparator();

		//Open PPN button
		button = new JButton("OPEN IL MODEL");
		button.setToolTipText("Open IL model");
		button.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

			System.out.println("IL");
		}
		});
		toolBar.add(button);
	}

/****************************************************/
    public void sattRef(Gui guiref) {
			guireferens = guiref;
	}

/****************************************************/
    	public void saveDoc(Document d) {
			Document doc = d;
			String fileName;
			String path = "";

			//Create a file chooser
			final JFileChooser f = new JFileChooser();
			int returnVal = f.showSaveDialog(ToolBar.this);


			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File file = f.getSelectedFile();
				fileName = file.getName();
				path = file.getPath();
			}

			try {
				XMLOutputter outp = new XMLOutputter();
				outp.setFormat(org.jdom.output.Format.getPrettyFormat());

				FileOutputStream fileStream = new FileOutputStream(path);

				outp.output(doc, fileStream);
			}
			catch (FileNotFoundException e) {
				System.out.println("No file");
			}
			catch (IOException sxe) {

			}
		}
}