package org.supremica.external.operationframeworkto61131.rslogix;

import org.supremica.external.operationframeworkto61131.util.FileUtil;

public class Test {

	FileUtil file = new FileUtil(
			"C:\\Documents and Settings\\HAHA\\Desktop\\test.txt");

	private String headerFile = ".\\RSLogix5000\\header.txt";
	private String footerFile = ".\\RSLogix5000\\footer.txt";

	public void doTest() {

		StringBuffer buf = new StringBuffer();

		buf.append(CommonText.getTabs(3)).append("Start with 3 tabs").append(
				CommonText.NEW_LINE).append(CommonText.getTabs(3)).append(
				"Another line with 3 tabs");

		file.writeFile(buf.toString(), true);
	}

	public void doTagTest() {

		Tag tag1 = new Tag("Var1");
		Tag tag2 = new Tag("Var2");
		Tag tag3 = new Tag("Var3");
		Tag tag4 = new Tag("Var4");

		Tags tags = new Tags();

		tags.add(tag1);
		tags.add(tag2);
		tags.add(tag3);
		tags.add(tag4);

		file.writeFile(tags.getText(1), false);
	}

	public void doHeaderNFooterTest() {

		StringBuffer buf = new StringBuffer();
		HeaderNFooter headerNFooter = new HeaderNFooter(headerFile, footerFile);
		buf.append(headerNFooter.getHeader());
		buf.append(headerNFooter.getFooter());

		file.writeFile(buf.toString(), true);
	}

	public static void main(String args[]) {

		Test test = new Test();
		test.doHeaderNFooterTest();

		System.out.println("Done");
	}

}
