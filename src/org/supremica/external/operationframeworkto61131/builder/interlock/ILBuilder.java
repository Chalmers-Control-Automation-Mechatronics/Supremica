package org.supremica.external.operationframeworkto61131.builder.interlock;

import org.plcopen.xml.tc6.Body;
import org.plcopen.xml.tc6.PouType;
import org.plcopen.xml.tc6.Project;
import org.supremica.external.operationframeworkto61131.builder.Builder;
import org.supremica.external.operationframeworkto61131.builder.PouInterfaceBuilder;
import org.supremica.external.operationframeworkto61131.controlinfo.EquipmentStateLookUp;
import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.StateQuery;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.data.VarList;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.layout.ladder.Coil;
import org.supremica.external.operationframeworkto61131.layout.ladder.Contact;
import org.supremica.external.operationframeworkto61131.layout.ladder.LeftPowerRail;
import org.supremica.external.operationframeworkto61131.layout.ladder.RightPowerRail;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.ReflectionUtil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;
import org.supremica.manufacturingtables.xsd.interlock.*;



import java.util.List;
import java.util.LinkedList;

import java.lang.reflect.Method;

/**
 * ILBuilder.java Generate Ladder Diagram from Interlock check tables. Returned
 * Pou is in LD.
 * 
 * Created: Mar 31, 2009 5:33:31 PM
 * 
 * @author LC
 * @version 1.0
 */
public class ILBuilder extends Builder {

	// private LogUtil log = LogUtil.getInstance();

	private org.supremica.manufacturingtables.xsd.interlock.IL currentInterlock;

	private int positionX = 30;

	private int positionY = 80;

	private int distanceUnit = 6;

	private Boolean isExtended = false;

	private Boolean hasControlSystem = false;

	/*
	 * The variable list for machine with control system to communicate with PLC
	 * It should contains variable that represent Zone, Operation and
	 * ExteranlComponent State
	 */

	private VarList externalVarList = VarList.getInstance();

	/*
	 * The list of contacts at the end of all rows. Elements in this list will
	 * be connected to Coil.
	 */
	private List<Contact> endOfLineContactList = new LinkedList<Contact>();

	/*
	 * 
	 * This list store the contacts that are generate in previous step in one
	 * row. There could be one element when only one contact is generated in
	 * previous step or several contacts when a divergence is generated in
	 * previous step.
	 */
	private List<Contact> lastContactList;

	public ILBuilder() {
		super();
	}

	public Project.Types.Pous.Pou convertILToLD(
			org.supremica.manufacturingtables.xsd.interlock.IL interlock) {

		// FIXME may need some code to avoid overwriting exsiting code here,
		currentInterlock = interlock;
		log.info("IL:" + interlock.getId());

		Body.LD ld;

		hasControlSystem = equipmentStateLookUp.hasOwnSystem(currentInterlock
				.getMachine());
		ld = CommonLayoutObject.objectFactory.createBodyLD();

		List<Object> ldConnector = ld.getCommentOrErrorOrConnector();

		// Set pou name

		// The size of the Term list, equals the rows of left PowerRail.
		int rows = interlock.getILStructure().getTerm().size();

		LeftPowerRail leftPowerRail = new LeftPowerRail(super.nextLocalId(),
				rows);
		leftPowerRail.setPosition(positionX, positionY);
		// add LeftPowerRail's PLCopenObj to pou.LD;

		int row = 0;
		int maxRow = interlock.getILStructure().getTerm().size() - 1;
		// List<Contact> lastContactList = new LinkedList<Contact>();

		for (org.supremica.manufacturingtables.xsd.interlock.Term term : interlock
				.getILStructure().getTerm()) {

			// Connect the first element to the LeftPowerRail and the last
			// element to the Coil

			List<Contact> contactList = generateContactList(term,
					leftPowerRail, row);

			log.debug("leftPowerRail position:" + leftPowerRail.getPosition());

			if (!contactList.isEmpty()) {

				// add Contact's PLCopenObj to pou

				for (Contact contact : contactList) {

					// And add contact PLCopenObj to pou
					ldConnector.add(contact.getPLCOpenObject());

				}

			} else {

				log.debug("Empty Contact list");

			}

			log.debug("row:=======end==== " + row);

			if (row < maxRow && this.isExtended == true) {

				leftPowerRail.extendFromRowI(row + 1, 1);
				this.isExtended = false;
			}
			row++;

		}
		ldConnector.add(leftPowerRail.getPLCOpenObject());
		// Connection coil to the last Contacts on each line.And add coil
		// PLCopenObj to pou
		Coil coil = this.generateCoil(interlock);
		coil.connectToContacts(endOfLineContactList, leftPowerRail,
				this.distanceUnit);

		ldConnector.add(coil.getPLCOpenObject());

		// Generate RightPowerRail
		RightPowerRail rightPowerRail = new RightPowerRail(super.nextLocalId(),
				1);

		int distanceToCoil = coil.getVaraible().length() * distanceUnit;

		rightPowerRail.connectToOut(coil, new Position(distanceToCoil, 0));

		ldConnector.add(rightPowerRail.getPLCOpenObject());

		Project.Types.Pous.Pou pou = CommonLayoutObject.objectFactory
				.createProjectTypesPousPou();

		pou.setPouType(PouType.PROGRAM);
		Project.Types.Pous.Pou.Interface pouInterface = PouInterfaceBuilder
				.generatePouInterfaceFromVarList(super.getInterfaceVarList(),
						null);

		pou.setInterface(pouInterface);

		pou.setName(interlock.getId());

		Body body = CommonLayoutObject.objectFactory.createBody();
		body.setLD(ld);
		pou.setBody(body);

		return pou;

	}

	// return a Coil with localId, default size, variable name and
	// ConnectionPointOut
	// No position, no ConnectionPointIn
	private Coil generateCoil(
			org.supremica.manufacturingtables.xsd.interlock.IL il) {

		// Build the output Coil first
		// The actuator could be a name of a intelligent machine, and the
		// operation is the operation number
		// The operation is the equipment state when the equipment is not a
		// intelligent machine

		// Get varialbe from equipmentStateLookUp, the same way as in FB where
		// IL is needed.

		Var coilVar = this.getStateFeedbackVar(il.getMachine(), il
				.getActuator(), org.supremica.external.operationframeworkto61131.data.Interlock.class, il
				.getOperation());

		Coil coil = new Coil(super.nextLocalId());
		coil.setVaraible(coilVar.getName());

		super.addToInterfaceVarList(coilVar);

		return coil;

	}

	// startingPosition is the Position of LeftPowerRail's ConnectionPointOut on
	// the i th line
	private List<Contact> generateContactList(Term term,
			LeftPowerRail leftPowerRail, int row) {

		ReflectionUtil reflectionUtil = new ReflectionUtil();
		Method[] termDeclaredMethods = Term.class.getDeclaredMethods();

		// This is list of all contact generated from the current IL
		List<Contact> contactList = new LinkedList<Contact>();

		// The last generated Contacts, it's used to be connected to newly
		// generated Contacts.
		this.lastContactList = null;

		for (int i = 0; i < termDeclaredMethods.length; i++) {

			try {

				if (!termDeclaredMethods[i].getName().toString().startsWith(
						"get")) {

					// To skip Term.setRow()and Term.setMode

					continue;
				}

				Object conditionObjList = reflectionUtil.invokeMethod(term,
						termDeclaredMethods[i].getName(), new Object[0]);

				if (!conditionObjList.getClass().equals(
						java.util.ArrayList.class)) {

					// To skip Term.getRow()and Term.getMode
					continue;
				}

				log.debug("process method:"
						+ termDeclaredMethods[i].getName().toString());
				int conditionObjListSize = ((Integer) reflectionUtil
						.invokeMethod(conditionObjList, "size", new Object[0]))
						.intValue();

				for (int j = 0; j < conditionObjListSize; j++) {
					// Object conditionObj = reflectionUtil.invokeMethod(
					// conditionObjList, "get", j);

					Method method = conditionObjList.getClass().getMethod(
							"get", int.class);
					Object conditionObj = method.invoke(conditionObjList, j);

					// This is list of contact generated from one element
					// retrived from 'get' method in Term
					// As there is simutaneous divergece, there could be one or
					// two contacts generated from one IL conditon
					List<Contact> newContactList = new LinkedList<Contact>();
					// IL ActuatorValue
					if (conditionObj.getClass().equals(ActuatorValue.class)) {

						newContactList = this
								.generateContactFromActuatorValue(conditionObj);

						contactList.addAll(this
								.generateDivergentConnectionContacts(
										newContactList, leftPowerRail, row));
						continue;

						// IL SensorValue
					} else if (conditionObj.getClass()
							.equals(SensorValue.class)) {

						newContactList = this
								.generateContactFromSensorValue(conditionObj);

						contactList.addAll(this
								.generateDivergentConnectionContacts(
										newContactList, leftPowerRail, row));

						// IL ExternalComponent
					} else if (conditionObj.getClass().equals(
							ExternalComponentValue.class)) {
						newContactList = this
								.generateContactFromExternalComponentValue(conditionObj);
						contactList.addAll(this
								.generateDivergentConnectionContacts(
										newContactList, leftPowerRail, row));

					} else if (conditionObj.getClass().equals(ZoneCheck.class)) {

						contactList.addAll(this.generateContactFromZoneCheck(
								conditionObj, leftPowerRail, row));

					} else if (conditionObj.getClass().equals(
							OperationCheck.class)) {

						contactList.addAll(this
								.generateContactFromOperationCheck(
										conditionObj, leftPowerRail, row));

					}

				}
				// End of one Term's condition
			} catch (Exception e) {

				e.printStackTrace();
				continue;
			}

		}// End of one Term

		if (this.lastContactList != null) {

			log.debug("lastContactList  null");
			this.endOfLineContactList.addAll(this.lastContactList);

		}

		return contactList;

	}

	// look up the contact's variable in
	// equipmentStateLookUp to ensure the name is the same
	// as in EOPs
	private Var getStateFeedbackVar(String machineName,
			String equipmentEntityName, Class equipmentEntityType, String state) {
		StateQuery stateQuery = new StateQuery();
		stateQuery.setMachine(machineName);

		if (equipmentEntityType
				.equals(org.supremica.manufacturingtables.xsd.eop.ZoneState.class)) {

			// When the equipmentEntity is a Zone, equipmentEntityName is then
			// zone name, machine name should not be added
			stateQuery.setEquipmentEntityName(equipmentEntityName);
		} else {
			stateQuery
					.setEquipmentEntityName(machineName + equipmentEntityName);
		}

		stateQuery.setEquipmentEntityType(equipmentEntityType);
		stateQuery.setState(state);

		FBCallingVars fbCallingVars = equipmentStateLookUp
				.getFBCallingVars(stateQuery);

		log.debug("IL");
		log.debug("machineName:" + currentInterlock.getMachine());
		log.debug("equipmentEntityName:" + equipmentEntityName);
		log.debug("equipmentEntityType:" + equipmentEntityType);
		log.debug("state:" + state);
		log.debug("");

		return fbCallingVars.getFeedbackVar();

	}

	// Only the equipmentEntity class is different with sensorValue
	private List<Contact> generateContactFromActuatorValue(Object conditionObj) {

		List<Contact> contactList = new LinkedList<Contact>();

		ReflectionUtil reflectionUtil = new ReflectionUtil();
		try {

			String state = (String) reflectionUtil.invokeMethod(conditionObj,
					"getValue", new Object[0]);

			if (this.isIgnored(state)) {

				return contactList;
			}
			String equipmentEntityName = (String) reflectionUtil.invokeMethod(
					conditionObj, "getActuator", new Object[0]);
			Class equipmentEntityType = org.supremica.manufacturingtables.xsd.eop.ActuatorValue.class;

			Var stateFeedbackVar = getStateFeedbackVar(currentInterlock
					.getMachine(), equipmentEntityName, equipmentEntityType,
					state);

			super.addToInterfaceVarList(stateFeedbackVar);
			// Generate a new Contact
			Contact contact = new Contact(super.nextLocalId());
			contact.setVaraible(stateFeedbackVar.getName());

			contactList.add(contact);

		} catch (Exception e) {

			e.printStackTrace();
		}

		return contactList;
	}

	private List<Contact> generateContactFromSensorValue(Object conditionObj) {

		List<Contact> contactList = new LinkedList<Contact>();

		ReflectionUtil reflectionUtil = new ReflectionUtil();
		try {

			String state = (String) reflectionUtil.invokeMethod(conditionObj,
					"getValue", new Object[0]);

			if (this.isIgnored(state)) {

				return contactList;
			}
			String equipmentEntityName = (String) reflectionUtil.invokeMethod(
					conditionObj, "getSensor", new Object[0]);
			Class equipmentEntityType = org.supremica.manufacturingtables.xsd.eop.SensorValue.class;

			Var stateFeedbackVar = getStateFeedbackVar(currentInterlock
					.getMachine(), equipmentEntityName, equipmentEntityType,
					state);

			super.addToInterfaceVarList(stateFeedbackVar);
			// Generate a new Contact
			Contact contact = new Contact(super.nextLocalId());
			contact.setVaraible(stateFeedbackVar.getName());

			contactList.add(contact);

		} catch (Exception e) {

			e.printStackTrace();
		}

		return contactList;

	}

	// FIXME will need to extract the divergence connection from a explicit
	// attribute in the value
	// Can handle simutanouns divergence with on combination
	private Contact generateExternalComponentValueAlternativeContact() {

		// used to extend the LeftPowerRail ConnectionPointOut position
		isExtended = true;

		String operation = this.currentInterlock.getOperation();
		// The alternative condition is when the machine operation has been
		// started.

		FBCallingVars fbCallingVars = null;
		Var opStartedVar = null;
		// For machine with operating system, the expected value is a
		// operation start variable;The variable here for an intelligent machine
		// should be the variable from COP which start the corresponding EOP for
		// the machine operation, not the variable connected to function
		// block.The variable connected to FB will be set in the corresponding
		// EOP.
		if (equipmentStateLookUp.hasOwnSystem(currentInterlock.getMachine())) {

			// fbCallingVars = convertor.builder.cop.COPFBCallingVarsListBuilder
			// .generateCOPOperationVar(operation, currentInterlock
			// .getMachine(), equipmentStateLookUp);

			String opStartedVarName = org.supremica.external.operationframeworkto61131.builder.functionblock.IntelligentMachineFB
					.getOperationStartedVarName(currentInterlock.getMachine(),
							operation);

			opStartedVar = new Var(opStartedVarName, false);

		} else {

			StateQuery stateQuery = new StateQuery();

			stateQuery.setMachine(currentInterlock.getMachine());
			stateQuery.setState(currentInterlock.getOperation());

			// FIXME in the interlock xml file, the equipment name is not
			// unique yet. Need to put machine name at the beginning.
			stateQuery.setEquipmentEntityName(currentInterlock.getMachine()
					+ currentInterlock.getActuator());

			stateQuery.setEquipmentEntityType(org.supremica.external.operationframeworkto61131.data.Indicator.class);

			fbCallingVars = equipmentStateLookUp.getFBCallingVars(stateQuery);

			opStartedVar = fbCallingVars.getFeedbackVar();

		}
		// Var opStartVar = this.equipmentStateLookUp.getMachineStateVars(
		// currentInterlock.getMachine(), currentInterlock.getActuator(),
		// currentInterlock.getOperation()).getRequestVar();

		// the variable name = machine name+ equipment name

		super.addToInterfaceVarList(opStartedVar);

		// The variable list for machine with control system to communicate with
		// PLC
		if (hasControlSystem) {
			externalVarList.append(opStartedVar);
		}
		// Generate a new Contact
		Contact contact = new Contact(super.nextLocalId());
		contact.setVaraible(opStartedVar.getName());

		return contact;

	}

	private List<Contact> generateContactFromExternalComponentValue(
			Object conditionObj) {

		List<Contact> contactList = new LinkedList<Contact>();

		log.debug("generateContactFromgetExternalComponentValue");

		ReflectionUtil reflectionUtil = new ReflectionUtil();
		try {

			String state = (String) reflectionUtil.invokeMethod(conditionObj,
					"getValue", new Object[0]);
			Boolean hasAlternative = false;

			if (this.isIgnored(state)) {

				return contactList;
			}

			// handle off-i, first remove the postfix -i from the state, then
			// add a alternative Contact

			if (state.endsWith(Constant.STATE_INITIAL_SIGN)) {

				state = state.replaceAll(Constant.STATE_INITIAL_SIGN, "");
				hasAlternative = true;
			}

			ExternalComponent externalComponent = (ExternalComponent) reflectionUtil
					.invokeMethod(conditionObj, "getExternalComponent",
							new Object[0]);
			String externalMachineName = externalComponent.getMachine();

			String externalEquipmentEntityName = externalComponent
					.getComponent();

			Class externalEquipmentEntityType = org.supremica.manufacturingtables.xsd.eop.ExternalComponentValue.class;

			Var stateFeedbackVar = getStateFeedbackVar(externalMachineName,
					externalEquipmentEntityName, externalEquipmentEntityType,
					state);

			super.addToInterfaceVarList(stateFeedbackVar);
			// The variable list for machine with control system to communicate
			// with PLC
			if (hasControlSystem) {
				externalVarList.append(stateFeedbackVar);
			}

			// Generate a new Contact
			Contact contact = new Contact(super.nextLocalId());
			contact.setVaraible(stateFeedbackVar.getName());

			contactList.add(contact);

			if (hasAlternative) {

				contactList.add(this
						.generateExternalComponentValueAlternativeContact());
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		return contactList;
	}

	private List<Contact> generateContactFromZoneCheck(Object conditionObj,
			LeftPowerRail leftPowerRail, int row) {

		List<Contact> newContactList = new LinkedList<Contact>();
		ReflectionUtil reflectionUtil = new ReflectionUtil();
		try {

			org.supremica.manufacturingtables.xsd.interlock.BeforeZones beforeZones = (org.supremica.manufacturingtables.xsd.interlock.BeforeZones) reflectionUtil
					.invokeMethod(conditionObj, "getBeforeZones", new Object[0]);

			for (String zone : beforeZones.getZone()) {

				if (isIgnored(zone)) {

					continue;
				}

				String equipmentEntityName = zone;
				Class equipmentEntityType = org.supremica.manufacturingtables.xsd.eop.ZoneState.class;
				String state = Constant.ZONE_STATE_BOOK;

				log.debug("Zone :" + equipmentEntityName);

				Var stateFeedbackVar = getStateFeedbackVar(currentInterlock
						.getMachine(), equipmentEntityName,
						equipmentEntityType, state);

				super.addToInterfaceVarList(stateFeedbackVar);
				// // The variable list for machine with control system to
				// // communicate with PLC
				// if (hasControlSystem) {
				// externalVarList.append(stateFeedbackVar);
				// }

				// Generate a new Contact
				Contact contact = new Contact(super.nextLocalId());
				contact.setVaraible(stateFeedbackVar.getName());

				newContactList.add(contact);

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return generateSeriesConnectionContacts(newContactList, leftPowerRail,
				row);

	}

	private List<Contact> generateContactFromOperationCheck(
			Object conditionObj, LeftPowerRail leftPowerRail, int row) {

		List<Contact> contactList = new LinkedList<Contact>();

		ReflectionUtil reflectionUtil = new ReflectionUtil();
		try {

			org.supremica.manufacturingtables.xsd.interlock.NotStarted notStarted = (org.supremica.manufacturingtables.xsd.interlock.NotStarted) reflectionUtil
					.invokeMethod(conditionObj, "getNotStarted", new Object[0]);

			org.supremica.manufacturingtables.xsd.interlock.NotOngoing notOngoing = (org.supremica.manufacturingtables.xsd.interlock.NotOngoing) reflectionUtil
					.invokeMethod(conditionObj, "getNotOngoing", new Object[0]);

			contactList.addAll(this.generateContactFromNotStarted(notStarted,
					leftPowerRail, row));

			contactList.addAll(this.generateContactFromNotOngoing(notOngoing,
					leftPowerRail, row));

		} catch (Exception e) {

			e.printStackTrace();
		}

		return contactList;
	}

	// return a list of series connection Contacts generated from
	// NotStarted.operation list
	private List<Contact> generateContactFromNotStarted(
			org.supremica.manufacturingtables.xsd.interlock.NotStarted notStarted,
			LeftPowerRail leftPowerRail, int row) {

		List<Contact> newContactList = new LinkedList<Contact>();
		for (String operation : notStarted.getOperation()) {

			if (isIgnored(operation)) {

				continue;
			}

			Var stateRequestVar = org.supremica.external.operationframeworkto61131.builder.cop.COPFBCallingVarsListBuilder
					.generateCOPOperationVar(operation, "-",
							this.equipmentStateLookUp).getRequestVar();

			super.addToInterfaceVarList(stateRequestVar);
			// The variable list for machine with control system to communicate
			// with PLC
			if (hasControlSystem) {
				externalVarList.append(stateRequestVar);
			}

			// Generate a new Contact
			Contact contact = new Contact(super.nextLocalId());
			contact.setVaraible(stateRequestVar.getName());
			contact.setNegated();

			newContactList.add(contact);

		}

		return this.generateSeriesConnectionContacts(newContactList,
				leftPowerRail, row);

	}

	// return a list of Contacts with alternative Contact generated from
	// NotOngoing.operation list
	private List<Contact> generateContactFromNotOngoing(
			org.supremica.manufacturingtables.xsd.interlock.NotOngoing notOngoing,
			LeftPowerRail leftPowerRail, int row) {

		List<Contact> contactList = new LinkedList<Contact>();

		for (String operation : notOngoing.getOperation()) {
			if (isIgnored(operation)) {

				return contactList;
			}

			List<Contact> newContactList = new LinkedList<Contact>();

			Var stateRequestVar = org.supremica.external.operationframeworkto61131.builder.cop.COPFBCallingVarsListBuilder
					.generateCOPOperationVar(operation, "-",
							this.equipmentStateLookUp).getRequestVar();

			super.addToInterfaceVarList(stateRequestVar);
			// The variable list for machine with control system to communicate
			// with PLC
			if (hasControlSystem) {
				externalVarList.append(stateRequestVar);
			}
			// Generate a new Contact
			Contact contact = new Contact(super.nextLocalId());
			contact.setVaraible(stateRequestVar.getName());
			contact.setNegated();

			newContactList.add(contact);

			// used to extend the LeftPowerRail ConnectionPointOut position
			isExtended = true;

			FBCallingVars fbCallingVars = org.supremica.external.operationframeworkto61131.builder.cop.COPFBCallingVarsListBuilder
					.generateCopPreconditionVar(operation, "",
							this.equipmentStateLookUp);
			Var opEndVar = fbCallingVars.getRequestVar();

			super.addToInterfaceVarList(opEndVar);
			// The variable list for machine with control system to communicate
			// with PLC
			if (hasControlSystem) {
				externalVarList.append(opEndVar);
			}
			// Generate a alternative Contact
			Contact altContact = new Contact(super.nextLocalId());
			altContact.setVaraible(opEndVar.getName());

			newContactList.add(altContact);

			contactList.addAll(generateDivergentConnectionContacts(
					newContactList, leftPowerRail, row));
		}

		return contactList;
	}

	private List<Contact> generateSeriesConnectionContacts(
			List<Contact> newContactList, LeftPowerRail leftPowerRail, int row) {

		List<Contact> contactList = new LinkedList<Contact>();

		if (!newContactList.isEmpty()) {

			// Connect the new Contact with the last Contact in the
			// list.
			// If the new Contact is the first one in the row,
			// it will be connected later to the LeftPowerRail's i
			// ConnectionPointOut after the Contact list is
			// returned.

			for (Contact newContact : newContactList) {

				if (this.lastContactList != null) {

					// distance depends on names of both the last
					// and
					// the new Contacts

					int distance = distanceUnit
							* (newContact.getVaraible().length() + this
									.getLongestNameLength(this.lastContactList));

					log.debug("distance:" + distance);

					// Set the position of the new Contacts, take
					// the
					// last Contact in lastContactList as starting
					// point;

					Position lastContactPosition = this.lastContactList.get(0)
							.getPosition();
					log.debug("last contact position:" + lastContactPosition);

					int startingPointY = lastContactPosition.getY();
					int startingPointX = lastContactPosition.getX() + distance;
					Position startingPoition = new Position(startingPointX,
							startingPointY);

					newContact.setPosition(startingPoition);

					for (Contact lastContactI : this.lastContactList) {

						newContact.connectToOut(lastContactI);

					}

					log.debug("-----------------");

					contactList.add(newContact);
					this.lastContactList.clear();
					this.lastContactList.add(newContact);
				} else {

					// If the Contact the first one, connect it to
					// leftPowerRail's n th ConnectionPointOut;

					int distance = distanceUnit
							* (newContact.getVaraible().length());

					newContact.connectToOut(leftPowerRail, row, 0, null,
							new Position(distance, 0));

					contactList.add(newContact);

					this.lastContactList = new LinkedList<Contact>();
					this.lastContactList.add(newContact);

				}

			}
		}
		return contactList;

	}

	/*
	 * 
	 * Generate divergent contacts for a list of variable. The leftPowerRail is
	 * used to be connected to when the divergence is the first contact next to
	 * the leftPowerRail. Or the newly generated contacts will be connected to
	 * lastContactList.
	 */

	private List<Contact> generateDivergentConnectionContacts(
			List<Contact> newContactList, LeftPowerRail leftPowerRail, int row) {

		List<Contact> contactList = new LinkedList<Contact>();

		if (!newContactList.isEmpty()) {

			// Connect the new Contact with the last Contact in the
			// list.
			// If the new Contact is the first one in the row,
			// it will be connected later to the LeftPowerRail's i
			// ConnectionPointOut after the Contact list is
			// returned.

			if (this.lastContactList != null) {

				// distance depends on names of both the last and
				// the new Contacts
				int distance = distanceUnit
						* (this.getLongestNameLength(newContactList) + this
								.getLongestNameLength(this.lastContactList));

				log.debug("distance:" + distance);

				// Set the position of the new Contacts, take the
				// last Contact in lastContactList as starting
				// point;

				Position lastContactPosition = this.lastContactList.get(0)
						.getPosition();
				log.debug("last contact position:" + lastContactPosition);

				int startingPointY = lastContactPosition.getY();
				int startingPointX = lastContactPosition.getX() + distance;
				Position startingPoition = new Position(startingPointX,
						startingPointY);
				newContactList = setContactPosition(newContactList,
						startingPoition,
						org.supremica.external.operationframeworkto61131.layout.ladder.LeftPowerRail.lineMargin);

				List<Contact> tempNewContactList = new LinkedList<Contact>();
				for (Contact newContactI : newContactList) {

					for (Contact lastContactI : this.lastContactList) {

						newContactI.connectToOut(lastContactI);

					}

					log.debug("-----------------");

					tempNewContactList.add(newContactI);

				}
				contactList.addAll(tempNewContactList);
				this.lastContactList = tempNewContactList;
			} else {

				// If the Contact the first one, connect it to
				// leftPowerRail's n th ConnectionPointOut;

				this.lastContactList = new LinkedList<Contact>();
				int distance = distanceUnit
						* (this.getLongestNameLength(newContactList));

				Position startingPoition = newContactList.get(0)
						.getNextPosition(leftPowerRail, row, 0,
								new Position(distance, 0));

				newContactList = setContactPosition(newContactList,
						startingPoition,
						org.supremica.external.operationframeworkto61131.layout.ladder.LeftPowerRail.lineMargin);

				for (Contact newContactI : newContactList) {

					newContactI.connectToOut(leftPowerRail, row, 0, null, null);

					contactList.add(newContactI);
					this.lastContactList.add(newContactI);

				}

			}

		}

		return contactList;
	}

	/*
	 * 
	 * return the longest contact name, will be used to adjust constact's
	 * position
	 */
	private int getLongestNameLength(List<Contact> contactList) {

		int longest = 0;

		for (Contact contact : contactList) {

			if (contact.getVaraible().length() >= longest) {

				longest = contact.getVaraible().length();
			}
		}

		return longest;

	}

	// set the divergent contact's position, set their positions to be in the
	// same column
	private List<Contact> setContactPosition(List<Contact> contactList,
			Position startingPosition, int lineMargin) {

		List<Contact> tempContactList = new LinkedList<Contact>();
		for (Contact newContactI : contactList) {

			newContactI.setPosition(new Position(startingPosition));

			startingPosition.addY(lineMargin);
			log.debug("New contact position:" + newContactI.getPosition());

			tempContactList.add(newContactI);

		}
		return tempContactList;

	}

	/*
	 * If to ignore the IL value
	 */
	private Boolean isIgnored(String state) {

		if (state == null || state.equalsIgnoreCase(Constant.STATE_IGNORE_SIGN)
				|| state.equalsIgnoreCase(Constant.STATE_SKIP_SIGN)
				|| state.length() == 0) {

			return true;
		} else {
			return false;
		}

	}

	/*
	 * Return the variable list for machine with control system to communicate
	 * with PLC
	 */
	public VarList getExternalVarList() {

		return this.externalVarList;
	}

}
