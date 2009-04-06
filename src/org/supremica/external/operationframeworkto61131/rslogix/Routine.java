package org.supremica.external.operationframeworkto61131.rslogix;
/**
 * @author LC
 *
 */
import java.util.List;
import java.util.LinkedList;
import org.plcopen.xml.tc6.Body.LD;

public class Routine extends CommonText {

	public final static String ROUTINE = "ROUTINE";
	public final static String END_ROUTINE = "END_ROUTINE";

	private String name;

	private List<Rung> rungs = new LinkedList<Rung>();

	public Routine(org.plcopen.xml.tc6.Body.LD ld) {

		rungs.add(new Rung(ld));

	}

	public String getText(int nTabs) {

		String tabs = CommonText.getTabs(nTabs);

		StringBuffer buf = new StringBuffer();

		buf.append(tabs).append(ROUTINE).append(SPACE).append(name).append(
				NEW_LINE);

		for (Rung rung : rungs) {

			buf.append(rung.getText(nTabs + 2));

		}

		buf.append(tabs).append(END_ROUTINE).append(NEW_LINE).append(NEW_LINE);

		return buf.toString();
	}

	public String getText_test(int nTabs) {

		String tabs = CommonText.getTabs(nTabs);

		StringBuffer buf = new StringBuffer();

		for (Rung rung : rungs) {

			buf.append(rung.getText_test(nTabs));

		}

		return buf.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
