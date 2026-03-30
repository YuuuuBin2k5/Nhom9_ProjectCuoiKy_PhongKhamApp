# Server-Driven Countdown System - Detailed Design

## 🎯 Objective
Implement a **server-authoritative countdown timer** that provides accurate, real-time countdown for patients in queue while avoiding common pitfalls.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    MOBILE CLIENT                             │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  PatientQueueActivity                                  │ │
│  │                                                        │ │
│  │  Server Time: 2026-03-31 10:30:45                     │ │
│  │  Local Time:  2026-03-31 10:30:45                     │ │
│  │  Offset: 0ms                                           │ │
│  │                                                        │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  Countdown Timer (Client-side tick)              │ │ │
│  │  │                                                  │ │ │
│  │  │  Server says: "5 minutes remaining"             │ │ │
│  │  │  Target time: 10:35:45                          │ │ │
│  │  │                                                  │ │ │
│  │  │  Display: 04:58 (counting down)                 │ │ │
│  │  │           ↓ every second                        │ │ │
│  │  │           04:57                                 │ │ │
│  │  │           04:56                