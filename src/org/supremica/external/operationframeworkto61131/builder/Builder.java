package org.supremica.external.operationframeworkto61131.builder;

import java.math.BigInteger;
import java.util.List;
import java.util.LinkedList;
import org.plcopen.xml.tc6.Project;
import org.supremica.external.operationframeworkto61131.controlinfo.EquipmentStateLookUp;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.data.VarList;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;



/**
 * Builder.java is the super class of other builder classes. It contains
 * functions that all type of builders have in common: 1. Handle variable list
 * for newly generated variables 2. Allocate localId, avoid duplicated localId
 * during FB reuse. 3. Handle the positions of newly generated objects, avoid
 * overlapping.
 * 
 * Created: Mar 31, 2009 5:49:47 PM
 * 
 * @author LC
 * @version 1.0
 */
public class Builder {

	public static LogUtil log = LogUtil.getInstance();

	// The list of variable of all Pous that were generated in this class, will
	// be added to global varialbe list in configuration.
	private org.supremica.external.operationframeworkto61131.data.VarList localAllPouInterfaceVarList = org.supremica.external.operationframeworkto61131.data.VarList
			.getInstance();;

	private LinkedList<BigInteger> localIdList = new LinkedList<BigInteger>();

	private int localId = 1;

	public int leftBorder;
	// TODO move (100,100) to config.xml or set dynamically

	private Position lastPosition = new Position(100, 100);

	public static final String AND = "AND";
	public static String OR = "OR";

	public static EquipmentStateLookUp equipmentStateLookUp = null;

	public Builder() {

		if (equipmentStateLookUp == null) {

			try {

				equipmentStateLookUp = (EquipmentStateLookUp) Class.forName(
						Constant.EQUIPMENT_STATE_LOOK_UP_IMPLEMENT)
						.newInstance();
				//			 
			} catch (Exception e) {

				log
						.error("Can not build equipment state look up implement from class:"
								+ Constant.EQUIPMENT_STATE_LOOK_UP_IMPLEMENT);

				return;

			}
		}

	}

	// Will be called in POUBuilder to get all Pou's Variable list
	public org.supremica.external.operationframeworkto61131.data.VarList getInterfaceVarList() {

		return localAllPouInterfaceVarList;
	}

	// Duplicate element will be ignored
	public void addToInterfaceVarList(org.supremica.external.operationframeworkto61131.data.Var var) {

		localAllPouInterfaceVarList.append(var);
	}

	// add a list of var to InterfaceVarList
	public void addToInterfaceVarList(org.supremica.external.operationframeworkto61131.data.VarList varList) {

		localAllPouInterfaceVarList.append(varList);
	}

	public void addToLocalIdList(int localId) {

		this.localIdList.add(BigInteger.valueOf(localId));

	}

	public void addToLocalIdList(BigInteger localId) {

		this.localIdList.add(localId);

	}

	public int nextLocalId() {

		int ret = localId++;

		if (localIdList.contains(BigInteger.valueOf(ret))) {

			return nextLocalId();

		} else {

			this.addToLocalIdList(ret);

			return ret;
		}
	}

	public void adjustLocalId(int op) {

		localId = localId + op;
	}

	public Position getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(Position lastPosition) {
		this.lastPosition = lastPosition;
	}

	public void adjustLastPositionY(int op) {

		lastPosition.addY(op);

	}

	public void resetLocalId() {

		this.localIdList.clear();
		this.localId = 1;

	}

	public static void main(String[] args) {

		Builder builder = new Builder();

		int[] exsitLocalId = { 14, 15, 16, 3, 5, 1, 6, 7, 8, 9 };

		for (int i = 0; i < exsitLocalId.length; i++) {

			builder.addToLocalIdList(exsitLocalId[i]);

		}

		int j = 1;

		while (j < 15) {

			System.out.println("Next LocalId " + j + ":"
					+ builder.nextLocalId());

			j++;
		}

	}

}
