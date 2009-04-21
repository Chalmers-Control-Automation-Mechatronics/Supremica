package org.supremica.external.operationframeworkto61131.builder.sfc;

import java.util.List;

import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.FBCallingVarsList;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.layout.sfc.ActionBlock;
import org.supremica.external.operationframeworkto61131.layout.sfc.Step;



/**
 * ActionBuilder.java Generate a action block and add the new ActionBlock to
 * SFC's live list. Return type is null.
 * 
 * Created: Mar 31, 2009 5:35:14 PM
 * 
 * @author LC
 * @version 1.0
 */
public class ActionBuilder {

	public static void generateActions(List<Object> SFCLiveList,
			FBCallingVarsList operationVarList, Position distance, Step step,
			org.supremica.external.operationframeworkto61131.builder.Builder builder) {

		ActionBlock actionBlock = new ActionBlock(builder.nextLocalId());

		for (FBCallingVars operationVar : operationVarList
				.getFBCallingVarsList()) {

			Var opRequestVar = operationVar.getRequestVar();
			actionBlock.addReferenceToActionList(opRequestVar.getName());

			builder.addToInterfaceVarList(opRequestVar);

		}

		actionBlock.connectToOut(step, step.getConnectionPointOutAction(),
				actionBlock.getConnectionPointIn(), null, distance);

		SFCLiveList.add(actionBlock.getPLCOpenObject());

//		Add the Step object outside this function, not here.
//		SFCLiveList.add(step.getPLCOpenObject());

	}
}
