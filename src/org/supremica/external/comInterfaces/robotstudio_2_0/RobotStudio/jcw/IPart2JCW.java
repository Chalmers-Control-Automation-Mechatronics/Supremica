package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IPart2 Implementation
public class IPart2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPartJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 getIPart2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPart2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 getIPart2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPart2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 getIPart2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPart2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 convertComPtrToIPart2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPart2JCW(comPtr,true,releaseComPtr); }
  protected IPart2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPart2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2.IID); }
  protected IPart2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPart2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2.IID); }
  protected IPart2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPart2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2.IID,releaseComPtr);}
  protected IPart2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getLibraryName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(236,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getNormalAtPoint(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Position) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Position,false),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(240,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2.IID);
    com.inzoom.comjni.Variant rv = _v[1].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
