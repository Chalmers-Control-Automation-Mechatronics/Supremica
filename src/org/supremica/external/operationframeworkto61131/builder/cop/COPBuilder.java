package org.supremica.external.operationframeworkto61131.builder.cop;

import java.util.List;
import java.util.ArrayList;
import org.plcopen.xml.tc6.Body;
import org.plcopen.xml.tc6.PouType;
import org.plcopen.xml.tc6.Project;
import org.supremica.external.operationframeworkto61131.builder.PouInterfaceBuilder;
import org.supremica.external.operationframeworkto61131.builder.sfc.ActionBuilder;
import org.supremica.external.operationframeworkto61131.builder.sfc.BlockBuilder;
import org.supremica.external.operationframeworkto61131.builder.sfc.ConditionBuilder;
import org.supremica.external.operationframeworkto61131.builder.sfc.StepBuilder;
import org.supremica.external.operationframeworkto61131.data.FBCallingVarsList;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.layout.common.*;
import org.supremica.external.operationframeworkto61131.layout.sfc.*;
import org.supremica.external.operationframeworkto61131.layout.sfc.block.Block;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.manufacturingtables.xsd.rop.Activity;
import org.supremica.manufacturingtables.xsd.rop.ROP;

/**
 * COPBuilder.java convert COP to SFC
 * 
 * Created: Mar 31, 2009 5:05:58 PM
 * 
 * @author LC
 * @version 1.0
 */
public class COPBuilder extends
		org.supremica.external.operationframeworkto61131.builder.Builder {

	private COPFBCallingVarsListBuilder copCallingVarListBuilder;

	private Position DistanceConditionToTransition = new Position(
			Constant.DistanceConditionToTransition.intValue(), 0);

	private Position DistanceActionBlockToStep = new Position(
			Constant.DistanceActionBlockToStep.intValue(), 0);

	private ROP rop;

	public COPBuilder() {

		super();

	}

	public Project.Types.Pous.Pou convertROPToPou(ROP rop,
			Project.Types.Pous.Pou pou) {

		log.debug("-----------------------ROP:" + rop.getId());
		log.debug("=================Machine:" + rop.getMachine());

		this.rop = rop;

		copCallingVarListBuilder = new COPFBCallingVarsListBuilder(rop,
				equipmentStateLookUp);

		pou.setPouType(PouType.PROGRAM);

		Body.SFC sfc = CommonLayoutObject.objectFactory.createBodySFC();

		// SFC live list , add action,step,transition to it.
		List<Object> SFCLiveList = sfc.getCommentOrErrorOrConnector();
		// Generate the init step

		Step initStep = new Step(super.nextLocalId());
		initStep.setPosition(Constant.InitStepPositionX.intValue(),
				Constant.InitStepPositionY.intValue());
		initStep.setInitialStep();
		initStep.setName(Constant.InitStepName);

		// add step to SFC
		SFCLiveList.add(initStep.getPLCOpenObject());

		Step lastStep = initStep;
		List<Activity> activityList = rop.getRelation().getActivity();

		int numOfLastCondition = 0;
		// int numOfLastAction = 0;

		super.setLastPosition(new Position(0, 0));
		int sizeOfActivityList = activityList.size();

		// int addtionalConditions = 0;
		for (int i = 0; i <= sizeOfActivityList; i++) {

			FBCallingVarsList preconditionVarList = this.copCallingVarListBuilder
					.getPreconditionVarList(i);
			FBCallingVarsList operationVarList = this.copCallingVarListBuilder
					.getCOPOperationVarList(i);

			Transition transition = new Transition(super.nextLocalId());

			// FIXME extend the length by numOfLastAction/numOfLastCondition
			transition.connectToOut(lastStep, new Position(0,
					Constant.DistanceStepToTransition.intValue()));

			if (preconditionVarList != null
					&& preconditionVarList.getFBCallingVarsList().size() > 0) {

				numOfLastCondition = preconditionVarList.getFBCallingVarsList()
						.size();
				if (i == 0) {
					// when it's the first transition, one more connection is
					// neededfor CC.The extra connection could be used for one
					// CC or several connected by a AND block

					// One addtional Conditions for CC;

					ArrayList<CommonConnectionIn> conInList = new ArrayList<CommonConnectionIn>(
							1);
					conInList.add(new CommonConnectionIn());
					Block blockAND = ConditionBuilder.generateConditions(
							SFCLiveList, preconditionVarList,
							this.DistanceConditionToTransition, transition,
							conInList, AND, this);

					// this.generateConditions(SFCLiveList,
					// preconditionVarList,
					// , transition,
					// true, );

					if (blockAND != null) {

						// one or more condition, connect cc to AND's last param
						generateCC(SFCLiveList,
								this.DistanceConditionToTransition, blockAND,
								conInList.get(0));

						SFCLiveList.add(blockAND.getPLCOpenObject());
					} else {
						// no condition, connect the cc to transition directly
						generateCC(SFCLiveList,
								this.DistanceConditionToTransition, transition,
								conInList.get(0));
						SFCLiveList.add(transition.getPLCOpenObject());

					}

					numOfLastCondition = numOfLastCondition
							+ rop.getCC().size();

				} else {

					// SFCBuilder.generateConditions(SFCLiveList,
					// preconditionVarList,
					// this.DistanceConditionToTransition, transition,
					// 0, this.AND);
					// No additional Conditions;
					ConditionBuilder.generateConditions(SFCLiveList,
							preconditionVarList,
							this.DistanceConditionToTransition, transition,
							null, AND, this);

				}

			} else {

				// empty condition, generate a true variable
				// FIXME a TRUE variable may be needed.

			}

			// Step step2 = new Step(super.nextLocalId());
			// step2.setName(Constant.STEP_PREFIX + i + 1);
			int distanceStepToTransitionY = Constant.DistanceStepToTransition
					.intValue()
					+ Constant.StepToTransitionExtendUnit.intValue()
					* numOfLastCondition;
			//
			// step2.connectToOut(transition, new Position(0,
			// distanceStepToTransitionY));

			Step step = StepBuilder.generateStep(super.nextLocalId(),
					transition, Constant.STEP_PREFIX + (i + 1), new Position(0,
							distanceStepToTransitionY));

			SFCLiveList.add(step.getPLCOpenObject());

			ActionBuilder.generateActions(SFCLiveList, operationVarList,
					this.DistanceActionBlockToStep, step, this);

			// numOfLastAction = operationVarList.getFBCallingVarsList().size();

			lastStep = step;

		}
		// end of activity list;

		// add all cop done transition;
		Position distance = new Position(Constant.DistanceConditionToTransition
				.intValue(), 0);
		Transition transition = new Transition(super.nextLocalId());

		transition.connectToOut(lastStep, new Position(0,
				Constant.DistanceStepToTransition.intValue()));
		FBCallingVarsList getAllCOPDoneVarList = copCallingVarListBuilder
				.getAllCOPDoneVarList();

		ConditionBuilder.generateConditions(SFCLiveList, getAllCOPDoneVarList,
				distance, transition, null, OR, this);

		// FIXME distance?
		JumpStep jumpStep = StepBuilder.generateJumpStep(super.nextLocalId(),
				transition, Constant.InitStepName, new Position(0,
						Constant.DistanceStepToJumpStep.intValue()));

		SFCLiveList.add(jumpStep.getPLCOpenObject());

		Project.Types.Pous.Pou.Interface pouInterface = PouInterfaceBuilder
				.generatePouInterfaceFromVarList(super.getInterfaceVarList(),
						null);

		pou.setInterface(pouInterface);

		Body body = CommonLayoutObject.objectFactory.createBody();
		body.setSFC(sfc);
		pou.setBody(body);

		return pou;
	}

	private Object generateCC(List<Object> SFCLiveList, Position distance,
			CommonLayoutObject connector, CommonConnectionIn conIn) {
		List<String> ccList = rop.getCC();
		Position ccDistance = new Position(distance);

		// more than one cc, add a AND
		if (ccList.size() > 1) {

			Block blockOR = BlockBuilder.getORBlock(super.nextLocalId(), ccList
					.size());

			// connect cc's OR to connector's last conOut

			if (!super.getLastPosition().equals(new Position(0, 0))) {

				blockOR.setPosition(super.getLastPosition());

				Position lastPosition = blockOR.getPosition();

				lastPosition.addY(blockOR.getHeight()
						+ Constant.BlockANDHeightUnit.intValue());

				super.setLastPosition(lastPosition);

				ccDistance = null;

				connector.connectToOut(blockOR, 0, conIn, null, null);

			} else {

				blockOR.connectToIn(connector, conIn, ccDistance);
			}

			int n = 0;
			for (String cc : ccList) {

				InVariable ccInVariable = new InVariable(super.nextLocalId(),
						"CC_" + cc);

				super.addToInterfaceVarList(new Var(ccInVariable
						.getExpression(), false));
				ccInVariable.connectToIn(blockOR, blockOR
						.getConnectionPointIn(n++), distance);

				SFCLiveList.add(ccInVariable.getPLCOpenObject());

			}

			SFCLiveList.add(blockOR.getPLCOpenObject());

			return blockOR;
		} else if (ccList.size() == 1) {

			// CC should not be empty
			InVariable ccinVariable = new InVariable(super.nextLocalId(), "CC_"
					+ ccList.get(0));
			super.addToInterfaceVarList(new Var(ccinVariable.getExpression(),
					false));
			// connect cc to connector's last conOut

			ccinVariable.connectToIn(connector, connector
					.getConnectionPointIn(connector.getConnectionPointInList()
							.size() - 1), ccDistance);

			SFCLiveList.add(ccinVariable.getPLCOpenObject());

			return ccinVariable;
		} else {

			log.error("empty cc in cop:" + rop.getId());
			return null;
		}

	}

	public void doTest() {
		// convertor.util.LogUtil log = convertor.util.LogUtil.getInstance();

		Step initStep = new Step(super.nextLocalId());
		initStep.setPosition(100, 100);
		initStep.setInitialStep();

		Step lastStep = initStep;

		Transition transition = new Transition(super.nextLocalId());

		Position stepToTransition = new Position(0, 10);
		transition.connectToOut(lastStep, stepToTransition);

		Block blockAND = BlockBuilder.getANDBlock(super.nextLocalId(), 4);

		Position distance = new Position(20, 0);
		blockAND.connectToIn(transition, transition.getCondition(), distance);

		InVariable inVar = new InVariable(super.nextLocalId(), "haha");
		InVariable inVar2 = new InVariable(super.nextLocalId(), "hehe");

		inVar.connectToIn(blockAND, blockAND.getConnectionPointIn(0),
				new Position(30, 0));
		inVar2.connectToIn(blockAND, blockAND.getConnectionPointIn(1),
				new Position(30, 0));

		org.supremica.external.operationframeworkto61131.util.DebugUtil
				.printCommonLayoutObject(initStep);
		org.supremica.external.operationframeworkto61131.util.DebugUtil
				.printCommonLayoutObject(transition);

		org.supremica.external.operationframeworkto61131.util.DebugUtil
				.printCommonLayoutObject(blockAND);

		org.supremica.external.operationframeworkto61131.util.DebugUtil
				.printCommentConnectionIn(transition.getCondition());

		org.supremica.external.operationframeworkto61131.util.DebugUtil
				.printCommonLayoutObject(inVar);
		org.supremica.external.operationframeworkto61131.util.DebugUtil
				.printCommonLayoutObject(inVar2);
	}

	private void testDivergenceNConvergence(List<Object> SFCLiveList) {

		
		
		
		
	}

	public static void main(String[] args) {

		// convertor.util.DebugUtil debuger = new convertor.util.DebugUtil();

		// COPBuilder co = new COPBuilder();

		// co.doTest();

	}

}
