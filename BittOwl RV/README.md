# BittOwl

BittOwl is a lightweight Android app available in two versions: a privacy-first calculator (Green Version) and an enterprise/consent-based diagnostics tool (Red Version). 

---

This is the Red Version.

### BittOwl RV — Red Version (Diagnostics & Forensics — Consent Required)

A diagnostics and reporting tool intended for **lawful, consented** device diagnostics, troubleshooting, or enterprise management.  

**Important:** This edition must only be used on devices you own/manage or with **explicit informed consent** from the device owner. Improper or non-consensual use may be illegal.

**Capabilities (consent-based)**
- Device metadata (model, OS version)
- Installed apps list
- App usage & screen time summaries (requires Usage Access)
- Data usage summaries (per-app)
- Storage/space summary and top folders
- Call log & SMS export (requires explicit permission)
- Contacts export (requires explicit permission)
- Optional user-initiated media attachments: photo and short voice note
- Packages all artifacts into a single encrypted archive for secure upload
- Audit logging and configurable retention policies

**Intended use-cases**
- Enterprise device support (MDM-managed devices)
- Lawful forensic collection in controlled environments
- Troubleshooting where device owner consents to data collection

---

## Permissions

**RV (Diagnostics — requires explicit consent)**  
The Red Version may request permissions such as:

- `INTERNET` — upload reports securely  
- `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE` — check and manage network state  
- `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` (maxSdkVersion=28) — save and read report files  
- `PACKAGE_USAGE_STATS` — get app usage & screen time  
- `READ_CALL_LOG`, `READ_SMS` — export call and SMS logs  
- `READ_CONTACTS` — export contacts  
- `READ_CALENDAR`, `WRITE_CALENDAR` — export calendar events  
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` — get precise or approximate location  
- `RECEIVE_BOOT_COMPLETED` — resume scheduled tasks after reboot  
- `BLUETOOTH`, `BLUETOOTH_ADMIN` — detect paired devices  
- `GET_ACCOUNTS` — retrieve device accounts  
- `CAMERA` — capture user photo  
- `RECORD_AUDIO` — capture short audio note

> Each permission must be explained to the user and requested only with a clear, informed consent screen.

---

## Install & Build (AIDE / Android Studio)

1. Clone the repository:
   ```bash
   git clone https://github.com/MwendaKE/BittOwl.git
   cd BittOwl
