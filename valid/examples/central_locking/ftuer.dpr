@project ftuer {
@events {
stop_VR {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
MER_an {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_ZS {
  @controllable
  @generateCodeFragment
}
stop_ZS {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
time_ER {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
tuer_auf {
  @uncontrollable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
start_ER {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
reset {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
MVR_aus {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
done_ER {
  @controllable
  @generateCodeFragment
}
async {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
sync {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
time_VR {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
MVR_an {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_SER {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_CS {
  @controllable
  @generateCodeFragment
}
time_ZS {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
start_VR {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_ER {
  @controllable
  @generateCodeFragment
}
done_VR {
  @controllable
  @generateCodeFragment
}
time_W {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
tuer_zu {
  @uncontrollable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
stop_ER {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
start_ZS {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
done_ZS {
  @controllable
  @generateCodeFragment
}
MER_aus {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
STZV {
  @uncontrollable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
timeout {
  @uncontrollable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_VR {
  @controllable
  @generateCodeFragment
}
CS_ok {
  @controllable
  @generateCodeFragment
}
done_SER {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
}
@use {
  mer
  tuermodell
  ttimguard
  tklemme
  sync
  ftuer
  tmotor1
  ttimer
  mvrguard
  mvrstzv
  merguard
}
@plant mer {
  @cloning {mer}
}
@instantiation _tuermodell__tuermodell {
  @parameters {
    { _0 @any }
  }
  @rules {
    { FT $_0 }
  }
}
<@plant _0 {TUER}
  tuermodell {
    @cloning {tuermodell}
    @instantiating {_tuermodell__tuermodell $_0}
  }
>
@spec ttimguard {
  @cloning {ttimguard}
}
@plant tklemme {
  @cloning {tklemme}
}
@spec sync {
  @cloning {sync}
}
@spec ftuer {
  @cloning {ftuer}
}
@spec tmotor1 {
  @cloning {tmotor1}
}
@plant ttimer {
  @cloning {ttimer}
}
@spec mvrguard {
  @cloning {mvrguard}
}
@plant mvrstzv {
  @cloning {mvrstzv}
}
@spec merguard {
  @cloning {merguard}
}
}

