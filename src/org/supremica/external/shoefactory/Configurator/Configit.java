/*
* Supremica Software License Agreement
*
* The Supremica software is not in the public domain
* However, it is freely available without fee for education,
* research, and non-profit purposes.  By obtaining copies of
* this and other files that comprise the Supremica software,
* you, the Licensee, agree to abide by the following
* conditions and understandings with respect to the
* copyrighted software:
*
* The software is copyrighted in the name of Supremica,
* and ownership of the software remains with Supremica.
*
* Permission to use, copy, and modify this software and its
* documentation for education, research, and non-profit
* purposes is hereby granted to Licensee, provided that the
* copyright notice, the original author's names and unit
* identification, and this permission notice appear on all
* such copies, and that no charge be made for such copies.
* Any entity desiring permission to incorporate this software
* into commercial products or to use it for commercial
* purposes should contact:
*
* Knut Akesson (KA), knut@supremica.org
* Supremica,
* Haradsgatan 26A
* 431 42 Molndal
* SWEDEN
*
* to discuss license terms. No cost evaluation licenses are
* available.
*
* Licensee may not use the name, logo, or any other symbol
* of Supremica nor the names of any of its employees nor
* any adaptation thereof in advertising or publicity
* pertaining to the software without specific prior written
* approval of the Supremica.
*
* SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
* SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
* IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
*
* Supremica or KA shall not be liable for any damages
* suffered by Licensee from the use of this software.
*
* Supremica is owned and represented by KA.
*/
package org.supremica.external.shoefactory.Configurator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.ImageIcon.*;
import com.configit_software.ctrlmngr.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Configit extends JFrame implements ItemListener,  ActionListener
{
	CS_CtrlMngr ctrl_mngr = new CS_CtrlMngr("../src/org/supremica/external/shoeFactory/Configurator/ShoeFactory.vt", CS_CtrlMngr.CONFIG_STATIC);
	Container c;
	boolean resetok = true;
	JPanel space = new JPanel();
	JPanel buttonPanel = new JPanel();
	JPanel infoSpace = new JPanel();
   	JPanel kommArea = new JPanel();
	JPanel kommArea1 = new JPanel();
	JPanel kommArea2 = new JPanel();
	JPanel additionalChoices = new JPanel();
	JPanel scrollPanel = new JPanel();
	JPanel scrollPanelTitles = new JPanel();
   	JButton JB = new JButton ("Quit");
	JButton JR = new JButton ("Reset");
	JButton JC = new JButton ("Order");
    private boolean isdone =false;
    private String [] Gender = {"Male" ,"Female"};
	private String [] GeneralType = {"Children", "Adult" };
   	private String [] Size = new String [ctrl_mngr.getDomainSize(1)];
	private String [] Color = new String [ctrl_mngr.getDomainSize(3)];
  	private String [] Sole = new String [ctrl_mngr.getDomainSize(4)];
  	private String [] ShoeType = new String [ctrl_mngr.getDomainSize(5)];

	int[] Allowed_Gender = {0,0};
	int[] Allowed_GT = {0,0};
	int[] Allowed_Colors = new int[ctrl_mngr.getDomainSize(3)];
	int[] Allowed_Size = new int[ctrl_mngr.getDomainSize(1)];
	int[] Allowed_Sole = new int[ctrl_mngr.getDomainSize(4)];
 	int[] Allowed_ShoeType = new int[ctrl_mngr.getDomainSize(5)];

	Choice lCol = new Choice();
	CheckboxGroup lGend = new CheckboxGroup();
	CheckboxGroup lGT = new CheckboxGroup();
	Checkbox lGend1 = new Checkbox("Male",false,lGend);
	Checkbox lGend2 = new Checkbox("Female",false,lGend);
	Checkbox lGT1 = new Checkbox("Children",false,lGT);
	Checkbox lGT2 = new Checkbox("Adult",false,lGT);

	Choice lSole = new Choice();
	Choice lSize = new Choice();
	Choice lShoeType = new Choice();

	JLabel JL = new JLabel("You have not selected anything yet. Available combinations: "+ctrl_mngr.getSolutionCount());
	JLabel mainTitle = new JLabel("Select your shoe!");
	JLabel selectColor = new JLabel("Select color");
	JLabel selectSole = new JLabel("Select sole");
	JLabel selectModel = new JLabel("Select model");
	JLabel selectSize = new JLabel("Select size");

	Border panelBorder = BorderFactory.createRaisedBevelBorder();
	Border choicesBorder = BorderFactory.createTitledBorder(panelBorder,"Additional choices");

   	ImageIcon icon1 = new ImageIcon("../src/org/supremica/external/shoeFactory/Configurator/blshoe.gif");
	ImageIcon icon2 = new ImageIcon("../src/org/supremica/external/shoeFactory/Configurator/pnkshoe.gif");

	public Configit ()
	{
	    setTitle("Configit");
    	addWindowListener(new WindowAdapter()
    	{
			public void windowClosing(WindowEvent e)
			{
         		System.exit(0);
    		}
   		});
   		Res_Sellist();

		for(int val = 0; val < ctrl_mngr.getDomainSize(1); val++)
		{
			Size[val] = ctrl_mngr.getValueName(1, val);
			lSize.add(Size[val]);
		}

		for(int val = 0; val < ctrl_mngr.getDomainSize(3); val++)
		{
			Color[val] = ctrl_mngr.getValueName(3, val);
			lCol.add(Color[val]);
		}
		for(int val = 0; val < ctrl_mngr.getDomainSize(4); val++)
		{
			Sole[val] = ctrl_mngr.getValueName(4, val);
			lSole.add(Sole[val]);
		}
		for(int val = 0; val < ctrl_mngr.getDomainSize(5); val++)
		{
			ShoeType[val]= ctrl_mngr.getValueName(5, val);
			lShoeType.add(ShoeType[val]);
		}
   		lSize.add("");
   		lCol.add("");
   		lSole.add("");
   		lShoeType.add("");
		lSize.select("");
		lCol.select("");
		lSole.select("");
		lShoeType.select("");

		c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(space, "South");
		c.add(kommArea, "North");
		c.add(additionalChoices, "Center");

		mainTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
		mainTitle.setHorizontalAlignment(0);

		kommArea.setLayout(new BorderLayout(20,20));
		kommArea.setBorder(panelBorder);
		kommArea1.setLayout(new FlowLayout(FlowLayout.CENTER));
		kommArea2.setLayout(new FlowLayout(FlowLayout.CENTER));
		kommArea.add(mainTitle, "North");
		kommArea.add(kommArea1, "Center");
		kommArea.add(kommArea2, "South");
		kommArea.add(new JLabel(icon2), "West");
		kommArea.add(new JLabel(icon1), "East");
		kommArea.setOpaque(false);
		kommArea1.add(lGend1);
		kommArea1.add(lGend2);
		kommArea2.add(lGT1);
		kommArea2.add(lGT2);

		additionalChoices.setLayout(new BorderLayout());
		additionalChoices.setBorder(choicesBorder);
		additionalChoices.add(scrollPanelTitles, "North");
		additionalChoices.add(scrollPanel, "South");

		scrollPanelTitles.setLayout(new GridLayout(1,4,20,0));
		scrollPanelTitles.add(selectColor);
		scrollPanelTitles.add(selectSole);
		scrollPanelTitles.add(selectSize);
		scrollPanelTitles.add(selectModel);
		selectColor.setFont(new Font("Dialog", Font.PLAIN, 12));
		selectSole.setFont(new Font("Dialog", Font.PLAIN, 12));
		selectSize.setFont(new Font("Dialog", Font.PLAIN, 12));
		selectModel.setFont(new Font("Dialog", Font.PLAIN, 12));

		scrollPanel.setLayout(new GridLayout(1,4,20,0));
		scrollPanel.add(lCol);
		scrollPanel.add(lSole);
		scrollPanel.add(lSize);
		scrollPanel.add(lShoeType);

		space.setLayout(new BorderLayout());
		space.add(infoSpace, "North");
		space.add(buttonPanel, "South");

		infoSpace.setLayout(new FlowLayout(FlowLayout.CENTER));
		infoSpace.add(JL);

		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.setBorder(panelBorder);
		buttonPanel.add(JR, FlowLayout.LEFT);
		buttonPanel.add(JB, FlowLayout.CENTER);
		buttonPanel.add(JC, FlowLayout.RIGHT);

		lCol.addItemListener(this);
		lGend1.addItemListener(this);
		lGend2.addItemListener(this);
		lGT1.addItemListener(this);
		lGT2.addItemListener(this);
		lSole.addItemListener(this);
		lShoeType.addItemListener(this);
		lSize.addItemListener(this);
		lSole.addItemListener(this);

		JB.addActionListener(this);
		JR.addActionListener(this);
		JC.addActionListener(this);

		pack();
    }

   	public String UnStr (String S)
	{
		char [] tmp =new char [S.length()+2];
		int i=0;
		tmp[0]='(';
		while(i<S.length())
		{
			tmp[i+1]=S.charAt(i);
			i=i+1;
		}
		tmp[i+1]=')';
		String str = new String(tmp);
		return str;
	}

	private static String printValueState(CS_CtrlMngr ctrl_mngr, int variable, int value)
	{
    	String state;
	    switch (ctrl_mngr.getState(variable, value))
	    {
    		case CS_CtrlMngr.VALUE_FORCEABLE     : state = "Forceable"; break;
    		case CS_CtrlMngr.VALUE_BLOCKED       : state = "Blocked"; break;
    		case CS_CtrlMngr.VALUE_SELECTABLE    : state = "Selectable"; break;
    		case CS_CtrlMngr.VALUE_USER_SELECTED : state = "User Selected"; break;
    		case CS_CtrlMngr.VALUE_SYS_SELECTED  : state = "System Selected"; break;
    		default :                              state = "Unknown";
    	}
		return (state);
	}

	public void Res_Sellist()
	{
		for(int i=0;i<2;i++)
		{
			Allowed_GT[i] = 0;
			Allowed_Gender[i] = 0;
		}
		for(int i=0;i< ctrl_mngr.getDomainSize(1);i++)
			Allowed_Size[i] = 0;
		for(int i=0;i< ctrl_mngr.getDomainSize(3);i++)
			Allowed_Colors[i] = 0;
		for(int i=0;i< ctrl_mngr.getDomainSize(4);i++)
			Allowed_Sole[i] = 0;
		for(int i=0;i< ctrl_mngr.getDomainSize(5);i++)
			Allowed_ShoeType[i] = 0;
	}

	public void reset()
	{
		if(resetok)
		{
			lCol.removeItemListener(this);
			lGend1.removeItemListener(this);
			lGend2.removeItemListener(this);
			lGT1.removeItemListener(this);
			lGT2.removeItemListener(this);
			lSole.removeItemListener(this);
			lShoeType.removeItemListener(this);
			lSize.removeItemListener(this);
			lSole.removeItemListener(this);

			kommArea1.removeAll();
			kommArea2.removeAll();
			scrollPanel.removeAll();

			if (Allowed_GT[0] == 0)
				lGT1 = new Checkbox("Children",false, lGT);
			else if (Allowed_GT[0] == 1)
				lGT1 = new Checkbox("(Children)",false, lGT);
			else if (Allowed_GT[0] == 2)
				lGT1 = new Checkbox("Children",true, lGT);

			if (Allowed_GT[1] == 0)
				lGT2 = new Checkbox("Adult",false, lGT);
			else if (Allowed_GT [1] ==1)
				lGT2 = new Checkbox("(Adult)",false, lGT);
			else if (Allowed_GT[1] == 2)
				lGT2 = new Checkbox("Adult",true, lGT);

			if (Allowed_Gender[0] == 0)
				lGend1 = new Checkbox("Male",false,lGend);
			else if (Allowed_Gender [0] ==1)
				lGend1 = new Checkbox("(Male)",false,lGend);
			else if (Allowed_Gender [0] ==2)
				lGend1 = new Checkbox("Male",true, lGend);

			if (Allowed_Gender[1] == 0)
				lGend2 = new Checkbox("Female",false,lGend);
			else if (Allowed_Gender [1] ==1)
				lGend2 = new Checkbox("(Female)",false,lGend);
			else if (Allowed_Gender [1] ==2)
				lGend2 = new Checkbox("Female",true, lGend);

			lCol = new Choice();
			lSole = new Choice();
			lSize = new Choice();
			lShoeType = new Choice();

			boolean ifSelected = false;
			for(int i=0; i<ctrl_mngr.getDomainSize(3);i++)
			{
				if(Allowed_Colors[i] ==1)
				{
					lCol.add(UnStr(Color[i]));
				}
				if(Allowed_Colors[i] ==2)
				{
					ifSelected = true;
					lCol.add(Color[i]);
					lCol.select(i);
				}
				else if(Allowed_Colors[i] ==0)
					lCol.add(Color[i]);
			}
			if(!ifSelected)
			{
				lCol.add("");
				lCol.select("");
			}

			ifSelected = false;
			for(int i=0; i<ctrl_mngr.getDomainSize(4); i++)
			{
				if(Allowed_Sole[i]==1)
				{
					lSole.add(UnStr(Sole[i]));
				}
				if(Allowed_Sole[i]==2)
				{
					ifSelected = true;
					lSole.add(Sole[i]);
					lSole.select(i);
				}
				else if (Allowed_Sole[i]==0)
					lSole.add(Sole[i]);
			}
			if(!ifSelected)
			{
				lSole.add("");
				lSole.select("");
			}

			ifSelected = false;
			for(int i=0; i<ctrl_mngr.getDomainSize(1); i++)
			{
				if(Allowed_Size[i]==1)
				{
					lSize.add(UnStr(Size[i]));
				}
				if(Allowed_Size[i] ==2)
				{
					ifSelected = true;
					lSize.add(Size[i]);
					lSize.select(i);
				}
				else if (Allowed_Size[i]==0)
					lSize.add(Size[i]);
			}
			if(!ifSelected)
			{
				lSize.add("");
				lSize.select("");
			}

			ifSelected = false;
			for(int i=0; i<ctrl_mngr.getDomainSize(5); i++)
			{
				if(Allowed_ShoeType[i]==1)
				{
					lShoeType.add(UnStr(ShoeType[i]));
				}
				if(Allowed_ShoeType[i]==2)
				{
					ifSelected = true;
					lShoeType.add(ShoeType[i]);
					lShoeType.select(i);
				}
				else if(Allowed_ShoeType[i]==0)
					lShoeType.add(ShoeType[i]);
			}
			if(!ifSelected)
			{
				lShoeType.add("");
				lShoeType.select("");
			}

			kommArea1.setLayout(new FlowLayout(FlowLayout.CENTER));
			kommArea2.setLayout(new FlowLayout(FlowLayout.CENTER));
			kommArea1.add(lGend1);
			kommArea1.add(lGend2);
			kommArea2.add(lGT1);
			kommArea2.add(lGT2);

			scrollPanel.setLayout(new GridLayout(1,4,20,0));
			scrollPanel.add(lCol);
			scrollPanel.add(lSole);
			scrollPanel.add(lSize);
			scrollPanel.add(lShoeType);

			lCol.addItemListener(this);
			lGend1.addItemListener(this);
			lGend2.addItemListener(this);
			lGT1.addItemListener(this);
			lGT2.addItemListener(this);
			lSole.addItemListener(this);
			lShoeType.addItemListener(this);
			lSize.addItemListener(this);
			lSole.addItemListener(this);

			pack();
		}
	}

	public void update()
	{
   		String S;
        for(int var = 0; var < ctrl_mngr.getVarCount(); var++ )
        {
			for(int val = 0; val < ctrl_mngr.getDomainSize(var); val++ )
			{
    			S = printValueState(ctrl_mngr, var, val);

				if (S.compareTo("Selectable")==0 || S.compareTo("System Selected")==0)
				{
					if(var ==0)
						Allowed_Gender[val]=0;
					if(var ==1)
						Allowed_Size[val]=0;
					if(var ==2)
						Allowed_GT[val]=0;
					if(var ==3)
						Allowed_Colors[val]=0;
					if(var ==4)
						Allowed_Sole[val]=0;
					if(var ==5)
						Allowed_ShoeType[val]=0;
				}

			   	else if(S.compareTo("Forceable")==0)
   				{
					if(var ==0)
						Allowed_Gender[val]=1;
					if(var ==1)
						Allowed_Size[val]=1;
					if(var ==2)
						Allowed_GT[val]=1;
					if(var ==3)
						Allowed_Colors[val]=1;
					if(var ==4)
						Allowed_Sole[val]=1;
					if(var ==5)
						Allowed_ShoeType[val]=1;
				}

				else if (S.compareTo("User Selected")==0)
				{
					if(var ==0)
						Allowed_Gender[val]=2;
					if(var ==1)
						Allowed_Size[val]=2;
					if(var ==2)
						Allowed_GT[val]=2;
					if(var ==3)
						Allowed_Colors[val]=2;
					if(var ==4)
						Allowed_Sole[val]=2;
					if(var ==5)
						Allowed_ShoeType[val]=2;
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e)
	{
      	if(e.getSource() == JB){
			this.setVisible(false);
        	//System.exit(0);
		}
		if(e.getSource() == JR)
		{
			ctrl_mngr.resetConf();
			Res_Sellist();
			reset();
			resetok = false;
			JL.setText("You have not selected anything yet. Available combinations: "+ctrl_mngr.getSolutionCount());
		}

		if(e.getSource() == JC)
		{
			if(ctrl_mngr.getSelectedValue(3)==-1 || ctrl_mngr.getSelectedValue(1)==-1 || ctrl_mngr.getSelectedValue(4)==-1 || ctrl_mngr.getSelectedValue(5)==-1)
				JOptionPane.showMessageDialog(this, "You have not completed the form", "Please check your order", JOptionPane.ERROR_MESSAGE);
			else
			{
				int selection = JOptionPane.showConfirmDialog(this, "You have made the following choices:\nColor: "+
				ctrl_mngr.getValueName(3, ctrl_mngr.getSelectedValue(3))+"\nSize: "+
				ctrl_mngr.getValueName(1, ctrl_mngr.getSelectedValue(1))+"\nSole: "+
				ctrl_mngr.getValueName(4, ctrl_mngr.getSelectedValue(4))+"\nModel: "+
				ctrl_mngr.getValueName(5, ctrl_mngr.getSelectedValue(5))+
				"\nPress OK to confirm.", "Submit order", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

				if(selection == JOptionPane.OK_OPTION)
				{
					ctrl_mngr.completeConf();
					isdone =true;
					ctrl_mngr.resetConf();
					Res_Sellist();
					reset();
					resetok = false;
					JL.setText("You have not selected anything yet. Available combinations: "+ctrl_mngr.getSolutionCount());
				}
			}
		}
		isdone =false;
	}

	public void itemStateChanged(ItemEvent e)
	{
		resetok=true;
		double Solutions;

		if(e.getSource() ==lCol)
		{
			for(int i=0;i< ctrl_mngr.getDomainSize(3);i++)
			{
				if(lCol.getSelectedIndex()==i )
				{
					assign(3,i);
					Solutions=ctrl_mngr.getSolutionCount();
					JL.setText("Available combinations: " + Solutions);
				}
			}
		}

		if(e.getSource() == lGend1)
		{
			assign(0,0);
			Solutions=ctrl_mngr.getSolutionCount();
			JL.setText("Available combinations: " +Solutions);
		}

		if(e.getSource() == lGend2)
		{
			assign(0,1);
			Solutions=ctrl_mngr.getSolutionCount();
			JL.setText("Available combinations: " +Solutions);
		}

		if(e.getSource() == lGT1)
		{
   			assign(2,0);
			Solutions=ctrl_mngr.getSolutionCount();
			JL.setText("Available combinations: " +Solutions);
		}

		if(e.getSource() == lGT2)
		{
   			assign(2,1);
			Solutions=ctrl_mngr.getSolutionCount();
			JL.setText("Available combinations: " +Solutions);
		}

		if(e.getSource() == lSole)
		{
			for(int i=0;i<ctrl_mngr.getDomainSize(4);i++)
			{
				if(lSole.getSelectedIndex()==i)
				{
					assign(4,i);
					Solutions=ctrl_mngr.getSolutionCount();
					JL.setText("Available combinations: " +Solutions);
				}
			}
		}

		if(e.getSource () == lSize)
		{
			for(int i=0;i< ctrl_mngr.getDomainSize(1);i++)
			{
				if(lSize.getSelectedIndex()==i )
				{
					assign(1,i);
					Solutions=ctrl_mngr.getSolutionCount();
					JL.setText("Available combinations: " +Solutions);
				}
			}
		}

		if(e.getSource () == lShoeType)
		{
			for(int i=0;i< ctrl_mngr.getDomainSize(5);i++)
			{
				if(lShoeType.getSelectedIndex()==i)
				{
					assign(5,i);
					Solutions=ctrl_mngr.getSolutionCount();
					JL.setText("Available combinations: " +Solutions);
				}
			}
		}
		update();
		reset();
	}

	public void assign(int var,int val)
	{
		boolean force = ctrl_mngr.getState(var, val) == CS_CtrlMngr.VALUE_FORCEABLE;
        if(force)
        {
          	int selection = JOptionPane.showConfirmDialog(this, "The selected item is not compatible with previous choices.\nPress OK to force your new choice.\nPress Cancel to return without changes", "Conflicting selections", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if(selection == JOptionPane.OK_OPTION)
			{
				ctrl_mngr.removeSelection(var, true, false, false);
				ctrl_mngr.assignValue(var, val, force);
			}
		}
		else
		{
			ctrl_mngr.removeSelection(var, true, false, false);
			ctrl_mngr.assignValue(var, val, false);
		}
	}

	public boolean finished ()
	{
		return isdone;
	}

}
