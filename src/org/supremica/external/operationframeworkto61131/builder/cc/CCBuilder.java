
package org.supremica.external.operationframeworkto61131.builder.cc;

import java.util.List;
import java.util.LinkedList;

import org.plcopen.xml.tc6.Body;
import org.plcopen.xml.tc6.PouType;
import org.plcopen.xml.tc6.Project;
import org.supremica.external.operationframeworkto61131.builder.Builder;
import org.supremica.external.operationframeworkto61131.builder.PouInterfaceBuilder;
import org.supremica.external.operationframeworkto61131.builder.sfc.BlockBuilder;
import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.FBCallingVarsList;
import org.supremica.external.operationframeworkto61131.data.StateQuery;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.data.VarList;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.layout.sfc.block.Block;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.manufacturingtables.xsd.cc.*;
import org.supremica.manufacturingtables.xsd.eop.Operation;
import org.supremica.manufacturingtables.xsd.eop.SensorValue;






/**
 * CCBuilder.java Builds the Pou for CC. The returned Pou is in LD.
 *
 * Created: Mar 31, 2009 5:05:03 PM
 *
 * @author LC
 * @version 1.0
 */
public class CCBuilder extends org.supremica.external.operationframeworkto61131.builder.Builder {

	private List<String> CCNameList = new LinkedList<String>();

	private String cellName;

	private static final String _MODE_AUTO = "_auto";

	private static final String _INIT_POSITION = "_init_position";

	private static final String CC_ = "CC_";

	private static final String POU_NAME = "CC";

	private static final org.plcopen.xml.tc6.StorageModifierType COIL_RESET = org.plcopen.xml.tc6.StorageModifierType.RESET;

	public CCBuilder() {

		super();
	}

	/*
	 * Return a Pou that contains the CC, machine in initial position, Start All
	 * COPs, All COPs are Done, Varaible reset. 
	 * The returned Pou is in LD
	 */
	public Project.Types.Pous.Pou getCC(List<Object> listOfEOP,
			List<Object> listOfCOP, CycleStartConditions cycleStartConditions,
			String _cellName) {

		for (CC cc : cycleStartConditions.getCC()) {

			CCNameList.add(cc.getName());

		}

		this.cellName = _cellName;

		// Body.FBD fbd = CommonLayoutObject.objectFactory.createBodyFBD();

		Body.LD ld = CommonLayoutObject.objectFactory.createBodyLD();

		this.buildCellInInitialPosition(listOfEOP, ld
				.getCommentOrErrorOrConnector());
		this.buildCC(cycleStartConditions, ld.getCommentOrErrorOrConnector());

		this.buildStartAllCOPs(cycleStartConditions, ld
				.getCommentOrErrorOrConnector());

		this.buildDoneAllCOPs(listOfCOP, ld.getCommentOrErrorOrConnector());

		this.buildOpEndReset(listOfEOP, ld.getCommentOrErrorOrConnector());

		Project.Types.Pous.Pou pou = CommonLayoutObject.objectFactory
				.createProjectTypesPousPou();

		pou.setPouType(PouType.PROGRAM);
		Project.Types.Pous.Pou.Interface pouInterface = PouInterfaceBuilder
				.generatePouInterfaceFromVarList(super.getInterfaceVarList(),
						null);

		pou.setInterface(pouInterface);

		pou.setInterface(pouInterface);
		pou.setName(POU_NAME);
		Body body = CommonLayoutObject.objectFactory.createBody();
		body.setLD(ld);
		pou.setBody(body);

		return pou;

	}

	/*
	 * Build the condition for each machine's initial condition by analyzing all
	 * eop's. When the eop has the attribute <InitialState isInitial="true">,
	 * then the initial state should be added to the CellInInitalState Check.
	 * When one machine has more than one <InitialState isInitial="true"> those
	 * InitalStates are connected by a OR block and the OR block is connected to
	 * the CellInInitalState.
	 */
	private void buildCellInInitialPosition(List<Object> listOfEOP,
			List<Object> commonConnector) {

		Position distance = new Position(Constant.DistanceInVariableToBlock
				.intValue() * 2, 0);

		// Get inital state(s) for one machine

		// First build a list of EOP that has a intialState and isInitial="true"
		List<Operation> listOfInitalStateCheckOperation = new LinkedList<Operation>();
		log.debug("size of eop:" + listOfEOP.size());
		for (int i = 0; i < listOfEOP.size(); i++) {

			Operation operation = (Operation) listOfEOP.get(i);

			// only need isInitial="true"
			if (operation.getEOP().getInitialState() != null
					&& operation.getEOP().getInitialState().isIsInitial() != null
					&& operation.getEOP().getInitialState().isIsInitial()) {

				listOfInitalStateCheckOperation.add(operation);

			}

		}

		// Then process the intialStateCheck for each machine
		log.debug("size of intial:" + listOfInitalStateCheckOperation.size());

		// The element is a variable or AND or OR
		List<CommonLayoutObject> commonObjOfAllMachineAND = new LinkedList<CommonLayoutObject>();

		while (true) {

			// the list of all initial state check for machine I, returned
			// elements will be removed from the input list
			List<Operation> listOfOperationOfMachineI = getAllInitalStateForMachineI(listOfInitalStateCheckOperation);

			// no more initial state for any machine, stop;
			if (listOfOperationOfMachineI.size() == 0) {

				break;

			}

			// The element is a variable or AND, when there are more than one
			// initial state check for one machine, they are connected with an
			// OR block;
			List<CommonLayoutObject> commonObjOfOR = new LinkedList<CommonLayoutObject>();
			// The initalStateCheck for one machine.
			for (Operation operation : listOfOperationOfMachineI) {
				log
						.debug("inital state from machine:"
								+ operation.getMachine());

				// build the AND

				FBCallingVarsList callingVarsList = org.supremica.external.operationframeworkto61131.builder.eop.EOPBuilder
						.getEOPInitalStateFBCallingVarsList(operation,
								Builder.equipmentStateLookUp);

				// for (FBCallingVars fbCallingVars : callingVarsList
				// .getFBCallingVarsList()) {
				//
				// log
				// .debug("Var :"
				// + fbCallingVars.getFeedbackVar().getName());
				//
				// }

				if (callingVarsList.getFeedbackVarList().getVars().size() < 1) {

					log.debug("Empty intial state check in machine:"
							+ operation.getMachine() + ", opID:"
							+ operation.getOpID());
					continue;
				}

				CommonLayoutObject commonObj = BlockBuilder
						.generateVariablesWithBlock(callingVarsList
								.getFeedbackVarList(), super.getLastPosition(),
								commonConnector, this, distance,
								BlockBuilder.AND, null);

				commonObjOfOR.add(commonObj);

				super.adjustLastPositionY(Constant.BlockANDHeightUnit
						.intValue() * 2);

			}

			if (commonObjOfOR.size() > 1) {

				Block blockOR = BlockBuilder.getORBlock(super.nextLocalId(),
						commonObjOfOR.size());

				BlockBuilder.connectCommonLayoutObjectToBlock(commonObjOfOR,
						blockOR, new Position(distance.getX() * 2, 0));

				commonConnector.add(blockOR.getPLCOpenObject());

				commonObjOfAllMachineAND.add(blockOR);
			} else if (commonObjOfOR.size() == 1) {

				commonObjOfAllMachineAND.add(commonObjOfOR.get(0));

			} else {

				continue;
			}

			super
					.adjustLastPositionY(Constant.BlockANDHeightUnit.intValue() * 2);

		}

		// connect all machine's initial state check to an AND

		Block allMachineBlockAND = BlockBuilder.getANDBlock(
				super.nextLocalId(), commonObjOfAllMachineAND.size());

		// FIXME fix the distance
		BlockBuilder.connectCommonLayoutObjectToBlock(commonObjOfAllMachineAND,
				allMachineBlockAND, new Position(distance.getX() * 2, 0));

		commonConnector.add(allMachineBlockAND.getPLCOpenObject());

		// Add the Cell in initial position variable at the last AND
		org.supremica.external.operationframeworkto61131.layout.sfc.OutVariable cellInInitPosition = new org.supremica.external.operationframeworkto61131.layout.sfc.OutVariable(
				super.nextLocalId(), this.getCellInInitialPositionString());

		cellInInitPosition.connectToOut(allMachineBlockAND, 0, 0, null,
				new Position(distance.getX() * 2, 0));

		commonConnector.add(cellInInitPosition.getPLCOpenObject());

	}

	// return a list of Operation that belongs to the same machine as the first
	// element of the list of input Operation
	private List<Operation> getAllInitalStateForMachineI(
			List<Operation> listOfOperation) {

		log.debug("size of getAllInitalStateForMachineI:"
				+ listOfOperation.size());
		List<Operation> listOfOperationOfMachineI = new LinkedList<Operation>();

		int size = listOfOperation.size();

		// No element or only one element is in the list, return the input list
		if (size == 0) {

			return listOfOperationOfMachineI;
		} else {

			// size>=1 More than one element is in the input list, use the first
			// element as the starting point and compare the rest elements in
			// the list with it
			Operation operationOne = listOfOperation.get(0);

			listOfOperationOfMachineI.add(operationOne);
			// start comparing from the second element
			int i = 1;
			while (i < size) {

				Operation operationN = listOfOperation.get(i);

				if (operationN.getMachine().equalsIgnoreCase(
						operationOne.getMachine())) {

					listOfOperationOfMachineI.add(operationN);
				}

				i++;
			}

		}

		// remove the returned element; so next time when the function is
		// called, only the elements remain in the list will be compared.
		listOfOperation.removeAll(listOfOperationOfMachineI);
		return listOfOperationOfMachineI;

	}

	public static FBCallingVars getCCVar(String cc, String machine) {

		FBCallingVars ccVars = new FBCallingVars();
		Var ccFeedbackVar = getCCVar(cc);
		ccVars.setFeedbackVar(ccFeedbackVar);
		ccVars.setRequestVar(ccFeedbackVar);
		ccVars.setTargetState(Boolean.TRUE.toString());
		// Set machine's name to equipment entity. May not be used when
		// being converted to SFC
		ccVars.setEquipmentEntity(machine);

		return ccVars;

	}

	private static Var getCCVar(String cc) {

		Var ccVar = new Var(CC_ + cc, Boolean.FALSE);

		return ccVar;
	}

	private void buildCC(CycleStartConditions cycleStartConditions,
			List<Object> commonConnector) {

		Position distance = new Position(Constant.DistanceInVariableToBlock
				.intValue() * 2, 0);

		// For each CC
		for (CC cc : cycleStartConditions.getCC()) {

			// For each condition in one CC, the condition is check by build an
			// external machine state query, because in CC, the equipment type
			// is ignored, only consider the machine and the state.

			VarList conditionVarList = VarList.getInstance();
			for (Condition condition : cc.getCondition()) {

				String state = condition.getState();

				// Check if the state should be ignored.
				if (state.equals(Constant.STATE_IGNORE_SIGN)
						|| state.equals(Constant.STATE_SKIP_SIGN)) {

					continue;
				}
				String machine = condition.getMachine();
				String equipment = condition.getComponent();

				// Use the feedback Var's name

				FBCallingVars ccFBFBCallingVars = this.getMachineStateVars(
						machine, equipment, state);
				Var conditionVar;
				if (ccFBFBCallingVars == null) {
					continue;
				} else {
					conditionVar = ccFBFBCallingVars.getFeedbackVar();

				}

				conditionVarList.append(conditionVar);
			}

			// To make the CC exclusive true, also add all the other CC's
			// negated value to the AND block

			conditionVarList.append(this.getExclusiveConditions(CCNameList, cc
					.getName()));

			CommonLayoutObject conditionAND = BlockBuilder
					.generateVariablesWithBlock(conditionVarList, super
							.getLastPosition(), commonConnector, this,
							distance, BlockBuilder.AND, null);

			// At last add the cc variable to AND output
			org.supremica.external.operationframeworkto61131.layout.sfc.OutVariable ccVariable = new org.supremica.external.operationframeworkto61131.layout.sfc.OutVariable(
					super.nextLocalId(), getCCVar(cc.getName()).getName());

			ccVariable.connectToOut(conditionAND, 0, 0, null, new Position(
					distance.getX() * 2, 0));

			commonConnector.add(ccVariable.getPLCOpenObject());

			// Add a margin between CC conditions

			super
					.adjustLastPositionY(Constant.BlockANDHeightUnit.intValue() * 2);

		}

	}

	private void buildStartAllCOPs(CycleStartConditions cycleStartConditions,
			List<Object> commonConnector) {

		Position distance = new Position(Constant.DistanceInVariableToBlock
				.intValue() * 2, 0);

		// Add all CCs
		VarList allCCs = getAllCCsVarList();

		List<CommonLayoutObject> allCCsORList = new LinkedList<CommonLayoutObject>();

		CommonLayoutObject allCCsOR = BlockBuilder.generateVariablesWithBlock(
				allCCs, super.getLastPosition(), commonConnector, this,
				distance, BlockBuilder.OR, null);

		allCCsORList.add(allCCsOR);

		super.adjustLastPositionY(Constant.BlockANDHeightUnit.intValue() * 2);

		VarList additionVars = VarList.getInstance();
		// Add cell in initial position

		additionVars.append(new Var(this.getCellInInitialPositionString(),
				false));

		// Add cell mode
		additionVars.append(new Var(this.getCellModeInAuto(), false));

		CommonLayoutObject allCCsAND = BlockBuilder.generateVariablesWithBlock(
				additionVars, super.getLastPosition(), commonConnector, this,
				distance, BlockBuilder.AND, allCCsORList);

		// At last add the cc variable to AND output

		Var all_COP_start = org.supremica.external.operationframeworkto61131.builder.cop.COPFBCallingVarsListBuilder
				.generateAllCOPsStartVar(null).getFeedbackVar();

		BlockBuilder.generateOutVariableForBlock(all_COP_start, allCCsAND,
				commonConnector, this, new Position(distance.getX() * 2, 0));

		super.adjustLastPositionY(Constant.BlockANDHeightUnit.intValue() * 2);

	}

	private void buildDoneAllCOPs(List<Object> listOfCOP,
			List<Object> commonConnector) {

		Position distance = new Position(Constant.DistanceInVariableToBlock
				.intValue() * 2, 0);

		// for each cc, find COPs that has the same cc
		for (String ccName : this.CCNameList) {

			VarList COPDoneVarList = VarList.getInstance();
			// Add CC var at the beginning
			COPDoneVarList.append(getCCVar(ccName));

			// Add all COP of CC 's done variable
			for (Object obj : listOfCOP) {

				org.supremica.manufacturingtables.xsd.rop.ROP rop = (org.supremica.manufacturingtables.xsd.rop.ROP) obj;

				for (String cc : rop.getCC()) {

					if (cc.equals(ccName)) {

						Var varCOPDone = org.supremica.external.operationframeworkto61131.builder.cop.COPFBCallingVarsListBuilder
								.generateCopDoneVar(rop.getId(),
										rop.getMachine()).getFeedbackVar();

						COPDoneVarList.append(varCOPDone);
						break;

					}

				}

			}

			// Connect the Vars to AND

			CommonLayoutObject COPDoneAND = BlockBuilder
					.generateVariablesWithBlock(COPDoneVarList, null,
							commonConnector, this, distance, BlockBuilder.AND,
							null);

			// All_Cops_Done_CC

			Var allCOPDoneCCVar = org.supremica.external.operationframeworkto61131.builder.cop.COPFBCallingVarsListBuilder
					.generateALLCOPDoneVar(ccName);

			BlockBuilder.generateOutVariableForBlock(allCOPDoneCCVar,
					COPDoneAND, commonConnector, this, distance);

			super
					.adjustLastPositionY(Constant.BlockANDHeightUnit.intValue() * 2);
		}

	}

	private void buildOpEndReset(List<Object> listOfEOP,
			List<Object> commonConnector) {

		Position distance = new Position(Constant.DistanceInVariableToBlock
				.intValue() * 2, 0);

		VarList allCOPDoneCCVarList = VarList.getInstance();
		for (String cc : this.CCNameList) {

			Var allCOPDoneCCVar = org.supremica.external.operationframeworkto61131.builder.cop.COPFBCallingVarsListBuilder
					.generateALLCOPDoneVar(cc);

			allCOPDoneCCVarList.append(allCOPDoneCCVar);

		}

		CommonLayoutObject blockOR = BlockBuilder.generateVariablesWithBlock(
				allCOPDoneCCVarList, null, commonConnector, this, distance,
				BlockBuilder.OR, null);

		VarList opEndVarList = VarList.getInstance();

		// generate op_end variable
		for (Object obj : listOfEOP) {

			org.supremica.manufacturingtables.xsd.eop.Operation op = (org.supremica.manufacturingtables.xsd.eop.Operation) obj;

			Var opEndVar = org.supremica.external.operationframeworkto61131.builder.cop.COPFBCallingVarsListBuilder
					.generateCopPreconditionVar(op.getOpID().toString(),
							op.getMachine(), Builder.equipmentStateLookUp)
					.getFeedbackVar();

			opEndVarList.append(opEndVar);

		}

		BlockBuilder.generateOutCoilForBlock(opEndVarList, blockOR,
				commonConnector, this, distance, COIL_RESET);

		// // connect op_end variable to OR
		//
		// for (Var var : opEndVarList.getVars()) {
		//
		// convertor.layout.sfc.OutVariable outVariable = new
		// convertor.layout.sfc.OutVariable(
		// super.nextLocalId(), var.getName());
		//
		// outVariable.connectToOut(blockOR, 0, 0, null, distance);
		//
		// //
		// outVariable.setPosition(outVariable.getPosition().getX(),super.getLastPosition().getY());
		//
		// commonConnector.add(outVariable.getPLCOpenObject());
		//
		// distance.addY(Constant.BlockANDHeightUnit.intValue() + 20);
		//
		// // If the Out variable is too many and exceed input variable, extend
		// // the LastPositionY
		// if (outVariable.getPosition().getY() > super.getLastPosition()
		// .getY()) {
		// super.adjustLastPositionY(Constant.BlockANDHeightUnit
		// .intValue());
		//
		// }
		//
		// }

		super.adjustLastPositionY(Constant.BlockANDHeightUnit.intValue() * 2);

	}

	// return a list of negated Var for all CCs, exception the input one
	private VarList getExclusiveConditions(List<String> CCNameList, String cc) {

		VarList otherCCs = VarList.getInstance();

		for (String ccName : CCNameList) {

			if (!ccName.equals(cc)) {

				Var ccI = getCCVar(ccName);
				ccI.setNegated();

				otherCCs.append(ccI);
			}
		}

		return otherCCs;

	}

	// return a list of negated Var for all CCs, except the input one
	private VarList getAllCCsVarList() {

		VarList allCCs = VarList.getInstance();

		for (String ccName : CCNameList) {

			Var ccI = getCCVar(ccName);

			allCCs.append(ccI);

		}

		return allCCs;

	}

	public void doTest() {

	}

	// FIXME pass Cell name
	public String getCellInInitialPositionString() {

		return cellName + _INIT_POSITION;

	}

	// FIXME pass Cell name
	public String getCellModeInAuto() {

		return cellName + _MODE_AUTO;

	}

	public FBCallingVars getMachineStateVars(String machine, String equipment,
			String state) {

		FBCallingVars fbCallingVars = null;

		StateQuery stateQuery = new StateQuery();

		stateQuery.setMachine(machine);
		stateQuery.setState(state);

		// FIXME in the interlock xml file, the equipment name is not
		// unique yet. Need to put machine name at the beginning.
		stateQuery.setEquipmentEntityName(machine + equipment);

		stateQuery.setEquipmentEntityType(SensorValue.class);

		fbCallingVars = Builder.equipmentStateLookUp
				.getFBCallingVars(stateQuery);

		return fbCallingVars;

	}

	public static void main(String[] args) {

	}

}
