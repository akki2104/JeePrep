# JEE Prep — Offline AI-Powered Exam Preparation App

An Android app for JEE Mains & Advanced preparation, powered by **Gemma 4 E2B** running fully on-device via Google's **LiteRT-LM** SDK. All AI features work **100% offline** after a one-time model download.

## What It Does

- **MCQ Practice** — Topic-wise question sessions from a pre-loaded bank of JEE Previous Year Questions (PYQs). Submit an answer → get an AI-generated step-by-step explanation streamed in real-time.
- **AI-Generated MCQs** — The model generates brand-new JEE-level questions on-the-fly, giving students unlimited practice beyond the PYQ bank.
- **Mock Tests** — Full-length (90 questions / 180 min) or mini (30 questions / 60 min) timed tests with JEE marking scheme (+4 correct, -1 wrong for MCQ). Includes a question palette, mark-for-review, and auto-submit on time expiry.
- **Study Notes** — AI-generated concise revision notes per topic (key concepts, formulas in LaTeX, common mistakes, quick tips). Generated once and cached offline.
- **Memory Tricks** — AI creates mnemonics and memory aids for hard-to-remember concepts. Saveable to favorites.
- **Performance Tracking** — Overall accuracy, subject-wise breakdown, topic-wise heatmap, daily streak, mock test score progression, and AI-powered weak area analysis.
- **Bookmarks** — Save important questions for later review.
- **Previous Year Papers** — Browse PYQs by year (2015–2025), run full timed simulations or go question-by-question with AI explanations.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| AI Engine | [LiteRT-LM](https://ai.google.dev/edge/litert-lm/overview) + Gemma 4 E2B (2.58 GB, on-device) |
| Database | Room (SQLite) |
| DI | Hilt (Dagger) |
| Async | Kotlin Coroutines + Flow |
| Architecture | MVVM + Repository pattern |
| Navigation | Jetpack Navigation Compose |
| Min SDK | 26 (Android 8.0) |
| License | Apache 2.0 (Gemma 4 model) |

## Project Structure

```
app/src/main/java/com/jeeprep/app/
├── JeePrepApp.kt                          # Application class (Hilt entry, DB seeding)
├── MainActivity.kt                        # Single-activity Compose host
│
├── ai/                                    # AI / LLM layer
│   ├── AiEngine.kt                        # LiteRT-LM wrapper (init, GPU/CPU, streaming)
│   ├── ModelDownloadManager.kt            # Download Gemma 4 E2B with resume support
│   └── PromptTemplates.kt                 # JEE-specific prompts (MCQ, notes, tricks, analysis)
│
├── data/                                  # Data layer
│   ├── SeedData.kt                        # Subjects, topics, sample PYQs
│   ├── DatabaseInitializer.kt             # Auto-seeds on first launch
│   ├── db/
│   │   ├── AppDatabase.kt                 # Room DB (9 tables)
│   │   ├── entity/                        # Subject, Topic, Question, UserAttempt, MockTest, etc.
│   │   └── dao/                           # Data access objects with stats queries
│   └── repository/
│       ├── QuestionRepository.kt          # Practice, AI MCQ generation, bookmarks
│       ├── NotesRepository.kt             # AI study notes + memory tricks
│       └── PerformanceRepository.kt       # Accuracy, weak areas, mock test stats
│
├── di/                                    # Dependency injection
│   ├── DatabaseModule.kt                  # Room + DAO providers
│   └── AiModule.kt                        # AiEngine provider
│
└── ui/                                    # Presentation layer
    ├── theme/
    │   ├── Color.kt                       # Subject colors, difficulty, status
    │   └── Theme.kt                       # Material 3 dynamic theming + dark mode
    ├── navigation/
    │   ├── BottomNav.kt                   # Bottom bar (Home, Practice, Notes, Progress)
    │   └── NavGraph.kt                    # All routes and navigation
    └── screens/
        ├── home/                          # Dashboard: stats, quick actions, subject cards
        ├── practice/                      # Subject → Topic → Question session with AI explanations
        ├── mocktest/                      # Timed test, question palette, results
        ├── notes/                         # AI-generated revision notes per topic
        ├── progress/                      # Accuracy charts, subject breakdown, streaks
        └── download/                      # Model download screen with progress bar
```

## Database Schema

| Table | Purpose |
|---|---|
| `subjects` | Physics, Chemistry, Mathematics |
| `topics` | 54 topics across 3 subjects (Kinematics, Organic Chemistry, Integration, etc.) |
| `questions` | PYQs + AI-generated MCQs. Fields: text, 4 options, answer, difficulty, year, exam type |
| `user_attempts` | Every submitted answer with correctness and time taken |
| `study_notes` | Cached AI-generated revision notes per topic |
| `memory_tricks` | Cached mnemonics with favorites |
| `mock_tests` | Test configs (question IDs, time limit) |
| `mock_test_results` | Scores, subject-wise breakdown, time taken |
| `bookmarks` | Saved questions |

## AI Integration

The app uses **Gemma 4 E2B** (Effective 2 Billion parameters) via Google's **LiteRT-LM** SDK for on-device inference.

### How it works:
1. **First launch** → User downloads the model (~2.58 GB) from HuggingFace
2. **Engine init** → `LiteRT-LM Engine` loads with GPU backend (falls back to CPU)
3. **Inference** → Each AI feature sends a structured prompt → model streams response via Kotlin Flow
4. **Caching** → Generated content (explanations, notes, tricks) is cached in Room DB. Subsequent requests serve from cache instantly.

### AI features and their prompts:
- **Explain Answer** → Sends question + options + correct answer → gets step-by-step solution
- **Generate MCQ** → Sends subject + topic + difficulty → gets a new question as structured JSON (constrained decoding)
- **Generate Notes** → Sends topic name → gets formatted revision notes with LaTeX formulas
- **Memory Tricks** → Sends concept name → gets a catchy mnemonic
- **Weakness Analysis** → Sends topic-wise accuracy stats → gets personalized study advice

### Performance expectations (Samsung S24 FE):
- Simple explanation: ~5-10 seconds
- MCQ generation: ~5-10 seconds
- Notes generation: ~10-15 seconds (longer output)
- All responses stream token-by-token for responsive UX

## How to Build & Run

### Prerequisites
- Android Studio Ladybug or later
- JDK 17+
- An Android phone (Min SDK 26) or emulator

### Steps

```bash
# 1. Open the project in Android Studio
#    File → Open → select C:\Users\Akash Yadav\Desktop\JeePrep

# 2. Wait for Gradle sync to complete (downloads dependencies)

# 3. Connect your phone via USB (enable USB debugging in Developer Options)

# 4. Click Run (▶) or:
./gradlew installDebug

# 5. On first launch:
#    - The app auto-seeds the database with subjects, topics, and sample questions
#    - Go to the Model Download screen to download Gemma 4 E2B (~2.58 GB)
#    - After download, AI features become available
```

## Expanding the Question Bank

The app ships with 15 sample PYQs for demo purposes. To add the full ~3000 question bank:

1. Parse publicly available JEE Mains papers (2015–2025) from NTA's website
2. Structure them as `QuestionEntity` objects in `SeedData.kt`, or
3. Create a `questions.json` file in `assets/` and load it on first launch

Each question needs:
```kotlin
QuestionEntity(
    topicId = 2,                    // maps to a topic in SeedData.topics
    questionText = "...",           // supports LaTeX in $..$ and $$..$$
    optionA = "...", optionB = "...", optionC = "...", optionD = "...",
    correctAnswer = "B",            // "A", "B", "C", or "D"
    difficulty = "Medium",          // Easy, Medium, Hard
    year = 2024,                    // PYQ year (null for AI-generated)
    examType = "Mains"              // Mains or Advanced
)
```

## Subjects & Topics (Pre-loaded)

### Physics (17 topics)
Mechanics, Kinematics, Laws of Motion, Work/Energy/Power, Rotational Motion, Gravitation, Properties of Matter, Thermodynamics, Kinetic Theory, Oscillations & Waves, Electrostatics, Current Electricity, Magnetic Effects, EM Induction, Optics, Modern Physics, Semiconductors

### Chemistry (18 topics)
Atomic Structure, Chemical Bonding, Periodic Table, States of Matter, Thermodynamics, Equilibrium, Redox Reactions, Organic Chemistry Basics, Hydrocarbons, Alcohols/Phenols/Ethers, Aldehydes/Ketones/Acids, Coordination Compounds, Electrochemistry, Chemical Kinetics, Surface Chemistry, p-Block Elements, d & f Block Elements, Biomolecules & Polymers

### Mathematics (19 topics)
Sets/Relations/Functions, Complex Numbers, Quadratic Equations, Permutations & Combinations, Binomial Theorem, Sequences & Series, Matrices & Determinants, Limits & Continuity, Differentiation, Integration, Differential Equations, Coordinate Geometry, Straight Lines, Conic Sections, 3D Geometry, Vectors, Probability, Statistics, Trigonometry

## Monetization Plan

- **Free tier**: 20 PYQs/day, 1 AI explanation/day, no mock tests
- **Paid (₹199–499 one-time)**: Unlimited everything via Google Play Billing
- **Cost per user**: ₹0 (model runs on their device, no API calls)

## Key Design Decisions

| Decision | Reasoning |
|---|---|
| Gemma 4 **E2B** (not E4B) | 2.58 GB fits more phones. E4B (3.65 GB) too large for 64/128 GB devices |
| **LiteRT-LM** (not Ollama) | Official Google SDK for Android, uses GPU/NPU, purpose-built for mobile |
| PYQs as seed + AI supplementary | Students trust real past papers. AI adds explanations + generates new Qs |
| **One-time purchase** (not subscription) | Target audience (tier 2/3 students) won't pay monthly |
| English only (MVP) | JEE is primarily in English. Hindi support planned for v2 |
| Model downloaded post-install | Keeps APK small (~30 MB). 2.58 GB download shown clearly upfront |
| **Apache 2.0 license** | Gemma 4 allows commercial use with no restrictions |

## Roadmap

- [ ] Load full ~3000 PYQ bank from JSON asset
- [ ] LaTeX rendering via WebView + KaTeX for math formulas
- [ ] Image support for diagram-based questions
- [ ] Hindi language support
- [ ] Fine-tune E2B on JEE question-answer pairs (LoRA on Google Colab)
- [ ] Google Play Billing integration (free/paid tiers)
- [ ] Analytics & crash reporting
- [ ] Widget for daily practice reminder
- [ ] PYQ topic frequency analysis ("This topic appeared in 8/10 years")
