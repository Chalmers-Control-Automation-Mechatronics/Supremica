package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IUserOptions Implementation
public class IUserOptionsJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions getIUserOptionsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IUserOptionsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions getIUserOptionsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IUserOptionsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions getIUserOptionsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IUserOptionsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions convertComPtrToIUserOptions(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IUserOptionsJCW(comPtr,true,releaseComPtr); }
  protected IUserOptionsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IUserOptionsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID); }
  protected IUserOptionsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IUserOptionsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID); }
  protected IUserOptionsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IUserOptionsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID,releaseComPtr);}
  protected IUserOptionsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public int getSelectionRadius() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setSelectionRadius(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getTwosidedLighting() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTwosidedLighting(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getSelectionMode() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setSelectionMode(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getBackFaceCulling() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBackFaceCulling(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public double getSurfaceTolerance() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    double rv = _v[0].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setSurfaceTolerance(double pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,com.inzoom.comjni.enum.VarType.R8,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public double getNormalTolerance() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    double rv = _v[0].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setNormalTolerance(double pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,com.inzoom.comjni.enum.VarType.R8,false)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getUCSGridSizeX() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUCSGridSizeX(float pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getUCSGridSizeY() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUCSGridSizeY(float pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getUCSGridSectionSizeX() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUCSGridSectionSizeX(float pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getUCSGridSectionSizeY() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUCSGridSectionSizeY(float pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getUCSGridVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUCSGridVisible(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getUndoSize() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUndoSize(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getFloorSize() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setFloorSize(float pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getFloorSectionSize() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setFloorSectionSize(float pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getStepRate() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setStepRate(float pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getShowCoordinateSystemPart() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setShowCoordinateSystemPart(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(152,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getShowCoordinateSystemAssembly() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(156,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setShowCoordinateSystemAssembly(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(160,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.Variant getCustomColor(int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,false),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(164,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    com.inzoom.comjni.Variant rv = _v[1].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCustomColor(int Index,com.inzoom.comjni.Variant pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,false),
      createVTblVArg(pVal)
    };
    vtblCall(168,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getLengthUnit() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(172,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLengthUnit(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(176,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getVolumeUnit() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVolumeUnit(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(184,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getAreaUnit() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(188,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAreaUnit(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(192,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getAngleUnit() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(196,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAngleUnit(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(200,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getAngularVelocityUnit() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(204,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAngularVelocityUnit(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(208,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getVelocityUnit() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(212,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVelocityUnit(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(216,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getAngularAccelerationUnit() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(220,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAngularAccelerationUnit(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(224,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getAccelerationUnit() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(228,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAccelerationUnit(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(232,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public double getTargetOffset() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(236,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    double rv = _v[0].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTargetOffset(double pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,com.inzoom.comjni.enum.VarType.R8,false)
    };
    vtblCall(240,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getApprVec() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(244,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setApprVec(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(248,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTravelVec() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(252,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTravelVec(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(256,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getActiveProcess() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(260,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setActiveProcess(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(264,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public double getUpdateRate() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(268,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    double rv = _v[0].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUpdateRate(double pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,com.inzoom.comjni.enum.VarType.R8,false)
    };
    vtblCall(272,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.Variant getUCSGridColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(276,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUCSGridColor(com.inzoom.comjni.Variant pRGB) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(pRGB)
    };
    vtblCall(280,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(284,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication3JCW.getIApplication3FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getUseRobotConfiguration() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(288,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setUseRobotConfiguration(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(292,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getRefreshProgramBrowser() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(296,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRefreshProgramBrowser(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(300,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
