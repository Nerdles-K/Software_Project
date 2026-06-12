# 🎬 VisiTalk — Demo Script (Presentation Day)

| | |
|---|---|
| **Demo date** | 2026-06-15 |
| **Live app** | https://frontend-nine-lyart-85.vercel.app |
| **Demo account (family FAM001)** | Parent: `parent@test.com` / `password123` · Child: `child@test.com` / `password123` |
| **Backend wake-up** | https://software-project-m0jm.onrender.com/api/health |
| **Total demo length** | ~8 minutes |

> **The story:** *Leo, a 6-year-old autistic boy, and his mum Sarah.* We follow them through one day — Leo using picture cards and a visual schedule to navigate daily life, and Sarah tracking his emotions privately. Lead with the *why*, not the features.

---

## ✅ Pre-demo checklist (do this 5 min before going on stage)

1. **WAKE THE BACKEND.** Open the health URL above; wait until it shows `{"status":"UP"}`. The free tier sleeps after 15 min idle and cold-starts take 30–90 s — do **not** skip this or the first screen will hang.
2. Open **two browser windows** side by side (or two devices):
   - **Window C** → log in as **child** (`child@test.com`). This is "Leo's tablet".
   - **Window P** → log in as **parent** (`parent@test.com`). This is "Sarah's phone".
   - *(If a screen asks for a PIN, it is `1234`.)*
3. In each window, if anything looks stale: open DevTools console → `localStorage.clear(); location.reload()` then log in again.
4. Have the **backup video** ready in another tab in case the network drops.

---

## 🗣️ Opening (30 s) — the *why*

> "About 1 in 100 children is on the autism spectrum, and many are non-verbal or struggle to communicate needs. **VisiTalk** gives them a voice through picture cards and visual routines — and gives parents a private way to understand their child's emotions. Meet Leo and his mum, Sarah."

---

## 👦 Part 1 — Leo's side (child mode, ~3 min) · *Window C*

### Scene ① Morning routine — Visual Schedule (B-1/B-2/B-3)
1. On Leo's home, tap the **🗓️ schedule** button (bottom-left).
2. Show **"Go to the supermarket"** — 8 picture steps (shoes → walk → cart → apples → milk → bread → pay → home).
3. Tap the big ✓ checkboxes one by one. Point out: the **current step is highlighted**, finished steps go green.
4. Finish the last step → **🎉 celebration**.
   > *"Routines and transitions are hard for autistic kids. A picture schedule turns an abstract task into clear, finishable steps."*

### Scene ② "I want…" — PECS communication (A-1/A-2 + new library)
1. Tap **← back** to Leo's board. Show the **category tabs**: Need, Daily, Place, Eat, Drink, Play, Feel (100+ cards).
2. From **Need** tap **More / Help**, then **Eat → Apple**, building a strip at the top: *I want apple.*
3. Tap **Save / send** → it appears in the family chat.
   > *"Every card shows a picture **and** the word — that's how PECS builds language."*

### Scene ③ Expressing feelings — Private diary (C-6)
1. Tap the **💗 diary** button (bottom-right).
2. Pick an emotion face, optionally **doodle** on the pad, tap **Done ✓** → "Saved!".
   > *"This is Leo's private space — and here's the important part…"* (segue to Sarah).

---

## 👩 Part 2 — Sarah's side (parent mode, ~3 min) · *Window P*

### Scene ④ Log a behavior event in 3 taps (C-1/C-2)
1. Note the **dark, professional UI** — deliberately different from the child's playful one.
2. Go to **Log** → tap **intensity** → tap a **trigger** (e.g. loud noise) → **Save**. Three taps, done.
   > *"In a real meltdown moment a parent has seconds — logging must be effortless."*

### Scene ⑤ Weekly trend + alert (C-3/C-5/C-4)
1. Open **Report**. Show the **emotion-intensity trend** for the week — it's **trending down** (calmer).
   > *"Over the week Leo's episodes are getting less intense — the routines are helping."*
2. Show **Top 3 triggers**: *loud noise, transitions, hunger.*
3. Go to **Settings** (or the alert banner): a red **3-day alert** — *"loud noise" has recurred 3 days running.*
   > *"VisiTalk spots patterns a tired parent might miss and nudges early intervention."*
4. Back on Report, tap **Share / Export** → a **24-hour share link** (for a therapist or co-parent).

### Scene ⑥ The privacy boundary (C-7) — the ethical highlight
1. In **Settings**, show **"Child's private diary"**: the status reads **"✓ wrote today"** — **no content, ever**.
   > *"Sarah knows Leo expressed himself today, but his drawing and feelings stay private. Trust by design — enforced in the database, not just the UI."*

---

## 🛠️ Part 3 — It's real, and it's engineered (~1.5 min)

1. Pull out a **phone**, scan/open the **live URL** — "anyone can use this right now."
2. One slide / 20 seconds on the build:
   - **Frontend** Vue 3 + TypeScript + Tailwind → **Vercel**
   - **Backend** Spring Boot 3 + JWT → **Render** (Docker)
   - **Database** PostgreSQL 17 → **Neon**, with row-level privacy
   - GitHub + CI; deployed and verified end-to-end.

> **Closing line:** *"VisiTalk isn't a mock-up — it's a deployed product that helps an autistic child ask for an apple, finish their morning, and keep their feelings their own."*

---

## 🧯 If something breaks
- **Screen hangs on first load** → backend was asleep; wait ~60 s and refresh (this is why we pre-warm).
- **Login rejected** → `localStorage.clear(); location.reload()`.
- **Network dies** → switch to the **backup video** tab; narrate over it.

## 👥 Roles
- **Driver** (operates both windows) — Xu Ziyang
- **Narrator / value & privacy story** — PO
- **Architecture & engineering** — Xu Zihe
- **Timekeeper + backup video + Q&A** — Ke Hongyi (SM)
