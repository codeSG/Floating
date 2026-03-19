# 📱 Floating Screen Translator (Android)

A real-time floating overlay application that reads on-screen text from other apps (like Instagram/WhatsApp) and displays it in a draggable floating window. Designed as a base for building **inline translation systems**.

---

## 🚀 Features

* 🟢 Floating draggable overlay (chat-head style)
* 🟢 Reads visible UI text using Accessibility Service
* 🟢 Displays multiple messages in a scrollable view
* 🟢 Real-time updates from screen content
* 🟢 Expandable architecture for translation (Telugu → Hindi/English)

---

## 🧠 Architecture

```
Other Apps (Instagram / WhatsApp)
        ↓
Accessibility Service
        ↓
Extract UI Text
        ↓
Shared Data Holder
        ↓
Floating Overlay Service
        ↓
Display in UI
```

---

## 📂 Project Structure

```
com.example.floating
│
├── MainActivity.kt
├── FloatingViewService.kt        // Overlay UI
├── ChatAccessibilityService.kt   // Reads screen text
├── FloatingDataHolder.kt         // Shared data
│
├── res/
│   ├── layout/
│   │   └── overlay_layout.xml
│   ├── xml/
│   │   └── accessibility_service_config.xml
│   └── drawable/
│       └── bg_overlay.xml
```

---

## ⚙️ Setup Instructions

### 1. Clone Project

```bash
git clone <your-repo-url>
```

---

### 2. Open in Android Studio

* Recommended: **Android Studio Hedgehog or later**
* Use compatible **AGP (8.2.1)**

---

### 3. Permissions Required

Add in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```

---

### 4. Enable Overlay Permission

Go to:

```
Settings → Apps → Your App → Display over other apps → Enable
```

---

### 5. Enable Accessibility Service

```
Settings → Accessibility → Floating → Enable
```

⚠️ Required for reading screen content

---

## 🧩 Key Components

### 🔹 Accessibility Service

* Captures UI elements from other apps
* Extracts visible text recursively

```kotlin
rootInActiveWindow → traverse → node.text
```

---

### 🔹 Floating Overlay Service

* Uses `WindowManager`
* Displays draggable UI
* Updates text dynamically

---

### 🔹 Shared Data Holder

Simple singleton for communication:

```kotlin
object FloatingDataHolder {
    val messages = mutableListOf<String>()
}
```

---

## 📱 UI Overview

* Floating card with:

  * Title bar
  * Close button
  * Scrollable message area

---

## 📸 Screenshots

> Add your screenshots here

### Example:

```
![Overlay UI](screenshots/overlay.png)
![Accessibility Enabled](screenshots/accessibility.png)
```

---

## ⚠️ Known Limitations

* Not all apps expose UI text (privacy restrictions)
* Accessibility events may trigger duplicates
* Requires manual enabling by user
* Play Store requires justification for accessibility usage

---

## 🔮 Future Improvements

* 🔄 Real-time translation (Google ML Kit / API)
* 🎯 Filter only chat messages (Instagram/WhatsApp)
* 🧠 Sender/receiver detection
* 🎨 Better UI (bubble, minimize, expand)
* ⚡ Replace polling with event-driven updates

---

## 🛠 Tech Stack

* Kotlin
* Android SDK
* AccessibilityService
* WindowManager (Overlay)
* XML UI

---

## 💡 Use Cases

* Inline chat translation
* Accessibility tools
* Screen reading assistants
* Productivity overlays

---

## 🤝 Contributing

Feel free to fork and improve:

* UI enhancements
* Better text filtering
* Translation integrations

---

## 📄 License

MIT License

---

## 🙌 Author

Built by **Sourabh Garg**

---

## ⭐ If you like this project

Give it a star ⭐ and share!

---
