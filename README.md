# Build a Golem ⚒

A rich alchemical workshop and action-defense combat mobile game for Android, written in Kotlin with high-performance hardware-accelerated 2D Canvas rendering and zero-dependency procedural audio synthesis.

---

## 🌟 Game Highlights

### 🛠 The Workshop (Anatomical Golem Builder)
- **Modular 5-Slot Golem Anatomy**:
  - **Core (The Heart)**: Defines life pulse, energy recharge, and active elemental magic.
  - **Head (The Visage)**: Determines targeting, crit rates, horns, cyclops lenses, and scanner beams.
  - **Torso**: Provides health pools, armor plating, moss regeneration, and magma crusts.
  - **Arms & Fists**: Defines attack power, punch reach, warhammers, and flaming claws.
  - **Legs & Base**: Determines sprint speed, hydraulic dashing, earth tremors, or levitation runes.
- **7 Elements & 3+ Set Resonances**:
  - `Earth Harmony`: +25% Max HP & passive health regeneration.
  - `Granite Bastion`: +35% Armor & knockback resistance.
  - `Steel Vanguard`: +25% Strike damage.
  - `Infernal Fury`: +30% Attack power & explosive flame retaliation.
  - `Glacial Shroud`: +20% Armor & chilling attacks that slow foes.
  - `Storm Catalyst`: +30% Movement speed & electric shock arcs.
  - `Cosmic Resonance`: +15% HP, +20% Attack, and accelerated ability recharge.
- **Interactive 2D Procedural Renderer**:
  - Real-time animated vector rendering with idle breathing, pulsing runic heart, swinging arms, eye glow, and ground shadows.
  - Interactive touch points: tap any anatomical region of the golem to jump to that part slot.
  - Motion test suite: preview **Punch**, **Block**, and **Overdrive Roar** animations directly inside the workshop.

---

### 🔥 The Crucible (Alchemical Forging Minigame)
- **Rhythmic Anvil & Heat Gauge**:
  - Pump the furnace bellows to heat the incandescent metal into the optimal green **Masterwork Zone**.
  - Strike the anvil with rhythmic precision to forge parts with quality ratings (*Standard*, *Artisan*, or *Mythic Masterwork*).
  - Procedural spark particles and dynamic hammer clangs.
- **Transmutation Station**:
  - Transmute surplus Clay into Iron Ore, and Iron Ore into rare Arcane Crystals.

---

### ⚔ Arena & Defense Expeditions
- **Active Real-Time Combat**:
  - 60 FPS Canvas rendering with parallax atmospheric backdrops.
  - Combat controls: **Move Left / Right**, **Punch Combo**, **Core Blast**, **Shield Wall** (75% damage mitigation), and **Overdrive Rampage** (screen-clearing elemental apocalypse).
  - Dynamic floating damage numbers, knockback physics, hit flashes, and camera screen shake.
- **Campaign Stages & Endless Mode**:
  - Stage 1: *Whispering Caverns* (Corrupted Imp swarmers)
  - Stage 2: *Clockwork Mines* (Armored Clockwork Beetles)
  - Stage 3: *Molten Caldera* (Boss: *Ignis the Magma Colossus*)
  - Stage 4: *Aether Spire Ruins* (Boss: *Aetherius Prime*)
  - *Endless Siege Arena*: Test golem endurance across infinitely scaling waves with personal high-score tracking.

---

### 🔊 Procedural Audio & Save Persistence
- **Zero-Dependency Procedural Audio**:
  - Real-time PCM waveform synthesis using Android's `AudioTrack`.
  - Synthesizes metallic anvil clangs, heavy stone crunches, flame bursts, shield deflects, laser hums, and victory fanfares.
- **Persistent Storage**:
  - Automatically saves unlocked parts, equipped configurations, collected resources (Gold, Clay, Ore, Crystals), high scores, and user settings to JSON storage.

---

## 🛠 Tech Stack
- **Language**: Kotlin 1.9
- **Platform**: Android SDK (Min SDK: 24, Target SDK: 35)
- **UI & Graphics**: Hardware-accelerated custom Canvas views
- **Audio**: Procedural PCM audio via `AudioTrack`
- **Build System**: Gradle 8.14
