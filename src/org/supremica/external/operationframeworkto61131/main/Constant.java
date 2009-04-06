package org.supremica.external.operationframeworkto61131.main;
/**
 * @author LC
 *
 */
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;

import org.supremica.external.operationframeworkto61131.util.FileUtil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;



public class Constant {

	private static LogUtil log = LogUtil.getInstance();

	public static String CELL_NAME;

	public static Boolean FB_REUSE = false;

	/*
	 * XML file binding context setting
	 */
	public static String EQUIPMENT_STATE_LOOK_UP_IMPLEMENT;

	public static String CONFIGURATION_NAME;

	public static String RESOURCE_NAME;

	/*
	 * XML file binding context setting
	 */
	public static String COP_XML_CONTEXT;

	public static String EOP_XML_CONTEXT;

	public static String PLC_OPEN_TC6;

	public static String VIRTUAL_RESOURCES_XML_CONTEXT;

	public static String PHYSICALRESOURCE_XML_CONTEXT;

	public static String FID_XML_CONTEXT;

	public static String CONTROL_INFO_XML_CONTEXT;

	public static String INTERLOCK_XML_CONTEXT;

	public static String CC_XML_CONTEXT;

	/*
	 * Input and output XML file name and path
	 */
	public static String XML_FILE_PATH;

	public static String VIRTUAL_RESOURCES_XML_FILE_NAME;

	public static String PHYSICALRESOURCE_XML_FILE_NAME;

	public static String CONTROL_INFO_XML_FILE_NAME;

	public static String FID_XML_FILE_NAME;

	public static String CC_XML_FILE_NAME;

	public static String PLCOPEN_IN_PUT_FILE;

	public static String PLCOPEN_OUT_PUT_FILE;

	public static String PLCOPEN_VARLIST_OUT_PUT_FILE;

	public static String PLCOPEN_OUT_PUT_FILE_PATH;

	/*
	 * Keywords prefix
	 */
	public static String COP_PREFIX;

	public static String EOP_PREFIX;

	public static String INTERLOCK_PREFIX;

	public static String FACTORY;

	public static String VIRTUAL_RESOURCES;

	public static String STATE_IGNORE_SIGN;

	public static String STATE_SKIP_SIGN;

	public static String STATE_INITIAL_SIGN;

	public static String SFC_VAR_NAME_PREFIX_OP;

	public static String SFC_VAR_NAME_POSTFIX_START;

	public static String SFC_VAR_NAME_POSTFIX_END;

	public static String ZONE_STATE_BOOK;

	public static String ZONE_STATE_UNBOOK;

	public static String ERROR_VARIABLE_NAME;

	/*
	 * SFC graphical setting, will be referred in convertor.sfc.SFCBuilder.java
	 */
	public static BigDecimal TransitionHeight;

	public static BigDecimal TransitionWidth;

	public static BigDecimal InVariableHeight;

	public static BigDecimal InVariableWidthUnit;

	public static BigDecimal BlockANDHeightUnit;

	public static BigDecimal BlockANDWidth;

	public static BigDecimal StepHeight;

	public static BigDecimal StepWidth;

	public static BigDecimal ActionBlockHeight;

	public static BigDecimal ActionBlockWidthExtendUnit;

	public static BigDecimal DistanceStepToTransition;

	public static BigDecimal DistanceConditionToTransition;

	public static BigDecimal DistanceActionBlockToStep;

	public static BigDecimal DistanceInVariableToBlock;

	public static BigDecimal DistanceStepToJumpStep;

	public static BigDecimal DistanceInVariableToFunctionBlock;

	public static BigDecimal StepToTransitionExtendUnit;

	public static BigDecimal InitStepPositionX;

	public static BigDecimal InitStepPositionY;

	public static BigDecimal JumpStepHeight;

	public static BigDecimal JumpStepWidth;

	public static BigDecimal FunctionBlockPositionX;

	public static BigDecimal FunctionBlockPositionY;

	public static String InitStepName;

	public static String STEP_PREFIX;

	public static String QUALIFIER_S;

	public static String QUALIFIER_N;

	public static BigDecimal FunctionBlockANDDistance;

	private Constant() {
	}

	public static Boolean initialize(String path, String configFileName) {
		Properties prop = new Properties();
		FileInputStream fis = null;
		try {

			File configFile = null;

			if (path != null && path.trim().length() > 0) {

				configFile = new File(path, configFileName);

			} else {

				configFile = new File(configFileName);

			}

			if (!FileUtil.isValid(configFile, FileUtil.fixPathEndSign(path)
					+ configFileName)) {

				return false;
			}

			fis = new FileInputStream(configFile);

			prop.loadFromXML(fis);
			fis.close();
		} catch (Exception e) {

			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				log.error("Failed to close Config xml file:" + configFileName+". Exception:"+e.getMessage());
			}

			log.error("Can not initialize Config xml file:" + configFileName+". Exception:"+e.getMessage());
			
		}

		if (prop.getProperty("FB_REUSE") != null
				&& prop.getProperty("FB_REUSE").equalsIgnoreCase("TRUE")) {

			FB_REUSE = true;
		}

		CELL_NAME = prop.getProperty("CELL_NAME");
		/*
		 * XML file binding context setting
		 */
		EQUIPMENT_STATE_LOOK_UP_IMPLEMENT = prop
				.getProperty("EQUIPMENT_STATE_LOOK_UP_IMPLEMENT");

		CONFIGURATION_NAME = prop.getProperty("CONFIGURATION_NAME");

		RESOURCE_NAME = prop.getProperty("RESOURCE_NAME");

		/*
		 * XML file binding context setting
		 */
		COP_XML_CONTEXT = prop.getProperty("COP_XML_CONTEXT");

		EOP_XML_CONTEXT = prop.getProperty("EOP_XML_CONTEXT");

		PLC_OPEN_TC6 = prop.getProperty("PLC_OPEN_TC6");

		VIRTUAL_RESOURCES_XML_CONTEXT = prop
				.getProperty("VIRTUAL_RESOURCES_XML_CONTEXT");

		PHYSICALRESOURCE_XML_CONTEXT = prop
				.getProperty("PHYSICALRESOURCE_XML_CONTEXT");

		FID_XML_CONTEXT = prop.getProperty("FID_XML_CONTEXT");

		CONTROL_INFO_XML_CONTEXT = prop.getProperty("CONTROL_INFO_XML_CONTEXT");

		INTERLOCK_XML_CONTEXT = prop.getProperty("INTERLOCK_XML_CONTEXT");

		INTERLOCK_PREFIX = prop.getProperty("INTERLOCK_PREFIX");

		CC_XML_CONTEXT = prop.getProperty("CC_XML_CONTEXT");

		CC_XML_FILE_NAME = prop.getProperty("CC_XML_FILE_NAME");

		/*
		 * Input and output XML file name and path
		 */
		XML_FILE_PATH = prop.getProperty("XML_FILE_PATH");

		VIRTUAL_RESOURCES_XML_FILE_NAME = prop
				.getProperty("VIRTUAL_RESOURCES_XML_FILE_NAME");

		PHYSICALRESOURCE_XML_FILE_NAME = prop
				.getProperty("PHYSICALRESOURCE_XML_FILE_NAME");

		CONTROL_INFO_XML_FILE_NAME = prop
				.getProperty("CONTROL_INFO_XML_FILE_NAME");

		FID_XML_FILE_NAME = prop.getProperty("FID_XML_FILE_NAME");

		PLCOPEN_IN_PUT_FILE = prop.getProperty("PLCOPEN_IN_PUT_FILE");

		PLCOPEN_OUT_PUT_FILE = prop.getProperty("PLCOPEN_OUT_PUT_FILE");

		PLCOPEN_VARLIST_OUT_PUT_FILE = prop
				.getProperty("PLCOPEN_VARLIST_OUT_PUT_FILE");

		PLCOPEN_OUT_PUT_FILE_PATH = prop
				.getProperty("PLCOPEN_OUT_PUT_FILE_PATH");

		/*
		 * Keywords prefix
		 */
		COP_PREFIX = prop.getProperty("COP_PREFIX");

		EOP_PREFIX = prop.getProperty("EOP_PREFIX");

		FACTORY = prop.getProperty("FACTORY");

		VIRTUAL_RESOURCES = prop.getProperty("VIRTUAL_RESOURCES");

		STATE_IGNORE_SIGN = prop.getProperty("STATE_IGNORE_SIGN");

		STATE_SKIP_SIGN = prop.getProperty("STATE_SKIP_SIGN");

		STATE_INITIAL_SIGN = prop.getProperty("STATE_INITIAL_SIGN");

		SFC_VAR_NAME_PREFIX_OP = prop.getProperty("SFC_VAR_NAME_PREFIX_OP");

		SFC_VAR_NAME_POSTFIX_START = prop
				.getProperty("SFC_VAR_NAME_POSTFIX_START");

		SFC_VAR_NAME_POSTFIX_END = prop.getProperty("SFC_VAR_NAME_POSTFIX_END");

		ZONE_STATE_BOOK = prop.getProperty("ZONE_STATE_BOOK");

		ZONE_STATE_UNBOOK = prop.getProperty("ZONE_STATE_UNBOOK");

		ERROR_VARIABLE_NAME = prop.getProperty("ERROR_VARIABLE_NAME");

		/*
		 * SFC graphical setting, will be referred in
		 * convertor.sfc.SFCBuilder.java
		 */
		TransitionHeight = new BigDecimal(prop.getProperty("TransitionHeight"));

		TransitionWidth = new BigDecimal(prop.getProperty("TransitionWidth"));

		InVariableHeight = new BigDecimal(prop.getProperty("InVariableHeight"));

		InVariableWidthUnit = new BigDecimal(prop
				.getProperty("InVariableWidthUnit"));

		BlockANDHeightUnit = new BigDecimal(prop
				.getProperty("BlockANDHeightUnit"));

		BlockANDWidth = new BigDecimal(prop.getProperty("BlockANDWidth"));

		StepHeight = new BigDecimal(prop.getProperty("StepHeight"));

		StepWidth = new BigDecimal(prop.getProperty("StepWidth"));

		ActionBlockHeight = new BigDecimal(prop
				.getProperty("ActionBlockHeight"));

		ActionBlockWidthExtendUnit = new BigDecimal(prop
				.getProperty("ActionBlockWidthExtendUnit"));

		DistanceStepToTransition = new BigDecimal(prop
				.getProperty("DistanceStepToTransition"));

		DistanceConditionToTransition = new BigDecimal(prop
				.getProperty("DistanceConditionToTransition"));

		DistanceActionBlockToStep = new BigDecimal(prop
				.getProperty("DistanceActionBlockToStep"));

		DistanceInVariableToBlock = new BigDecimal(prop
				.getProperty("DistanceInVariableToBlock"));

		DistanceStepToJumpStep = new BigDecimal(prop
				.getProperty("DistanceStepToJumpStep"));

		DistanceInVariableToFunctionBlock = new BigDecimal(prop
				.getProperty("DistanceInVariableToFunctionBlock"));

		StepToTransitionExtendUnit = new BigDecimal(prop
				.getProperty("StepToTransitionExtendUnit"));

		InitStepPositionX = new BigDecimal(prop
				.getProperty("InitStepPositionX"));

		InitStepPositionY = new BigDecimal(prop
				.getProperty("InitStepPositionY"));

		JumpStepHeight = new BigDecimal(prop.getProperty("JumpStepHeight"));

		JumpStepWidth = new BigDecimal(prop.getProperty("JumpStepWidth"));

		FunctionBlockPositionX = new BigDecimal(prop
				.getProperty("FunctionBlockPositionX"));

		FunctionBlockPositionY = new BigDecimal(prop
				.getProperty("FunctionBlockPositionY"));

		InitStepName = prop.getProperty("InitStepName");

		STEP_PREFIX = prop.getProperty("STEP_PREFIX");

		QUALIFIER_S = prop.getProperty("QUALIFIER_S");

		QUALIFIER_N = prop.getProperty("QUALIFIER_N");

		FunctionBlockANDDistance = new BigDecimal(prop
				.getProperty("FunctionBlockANDDistance"));

		
		return true;
		// prop.list(System.out);
	}

}
