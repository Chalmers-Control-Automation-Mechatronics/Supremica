package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass SelectionLevels
public class SelectionLevels extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ISelectionLevelsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelectionLevels {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xC02A25D7,(short)0xE9C2,(short)0x11D3,new char[]{0xAD,0x4F,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static SelectionLevels getSelectionLevelsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new SelectionLevels(comPtr,bAddRef); }
  public static SelectionLevels getSelectionLevelsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new SelectionLevels(comPtr); }
  public static SelectionLevels getSelectionLevelsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new SelectionLevels(unk); }
  public static SelectionLevels convertComPtrToSelectionLevels(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new SelectionLevels(comPtr,true,releaseComPtr); }
  protected SelectionLevels(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected SelectionLevels(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected SelectionLevels(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected SelectionLevels(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
