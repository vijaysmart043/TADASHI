# 🤖 TADASHI – AI Powered Android Voice Assistant

<p align="center">
  <img src="assets/logo.png" alt="TADASHI Logo" width="180"/>
</p>

<p align="center">
  <b>An AI-powered Android voice assistant built with Kotlin, Jetpack Compose, Gemini AI, and Clean Architecture.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-purple"/>
  <img src="https://img.shields.io/badge/Architecture-Clean%20Architecture-blue"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange"/>
  <img src="https://img.shields.io/badge/AI-Gemini-red"/>
</p>

---

# 📖 Overview

TADASHI is an intelligent Android voice assistant designed to perform natural conversations and control Android device functions through voice commands.

The project combines Google's Gemini AI with a modular Tool Framework, allowing the assistant to understand user intent, execute Android system operations, and maintain conversational context.

The long-term vision is to build a production-ready Android AI assistant comparable to Google Assistant while remaining modular, extensible, and privacy-aware.

---

# ✨ Features

## 🧠 AI Features

- Gemini AI Integration
- Context-aware conversations
- Conversation memory
- Streaming AI responses
- Markdown response rendering
- Intelligent intent classification
- Confidence scoring
- Parameter extraction
- Multi-step planning
- Context-aware follow-up commands
- Conflict resolution
- Sequential task execution

---

## 🎤 Voice Features

- Speech Recognition
- Text-to-Speech
- Voice conversation
- Continuous interaction flow

---

## ⚙️ Android Device Control

Current supported tools include:

- 🔦 Flashlight
- 🔊 Volume Control
- ☀️ Brightness Control
- 📱 Open Installed Applications
- 📶 Wi-Fi
- 🟦 Bluetooth
- 🔋 Battery Information
- 📋 Clipboard
- 🔄 Screen Rotation
- 🔔 Ringer Mode
- 📱 Device Information

---

## 📷 Interaction Tools

- Camera
- Gallery
- Notifications
- Media Controls
- Alarm & Timer
- Calendar

---

# 🏗️ Architecture

```
                Voice Input
                     │
                     ▼
          Speech Recognition
                     │
                     ▼
          Intent Classifier
                     │
                     ▼
          AI Tool Planner
                     │
         ┌───────────┴────────────┐
         │                        │
         ▼                        ▼
 Tool Executor              Gemini AI
         │                        │
         └───────────┬────────────┘
                     ▼
             Streaming Engine
                     │
                     ▼
             Text To Speech
```

---

# 📂 Project Structure

```
app
 ├── core
 │    ├── ai
 │    │     ├── conversation
 │    │     ├── gemini
 │    │     ├── planner
 │    │     └── streaming
 │    │
 │    ├── tools
 │    │     ├── flashlight
 │    │     ├── brightness
 │    │     ├── volume
 │    │     ├── wifi
 │    │     ├── bluetooth
 │    │     ├── battery
 │    │     ├── clipboard
 │    │     ├── camera
 │    │     ├── gallery
 │    │     ├── notifications
 │    │     ├── media
 │    │     ├── alarmtimer
 │    │     ├── calendar
 │    │     └── deviceinfo
 │    │
 │    ├── voice
 │    ├── logger
 │    ├── utils
 │    └── repository
 │
 ├── presentation
 │
 ├── ui
 │
 └── di
```

---

# 🛠️ Tech Stack

## Language

- Kotlin

## UI

- Jetpack Compose
- Material Design 3

## AI

- Google Gemini API

## Architecture

- MVVM
- Clean Architecture
- Repository Pattern

## Dependency Injection

- Hilt

## Android APIs

- SpeechRecognizer
- TextToSpeech
- Camera APIs
- AudioManager
- ClipboardManager
- BluetoothManager
- WifiManager
- NotificationManager
- AlarmManager

---

# 🧩 AI Planning Pipeline

```
Speech
    │
    ▼
Intent Classifier
    │
    ▼
Planner
    │
    ▼
Parameter Extractor
    │
    ▼
Tool Request
    │
    ▼
Tool Executor
    │
    ▼
Android Tool
    │
    ▼
Tool Result
    │
    ▼
Streaming
    │
    ▼
Text To Speech
```

---

# 🚀 Current Progress

| Module | Status |
|---------|--------|
| Voice Recognition | ✅ |
| Gemini Integration | ✅ |
| Conversation Memory | ✅ |
| Streaming Responses | ✅ |
| AI Planner | ✅ |
| Intent Classification | ✅ |
| Multi-Step Planning | ✅ |
| Parameter Extraction | ✅ |
| Tool Framework | ✅ |
| Flashlight | ✅ |
| Brightness | 🚧 |
| Volume | 🚧 |
| Wi-Fi | 🚧 |
| Bluetooth | 🚧 |
| Clipboard | 🚧 |
| Device Information | 🚧 |
| Notifications | 🚧 |
| Camera | 🚧 |
| Gallery | 🚧 |
| Alarm & Timer | 🚧 |
| Calendar | 🚧 |

> 🚧 = Implemented and under active testing/refinement.

---

# 📅 Roadmap

### Phase 1
- [x] Voice Recognition
- [x] Text-to-Speech
- [x] Gemini AI Integration
- [x] Conversation Memory

### Phase 2
- [x] Streaming Responses
- [x] Planner
- [x] Intent Classification
- [x] Multi-Step Planning

### Phase 3
- [x] Tool Framework
- [x] Android System Tools
- [x] Device Controls
- [x] Branding

### Upcoming

- Universal App Launcher
- Contacts
- Phone Calls
- SMS
- File Manager
- Wake Word ("Hey TADASHI")
- Vision AI
- Offline AI Support
- Automation Engine
- Smart Routines

---

# 📸 Screenshots

```
Coming Soon...
```

---

# ⚡ Getting Started

### Clone

```bash
git clone https://github.com/your-username/TADASHI.git
```

### Open

Android Studio Hedgehog or newer.

### Configure

Add your Gemini API Key in the application Settings screen.

### Run

```bash
./gradlew assembleDebug
```

---

# 🤝 Contributing

Contributions, ideas, feature requests, and bug reports are welcome.

Feel free to fork the repository and submit a Pull Request.

---

# 📄 License

This project is licensed under the MIT License.

---

# 👨‍💻 Author

**Vijay Raj**

AI & Android Developer

Building intelligent Android experiences using AI, Kotlin, and Clean Architecture.

---

⭐ If you like this project, don't forget to Star the repository!