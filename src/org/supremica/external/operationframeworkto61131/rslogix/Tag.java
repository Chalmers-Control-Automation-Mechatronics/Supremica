package org.supremica.external.operationframeworkto61131.rslogix;
/**
 * @author LC
 *
 */
import java.lang.StringBuffer;
import org.plcopen.xml.tc6.VarListPlain.Variable;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;


public class Tag extends CommonText {

	public final static String TYPE_BASE = "Base";
	public final static String TYPE_ALIAS = "Alias";
	public final static String TYPE_PRODUCED = "Produced";
	public final static String TYPE_CONSUMED = "Consumed";
	public final static String DATA_TYPE_BOOL = "BOOL";
	public final static String DATA_TYPE_DINT = "DINT";
	public final static String STYLE_DECIMAL = "Decimal";
	public final static String STYLE_BINARY = "Binary";
	public final static String STYLE_OCTAL = "Octal";
	public final static String STYLE_HEX = "Hex";
	public final static String RADIX = "RADIX";
	public final static String BOOL_FALSE = "0";
	public final static String BOOL_TRUE = "1";

	private String name;
	private String description;
	private String type = TYPE_BASE;
	private String dataType = DATA_TYPE_BOOL;
	private String style = STYLE_DECIMAL;
	private String value = BOOL_FALSE;

	public Tag(String name) {

		setName(name);

	}

	public Tag(Variable variable) {

		this.setName(variable.getName());

		// TODO parse more data type here

		if (variable.getType().getINT() != null) {

			this.setDataType(DATA_TYPE_DINT);
		}

		// TODO parse default value;
		// variable.getInitialValue().getSimpleValue().getValue();

	}

	public String getText(int nTabs) {

		String tabs = CommonText.getTabs(nTabs);

		StringBuffer buf = new StringBuffer();

		// Tabs
		buf.append(tabs);

		// Tag Name
		if (!getName(buf)) {
			return null;
		}

		// Colon
		buf.append(COLON);

		// Data Type
		if (!getDataType(buf)) {
			return null;
		}

		// Style
		if (!getStyle(buf)) {
			return null;
		}

		// Value
		if (!getValue(buf)) {
			return null;
		}

		buf.append(NEW_LINE);

		return buf.toString();
	}

	private Boolean getName(StringBuffer buf) {
		log.info("name:" + name);
		// Tag Name
		if (this.name != null && !this.name.isEmpty()) {

			buf.append(this.name).append(SPACE);
			return true;
		} else {
			log.error("Empty tag name");
			return false;
		}
	}

	private Boolean getDataType(StringBuffer buf) {
		// Data Type
		if (this.dataType != null && !this.dataType.isEmpty()) {
			buf.append(this.dataType).append(SPACE);
			return true;
		} else {
			log.error("Empty tag date type: " + buf.toString());
			return false;
		}
	}

	public Boolean getStyle(StringBuffer buf) {
		// Style
		if (this.style != null && !this.style.isEmpty()) {
			buf.append(LEFT_ROUND_BRACKET).append(RADIX).append(EQUAL).append(
					this.style).append(RIGHT_ROUND_BRACKET);
			return true;
		} else {
			log.error("Empty tag style: " + buf.toString());
			return false;
		}
	}

	public Boolean getValue(StringBuffer buf) {
		if (value != null && !value.isEmpty()) {

			buf.append(EQUAL).append(this.value).append(SEMICOLON);
			return true;
		} else {

			log.error("Empty default tag value: " + buf.toString());

			return false;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static void testTag() {

		Tag tag = new Tag("testTagName");

		tag.setType(TYPE_BASE);
		tag.setDataType(DATA_TYPE_BOOL);
		tag.setValue(BOOL_TRUE);

		System.out.println(tag.getText(2));

		Tag tag2 = new Tag("defaultTag");

		System.out.println(tag2.getText(2));

	}

	public static void main(String[] args) {

		Tag.testTag();

	}

}
