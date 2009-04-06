package org.supremica.external.operationframeworkto61131.builder;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;
import java.lang.StringBuffer;

import javax.swing.JCheckBox;

import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.data.VarList;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.FileUtil;
import org.supremica.external.operationframeworkto61131.util.ReflectionUtil;
import org.supremica.manufacturingtables.xsd.eop.Operation;



/**
 * VariableListBuilder.java build the variables list for PLC's external
 * communication
 * 
 * Created: Mar 31, 2009 6:13:25 PM
 * 
 * @author LC
 * @version 1.0
 */
public class VariableListBuilder extends org.supremica.external.operationframeworkto61131.builder.Builder {

	private FileUtil fileUtil = null;

	public final static String[] columns = { "Name", "Type", "Location",
			"Initial Value", "Retain", "Constant" };

	private Boolean[] isSelected;
	
	private static final String commentSign="#";

	private String choosedColumns;


	// Name| Type|Location|Initial Value|Retain|Constant|

	public void buildVariableList(
			HashMap<String, VarList> intelligentMachineExternalVariableListHashMap,
			VarList allPouInterfaceVarList, String ouputPath,
			JCheckBox[] infoChoices) {

		ouputPath = FileUtil.fixPathEndSign(ouputPath);

		String outputFile = ouputPath + Constant.PLCOPEN_VARLIST_OUT_PUT_FILE;

		fileUtil = new FileUtil(outputFile);

		if (fileUtil == null) {
			return;
		}

		// Generate all pou's variable list

		if (infoChoices != null) {

			isSelected = new Boolean[infoChoices.length];

			for (int i = 0; i < infoChoices.length; i++) {

				isSelected[i] = infoChoices[i].isSelected();

			}

		} else {

			isSelected = new Boolean[columns.length + 1];
			isSelected[0] = true;
			for (int i = 1; i < isSelected.length; i++) {

				isSelected[i] = true;

			}
		}

		// Title
		fileUtil.writeLine(commentSign+"Variable list for PLCopen xml program :"
				+ ouputPath + Constant.PLCOPEN_OUT_PUT_FILE, false);

		choosedColumns = this.getColumns();

		fileUtil.writeLine(choosedColumns, true);
		fileUtil.writeLine("", true);

		if (!isSelected[0]) {

			outputAllPouVarList(allPouInterfaceVarList);

		}

		outputIntelligentMachineVariableList(intelligentMachineExternalVariableListHashMap);

		fileUtil.close();

	}

	private void outputAllPouVarList(VarList allPouInterfaceVarList) {

		for (Var var : allPouInterfaceVarList.getVars()) {

			this.writeVarToFile(var);

		}

	}

	private void outputIntelligentMachineVariableList(
			HashMap<String, VarList> intelligentMachineExternalVariableListHashMap) {

		Set keySet = intelligentMachineExternalVariableListHashMap.keySet();

		for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {

			String machineName = (String) iterator.next();

			VarList varList = intelligentMachineExternalVariableListHashMap
					.get(machineName);

			fileUtil.writeLine("", true);
			fileUtil.writeLine("", true);
			fileUtil.writeLine(commentSign+"Machine:" + machineName, true);

			for (Var var : varList.getVars()) {

				this.writeVarToFile(var);

			}

		}

	}

	private void writeVarToFile(org.supremica.external.operationframeworkto61131.data.Var var) {

		// Name| Class| Type|Location|Initial Value|Retain|Constant|

		String comma = ",";
		String empty = "null";

		String name = var.getName();

		if (name == null) {

			name = empty;
		}

		// TODO add more data type
		Class typeClass = var.getType();
		String type = empty;
		if (typeClass != null) {
			type = typeClass.getSimpleName();
		}

		String location = var.getAddress();

		if (location == null) {

			location = empty;
		}

		String initialValue = var.getValue();

		if (initialValue == null) {

			initialValue = empty;
		}

		String retain = var.isRetain().toString();

		if (retain == null) {

			retain = empty;
		}

		String constant = var.isConstant().toString();

		if (constant == null) {

			constant = empty;
		}

		StringBuffer buf = new StringBuffer();

		int i = 1;
		if (isSelected[i++]) {

			buf.append(name).append(comma);
		}

		if (isSelected[i++]) {

			buf.append(type).append(comma);

		}

		if (isSelected[i++]) {
			buf.append(location).append(comma);

		}

		if (isSelected[i++]) {
			buf.append(initialValue).append(comma);

		}

		if (isSelected[i++]) {
			buf.append(retain).append(comma);
		}

		if (isSelected[i]) {
			buf.append(constant);
		}

		// System.out.println("=================================buf:" + buf);

		fileUtil.writeLine(buf.toString(), true);

	}

	public String getColumns() {

		StringBuffer columnsBuf = new StringBuffer();
		
		columnsBuf.append(commentSign).append("Fields:");

		for (int i = 1; i < isSelected.length; i++) {

			if (isSelected[i]) {

				columnsBuf.append(columns[i - 1]).append("|");

			}

		}

		return columnsBuf.toString();

	}

}
