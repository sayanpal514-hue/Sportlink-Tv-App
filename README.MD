# 📺 Sportlink TV App

A free, open-source application for streaming live sports — built for educational purposes.

🌐 **Live Site:** [https://sayanpal514-hue.github.io/Sportlink-Tv-App/](https://sayanpal514-hue.github.io/Sportlink-Tv-App/)

---

## ✨ Features

- 📡 Live sports streaming via open-source JSON APIs
- 📱 Designed and optimized for Android TV
- 🏆 Multiple sport categories (Football, Cricket, Basketball, and more)
- 🔗 Clean, minimal UI for TV navigation
- ⚡ Fast channel loading with no login required
- 🆓 Completely free — no subscriptions, no ads

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| Build System | Gradle (Kotlin DSL) |
| Platform | Android TV (Leanback) |
| Min SDK | Android 5.0+ (API 21) |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest stable)
- Android SDK with TV emulator or a physical Android TV device
- Java 11 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/sayanpal514-hue/Sportlink-Tv-App.git
   cd Sportlink-Tv-App
   ```

2. **Open in Android Studio**
   - Go to `File → Open` and select the project folder.

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device / emulator**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📂 Project Structure

```
Sportlink-Tv-App/
├── app/                  # Main application module
│   └── src/
│       ├── main/
│       │   ├── java/     # Kotlin source files
│       │   ├── res/      # Layouts, drawables, strings
│       │   └── AndroidManifest.xml
├── gradle/wrapper/       # Gradle wrapper files
├── build.gradle.kts      # Root build config
├── settings.gradle.kts   # Project settings
└── README.md
```

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'Add my feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](./LICENSE) file for details.

---

## ⚠️ Disclaimer

> **This application is developed strictly for educational purposes.**
>
> - The developer (**sayanpal514-hue**) does **not** host, own, or control any API, JSON feed, stream, or media content used by this application.
> - All streams, links, and JSON data sources are **publicly available on the open internet** and are not hosted or distributed by this project.
> - This app does **not** store, cache, or redistribute any copyrighted content.
> - The developer is **not responsible** for the content provided by third-party sources accessed through this application.
> - If you are a content owner and believe your rights are being infringed, please raise an issue or contact the developer directly for immediate removal.
> - Use this application **at your own risk** and in compliance with the laws of your country.

---

## 👨‍💻 Author

**sayanpal514-hue**
- GitHub: [@sayanpal514-hue](https://github.com/sayanpal514-hue)
- 🌐 Site: [sayanpal514-hue.github.io/Sportlink-Tv-App](https://sayanpal514-hue.github.io/Sportlink-Tv-App/)

---

<p align="center">Made with ❤️ for educational purposes only</p>
