package org.supremica.external.operationframeworkto61131.controlinfo;
/**
 * @author LC
 *
 */
import org.supremica.external.operationframeworkto61131.data.FBCallingQuery;
import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.FBCallingVarsList;
import org.supremica.external.operationframeworkto61131.data.StateQuery;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.JAXButil;
import org.supremica.external.operationframeworkto61131.util.StringUtil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;
import org.supremica.manufacturingtables.xsd.controlInformation.*;
import org.supremica.manufacturingtables.xsd.eop.*;
import org.supremica.manufacturingtables.xsd.virtualResourcesV3.*;



//the following two lines are only for testing, comment or remove for formal use.

// Build this class from factory.xml
public class ControlInformation implements EquipmentStateLookUp {

	private static Factory factory;

	private static VirtualResources virtualResources;

	private String currentCellPrefix;

	private LogUtil log = LogUtil.getInstance();

	private final String ZONE_ZONE = "zone";

	private final String ZONE_BOOK = "book";

	private final String ZONE_UNBOOK = "unbook";

	private final String ZONE_BOOKED = "booked";

	private final String ZONE_FREE = "free";

	public ControlInformation() {

		if (factory == null) {

			factory = (Factory) JAXButil.getInstance(
					Constant.CONTROL_INFO_XML_CONTEXT)
					.getRootElementObject(Constant.XML_FILE_PATH,
							Constant.CONTROL_INFO_XML_FILE_NAME);

		}
		if (virtualResources == null) {
			virtualResources = (VirtualResources) JAXButil.getInstance(
					Constant.VIRTUAL_RESOURCES_XML_CONTEXT)
					.getRootElementObject(Constant.XML_FILE_PATH,
							Constant.VIRTUAL_RESOURCES_XML_FILE_NAME);
		}

	}

	public Factory getFactory() {

		// Initialized in contructor
		return factory;

	}

	public String getPackageName() {

		return Factory.class.getPackage().getName();

	}

	public FBCallingVarsList getFBCallingVars(FBCallingQuery queryList) {

		FBCallingVarsList callingVarsList = new FBCallingVarsList();

		for (StateQuery stateQuery : queryList.getQueryList()) {

			callingVarsList.append(getFBCallingVars(stateQuery));
		}
		return callingVarsList;
	}

	public FBCallingVars getFBCallingVars(StateQuery stateQuery) {

		// log.debug("state query:");
		// log.debug("Machine:" + stateQuery.getMachine());
		// log.debug("EquipmentEntity Name:" +
		// stateQuery.getEquipmentEntityName());
		// log.debug("EquipmentEntity Type:" +
		// stateQuery.getEquipmentEntityType().toString());
		// log.debug("State:" + stateQuery.getState());

		Class entityType = stateQuery.getEquipmentEntityType();

		if (entityType != null) {

			if (entityType.equals(SensorValue.class)) {

				return getSensorState(stateQuery);

			} else if (entityType.equals(ActuatorValue.class)) {

				return getActuatorState(stateQuery);
			} else if (entityType.equals(ZoneState.class)) {

				return getZoneState(stateQuery);

			} else if (entityType.equals(VariableValue.class)) {

				return getVariableState(stateQuery);

			} else if (entityType.equals(ExternalComponentValue.class)) {

				return getExternalComponentState(stateQuery);
			} else if (entityType
					.equals(org.supremica.external.operationframeworkto61131.data.Interlock.class)) {
				return getInterlockState(stateQuery);

			} else if (entityType
					.equals(org.supremica.external.operationframeworkto61131.data.Indicator.class)) {

				return getIndicatorState(stateQuery);

			} else {

				log.error("Can't handle entityType:" + entityType.toString()
						+ ",in machine:" + stateQuery.getMachine()
						+ ",for state:" + stateQuery.getState());
			}
		} else if (this
				.getMachine(stateQuery.getMachine())
				.getHasOwnControlSystem()
				.value()
				.toString()
				.equalsIgnoreCase(
						org.supremica.manufacturingtables.xsd.physicalResource.ControlSystemType.YES
								.toString())) {

			return getMachineOperation(stateQuery);
		} else {

			log.error("Null equipment type in state query, machine:"
					+ stateQuery.getMachine() + ", state:"
					+ stateQuery.getState());

		}

		return null;
	}

	public FBCallingVars getSensorState(StateQuery stateQuery) {

		Machine machine = getMachine(stateQuery.getMachine());
		EquipmentEntity equipmentEntity = getEquipmentEntity(stateQuery
				.getEquipmentEntityName(), machine);

		State state = getState(stateQuery.getState(), equipmentEntity);

		if (state == null) {

			return null;
		}

		// TODO maybe need to add the state sematic stateQuery.getStateName()
		FBCallingVars callingVars = new FBCallingVars();
		callingVars.setRequestVar(new Var(state.getRequestVar().getName(),
				Boolean.FALSE));
		callingVars.setFeedbackVar(new Var(state.getFeedbackVar().getName(),
				Boolean.FALSE));
		callingVars.setOwnerType(SensorValue.class);
		callingVars.setTargetState(stateQuery.getState());
		callingVars.setEquipmentEntity(equipmentEntity.getName());

		return callingVars;

	}

	public FBCallingVars getActuatorState(StateQuery stateQuery) {

		Machine machine = getMachine(stateQuery.getMachine());
		EquipmentEntity equipmentEntity = getEquipmentEntity(stateQuery
				.getEquipmentEntityName(), machine);
		State state = getState(stateQuery.getState(), equipmentEntity);

		if (state == null) {
			return null;
		}

		// TODO maybe need to add the state sematic state.getStateName()
		FBCallingVars callingVars = new FBCallingVars();
		callingVars.setRequestVar(new Var(state.getRequestVar().getName(),
				Boolean.FALSE));
		callingVars.setFeedbackVar(new Var(state.getFeedbackVar().getName(),
				Boolean.FALSE));
		callingVars.setOwnerType(ActuatorValue.class);
		callingVars.setTargetState(stateQuery.getState());
		callingVars.setEquipmentEntity(equipmentEntity.getName());
		return callingVars;

	}

	public FBCallingVars getZoneState(StateQuery stateQuery) {

		// TODO not finished, need to improve, one variable for each state?
		String zoneName = stateQuery.getEquipmentEntityName().trim();
		String zoneState = stateQuery.getState().trim();
		String machine = stateQuery.getMachine();
		// FIXME odd way , move to constant
		String orderVar = "";
		String feedbackVar = "";
		FBCallingVars callingVars = new FBCallingVars();
		// FIXME need a more specified method to decide "book" or "free"
		if (zoneState.equals(Constant.ZONE_STATE_UNBOOK)) {
			// orderVar = machine + "_unbook_zone_" + zoneName.trim();
			orderVar = getCancelZoneString(machine, zoneName);

			// feedbackVar = machine + "_zone_" + zoneName.trim() + "_unbooked";

			// cancel zone feedback var = negated(book zone feedback var);
			feedbackVar = getZoneBookedString(machine, zoneName);

			Var cancelZoneFeedbackVar = new Var(feedbackVar, Boolean.FALSE);
			cancelZoneFeedbackVar.setNegated();

			callingVars.setFeedbackVar(cancelZoneFeedbackVar);

		} else if (zoneState.equals(Constant.ZONE_STATE_BOOK)) {
			// orderVar = machine + "_book_zone_" + zoneName.trim();

			orderVar = getBookZoneString(machine, zoneName);

			// feedbackVar = machine + "_zone_" + zoneName.trim() + "_booked";

			feedbackVar = getZoneBookedString(machine, zoneName);

			callingVars.setFeedbackVar(new Var(feedbackVar, Boolean.FALSE));

		} else {

			log.error("Wrong zone state, can not recognise zone state:"
					+ zoneState);
		}

		callingVars.setRequestVar(new Var(orderVar, Boolean.FALSE));
		callingVars.setOwnerType(ZoneState.class);
		callingVars.setTargetState(stateQuery.getState());
		callingVars.setEquipmentEntity(stateQuery.getEquipmentEntityName());

		return callingVars;

	}

	// This function is for unifing the variable naming in both IL and FB
	public FBCallingVars getInterlockState(StateQuery stateQuery) {

		// The value of IL->Acuator
		String equipmentEntityName = stateQuery.getEquipmentEntityName().trim();

		// The value of IL->Operation, it's the state of the actuator or the
		// number of a operation
		String state = stateQuery.getState().trim();

		String varName = "";
		// Decide if the equipment is intelligent or not.
		Machine tempMachine = this.getMachine(stateQuery.getMachine());

		// The equipment is a intelligent machine, then the state is a machine
		// operation number
		if (tempMachine != null
				&& tempMachine
						.getHasOwnControlSystem()
						.equals(
								org.supremica.manufacturingtables.xsd.physicalResource.ControlSystemType.YES)) {
			// The equipment is a intelligent machine, then the state is a
			// machine operation number

			varName = equipmentEntityName + "IL_Op"
					+ StringUtil.replaceSpaceWithUnderscore(state);

		} else {

			// The equipment is not a machine, the state is a equipment's state.
			varName = equipmentEntityName + "_IL_to_"
					+ StringUtil.replaceSpaceWithUnderscore(state);
		}

		FBCallingVars callingVars = new FBCallingVars();
		// RequestVar and FeedbackVar are the same here. But only FeedbackVar is
		// needed.
		callingVars.setRequestVar(new Var(varName, Boolean.FALSE));
		callingVars.setFeedbackVar(new Var(varName, Boolean.FALSE));
		callingVars
				.setOwnerType(org.supremica.manufacturingtables.xsd.interlock.IL.class);
		callingVars.setTargetState(state);
		callingVars.setEquipmentEntity(equipmentEntityName);

		return callingVars;

	}

	// This function is for unifing the variable naming in both IL and FB
	public FBCallingVars getIndicatorState(StateQuery stateQuery) {

		// The value of IL->Acuator, it will not be used to look up in the
		// physical resource.
		// The name will be used only for displaying. If the input equipment
		// name is wrong, the program can not detect the error and the displayed
		// name will also be wrong.
		String equipmentEntityName = stateQuery.getEquipmentEntityName().trim();

		// The value of IL->Operation, it's the state of the actuator or the
		// number of a operation
		String state = stateQuery.getState().trim();

		// It's not needed yet.
		// String machine = stateQuery.getMachine();
		// FIXME odd way , move to constant

		String varName = "";
		Machine tempMachine = this.getMachine(stateQuery.getMachine());

		// The equipment is a intelligent machine, then the state is a machine
		// operation number
		if (tempMachine != null
				&& tempMachine
						.getHasOwnControlSystem()
						.equals(
								org.supremica.manufacturingtables.xsd.physicalResource.ControlSystemType.YES)) {
			// The equipment is a intelligent machine, then the state is a
			// machine operation number

			// varName = equipmentEntityName + "Op"
			// + StringUtil.replaceSpaceWithUnderscore(state) + "_started";

			varName = equipmentEntityName + "Op"
					+ StringUtil.replaceSpaceWithUnderscore(state) + "_started";
		} else {

			// The equipment is not a machine with operating system, the state
			// is a equipment's state.
			varName = equipmentEntityName + "_to_"
					+ StringUtil.replaceSpaceWithUnderscore(state) + "_started";
		}
		FBCallingVars callingVars = new FBCallingVars();
		// RequestVar and FeedbackVar are the same here. But only FeedbackVar is
		// needed.
		callingVars.setRequestVar(new Var(varName, Boolean.FALSE));
		callingVars.setFeedbackVar(new Var(varName, Boolean.FALSE));
		callingVars
				.setOwnerType(org.supremica.manufacturingtables.xsd.interlock.IL.class);
		callingVars.setTargetState(state);
		callingVars.setEquipmentEntity(equipmentEntityName);

		return callingVars;

	}

	public FBCallingVars getVariableState(StateQuery stateQuery) {
		// TODO not finished, need to improve, get the variable information
		// from virtual resources

		String variableName = stateQuery.getMachine()
				+ stateQuery.getEquipmentEntityName();
		String variableState = stateQuery.getState();
		log.debug("Looking for:" + variableName + "=" + variableState);
		for (Variable variable : virtualResources.getVariables().getVariable()) {
			// StringUtil.replaceSpace
			if (StringUtil.replaceSpaceWithUnderscore(variable.getName())
					.equals(variableName)) {
				log.debug("Find:"
						+ StringUtil.replaceSpaceWithUnderscore(variable
								.getName()));
				for (Value value : variable.getValues().getValue()) {
					log.debug("value:" + value.getValueName());
					if (value.getValueName().equalsIgnoreCase(variableState)) {

						FBCallingVars callingVars = new FBCallingVars();
						callingVars.setRequestVar(new Var(value.getOrderVar(),
								Boolean.FALSE));
						callingVars.setFeedbackVar(new Var(value
								.getFeedbackVar(), Boolean.FALSE));
						callingVars.setOwnerType(VariableValue.class);
						callingVars.setTargetState(stateQuery.getState());
						callingVars.setEquipmentEntity(stateQuery
								.getEquipmentEntityName());
						return callingVars;
					}

				}

			}

		}

		log.error("Can't find variable:" + variableName + "=" + variableState);
		return null;

	}

	public FBCallingVars getExternalComponentState(StateQuery stateQuery) {

		FBCallingVars callingVars = new FBCallingVars();

		Machine machine = getMachine(stateQuery.getMachine());

		EquipmentEntity equipmentEntity = getEquipmentEntity(stateQuery
				.getEquipmentEntityName(), machine);

		// log.info("getExternalComponentState: name:"+stateQuery
		// .getEquipmentEntityName());
		// log.info("getExternalComponentState: machine:"+machine.getName());

		State state = getState(stateQuery.getState(), equipmentEntity);

		// TODO maybe need to add the state sematic name ,like open/close,
		// state.getName()
		callingVars.setRequestVar(new Var(state.getRequestVar().getName(),
				Boolean.FALSE));
		callingVars.setFeedbackVar(new Var(state.getFeedbackVar().getName(),
				Boolean.FALSE));
		callingVars.setOwnerType(ExternalComponentValue.class);
		callingVars.setTargetState(stateQuery.getState());
		callingVars.setEquipmentEntity(stateQuery.getEquipmentEntityName());

		return callingVars;

		// // FIXME fix the strange name rule
		// if (machineName.contains("#")) {
		//
		// callingVars.setRequestVar(new Var(stateQuery
		// .getEquipmentEntityName(), Boolean.FALSE));
		// callingVars.setFeedbackVar(new Var(stateQuery
		// .getEquipmentEntityName(), Boolean.FALSE));
		// callingVars.setOwnerType(ExternalComponentValue.class);
		// callingVars.setTargetState(stateQuery.getState());
		// callingVars.setEquipmentEntity(stateQuery.getEquipmentEntityName());
		//
		// } else {
		//
		// Machine machine = getMachine(stateQuery.getMachine());
		// EquipmentEntity equipmentEntity = getEquipmentEntity(stateQuery
		// .getEquipmentEntityName(), machine);
		// State state = getState(stateQuery.getState(), equipmentEntity);
		//
		// // TODO maybe need to add the state sematic state.getStateName()
		//
		// callingVars.setRequestVar(new Var(state.getFeedbackVar().getName(),
		// Boolean.FALSE));
		// callingVars.setFeedbackVar(new Var(
		// state.getFeedbackVar().getName(), Boolean.FALSE));
		// callingVars.setOwnerType(ExternalComponentValue.class);
		// callingVars.setTargetState(stateQuery.getState());
		// callingVars.setEquipmentEntity(stateQuery.getEquipmentEntityName());
		//
		// }

	}

	public FBCallingVars getMachineOperation(StateQuery stateQuery) {

		String machineName = stateQuery.getMachine();
		FBCallingVars callingVars = new FBCallingVars();

		String robot_operation_request_var = machineName + "_Op"
				+ stateQuery.getState() + "_exe";
		String robot_operation_feedback_var = machineName + "_Op"
				+ stateQuery.getState() + "_done";

		callingVars.setRequestVar(new Var(robot_operation_request_var,
				Boolean.FALSE));
		callingVars.setFeedbackVar(new Var(robot_operation_feedback_var,
				Boolean.FALSE));
		// convertor.xsd.physicalResource.MahineType.ROBOT.class
		// FIXME needed?
		callingVars.setOwnerType(stateQuery.getEquipmentEntityType());
		// OpID
		callingVars.setTargetState(stateQuery.getState());
		// machine name
		callingVars.setEquipmentEntity(stateQuery.getEquipmentEntityName());

		return callingVars;
	}

	/*
	 * Implement a method that returns a Machine object when given a machine ID;
	 * Machine object should return sensor, actuator connection object
	 * ,FBCallingVars, when given a ID of sensor or actuator
	 * 
	 * Element in EquipmentEntity will be recured.
	 */

	/*
	 * return a machine object which has the same name as machineName
	 */

	public Machine getMachine(String machineName) {

		// search a machine from the facotry

		if (machineName == null || machineName.isEmpty()
				|| machineName.equals("-")) {

			return null;
		}

		for (Area area : factory.getAreas().getArea()) {

			for (Cell cell : area.getCells().getCell()) {

				for (Machine machine : cell.getMachines().getMachine()) {

					if (machine.getName().equals(machineName)) {

						return machine;
					}

				}

			}
		}
		log.info("Did not find matching machine: " + machineName);

		return null;
	}

	public EquipmentEntity getEquipmentEntity(String equipEntityName,
			Machine machine) {

		if (machine == null) {
			log.error("Can not find equipment:" + equipEntityName);
			return null;
		}

		// log.debug("Search for equipment:" + equipEntityName);
		// FIXME can not search in the 3th or deeper layer in equipment list

		// String fix_equipEntityName = machine.getName() + equipEntityName;
		//
		// EquipmentEntity equipmentEntity = getEquipmentEntity(
		// fix_equipEntityName, machine.getEquipment());

		EquipmentEntity equipmentEntity = getEquipmentEntity(equipEntityName,
				machine.getEquipment());

		if (equipmentEntity != null) {

			log.debug("find equipmentEntity:" + equipmentEntity.getName());

			return equipmentEntity;
		} else {
			log.error("Can not find equipment entity:" + equipEntityName);
			return null;
		}
	}

	public EquipmentEntity getEquipmentEntity(String equipEntityName,
			Equipment equipment) {

		for (EquipmentEntity equipmentEntity : equipment.getEquipmentEntity()) {

			if (equipmentEntity.getName().equals(equipEntityName)) {

				return equipmentEntity;
			} else if (equipmentEntity.getEquipment() != null) {
				if (equipmentEntity.getEquipment().getEquipmentEntity() != null) {
					if (!equipmentEntity.getEquipment().getEquipmentEntity()
							.isEmpty()) {
						EquipmentEntity subEquipmentEntity = getEquipmentEntity(
								equipEntityName, equipmentEntity.getEquipment());
						if (subEquipmentEntity != null) {
							return subEquipmentEntity;
						}
					}
				}
			}
		}
		return null;
	}

	public State getState(String targetStateName,
			EquipmentEntity equipmentEntity) {

		// log.debug("Search in equipmentEntity:" + equipmentEntity.getName()
		// + " for state:" + targetStateName);

		if (equipmentEntity == null) {
			return null;
		}

		if (targetStateName != null) {

			for (State iState : equipmentEntity.getStates().getState()) {

				if (iState != null && targetStateName.equals(iState.getName())) {

					// replace space in variable name with underscore
					String stateName = StringUtil
							.replaceSpaceWithUnderscore(targetStateName);

					State state = new org.supremica.manufacturingtables.xsd.controlInformation.ObjectFactory()
							.createState();

					state.setName(stateName);
					// if there is no default value for requestVar, use the
					// automatic generatic name
					if (iState.getRequestVar() == null
							|| iState.getRequestVar().getName().isEmpty()) {

						String requestVarName = equipmentEntity.getName()
								+ "_goto_" + stateName;

						RequestVar requestVar = new org.supremica.manufacturingtables.xsd.controlInformation.ObjectFactory()
								.createRequestVar();

						requestVar.setName(requestVarName);

						state.setRequestVar(requestVar);
					} else {

						// if there is default name, keep the it.

						state.setRequestVar(iState.getRequestVar());

					}

					// if there is no default value for feedbackVar, use the
					// automatic generatic name

					if (iState.getRequestVar() == null
							|| iState.getFeedbackVar().getName().isEmpty()) {

						String feedbackVarName = equipmentEntity.getName()
								+ "_in_" + stateName;
						FeedbackVar feedbackVar = new org.supremica.manufacturingtables.xsd.controlInformation.ObjectFactory()
								.createFeedbackVar();

						feedbackVar.setName(feedbackVarName);

						state.setFeedbackVar(feedbackVar);
					} else {

						// if there is default feedbackVar name, keep the it.

						state.setFeedbackVar(iState.getFeedbackVar());

					}

					return state;
				}
			}

		}
		log.error("Can not find state, stateName:" + targetStateName
				+ " in equipment entity: " + equipmentEntity.getName());

		return null;
	}

	private String getCancelZoneString(String machineName, String zoneNum) {

		String cancelZoneString = machineName + "_" + ZONE_UNBOOK + "_"
				+ ZONE_ZONE + "_" + zoneNum;

		return cancelZoneString;

	}

	private String getBookZoneString(String machineName, String zoneNum) {

		String bookZoneString = machineName + "_" + ZONE_BOOK + "_" + ZONE_ZONE
				+ "_" + zoneNum;

		return bookZoneString;

	}

	private String getZoneBookedString(String machineName, String zoneNum) {

		String zoneBookedString = machineName + "_" + ZONE_ZONE + "_" + zoneNum
				+ "_" + ZONE_BOOKED;

		return zoneBookedString;

	}

	private String getZoneCanceledString(String machineName, String zoneNum) {

		String zoneCanceledString = ZONE_ZONE + "_" + zoneNum + "_" + ZONE_FREE;

		return zoneCanceledString;

	}

	public Boolean hasOwnSystem(String machineName) {

		Machine machine = this.getMachine(machineName);

		if (machine != null) {

			if (machine
					.getHasOwnControlSystem()
					.value()
					.equals(
							org.supremica.manufacturingtables.xsd.physicalResource.ControlSystemType.YES
									.value())) {

				return true;

			} else {

				return false;
			}

		} else {
			// FIXME may need to return a null;
			return false;

		}

	}

	//

	/**
	 * @param machineName
	 * @param equipmentEntityName
	 * @return A list of feedbackvar representing all state of the input
	 *         equipmentEntity
	 */
	public org.supremica.external.operationframeworkto61131.data.VarList getEquipmentInterface(
			String machineName, String equipmentEntityName) {

		org.supremica.external.operationframeworkto61131.data.VarList stateVarList = org.supremica.external.operationframeworkto61131.data.VarList
				.getInstance();

		Machine machine = this.getMachine(machineName);

		if (machine != null) {
			EquipmentEntity equipmentEntity = this.getEquipmentEntity(
					equipmentEntityName, machine);

			if (equipmentEntity != null && equipmentEntity.getStates() != null) {

				for (State stateI : equipmentEntity.getStates().getState()) {

					State state = getState(stateI.getName(), equipmentEntity);

					stateVarList.append(new Var(state.getFeedbackVar()
							.getName(), Boolean.FALSE));
				}

			}
		}

		return stateVarList;

	}

}
