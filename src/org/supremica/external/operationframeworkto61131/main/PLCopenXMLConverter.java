package org.supremica.external.operationframeworkto61131.main;

/**
 * @author LC
 *
 */
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JTextArea;

import org.supremica.external.operationframeworkto61131.builder.ConfigurationBuilder;
import org.supremica.external.operationframeworkto61131.builder.POUBuilder;
import org.supremica.external.operationframeworkto61131.builder.ProjectInformationBuilder;
import org.supremica.external.operationframeworkto61131.builder.VariableListBuilder;
import org.supremica.external.operationframeworkto61131.controlinfo.EquipmentStateLookUp;
import org.supremica.external.operationframeworkto61131.util.JAXButil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;




public class PLCopenXMLConverter {

	private LogUtil log = LogUtil.getInstance();

	private List<Object> listOfEOP;

	private List<Object> listOfCOP;

	private List<Object> listOfInterlock;

	private org.supremica.manufacturingtables.xsd.fid.FunctionBlocks functionBlocks;

	private org.supremica.manufacturingtables.xsd.virtualResourcesV3.VirtualResources virtualResource;

	private org.supremica.manufacturingtables.xsd.cc.CycleStartConditions cycleStartConditions;

	private org.plcopen.xml.tc6.Project plcopenProject;

	private org.plcopen.xml.tc6.Project reusePlcopenProject;

	private String inputFileDir;

	private String outputDirectory;

	private String manualInputFile;

	private Boolean isInitialized = Constant.initialize("./", "config.xml");

	public PLCopenXMLConverter() {

	}

	public void start(String _inputFileDir, String _outputDirectory,
			String manualInputFile, JCheckBox[] infoChoices, JTextArea textArea) {
		
		// Default logger outputs to System.out
		if (textArea != null) {

			LogUtil
					.setLogger(new org.supremica.external.operationframeworkto61131.util.log.TextAreaLogger(
							textArea));
		}

		if (!isInitialized) {
			
			
			log.error("Can not find config file");

			return;
		}

		if (_inputFileDir != null && !_inputFileDir.isEmpty()) {

			this.inputFileDir = _inputFileDir;

			Constant.XML_FILE_PATH = this.inputFileDir;
		} else {
			this.inputFileDir = Constant.XML_FILE_PATH;
		}

		if (_outputDirectory != null && !_outputDirectory.isEmpty()) {

			this.outputDirectory = _outputDirectory;
		} else {

			this.outputDirectory = Constant.PLCOPEN_OUT_PUT_FILE_PATH;
		}

		if (manualInputFile != null && !manualInputFile.isEmpty()) {

			this.manualInputFile = manualInputFile;
		}



		// Create the PLCopen xml schema object tree.
		JAXButil JC = JAXButil.getInstance(Constant.PLC_OPEN_TC6);

		if (this.manualInputFile != null && !this.manualInputFile.isEmpty()) {

			reusePlcopenProject = (org.plcopen.xml.tc6.Project) JC
					.getRootElementObject(manualInputFile);
		}

		plcopenProject = (org.plcopen.xml.tc6.Project) JC.getRootElementObject(
				this.inputFileDir, Constant.PLCOPEN_IN_PUT_FILE);

		listOfCOP = JAXButil.getInstance(Constant.COP_XML_CONTEXT)
				.loadXMLFromPath(this.inputFileDir, Constant.COP_PREFIX);

		listOfEOP = JAXButil.getInstance(Constant.EOP_XML_CONTEXT)
				.loadXMLFromPath(this.inputFileDir, Constant.EOP_PREFIX);

		functionBlocks = (org.supremica.manufacturingtables.xsd.fid.FunctionBlocks) JAXButil
				.getInstance(Constant.FID_XML_CONTEXT).getRootElementObject(
						this.inputFileDir, Constant.FID_XML_FILE_NAME);

		virtualResource = (org.supremica.manufacturingtables.xsd.virtualResourcesV3.VirtualResources) JAXButil
				.getInstance(Constant.VIRTUAL_RESOURCES_XML_CONTEXT)
				.getRootElementObject(this.inputFileDir,
						Constant.VIRTUAL_RESOURCES_XML_FILE_NAME);

		listOfInterlock = JAXButil.getInstance(Constant.INTERLOCK_XML_CONTEXT)
				.loadXMLFromPath(this.inputFileDir, Constant.INTERLOCK_PREFIX);

		cycleStartConditions = (org.supremica.manufacturingtables.xsd.cc.CycleStartConditions) JAXButil
				.getInstance(Constant.CC_XML_CONTEXT).getRootElementObject(
						this.inputFileDir, Constant.CC_XML_FILE_NAME);

		EquipmentStateLookUp equipmentStateLookUp = null;

		try {

			equipmentStateLookUp = (EquipmentStateLookUp) Class.forName(
					Constant.EQUIPMENT_STATE_LOOK_UP_IMPLEMENT).newInstance();
			//			 
		} catch (Exception e) {

			log
					.error("Can not build equipment state look up implement from class:"
							+ Constant.EQUIPMENT_STATE_LOOK_UP_IMPLEMENT);

			return;

		}

		POUBuilder pouBuilder = new POUBuilder(plcopenProject);

		// Convert EOPs, variable names are obtained from class
		// EquipmentStateLookUp
		pouBuilder.convertEOPsToSFC(listOfEOP, equipmentStateLookUp);

		// Convert COPs, variable names are obtained from class
		// EquipmentStateLookUp
		pouBuilder.convertCOPsToSFC(listOfCOP, equipmentStateLookUp);

		// Add functionblocks
		pouBuilder.addFunctionBlocksInstantiation(functionBlocks,
				equipmentStateLookUp, listOfCOP, listOfEOP, virtualResource,
				reusePlcopenProject);

		// Add Interlock
		pouBuilder.convertILToLD(listOfInterlock, equipmentStateLookUp);

		// Add CC
		pouBuilder.generateCC(listOfEOP, listOfCOP, cycleStartConditions);

		String configurationName = Constant.CONFIGURATION_NAME;
		String resourceName = Constant.RESOURCE_NAME;

		// new ProjectInformationBuilder().buildDescription(plcopenProject);
		// Declare globalVar
		org.supremica.external.operationframeworkto61131.data.VarList interfaceVarList = pouBuilder
				.getAllPouInterfaceVarList();
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(
				plcopenProject);
		configurationBuilder.declareGlobalVariableInResource(configurationName,
				resourceName, interfaceVarList);

		// Generate variable list;

		VariableListBuilder variableListBuilder = new VariableListBuilder();
		variableListBuilder.buildVariableList(pouBuilder
				.getIntelligentMachineExternalHashMap(), interfaceVarList,
				this.outputDirectory, infoChoices);

		// add pou name to task

		configurationBuilder.addPouNamesToTask(configurationName, resourceName,
				pouBuilder.getPouNameList());

		// add project file header information
		// FIXME only date and time is added until now. Add other info or add by
		// editor externally?
		ProjectInformationBuilder.buildDescription(plcopenProject);

		// export the plcopen project to xml file here
		JC.exportToXMLFile(this.outputDirectory, Constant.PLCOPEN_OUT_PUT_FILE);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new PLCopenXMLConverter().start("", "", "", null, null);

	}
}
