package org.supremica.external.specificationSynthesis.gui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.*;

import java.io.*;
import javax.xml.parsers.*;

import org.xml.sax.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;

import org.supremica.external.specificationSynthesis.algorithms.*;


public class Gui extends JFrame implements ActionListener{

	Document ILDoc;
	Document EOPDoc; // An xml-document
	JPanel p = new JPanel();
	JButton open = new JButton("Get EOP file");
	JButton openIL = new JButton("Get IL file");
	boolean ILread = false, EOPread = false;


    public Gui() {
    	setSize(250,75);
    	setLayout(new GridLayout(1,1));
    	add(p);
    	p.setLayout(new GridLayout(1,1));
    	p.add(open);
    	p.add(openIL);
	}

	public void init() {
		openIL.addActionListener(this);
    	open.addActionListener(this);
    	setVisible(true);
	}


	/* Pushing open gives a window for choosing
	 * a file containing the supervisor as an automaton.
	 * Then a window for choosing where to
	 * save the supervisor appears. */
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==openIL)
		{
			System.out.println("Open IL");
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(Gui.this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();
				String inputFileName = file.getName();
				String[] fileNameField = {inputFileName};

				// Load filename and SAX driver class

				String filename = fileNameField[0];

				boolean expandEntities = true;
				String saxDriverClass = null;

				if (fileNameField.length > 1)
				{
					if (fileNameField[1].equalsIgnoreCase("false")) {
						expandEntities = false;
					}
					if (fileNameField.length > 2) {
						saxDriverClass = fileNameField[2];
					}
				} //if(fileNameField.length > 1)


				// Create a SAXBuilder
				String path = file.getPath(); // Path to the choosen file

				System.out.println("Open " + path);
				try
				{
				SAXBuilder builder = null; // In org.jdom.input
					if (saxDriverClass == null)
					{
						builder = new SAXBuilder();
						System.out.println("No saxDriverClass given");
					}
					else
					{
						builder = new SAXBuilder(saxDriverClass);
					}

					// Create an JDOM object
					// This sets whether or not to expand entities during the build.
					// A true means to expand entities as normal content. A false means
					// to leave entities unexpanded as EntityRef objects. The default is true.
					builder.setExpandEntities(expandEntities);
					if(expandEntities)
						System.out.println("Expand entities.");
					else
						System.out.println("Don´t expand entities.");

					// Builds a JDOM document from an xml-file.
					ILDoc = builder.build(file); // In org.jdom.input
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
				}
			ILread = true;
			}
		}
		if(e.getSource()==open)
		{
			System.out.println("Open EOP");
			final JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(Gui.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File file = fc.getSelectedFile();
				String inputFileName = file.getName();
				String[] fileNameField = {inputFileName};

				// Load filename and SAX driver class

				String filename = fileNameField[0];

				boolean expandEntities = true;
				String saxDriverClass = null;

				if (fileNameField.length > 1)
				{
					if (fileNameField[1].equalsIgnoreCase("false")) {
						expandEntities = false;
					}
					if (fileNameField.length > 2) {
						saxDriverClass = fileNameField[2];
					}
				} //if(fileNameField.length > 1)


				// Create a SAXBuilder
				String path = file.getPath(); // Path to the choosen file

				System.out.println("Open " + path);
				try
				{
					SAXBuilder builder = null; // In org.jdom.input
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
				builder.setExpandEntities(expandEntities);
				if(expandEntities)
					System.out.println("Expand entities.");
				else
					System.out.println("Don´t expand entities.");

				// Builds a JDOM document from an xml-file.
				EOPDoc = builder.build(file); // In org.jdom.input
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
				}
			EOPread = true;
			}

		}

		if(ILread && EOPread)
		{


			ConverterILandEOPtoILEOP convILEOP = new ConverterILandEOPtoILEOP();
			convILEOP.convertILandEOPtoILEOP(ILDoc, EOPDoc);
			Document ILEOPDoc = convILEOP.getDoc();
			System.out.println("Saving IL-EOP file.");
			saveDoc(ILEOPDoc);


			ConverterILtoAutomata convAut = new ConverterILtoAutomata();

			convAut.convertILtoAutomata(ILEOPDoc);


			Document AutDoc = convAut.getDoc();
			System.out.println("Saving automata file.");
			saveDoc(AutDoc);
		}
	}



  /****************************************************/
	public void saveDoc(Document d)
	{
		Document doc = d;
		String fileName;
		String path = "";

		//Create a file chooser
		final JFileChooser f = new JFileChooser();
		int returnVal = f.showSaveDialog(Gui.this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File file = f.getSelectedFile();
			fileName = file.getName();

			path = file.getPath();

		}

		try
		{
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