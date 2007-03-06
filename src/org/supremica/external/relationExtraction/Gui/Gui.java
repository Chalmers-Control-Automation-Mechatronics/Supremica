package org.supremica.external.relationExtraction.Gui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.*;
import java.util.*;

import java.io.*;
import javax.xml.parsers.*;

import org.xml.sax.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;



import org.supremica.external.relationExtraction.Algorithms.*;


public class Gui extends JFrame implements ActionListener{

	Document doc; // An xml-document
	Document supDoc;
	JPanel p = new JPanel();
	JButton openSup = new JButton("Get supervisor");
	JButton openROPs = new JButton("Get DOPs");
	ArrayList ROPs = new ArrayList(); // List containing all ROP-documents
	boolean supRead = false, ROPsRead = false;

	public Gui() {
    	setSize(250,75);
    	setLayout(new GridLayout(1,1));
    	add(p);
    	p.setLayout(new GridLayout(1,1));
    	p.add(openSup);
    	p.add(openROPs);
	}

	public void init() {

		openSup.addActionListener(this);
    	openROPs.addActionListener(this);
    	setVisible(true);
	}

	/* Pushing open gives a window for choosing
	 * a file containing the supervisor as an automaton. */
	public void actionPerformed(ActionEvent e)
	{
		final JFileChooser fc = new JFileChooser();
		File dir = fc.getCurrentDirectory();
		String dirS = dir.getPath();
		//dirS=dirS.concat("\\Testfiler\\COPGeneration");

		File newDir = new File(dirS);
		fc.setCurrentDirectory(newDir);

		if(e.getSource()==openSup)
		{
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
						System.out.println("saxDriverClass "+ saxDriverClass);
					}
				}


				// Create a SAXBuilder
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
					supDoc = builder.build(file); // In org.jdom.input
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
				supRead = true;
			}
		}


		if(e.getSource()==openROPs)
		{
			//final JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled(true);
			int returnVal = fc.showOpenDialog(Gui.this);


			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File[] ROPfiles = fc.getSelectedFiles();

				for( int i=0; i<ROPfiles.length; i++)
				{
					String inputFileName = ROPfiles[i].getName();
					String[] fileNameField = {inputFileName};

					String fileName = fileNameField[0];


					boolean expandEntities = true;
					String saxDriverClass = null;

					if (fileNameField.length > 1)
					{
						if (fileNameField[1].equalsIgnoreCase("false")) {
							expandEntities = false;
						}
						if (fileNameField.length > 2) {
							saxDriverClass = fileNameField[2];
							System.out.println("saxDriverClass "+ saxDriverClass);
						}
					}


					// Create a SAXBuilder
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

						Document ROPDoc = builder.build(ROPfiles[i]);

						ROPs.add(ROPDoc);

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
				}
			}

			ROPsRead = true;
		}


    	if(supRead && ROPsRead)
		{

			Extractor extr = new Extractor();
			String dirS2="";
			System.out.println("Observe that the automaton representation of the supervisor must be such that the states of the operation models are unique, and separated by a dot in the supervisor states");
			ArrayList COPList = extr.extractRestrictions(supDoc, ROPs);

			final JFileChooser f = new JFileChooser();
			f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = f.showSaveDialog(Gui.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File dir2 = f.getSelectedFile();
				dirS2 = dir2.getPath();
			}

			for( Iterator cIter = COPList.iterator(); cIter.hasNext(); )
			{
				Document COP = (Document) cIter.next();
				Element theCOP = COP.getRootElement();
				String id = theCOP.getAttributeValue("id");
				String path = dirS2.concat("\\COP_" + id + ".xml");

				saveDoc(COP, path);
			}

		}
    }


	/****************************************************/
	public void saveDoc(Document d, String path) {
		Document doc = d;

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