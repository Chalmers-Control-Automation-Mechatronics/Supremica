package org.supremica.external.specificationSynthesis;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import org.supremica.external.specificationSynthesis.algorithms.*;
import org.supremica.external.specificationSynthesis.gui.*;




public class SpecificationSynthesis extends JFrame {

	Gui window;

    public SpecificationSynthesis() {

	}

	public static void main(String[] args) {
		Gui gui  = new Gui();
		gui.init();
    }
}