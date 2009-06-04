package org.supremica.external.operationframeworkto61131.builder.cop;

import java.util.List;

import org.supremica.external.operationframeworkto61131.controlinfo.EquipmentStateLookUp;
import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.FBCallingVarsList;
import org.supremica.external.operationframeworkto61131.data.StateQuery;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.manufacturingtables.xsd.rop.*;

/**
 * COPFBCallingVarsListBuilder.java Build the FBCallingVarsList from COP'a
 * Activity for COPBuilder to SFC
 * 
 * Created: Mar 31, 2009 5:06:58 PM
 * 
 * @author LC
 * @version 1.0
 */
public class COPFBCallingVarsListBuilder {

	private EquipmentStateLookUp equipmentStateLookUp;

	private org.supremica.manufacturingtables.xsd.rop.ROP rop;

	private Activity lastActivity;

	private int sizeOfActivityList = 0;

	private List<Activity> activityList;

	public COPFBCallingVarsListBuilder(
			org.supremica.manufacturingtables.xsd.rop.ROP rop,
			EquipmentStateLookUp equipmentStateLookUp) {

		this.rop = rop;
		this.activityList = rop.getRelation().getActivity();
		this.equipmentStateLookUp = equipmentStateLookUp;
		this.sizeOfActivityList = activityList.size();
	}

	public FBCallingVarsList getPreconditionVarList(int indexOfActivity) {

		FBCallingVarsList preconditionVarList = new FBCallingVarsList();
		Activity activity = null;

		if (indexOfActivity < sizeOfActivityList) {

			activity = this.activityList.get(indexOfActivity);
		}

		if (indexOfActivity == 0) {
			// add the COP_Start transition at the beginning
			FBCallingVars allCOPsStartVar = generateAllCOPsStartVar(rop
					.getMachine());
			preconditionVarList.append(allCOPsStartVar);
		}

		// Add the precondition operation names to feedback var list. They will
		// be converted to transition.
		if (activity != null && activity.getPrecondition() != null) {

			for (OperationReferenceType predecessor : activity
					.getPrecondition().getPredecessor()) {

				FBCallingVars preconditionCallingVars = generateCopPreconditionVar(
						predecessor.getOperation().toString(), predecessor
								.getMachine(), this.equipmentStateLookUp);

				// add the Var to VarList
				preconditionVarList.append(preconditionCallingVars);

			}

		}
		// add the op_end var of last Activity's operation to next Activity's
		// transition
		// FIXME really needed?

		if (this.lastActivity != null && lastActivity.getOperation() != null) {

			FBCallingVars preconditionCallingVars = generateCopPreconditionVar(
					lastActivity.getOperation().toString(), this.rop
							.getMachine(), this.equipmentStateLookUp);

			// add the Var to VarList
			preconditionVarList.append(preconditionCallingVars);
		}

		this.lastActivity = activity;

		return preconditionVarList;
	}

	public FBCallingVarsList getCOPOperationVarList(int indexOfActivity) {

		FBCallingVarsList operationVarList = new FBCallingVarsList();

		Activity activity = null;
		if (indexOfActivity < sizeOfActivityList) {

			activity = this.activityList.get(indexOfActivity);
			// proecess operation in activity
			FBCallingVars operationCallingVars = generateCOPOperationVar(
					activity.getOperation().toString(), rop.getMachine(),
					this.equipmentStateLookUp);
			operationVarList.append(operationCallingVars);
		} else if (indexOfActivity == sizeOfActivityList) {

			// To add the COP done action block at the end of SFC

			FBCallingVars copDoneVar = generateCopDoneVar(rop.getId(), rop
					.getMachine());
			operationVarList.append(copDoneVar);

		}

		return operationVarList;
	}

	public static FBCallingVars generateCOPOperationVar(String opId,
			String machine, EquipmentStateLookUp equipmentStateLookUp) {

		// proecess operation in activity
		FBCallingVars operationCallingVars = new FBCallingVars();

		// For machine with operating system, the expected value is a
		// operation start variable

		// if (equipmentStateLookUp.hasOwnSystem(machine)) {
		// FIXME: Need a different name for action of one step of COP ?
		if (false) {

			StateQuery stateQuery = new StateQuery();

			stateQuery.setMachine(machine);
			stateQuery.setState(opId);

			// FIXME in the interlock xml file, the equipment name is not
			// unique yet. Need to put machine name at the beginning.
			stateQuery.setEquipmentEntityName(machine);

			stateQuery.setEquipmentEntityType(null);

			operationCallingVars = equipmentStateLookUp
					.getFBCallingVars(stateQuery);

			// This variable will be set be COP and act as the starting
			// condition for a EOP which will book zones for robot.
			// Here add a post fix to the machine operation variable
			// "C1R1_Op_20_exe"+"_start"
			Var operationRequestVar_fixed = new Var(operationCallingVars
					.getRequestVar().getName()
					+ "_start", Boolean.FALSE);

			operationCallingVars.setRequestVar(operationRequestVar_fixed);

		} else {

			// Set feedback to be null and will be ignored when being converted
			// to
			// SFC.Transition
			// operationCallingVars.setFeedbackVar(null);
			// Set operation's name to orderVar

			String op_action_expression = Constant.SFC_VAR_NAME_PREFIX_OP
					+ opId + Constant.SFC_VAR_NAME_POSTFIX_START;
			Var operationRequestVar = new Var(op_action_expression,
					Boolean.FALSE);

			operationCallingVars.setFeedbackVar(operationRequestVar);
			operationCallingVars.setRequestVar(operationRequestVar);
			operationCallingVars.setFeedbackVar(operationRequestVar);
			operationCallingVars.setEquipmentEntity(machine);
			operationCallingVars.setTargetState(Boolean.TRUE.toString());

		}

		return operationCallingVars;
	}

	public static FBCallingVars generateCopPreconditionVar(String opId,
			String machine, EquipmentStateLookUp equipmentStateLookUp) {

		// Set orderVar to be null and will be ignored when being
		// converted to SFC.Action
		// preconditionCallingVars.setOrderVar(null);
		// Set operation's name to feedback var
		// FIXME move 'Op' and '_end' to Constant and config.xml
		FBCallingVars preconditionCallingVars = new FBCallingVars();

		// if (equipmentStateLookUp.hasOwnSystem(machine)) {
		// FIXME: Need a different name for transition of one step of COP?
		if (false) {

			StateQuery stateQuery = new StateQuery();

			stateQuery.setMachine(machine);
			stateQuery.setState(opId);

			// FIXME in the interlock xml file, the equipment name is not
			// unique yet. Need to put machine name at the beginning.
			stateQuery.setEquipmentEntityName(machine);

			stateQuery.setEquipmentEntityType(null);

			preconditionCallingVars = equipmentStateLookUp
					.getFBCallingVars(stateQuery);

		} else {

			String op_end_expression = Constant.SFC_VAR_NAME_PREFIX_OP + opId
					+ Constant.SFC_VAR_NAME_POSTFIX_END;
			Var preconditoinFeedbackVar = new Var(op_end_expression,
					Boolean.FALSE);

			preconditionCallingVars.setFeedbackVar(preconditoinFeedbackVar);
			preconditionCallingVars.setRequestVar(preconditoinFeedbackVar);
			preconditionCallingVars.setTargetState(Boolean.TRUE.toString());
			// Set machine's name to equipment entity. May not be used when
			// being converted to SFC
			preconditionCallingVars.setEquipmentEntity(machine);

		}

		return preconditionCallingVars;

	}

	public static FBCallingVars generateCopDoneVar(String copId, String machine) {

		FBCallingVars copDoneVars = new FBCallingVars();
		Var copDoneFeedbackVar = new Var("COP_" + copId + "_done",
				Boolean.FALSE);
		copDoneVars.setFeedbackVar(copDoneFeedbackVar);
		copDoneVars.setRequestVar(copDoneFeedbackVar);
		copDoneVars.setTargetState(Boolean.TRUE.toString());
		// Set machine's name to equipment entity. May not be used when
		// being converted to SFC
		if (machine != null) {
			copDoneVars.setEquipmentEntity(machine);
		}
		return copDoneVars;

	}

	public static FBCallingVars generateAllCOPsStartVar(String machine) {

		FBCallingVars copStartVars = new FBCallingVars();
		Var copStartFeedbackVar = new Var("Start_COPs", Boolean.FALSE);
		copStartVars.setFeedbackVar(copStartFeedbackVar);
		copStartVars.setRequestVar(copStartFeedbackVar);
		copStartVars.setTargetState(Boolean.TRUE.toString());
		// Set machine's name to equipment entity. May not be used when
		// being converted to SFC
		if (machine != null) {
			copStartVars.setEquipmentEntity(machine);
		}

		return copStartVars;

	}

	public FBCallingVarsList getAllCOPDoneVarList() {

		FBCallingVarsList operationVarList = new FBCallingVarsList();

		for (String cc : rop.getCC()) {

			FBCallingVars allCopDoneCCVars = new FBCallingVars();
			Var copDoneCCFeedbackVar = generateALLCOPDoneVar(cc);
			allCopDoneCCVars.setFeedbackVar(copDoneCCFeedbackVar);
			allCopDoneCCVars.setRequestVar(copDoneCCFeedbackVar);
			allCopDoneCCVars.setTargetState(Boolean.FALSE.toString());
			// Set machine's name to equipment entity. May not be used when
			// being converted to SFC
			allCopDoneCCVars.setEquipmentEntity(rop.getMachine());

			operationVarList.append(allCopDoneCCVars);
		}

		return operationVarList;
	}

	public static Var generateALLCOPDoneVar(String cc) {

		Var copDoneCCFeedbackVar = new Var("All_COP_Done_CC_" + cc,
				Boolean.FALSE);

		return copDoneCCFeedbackVar;

	}

}
