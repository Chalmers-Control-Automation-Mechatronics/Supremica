package org.supremica.external.operationframeworkto61131.layout.sfc;
/**
 * @author LC
 *
 */
import java.math.BigDecimal;
import java.util.List;
import java.util.LinkedList;

import org.plcopen.xml.tc6.Body;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.main.Constant;




public class ActionBlock extends CommonLayoutObject {

	List<Action> actionList;

	// Generate a coil of default sizew with ConnectionPointIn and
	// ConnectionPointOut
	// at the default position
	public ActionBlock(int localId) {

		actionList = new LinkedList<Action>();

		// round to integer
		int halfHeight = Constant.ActionBlockHeight.divideToIntegralValue(
				BigDecimal.valueOf(2)).intValue();

		Position conInRelPosition = new Position(0, halfHeight);

		Position conOutRelPosition = null;
		// height and width will be adjusted according to the names in
		// action.reference
		super.setDefault(localId, 0, 0, conInRelPosition, conOutRelPosition);

	}

	public org.plcopen.xml.tc6.Body.SFC.ActionBlock getPLCOpenObject() {

		org.plcopen.xml.tc6.Body.SFC.ActionBlock actionBlock = CommonLayoutObject.objectFactory
				.createBodySFCActionBlock();

		try {
			this.adjustSize();
			super.getPLCopenObject(actionBlock);
			super.getCommenConInPLCOpenObject(actionBlock);

			if (!this.actionList.isEmpty()) {
				// FIXME could cause problem if values other than referece are
				// in Action
				// only referece is considered

				for (Action actionI : this.actionList) {
					Body.SFC.ActionBlock.Action action = CommonLayoutObject.objectFactory
							.createBodySFCActionBlockAction();

					Body.SFC.ActionBlock.Action.Reference reference = CommonLayoutObject.objectFactory
							.createBodySFCActionBlockActionReference();
					reference.setName(actionI.getReferece());

					// action.setQualifier("N");

					action.setReference(reference);

					actionBlock.getAction().add(action);

				}
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return actionBlock;
	}
// the connectionPointIn is on the left, do not need to care about the right edge
	public void adjustSize() {

		int longestExpression = 0;

		// FIXME could cause problem if values other than referece are in Action
		for (Action action : this.actionList) {
			if (action.getReferece().length() > longestExpression) {
				longestExpression = action.getReferece().length();
			}
		}

		int height = Constant.ActionBlockHeight.intValue() * actionList.size();
		int width = Constant.ActionBlockWidthExtendUnit.intValue()
				* longestExpression;

		super.setHeight(height);
		super.setWidth(width);

	}

	public void addReferenceToActionList(String toAdd) {

		if (toAdd != null || !toAdd.isEmpty()) {
			Action action = new Action();
			action.setReferece(toAdd);
			this.actionList.add(action);
		}

	}

	public void removeRefereceFromActionList(String toRemove) {

		if (toRemove != null || !toRemove.isEmpty()) {

			for (Action action : this.actionList) {

				if (action.getReferece().equalsIgnoreCase(toRemove)) {

					actionList.remove(action);

					break;
				}

			}

		}
	}

	public List<Action> getActionList() {
		return actionList;
	}

	public void setActionList(List<Action> actionList) {

		if (actionList != null) {
			this.actionList = actionList;
		}
	}

}
