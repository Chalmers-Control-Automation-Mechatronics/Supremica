package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _CodePane Declaration
public interface _CodePane extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E176,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePanes getCollection() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window getWindow() throws com.inzoom.comjni.ComJniException;
  public void getSelection(int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn) throws com.inzoom.comjni.ComJniException;
  public void setSelection(int StartLine,int StartColumn,int EndLine,int EndColumn) throws com.inzoom.comjni.ComJniException;
  public int getTopLine() throws com.inzoom.comjni.ComJniException;
  public void setTopLine(int TopLine) throws com.inzoom.comjni.ComJniException;
  public int getCountOfVisibleLines() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodeModule getCodeModule() throws com.inzoom.comjni.ComJniException;
  public void show() throws com.inzoom.comjni.ComJniException;
  public int getCodePaneView() throws com.inzoom.comjni.ComJniException;
}
