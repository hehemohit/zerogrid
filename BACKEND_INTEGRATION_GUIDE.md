# Briar & Bramble Backend Integration & Custom Mesh Transport Implementation Guide

This document provides a comprehensive technical blueprint for extracting, embedding, and integrating the **Briar** and **Bramble** P2P backend framework into a custom Android or JVM UI application, complete with step-by-step implementation instructions and exact code patterns used in this codebase.

---

## 1. System Architecture Overview

Briar's architecture is modularized into distinct protocol, backend, and presentation layers:

```
+-------------------------------------------------------------------+
|                    Custom UI Application                          |
|         (Jetpack Compose / Compose Multiplatform / Custom)         |
+-------------------------------------------------------------------+
                                  |
            [RxJava / Kotlin Flow / EventBus Bridge]
                                  |
+---------------------------------+---------------------------------+
|                         briar-api                                 |
|  - AccountManager       - ConversationManager    - GroupManager   |
|  - ContactManager       - ForumManager           - BlogManager    |
|  - AvatarManager        - AutoDeleteManager      - FeedManager    |
+---------------------------------+---------------------------------+
                                  |
+---------------------------------+---------------------------------+
|                         briar-core                                |
|  - Database / H2 Engine - Messaging Protocol     - Group Sync     |
+---------------------------------+---------------------------------+
                                  |
+---------------------------------+---------------------------------+
|                     bramble-api & bramble-core                    |
|  - Transport Cryptography (Ed25519, Curve25519, SecretBox)         |
|  - Encrypted P2P Sync Engine                                      |
|  - Transports: Tor (Onion Service), Bluetooth RFCOMM, LAN / Wi-Fi |
+-------------------------------------------------------------------+
```

### Module Breakdown
* **`bramble-api`**: Low-level P2P protocol interfaces, crypto primitives, contact model, transport definitions, and event bus contracts.
* **`bramble-core`**: P2P network implementation, Tor integration, Bluetooth/LAN connections, database transaction manager, and sync engine.
* **`bramble-android` / `bramble-java`**: OS-level bindings (Android services, network state monitoring, Tor binary management).
* **`briar-api`**: High-level application service contracts (Private Messages, Groups, Forums, Blogs, RSS Feeds, Introductions, Avatars).
* **`briar-core`**: Implementations of messaging logic, client group managers, database schema migrations, and sync clients.
* **`briar-headless`** *(Optional)*: Standalone Kotlin daemon providing a HTTP/REST API wrapper around the backend.

---

## 2. P2P Mesh Architecture, Routing & Transports

### A. Multi-hop Packet Routing & Peer Propagation
* **Store-and-Forward / Delay-Tolerant Networking (DTN)**:
  Briar implements **Store-and-Forward multi-hop message propagation** rather than real-time ad-hoc IP packet routing.
  * **How it works**: When User A creates a message in a shared Group or Forum, User A synchronizes with User B over an available transport (Bluetooth, LAN, or Tor). User B stores the encrypted message in their local H2 database. When User B later comes near User C, User B automatically forwards the message to User C.
  * **Privacy Advantage**: Unlike real-time ad-hoc mesh routers (e.g. B.A.T.M.A.N. or Yggdrasil) that relay raw unauthenticated IP traffic for strangers, Briar only synchronizes encrypted data with authenticated contacts or shared group subscribers.

### B. Transports & Peer Discovery
| Transport | Technology Used | Discovery Method | Status in Briar |
| :--- | :--- | :--- | :--- |
| **Bluetooth** | Bluetooth Classic (RFCOMM) | SDP / Device Pairing & Scanning | **Built-in & Fully Supported** |
| **Local Wi-Fi / LAN** | TCP Sockets | mDNS (Multicast DNS) Local Discovery | **Built-in & Fully Supported** |
| **Internet** | Tor Onion Services (v3) | Tor Rendezvous / Handshake Links | **Built-in & Fully Supported** |
| **Sneakernet** | USB / Removable Drives | File System Import / Export | **Built-in & Fully Supported** |
| **BLE (Bluetooth Low Energy)** | GATT Services | BLE Advertising / Scanning | *Extensible via `DuplexPlugin`* |
| **Wi-Fi Direct (P2P)** | Wi-Fi P2P / Wi-Fi Aware | Wi-Fi Direct Group Owner / Client | *Extensible via `DuplexPlugin`* |

---

## 3. How Transport Plugins Work in Bramble (With Project Code)

All transport engines in Bramble (Bluetooth, LAN, Tor, or custom BLE/Wi-Fi Direct) implement the `DuplexPlugin` interface.

### A. Transport Plugin Pipeline

When a transport plugin accepts or creates a connection:
```
Custom Transport (Socket / Stream)
          │
          ▼
DuplexTransportConnection (Reader / Writer)
          │
          ▼
PluginCallback.handleConnection(duplexTransportConnection)
          │
          ▼
ConnectionManagerImpl.manageIncomingConnection()
          │
          ▼
[ Bramble Transport Layer Encryption & Ed25519 Authentication ]
          │
          ▼
SyncEngine (Database Store-and-Forward Multi-hop Propagation)
```

### B. How `PluginConfig` Registers Plugins (`AppModule.java`)

In `briar-android/src/main/java/org/briarproject/briar/android/AppModule.java`:

```java
@Provides
@Singleton
PluginConfig providePluginConfig(
        AndroidBluetoothPluginFactory bluetooth,
        AndroidTorPluginFactory tor,
        AndroidLanTcpPluginFactory lan,
        AndroidRemovableDrivePluginFactory drive,
        MailboxPluginFactory mailbox) {

    return new PluginConfig() {

        @Override
        public Collection<DuplexPluginFactory> getDuplexFactories() {
            // Add custom plugin factories here (e.g., customBleFactory, customWifiDirectFactory)
            return asList(bluetooth, tor, lan);
        }

        @Override
        public Collection<SimplexPluginFactory> getSimplexFactories() {
            return asList(mailbox, drive);
        }

        @Override
        public boolean shouldPoll() {
            return true;
        }

        @Override
        public Map<TransportId, List<TransportId>> getTransportPreferences() {
            // Prefer LAN over Bluetooth for higher bandwidth when available
            return singletonMap(BluetoothConstants.ID, singletonList(LanTcpConstants.ID));
        }
    };
}
```

---

## 4. Step-by-Step Implementation Guide: Adding a Custom Mesh Transport (e.g., BLE or Wi-Fi Direct)

To add a custom transport engine (such as BLE or Wi-Fi Direct) to Bramble, follow these 4 steps:

### Step 1: Define Transport ID Constants

```java
package org.briarproject.bramble.api.plugin;

public class BleConstants {
    // Unique String ID for the custom transport
    public static final TransportId ID = new TransportId("org.briarproject.bramble.plugin.ble");
}
```

---

### Step 2: Implement `DuplexTransportConnection`

Wrap your underlying BLE/Wi-Fi Direct socket or streams into a `DuplexTransportConnection`:

```java
import org.briarproject.bramble.api.plugin.TransportConnectionReader;
import org.briarproject.bramble.api.plugin.TransportConnectionWriter;
import org.briarproject.bramble.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.bramble.api.properties.TransportProperties;

import java.io.InputStream;
import java.io.OutputStream;

public class CustomBleTransportConnection implements DuplexTransportConnection {

    private final TransportConnectionReader reader;
    private final TransportConnectionWriter writer;
    private final TransportProperties remoteProperties;

    public CustomBleTransportConnection(InputStream in, OutputStream out, TransportProperties remoteProperties) {
        this.reader = () -> in;
        this.writer = () -> out;
        this.remoteProperties = remoteProperties;
    }

    @Override
    public TransportConnectionReader getReader() {
        return reader;
    }

    @Override
    public TransportConnectionWriter getWriter() {
        return writer;
    }

    @Override
    public TransportProperties getRemoteProperties() {
        return remoteProperties;
    }
}
```

---

### Step 3: Implement `DuplexPlugin`

Implement `DuplexPlugin` to handle lifecycle (`start()`, `stop()`), incoming listener setup, and outgoing connections:

```java
import org.briarproject.bramble.api.plugin.ConnectionHandler;
import org.briarproject.bramble.api.plugin.PluginCallback;
import org.briarproject.bramble.api.plugin.PluginException;
import org.briarproject.bramble.api.plugin.TransportId;
import org.briarproject.bramble.api.plugin.duplex.DuplexPlugin;
import org.briarproject.bramble.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.bramble.api.properties.TransportProperties;

public class CustomBlePlugin implements DuplexPlugin {

    private final PluginCallback callback;
    private volatile State state = State.STARTING_STOPPING;

    public CustomBlePlugin(PluginCallback callback) {
        this.callback = callback;
    }

    @Override
    public TransportId getId() {
        return BleConstants.ID;
    }

    @Override
    public long getMaxLatency() {
        return 30_000; // 30 seconds max latency
    }

    @Override
    public int getMaxIdleTime() {
        return 60_000; // 60 seconds max idle time
    }

    @Override
    public void start() throws PluginException {
        // Initialize BLE GATT Server / Advertising / Scanning here
        state = State.ACTIVE;
        callback.pluginStateChanged(State.ACTIVE);
    }

    @Override
    public void stop() throws PluginException {
        // Stop GATT Server and active connections
        state = State.DISABLED;
        callback.pluginStateChanged(State.DISABLED);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public int getReasonsDisabled() {
        return 0;
    }

    @Override
    public boolean shouldPoll() {
        return true;
    }

    @Override
    public int getPollingInterval() {
        return 60_000; // Poll every 60 seconds
    }

    @Override
    public void poll(Collection<Pair<TransportProperties, ConnectionHandler>> properties) {
        // Attempt connecting to nearby discovered peers
    }

    @Override
    public DuplexTransportConnection createConnection(TransportProperties p) {
        // Connect to remote BLE address in transport properties
        // InputStream in = ...; OutputStream out = ...;
        // return new CustomBleTransportConnection(in, out, p);
        return null;
    }

    // When an incoming BLE connection arrives:
    private void onIncomingBleConnection(InputStream in, OutputStream out, TransportProperties remoteProps) {
        DuplexTransportConnection connection = new CustomBleTransportConnection(in, out, remoteProps);
        // Hand off connection to Bramble's ConnectionManager for encryption & sync:
        callback.handleConnection(connection);
    }

    @Override
    public boolean supportsKeyAgreement() { return false; }
    @Override
    public KeyAgreementListener createKeyAgreementListener(byte[] localCommitment) { return null; }
    @Override
    public DuplexTransportConnection createKeyAgreementConnection(byte[] remoteCommitment, BdfList descriptor) { return null; }
    @Override
    public boolean supportsRendezvous() { return false; }
    @Override
    public RendezvousEndpoint createRendezvousEndpoint(KeyMaterialSource k, boolean alice, ConnectionHandler incoming) { return null; }
}
```

---

### Step 4: Implement `DuplexPluginFactory` and Register in `PluginConfig`

```java
import org.briarproject.bramble.api.plugin.PluginCallback;
import org.briarproject.bramble.api.plugin.TransportId;
import org.briarproject.bramble.api.plugin.duplex.DuplexPlugin;
import org.briarproject.bramble.api.plugin.duplex.DuplexPluginFactory;

public class CustomBlePluginFactory implements DuplexPluginFactory {

    @Override
    public TransportId getId() {
        return BleConstants.ID;
    }

    @Override
    public DuplexPlugin createPlugin(PluginCallback callback) {
        return new CustomBlePlugin(callback);
    }
}
```

Register `CustomBlePluginFactory` in your `PluginConfig`:
```java
@Override
public Collection<DuplexPluginFactory> getDuplexFactories() {
    return asList(bluetooth, tor, lan, customBlePluginFactory);
}
```

---

## 5. Integrating Core Services into a Custom UI App

### Step 1: Initialize Database and Account Lifecycle

```java
@Inject AccountManager accountManager;
@Inject LifecycleManager lifecycleManager;

// 1. Check if account exists
if (!accountManager.hasAccount()) {
    accountManager.createAccount("Alice", "UserPassword123!");
}

// 2. Start services & decrypt H2 database
lifecycleManager.startServices("UserPassword123!");
```

---

### Step 2: Observe Real-Time Events (`EventBus`)

```java
@Inject EventBus eventBus;

// Subscribe to incoming private messages
eventBus.addListener(PrivateMessageReceivedEvent.class, event -> {
    ContactId contactId = event.getContactId();
    MessageId messageId = event.getMessageHeader().getId();
    // Dispatch to StateFlow / UI ViewModel
});

// Subscribe to contact connection status changes (Tor, Bluetooth, LAN)
eventBus.addListener(ContactConnectedEvent.class, event -> {
    ContactId contactId = event.getContactId();
    TransportId transportId = event.getTransportId();
    // Update contact status indicator (Online via Bluetooth/LAN/Tor)
});
```

---

### Step 3: Send & Receive Messages (`ConversationManager`)

```java
@Inject ConversationManager conversationManager;

// Send private message to contact
ContactId contactId = ...;
String text = "Hello over P2P mesh!";
long now = System.currentTimeMillis();

PrivateMessageHeader header = conversationManager.sendPrivateMessage(contactId, text, now);

// Retrieve conversation history
Collection<ConversationMessageHeader> messages = conversationManager.getMessageHeaders(contactId);
```

---

### Step 4: Contact Exchange & Handshakes (`HandshakeManager`)

```java
@Inject HandshakeManager handshakeManager;

// Share your local handshake link (via QR code or link)
String myHandshakeLink = handshakeManager.getHandshakeLink();

// Add pending contact from scanned QR / link
PendingContact pending = handshakeManager.addPendingContact(scannedLink, "Bob");
```

---

## 6. Android Service Setup (`BriarService.kt`)

On Android, keep the backend alive in a foreground service (`BriarService.kt`) with an ongoing notification to maintain Bluetooth, LAN, and Tor connections while the UI app is in the background.

```kotlin
class CustomP2PService : Service() {

    @Inject lateinit var lifecycleManager: LifecycleManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        lifecycleManager.stopServices()
        super.onDestroy()
    }
}
```

---

## 7. Summary & Implementation Checklist for AI Agent

1. [ ] **Include Modules**: Include `:bramble-api`, `:bramble-core`, `:bramble-android`, `:briar-api`, `:briar-core` in `settings.gradle`.
2. [ ] **Register Transports**: Configure `PluginConfig` with `Bluetooth`, `LAN`, `Tor` (and custom `DuplexPluginFactory` for BLE/Wi-Fi Direct if required).
3. [ ] **Account UI Flow**: Create screens for Account Creation & Database Password Unlock using `AccountManager`.
4. [ ] **EventBus Bridge**: Convert `EventBus.addListener()` events to Kotlin `StateFlow` streams for reactive Jetpack Compose / Flutter UI.
5. [ ] **Messaging & Contacts UI**: Wire UI screens to `ConversationManager`, `ContactManager`, and `PrivateGroupManager`.
6. [ ] **Foreground Service**: Ensure `BriarService` is registered in `AndroidManifest.xml` to keep P2P sync active in the background.
