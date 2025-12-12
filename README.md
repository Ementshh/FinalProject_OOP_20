# LabuBoom - Final Project OOP

A 2D platformer shooter game with backend integration for save/load functionality.

Built with [libGDX](https://libgdx.com/) and Spring Boot.

## 🚀 Quick Start

### 1. Start Backend (MUST BE FIRST!)
```powershell
cd Backend
./gradlew bootRun
```
Wait for: `Started BackendApplication in X.XXX seconds`

### 2. Test Backend (Recommended)
```powershell
powershell -ExecutionPolicy Bypass -File .\test-backend.ps1
```
Should show: `SUCCESS - Backend is RUNNING!`

### 3. Start Frontend
```powershell
cd Frontend
./gradlew lwjgl3:run
```

## 📖 Full Documentation

- **[STARTUP_GUIDE.md](STARTUP_GUIDE.md)** - Complete startup instructions
- **[IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)** - Technical implementation details

## ✅ Features

- ✅ Username-based login system
- ✅ Save/Load game progress
- ✅ Auto-save on level completion
- ✅ Auto-save on quit
- ✅ Continue or New Game for returning players
- ✅ 5 Levels with boss battles
- ✅ Multiple weapons
- ✅ Coin collection system

## 🎮 Controls

- **W** - Jump
- **A/D** - Move
- **Mouse** - Aim
- **Left Click** - Shoot
- **1/2/3** - Switch weapons
- **ESC** - Pause

## 🛠️ Tech Stack

**Backend:**
- Spring Boot
- PostgreSQL (Neon Cloud)
- JPA/Hibernate

**Frontend:**
- LibGDX
- Java

## ⚠️ Important

Always start **Backend FIRST**, then Frontend!

Use `test-backend.ps1` to verify backend is ready before starting frontend.

## 📝 API Endpoints

- `POST /api/players/login` - Login/create player
- `PUT /api/players/{id}/progress` - Save progress
- `PUT /api/players/{id}/reset` - Reset to stage 1
- `GET /api/players` - Get all players

## 🐛 Troubleshooting

See [STARTUP_GUIDE.md](STARTUP_GUIDE.md) for detailed troubleshooting steps.

---

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
