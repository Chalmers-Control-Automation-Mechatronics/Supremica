@project koordinator {
@type Timers {FS WSP}
@type Counters {WSP}
@type Tueren {FT BT HT}
@events {
cod_er {
  @uncontrollable
  @generateCodeFragment
}
SERFS {
  @controllable
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
zuendung_an {
  @uncontrollable
  @generateCodeFragment
}
zuendung_aus {
  @uncontrollable
  @generateCodeFragment
}
tuer_auf {
  @uncontrollable
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
done_ER {
  @controllable
  @generateCodeFragment
}
TAST {
  @controllable
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
soft_ER {
  @controllable
  @generateCodeFragment
}
CS {
  @uncontrollable
  @generateCodeFragment
}
cod_ser {
  @uncontrollable
  @generateCodeFragment
}
done_VR {
  @controllable
  @generateCodeFragment
}
ZSA {
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
ER {
  @controllable
  @generateCodeFragment
}
autom_VR {
  @controllable
  @generateCodeFragment
}
ctr_ge_max {
  @uncontrollable
  @signature {
    {_0 Counters}
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
soft_VR {
  @controllable
  @generateCodeFragment
}
CS_ok {
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
  wspcount
  noreset
  tuermodell
  ircount
  tklemme
  automvr
  eingabe_er
  eingabe_fs
  crash
  decoder
  timer
  keine_wsp
  zsi
  cod_ser
  eingabe_ser
  eingabe_vr
  check_cod_ser
  zuendung
  kein_zs
  wsptime
  warten
  eingabe_zs
}
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
@spec tuermodell {
  @cloning {tuermodell}
}
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
@spec automvr {
  @cloning {automvr}
}
@spec eingabe_er {
  @cloning {eingabe_er}
}
@spec eingabe_fs {
  @cloning {eingabe_fs}
}
@spec crash {
  @cloning {crash}
}
@spec decoder {
  @cloning {decoder}
}
@instantiation _timer__timer {
  @parameters {
    { _0 @any }
  }
  @rules {
    { NAME $_0 }
  }
}
<@plant _0 Timers
  timer {
    @cloning {timer}
    @instantiating {_timer__timer $_0}
  }
>
@spec keine_wsp {
  @cloning {keine_wsp}
}
@spec zsi {
  @cloning {zsi}
}
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
@spec wsptime {
  @cloning {wsptime}
}
@spec warten {
  @cloning {warten}
}
@spec eingabe_zs {
  @cloning {eingabe_zs}
}
}

