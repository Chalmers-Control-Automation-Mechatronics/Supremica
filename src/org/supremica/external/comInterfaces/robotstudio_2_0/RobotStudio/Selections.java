package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Selections
public class Selections extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ISelectionsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelections {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x78A4FEB9,(short)0xA30E,(short)0x11D3,new char[]{0xAD,0x05,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Selections getSelectionsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Selections(comPtr,bAddRef); }
  public static Selections getSelectionsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Selections(comPtr); }
  public static Selections getSelectionsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Selections(unk); }
  public static Selections convertComPtrToSelections(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Selections(comPtr,true,releaseComPtr); }
  protected Selections(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Selections(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Selections(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Selections(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
