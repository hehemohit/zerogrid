# ZeroGrid 🌐

> **Decentralized, Off-Grid Peer-to-Peer Mesh Communication & Emergency Network for Android**

ZeroGrid is a decentralized off-grid mesh communication application that enables secure peer-to-peer messaging, channel broadcasts, multi-hop file sharing, and emergency SOS alerts without relying on cellular networks or internet connectivity.

---

## 📌 Project Status & Overview

```
┌────────────────────────────────────────────────────────────────────────┐
│ UI / Frontend Layer (Jetpack Compose Material 3)    [ ✔ COMPLETED ]    │
│ Navigation & Screen Routing                         [ ✔ COMPLETED ]    │
│ Design System & Dark Theming                        [ ✔ COMPLETED ]    │
│ C++ JNI Scaffold (CMake / NDK)                      [ ✔ CONFIGURED ]   │
│ ────────────────────────────────────────────────────────────────────── │
│ BLE / Wi-Fi Direct Mesh Networking Engine           [ ⏳ PENDING ]     │
│ Multi-hop Packet Routing & Peer Discovery           [ ⏳ PENDING ]     │
│ End-to-End Encryption (E2EE) & Key Exchange         [ ⏳ PENDING ]     │
│ Local Database (Room / SQLCipher) & Storage         [ ⏳ PENDING ]     │
│ Foreground Service (Mesh Node Background Daemon)    [ ⏳ PENDING ]     │
│ ViewModel & Repository Data Binding                 [ ⏳ PENDING ]     │
└────────────────────────────────────────────────────────────────────────┘
```

> [!NOTE]
> **To the Backend / Systems Engineer:**
> All UI screens, layout states, mock interactions, and navigation structures are **fully designed and implemented** in Jetpack Compose.
> Your primary objective is to build the networking core (Wi-Fi Direct / BLE / multi-hop routing), local persistence, cryptographic layer, Android runtime permissions/services, and bind them to the existing UI composables via ViewModels.

---

## 🛠 Tech Stack & Environment

- **Language:** Kotlin (`17` JVM target) & C++17 (Native NDK)
- **UI Framework:** Jetpack Compose (Material 3 + Compose BOM)
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)
- **Native Build:** CMake `3.22.1`, Android NDK `27.0.12077973`
- **Target SDK:** Android 15 (API 35) | **Min SDK:** Android 8.0 (API 26)

---

## 🎨 Completed Frontend Modules (What's in the Box)

All user interface screens are organized by feature domain under `app/src/main/java/com/example/zerogrid/`:

### 1. 🏠 Home & Dashboard (`home/`)
- [`MeshDashboardScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/home/MeshDashboardScreen.kt)
  - Active node identity header (Node ID, Signal strength, Battery level).
  - Real-time mesh connection status card (hop count, routing mode, active peers count).
  - Quick action shortcuts (Send Message, Share File, Channels, SOS Beacon).
  - Nearby discovered peers list with signal dBm, hop distance, and device roles.
  - Floating SOS emergency broadcast FAB.

### 2. 💬 Messaging & Channels (`messaging/`)
- [`MessagesScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/messaging/MessagesScreen.kt)
  - Direct 1-on-1 conversations list with unread badges, timestamp, encryption status, and last seen metrics.
  - Search bar and floating action button to initiate new direct peer conversations.
- [`ChannelsScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/messaging/ChannelsScreen.kt)
  - Public and localized mesh broadcast channels list (e.g., `#general`, `#emergency`, `#camp-alpha`).
  - Channel subscription status, active members counter, and channel creation modal/flow.
- [`ChannelChatScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/messaging/ChannelChatScreen.kt)
  - Real-time chat timeline with incoming/outgoing message bubbles.
  - Message delivery status indicators (Sent, Relayed via X Hops, Delivered, Read).
  - Attachment preview, voice memo placeholders, and mesh packet relay info.

### 3. 📡 Mesh Management & Discovery (`mesh/`)
- [`NearbyDevicesScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/mesh/NearbyDevicesScreen.kt)
  - BLE & Wi-Fi Direct peer radar/scanner list.
  - Signal strength indicators (RSSI dBm), transport type (BLE/Wi-Fi Direct), and connection state.
  - Manual connection trigger and discovery refresh.
- [`PeerDetailsScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/mesh/PeerDetailsScreen.kt)
  - Detailed peer telemetry (Public Key fingerprint, MAC/Bluetooth address, Ping/RTT latency, Relay status).
  - Actions: Direct Message, Send File, Block/Trust Peer, Ping Route.
- [`NetworkStatusScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/mesh/NetworkStatusScreen.kt)
  - Mesh topology statistics: Active nodes, direct neighbors, relay hops, packets forwarded, bandwidth throughput.

### 4. 📁 File Sharing & Transfer (`files/`)
- [`FilesScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/files/FilesScreen.kt)
  - Offline file explorer showing received and shared media, documents, and APK packages.
  - Storage usage breakdown and filter tabs (All, Images, Documents, Media).
- [`SendFileScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/files/SendFileScreen.kt)
  - File picker interface, recipient selection (direct peer vs. multi-hop broadcast), and transfer compression options.
- [`FileTransferScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/files/FileTransferScreen.kt)
  - Live transfer progress bar with chunk transfer rate (KB/s), estimated time remaining, SHA-256 chunk validation status, and pause/cancel controls.

### 5. 🚨 Emergency & SOS Beacon (`emergency/`)
- [`SosCenterScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/emergency/SosCenterScreen.kt)
  - High-visibility emergency control room.
  - Active incoming emergency alerts received over the mesh with GPS coordinates, sender ID, timestamp, and distance estimate.
  - Quick emergency guides (Medical, Disaster, Rescue).
- [`SendSosScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/emergency/SendSosScreen.kt)
  - Emergency beacon trigger with countdown cancel shield to prevent accidental triggers.
  - Incident category selector (Medical, Natural Disaster, Lost/Trapped, Security Threat).
  - Custom emergency message and GPS location attachment toggle.

### 6. 🚀 Onboarding & Identity (`onboarding/`)
- [`SplashScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/onboarding/SplashScreen.kt): Animated brand entrance.
- [`OnboardingScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/onboarding/OnboardingScreen.kt): Feature walkthrough explaining off-grid mesh concepts.
- [`PermissionsScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/onboarding/PermissionsScreen.kt): UI requesting Bluetooth, Nearby Devices, Location, and Wi-Fi permissions.
- [`CreateIdentityScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/onboarding/CreateIdentityScreen.kt): Cryptographic node pseudonym, avatar generator, and public/private key generation prompt.

### 7. ⚙️ Settings & Privacy (`settings/`)
- [`SettingsScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/settings/SettingsScreen.kt): Radio configurations, routing preferences, battery optimization settings.
- [`SecurityPrivacyScreen.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/settings/SecurityPrivacyScreen.kt): Encryption key management, public key export/QR code, identity reset, anonymous routing toggles.

### 8. 🗺 Navigation & Theme (`navigation/`, `ui/theme/`)
- [`Routes.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/navigation/Routes.kt): `Screen` enum definition for all routes.
- [`NavGraph.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/navigation/NavGraph.kt): Compose navigation controller with back-stack support and back-press handlers.
- [`BottomNavigation.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/navigation/BottomNavigation.kt): Persistent bottom navigation bar with active route highlighting.
- [`Color.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/ui/theme/Color.kt) & [`Theme.kt`](file:///c:/Users/ACER/AndroidStudioProjects/gridzero/app/src/main/java/com/example/zerogrid/ui/theme/Theme.kt): Tailored dark mesh cybernetic palette (Neon Cyan, Emergency Orange/Red, Tactical Slate).

---

## 🏗 Backend & Networking Handover: Tasks to Implement

The backend/networking engineer has the following responsibilities to turn this UI into a functional off-grid mesh app:

### 1. 📡 P2P Hardware Discovery & Transports
- [ ] **Android Manifest Permissions (`AndroidManifest.xml`):**
  - Add `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`, `ACCESS_FINE_LOCATION`, `NEARBY_WIFI_DEVICES`, `CHANGE_WIFI_MULTICAST_STATE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE`.
- [ ] **BLE Mesh Driver:**
  - Implement GATT Server & Client for low-power neighbor discovery and small packet exchange (keepalives, handshakes, SOS beacons).
- [ ] **Wi-Fi Direct (P2P) Driver:**
  - Implement `WifiP2pManager` discovery, group negotiation, and socket channels for high-bandwidth data (file transfers, images).

### 2. 🔄 Mesh Routing Protocol
- [ ] **Packet Definition & Serialization:**
  - Header: `packet_id`, `source_pubkey`, `dest_pubkey`, `ttl / hop_limit`, `payload_type`, `payload_len`, `signature`.
- [ ] **Multi-Hop Relay Engine:**
  - Routing algorithm (e.g. Flooding with Deduplication Cache, Epidemic Routing, or reactive AODV).
  - Loop prevention (seen packet bloom filter / TTL decrement).
- [ ] **Peer Table & Heartbeat:**
  - Dynamic routing table tracking active neighbors, RSSI metrics, and hop counts.

### 3. 🔐 Cryptography & Key Management (E2EE)
- [ ] **Identity & Key Generation:**
  - Asymmetric key generation (Ed25519 for signing, X25519 for ECDH key exchange) using Android KeyStore or native Libsodium.
- [ ] **Message Encryption:**
  - Noise Protocol / Signal Double Ratchet or ChaCha20-Poly1305 payload encryption for 1-to-1 chats.
  - Channel broadcast signature validation.

### 4. 🗄 Local Persistence & Database (Room / SQLCipher)
- [ ] **Entity Models & DAOs:**
  - `MessageEntity` (id, conversationId, senderId, text, timestamp, status, hopCount).
  - `PeerEntity` (pubKey, alias, rssi, lastSeen, isBlocked, isTrusted).
  - `ChannelEntity` (channelId, name, description, isSubscribed).
  - `FileTransferEntity` (transferId, fileName, totalBytes, transferredBytes, status, path).
  - `SosEventEntity` (sosId, senderId, latitude, longitude, incidentType, timestamp).

### 5. ⚙️ C++ / Native Layer (`cpp/`)
- [ ] Implement performance-critical packet framing, serialization (e.g. Protobuf/FlatBuffers), and cryptographic primitives in `src/main/cpp/native-lib.cpp` via JNI.

### 6. 📱 Foreground Service & Background Daemon
- [ ] Implement `MeshService` as an Android Foreground Service with ongoing notification to keep radio listeners and relay nodes active when the app is in the background.

### 7. 🔗 UI Data Binding (ViewModels & StateFlow)
- [ ] Replace screen-level `remember { mutableStateOf(...) }` mock data with `ViewModel`s exposing Kotlin `StateFlow<UIState>` consumed by the composables.

---

## 📂 Project Directory Structure

```
gridzero/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── cpp/
│   │   │   │   ├── CMakeLists.txt         # Native C++ build configuration
│   │   │   │   └── native-lib.cpp         # JNI bridge (scaffold ready)
│   │   │   ├── java/com/example/zerogrid/
│   │   │   │   ├── MainActivity.kt        # Compose Entry Point
│   │   │   │   ├── emergency/             # SOS Beacon & Emergency Center UI
│   │   │   │   ├── files/                 # File sharing & chunk transfer UI
│   │   │   │   ├── home/                  # Mesh Dashboard & Quick Actions UI
│   │   │   │   ├── mesh/                  # Peer discovery, status & telemetry UI
│   │   │   │   ├── messaging/             # 1-to-1 & Broadcast channels UI
│   │   │   │   ├── navigation/            # NavGraph, Route enum, BottomBar
│   │   │   │   ├── onboarding/            # Splash, Intro, Permissions & Identity UI
│   │   │   │   ├── settings/              # Settings & Security UI
│   │   │   │   └── ui/theme/              # Material3 Theme & Color tokens
│   │   │   └── res/                       # App icons, strings, themes
│   ├── build.gradle.kts                   # App module build dependencies
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml                 # Version Catalog
├── build.gradle.kts                       # Root build configuration
├── settings.gradle.kts
└── README.md                              # Project documentation & handover
```

---

## 🚀 Getting Started for Developers

### Prerequisites
1. **Android Studio** (Koala / Ladybug or newer recommended).
2. **Android SDK** API level 35 installed.
3. **Android NDK** `27.0.12077973` and **CMake** `3.22.1` (install via Android Studio SDK Manager -> SDK Tools -> NDK & CMake).
4. **JDK 17** configured as Gradle JDK.

### Building & Running
```bash
# Clone the repository
git clone https://github.com/hehemohit/zerogrid.git

# Navigate to project directory
cd gridzero

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

---

## 👥 Contributors & Maintainers
- **Frontend & UI/UX Architecture:** Completed in Jetpack Compose
- **Backend & Mesh Engine:** *In Development*
