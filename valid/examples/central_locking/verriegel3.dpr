@project verriegel3 {
@type HeckTueren {HT}
@type Timers {FT BT HT FS WSP}
@type NichtFahrerTueren {BT HT}
@type Counters {WSP}
@type FahrerTueren {FT}
@type VorderTueren {FT BT}
@type Tueren {FT BT HT}
@events {
cod_er {
  @uncontrollable
  @generateCodeFragment
}
stop_VR {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
SERFS {
  @controllable
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
ZS {
  @controllable
  @generateCodeFragment
}
kein_ZS {
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
zuendung_an {
  @uncontrollable
  @generateCodeFragment
}
zuendung_aus {
  @uncontrollable
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
TAST {
  @controllable
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
ctr_lt_max {
  @uncontrollable
  @signature {
    {_0 Counters}
  }
  @generateCodeFragment
}
ER_BT {
  @uncontrollable
  @generateCodeFragment
}
time {
  @controllable
  @signature {
    {_0 Timers}
  }
  @generateCodeFragment
}
WSP {
  @controllable
  @generateCodeFragment
}
ER_FS {
  @uncontrollable
  @generateCodeFragment
}
ER_FT {
  @uncontrollable
  @generateCodeFragment
}
ER_HK {
  @uncontrollable
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
soft_CS {
  @controllable
  @generateCodeFragment
}
soft_SER {
  @controllable
  @signature {
    {_0 Tueren}
  }
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
CS {
  @uncontrollable
  @generateCodeFragment
}
soft_ER {
  @controllable
  @generateCodeFragment
}
cod_ser {
  @uncontrollable
  @generateCodeFragment
}
time_W {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
done_VR {
  @controllable
  @generateCodeFragment
}
tuer_zu {
  @uncontrollable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
ZSA {
  @controllable
  @generateCodeFragment
}
ER {
  @controllable
  @generateCodeFragment
}
autom_VR {
  @controllable
  @generateCodeFragment
}
stop_ER {
  @controllable
  @signature {
    {_0 Tueren}
  }
  @generateCodeFragment
}
ctr_ge_max {
  @uncontrollable
  @signature {
    {_0 Counters}
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
ZSI {
  @controllable
  @generateCodeFragment
}
done_ZS {
  @controllable
  @generateCodeFragment
}
SER {
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
TASTVR {
  @uncontrollable
  @generateCodeFragment
}
timeout {
  @uncontrollable
  @signature {
    {_0 Timers}
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
is_cod_ser {
  @controllable
  @generateCodeFragment
}
is_cod_er {
  @controllable
  @generateCodeFragment
}
reset_ctr {
  @controllable
  @signature {
    {_0 Counters}
  }
  @generateCodeFragment
}
VR_BT {
  @uncontrollable
  @generateCodeFragment
}
inc_ctr {
  @controllable
  @signature {
    {_0 Counters}
  }
  @generateCodeFragment
}
CS_ok {
  @controllable
  @generateCodeFragment
}
soft_VR {
  @controllable
  @generateCodeFragment
}
VR_FS {
  @uncontrollable
  @generateCodeFragment
}
VR_FT {
  @uncontrollable
  @generateCodeFragment
}
VR_HK {
  @uncontrollable
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
  wspcount
  noreset
  ircount
  nftuer
  ttimguard
  tklemme
  mvr
  automvr
  eingabe_er
  eingabe_fs
  mzs
  crash
  sync
  decoder
  ftuer
  keine_wsp
  timer
  tmotor1
  tmotor2
  ttimer
  zsi
  mvrguard
  cod_ser
  eingabe_ser
  eingabe_vr
  check_cod_ser
  zuendung
  kein_zs
  warten
  wsptime
  eingabe_zs
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
@spec wspcount {
  @cloning {wspcount}
}
@instantiation _noreset__noreset {
  @parameters {
    { _0 @any }
  }
  @rules {
    { NAME $_0 }
  }
}
<@spec _0 {WSP}
  noreset {
    @cloning {noreset}
    @instantiating {_noreset__noreset $_0}
  }
>
@instantiation _ircount__ircount {
  @parameters {
    { _0 @any }
  }
  @rules {
    { NAME $_0 }
  }
}
<@plant _0 {WSP}
  ircount {
    @cloning {ircount}
    @instantiating {_ircount__ircount $_0}
  }
>
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
@spec automvr {
  @cloning {automvr}
}
@spec eingabe_er {
  @cloning {eingabe_er}
}
@spec eingabe_fs {
  @cloning {eingabe_fs}
}
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
@spec crash {
  @cloning {crash}
}
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
@spec decoder {
  @cloning {decoder}
}
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
@spec keine_wsp {
  @cloning {keine_wsp}
}
@instantiation _timer__timer {
  @parameters {
    { _0 @any }
  }
  @rules {
    { NAME $_0 }
  }
}
<@plant _0 {WSP FS}
  timer {
    @cloning {timer}
    @instantiating {_timer__timer $_0}
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
@spec zsi {
  @cloning {zsi}
}
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
@plant cod_ser {
  @cloning {cod_ser}
}
@spec eingabe_ser {
  @cloning {eingabe_ser}
}
@spec eingabe_vr {
  @cloning {eingabe_vr}
}
@spec check_cod_ser {
  @cloning {check_cod_ser}
}
@plant zuendung {
  @cloning {zuendung}
}
@spec kein_zs {
  @cloning {kein_zs}
}
@spec warten {
  @cloning {warten}
}
@spec wsptime {
  @cloning {wsptime}
}
@spec eingabe_zs {
  @cloning {eingabe_zs}
}
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

