package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// coclass CommandBarControl
public class CommandBarControl extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDA13E9DD,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static CommandBarControl getCommandBarControlFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControl(comPtr,bAddRef); }
  public static CommandBarControl getCommandBarControlFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControl(comPtr); }
  public static CommandBarControl getCommandBarControlFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarControl(unk); }
  public static CommandBarControl convertComPtrToCommandBarControl(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControl(comPtr,true,releaseComPtr); }
  protected CommandBarControl(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarControl(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBarControl(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBarControl(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
