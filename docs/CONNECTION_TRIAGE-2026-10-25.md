# Upstream Connection triage — 2026-10-25

Local-only record for TuPigPlay issues #106 (cannot fix), #107 (already in tree), #108 (home card / upstream #742). GitHub comments may be added when `gh` auth is available; do not push to upstream.

## Already in tree (`#107`)

| Upstream | Topic | Local evidence |
| --- | --- | --- |
| #921 #971 #991 #958 #1036 #955… | Hotspot picked `rmnet_data*` | `mobileDataInterfaceName`, unobservable tether (#936/#950) |
| #889 #335 | Wired second start timed out at 30s | `WiredStartWatchdog` 45s hold (#1057) |
| #953 #763 #1038 | No iPhone on USB wait | `#1060` USB device messaging |
| #1004 shape | No AirPlay TCP after StartSession | TuPigPlay #104 multi-address StartSession |
| #742 | Wired shown as Wireless on home card | `HomeCardTransport` + `#744` |

## Cannot fix in-app (`#106`)

| Upstream | Why | Workaround |
| --- | --- | --- |
| #1030 #890 | Unisoc/SPRD USB bulk-IN never completes | Wireless |
| #764 #581 #709 | App RFCOMM unusable; OEM BT still works | USB if possible; needs vendor BT |
| #442 | `requestPermission` never returns | Wireless; OEM USB grant |
| #792 | CH341 MFi missing on later attempts | Replug; offline auth build |
| #1088 | iAP2 107 unacked without decisive capture | Diagnostic report + focused retest |

## Local connection work this round (not pushed)

- #103 `fix/103-prefs-migration` — prefs → `tupigcarplay*` + migrate
- #104 `fix/104-startsession-multi-ip` — StartSession multi-IP tests
- #105 `fix/105-leapmotor-os` — Leapmotor OS user-facing copy
