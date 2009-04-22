package org.supremica.external.operationframeworkto61131.builder.eop;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;

import org.plcopen.xml.tc6.Body;
import org.plcopen.xml.tc6.PouType;
import org.plcopen.xml.tc6.Project;
import org.supremica.external.operationframeworkto61131.builder.FBCallingQueryBuilder;
import org.supremica.external.operationframeworkto61131.builder.PouInterfaceBuilder;
import org.supremica.external.operationframeworkto61131.builder.cop.COPFBCallingVarsListBuilder;
import org.supremica.external.operationframeworkto61131.builder.sfc.ActionBuilder;
import org.supremica.external.operationframeworkto61131.builder.sfc.ConditionBuilder;
import org.supremica.external.operationframeworkto61131.builder.sfc.StepBuilder;
import org.supremica.external.operationframeworkto61131.controlinfo.EquipmentStateLookUp;
import org.supremica.external.operationframeworkto61131.data.FBCallingQuery;
import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.FBCallingVarsList;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.data.VarList;
import org.supremica.external.operationframeworkto61131.layout.common.*;
import org.supremica.external.operationframeworkto61131.layout.sfc.*;
import org.supremica.external.operationframeworkto61131.layout.sfc.block.Block;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.manufacturingtables.xsd.eop.Action;
import org.supremica.manufacturingtables.xsd.eop.EOP;
import org.supremica.manufacturingtables.xsd.eop.InitialState;
import org.supremica.manufacturingtables.xsd.eop.Operation;
import org.supremica.manufacturingtables.xsd.eop.TypeType;

/**
 * EOPBuilder.java convert EOP to SFC
 * 
 * Created: Mar 31, 2009 5:13:23 PM
 * 
 * @author LC
 * @version 1.0
 */
public class EOPBuilder extends
		org.supremica.external.operationframeworkto61131.builder.Builder {

	/*
	 * The variable list for machine with control system to communicate with PLC
	 * For EOP, it should contains variable that meets: 1.The EOP is for machine
	 * with control system. 2.Operation.Type=alternative 3.All the variable
	 * represent components states in InitialState
	 */

	private VarList externalVarList = VarList.getInstance();
	/*
	 * 
	 * This map is used to remove duplicate Action in SFC for EOP. Visited
	 * equipment's state will be added to the map and ignored next time when
	 * it's visited again. If another state other than the state stored in the
	 * map of the same equipment is visited, the equipment's stored state will
	 * be updated
	 * 
	 * Key: equipment entity name Value: state name.
	 */
	private HashMap<String, String> varMap = new HashMap<String, String>(20);;

	private Operation operation;

	private int numOfLastTransitionInputs = 1;

	private int nStep = 1;

	public EOPBuilder() {

		super();
	}

	public Project.Types.Pous.Pou convertOperationToPou(Operation operation,
			Project.Types.Pous.Pou pou) {
		this.operation = operation;
		// this.pou = pou;

		pou.setPouType(PouType.PROGRAM);

		Body.SFC sfc = CommonLayoutObject.objectFactory.createBodySFC();
		// SFC live list , add action,step,transition to it.
		List<Object> SFCLiveList = sfc.getCommentOrErrorOrConnector();

		log.debug("-----------------------EOP:"
				+ operation.getOpID().toString());
		log.debug("=================Machine:" + operation.getMachine());
		log.debug("-------Initial State");
		// process InitialState
		Transition transition = proecessEOPInitialState(SFCLiveList);

		// process Action
		log.debug("-------Actions");
		int i = 1;
		for (Action action : operation.getEOP().getAction()) {

			log.debug("---Action:" + i++);
			transition = processAction(SFCLiveList, action, transition);

		}

		log.debug(" transition localId:" + transition.getLocalId());

		// String comment=operation.getComment();
		// TODO: add comment, type type=alternative, init step might be
		// different.

		// process the last step, add op_end action
		processLastStep(SFCLiveList, transition);

		// add Vars to pou.interface, declared as external.
		Project.Types.Pous.Pou.Interface pouInterface = PouInterfaceBuilder
				.generatePouInterfaceFromVarList(super.getInterfaceVarList(),
						null);

		pou.setInterface(pouInterface);
		Body body = CommonLayoutObject.objectFactory.createBody();
		body.setSFC(sfc);
		pou.setBody(body);

		log.info("Generated EOP pou:" + pou.getName());
		return pou;
	}

	/*
	 * 
	 * Alternative EOP use op_start and initial state together as the first
	 * transition condition, if op_start is null , it will be a normal initial
	 * state check. The returned object is a transition connecting AND block
	 * with initial state variable names as input.
	 */

	public Transition proecessEOPInitialState(List<Object> SFCLiveList) {

		FBCallingVarsList preconditionVarsList = getEOPInitalStateFBCallingVarsList(
				operation, equipmentStateLookUp);

		/*
		 * As the initial state is at beginning of SFC, the following function
		 * doesn't really remove any variable but adds variables in initial
		 * state to the variable state list to prevent duplicate variable in the
		 * Action next to the first transition.
		 */
		preconditionVarsList = removeDuplicateCallingVars(preconditionVarsList);

		// sfcUtil.displayQueryList(queryList);
		// plcopenUtil.displayCallingVarList(callingVarsList);

		// generate initial Step
		Step initStep = new Step(super.nextLocalId());
		initStep.setPosition(Constant.InitStepPositionX.intValue(),
				Constant.InitStepPositionY.intValue());
		initStep.setInitialStep();
		initStep.setName(Constant.InitStepName);

		// add step to SFC
		SFCLiveList.add(initStep.getPLCOpenObject());

		// Generate a transition following the initial step. The transition is
		// the start condition of the EOP.

		Transition initTransition = new Transition(super.nextLocalId());

		// Connect transition to initStep
		initTransition.connectToOut(initStep, new Position(0,
				Constant.DistanceStepToTransition.intValue()));

		// create a Var object for Op_start,
		// FIXME when it's a robot, different var name

		Var op_start_var = getOPStartCallingVar(operation.getOpID().toString());
		/*
		 * Different initial state according to EOP type: alternative or basic
		 * Basic EOP start with Op_start as first transition condition. The
		 * variable is connected to the transition directly, there is no block
		 * between them.
		 */

		InVariable op_startInVariable = new InVariable(super.nextLocalId(),
				op_start_var.getName());
		// The first transition contains only op_start, no precondition
		if (operation.getType().equals(TypeType.BASIC)
				|| preconditionVarsList.getFBCallingVarsList().size() == 0) {

			numOfLastTransitionInputs = 1;
			super.addToInterfaceVarList(op_start_var);

			// Generate condition for initial step

			// Link condition to transition
			op_startInVariable.connectToIn(initTransition, initTransition
					.getCondition(),
					ConditionBuilder.distanceConditionToTransition);
			// add op_startInVariable and transition to SFC
			SFCLiveList.add(op_startInVariable.getPLCOpenObject());
			SFCLiveList.add(initTransition.getPLCOpenObject());

		} else {
			/*
			 * Alternative EOP use op_start and initial state together as the
			 * first transition condition.
			 */

			/*
			 * this is used to decide the length from transition to the next
			 * step
			 */

			numOfLastTransitionInputs = preconditionVarsList
					.getFBCallingVarsList().size() + 1;

			// One additional condition for op_start
			ArrayList<CommonConnectionIn> conInList = new ArrayList<CommonConnectionIn>(
					1);
			conInList.add(new CommonConnectionIn());
			Block blockAND = ConditionBuilder.generateConditions(SFCLiveList,
					preconditionVarsList,
					ConditionBuilder.distanceConditionToTransition,
					initTransition, conInList, AND, this);
			// Link condition to the return AND's last input
			op_startInVariable.connectToIn(blockAND, conInList.get(0),
					ConditionBuilder.distanceConditionToTransition);

			SFCLiveList.add(blockAND.getPLCOpenObject());
			SFCLiveList.add(op_startInVariable.getPLCOpenObject());

			// process variable list for machine with control system
			if (equipmentStateLookUp.hasOwnSystem(operation.getMachine())) {

				this.externalVarList
						.append(getEOPInitalStateExternalComponentFBCallingVarsList(
								operation, equipmentStateLookUp)
								.getFeedbackVarList());

			}

		}

		// pass the transition as a starting point to the next generated step.
		// this is step S1

		Step stepS1 = StepBuilder.generateStep(super.nextLocalId(),
				initTransition, this.getNextStepName(),
				getDistanceStepToTransitionY());

		stepS1.setConnectionPointOutAction(null);

		SFCLiveList.add(stepS1.getPLCOpenObject());

		// FIXME this step has no action. Or add empty action?
		// Body.SFC.ActionBlock emptyActionBlock = ActionBuilder
		// .getActionBlockFromStep(stepS1, actionBlockHeight,
		// getNextLocalID(), distanceActionBlockToStep);
		// emptyActionBlock = ActionBuilder
		// .addEmptyActionToActionBlock(emptyActionBlock);
		// SFCLiveList.add(emptyActionBlock);

		// FIXME when there is no transition, add TRUE variable?

		// this is transition connected to step S1

		Transition transitionS1 = new Transition(super.nextLocalId());

		transitionS1.connectToOut(stepS1, new Position(0,
				Constant.DistanceStepToTransition.intValue()));

		// do the initial state check again, with op_start null

		ConditionBuilder.generateConditions(SFCLiveList, preconditionVarsList,
				ConditionBuilder.distanceConditionToTransition, transitionS1,
				null, AND, this);

		numOfLastTransitionInputs = preconditionVarsList.getFBCallingVarsList()
				.size();

		return transitionS1;

	}

	/*
	 * 
	 * process EOP's action,
	 */

	private Transition processAction(List<Object> SFCLiveList, Action action,
			Transition transition) {

		FBCallingQuery queryList = FBCallingQuery.getInstance();

		FBCallingQueryBuilder queryListBuilder = new FBCallingQueryBuilder();

		String machine = operation.getMachine();
		// FIXME the location of YES or NO should be more adaptive.

		// Yes!=YES
		if (((org.supremica.manufacturingtables.xsd.controlInformation.Machine) equipmentStateLookUp
				.getMachine(machine))
				.getHasOwnControlSystem()
				.value()
				.toString()
				.equalsIgnoreCase(
						org.supremica.manufacturingtables.xsd.physicalResource.ControlSystemType.YES
								.toString())) {
			if (action.getActuatorValue().isEmpty()
					&& action.getSensorValue().isEmpty()
					&& action.getVariableValue().isEmpty()
					&& action.getZoneState().isEmpty()) {
				// the operation for robot or machine with own control system
				queryList.append(queryListBuilder.processMachineOperation(
						this.operation.getOpID(), machine));

			}

		}

		queryList.append(queryListBuilder.processActuatorValues(action
				.getActuatorValue(), machine));
		queryList.append(queryListBuilder.processSensorValues(action
				.getSensorValue(), machine));

		queryList.append(queryListBuilder.processVariableValues(action
				.getVariableValue(), machine));

		queryList.append(queryListBuilder.processZoneStates(action
				.getZoneState(), machine));

		queryList.removeStateWithValue(Constant.STATE_IGNORE_SIGN);

		FBCallingVarsList operationVarList = equipmentStateLookUp
				.getFBCallingVars(queryList);

		// sfcUtil.displayQueryList(queryList);
		// plcopenUtil.displayCallingVarList(callingVarsList);

		Step newStep = StepBuilder.generateStep(super.nextLocalId(),
				transition, this.getNextStepName(),
				getDistanceStepToTransitionY());

		SFCLiveList.add(newStep.getPLCOpenObject());
		numOfLastTransitionInputs = operationVarList.getFBCallingVarsList()
				.size();
		ActionBuilder.generateActions(SFCLiveList, this
				.removeDuplicateCallingVars(operationVarList),
				ConditionBuilder.distanceActionBlockToStep, newStep, this);

		Transition newTransition = new Transition(super.nextLocalId());

		// FIXME extend the length by numOfLastAction/numOfLastCondition
		newTransition.connectToOut(newStep, new Position(0,
				Constant.DistanceStepToTransition.intValue()));

		ConditionBuilder.generateConditions(SFCLiveList, operationVarList,
				ConditionBuilder.distanceConditionToTransition, newTransition,
				null, AND, this);

		return newTransition;
	}

	private void processLastStep(List<Object> SFCLiveList, Transition transition) {

		FBCallingVarsList callingVarsList = new FBCallingVarsList();

		Var op_end_var = getOPEndCallingVar(operation.getOpID().toString());

		FBCallingVars op_end = new FBCallingVars();
		op_end.setRequestVar(op_end_var);
		op_end.setFeedbackVar(op_end_var);
		op_end.setTargetState(Boolean.TRUE.toString());

		callingVarsList.append(op_end);

		Step lastStep = StepBuilder.generateStep(super.nextLocalId(),
				transition, this.getNextStepName(),
				getDistanceStepToTransitionY());

		SFCLiveList.add(lastStep.getPLCOpenObject());

		ActionBuilder.generateActions(SFCLiveList, callingVarsList,
				ConditionBuilder.distanceActionBlockToStep, lastStep, this);

		JumpStep jumpStep = StepBuilder.generateJumpStep(super.nextLocalId(),
				lastStep, Constant.InitStepName, new Position(0,
						Constant.DistanceStepToJumpStep.intValue()));

		SFCLiveList.add(jumpStep.getPLCOpenObject());

		super.addToInterfaceVarList(op_end.getRequestVar());

	}

	private String getNextStepName() {

		return Constant.STEP_PREFIX + nStep++;
	}

	private Var getOPStartCallingVar(String opId) {

		// The starting condition of EOP is the variable to be set be a COP
		// action

		Var op_start_var = COPFBCallingVarsListBuilder.generateCOPOperationVar(
				opId, operation.getMachine(), equipmentStateLookUp)
				.getRequestVar();

		return op_start_var;
	}

	private Var getOPEndCallingVar(String opId) {

		Var op_end_var = COPFBCallingVarsListBuilder
				.generateCopPreconditionVar(opId, operation.getMachine(),
						equipmentStateLookUp).getFeedbackVar();

		String op_end_expression = Constant.SFC_VAR_NAME_PREFIX_OP
				+ operation.getOpID() + Constant.SFC_VAR_NAME_POSTFIX_END;
		Var op_end_feedbackVar = new Var(op_end_expression, Boolean.FALSE);
		FBCallingVars op_end = new FBCallingVars();
		op_end.setRequestVar(op_end_feedbackVar);
		op_end.setFeedbackVar(op_end_feedbackVar);
		op_end.setTargetState(Boolean.TRUE.toString());

		return op_end_var;
	}

	public static FBCallingVarsList getEOPInitalStateFBCallingVarsList(
			Operation operation, EquipmentStateLookUp equipmentStateLookUp) {

		String machine = operation.getMachine();

		FBCallingVarsList callingVarsList = new FBCallingVarsList();

		FBCallingQuery queryList = FBCallingQuery.getInstance();

		FBCallingQueryBuilder queryListBuilder = new FBCallingQueryBuilder();

		EOP eop = operation.getEOP();
		InitialState initialState = eop.getInitialState();

		if (eop != null && eop.getInitialState() != null
				&& eop.getInitialState().isIsInitial() != null) {

			if (equipmentStateLookUp.hasOwnSystem(machine)
					&& eop.getInitialState().isIsInitial()) {

				/*
				 * For machine with control system. There is no need to do
				 * initial state check. Initial state check will be done by the
				 * machine it self;
				 */

				callingVarsList
						.append(org.supremica.external.operationframeworkto61131.builder.functionblock.IntelligentMachineFB
								.getMachineInInitialStateVar(machine));

			} else {

				queryList.append(queryListBuilder.processActuatorValues(
						initialState.getActuatorValue(), machine));
				queryList.append(queryListBuilder.processSensorValues(
						initialState.getSensorValue(), machine));
				queryList.append(queryListBuilder.processVariableValues(
						initialState.getVariableValue(), machine));
				queryList.append(queryListBuilder.processZoneStates(
						initialState.getZoneState(), machine));

				queryList.append(queryListBuilder
						.processExteranlComponents(initialState
								.getExternalComponentValue()));

				queryList.removeStateWithValue(Constant.STATE_IGNORE_SIGN);

				callingVarsList.append(equipmentStateLookUp
						.getFBCallingVars(queryList));
			}

		}
		return callingVarsList;

	}

	public static FBCallingVarsList getEOPInitalStateExternalComponentFBCallingVarsList(
			Operation operation, EquipmentStateLookUp equipmentStateLookUp) {

		EOP eop = operation.getEOP();
		InitialState initialState = eop.getInitialState();

		FBCallingQuery queryList = FBCallingQuery.getInstance();

		FBCallingQueryBuilder queryListBuilder = new FBCallingQueryBuilder();

		// String machine = operation.getMachine();

		queryList.append(queryListBuilder
				.processExteranlComponents(initialState
						.getExternalComponentValue()));

		queryList.removeStateWithValue(Constant.STATE_IGNORE_SIGN);

		FBCallingVarsList callingVarsList = equipmentStateLookUp
				.getFBCallingVars(queryList);

		return callingVarsList;

	}

	private Position getDistanceStepToTransitionY() {

		int distanceStepToTransitionY = Constant.DistanceStepToTransition
				.intValue()
				+ Constant.BlockANDHeightUnit.intValue()
				* (numOfLastTransitionInputs - 1);

		return new Position(0, distanceStepToTransitionY);
	}

	public VarList getExternalVarList() {
		return externalVarList;
	}

	/*
	 * Return a new FBCallingList with no duplicate action
	 */
	private FBCallingVarsList removeDuplicateCallingVars(
			FBCallingVarsList callingVarsList) {

		FBCallingVarsList reducedList = new FBCallingVarsList();

		for (FBCallingVars callingVars : callingVarsList.getFBCallingVarsList()) {

			if (!this.isDuplicateEquipState(callingVars)) {

				reducedList.append(callingVars);
			}
		}

		return reducedList;
	}

	/*
	 * Check if the value pair Key:equipmentEntityName,Value:state is visited
	 * continuous twice. If affirmative, return true.Else update the existing
	 * value pair with the new state and return false;
	 */

	private Boolean isDuplicateEquipState(FBCallingVars callingVars) {
		// log.debug("----------------Duplicate action:");
		String equipName = callingVars.getEquipmentEntity();
		String equipState = callingVars.getTargetState();

		if (varMap.containsKey(equipName)) {
			String lastEquipState = varMap.get(equipName);

			if (lastEquipState.equals(equipState)) {

				// log.debug("----------------Duplicate action:" + equipName +
				// "="
				// + equipState);
				return true;

			} else {

				varMap.remove(equipName);
				varMap.put(equipName, equipState);

				// log.debug("----------------Action updated:" + equipName + "="
				// + equipState);
				return false;

			}

		} else {
			// log.debug("----------------New Action:" + equipName + "="
			// + equipState);
			varMap.put(equipName, equipState);

			return false;
		}

	}

}
