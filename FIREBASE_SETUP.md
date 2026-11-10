# Connect NEBULA to your Firebase account

NEBULA uses three Firebase services, all on the free **Spark** plan:

| Service | Used for |
|---|---|
| **Authentication** | Email/password accounts that work on any phone (+ anonymous guests) |
| **Cloud Firestore** | The shared Socials feed (posts and their photos) |
| **Realtime Database** | Live likes on posts |

Until you finish this, the app still runs: login/register work locally and the Socials tab shows
"not connected".

## Step 1: Create the project
1. Open https://console.firebase.google.com and sign in with your Google account.
2. **Add project**, give it a name (e.g. `nebula`), Analytics is optional (you can turn it off), **Create**.

## Step 2: Turn on Authentication
**Build > Authentication > Get started > Sign-in method**. Enable both:
- **Email/Password**
- **Anonymous**

## Step 3: Create Firestore and publish its rules
1. **Build > Firestore Database > Create database**. Choose a location, start in **production mode**.
2. Open the **Rules** tab, delete what is there, paste the full contents of `firestore.rules`
   (in the project root), and click **Publish**.

## Step 4: Create the Realtime Database and publish its rules
1. **Build > Realtime Database > Create database**. Choose a location, start in **locked mode**.
2. Open the **Rules** tab, paste the full contents of `database.rules.json`, and click **Publish**.

## Step 5: Register the Android app and download the config file
Do this step LAST, after steps 3 and 4. The file has to contain your Realtime Database URL.
1. **Project settings** (gear icon) > **Your apps** > **Add app** > Android icon.
2. **Android package name: `com.nebula.app`** (exactly). Nickname optional. SHA-1 is not needed.
   Click **Register app**.
3. Click **Download google-services.json**.
   (If you registered the app earlier, re-download it from Project settings > Your apps so it includes
   the database URL.)

## Step 6: Drop the file into the project
1. Copy the downloaded file over `Nebula/app/google-services.json` (replace the existing placeholder).
   It must sit in the `app` folder, next to `app/build.gradle`.
2. In Android Studio: **File > Sync Project with Gradle Files**, then **Build > Clean Project**, then **Run**.

## Step 7: Test it
1. Run on a phone/emulator, tap **Register**, create an account.
2. Open the **Socials** tab, tap **+**, choose **Camera** or **Gallery**, add a caption, **Post**.
3. Run the app on a second device, register a different account, and you should see the post.
   Tap the heart to like it; the count updates live on both devices.

## Troubleshooting
| Symptom | Fix |
|---|---|
| Socials says "not connected to a server yet" | `app/google-services.json` is still the placeholder. Redo steps 5 and 6. |
| "Couldn't connect to Socials" | Enable **Anonymous** sign-in (step 2) and check internet. |
| "Couldn't load posts. PERMISSION_DENIED" | Publish `firestore.rules` (step 3). |
| Login says "operation not allowed" | Enable **Email/Password** (step 2). |
| Hearts do nothing / stay 0 | Create the Realtime Database and publish `database.rules.json` (step 4), then re-download `google-services.json` (step 5) and rebuild. |
| Build error about `google-services.json` | The package name in the file must be exactly `com.nebula.app`. |

## Notes
- Photos are resized (max 1080 px), compressed to ~350 KB and stored inside the Firestore post, so
  Firebase **Storage** is not needed (it now requires a paid plan for new projects).
- Watch the Activity lifecycle in Logcat: filter by tag `NebulaLifecycle`.

## Easy tweaks
| What | Where |
|---|---|
| Time of the daily space-photo notification (19:00) | `DAILY_HOUR` in `util/NebulaScheduler.java` |
| Topics the daily photo rotates through | `TOPICS` in `network/SpacePhotoRepository.java` |
| "Don't you miss the stars." wording | `nudge_title` / `nudge_text` in `res/values/strings.xml` |
