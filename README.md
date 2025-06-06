# Remote Smart Home Control Application

An Android application that allows users to control their smart home remotely through a real-time interface .

## Main Features

- 🔐 Login to personal accounts using **Firebase Authentication**
- 🧪 Test login available with a demo account
- 📱 Application includes 5 main pages:

### 1. Home Page
- 🌤️ Display current outdoor weather and city using **OpenWeatherMap**
- 🔍 Search and display weather in other cities

### 2. Room Page
- 🌡️ Monitor room temperature
- 🔌 Monitor indoor equipment: doors, air conditioners, fans, lights
- 📷 Some rooms support live monitoring via **ESP32-CAM**

### 3. Personal Page
- 🧾 Display home status and user (owner) information

### 4. Function Page
- 🎙️ Control devices using **Google Voice Recognition**
- 📹 Monitor outdoor areas: gates, garden lights, outdoor lights
- ➕➖ Add or remove devices to manage home configuration

### 5. Support Page
- ⏱️ Set timer for room devices
- 🌐 Switch app language: English / Vietnamese
- 🚪 Logout

> 🧠 The application communicates with IoT hardware using **WebSocket** protocol.

---

## 🛠 Technologies Used

- **Java** 
- **Firebase Authentication** 
- **WebSocket** – Real-time communication with IoT devices
- **OpenWeatherMap API** – Real-time weather display
- **Google Voice Recognition** – Voice control functionality
- **ESP32-CAM** – Camera integration for monitoring
- **Android Studio** – Development environment

---

## Author

- **Hoang Bao Thinh**
