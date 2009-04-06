package org.supremica.external.operationframeworkto61131.rslogix;
/**
 * @author LC
 *
 */
import java.util.List;
import java.util.LinkedList;

public class Task extends CommonText {

	private String type = TYPE_CONTINUOUS;
	private int rate = 10;
	private int priority = 10;
	private int watchdog = 500;
	private String disableUpdateOutputs = NO;
	private String InhibitTask = NO;
	private String name;
	public static final String TYPE = "Type";
	public static final String RATE = "Rate";
	public static final String PRIORITY = "Priority";
	public static final String WATCHDOG = "Watchdog";
	public static final String DISABLE_UPDATE_OUTPUTS = "DisableUpdateOutputs";
	public static final String INHIBITTASK = "InhibitTask";
	public static final String TYPE_CONTINUOUS = "CONTINUOUS";
	public static final String TASK = "TASK";
	public static final String END_TASK = "END_TASK";

	private List<Program> programs = new LinkedList<Program>();

	public String getText(int nTabs) {

		String tabs = CommonText.getTabs(nTabs);

		StringBuffer buf = new StringBuffer();

		buf.append(tabs).append(TASK).append(SPACE).append(this.getName())
				.append(SPACE);
		buf.append(LEFT_ROUND_BRACKET).append(
				getPropertyPair(TYPE, this.getType(), 0));

		buf.append(getPropertyPair(RATE, String.valueOf(this.getRate()),
				nTabs + 2));
		buf.append(getPropertyPair(PRIORITY,
				String.valueOf(this.getPriority()), nTabs + 2));
		buf.append(getPropertyPair(WATCHDOG,
				String.valueOf(this.getWatchdog()), nTabs + 2));
		buf.append(getPropertyPair(DISABLE_UPDATE_OUTPUTS, String.valueOf(this
				.getDisableUpdateOutputs()), nTabs + 2));

		buf.append(CommonText.getTabs(nTabs + 2)).append(INHIBITTASK).append(
				EQUAL).append(this.getInhibitTask())
				.append(RIGHT_ROUND_BRACKET).append(NEW_LINE);

		String tabs1 = CommonText.getTabs(nTabs + 1);
		for (Program program : programs) {

			buf.append(tabs1).append(program.getName()).append(SEMICOLON)
					.append(NEW_LINE);

		}

		buf.append(tabs).append(END_TASK).append(NEW_LINE).append(NEW_LINE);

		return buf.toString();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getWatchdog() {
		return watchdog;
	}

	public void setWatchdog(int watchdog) {
		this.watchdog = watchdog;
	}

	public String getDisableUpdateOutputs() {
		return disableUpdateOutputs;
	}

	public void setDisableUpdateOutputs(String disableUpdateOutputs) {
		this.disableUpdateOutputs = disableUpdateOutputs;
	}

	public String getInhibitTask() {
		return InhibitTask;
	}

	public void setInhibitTask(String inhibitTask) {
		InhibitTask = inhibitTask;
	}

	public void addProgram(Program program) {

		this.programs.add(program);

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
