# 🤖 AI Learning Companion (Offline & Multilingual)

**Project:** Bharat AI SkillPath ARM  
**Team Leader:** M. Shreyas  
**Team Members:** Batool Ather, Rafooz Shaik  
**Faculty Mentor:** P. Sathish  
**Date:** November 2026  

## 📖 About
The AI Learning Companion is a 100% offline, edge-computing mobile application designed to provide personalized, adaptive learning pathways for technical career tracks (Data Analyst, AI Engineer, and VLSI Engineer). By leveraging Meta’s Llama 3.2 (1B parameters) running locally via the ExecuTorch framework, the app delivers intelligent, multilingual tutoring and resource recommendations without requiring an internet connection.

## 🌟 Key Features
- **100% Offline AI:** Runs Llama 3.2 1B locally using ExecuTorch (INT4 quantized).
- **3 Career Paths:** Data Analyst, AI Engineer, and VLSI Engineer with adaptive prerequisite tracking.
- **Adaptive Learning Engine:** Uses Kahn’s Algorithm (Topological Sorting) for skill ordering and Exponential Moving Average (EMA) for mastery tracking. Dynamically reschedules study plans based on quiz performance.
- **25+ Languages:** Full UI translation and AI multilingual generation.
- **Voice Input:** Built-in Speech-to-Text for hands-free onboarding and chatting.
- **Verified Resources:** Only recommends pre-validated, working learning links (verified via Python pipeline).

## 🛠️ Technology Stack
- **Frontend & UI:** Kotlin, Jetpack Compose
- **AI Inference Engine:** Meta’s ExecuTorch (Llama 3.2 1B Instruct, INT4)
- **Local Database:** Android Room (SQLite) with pre-populated skill graphs
- **Data Pipeline:** Python (SQLite, CSV/JSON parsing, HTTP link verification)

## 📂 Project Structure
- `app/`: Android application source code (Kotlin/Jetpack Compose).
- `reference_python/`: Python scripts for building the database, verifying links, and reference logic (`build_db.py`, `check_links.py`, `logic.py`).
- `data/`: Raw CSV and JSON data files for skills, resources, and questions.
- `docs/`: Project documentation and team contract.
- `tests/`: Test cases for the adaptive logic.

## 🚀 How to Run the App

### Prerequisites
- Android Studio (Latest Stable)
- An ARM64 Android Device (Physical device recommended for ExecuTorch performance)
- ADB (Android Debug Bridge)

### 1. Clone and Open
```bash
git clone https://github.com/mshreyas3213-design/skillpath-arm.git
cd skillpath-arm
