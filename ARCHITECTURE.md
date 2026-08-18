# EchoSmart Android architecture

Feature-first modular structure:

- `core/ui`: shared visual primitives and design-system components.
- `data`: API clients, Azure service catalog and live network probes.
- `domain/server`: pure sorting/business rules.
- `engine`: VPN engine contracts, Android VpnService and IKEv2 provisioning.
- `feature/auth`: registration, login and password reset UI.
- `feature/home`: connection dashboard and protocol selection.
- `feature/servers`: country list, live latency/jitter, load and sorting.
- `feature/proxies`: SOCKS5, HTTPS and SOCKS4 endpoint catalog.
- `feature/settings`: protocol, kill switch, fallback and split-tunnel preferences.
- `feature/account`: user profile and session actions.
- `feature/admin`: clients, sessions, nodes and regional service health.
- `viewmodel`: isolated application, server telemetry and settings state holders.
- `model`: immutable domain/UI models.

UI modules do not perform network I/O. `ServersViewModel` owns latency probes;
`EchoSmartViewModel` coordinates authenticated session and connection state;
`SettingsViewModel` owns preferences. Pure ordering logic is unit-tested in
`ServerSorterTest`.

## Proton-style experience layer

The current product experience adds a global map, quick-connect profiles,
activity-oriented profiles, searchable/sortable regional servers, live
latency/jitter, NetShield DNS modes, persisted kill-switch/accelerator settings,
and real tunnel byte statistics. Branding and visual assets remain EchoSmart.

## Production tunnel engines

- WireGuard: embedded official `com.wireguard.android:tunnel`, peer provisioned
  through the authenticated Azure backend and installed into every regional
  Azure gateway with ARM Run Command.
- SOCKS5: embedded MIT-licensed HevSocks5Tunnel native tun2socks engine with
  TCP/UDP forwarding and credentials retrieved from Key Vault through the API.
- IKEv2: Android `VpnManager` provisioning adapter exists; profile enrollment
  remains a system-managed flow.
- OpenVPN: regional `.ovpn` profiles are generated and exported; an embedded
  OpenVPN runtime is not bundled in this build.
