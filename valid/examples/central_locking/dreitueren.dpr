@project dreitueren {
@type HeckTueren {HT}
@type Timers {FT BT HT}
@type NichtFahrerTueren {BT HT}
@type FahrerTueren {FT}
@type VorderTueren {FT BT}
@type Tueren {FT BT HT}
@events {
stop_VR {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
MER_an {
  @controllable
  @signature {
    {_0 Tueren}
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
    {_0 Tueren}
  }
  @generateCodeFragment
}
time_ER {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
tuer_auf {
  @uncontrollable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
start_ER {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
reset {
  @controllable
  @signature {
    {_0 Timers}
  }
  @generateCodeFragment
}
MVR_aus {
  @controllable
  @signature {
    {_0 Tueren}
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
    {_0 Tueren}
  }
  @generateCodeFragment
}
sync {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
time_VR {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
MVR_an {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
MZS_aus {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
soft_SER {
  @controllable
  @signature {
    {_0 Tueren}
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
    {_0 Tueren}
  }
  @generateCodeFragment
}
start_VR {
  @controllable
  @signature {
    {_0 Tueren}
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
    {_0 Tueren}
  }
  @generateCodeFragment
}
tuer_zu {
  @uncontrollable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
stop_ER {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
start_ZS {
  @controllable
  @signature {
    {_0 Tueren}
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
    {_0 Tueren}
  }
  @generateCodeFragment
}
MZS_an {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
STZV {
  @uncontrollable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
timeout {
  @uncontrollable
  @signature {
    {_0 Timers}
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
    {_0 Tueren}
  }
  @generateCodeFragment
}
}
@use {
  mer
  tuermodell
  nftuer
  ttimguard
  tklemme
  mvr
  mzs
  sync
  ftuer
  tmotor1
  tmotor2
  ttimer
  mvrguard
  mvrstzv
  mzsguard
  merguard
}
@instantiation _mer__mer {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@plant _0 Tueren
  mer {
    @cloning {mer}
    @instantiating {_mer__mer $_0}
  }
>
@plant tuermodell {
  @cloning {tuermodell}
}
@instantiation _nftuer__nftuer {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@spec _0 NichtFahrerTueren
  nftuer {
    @cloning {nftuer}
    @instantiating {_nftuer__nftuer $_0}
  }
>
@instantiation _ttimguard__ttimguard {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@spec _0 Tueren
  ttimguard {
    @cloning {ttimguard}
    @instantiating {_ttimguard__ttimguard $_0}
  }
>
@instantiation _tklemme__tklemme {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@plant _0 Tueren
  tklemme {
    @cloning {tklemme}
    @instantiating {_tklemme__tklemme $_0}
  }
>
@instantiation _mvr__mvr {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@plant _0 HeckTueren
  mvr {
    @cloning {mvr}
    @instantiating {_mvr__mvr $_0}
  }
>
@instantiation _mzs__mzs {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@plant _0 HeckTueren
  mzs {
    @cloning {mzs}
    @instantiating {_mzs__mzs $_0}
  }
>
@instantiation _sync__sync {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@spec _0 Tueren
  sync {
    @cloning {sync}
    @instantiating {_sync__sync $_0}
  }
>
@instantiation _ftuer__ftuer {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@spec _0 FahrerTueren
  ftuer {
    @cloning {ftuer}
    @instantiating {_ftuer__ftuer $_0}
  }
>
@instantiation _tmotor1__tmotor1 {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@spec _0 VorderTueren
  tmotor1 {
    @cloning {tmotor1}
    @instantiating {_tmotor1__tmotor1 $_0}
  }
>
@instantiation _tmotor2__tmotor2 {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@spec _0 HeckTueren
  tmotor2 {
    @cloning {tmotor2}
    @instantiating {_tmotor2__tmotor2 $_0}
  }
>
@instantiation _ttimer__ttimer {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@plant _0 Tueren
  ttimer {
    @cloning {ttimer}
    @instantiating {_ttimer__ttimer $_0}
  }
>
@instantiation _mvrguard__mvrguard {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@spec _0 Tueren
  mvrguard {
    @cloning {mvrguard}
    @instantiating {_mvrguard__mvrguard $_0}
  }
>
@instantiation _mvrstzv__mvrstzv {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@plant _0 VorderTueren
  mvrstzv {
    @cloning {mvrstzv}
    @instantiating {_mvrstzv__mvrstzv $_0}
  }
>
@instantiation _mzsguard__mzsguard {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@spec _0 HeckTueren
  mzsguard {
    @cloning {mzsguard}
    @instantiating {_mzsguard__mzsguard $_0}
  }
>
@instantiation _merguard__merguard {
  @parameters {
    { _0 @any }
  }
  @rules {
    { TUER $_0 }
  }
}
<@spec _0 Tueren
  merguard {
    @cloning {merguard}
    @instantiating {_merguard__merguard $_0}
  }
>
}

