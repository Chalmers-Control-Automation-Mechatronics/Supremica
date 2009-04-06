package org.supremica.external.operationframeworkto61131.rslogix;
/**
 * @author LC
 *
 */
import java.util.List;
import java.util.LinkedList;
import org.plcopen.xml.tc6.Project.Types.Pous.Pou;

public class Program extends CommonText {

	private int mode = 0;
	private int disableFlag = 0;
	private String main;
	private String name = "";
	public static final String PROGRAM = "PROGRAM";
	public static final String END_PROGRAM = "END_PROGRAM";
	public static final String MAIN = "MAIN";
	public static final String MODE = "MODE";
	public static final String DisableFlag = "DisableFlag";
	// private Tags tags;
	// private List<Routine> routines = new LinkedList<Routine>();

	private List<Pou> pouList;

	public Program(List<Pou> pouList) {

		this.pouList = pouList;
	}

	public String getText(int nTabs) {

		StringBuffer buf = new StringBuffer();
		String tabs = CommonText.getTabs(nTabs);
		buf.append(tabs).append(PROGRAM);
		buf.append(SPACE).append(this.getName()).append(SPACE);

		if (this.getMain() != null) {
			buf.append(LEFT_ROUND_BRACKET).append(MAIN).append(EQUAL).append(
					QUOTATION).append(this.getMain()).append(QUOTATION).append(
					COMMA).append(NEW_LINE);
			buf.append(CommonText.getTabs(nTabs + 2)).append(MODE)
					.append(EQUAL).append(this.getMode()).append(COMMA).append(
							NEW_LINE);
		} else {
			buf.append(LEFT_ROUND_BRACKET).append(MODE).append(EQUAL).append(
					this.getMode()).append(COMMA).append(NEW_LINE);

		}

		buf.append(CommonText.getTabs(nTabs + 2)).append(DisableFlag).append(
				EQUAL).append(this.getDisableFlag())
				.append(RIGHT_ROUND_BRACKET).append(NEW_LINE);

		Tags tags = new Tags();
		for (Pou iPou : pouList) {

			tags.append(iPou.getInterface());

		}

		buf.append(tags.getText(nTabs + 2));

		for (Pou iPou : pouList) {
			Routine routine = new Routine(iPou.getBody().getLD());
			routine.setName(iPou.getName());

			buf.append(routine.getText(nTabs + 2));
		}

		buf.append(tabs).append(END_PROGRAM).append(NEW_LINE).append(NEW_LINE);

		return buf.toString();
	}

	public String getText_test(int nTabs) {

		StringBuffer buf = new StringBuffer();
		String tabs = CommonText.getTabs(nTabs);

		for (Pou iPou : pouList) {
			Routine routine = new Routine(iPou.getBody().getLD());
			routine.setName(iPou.getName());

			buf.append(routine.getText_test(nTabs));
		}

		return buf.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public String getMain() {
		return main;
	}

	public void setMain(String main) {
		this.main = main;
	}

	public int getDisableFlag() {
		return disableFlag;
	}

	public void setDisableFlag(int disableFlag) {
		this.disableFlag = disableFlag;
	}

}
