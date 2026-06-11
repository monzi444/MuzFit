# 🏋️ MuzFit — Android Fitness Tracker

**Gruppo:** La Volpe
**Corso:** Dispositivi Mobili — Università degli Studi di Milano-Bicocca  
**Anno:** 2025/2026  

---

## 👥 Componenti

| Nome | Matricola |
|---|---|-|
| **Simone Monzardo** | 910008 |
| **Federico Galbiati** | 909517 |
| **Leonardo Boschi** | 914255 |
| **Tommaso Toloni** | 914293 |
| **Davide Pirovano** | 914536 |
| **Emilio Proserpio** | 914546 |

---

## 📱 Il Progetto

MuzFit è un'app Android per il **tracciamento fitness e nutrizionale**. L'utente può:

- 📊 Monitorare **peso, altezza, calorie e macro** su dashboard e profilo
- 🍽️ **Registrare pasti** cercando nel database Open Food Facts o inserendo cibi manualmente
- 🏋️ **Creare ed eseguire workout** con esercizi, serie e timer di recupero
- ⚡ **Quick menu** per azioni rapide (pasto veloce, peso, workout, obiettivi)
- 🔐 **Autenticazione** via email/password o Google Sign-In (Firebase Auth)
- 🌙 **Dark mode** nativa con glassmorphism e blur gaussiano

### Tech Stack

| Categoria | Tecnologia |
|---|---|
| **Linguaggi** | Java + Kotlin (Compose) |
| **UI Framework** | Material3, Jetpack Compose |
| **Database** | Firebase Firestore + Room (locale) |
| **Auth** | Firebase Authentication (email + Google) |
| **Blur** | Dimezis BlurView (`RenderEffectBlur`, API 31+) |
| **Icone** | Lucide Icons (35+ drawable vettoriali) |
| **Font** | Syne (unico font per tutta l'app) |
| **Chart** | MPAndroidChart (grafico peso) |
| **Food API** | Open Food Facts |
| **Exercise API** | ExerciseDB |
| **Build** | Gradle 9.4.1 + Kotlin DSL |

---

## 🔧 Setup Locale

### Prerequisiti

- **Android Studio** Hedgehog (2024.1+) o superiore
- **JDK 21** (consigliato `C:\Program Files\Java\jdk-21`)
- **Gradle 9.x** (incluso nel wrapper `./gradlew`)
- **Git** con Git Bash (Windows)

### Clonare il progetto

```bash
git clone <url-repository>
cd MuzFitreal
```

### Configurare Firebase

1. Vai alla [Firebase Console](https://console.firebase.google.com/)
2. Crea o apri il progetto MuzFit
3. Aggiungi la tua **SHA-1** in **Impostazioni progetto → App Android**
4. Scarica `google-services.json` e mettilo in `app/`
5. Abilita i provider di autenticazione: **Email/Password** e **Google**

### API Key

Crea `app/local.properties` (se non esiste) e aggiungi:

```properties
# Open Food Facts — non serve API key (gratuito, rate-limited)
# ExerciseDB — chiave pubblica già in Constants.java
```

Le API key per ExerciseDB e Open Food Facts sono già configurate in `Constants.java`. Nessuna configurazione aggiuntiva necessaria.

### Build

```bash
# Su Windows (Git Bash):
cd "C:\Users\<tuo-utente>\Desktop\...\MuzFitreal"

# Build debug
JAVA_HOME="C:\Program Files\Java\jdk-21" ./gradlew assembleDebug

# Build release
JAVA_HOME="C:\Program Files\Java\jdk-21" ./gradlew assembleRelease
```

Se non hai JDK 21 installato, impostalo in Android Studio:  
`File → Project Structure → SDK Location → JDK Location`

---

## 🌳 Struttura Branch

### Branch Principali

| Branch | Scopo |
|---|---|
| **`main`** | Produzione — codice stabile, pronto per il rilascio |
| **`develop`** | Sviluppo — base per tutte le nuove feature |

### Branch Temporanei

| Tipo | Pattern | Uso |
|---|---|---|
| Feature | `feature/nome-feature` | Nuove funzionalità |
| Bugfix | `bugfix/nome-bug` | Correzione bug |
| Hotfix | `hotfix/nome-fix` | Fix urgenti in produzione |
| UI | `ui/nome-componente` | Ridisegno UI / design system |

---

## 🔄 Workflow Git

### 1. Iniziare una Feature

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nome-feature
```

### 2. Lavorare e Committare

```bash
git add .
git commit -m "<tipo>: <descrizione>"
git push -u origin feature/nome-feature
```

### 3. Rimanere Aggiornati

```bash
git checkout develop
git pull origin develop
git checkout feature/nome-feature
git rebase develop
```

**⚠️ Se ci sono conflitti:** risolvi i file, poi:
```bash
git add .
git rebase --continue
git push --force-with-lease
```

### 4. Pull Request

1. Vai su GitHub → **Pull Requests → New Pull Request**
2. Base: `develop` ← Compare: `feature/nome-feature`
3. Compila il template PR (vedi sotto)
4. Assegna almeno **2 reviewer** del gruppo
5. Dopo approvazione: **Merge** e cancella il branch

---

## 📝 Convenzioni Commit

Usiamo **Conventional Commits**:

| Prefisso | Quando usarlo |
|---|---|
| `feat:` | Nuova funzionalità |
| `fix:` | Correzione di un bug |
| `ui:` | Modifica UI / stile / layout |
| `docs:` | Documentazione (README, commenti) |
| `refactor:` | Ristrutturazione codice senza cambiare comportamento |
| `test:` | Aggiunta o modifica test |
| `style:` | Formattazione, spazi, punto e virgola (nessun cambiamento logico) |
| `perf:` | Miglioramento performance |

**Esempi:**
```bash
git commit -m "feat: add weight tracking chart to dashboard"
git commit -m "ui: redesign profile page with new glass card style"
git commit -m "fix: prevent ClassCastException in weight management dialog"
git commit -m "docs: update README with setup instructions"
```

---

## 🔍 Template Pull Request

```markdown
## ✏️ Cosa fa
Breve descrizione della modifica (1-2 frasi).

## 📋 Modifiche
- Modifica 1
- Modifica 2

## 🧪 Test
- [ ] Build completata con successo (`assembleDebug`)
- [ ] Testato su emulatore / dispositivo fisico
- [ ] Nessun crash su dark mode / light mode
- [ ] Nessuna regressione su dialog esistenti

## 🎨 UI (se applicabile)
- [ ] Usa font **Syne** (mai altro font)
- [ ] Icone **Lucide** (mai `@android:drawable`)
- [ ] Bottoni con `cornerRadius="14dp"`
- [ ] BlurView ha `applyRoundedOutline()` chiamato
- [ ] Label + Title header presenti

## 📸 Screenshot
<!-- Aggiungi screenshot prima/dopo se la modifica è visiva -->
```

---

## 🗂️ Struttura del Progetto

```
MuzFitreal/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/muzfit/
│   │   │   ├── adapter/            # RecyclerView adapters
│   │   │   ├── model/              # Data classes (User, Meal, WorkoutRoutine...)
│   │   │   ├── repository/         # Diet, Training, Quick repositories
│   │   │   ├── service/            # API services + DTOs
│   │   │   ├── source/             # Firebase, Room, OpenFoodFacts data sources
│   │   │   ├── ui/
│   │   │   │   ├── auth/           # Login, ProfileSetup
│   │   │   │   ├── dashboard/      # HomeFragment (weight chart, insights)
│   │   │   │   ├── diet/           # DietFragment, MealSections (Compose), DietDialogHelper
│   │   │   │   ├── profile/        # ProfileFragment, ProfileDialogHelper
│   │   │   │   ├── quick/          # QuickOverlayFragment (FAB menu)
│   │   │   │   └── training/       # WorkoutFragment, WorkoutSessionActivity, TrainingDialogHelper
│   │   │   └── utils/              # Constants, ServiceLocator, helpers
│   │   ├── res/
│   │   │   ├── drawable/           # Lucide vector icons (35+), glass drawables, placeholders
│   │   │   ├── font/               # Syne Regular + Bold
│   │   │   ├── layout/             # 25+ XML layout files
│   │   │   ├── values/             # colors.xml, strings.xml, themes.xml
│   │   │   └── anim/               # quick_fade_in, quick_slide_in_up
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json        # NON committare (in .gitignore)
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

---

## 🎨 Design System

MuzFit segue il **MuzFit Design System v4.0**.  
Documenti di riferimento nella cartella `docs/`:

- **`muzfit-design-system.md`** — Specifica completa (colori, tipografia, spaziatura, componenti)
- **`muzfit-brandboard.html`** — Brand identity guidelines con asset visivi

### Principi fondamentali

- 🎯 **UI in inglese** — tutte le stringhe utente sono in inglese
- 🔤 **Font Syne** — unico font per tutta l'app (display, body, label)
- 🖼️ **Icone Lucide** — solo `ic_lucide_*` drawable, mai icone di sistema
- 🔲 **Blur su ogni dialog** — Dimezis `BlurView` a 30f con `applyRoundedOutline` 28dp
- 🔘 **Bottoni Material3** — `cornerRadius="14dp"`, altezza `52dp`
- 🧊 **Glass card** — `MuzFit.Card.Glass` per tutte le card
- 🏷️ **Pattern Label+Title** — ogni schermata e dialog inizia con label 11sp uppercase + title 22sp bold

### Checklist rapida per contributi UI

```
✓ Font: `@font/syne_regular` / `@font/syne`
✓ Icone: `@drawable/ic_lucide_*`  (mai `@android:drawable`)
✓ Bottoni: `style="@style/Widget.Material3.Button"` + `cornerRadius="14dp"`
✓ Dialog: BlurView + `setup*Blur()` + `applyRoundedOutline` 
✓ Stringhe: in inglese in `strings.xml`
✓ Backend: chiavi JSON e path API restano in italiano
✓ ID view: mantenuti per retrocompatibilità con `findViewById`
```

---

## ⚠️ Regole Importanti

### ✅ DA FARE

- Commit **frequenti e piccoli** (max ~200 righe per commit)
- Branch **brevi** (< 1 settimana, < 20 file cambiati)
- **Testare la build** prima di pushare (`assembleDebug`)
- **Aggiornarsi spesso** da `develop` (ogni giorno)
- Segnalare **subito** se si tocca un file condiviso (es. `strings.xml`)
- Usare `@color/muz_*` per i colori, mai HEX hardcoded

### ❌ NON FARE

- **Push diretto** su `main` o `develop` — solo via PR
- **Force push** su branch condivisi (solo `--force-with-lease` sui propri)
- **Committare** file sensibili: `google-services.json`, `local.properties`, `.env`
- **Branch aperti troppo a lungo** — massimo 1 settimana, poi merge o chiudi
- **Modificare file di altri** senza comunicarlo
- **Cambiare il root layout** di un dialog senza aggiornare i cast Java (`LinearLayout` → `ViewGroup`)

---

## 🛠️ Comandi Essenziali

```bash
# Stato repository
git status
git branch -a
git diff

# Cambiare branch
git checkout nome-branch
git checkout -b feature/nuova-feature

# Aggiornare
git pull origin develop
git fetch --all

# Salvare temporaneamente
git stash
git stash pop

# Annullare modifiche
git checkout -- <file>        # Singolo file
git reset --hard HEAD         # Tutto (ATTENZIONE: perde modifiche non committate)

# Eliminare branch locale
git branch -d feature/vecchia-feature

# Vedere lo storico
git log --oneline --graph -20
```

---

## 🆘 Risoluzione Problemi Comuni

### Build fallita: `resource string/... not found`
Un altro membro ha modificato `strings.xml`. Fai `git pull origin develop` e verifica che la stringa esista.

### Crash `ClassCastException: FrameLayout cannot be cast to LinearLayout`
Qualcuno ha aggiunto un `FrameLayout` root a un dialog per il BlurView. Aggiorna il cast Java a `ViewGroup`.

### Conflitti durante rebase
```bash
# Risolvi i conflitti nei file (cerca `<<<<<<<`)
git add .
git rebase --continue
# Se sei bloccato:
git rebase --abort   # Torna indietro e chiedi aiuto
```

### Devo modificare l'ultimo commit
```bash
git commit --amend -m "feat: nuovo messaggio corretto"
git push --force-with-lease
```

### Ho committato sul branch sbagliato
```bash
git reset --soft HEAD~1        # Annulla ultimo commit (mantiene modifiche)
git stash                      # Salva modifiche
git checkout branch-giusto
git stash pop                  # Ripristina modifiche
git commit -m "messaggio"
```

### `google-services.json` mancante
Il file è in `.gitignore`. Ogni membro deve generare il proprio dalla Firebase Console e metterlo in `app/`.

---

## 📊 Flusso Completo

```
main (produzione)
  │
  └── develop (sviluppo)
        │
        ├── feature/login-google
        │     ↓ (lavoro + commit + push)
        │     ↓ (Pull Request → develop)
        │     ↓ (Review + Approvazione)
        │     ↓ (Merge)
        └── develop (aggiornato)
              │
              └── ... prossima feature

Quando develop è stabile:
  develop → Pull Request → main → Tag versione (v1.0, v1.1...)
```

---

## 📞 Contatti & Supporto

- **Gruppo Telegram/WhatsApp:** La Volpe 🦊
- **Repository:** (URL da inserire)
- **Design System:** `muzfit-design-system.md` + `muzfit-brandboard.html`

> *"Se hai un dubbio, chiedi. È meglio una domanda in più che un force push di troppo."*

---

**La Volpe 🦊 — 2025/2026**
