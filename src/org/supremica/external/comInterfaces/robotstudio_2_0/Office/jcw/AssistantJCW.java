package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface Assistant Implementation
public class AssistantJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant getAssistantFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new AssistantJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant getAssistantFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new AssistantJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant getAssistantFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new AssistantJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant convertComPtrToAssistant(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new AssistantJCW(comPtr,true,releaseComPtr); }
  protected AssistantJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected AssistantJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID); }
  protected AssistantJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected AssistantJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID); }
  protected AssistantJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected AssistantJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID,releaseComPtr);}
  protected AssistantJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void move(int xLeft,int yTop) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(xLeft,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(yTop,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setTop(int pyTop) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pyTop,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTop() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLeft(int pxLeft) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pxLeft,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getLeft() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void help() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int startWizard(boolean On,String Callback,int PrivateX,com.inzoom.comjni.Variant Animation,com.inzoom.comjni.Variant CustomTeaser,com.inzoom.comjni.Variant Top,com.inzoom.comjni.Variant Left,com.inzoom.comjni.Variant Bottom,com.inzoom.comjni.Variant Right) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(On,false),
      new com.inzoom.comjni.Variant(Callback,false),
      new com.inzoom.comjni.Variant(PrivateX,false),
      createVTblVArg(Animation),
      createVTblVArg(CustomTeaser),
      createVTblVArg(Top),
      createVTblVArg(Left),
      createVTblVArg(Bottom),
      createVTblVArg(Right),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[9].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int startWizard(boolean On,String Callback,int PrivateX,com.inzoom.comjni.Variant Animation,com.inzoom.comjni.Variant CustomTeaser,com.inzoom.comjni.Variant Top,com.inzoom.comjni.Variant Left,com.inzoom.comjni.Variant Bottom) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(On,false),
      new com.inzoom.comjni.Variant(Callback,false),
      new com.inzoom.comjni.Variant(PrivateX,false),
      createVTblVArg(Animation),
      createVTblVArg(CustomTeaser),
      createVTblVArg(Top),
      createVTblVArg(Left),
      createVTblVArg(Bottom),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[9].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int startWizard(boolean On,String Callback,int PrivateX,com.inzoom.comjni.Variant Animation,com.inzoom.comjni.Variant CustomTeaser,com.inzoom.comjni.Variant Top,com.inzoom.comjni.Variant Left) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(On,false),
      new com.inzoom.comjni.Variant(Callback,false),
      new com.inzoom.comjni.Variant(PrivateX,false),
      createVTblVArg(Animation),
      createVTblVArg(CustomTeaser),
      createVTblVArg(Top),
      createVTblVArg(Left),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[9].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int startWizard(boolean On,String Callback,int PrivateX,com.inzoom.comjni.Variant Animation,com.inzoom.comjni.Variant CustomTeaser,com.inzoom.comjni.Variant Top) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(On,false),
      new com.inzoom.comjni.Variant(Callback,false),
      new com.inzoom.comjni.Variant(PrivateX,false),
      createVTblVArg(Animation),
      createVTblVArg(CustomTeaser),
      createVTblVArg(Top),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[9].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int startWizard(boolean On,String Callback,int PrivateX,com.inzoom.comjni.Variant Animation,com.inzoom.comjni.Variant CustomTeaser) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(On,false),
      new com.inzoom.comjni.Variant(Callback,false),
      new com.inzoom.comjni.Variant(PrivateX,false),
      createVTblVArg(Animation),
      createVTblVArg(CustomTeaser),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[9].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int startWizard(boolean On,String Callback,int PrivateX,com.inzoom.comjni.Variant Animation) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(On,false),
      new com.inzoom.comjni.Variant(Callback,false),
      new com.inzoom.comjni.Variant(PrivateX,false),
      createVTblVArg(Animation),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[9].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int startWizard(boolean On,String Callback,int PrivateX) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(On,false),
      new com.inzoom.comjni.Variant(Callback,false),
      new com.inzoom.comjni.Variant(PrivateX,false),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[9].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void endWizard(int WizardID,boolean varfSuccess,com.inzoom.comjni.Variant Animation) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(WizardID,false),
      new com.inzoom.comjni.Variant(varfSuccess,false),
      createVTblVArg(Animation)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void endWizard(int WizardID,boolean varfSuccess) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(WizardID,false),
      new com.inzoom.comjni.Variant(varfSuccess,false),
      createVTblVArg(noParam)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void activateWizard(int WizardID,int act,com.inzoom.comjni.Variant Animation) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(WizardID,false),
      new com.inzoom.comjni.Variant(act,false),
      createVTblVArg(Animation)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void activateWizard(int WizardID,int act) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(WizardID,false),
      new com.inzoom.comjni.Variant(act,false),
      createVTblVArg(noParam)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void resetTips() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Balloon getNewBalloon() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Balloon rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.BalloonJCW.getBalloonFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getBalloonError() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(boolean pvarfVisible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfVisible,false)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getAnimation() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAnimation(int pfca) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pfca,false)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getReduced() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setReduced(boolean pvarfReduced) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfReduced,false)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setAssistWithHelp(boolean pvarfAssistWithHelp) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfAssistWithHelp,false)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getAssistWithHelp() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAssistWithWizards(boolean pvarfAssistWithWizards) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfAssistWithWizards,false)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getAssistWithWizards() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAssistWithAlerts(boolean pvarfAssistWithAlerts) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfAssistWithAlerts,false)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getAssistWithAlerts() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMoveWhenInTheWay(boolean pvarfMove) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfMove,false)
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getMoveWhenInTheWay() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setSounds(boolean pvarfSounds) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfSounds,false)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getSounds() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setFeatureTips(boolean pvarfFeatures) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfFeatures,false)
    };
    vtblCall(152,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getFeatureTips() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(156,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMouseTips(boolean pvarfMouse) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfMouse,false)
    };
    vtblCall(160,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getMouseTips() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(164,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setKeyboardShortcutTips(boolean pvarfKeyboardShortcuts) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfKeyboardShortcuts,false)
    };
    vtblCall(168,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getKeyboardShortcutTips() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(172,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHighPriorityTips(boolean pvarfHighPriorityTips) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfHighPriorityTips,false)
    };
    vtblCall(176,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getHighPriorityTips() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTipOfDay(boolean pvarfTipOfDay) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfTipOfDay,false)
    };
    vtblCall(184,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getTipOfDay() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(188,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setGuessHelp(boolean pvarfGuessHelp) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfGuessHelp,false)
    };
    vtblCall(192,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getGuessHelp() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(196,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setSearchWhenProgramming(boolean pvarfSearchInProgram) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfSearchInProgram,false)
    };
    vtblCall(200,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getSearchWhenProgramming() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(204,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getItem() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(208,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getFileName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(212,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setFileName(String pbstr) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstr,false)
    };
    vtblCall(216,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(220,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getOn() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(224,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setOn(boolean pvarfOn) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfOn,false)
    };
    vtblCall(228,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Assistant.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
