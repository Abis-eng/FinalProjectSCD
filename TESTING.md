# E-Clinic Testing Guide

## Automated tests (JUnit)

Run all tests:

```bash
mvn test
```

**70 tests** cover:

| Area | What is tested |
|------|----------------|
| Registration | Patient (active), doctor/nurse (pending), duplicate username/email, validation |
| Auth | Login, wrong password, pending/rejected/deactivated accounts |
| Staff verification | Approve, reject with reason, notifications |
| Appointments | Booking, conflicts, past dates, status transitions |
| Fees (PKR) | Doctor base fee, multipliers, nurse fee, custom fee |
| Billing | Invoice create, pay, waive, invalid payment method |
| Validation | Username, email, password, dates, IDs |
| End-to-end | Patient register → login → doctor approve → book → pay |

## Manual UI checklist (requires MySQL + Tomcat)

**Prerequisite:** XAMPP MySQL **running**, then restart Smart Tomcat.

### 1. Admin
- [ ] Login: `admin` / `admin123`
- [ ] Dashboard charts load (appointments, status, revenue in PKR)
- [ ] **Verify Staff** — approve a pending doctor
- [ ] **Patients** — patient **names** visible (not only ID)
- [ ] Create staff doctor with fee

### 2. Register accounts (incognito / different browsers)

| Role | Expected after register |
|------|-------------------------|
| Patient | Can login immediately |
| Doctor | Cannot login until admin approves |
| Nurse | Cannot login until admin approves |

**Validation to try:**
- Username `ab` → error (too short)
- Password `123` → error (min 6 chars)
- Email `bad` → error
- Duplicate username → error

### 3. Doctor
- [ ] After approval, login works
- [ ] **Settings** → set consultation fee (e.g. Rs. 5000)
- [ ] **Patients** — see assigned patients with **names**
- [ ] Book appointment — fee shows in PKR from your fee × visit type

### 4. Patient
- [ ] Register + login
- [ ] Book appointment with a doctor — see estimated PKR fee
- [ ] **Billing** — pay with CASH/CARD/ONLINE/BANK_TRANSFER only
- [ ] View records, labs, profile

### 5. Nurse
- [ ] Register → admin approve → login
- [ ] View appointments

### 6. Negative cases
- [ ] Pending doctor login → “awaiting administrator approval”
- [ ] Rejected nurse login → shows rejection reason
- [ ] Book same doctor + date + time twice → conflict error
- [ ] Pay invoice with method `BITCOIN` → error

## Bugs fixed during test pass

1. **Registration cleared password in DB** — fixed (return copy without mutating stored user).
2. **Rejected login showed “deactivated”** — fixed (check rejected before inactive).
