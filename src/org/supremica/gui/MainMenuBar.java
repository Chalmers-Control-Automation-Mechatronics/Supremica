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

/********************* MainMenuBar.java ***************/
// Free standing leaf class implementing Supremicas
// main menu bar. Prime reason for this is easy access
// The class instantiates itself with the menu stuff
/* Note:
	This
		JMenuItem menuFileNew = new JMenuItem();

		menuFileNew.setText("New...");
		menuFileNew.setEnabled(false);
		menuFile.add(menuFileNew);
		menuFileNew.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.fileNew(ActionMan.getGui());
			}
		});

	Should be replaced with this
		JMenuItem menuFileNew = new JMenuItem(ActionMan.fileNewAction);
		menuFile.add(menuFileNew);

	where fileNewAction is a static AbstractAction descendant
*/
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import java.net.URL;

import org.supremica.gui.help.ContentHelp;
import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.templates.TemplateItem;
import org.supremica.automata.templates.TemplateGroup;
import org.supremica.gui.animators.scenebeans.AnimationItem;
import org.supremica.gui.animators.scenebeans.AnimationGroup;
import org.supremica.util.BrowserControl;
import org.supremica.util.SupremicaMenuItem;
import org.supremica.automata.algorithms.GeneticAlgorithms;
import org.supremica.automata.algorithms.RobotStudioLink;

public class MainMenuBar
	extends JMenuBar
{
	private Supremica supremica;
 	private ContentHelp help = null;
	private CSH.DisplayHelpFromSource helpDisplayer = null;

	public MainMenuBar(Supremica supremica) // should get rid of supremica here
	{
		this.supremica = supremica;
		this.help = new ContentHelp();
		this.helpDisplayer = new CSH.DisplayHelpFromSource(help.getStandardHelpBroker());

		initMenubar();
	}

	private void initMenubar()	// This is copied (almost) straight from Supremica.java
	{
		class NewFromTemplateHandler
			implements ActionListener
		{
			private TemplateItem item = null;

			public NewFromTemplateHandler(TemplateItem item)
			{
				this.item = item;
			}

			public void actionPerformed(ActionEvent e)
			{
				ActionMan.fileNewFromTemplate(ActionMan.getGui(), item);
			}
		}

		class ToolsAnimationHandler
			implements ActionListener
		{
			private AnimationItem item = null;

			public ToolsAnimationHandler(AnimationItem item)
			{
				this.item = item;
			}

			public void actionPerformed(ActionEvent e)
			{
				ActionMan.animator(ActionMan.getGui(), item);
			}
		}

		boolean separatorNeeded = false;


		// File
		JMenu menuFile = new JMenu();

		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		add(menuFile);

		// File.New
		JMenuItem menuFileNew = new JMenuItem();

		menuFileNew.setText("New...");
		menuFileNew.setEnabled(false);
		menuFile.add(menuFileNew);
		menuFileNew.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.fileNew(ActionMan.getGui());
			}
		});

		// File.NewFromTemplate
		JMenu menuFileNewFromTemplate = new JMenu();

		menuFileNewFromTemplate.setText("New From Template");
		menuFile.add(menuFileNewFromTemplate);

		ExampleTemplates exTempl = ExampleTemplates.getInstance();

		for (Iterator groupIt = exTempl.iterator(); groupIt.hasNext(); )
		{
			TemplateGroup currGroup = (TemplateGroup) groupIt.next();
			JMenu menuFileNewFromTemplateGroup = new JMenu();

			menuFileNewFromTemplateGroup.setText(currGroup.getDescription());
			menuFileNewFromTemplate.add(menuFileNewFromTemplateGroup);

			for (Iterator itemIt = currGroup.iterator(); itemIt.hasNext(); )
			{
				TemplateItem currItem = (TemplateItem) itemIt.next();
				JMenuItem menuItem = new JMenuItem();

				menuItem.setText(currItem.getDescription());
				menuFileNewFromTemplateGroup.add(menuItem);
				menuItem.addActionListener(new NewFromTemplateHandler(currItem));
			}
		}

		if (SupremicaProperties.fileAllowOpen())
		{

			// File.Open
			/* JMenuItem menuFileOpen = new JMenuItem();

			menuFileOpen.setText("Open...");
			menuFile.add(menuFileOpen);
			menuFileOpen.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileOpen(ActionMan.getGui());
				}
			});*/
			JMenuItem menuFileOpen = new SupremicaMenuItem(ActionMan.openAction);
			menuFile.add(menuFileOpen);

			separatorNeeded = true;
		}

		if (SupremicaProperties.fileAllowSave())
		{

			// File.Save
			JMenuItem menuFileSave = new JMenuItem();

			menuFileSave.setText("Save");
			menuFileSave.setMnemonic(KeyEvent.VK_S);
			menuFile.add(menuFileSave);
			menuFileSave.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileSave(ActionMan.getGui());
				}
			});

			// File.SaveAs
			JMenuItem menuFileSaveAs = new JMenuItem();

			menuFileSaveAs.setText("Save As...");
			menuFile.add(menuFileSaveAs);
			menuFileSaveAs.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileSaveAs(ActionMan.getGui());
				}
			});

			separatorNeeded = true;
		}

		if (separatorNeeded)
		{
			menuFile.addSeparator();
			separatorNeeded = false;
		}

		if (SupremicaProperties.fileAllowImport())
		{
			// File.Import
			JMenu menuFileImport = new JMenu();

			menuFileImport.setText("Import");
			menuFile.add(menuFileImport);

			// File.Import.Desco
			JMenuItem menuFileImportDesco = new JMenuItem();
			menuFileImportDesco.setEnabled(false);

			menuFileImportDesco.setText("From Desco...");
			menuFileImport.add(menuFileImportDesco);
			menuFileImportDesco.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileImportDesco(ActionMan.getGui());
				}
			});

			// File.Import.TCT
			JMenuItem menuFileImportTCT = new JMenuItem();
			menuFileImportTCT.setEnabled(false);

			menuFileImportTCT.setText("From TCT...");
			menuFileImport.add(menuFileImportTCT);
			menuFileImportTCT.setEnabled(false);
			menuFileImportTCT.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					// ActionMan.fileImportTCT(ActionMan.getGui());
				}
			});

			// File.Import.UMDES
			JMenuItem menuFileImportUMDES = new JMenuItem();
			menuFileImportDesco.setEnabled(false);

			menuFileImportUMDES.setText("From UMDES...");
			menuFileImport.add(menuFileImportUMDES);
			menuFileImportUMDES.setEnabled(false);
			menuFileImportUMDES.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					// ActionMan.fileImportUMDES(this);
				}
			});

			// File.Import.Valid
			JMenuItem menuFileImportValid = new JMenuItem();

			menuFileImportValid.setText("From Valid...");
			menuFileImport.add(menuFileImportValid);
			menuFileImportValid.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileImportValid(ActionMan.getGui());
				}
			});



			if (SupremicaProperties.generalUseRobotCoordination())
			{
				// File.Import.RobotCoordination
				JMenuItem menuFileImportRobotCoordination = new JMenuItem();

				menuFileImportRobotCoordination.setText("From Robot Coordinator...");
				menuFileImport.add(menuFileImportRobotCoordination);
				menuFileImportRobotCoordination.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.fileImportRobotCoordination(ActionMan.getGui());
					}
				});
			}

			if (SupremicaProperties.generalUseRobotCoordinationABB())
			{
				// File.Import.RobotCoordinationABB
				JMenuItem menuFileImportRobotCoordinationABB = new JMenuItem();

				menuFileImportRobotCoordinationABB.setText("From Robot Coordinator ABB...");
				menuFileImport.add(menuFileImportRobotCoordinationABB);
				menuFileImportRobotCoordinationABB.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.fileImportRobotCoordinationABB(ActionMan.getGui());
					}
				});
			}

			separatorNeeded = true;
		}

		if (SupremicaProperties.fileAllowExport())
		{
			// File.Export
			JMenu menuFileExport = new JMenu();

			menuFileExport.setText("Export");
			menuFile.add(menuFileExport);

			// File.Export.Html
			JMenuItem menuFileExportHtml = new JMenuItem();

			menuFileExportHtml.setText("To Html...");
			menuFileExport.add(menuFileExportHtml);
			menuFileExportHtml.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileExportHtml(ActionMan.getGui());
				}
			});


			// File.Export.Desco
			JMenuItem menuFileExportDesco = new JMenuItem();

			menuFileExportDesco.setText("To Desco...");
			menuFileExport.add(menuFileExportDesco);
			menuFileExportDesco.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileExportDesco(ActionMan.getGui());
				}
			});

			// File.Export.TCT
			JMenuItem menuFileExportTCT = new JMenuItem();
			menuFileExportTCT.setEnabled(false);

			menuFileExportTCT.setText("To TCT...");
			menuFileExport.add(menuFileExportTCT);
			menuFileExportTCT.setEnabled(false);
			menuFileExportTCT.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					// ActionMan.fileExportTCT(ActionMan.getGui());
				}
			});

			// File.Export.UMDES
			JMenuItem menuFileExportUMDES = new JMenuItem();
			menuFileExportUMDES.setEnabled(false);

			menuFileExportUMDES.setText("To UMDES...");
			menuFileExport.add(menuFileExportUMDES);
			menuFileExportUMDES.setEnabled(false);
			menuFileExportUMDES.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					// ActionMan.fileExportUMDES(ActionMan.getGui());
				}
			});

			// File.Export.Valid
			JMenuItem menuFileExportValid = new JMenuItem();
			menuFileExportValid.setEnabled(false);

			menuFileExportValid.setText("To Valid...");
			menuFileExport.add(menuFileExportValid);
			menuFileExportUMDES.setEnabled(false);
			menuFileExportValid.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileExportValid(ActionMan.getGui());
				}
			});

			// ++ ARASH:
			// File.Export.RCP
			if (SupremicaProperties.generalUseRCP())
			{
				JMenuItem menuFileExportRCP = new JMenuItem();

				menuFileExportRCP.setText("To RCP...");
				menuFileExport.add(menuFileExportRCP);
				menuFileExportRCP.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.fileExportRCP(ActionMan.getGui());
					}
				});

				separatorNeeded = true;
			}
		}

		if (separatorNeeded)
		{
			menuFile.addSeparator();

			separatorNeeded = false;
		}

		if (SupremicaProperties.generalUseSecurity())
		{
			// File.Login
			JMenuItem menuFileLogin = new JMenuItem();

			menuFileLogin.setText("Login");
			menuFile.add(menuFileLogin);
			menuFileLogin.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileLogin(ActionMan.getGui());
				}
			});

			separatorNeeded = true;
		}

		if (separatorNeeded)
		{
			menuFile.addSeparator();

			separatorNeeded = false;
		}

		if (SupremicaProperties.fileAllowQuit())
		{
			// File.Exit
			JMenuItem menuFileExit = new JMenuItem();

			menuFileExit.setText("Exit");
			menuFile.add(menuFileExit);
			menuFileExit.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileExit(ActionMan.getGui());
				}
			});
		}
		else
		{
			// File.Close
			JMenuItem menuFileExit = new JMenuItem();

			menuFileExit.setText("Close");
			menuFile.add(menuFileExit);
			menuFileExit.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.fileClose(ActionMan.getGui());
				}
			});
		}

		// Project
		JMenu menuProject = new JMenu();

		menuProject.setText("Project");
		menuProject.setMnemonic(KeyEvent.VK_P);
		add(menuProject);

		// Project.Rename
		JMenuItem menuProjectRename = new JMenuItem();

		menuProjectRename.setText("Rename...");
		menuProject.add(menuProjectRename);
		menuProjectRename.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				supremica.renameProject();
			}
		});

		menuProject.addSeparator();

		// Project.ActionAndControlViewer
		JMenuItem menuProjectActionAndControlViewer = new JMenuItem();

		menuProjectActionAndControlViewer.setText("Execution Parameters...");
		menuProject.add(menuProjectActionAndControlViewer);
		menuProjectActionAndControlViewer.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.actionAndControlViewer_actionPerformed(ActionMan.getGui());
			}
		});

		if (SupremicaProperties.includeAnimator())
		{
			// Project.Animator
			JMenuItem menuProjectAnimator = new JMenuItem();

			menuProjectAnimator.setText("Animator...");
			menuProject.add(menuProjectAnimator);
			menuProjectAnimator.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.animator_actionPerformed(ActionMan.getGui());
				}
			});

			// Project.Simulator
			JMenuItem menuProjectSimulator = new JMenuItem();

			menuProjectSimulator.setText("Simulator...");
			menuProject.add(menuProjectSimulator);
			menuProjectSimulator.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.simulator_actionPerformed(ActionMan.getGui());
				}
			});

			// Project.Clear
			JMenuItem menuProjectSimulatorClear = new JMenuItem();

			menuProjectSimulatorClear.setText("Clear Simulation Data");
			menuProject.add(menuProjectSimulatorClear);
			menuProjectSimulatorClear.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.simulatorClear_actionPerformed(ActionMan.getGui());
				}
			});
		}

		// Tools
		JMenu menuTools = new JMenu();

		menuTools.setText("Tools");
		menuTools.setMnemonic(KeyEvent.VK_T);
		add(menuTools);

		// Tools.TestCases
		JMenuItem test_cases = new JMenuItem();

		test_cases.setText("Test Cases...");
		menuTools.add(test_cases);
		test_cases.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					ActionMan.testCases(ActionMan.getGui());
				}
				catch (Exception excp)
				{
					// what the f*** do we do?
				}
			}
		});

		// Tools.Animations
		if (SupremicaProperties.includeAnimator())
		{
			JMenu menuToolsAnimations = new JMenu();

			menuToolsAnimations.setText("Animations");
			menuTools.add(menuToolsAnimations);

			ExampleAnimations exAnim = ExampleAnimations.getInstance();

			for (Iterator groupIt = exAnim.iterator(); groupIt.hasNext(); )
			{
				AnimationGroup currGroup = (AnimationGroup) groupIt.next();
				JMenu menuToolsAnimationGroup = new JMenu();

				menuToolsAnimationGroup.setText(currGroup.getDescription());
				menuToolsAnimations.add(menuToolsAnimationGroup);

				for (Iterator itemIt = currGroup.iterator(); itemIt.hasNext(); )
				{
					AnimationItem currItem = (AnimationItem) itemIt.next();
					JMenuItem menuItem = new JMenuItem();

					menuItem.setText(currItem.getDescription());
					menuToolsAnimationGroup.add(menuItem);
					menuItem.addActionListener(new ToolsAnimationHandler(currItem));
				}
			}
		}

		// Tools.AutomataEditor
		if (SupremicaProperties.includeEditor())
		{
			menuTools.add(new JSeparator());

			JMenuItem menuToolsAutomataEditor = new JMenuItem();

			menuToolsAutomataEditor.setText("Editor...");
			menuTools.add(menuToolsAutomataEditor);
			menuToolsAutomataEditor.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					supremica.toolsAutomataEditor();
				}
			});
		}

		// Tools.ShoeFactory
		if (SupremicaProperties.includeShoeFactory())
		{
			menuTools.add(new JSeparator());

			JMenu menuToolsShoeFactory = new JMenu();

			menuToolsShoeFactory.setText("Shoe Factory...");
			menuTools.add(menuToolsShoeFactory);

			JMenuItem menuBuildConfigit = new JMenuItem("Shoeconfigurator");
			JMenuItem menuBuildPlant = new JMenuItem("Build Plant");
			JMenuItem menuBuildSFC = new JMenuItem("Build SFC");
			JMenuItem menuBuildAnimator = new JMenuItem("Animator");
			menuBuildPlant.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.shoeFactoryBuildPlant(ActionMan.getGui());
				}
			});

			menuBuildConfigit.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.shoeFactoryConfigurator();
				}
			});
			menuBuildSFC.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.shoeFactorySFC();
				}
			});
			menuBuildAnimator.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.shoeFactoryAnimator(ActionMan.getGui());
				}
			});
			menuToolsShoeFactory.add(menuBuildConfigit);
			menuToolsShoeFactory.add(menuBuildPlant);
			menuToolsShoeFactory.add(menuBuildSFC);
			menuToolsShoeFactory.add(menuBuildAnimator);
		}


		// Tools.JGrafchart
		if (SupremicaProperties.includeJGrafchart())
		{
			menuTools.add(new JSeparator());

			JMenu menuToolsJGrafchart = new JMenu();

			menuToolsJGrafchart.setText("JGrafchart...");
			menuTools.add(menuToolsJGrafchart);

			JMenuItem menuOpenEditor= new JMenuItem("Open Editor");

			menuOpenEditor.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.openJGrafchartEditor(ActionMan.getGui());
				}
			});

			menuToolsJGrafchart.add(menuOpenEditor);
		}

		menuTools.add(new JSeparator());

		JMenuItem menuToolsCellEditor = new JMenuItem();

		menuToolsCellEditor.setText("Cell Editor...");
		menuTools.add(menuToolsCellEditor);
		menuToolsCellEditor.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.startCellEditor(ActionMan.getGui());
			}
		});

		JMenuItem menuToolsRecipeEditor = new JMenuItem();

		menuToolsRecipeEditor.setText("Recipe Editor...");
		menuTools.add(menuToolsRecipeEditor);
		menuToolsRecipeEditor.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.startRecipeEditor(ActionMan.getGui());
			}
		});

		menuTools.add(new JSeparator());

		// Tools.CodeGeneration
		JMenu menuToolsCodeGeneration = new JMenu();

		menuToolsCodeGeneration.setText("Code Generation");
		menuTools.add(menuToolsCodeGeneration);

		JMenuItem menuToolsCodeGenerationIL = new JMenuItem();

		menuToolsCodeGenerationIL.setText("IEC-61131 Instruction List...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationIL);
		menuToolsCodeGenerationIL.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				ActionMan.ProjectTo1131IL(ActionMan.getGui());
			}
		});

		JMenuItem menuToolsCodeGeneration1131ST = new JMenuItem();

		menuToolsCodeGeneration1131ST.setText("IEC-61131 Structured Text...");
		menuToolsCodeGeneration.add(menuToolsCodeGeneration1131ST);
		menuToolsCodeGeneration1131ST.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.ProjectTo1131ST(ActionMan.getGui());
			}
		});

		JMenuItem menuToolsCodeGenerationControlBuilderIL = new JMenuItem();

		menuToolsCodeGenerationControlBuilderIL.setText("ABB Control Builder Instruction List...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationControlBuilderIL);
		menuToolsCodeGenerationControlBuilderIL.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				ActionMan.ProjectToControlBuilderIL(ActionMan.getGui());
			}
		});

		JMenuItem menuToolsCodeGenerationControlBuilderST = new JMenuItem();

		menuToolsCodeGenerationControlBuilderST.setText("ABB Control Builder Structured Text...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationControlBuilderST);
		menuToolsCodeGenerationControlBuilderST.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				ActionMan.ProjectToControlBuilderST(ActionMan.getGui());
			}
		});


		JMenuItem menuToolsCodeGenerationControlBuilderSFC = new JMenuItem();

		menuToolsCodeGenerationControlBuilderSFC.setText("ABB Control Builder Sequential Function Chart...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationControlBuilderSFC);
		menuToolsCodeGenerationControlBuilderSFC.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.AutomataToControlBuilderSFC(ActionMan.getGui());
			}
		});

		JMenuItem menuToolsCodeGenerationSattLineSFC = new JMenuItem();

		menuToolsCodeGenerationSattLineSFC.setText("ABB SattLine Sequential Function Chart...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationSattLineSFC);
		menuToolsCodeGenerationSattLineSFC.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.AutomataToSattLineSFC(ActionMan.getGui());
			}
		});

		JMenuItem menuToolsCodeGenerationBC = new JMenuItem();

		menuToolsCodeGenerationBC.setText("Java Bytecode...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationBC);
		menuToolsCodeGenerationBC.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.AutomataToJavaBytecode(ActionMan.getGui());
			}
		});

		JMenuItem menuToolsCodeGenerationC = new JMenuItem();

		menuToolsCodeGenerationC.setText("ANSI C...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationC);
		menuToolsCodeGenerationC.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				ActionMan.AutomataToC(ActionMan.getGui());
			}
		});


		JMenuItem menuToolsCodeGenerationNQC = new JMenuItem();

		menuToolsCodeGenerationNQC.setText("Lego Mindstorm NQC...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationNQC);
		menuToolsCodeGenerationNQC.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				ActionMan.AutomataToMindstormNQC(ActionMan.getGui());
			}
		});

		JMenuItem menuToolsCodeGenerationSMV = new JMenuItem();

		menuToolsCodeGenerationSMV.setText("SMV...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationSMV);
		menuToolsCodeGenerationSMV.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				ActionMan.AutomataToSMV(ActionMan.getGui());
			}
		});

		// Tools.RunSimulation
		JMenuItem run_simulation = new JMenuItem();

		run_simulation.setText("Run Simulation...");
		menuTools.add(run_simulation);
		run_simulation.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			        ActionMan.RunSimulation(ActionMan.getGui());
			}
		});








/*
		// Tools.CodeGeneration
		JMenu menuToolsCodeGeneration = new JMenu();

		menuToolsCodeGeneration.setText("Code Generation");
		menuTools.add(menuToolsCodeGeneration);

		JMenuItem menuToolsCodeGenerationIL = new JMenuItem();

		menuToolsCodeGenerationIL.setText("IEC-61131 Instruction List...");
		menuToolsCodeGeneration.add(menuToolsCodeGenerationIL);
		menuToolsCodeGenerationIL.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				ActionMan.ProjectTo1131IL(ActionMan.getGui());
			}
		});

		JMenuItem menuToolsCodeGeneration1131ST = new JMenuItem();

		menuToolsCodeGeneration1131ST.setText("IEC-61131 Structured Text...");
		menuToolsCodeGeneration.add(menuToolsCodeGeneration1131ST);
		menuToolsCodeGeneration1131ST.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.ProjectTo1131ST(ActionMan.getGui());
			}
		});
*/

		// Tools.RobotStudio
		if (SupremicaProperties.showRobotstudioLink()&&SupremicaProperties.useActiveXBridge())
		{
			menuTools.add(new JSeparator());

			JMenu menuRobotStudioLink = new JMenu();
			menuRobotStudioLink.setText("RobotStudio");
			menuTools.add(menuRobotStudioLink);

/*
			// RobotStudioExtractAutomata
			JMenuItem robLink = new JMenuItem();
			robLink.setText("Extract Automata From Station...");
			menuRobotStudioLink.add(robLink);
			robLink.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.robotStudioLink(ActionMan.getGui());
					}
				});
*/

			// RobotStudioOpenStation
			JMenuItem robOpen = new JMenuItem();
			robOpen.setText("Open RobotStudio Station...");
			menuRobotStudioLink.add(robOpen);
			robOpen.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.robotStudioOpenStation(ActionMan.getGui());
					}
				});
			// RobotStudioCreateMutexZones
			JMenuItem robMutex = new JMenuItem();
			robMutex.setText("Create Mutex Zones");
			menuRobotStudioLink.add(robMutex);
			robMutex.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.robotStudioCreateMutexZones(ActionMan.getGui());
					}
				});
			// RobotStudioCreateMutexZones
			JMenuItem robSpanMutex = new JMenuItem();
			robSpanMutex.setText("Create Mutex Zones From Span");
			menuRobotStudioLink.add(robSpanMutex);
			robSpanMutex.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.robotStudioCreateMutexZonesFromSpan(ActionMan.getGui());
					}
				});
			// RobotStudioExtractAutomata
			JMenuItem robExtract = new JMenuItem();
			robExtract.setText("Extract Automata");
			menuRobotStudioLink.add(robExtract);
			robExtract.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.robotStudioExtractAutomata(ActionMan.getGui());
					}
				});
			// RobotStudioExecuteAutomaton
			JMenuItem robExec = new JMenuItem();
			robExec.setText("Execute Robot Automaton");
			menuRobotStudioLink.add(robExec);
			robExec.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.robotStudioExecuteRobot(ActionMan.getGui());
					}
				});
			// RobotStudioKill
			JMenuItem robKill = new JMenuItem();
			robKill.setText("Kill RobotStudio Link");
			menuRobotStudioLink.add(robKill);
			robKill.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.robotStudioKill();
					}
				});
			// RobotStudioTest
			JMenuItem robTest = new JMenuItem();
			robTest.setText("Run Tests");
			menuRobotStudioLink.add(robTest);
			robTest.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.robotStudioTest(ActionMan.getGui());
					}
				});
		}

		// Tools.CoordinationABB
		if(SupremicaProperties.showCoordinationABB())
		{
			menuTools.add(new JSeparator());

			JMenu menuCoordinationABB = new JMenu();
			menuCoordinationABB.setText("CoordinationABB");
			menuTools.add(menuCoordinationABB);

			// OpenRobotStudioStation
			JMenuItem stationOpen = new JMenuItem();
			stationOpen.setText("Open Robot Studio Station...");
			menuCoordinationABB.add(stationOpen);
			stationOpen.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.robotStudioOpenStation(ActionMan.getGui());
						RobotStudioLink.configureCreateXml();
					}
				});

			// CreatePaths
			JMenuItem createPaths = new JMenuItem();
			createPaths.setText("Create paths in Robot Studio");
			menuCoordinationABB.add(createPaths);
			createPaths.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.createPathsInRS(ActionMan.getGui());
					}
				});
			// CreateSpans
			JMenuItem createSpans = new JMenuItem();
			createSpans.setText("Simulations to create Spans");
			menuCoordinationABB.add(createSpans);
			createSpans.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.createSpansInRS(ActionMan.getGui());
				}
			});
			// CreateMutexZones
			JMenuItem createMutexZones = new JMenuItem();
			createMutexZones.setText("Intersect Spans -> Mutex Zones");
			menuCoordinationABB.add(createMutexZones);
			createMutexZones.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.createMutexZonesInRS(ActionMan.getGui());
				}
			});
			// AddViaPoints
			JMenuItem addViaPoints = new JMenuItem();
			addViaPoints.setText("Simulations to add via-points");
			menuCoordinationABB.add(addViaPoints);
			addViaPoints.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.addViaPointsInRS(ActionMan.getGui());
				}
			});
			// BuildXmlFile
			JMenuItem buildXmlFile = new JMenuItem();
			buildXmlFile.setText("Build xml file");
			menuCoordinationABB.add(buildXmlFile);
			buildXmlFile.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ActionMan.buildXmlFile(ActionMan.getGui());
				}
			});
			// ExecuteScheduledAutomaton
			JMenuItem scheduledExecute = new JMenuItem();
			scheduledExecute.setText("Execute Optimal Coordination");
			menuCoordinationABB.add(scheduledExecute);
			scheduledExecute.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.executeScheduledAutomaton(ActionMan.getGui());
					}
				});

			JMenuItem demo = new JMenuItem();
			demo.setText("Optimal coordination DEMO");
			menuCoordinationABB.add(demo);
			demo.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.demonstrate(ActionMan.getGui());
					}
				});

		}


		// Tools.Evolution
		if (SupremicaProperties.showGeneticAlgorithms())
		{
			menuTools.add(new JSeparator());

			JMenu menuEvoComp = new JMenu();
			menuEvoComp.setText("Evolution");
			menuTools.add(menuEvoComp);

			// EvoComp.CalculateSynchTable
			JMenuItem synchTable = new JMenuItem();
			synchTable.setText("Calculate Synchtable");
			menuEvoComp.add(synchTable);
			synchTable.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.evoCompSynchTable(ActionMan.getGui(), false);
					}
				});

			// EvoComp.PredictSize
			JMenuItem predictSize = new JMenuItem();
			predictSize.setText("Predict Synchronization Size");
			menuEvoComp.add(predictSize);
			predictSize.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						ActionMan.evoCompPredictSize(ActionMan.getGui());
					}
				});
		}

		// Configure
		JMenu menuConfigure = new JMenu();

		menuConfigure.setText("Configure");
		menuConfigure.setMnemonic(KeyEvent.VK_C);
		add(menuConfigure);

		// Configure.Preferences
		JMenuItem menuConfigurePreferences = new JMenuItem();

		menuConfigurePreferences.setText("Preferences...");
		menuConfigure.add(menuConfigurePreferences);
		menuConfigurePreferences.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ActionMan.configurePreferences_actionPerformed(ActionMan.getGui());
			}
		});

		// Help
		JMenu menuHelp = new JMenu();

		menuHelp.setText("Help");
		menuHelp.setMnemonic(KeyEvent.VK_H);
		add(menuHelp);

		// Help.Help Topics
		JMenuItem supremicaOnTheWeb = new JMenuItem("Supremica on the Web");
		menuHelp.add(supremicaOnTheWeb);
		supremicaOnTheWeb.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				BrowserControl.displayURL("http://www.supremica.org");
			}
		});
		JMenuItem supremicaDocumentation = new JMenuItem("Documentation");
		menuHelp.add(supremicaDocumentation);
		supremicaDocumentation.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				BrowserControl.displayURL("http://www.supremica.org/documentation");
			}
		});
		menuHelp.addSeparator();

		/* Help.Help Topics
		JMenuItem menuHelpTopics = new JMenuItem("Supervisory Control");
		menuHelpTopics.addActionListener(helpDisplayer);
		menuHelp.add(menuHelpTopics);
		*/
		JMenuItem menuHelpTopics = new SupremicaMenuItem(ActionMan.helpAction);
		menuHelp.add(menuHelpTopics);

		menuHelp.addSeparator();

		// Help.About
		JMenuItem menuHelpAbout = new JMenuItem();

		menuHelpAbout.setText("About...");
		menuHelp.add(menuHelpAbout);
		menuHelpAbout.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				supremica.helpAbout();
			}
		});
	}


}
