package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface CommandBarControl Implementation
public class CommandBarControlJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoOleAccDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl getCommandBarControlFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControlJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl getCommandBarControlFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControlJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl getCommandBarControlFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarControlJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl convertComPtrToCommandBarControl(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControlJCW(comPtr,true,releaseComPtr); }
  protected CommandBarControlJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarControlJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID); }
  protected CommandBarControlJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected CommandBarControlJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID); }
  protected CommandBarControlJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected CommandBarControlJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID,releaseComPtr);}
  protected CommandBarControlJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public boolean getBeginGroup() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBeginGroup(boolean pvarfBeginGroup) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfBeginGroup,false)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getBuiltIn() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getCaption() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCaption(String pbstrCaption) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrCaption,false)
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.IDispatch getControl() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl copy(com.inzoom.comjni.Variant Bar,com.inzoom.comjni.Variant Before) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Bar),
      createVTblVArg(Before),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl copy(com.inzoom.comjni.Variant Bar) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Bar),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl copy() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete(com.inzoom.comjni.Variant Temporary) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Temporary)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getDescriptionText() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(152,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDescriptionText(String pbstrText) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrText,false)
    };
    vtblCall(156,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getEnabled() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(160,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setEnabled(boolean pvarfEnabled) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfEnabled,false)
    };
    vtblCall(164,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void execute() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(168,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getHeight() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(172,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHeight(int pdy) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pdy,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(176,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getHelpContextId() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHelpContextId(int pid) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pid,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(184,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getHelpFile() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(188,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHelpFile(String pbstrFilename) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrFilename,false)
    };
    vtblCall(192,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getId() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(196,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getIndex() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(200,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getInstanceId() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(204,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl move(com.inzoom.comjni.Variant Bar,com.inzoom.comjni.Variant Before) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Bar),
      createVTblVArg(Before),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(208,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl move(com.inzoom.comjni.Variant Bar) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Bar),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(208,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl move() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(208,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getLeft() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(212,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getOLEUsage() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(216,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setOLEUsage(int pcou) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pcou,false)
    };
    vtblCall(220,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getOnAction() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(224,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setOnAction(String pbstrOnAction) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrOnAction,false)
    };
    vtblCall(228,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(232,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getParameter() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(236,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setParameter(String pbstrParam) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrParam,false)
    };
    vtblCall(240,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getPriority() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(244,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setPriority(int pnPri) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pnPri,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(248,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void reset() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(252,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setFocus() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(256,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getTag() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(260,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTag(String pbstrTag) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrTag,false)
    };
    vtblCall(264,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getTooltipText() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(268,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTooltipText(String pbstrTooltip) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrTooltip,false)
    };
    vtblCall(272,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTop() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(276,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(280,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(284,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(boolean pvarfVisible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfVisible,false)
    };
    vtblCall(288,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getWidth() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(292,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setWidth(int pdx) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pdx,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(296,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getIsPriorityDropped() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(300,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void reserved1() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(304,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void reserved2() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(308,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void reserved3() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(312,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void reserved4() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(316,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void reserved5() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(320,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void reserved6() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(324,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void reserved7() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(328,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
