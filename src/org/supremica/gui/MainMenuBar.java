//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.help.*;
import org.supremica.gui.help.ContentHelp;
import org.supremica.properties.Config;
import org.supremica.automata.templates.TemplateItem;
import org.supremica.automata.templates.TemplateGroup;
import org.supremica.gui.animators.scenebeans.AnimationItem;
import org.supremica.gui.animators.scenebeans.AnimationGroup;
import org.supremica.util.BrowserControl;
import org.supremica.util.SupremicaMenuItem;

public class MainMenuBar
    extends JMenuBar
{
    private static final long serialVersionUID = 1L;
    private Supremica supremica;
    private ContentHelp help = null;

    @SuppressWarnings("unused")
	private CSH.DisplayHelpFromSource helpDisplayer = null;

    public MainMenuBar(Supremica supremica)    // should get rid of supremica here
    {
        this.supremica = supremica;
        this.help = new ContentHelp();
        this.helpDisplayer = new CSH.DisplayHelpFromSource(help.getStandardHelpBroker());

        initMenubar();
    }

    private void initMenubar()    // This is copied (almost) straight from Supremica.java
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

                /*
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
                 */

        // File.NewFromTemplate
        JMenu menuFileNewFromTemplate = new JMenu();

        menuFileNewFromTemplate.setText("New From Template");
        menuFile.add(menuFileNewFromTemplate);

        ExampleTemplates exTempl = ExampleTemplates.getInstance();
        for (Iterator<TemplateGroup> groupIt = exTempl.iterator(); groupIt.hasNext(); )
        {
            TemplateGroup currGroup = (TemplateGroup) groupIt.next();
            JMenu menuFileNewFromTemplateGroup = new JMenu();

            menuFileNewFromTemplateGroup.setText(currGroup.getName());
            menuFileNewFromTemplate.add(menuFileNewFromTemplateGroup);

            for (Iterator<TemplateItem> itemIt = currGroup.iterator(); itemIt.hasNext(); )
            {
                TemplateItem currItem = (TemplateItem) itemIt.next();
                JMenuItem menuItem = new JMenuItem();

                menuItem.setText(currItem.getName());
                menuFileNewFromTemplateGroup.add(menuItem);
                menuItem.addActionListener(new NewFromTemplateHandler(currItem));
            }
        }

        if (Config.FILE_ALLOW_OPEN.isTrue())
        {
            // File.Open
            JMenuItem menuFileOpen = new SupremicaMenuItem(ActionMan.openAction);
            menuFile.add(menuFileOpen);

            separatorNeeded = true;
        }

        if (Config.FILE_ALLOW_SAVE.isTrue())
        {
            // File.Save
            JMenuItem menuFileSave = new SupremicaMenuItem(ActionMan.saveAction);
            menuFile.add(menuFileSave);

            // File.SaveAs
            JMenuItem menuFileSaveAs = new SupremicaMenuItem(ActionMan.saveAsAction);
            menuFile.add(menuFileSaveAs);

            separatorNeeded = true;
        }

        if (separatorNeeded)
        {
            menuFile.addSeparator();

            separatorNeeded = false;
        }

        if (Config.FILE_ALLOW_IMPORT.isTrue())
        {
            // File.Import
            JMenu menuFileImport = new JMenu("Import");
            menuFileImport.setMnemonic(KeyEvent.VK_I);
            menuFileImport.setToolTipText("Import file");
            menuFile.add(menuFileImport);

            // File.Import.Waters
            JMenuItem menuFileImportWaters = new JMenuItem("From Waters...");
            menuFileImport.add(menuFileImportWaters);
            menuFileImportWaters.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.fileImportWaters(ActionMan.getGui());
                }
            });
            // File.Import.UMDES
            JMenuItem menuFileImportUMDES = new JMenuItem("From UMDES...");
            menuFileImport.add(menuFileImportUMDES);
            menuFileImportUMDES.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.fileImportUMDES(ActionMan.getGui());
                }
            });

            /*
                        // File.Import.HYB (the file format of Balemi's tool)
                        JMenuItem menuFileImportHYB = new JMenuItem("From HYB...");
                        menuFileImport.add(menuFileImportHYB);
                        menuFileImportHYB.addActionListener(new ActionListener()
                        {
                        public void actionPerformed(ActionEvent e)
                        {
                        ActionMan.fileImportHYB(ActionMan.getGui());
                        }
                        });
             */

            // File.Import.HISC
            JMenuItem menuFileImportHISC = new JMenuItem("From HISC...");
            menuFileImport.add(menuFileImportHISC);
            menuFileImportHISC.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.fileImportHISC(ActionMan.getGui());
                }
            });

                        /*
                        //File.Import.RobotCoordination
                        JMenuItem menuFileImportRobotCoordination = new JMenuItem("From Robot Coordinator...");
                        menuFileImport.add(menuFileImportRobotCoordination);
                        menuFileImportRobotCoordination.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                        ActionMan.fileImportRobotCoordination(ActionMan.getGui());
                        }
                    });
                         */
        }

                /*
                // if (SupremicaProperties.generalUseRobotCoordinationABB())
                if (SupremicaProperties.generalUseRobotCoordination())
                {
                // File.Import.RobotCoordinationABB
                JMenuItem menuFileImportRobotCoordinationABB = new JMenuItem("From Robot Coordinator ABB...");
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
                 */

        if (false && Config.FILE_ALLOW_EXPORT.isTrue())
        {
            // File.Export
            JMenu menuFileExport = new JMenu("Export");
            menuFile.add(menuFileExport);

            // File.Export.Html
            JMenuItem menuFileExportHtml = new JMenuItem("To Html...");
            menuFileExport.add(menuFileExportHtml);
            menuFileExportHtml.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.fileExportHtml(ActionMan.getGui());
                }
            });

            // File.Export.Desco
            JMenuItem menuFileExportDesco = new JMenuItem("To Desco...");
            menuFileExport.add(menuFileExportDesco);
            menuFileExportDesco.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.fileExportDesco(ActionMan.getGui());
                }
            });

            // File.Export.UMDES
            JMenuItem menuFileExportUMDES = new JMenuItem("To UMDES...");
            menuFileExport.add(menuFileExportUMDES);
            menuFileExportUMDES.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.fileExportUMDES(ActionMan.getGui());
                }
            });
        }

        if (separatorNeeded)
        {
            menuFile.addSeparator();

            separatorNeeded = false;
        }

        if (separatorNeeded)
        {
            menuFile.addSeparator();

            separatorNeeded = false;
        }

        if (Config.FILE_ALLOW_QUIT.isTrue())
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

        // Project.Comment
        JMenuItem menuProjectComment = new JMenuItem();

        menuProjectComment.setText("Comment...");
        menuProject.add(menuProjectComment);
        menuProjectComment.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                supremica.commentProject();
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

        if (Config.INCLUDE_USERINTERFACE.isTrue())
        {

            // Project.UserInterface
            JMenuItem menuProjectUserInterface = new JMenuItem();

            menuProjectUserInterface.setText("User Interface...");
            menuProject.add(menuProjectUserInterface);
            menuProjectUserInterface.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.userInterface_actionPerformed(ActionMan.getGui());
                }
            });

            // Project.UserInterface
            JMenuItem menuProjectGenerateUserInterfaceAutomata = new JMenuItem();

            menuProjectGenerateUserInterfaceAutomata.setText("Generate User Interface Automata");
            menuProject.add(menuProjectGenerateUserInterfaceAutomata);
            menuProjectGenerateUserInterfaceAutomata.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.generateUserInterfaceAutomata_actionPerformed(ActionMan.getGui());
                }
            });

            menuProject.addSeparator();
        }

        if (Config.INCLUDE_ANIMATOR.isTrue())
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
                    // Do nothing?
                }
            }
        });

        // Tools.Animations
        if (Config.INCLUDE_ANIMATOR.isTrue())
        {
            JMenu menuToolsAnimations = new JMenu();

            menuToolsAnimations.setText("Animations");
            menuTools.add(menuToolsAnimations);

            ExampleAnimations exAnim = ExampleAnimations.getInstance();

            for (Iterator<AnimationGroup> groupIt = exAnim.iterator(); groupIt.hasNext(); )
            {
                AnimationGroup currGroup = (AnimationGroup) groupIt.next();
                JMenu menuToolsAnimationGroup = new JMenu();

                menuToolsAnimationGroup.setText(currGroup.getDescription());
                menuToolsAnimations.add(menuToolsAnimationGroup);

                for (Iterator<AnimationItem> itemIt = currGroup.iterator(); itemIt.hasNext(); )
                {
                    AnimationItem currItem = (AnimationItem) itemIt.next();
                    JMenuItem menuItem = new JMenuItem();

                    menuItem.setText(currItem.getDescription());
                    menuToolsAnimationGroup.add(menuItem);
                    menuItem.addActionListener(new ToolsAnimationHandler(currItem));
                }
            }
        }


        // Tools.ShoeFactory
        if (Config.INCLUDE_SHOE_FACTORY.isTrue())
        {
            menuTools.add(new JSeparator());

            JMenu menuToolsShoeFactory = new JMenu();

            menuToolsShoeFactory.setText("Shoe Factory...");
            menuTools.add(menuToolsShoeFactory);

            JMenuItem menuBuildConfigit = new JMenuItem("Shoeconfigurator");
            JMenuItem menuBuildPlant = new JMenuItem("Build Plant");
            JMenuItem menuBuildConfigitDEMO = new JMenuItem("ShoeconfiguratorDEMO");

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
            menuBuildConfigitDEMO.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.shoeFactoryConfiguratorDEMO();
                }
            });
            menuToolsShoeFactory.add(menuBuildConfigit);
            menuToolsShoeFactory.add(menuBuildConfigitDEMO);
            menuToolsShoeFactory.add(menuBuildPlant);
        }

        // Tools.JGrafchart
        if (Config.INCLUDE_JGRAFCHART.isTrue())
        {
            menuTools.add(new JSeparator());

            JMenu menuToolsJGrafchart = new JMenu();

            menuToolsJGrafchart.setText("JGrafchart");
            menuTools.add(menuToolsJGrafchart);

            JMenuItem menuToolsOpenJGrafchart = new SupremicaMenuItem(ActionMan.openJGrafchartAction);

            menuToolsJGrafchart.add(menuToolsOpenJGrafchart);

            JMenuItem menuToolsUpdateFromJGrafchart = new SupremicaMenuItem(ActionMan.updateFromJGrafchartAction);

            menuToolsJGrafchart.add(menuToolsUpdateFromJGrafchart);

                        /*
                          JMenuItem menuOpenEditor= new JMenuItem("Open Editor");

                          menuOpenEditor.addActionListener(new ActionListener()
                          {
                          public void actionPerformed(ActionEvent e)
                          {
                          ActionMan.openJGrafchartEditor(ActionMan.getGui());
                          }
                          });

                          JMenuItem menuUpdateFromJGrafchart = new JMenuItem("Update Automata");
                          menuUpdateFromJGrafchart.addActionListener(new ActionListener()
                          {
                          public void actionPerformed(ActionEvent e)
                          {
                          ActionMan.updateFromJGrafchart(ActionMan.getGui());
                          }
                          });

                          menuToolsJGrafchart.add(menuUpdateFromJGrafchart);
                         */
        }


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

        JMenuItem menuToolsCodeGenerationIEC61499 = new JMenuItem();

        menuToolsCodeGenerationIEC61499.setText("IEC-61499 Function Blocks...");
        menuToolsCodeGeneration.add(menuToolsCodeGenerationIEC61499);
        menuToolsCodeGenerationIEC61499.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ActionMan.ProjectToIEC61499(ActionMan.getGui());
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

        JMenuItem menuToolsCodeGenerationSattLineSFCForBallProcess = new JMenuItem();

        menuToolsCodeGenerationSattLineSFCForBallProcess.setText("ABB SattLine SFC for Ball Process...");
        menuToolsCodeGeneration.add(menuToolsCodeGenerationSattLineSFCForBallProcess);
        menuToolsCodeGenerationSattLineSFCForBallProcess.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ActionMan.AutomataToSattLineSFCForBallProcess(ActionMan.getGui());
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

        JMenuItem menuToolsCodeGenerationJava = new JMenuItem();

        menuToolsCodeGenerationJava.setText("Java...");
        menuToolsCodeGeneration.add(menuToolsCodeGenerationJava);
        menuToolsCodeGenerationJava.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ActionMan.AutomataToJava(ActionMan.getGui());
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

        // Tools.SoftPLC
        if (Config.INCLUDE_SOFTPLC.isTrue())
        {

            // Tools.RunSimulation
            JMenuItem run_simulation = new JMenuItem();

            run_simulation.setText("Run SoftPLC Simulation...");
            menuTools.add(run_simulation);
            run_simulation.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.runSoftPLCSimulation(ActionMan.getGui());
                }
            });
        }

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

                /*
                // Tools.CellExaminer
                if (SupremicaProperties.generalUseRobotCoordination())
                {
            menuTools.add(new JSeparator());

            JMenuItem robotCellExamine = new JMenuItem();

            robotCellExamine.setText("Robot Coordination...");
            menuTools.add(robotCellExamine);
            robotCellExamine.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                {
                ActionMan.showCellExaminer(ActionMan.getGui());
                }
                });
                }
                 */

        // Tools.Evolution
        if (false)
        {
            menuTools.add(new JSeparator());

            JMenu menuEvoComp = new JMenu();

            menuEvoComp.setText("Evolution");

            //menuEvoComp.setEnabled(false);
            menuTools.add(menuEvoComp);

            // EvoComp.CalculateSynchTable
            JMenuItem synchTable = new JMenuItem();

            synchTable.setText("Calculate Synchtable");
            menuEvoComp.add(synchTable);
            synchTable.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ActionMan.evoCompSynchTable(false);
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
                    ActionMan.evoCompPredictSize();
                }
            });
        }

                /*
                // Tools.TrainSimulator
                menuTools.add(new JSeparator());

                JMenuItem trainSimulator = new JMenuItem();
                trainSimulator.setText("Train Simulator");
                menuTools.add(trainSimulator);
                trainSimulator.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                {
                ActionMan.trainSimulator(ActionMan.getGui());
                }
                });
                 */

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
