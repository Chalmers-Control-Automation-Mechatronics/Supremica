package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Attachments
public class Attachments extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttachmentsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttachments {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xC59DDB9F,(short)0x2717,(short)0x11D4,new char[]{0xAD,0x97,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Attachments getAttachmentsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Attachments(comPtr,bAddRef); }
  public static Attachments getAttachmentsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Attachments(comPtr); }
  public static Attachments getAttachmentsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Attachments(unk); }
  public static Attachments convertComPtrToAttachments(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Attachments(comPtr,true,releaseComPtr); }
  protected Attachments(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Attachments(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Attachments(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Attachments(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
