package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _CodeModule Declaration
public interface _CodeModule extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E16E,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pbstrName) throws com.inzoom.comjni.ComJniException;
  public void addFromString(String String) throws com.inzoom.comjni.ComJniException;
  public void addFromFile(String FileName) throws com.inzoom.comjni.ComJniException;
  public String getLines(int StartLine,int Count) throws com.inzoom.comjni.ComJniException;
  public int getCountOfLines() throws com.inzoom.comjni.ComJniException;
  public void insertLines(int Line,String String) throws com.inzoom.comjni.ComJniException;
  public void deleteLines(int StartLine,int Count) throws com.inzoom.comjni.ComJniException;
  public void deleteLines(int StartLine) throws com.inzoom.comjni.ComJniException;
  public void replaceLine(int Line,String String) throws com.inzoom.comjni.ComJniException;
  public int getProcStartLine(String ProcName,int ProcKind) throws com.inzoom.comjni.ComJniException;
  public int getProcCountLines(String ProcName,int ProcKind) throws com.inzoom.comjni.ComJniException;
  public int getProcBodyLine(String ProcName,int ProcKind) throws com.inzoom.comjni.ComJniException;
  public String getProcOfLine(int Line,int[] ProcKind) throws com.inzoom.comjni.ComJniException;
  public int getCountOfDeclarationLines() throws com.inzoom.comjni.ComJniException;
  public int createEventProc(String EventName,String ObjectName) throws com.inzoom.comjni.ComJniException;
  public boolean find(String Target,int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn,boolean WholeWord,boolean MatchCase,boolean PatternSearch) throws com.inzoom.comjni.ComJniException;
  public boolean find(String Target,int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn,boolean WholeWord,boolean MatchCase) throws com.inzoom.comjni.ComJniException;
  public boolean find(String Target,int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn,boolean WholeWord) throws com.inzoom.comjni.ComJniException;
  public boolean find(String Target,int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePane getCodePane() throws com.inzoom.comjni.ComJniException;
}
