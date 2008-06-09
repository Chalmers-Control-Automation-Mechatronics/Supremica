package org.supremica.external.avocades.specificationsynthesis;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.xml.parsers.*;

import org.xml.sax.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;

import org.w3c.dom.Document;

public class ReadAndWrite extends JPanel {

    protected String newline = "\n";

    //PPNGui guireferens;
    Document doc;
	//ConverterSTEPtoPA convertersteptopa;
	//ConverterPAtoAutomata converterpatoautomata;

    public ReadAndWrite() {

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
			int returnVal = fc.showOpenDialog(ReadAndWrite.this);

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
				//try {
				SAXBuilder builder = null;

					if (saxDriverClass == null) {
						builder = new SAXBuilder();
					} else {
						builder = new SAXBuilder(saxDriverClass);
					}

				// Create an JDOM object
				builder.setExpandEntities(expandEntities);
				/*try
				{
					doc = builder.build(path);
				}
				catch (IOException ex)
				{
					System.err.println(ex.getMessage());
					return;
				}*/
				//Create a DocTree and updates the gui
				/*DOMOutputter outputter1 = new DOMOutputter();
				outputter1.output(doc);
				XMLTree tree = new XMLTree(outputter1.output(doc));
				guireferens.UppdateraTree(tree);

				// Convert the opened doc to a processalgebra doc
				convertersteptopa = new ConverterSTEPtoPA();
				convertersteptopa.convertSTEPtoPA(doc);
				Document PAdoc = convertersteptopa.getDoc();
				printPPNDoc(PAdoc);
				saveDoc(PAdoc);*/

				//} catch (JDOMException sxe) {}
			}
		}
		});
		toolBar.add(button);
	}

}