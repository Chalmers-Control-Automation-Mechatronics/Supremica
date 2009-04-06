package org.supremica.external.operationframeworkto61131.builder.functionblock;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.FBConnection;
import org.supremica.external.operationframeworkto61131.data.FBSystemConnection;
import org.supremica.external.operationframeworkto61131.data.FBUserConnection;
import org.supremica.external.operationframeworkto61131.data.StateQuery;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.StringUtil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;
import org.supremica.manufacturingtables.xsd.fid.SystemConnection;
import org.supremica.manufacturingtables.xsd.fid.UserConnection;



/**
 * DummyMachineFB.java generates the list of Function Block variable-parameter
 * connection pairs for Machine without control system from FID.xml. Connection
 * pairs in the list will be used to generate the graphical connection in
 * FBTypeBuilder.java.
 * 
 * Created: Mar 31, 2009 5:15:35 PM
 * 
 * @author LC
 * @version 1.0
 */
public class DummyMachineFB {

	private static LogUtil log = LogUtil.getInstance();

	private final org.supremica.manufacturingtables.xsd.fid.IoType INPUT = org.supremica.manufacturingtables.xsd.fid.IoType.INPUT;

	private final org.supremica.manufacturingtables.xsd.fid.IoType OUTPUT = org.supremica.manufacturingtables.xsd.fid.IoType.OUTPUT;

	private final org.supremica.manufacturingtables.xsd.fid.FunctionType FEEDBACK_VAR = org.supremica.manufacturingtables.xsd.fid.FunctionType.FEEDBACK_VAR;

	private final org.supremica.manufacturingtables.xsd.fid.FunctionType REQUEST_VAR = org.supremica.manufacturingtables.xsd.fid.FunctionType.REQUEST_VAR;

	private final org.supremica.manufacturingtables.xsd.fid.FunctionType IO_VAR = org.supremica.manufacturingtables.xsd.fid.FunctionType.IO_VARIABLE;

	private final org.supremica.manufacturingtables.xsd.fid.FunctionType IO_ADDRESS = org.supremica.manufacturingtables.xsd.fid.FunctionType.IO_ADDRESS;

	private final org.supremica.manufacturingtables.xsd.fid.FunctionType INDICATOR = org.supremica.manufacturingtables.xsd.fid.FunctionType.INDICATOR;

	private final org.supremica.manufacturingtables.xsd.fid.FunctionType INTERLOCK = org.supremica.manufacturingtables.xsd.fid.FunctionType.INTERLOCK;

	private org.supremica.external.operationframeworkto61131.controlinfo.EquipmentStateLookUp equipmentStateLookUp;

	public DummyMachineFB(
			org.supremica.external.operationframeworkto61131.controlinfo.EquipmentStateLookUp equipmentStateLookUp) {

		this.equipmentStateLookUp = equipmentStateLookUp;
	}

	public List<org.supremica.external.operationframeworkto61131.data.FBConnection> getFBConnectionList(
			String machineName,
			Object equipmentEntityObj,
			org.supremica.manufacturingtables.xsd.fid.FunctionBlock functionBlock) {

		// FIXME need to change the equipmentEntity for general use.
		org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity equipmentEntity = (org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity) equipmentEntityObj;

		List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> sysConnectionList = getFBSystemConnectionList(
				machineName, equipmentEntity, functionBlock);

		// list system connection, for debugging only
		/*
		 * log.debug("=================System connections:");
		 * 
		 * for (converter.data.FBConnection sysFB : sysConnectionList) {
		 * 
		 * for (String param : sysFB.getParam()) {
		 * 
		 * log.debug("param: " + param);
		 * 
		 * }
		 * 
		 * for (String var : sysFB.getVariable()) {
		 * 
		 * log.debug("  Var: " + var); }
		 * 
		 * log.debug("    ");
		 * 
		 * }
		 */
		List<org.supremica.external.operationframeworkto61131.data.FBUserConnection> userConnectionList = getFBUserConnectionList(
				machineName, equipmentEntity, functionBlock);
		// list user connection, for debugging only
		/*
		 * log.debug("=================User connections:");
		 * 
		 * for (converter.data.FBUserConnection userFB : userConnectionList) {
		 * 
		 * log.debug("connection IOtype:" + userFB.getIOType());
		 * 
		 * for (String param : userFB.getParam()) {
		 * 
		 * log.debug("param: " + param);
		 * 
		 * }
		 * 
		 * for (String var : userFB.getVariable()) {
		 * 
		 * log.debug("  Var: " + var); }
		 * 
		 * log.debug("    ");
		 * 
		 * }
		 */
		List<FBConnection> fbConnectionList = new LinkedList<FBConnection>();

		if (sysConnectionList != null) {
			// System Interface here.
			for (org.supremica.external.operationframeworkto61131.data.FBSystemConnection systemConnection : sysConnectionList) {

				fbConnectionList.add(systemConnection);

			}

		}

		if (userConnectionList != null) {
			// User Interface here
			for (org.supremica.external.operationframeworkto61131.data.FBUserConnection userConnection : userConnectionList) {

				fbConnectionList.add(userConnection);

			}

		}

		return fbConnectionList;

	}

	// No process here, for further use.
	public List<org.supremica.external.operationframeworkto61131.data.FBUserConnection> getFBUserConnectionList(
			String machineName,
			org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity equipmentEntity,
			org.supremica.manufacturingtables.xsd.fid.FunctionBlock functionBlock) {

		List<org.supremica.external.operationframeworkto61131.data.FBUserConnection> fbUserConnectionList = new LinkedList<org.supremica.external.operationframeworkto61131.data.FBUserConnection>();

		if (functionBlock.getUserInterface() != null
				&& functionBlock.getUserInterface().getUserConnection() != null) {

			for (UserConnection userConnection : functionBlock
					.getUserInterface().getUserConnection()) {

				org.supremica.external.operationframeworkto61131.data.FBUserConnection fbUserConnection = new org.supremica.external.operationframeworkto61131.data.FBUserConnection(
						userConnection);

				fbUserConnectionList.add(fbUserConnection);
			}

		}

		return fbUserConnectionList;
	}

	public List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> getFBSystemConnectionList(
			String machineName,
			org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity equipmentEntity,
			org.supremica.manufacturingtables.xsd.fid.FunctionBlock functionBlock) {

		List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> sysConnectionList = new LinkedList<org.supremica.external.operationframeworkto61131.data.FBSystemConnection>();

		// FB type
		String functionBlcokType = equipmentEntity.getFunctionBlock();

		// Equipment FB type not empty, top level equipment entity. Connect
		// variable to requestVar and feedbackVar
		if (!StringUtil.isEmpty(functionBlcokType)) {

			// For each state

			for (org.supremica.manufacturingtables.xsd.controlInformation.State state : equipmentEntity
					.getStates().getState()) {

				// add the connections for requestVar/feedbackVar
				// interlock/indicator which are visiable to SFC for state i
				sysConnectionList = getTopLevelConnectionListForStateI(
						machineName, equipmentEntity, state, functionBlock,
						sysConnectionList);

				// add the connections from sub-component for state i
				sysConnectionList = getSubComponentSysConnectionListForStateI(
						equipmentEntity.getEquipment(), state, functionBlock,
						sysConnectionList);
			}

		}

		return sysConnectionList;

	}

	private List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> getTopLevelConnectionListForStateI(
			String machineName,
			org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity equipmentEntity,
			org.supremica.manufacturingtables.xsd.controlInformation.State state,
			org.supremica.manufacturingtables.xsd.fid.FunctionBlock functionBlock,
			List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> sysConnectionList) {

		// add the requestVar and the feedbackVar connection for State i

		List<org.supremica.manufacturingtables.xsd.fid.FunctionType> topLevelVariableFunctionTypeList = new LinkedList<org.supremica.manufacturingtables.xsd.fid.FunctionType>();

		// FIXME move the functionType List to constant and initialize it in
		// config.xml
		topLevelVariableFunctionTypeList.add(this.FEEDBACK_VAR);
		topLevelVariableFunctionTypeList.add(this.REQUEST_VAR);
		topLevelVariableFunctionTypeList.add(this.INTERLOCK);
		topLevelVariableFunctionTypeList.add(this.INDICATOR);

		for (org.supremica.manufacturingtables.xsd.fid.FunctionType functionType : topLevelVariableFunctionTypeList) {

			sysConnectionList = getSysConnection(machineName, equipmentEntity,
					state, functionType, functionBlock, sysConnectionList);

		}

		// add the output connections to actuators for State i from
		// element
		// FIXME use reflection on element.
		for (org.supremica.manufacturingtables.xsd.controlInformation.Element element : equipmentEntity
				.getElements().getElement()) {

			for (org.supremica.manufacturingtables.xsd.controlInformation.IOConnectionMapping ioConnectionMapping : element
					.getIOConnectionMapping()) {

				if (ioConnectionMapping.getId().equals(state.getId())) {

					org.supremica.external.operationframeworkto61131.data.FBSystemConnection sysConnection_output = getSystemConnectionForStateI(
							state.getId(), this.IO_VAR, this.OUTPUT,
							functionBlock);

					// FIXME ioVariable's naming rule
					String output_var = ioConnectionMapping.getIOVariable();

					if (sysConnection_output != null && output_var != null) {

						sysConnection_output.getVariable().add(output_var);

						sysConnectionList.add(sysConnection_output);

					}

				}

			}

		}

		return sysConnectionList;
	}

	private List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> getSysConnection(
			String machineName,
			org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity equipmentEntity,
			org.supremica.manufacturingtables.xsd.controlInformation.State state,
			org.supremica.manufacturingtables.xsd.fid.FunctionType functionType,
			org.supremica.manufacturingtables.xsd.fid.FunctionBlock functionBlock,
			List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> sysConnectionList) {

		// Get the ioType of the connection
		org.supremica.manufacturingtables.xsd.fid.IoType ioType = getIoType(
				equipmentEntity, functionType);
		org.supremica.external.operationframeworkto61131.data.FBSystemConnection sysConnection = getSystemConnectionForStateI(
				state.getId(), functionType, ioType, functionBlock);

		// FIXME the variable name should be the same as in SFC. Machine name
		// may be needed.
		String varName = getVariableName(machineName, equipmentEntity, state,
				functionType);

		// log.debug("====add VarName:" + varName);
		// log.debug("When there are:");
		// for (String vars : sysConnection.getVariable()) {
		// log.debug("Var:" + vars);
		//
		// }

		if (sysConnection != null && varName != null) {
			sysConnection.addVariable(varName);

			sysConnectionList.add(sysConnection);

		}

		return sysConnectionList;
	}

	// decide the ioType of the connection by analysing equipment type and the
	// connection functionType
	private org.supremica.manufacturingtables.xsd.fid.IoType getIoType(
			org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity equipmentEntity,
			org.supremica.manufacturingtables.xsd.fid.FunctionType functionType) {

		// List<convertor.xsd.fid.FunctionType> INPUT_FunctionType_List=new
		// LinkedList<convertor.xsd.fid.FunctionType>();

		if (functionType.equals(this.REQUEST_VAR)
				|| functionType.equals(this.INTERLOCK)) {

			return this.INPUT;
		} else if (functionType.equals(this.IO_ADDRESS)
				|| functionType.equals(this.IO_ADDRESS)) {

			if (equipmentEntity
					.getType()
					.equals(
							org.supremica.manufacturingtables.xsd.controlInformation.EquipmentType.ACTUATOR)) {

				return this.OUTPUT;
			} else if (equipmentEntity
					.getType()
					.equals(
							org.supremica.manufacturingtables.xsd.controlInformation.EquipmentType.SENSOR)) {

				return this.INPUT;
			}

		} else if (functionType.equals(this.FEEDBACK_VAR)
				|| functionType.equals(this.INDICATOR)) {

			return this.OUTPUT;

		}

		return null;

	}

	// Decide the variable name
	private String getVariableName(
			String machineName,
			org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity equipmentEntity,
			org.supremica.manufacturingtables.xsd.controlInformation.State state,
			org.supremica.manufacturingtables.xsd.fid.FunctionType functionType) {

		StateQuery stateQuery = new StateQuery();

		stateQuery.setMachine(machineName);
		stateQuery.setState(state.getName());
		stateQuery.setEquipmentEntityName(equipmentEntity.getName());

		// FIXME here the machine name is removed from equipmentEntityName,
		// as the equipmentEntityName in EOP does not have the machine name
		// part.Should be unified
		// stateQuery.setEquipmentEntityName(equipmentEntityName.substring(
		// equipmentEntityName.length() - 2, equipmentEntityName
		// .length()));

		if (functionType.equals(this.REQUEST_VAR)
				|| functionType.equals(this.FEEDBACK_VAR)) {
			if (equipmentEntity
					.getType()
					.equals(
							org.supremica.manufacturingtables.xsd.controlInformation.EquipmentType.ACTUATOR)) {
				stateQuery
						.setEquipmentEntityType(org.supremica.manufacturingtables.xsd.eop.ActuatorValue.class);

			} else if (equipmentEntity
					.getType()
					.equals(
							org.supremica.manufacturingtables.xsd.controlInformation.EquipmentType.SENSOR)) {

				stateQuery
						.setEquipmentEntityType(org.supremica.manufacturingtables.xsd.eop.SensorValue.class);

			} else {

				log.error("Can not handle functionType:" + functionType
						+ "for machine:" + machineName + ",state:"
						+ state.getName() + ",entity:"
						+ equipmentEntity.getName());
			}

		} else if (functionType.equals(this.INTERLOCK)) {

			stateQuery.setEquipmentEntityType(org.supremica.external.operationframeworkto61131.data.Interlock.class);

		} else if (functionType.equals(this.INDICATOR)) {

			stateQuery.setEquipmentEntityType(org.supremica.external.operationframeworkto61131.data.Indicator.class);

		}

		FBCallingVars fbCallingVars = equipmentStateLookUp
				.getFBCallingVars(stateQuery);

		if (fbCallingVars != null) {

			if (functionType.equals(this.REQUEST_VAR)) {

				return fbCallingVars.getRequestVar().getName();

			} else if (functionType.equals(this.FEEDBACK_VAR)) {

				return fbCallingVars.getFeedbackVar().getName();
			} else {
				// For indicator and interlock
				return fbCallingVars.getFeedbackVar().getName();
			}
		} else {

			log.error("Can't find variable in machine:" + machineName
					+ ", for equipmentEntity:" + equipmentEntity.getName()
					+ " and state:" + state.getName());
			return Constant.ERROR_VARIABLE_NAME;
		}

	}

	// Get the connection list of all sub-component in the element list under
	// the component that has the function block type.
	private List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> getSubComponentSysConnectionListForStateI(
			org.supremica.manufacturingtables.xsd.controlInformation.Equipment equipment,
			org.supremica.manufacturingtables.xsd.controlInformation.State state,
			org.supremica.manufacturingtables.xsd.fid.FunctionBlock functionBlock,
			List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> sysConnectionList) {

		if (equipment == null) {
			log.error("Null equipment, functionBlock type:"
					+ functionBlock.getType());
		}

		if (equipment != null && equipment.getEquipmentEntity() != null) {

			// for each equipmentEntity
			for (org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity equipmentEntity : equipment
					.getEquipmentEntity()) {

				// if it has element,it means the element is visiable to SFC
				if (equipmentEntity.getElements() != null
						&& equipmentEntity.getElements().getElement().size() > 0) {

					// add the variable name in element for state i=stateID
					for (org.supremica.manufacturingtables.xsd.controlInformation.Element element : equipmentEntity
							.getElements().getElement()) {

						// FIXME need improvement for the situation when there
						// are
						// more than one IOConnectionMapping
						for (org.supremica.manufacturingtables.xsd.controlInformation.IOConnectionMapping ioMapping : element
								.getIOConnectionMapping()) {

							if (ioMapping.getId().equals(state.getId())) {

								org.supremica.manufacturingtables.xsd.fid.IoType ioType = this.INPUT;

								// Decide the ioTYPE
								// FIXME the type
								if (equipmentEntity
										.getType()
										.equals(
												org.supremica.manufacturingtables.xsd.controlInformation.EquipmentType.SENSOR)) {

									ioType = this.INPUT;

								} else if (equipmentEntity
										.getType()
										.equals(
												org.supremica.manufacturingtables.xsd.controlInformation.EquipmentType.ACTUATOR)) {

									ioType = this.OUTPUT;
								} else {

									log
											.error("Wrong equipment type, type="
													+ equipmentEntity.getType()
															.value());
								}
								// add the connection to sysConnectionList
								sysConnectionList = addToSysConnectionList(
										ioMapping.getIOVariable(), ioMapping
												.getId(), functionBlock,
										this.IO_VAR, ioType, sysConnectionList);

							}

						}

					}

				}
				// if the equipmentEntity has sub-component, search in the
				// equipment list, call this function again.

				if (equipmentEntity.getEquipment() != null
						&& equipmentEntity.getEquipment().getEquipmentEntity() != null
						&& equipmentEntity.getEquipment().getEquipmentEntity()
								.size() > 0) {
					sysConnectionList = getSubComponentSysConnectionListForStateI(
							equipmentEntity.getEquipment(), state,
							functionBlock, sysConnectionList);
				}

			}

		}

		return sysConnectionList;

	}

	private List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> addToSysConnectionList(
			String varName,
			BigInteger stateID,
			org.supremica.manufacturingtables.xsd.fid.FunctionBlock functionBlock,
			org.supremica.manufacturingtables.xsd.fid.FunctionType functionType,
			org.supremica.manufacturingtables.xsd.fid.IoType ioType,
			List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> sysConnectionList) {

		// The parameter list that could be connected to, it's obtained from the
		// connected parameter list
		List<org.supremica.external.operationframeworkto61131.data.FBSystemConnection> paramCandidateList = new LinkedList<org.supremica.external.operationframeworkto61131.data.FBSystemConnection>();

		for (org.supremica.external.operationframeworkto61131.data.FBSystemConnection sysConnection : sysConnectionList) {

			// log.debug("sysConnection.getStateID():"
			// + sysConnection.getStateID());
			// log.debug("stateID:" +stateID);

			if (sysConnection.getStateID().equals(stateID)) {

				// log.debug("sysConnection.getFunctionType():"
				// + sysConnection.getFunctionType());
				// log.debug("functionType:" +functionType.value());

				// functionType!=functionType.value()

				if (sysConnection.getFunctionType().equals(
						functionType.value().toString())) {

					// log.debug("sysConnection.getIOType():"
					// + sysConnection.getIOType());
					// log.debug("ioType.value():" + ioType.value());

					if (sysConnection.getIOType().equals(
							ioType.value().toString())) {

						// add the matching system connection to the candidate
						// list
						paramCandidateList.add(sysConnection);

						log.debug("Multi-Variable connection");

					}

				}

			}
		}

		// If there is no matching connection in the existing connection
		// list, create a new connection and add it to the list
		if (paramCandidateList.size() == 0) {

			org.supremica.external.operationframeworkto61131.data.FBSystemConnection newSysConnection = getSystemConnectionForStateI(
					stateID, functionType, ioType, functionBlock);

			if (newSysConnection != null) {

				// log.debug("Create new system connection:" + varName);

				newSysConnection.addVariable(varName);
				sysConnectionList.add(newSysConnection);

			}

			return sysConnectionList;

		} else if (paramCandidateList.size() == 1) {

			// If there is only one parameter and it has been occupied, connect
			// the variable to the same parameter

			// log.debug("Add to exsiting system connection" + varName);

			org.supremica.external.operationframeworkto61131.data.FBSystemConnection temp = paramCandidateList.get(0);

			sysConnectionList.remove(temp);

			temp.getVariable().add(varName);

			sysConnectionList.add(temp);

			return sysConnectionList;

		}

		// If there are more than one candidate connections available, error
		// occurs
		log.error("More than one system connections found for, stateID:"
				+ stateID + ",varName:" + varName + ",functionType:"
				+ functionType.value());

		return sysConnectionList;

	}

	// Find the matching parameters name in FB interface for specified stateID,
	// functionType and ioType
	private org.supremica.external.operationframeworkto61131.data.FBSystemConnection getSystemConnectionForStateI(
			BigInteger stateID,
			org.supremica.manufacturingtables.xsd.fid.FunctionType functionType,
			org.supremica.manufacturingtables.xsd.fid.IoType ioType,
			org.supremica.manufacturingtables.xsd.fid.FunctionBlock functionBlock) {

		for (org.supremica.manufacturingtables.xsd.fid.SystemConnection sysConnection : functionBlock
				.getSystemInterface().getSystemConnection()) {

			if (sysConnection.getStateID().equals(stateID)) {

				if (sysConnection.getFunctionType().equals(functionType)) {

					if (sysConnection.getIOType().equals(ioType)) {

						// log.debug("StateID:"
						// + stateID + ",functionType:"
						// + functionType.value()+", IoType:"+ioType.value());

						// log.debug("Return sysConnection When there are:");
						//
						// for (String vars : sysConnection.getVariable()) {
						// log.debug("Var:" + vars);
						//
						// }
						// sysConnection should not be return directly, as it's
						// a livelist, any operation on it will be passed the
						// the fid object
						return new org.supremica.external.operationframeworkto61131.data.FBSystemConnection(
								sysConnection);

					}

				}

			}
		}

		log.error("Cant find matchine system connection in FB:"
				+ functionBlock.getType() + ", stateID:" + stateID.toString()
				+ ", function type:" + functionType.value() + ", IoType:"
				+ ioType.value());
		return null;

	}

}
