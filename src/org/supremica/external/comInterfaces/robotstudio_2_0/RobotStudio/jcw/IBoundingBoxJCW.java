package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IBoundingBox Implementation
public class IBoundingBoxJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox getIBoundingBoxFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IBoundingBoxJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox getIBoundingBoxFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IBoundingBoxJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox getIBoundingBoxFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IBoundingBoxJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox convertComPtrToIBoundingBox(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IBoundingBoxJCW(comPtr,true,releaseComPtr); }
  protected IBoundingBoxJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IBoundingBoxJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox.IID); }
  protected IBoundingBoxJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IBoundingBoxJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox.IID); }
  protected IBoundingBoxJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IBoundingBoxJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox.IID,releaseComPtr);}
  protected IBoundingBoxJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition getMin() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPositionJCW.getIPositionFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition getMax() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPositionJCW.getIPositionFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
