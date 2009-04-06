package org.supremica.external.operationframeworkto61131.rslogix;
/**
 * @author LC
 *
 */
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.plcopen.xml.tc6.Project.Types.Pous.Pou.Interface;
import org.plcopen.xml.tc6.VarList;
import org.plcopen.xml.tc6.VarListPlain.Variable;

public class Tags extends CommonText {

	public final static String TAG = "TAG";
	public final static String END_TAG = "END_TAG";
	private List<Tag> tags = new LinkedList<Tag>();

	public Tags() {

	}

	public Tags(Interface pouInterface) {

		append(pouInterface);

	}

	public void append(Interface pouInterface) {

		List<VarList> varList = pouInterface
				.getLocalVarsOrTempVarsOrInputVars();

		for (VarList varListI : varList) {

			List<Variable> variables = varListI.getVariable();

			for (Variable variable : variables) {

				Tag tag = new Tag(variable);
				this.add(tag);
			}

		}

	}

	public void add(Tag tag) {

		tags.add(tag);
	}

	public List<Tag> getTags() {

		return tags;
	}

	public String getText(int nTabs) {

		String tabs = CommonText.getTabs(nTabs);

		StringBuffer buf = new StringBuffer();

		buf.append(tabs).append(TAG).append(CommonText.NEW_LINE);

		for (Tag tag : tags) {

			String tagString = tag.getText(nTabs + 1);

			if (tagString != null && !tagString.isEmpty()) {
				buf.append(tagString);
			}

		}
		buf.append(tabs).append(END_TAG).append(CommonText.NEW_LINE).append(NEW_LINE);

		return buf.toString();
	}

}
