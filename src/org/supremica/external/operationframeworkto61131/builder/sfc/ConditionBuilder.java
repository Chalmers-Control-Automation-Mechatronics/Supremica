package org.supremica.external.operationframeworkto61131.builder.sfc;

import java.util.List;

import java.util.ArrayList;

import org.supremica.external.operationframeworkto61131.builder.Builder;
import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.FBCallingVarsList;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.data.VarList;
import org.supremica.external.operationframeworkto61131.layout.common.CommonConnectionIn;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.layout.sfc.InVariable;
import org.supremica.external.operationframeworkto61131.layout.sfc.Transition;
import org.supremica.external.operationframeworkto61131.layout.sfc.block.Block;
import org.supremica.external.operationframeworkto61131.main.Constant;



/**
 * ConditionBuilder.java contains method that generate SFC's condition from
 * object of type FBCallingVarsList.java.
 * 
 * Created: Mar 31, 2009 5:47:08 PM
 * 
 * @author LC
 * @version 1.0
 */
public class ConditionBuilder {

	public static final Position distanceConditionToTransition = new Position(
			Constant.DistanceConditionToTransition.intValue(), 0);

	public static final Position distanceActionBlockToStep = new Position(
			Constant.DistanceActionBlockToStep.intValue(), 0);

	public static final Position distanceStepToTransition = new Position(0,
			Constant.DistanceStepToTransition.intValue());

	//
	/*
	 * Generate condition block for SFC.With empty extra input(s)(if any) which
	 * will be connected later.
	 * 
	 * @param SFCLiveList ThePou's live list, newly generated components will be
	 * appended to it.
	 * 
	 * @param preconditionVarList The list of variables to be placed in the
	 * condition
	 * 
	 * @param distance From condition to transition.
	 * 
	 * @param transition For the condition to connect to.
	 * 
	 * @param conInList The extra connectionPointIns in the return Block will be
	 * added to it, additional variable can be connected to it after returned.
	 * It's size is used to decide the block's inputs number.
	 * 
	 * @param blockType The type of the block when there are more than one
	 * variables
	 * 
	 * @param builder The super class ,Builder, it handles localId and variable
	 * list.
	 * 
	 * @return The newly generated block(AND/OR) connected to input transition
	 */

	public static Block generateConditions(List<Object> SFCLiveList,
			FBCallingVarsList preconditionVarList, Position distance,
			Transition transition, ArrayList<CommonConnectionIn> conInList,
			String blockType, Builder builder) {
		int nOfadditions = 0;

		if (conInList != null) {
			nOfadditions = conInList.size();
		}

		int nOfconditions = preconditionVarList.getFBCallingVarsList().size();

		int inputs = nOfconditions + nOfadditions;

		if (inputs == 0) {
			// No condition, add a true variable to transition

			FBCallingVars varTRUE = org.supremica.external.operationframeworkto61131.util.PLCopenUtil
					.getTRUECallingVar();
			preconditionVarList.append(varTRUE);
			inputs = 1;
			nOfconditions = 1;

		}

		if (inputs > 1) {// More than one variables, generate a AND block

			Block blockANDorOR;

			// decide the block type
			if (blockType.equals(Builder.AND)) {
				blockANDorOR = BlockBuilder.getANDBlock(builder.nextLocalId(),
						inputs);

			} else {

				blockANDorOR = BlockBuilder.getORBlock(builder.nextLocalId(),
						inputs);
			}

			// blockAND.connectTo(transition);

			blockANDorOR.connectToIn(transition, transition.getCondition(),
					distance);

			// Add transition to SFC
			SFCLiveList.add(transition.getPLCOpenObject());

			// Connect variables to block AND
			for (int j = 0; j < nOfconditions; j++) {

				Var inVar = preconditionVarList.getFBCallingVarsList().get(j)
						.getFeedbackVar();
				builder.addToInterfaceVarList(inVar);

				InVariable inVariable = new InVariable(builder.nextLocalId(),
						inVar.getName());
				if (inVar.isNegated()) {
					inVariable.setNegated();
				}

				inVariable.connectToIn(blockANDorOR, blockANDorOR
						.getConnectionPointIn(j), distance);

				// Add each variable to SFC
				SFCLiveList.add(inVariable.getPLCOpenObject());

				// Set the builder.last position to be the last variable's
				// position.It will be used by the extra conditions(if any) or
				// the next transition's condition
				if (j == nOfconditions - 1) {

					Position lastPosition = inVariable.getPosition();
					lastPosition.addY(inVariable.getHeight()
							+ Constant.BlockANDHeightUnit.intValue());
					builder.setLastPosition(lastPosition);
				}

			}

			// Add the block's extra input's to the input conInList.
			int i = 0;
			for (int j = nOfconditions; j < inputs; j++) {

				conInList.set(i, blockANDorOR.getConnectionPointIn(j));
				i++;
			}

			// Decide whether to add the blockANDorOR to SFC
			if (nOfadditions > 0) {
				// extra variables still need to be connected, add blockANDotOR
				// to SFCLive list later after the variables is
				// connected(block's connectionPointIn will be changed)

				return blockANDorOR;
			} else {
				// when there is no addtional variable to be connected, the
				// blockANDorOR is not needed to be returned, add it to
				// SFCLiveList.
				SFCLiveList.add(blockANDorOR.getPLCOpenObject());

				return null;

			}

		} else if (inputs == 1) {

			// When there is only one to variable to be connected to transition

			if (nOfconditions == 1) {

				// There is only one condition, connect it to transition
				// directly
				Var inVar = preconditionVarList.getFeedbackVarList().getVars()
						.get(0);

				builder.addToInterfaceVarList(inVar);
				InVariable inVariable = new InVariable(builder.nextLocalId(),
						inVar.getName());

				// Negated variable will be connected to block NOT first then to
				// transition or connect to transition directly
				if (inVar.isNegated()) {

					Block blockNOT = BlockBuilder.getNOTBlock(builder
							.nextLocalId());

					blockNOT.connectToIn(transition, transition.getCondition(),
							distance);

					inVariable.connectToIn(blockNOT, blockNOT
							.getConnectionPointIn(), distance);

					// add the block NOT to SFCLive list
					SFCLiveList.add(blockNOT.getPLCOpenObject());

				} else {

					inVariable.connectToIn(transition, transition
							.getCondition(), distance);

				}

				// add the variable to SFCLive list
				SFCLiveList.add(inVariable.getPLCOpenObject());

				// add the transition to SFCLive list
				SFCLiveList.add(transition.getPLCOpenObject());

				return null;
			} else {
				// There is no condition and only one additional condition,
				// connect it to transition directly after return

				conInList.add(transition.getCondition());

				return null;
			}

		}

		// No block or variable is generated, the additional variable can be
		// connected to transition directly after return;
		return null;

	}

}
