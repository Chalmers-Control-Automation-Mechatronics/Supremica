package org.supremica.external.operationframeworkto61131.builder.sfc;

import java.util.LinkedList;
import java.util.List;

import java.math.BigDecimal;

import org.supremica.external.operationframeworkto61131.builder.cc.CCBuilder;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.data.VarList;
import org.supremica.external.operationframeworkto61131.layout.common.*;
import org.supremica.external.operationframeworkto61131.layout.sfc.block.Block;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;
import org.supremica.external.operationframeworkto61131.layout.ladder.RightPowerRail;
import org.supremica.external.operationframeworkto61131.layout.ladder.Coil;

/**
 * BlockBuilder.java has the methods that is needed to connect to a block or
 * connect from a block. This class also has method to generate block of default
 * size with inputs/output specified by the input parameters.
 * 
 * Created: Mar 31, 2009 5:43:10 PM
 * 
 * @author LC
 * @version 1.0
 */
public class BlockBuilder {

	public static final String AND = "AND";

	public static final String OR = "OR";

	public static final String NOT = "NOT";

	private static LogUtil log = LogUtil.getInstance();

	public static final int lineMargin = Constant.BlockANDHeightUnit.intValue();

	private org.supremica.external.operationframeworkto61131.util.DebugUtil debuger = new org.supremica.external.operationframeworkto61131.util.DebugUtil();

	public static org.supremica.external.operationframeworkto61131.layout.sfc.InVariable generateInVariable(
			Var var,
			Position startingPosition,
			List<Object> commonConnector,
			org.supremica.external.operationframeworkto61131.builder.Builder builder,
			Position distance, String blockType,
			List<CommonLayoutObject> addtionObjList) {

		org.supremica.external.operationframeworkto61131.layout.sfc.InVariable inVariable = new org.supremica.external.operationframeworkto61131.layout.sfc.InVariable(
				builder.nextLocalId(), var.getName());

		// if the var is negated.

		if (var.isNegated()) {

			inVariable.setNegated();

		}

		inVariable.setPosition(startingPosition);

		// add the new InVariable to PLCopen commonConnector
		commonConnector.add(inVariable.getPLCOpenObject());

		// Add to Builder's variable list
		builder.addToInterfaceVarList(var);

		builder.setLastPosition(startingPosition);

		return inVariable;

	}

	public static CommonLayoutObject generateVariablesWithBlock(
			VarList varList,
			Position startingPosition,
			List<Object> commonConnector,
			org.supremica.external.operationframeworkto61131.builder.Builder builder,
			Position distance, String blockType,
			List<CommonLayoutObject> addtionObjList) {

		// FIXME move to constant

		int sizeOfVarList = varList.getVars().size();

		int addtionListSize = 0;

		if (addtionObjList != null) {

			addtionListSize = addtionObjList.size();
		}

		if (startingPosition == null) {

			startingPosition = builder.getLastPosition();
		}

		if (sizeOfVarList + addtionListSize > 1) {

			// log.info("size:"+sizeOfVarList);

			// more than one InVariable, add a AND
			org.supremica.external.operationframeworkto61131.layout.sfc.block.Block block;

			if (blockType == OR) {

				block = getORBlock(builder.nextLocalId(), sizeOfVarList
						+ addtionListSize);
			} else {

				block = getANDBlock(builder.nextLocalId(), sizeOfVarList
						+ addtionListSize);
			}

			// Generate the InVariables from VarList and find out the InVariable
			// with the longest expression; it will be used to decide the AND
			// block's position.
			List<CommonLayoutObject> inVariableList = new LinkedList<CommonLayoutObject>();
			// append the addtional objects
			if (addtionObjList != null) {
				inVariableList.addAll(addtionObjList);

			}

			for (int i = 0; i < sizeOfVarList; i++) {

				Var var = varList.getVars().get(i);

				org.supremica.external.operationframeworkto61131.layout.sfc.InVariable inVariable = new org.supremica.external.operationframeworkto61131.layout.sfc.InVariable(
						builder.nextLocalId(), var.getName());

				// if the var is negated.

				if (var.isNegated()) {

					inVariable.setNegated();

				}

				inVariable.setPosition(startingPosition);

				// add the new InVariable to PLCopen commonConnector
				commonConnector.add(inVariable.getPLCOpenObject());

				// add to the list to be connected to AND
				inVariableList.add(inVariable);

				// Add to Builder's variable list
				builder.addToInterfaceVarList(var);

				// increase the starting position
				startingPosition.addY(lineMargin);

				builder.setLastPosition(startingPosition);

			}

			// log.info("inVariableList size:"+inVariableList.size());
			connectCommonLayoutObjectToBlock(inVariableList, block, distance);
			// Add block AND to PLCopen commonConnector;

			commonConnector.add(block.getPLCOpenObject());

			return block;

		} else if (sizeOfVarList == 1) {

			// only one InVariable return it directly
			Var var = varList.getVars().get(0);
			org.supremica.external.operationframeworkto61131.layout.sfc.InVariable inVariable = new org.supremica.external.operationframeworkto61131.layout.sfc.InVariable(
					builder.nextLocalId(), var.getName());

			inVariable.setPosition(startingPosition);

			builder.addToInterfaceVarList(var);
			commonConnector.add(inVariable.getPLCOpenObject());

			// increase the starting position
			startingPosition.addY(lineMargin);

			builder.setLastPosition(startingPosition);

			return inVariable;

		} else if (addtionListSize == 1) {

			return addtionObjList.get(0);
		} else {

			log.error("Empty VarList in BlockBuilder.generateVariablesWithAND");
		}

		return null;

	}

	// connect a list of CommenLayoutObject(InVariable or Block...) to a block,
	public static void connectCommonLayoutObjectToBlock(
			List<CommonLayoutObject> commonObjList,
			org.supremica.external.operationframeworkto61131.layout.sfc.block.Block block,
			Position distance) {

		// log.info("connectCommonLayoutObjectToBlock");

		// first find out the ConnectionPointOut with the largest X

		int largestX = 0;
		CommonLayoutObject commonObjN = null;
		int indexOfLongestExp = 0;
		int i = 0;
		for (CommonLayoutObject commonObj : commonObjList) {

			int x = commonObj.getPosition().getX()
					+ commonObj.getConnectionPointOut().getRelPosition().getX();

			if (x > largestX) {

				largestX = x;
				commonObjN = commonObj;
				indexOfLongestExp = i;
			}

			i++;

		}

		// log.info("largestX:"+largestX);
		// log.info("indexOfLongestExp:"+indexOfLongestExp);
		// Set the position of AND block; the block's position.X is decided by
		// the longest expression

		// positionY is aligned to the first element in the commonObjList.

		int indexOfYAlign = BigDecimal.valueOf(commonObjList.size())
				.divideToIntegralValue(BigDecimal.valueOf(2)).intValue() - 1;

		org.supremica.external.operationframeworkto61131.layout.common.Position blockPositonY = block
				.getNextPosition(commonObjList.get(indexOfYAlign), 0,
						indexOfYAlign, distance);
		org.supremica.external.operationframeworkto61131.layout.common.Position blockPositon = block
				.getNextPosition(commonObjN, 0, indexOfLongestExp, distance);

		blockPositon.setY(blockPositonY.getY());

		// MiddlePoint is the shortest route's middle point

		Position midwayPoint = commonObjN.getConnectionPointOut()
				.getRelPosition().add(commonObjN.getPosition());

		//		
		// log.info("blockPositon:"+blockPositon);
		// // Align the block's Y to the first InVariable, Under most
		// // situation, this is not necessary.
		// blockPositon.setY(commonObjList.get(0).getPosition().getY());

		block.setPosition(blockPositon);

		int midwayPointExtendUnit = distance.getXOf(commonObjList.size() + 1);

		for (int j = 0; j < commonObjList.size(); j++) {

			if (commonObjList.size() > 1) {

				midwayPoint.add(new Position(midwayPointExtendUnit, 0));

			}

			block.connectToOut(commonObjList.get(j), 0, j, midwayPoint, null);
		}

	}

	// Connect one variable to block's out

	public static void generateOutVariableForBlock(
			Var outVar,
			CommonLayoutObject block,
			List<Object> commonConnector,
			org.supremica.external.operationframeworkto61131.builder.Builder builder,
			Position distance) {

		VarList outVarList = VarList.getInstance();

		outVarList.append(outVar);
		generateOutVariablesForBlock(outVarList, block, commonConnector,
				builder, distance);

	}

	// Connect a list of variables to block's out

	public static void generateOutVariablesForBlock(
			VarList outVarList,
			CommonLayoutObject block,
			List<Object> commonConnector,
			org.supremica.external.operationframeworkto61131.builder.Builder builder,
			Position distance) {

		// connect op_end variable to OR

		// The distance will be changed, need a new object to avoid reference
		Position distanceI = new Position(distance);
		for (Var var : outVarList.getVars()) {

			org.supremica.external.operationframeworkto61131.layout.sfc.OutVariable outVariable = new org.supremica.external.operationframeworkto61131.layout.sfc.OutVariable(
					builder.nextLocalId(), var.getName());

			outVariable.connectToOut(block, 0, 0, null, distanceI);

			// outVariable.setPosition(outVariable.getPosition().getX(),super.getLastPosition().getY());

			commonConnector.add(outVariable.getPLCOpenObject());

			distanceI.addY(lineMargin);

			// If the Out variable is too many and exceed input variable, extend
			// the LastPositionY
			if (outVariable.getPosition().getY() > builder.getLastPosition()
					.getY()) {
				builder.adjustLastPositionY(lineMargin);

			}

			// add the var to local Pou interface
			builder.addToInterfaceVarList(var);

		}

	}

	// Connect a list of Coil to block's out
	public static void generateOutCoilForBlock(
			VarList outVarList,
			CommonLayoutObject block,
			List<Object> commonConnector,
			org.supremica.external.operationframeworkto61131.builder.Builder builder,
			Position distance,
			org.plcopen.xml.tc6.StorageModifierType storageModifier) {

		// connect op_end variable to OR

		// The distance will be changed, need a new object to avoid reference
		Position distanceI = new Position(distance);

		// The list of newly generated Coil, they will be connected to
		// RightPowerRail
		List<Coil> newCoilList = new LinkedList<Coil>();

		int longestCoilNameLength = 0;
		for (Var var : outVarList.getVars()) {
			String coilName = var.getName();
			// Find the longest Coil name to decide the position.X of
			// RightPowerRail
			if (coilName.length() > longestCoilNameLength) {

				longestCoilNameLength = coilName.length();
			}
		}
		int distanceExtendUnit = 7;
		distanceI.addX(longestCoilNameLength * distanceExtendUnit);

		for (Var var : outVarList.getVars()) {

			Coil coil = new Coil(builder.nextLocalId());

			coil.setVaraible(var.getName());

			if (storageModifier != null) {

				coil.setStorage(storageModifier);
			}
			coil.connectToOut(block, 0, 0, null, distanceI);

			// outVariable.setPosition(outVariable.getPosition().getX(),super.getLastPosition().getY());

			commonConnector.add(coil.getPLCOpenObject());

			distanceI.addY(lineMargin);

			// If the Out variable are too many and exceed input variable,
			// extend
			// the LastPositionY
			if (coil.getPosition().getY() > builder.getLastPosition().getY()) {
				builder.adjustLastPositionY(lineMargin);

			}

			// add the var to local Pou interface
			builder.addToInterfaceVarList(var);

			newCoilList.add(coil);

		}

		// FIXME One rung on the RightPowerRail?Or one rung for each connecting
		// in Coil.
		int numOfRung = 1;

		RightPowerRail rightPowerRail = new RightPowerRail(builder
				.nextLocalId(), numOfRung);

		Position distanceCoilToRightPowerRail = distance.add(
				longestCoilNameLength * distanceExtendUnit, 0);

		// Connect RightPowerRail to the first coil and fix the position of
		// RightPowerRail
		rightPowerRail.connectToOut(newCoilList.get(0),
				distanceCoilToRightPowerRail);

		// Connect RightPowerRail to the rest Coil in the list with its position
		// fixed.
		for (int i = 1; i < newCoilList.size(); i++) {

			rightPowerRail.connectToOut(newCoilList.get(i));

		}

		commonConnector.add(rightPowerRail.getPLCOpenObject());

	}

	public static org.supremica.external.operationframeworkto61131.layout.sfc.block.Block getNOTBlock(
			int localId) {

		String typeName = NOT;

		org.supremica.external.operationframeworkto61131.layout.sfc.block.Block block = getBlock(
				localId, 1, typeName);

		return block;
	}

	public static org.supremica.external.operationframeworkto61131.layout.sfc.block.Block getANDBlock(
			int localId, int numberOfInputs) {

		String typeName = AND;

		org.supremica.external.operationframeworkto61131.layout.sfc.block.Block block = getBlock(
				localId, numberOfInputs, typeName);

		return block;
	}

	public static org.supremica.external.operationframeworkto61131.layout.sfc.block.Block getORBlock(
			int localId, int numberOfInputs) {

		String typeName = OR;

		org.supremica.external.operationframeworkto61131.layout.sfc.block.Block block = getBlock(
				localId, numberOfInputs, typeName);

		return block;
	}

	public static org.supremica.external.operationframeworkto61131.layout.sfc.block.Block getBlock(
			int localId, int numberOfInputs, String typeName) {

		String[] inputs = new String[numberOfInputs];
		String[] outputs = { "OUT" };

		if (numberOfInputs == 1) {

			inputs[0] = "IN";

		} else {

			for (int i = 0; i < numberOfInputs; i++) {
				inputs[i] = "IN" + i + 1;
			}
		}

		org.supremica.external.operationframeworkto61131.layout.sfc.block.Block block = new org.supremica.external.operationframeworkto61131.layout.sfc.block.Block(
				localId, typeName, inputs, outputs);

		return block;
	}

	public static void main(String[] args) {

		org.supremica.external.operationframeworkto61131.util.DebugUtil debuger = new org.supremica.external.operationframeworkto61131.util.DebugUtil();

		Var var1 = new Var("var1", false);
		Var var2 = new Var("var2", false);
		Var var3 = new Var("var3221", false);
		Var var4 = new Var("var4", false);

		VarList varList = VarList.getInstance();

		varList.append(var1);
		varList.append(var2);
		varList.append(var3);
		varList.append(var4);
		Position startingPosition = new Position(20, 40);
		List<Object> commonConnector = new LinkedList<Object>();
		Position distance = new Position(30, 0);
		CCBuilder ccBuilder = new CCBuilder();
		CommonLayoutObject commonObj = BlockBuilder.generateVariablesWithBlock(
				varList, startingPosition, commonConnector, ccBuilder,
				distance, BlockBuilder.AND, null);

		Var var11 = new Var("var1", false);
		Var var21 = new Var("var2", false);
		Var var31 = new Var("var3221", false);
		Var var41 = new Var("var4", false);

		VarList varList1 = VarList.getInstance();

		varList1.append(var11);
		// varList1.append(var21);
		// varList1.append(var31);
		// varList1.append(var41);

		Position startingPosition1 = new Position(20, 200);
		List<Object> commonConnector1 = new LinkedList<Object>();

		CommonLayoutObject commonObj1 = BlockBuilder
				.generateVariablesWithBlock(varList1, startingPosition1,
						commonConnector, ccBuilder, distance, BlockBuilder.AND,
						null);

		Block blockOR = BlockBuilder.getORBlock(ccBuilder.nextLocalId(), 2);

		List<CommonLayoutObject> inVariableList = new LinkedList<CommonLayoutObject>();

		inVariableList.add(commonObj);

		inVariableList.add(commonObj1);

		BlockBuilder.connectCommonLayoutObjectToBlock(inVariableList, blockOR,
				distance);

		log.info("Block or");

		org.supremica.external.operationframeworkto61131.util.DebugUtil
				.printCommonLayoutObject(blockOR);

	}

}
