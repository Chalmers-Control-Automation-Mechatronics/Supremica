package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IABBS4Controller Implementation
public class IABBS4ControllerJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IControllerJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller getIABBS4ControllerFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IABBS4ControllerJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller getIABBS4ControllerFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IABBS4ControllerJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller getIABBS4ControllerFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IABBS4ControllerJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller convertComPtrToIABBS4Controller(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IABBS4ControllerJCW(comPtr,true,releaseComPtr); }
  protected IABBS4ControllerJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IABBS4ControllerJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID); }
  protected IABBS4ControllerJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IABBS4ControllerJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID); }
  protected IABBS4ControllerJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IABBS4ControllerJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID,releaseComPtr);}
  protected IABBS4ControllerJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public boolean shutDown() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 getMechanism() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMechanism2JCW.getIMechanism2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMechanism(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Modules getModules() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Modules rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4ModulesJCW.getIABBS4ModulesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void exportProgram(String FileName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void importProgram(String FileName,boolean Overwrite) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant(Overwrite,false)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void importProgram(String FileName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant((boolean)false,false)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IProcessTypes getProcessTypes() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IProcessTypes rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IProcessTypesJCW.getIProcessTypesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IDataTypes getDataTypes() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IDataTypes rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IDataTypesJCW.getIDataTypesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void processAbsAccCalib(String InFileName,String OutFileName,int CalibrationOption) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(InFileName,false),
      new com.inzoom.comjni.Variant(OutFileName,false),
      new com.inzoom.comjni.Variant(CalibrationOption,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public double getMotionTime() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller.IID);
    double rv = _v[0].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
