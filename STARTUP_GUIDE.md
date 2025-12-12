# 🚀 LabuBoom Game - Startup Guide

## ⚠️ Important: Start Order

**ALWAYS start Backend BEFORE Frontend!**

---

## 📋 Step-by-Step Instructions

### **Step 1: Start Backend Server**

Open a PowerShell terminal and run:

```powershell
cd Backend
./gradlew bootRun
```

**Wait for this message:**
```
Started BackendApplication in X.XXX seconds (JVM running for X.XXX)
```

**You should also see:**
```
Mapped "{[/api/players/login],methods=[POST]}"
Mapped "{[/api/players/{playerId}/progress],methods=[PUT]}"
Mapped "{[/api/players/{playerId}/reset],methods=[PUT]}"
```

✅ Backend is now ready!

---

### **Step 2: Verify Backend (Optional but Recommended)**

In a **NEW** PowerShell terminal:

```powershell
./test-backend.ps1
```

You should see:
```
✓ Backend is RUNNING!
✓ Database connection: OK
✓ Player API: OK
```

If you get an error, go back to Step 1 and wait longer.

---

### **Step 3: Start Frontend**

In a **NEW** PowerShell terminal:

```powershell
cd Frontend
./gradlew lwjgl3:run
```

**Wait for the game window to appear**

---

### **Step 4: Play the Game!**

1. **Enter Username** - Type your username and click "START GAME"
2. **First Time Player** → Game starts at Level 1
3. **Returning Player** → Choose "Continue" or "New Game"

---

## 🎮 Game Controls

- **W** - Jump
- **A/D** - Move left/right
- **Mouse** - Aim
- **Left Click** - Shoot
- **1/2/3** - Switch weapons
- **ESC** - Pause menu

---

## 🐛 Troubleshooting

### Error: "Failed to connect to server"

**Cause:** Backend is not running or not ready

**Solution:**
1. Check if backend terminal shows "Started BackendApplication"
2. Run `./test-backend.ps1` to verify
3. If failed, restart backend and wait longer

---

### Error: "Error parsing JSON"

**Cause:** Frontend is hitting wrong endpoint or backend returned HTML error page

**Solution:**
1. Stop both backend and frontend
2. Clean build backend: `cd Backend; ./gradlew clean build`
3. Restart backend first, then frontend

---

### Error: Port 8080 already in use

**Cause:** Another application is using port 8080

**Solution:**
```powershell
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual number)
taskkill /PID <PID> /F
```

Or change port in `Backend/src/main/resources/application.properties`:
```properties
server.port=8081
```

And update `Frontend/core/src/main/java/com/labubushooter/frontend/services/PlayerApiService.java`:
```java
private static final String BASE_URL = "http://localhost:8081/api/players";
```

---

## 📊 Database

- **Type:** PostgreSQL (Neon Cloud Database)
- **Auto-create tables:** Yes, automatic on first run
- **Table:** `players` (created automatically)
- **No manual setup needed!**

---

## 🔄 Quick Restart

If you need to restart everything:

**Terminal 1 (Backend):**
```powershell
Ctrl+C  # Stop backend
./gradlew bootRun  # Restart
```

**Terminal 2 (Frontend):**
```powershell
Ctrl+C  # Stop frontend
./gradlew lwjgl3:run  # Restart
```

---

## 📝 API Endpoints (for testing)

### Login/Create Player
```powershell
curl -X POST http://localhost:8080/api/players/login `
  -H "Content-Type: application/json" `
  -d '{"username":"testuser"}'
```

### Save Progress
```powershell
curl -X PUT http://localhost:8080/api/players/{UUID}/progress `
  -H "Content-Type: application/json" `
  -d '{"lastStage":3,"coinsCollected":10}'
```

### Reset Progress
```powershell
curl -X PUT http://localhost:8080/api/players/{UUID}/reset
```

### Get All Players
```powershell
curl http://localhost:8080/api/players
```

---

## ✅ Checklist Before Playing

- [ ] Backend started and shows "Started BackendApplication"
- [ ] `./test-backend.ps1` returns success
- [ ] Frontend started successfully
- [ ] Game window appears

---

## 🎯 Game Features

✅ Username login system  
✅ Save/load game progress  
✅ Auto-save on level completion  
✅ Auto-save on quit  
✅ Continue or New Game for returning players  
✅ Lifetime coin tracking  
✅ Stage progress tracking  

---

## 📞 Need Help?

Check the logs:
- **Backend:** Terminal where you ran `./gradlew bootRun`
- **Frontend:** Terminal where you ran `./gradlew lwjgl3:run`

Look for ERROR or EXCEPTION messages.

---

**Happy Gaming! 🎮**
