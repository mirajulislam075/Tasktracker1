# Getting an APK without Android Studio

If you don't want to install Android Studio locally, GitHub can build the APK for you in the cloud. Free, one-time setup, ~10 minutes end to end.

## Steps

1. **Create a GitHub account** if you don't have one (github.com/join).

2. **Create a new empty repository** — go to github.com, click "New", name it `TaskTracker`, make it Private (or Public, doesn't matter), don't add a README or .gitignore (the zip already has them). Click "Create repository".

3. **Push this code to that repo.** GitHub's next page shows you three options; pick "…or push an existing repository from the command line". You'll need Git installed on your computer. On the command line inside the unzipped `TaskTracker` folder:
   ```
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR-USERNAME/TaskTracker.git
   git push -u origin main
   ```
   GitHub will ask for a username and a Personal Access Token (not your password) — instructions at github.com/settings/tokens if you don't have one.

4. **The build starts automatically.** Go to the "Actions" tab on your repo page. You'll see "Build APK" running. First run takes 5–8 minutes (it downloads the Android SDK). Later runs are faster (~2 minutes) because of caching.

5. **Download the APK.** When the run finishes with a green check, click into it, scroll to the bottom, find "Artifacts" → `TaskTracker-debug-apk`. Download the zip, unzip it, and you have `app-debug.apk`.

6. **Install on your phone.** Transfer the APK (email to self, WhatsApp, USB, Google Drive), tap it on the phone, allow "install from unknown sources" when prompted, install. Done.

## Every time you want an updated build

Change something in the code, commit, push. GitHub builds it. Download the new APK from the Actions tab.

## Alternatives if GitHub isn't your thing

- **Android Studio on your own computer.** Ladybug installer is a straightforward wizard, ~15 minutes end to end. Details in the main `README.md`.
- **Ask a colleague or friend who has Android Studio** to build the APK once. They open the project, `Build → Build APK`, send you the `app-debug.apk`.
- **Codemagic, Bitrise, or CircleCI** — other cloud CI services with free tiers. Setup is similar to GitHub Actions but different UIs.

## Notes on the debug APK

The build produces a **debug-signed APK**. That's fine for personal use — it installs and runs normally on your phone. The only differences from a release build: the app is slightly larger, marginally slower, and can't be published to the Play Store (which you don't want to do anyway). For a personal app, debug is the right choice.
