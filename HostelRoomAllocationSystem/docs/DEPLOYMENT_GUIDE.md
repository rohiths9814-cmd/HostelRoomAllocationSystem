# Online Deployment Guide

Putting this project on the internet: **Railway** for the database and the
Java API, **Vercel** for the React frontend. Both have free tiers that are
enough for a college project.

---

## 0. The picture, before you touch anything

Right now everything runs on your laptop. Online, the three parts split up
and live in different places:

```
   Your browser
        |
        |  https://your-project.vercel.app
        v
   ┌─────────────┐          ┌──────────────────┐         ┌──────────────┐
   │   VERCEL    │  HTTPS   │     RAILWAY      │  MySQL  │   RAILWAY    │
   │  React app  │ ───────> │   Java API       │ ──────> │   MySQL DB   │
   │  (static)   │  /api/.. │   ApiServer      │  :3306  │              │
   └─────────────┘          └──────────────────┘         └──────────────┘
      free              free trial / ~$5 credit        same Railway project
```

Three things must be true or nothing works:

1. The **API must know the database address** → environment variables
2. The **API must allow the Vercel address** → CORS
3. The **frontend must know the API address** → `VITE_API_BASE_URL`

Each of those is one setting, and each has its own confusing error message
when you get it wrong. §9 lists them.

> **The console app does not get deployed.** A container has no keyboard, so
> `Main` would start and exit immediately. Only `ApiServer` is deployed. Your
> console app keeps working on your laptop exactly as before — and that is
> still what you demo in the viva.

---

## 1. About the database — read this first

You said you have no idea about the DB, so here is the whole idea in a
paragraph.

Your MySQL right now lives inside your laptop. `localhost` means "this same
computer". Once the API is running on Railway's machines, `localhost` means
*Railway's* machine — which has no MySQL on it, and certainly cannot reach
into your bedroom to find yours. So the database has to move online too.

**Railway can host MySQL for you.** You add it to the same project with one
click, and Railway creates:

* a MySQL server
* an empty database (named `railway` by default)
* a username and a long random password
* environment variables holding all of the above

Because the API and the database are in the same Railway project, they talk
over Railway's **private network** — the database is never exposed to the
internet, and the traffic never leaves their data centre.

**You do not type the password anywhere.** You reference Railway's variables,
and Railway substitutes the real values at startup. That is why the code was
changed to read environment variables: so no password is ever written into a
`.java` file or pushed to GitHub.

---

## 2. What I changed so this can deploy

The code as you had it **could not be deployed** — four things would have
broken. All four are now fixed, and **nothing about running it locally has
changed** (every setting falls back to its old local value).

| # | Problem | Fix | File |
|---|---|---|---|
| 1 | Database settings were hard-coded to `localhost` / your password | Read from environment variables, falling back to local defaults | `util/DBConnection.java` |
| 2 | Port hard-coded to 8080. Cloud hosts assign their own port and route to it — a fixed port means **502 Bad Gateway** on every request | Reads `PORT`, defaults to 8080. Also binds `0.0.0.0` instead of localhost, or the container would be unreachable | `server/ApiServer.java` |
| 3 | CORS allowed only `http://localhost:5173`, so the Vercel site would be **blocked by the browser** on every page | Reads `ALLOWED_ORIGINS` (comma separated), defaults to localhost | `server/BaseHandler.java` |
| 4 | `schema.sql` starts with `DROP DATABASE` / `CREATE DATABASE`, which a cloud database user is **not allowed to do** | New `database/schema-cloud.sql` creates only the tables | `database/` |

New files: `Dockerfile`, `.dockerignore`, `railway.json`,
`hostel-frontend/vercel.json`, `server/HealthHandler.java`.

All of this is tested — see §10.

---

## 3. Push to GitHub first

Both platforms deploy from a Git repository. From the project folder:

```bash
git add -A
```

```bash
git commit -m "Prepare project for cloud deployment"
```

Create an empty repository on <https://github.com/new> (public is fine), then:

```bash
git remote add origin https://github.com/YOUR-USERNAME/hostel-management.git
```

```bash
git push -u origin main
```

**Check before pushing:** `bin/` and `node_modules/` must not be included —
`.gitignore` already handles both. `git status` should show roughly 100 files,
not thousands.

> Your MySQL password `admin123` is currently the default inside
> `DBConnection.java`. It is only your *local* password and the cloud will
> override it, but if that bothers you, change the default to something
> meaningless like `changeme` before pushing. The cloud reads the real value
> from an environment variable either way.

---

## 4. Railway — the database

1. Go to <https://railway.app> and sign in with GitHub
2. **New Project** → **Deploy MySQL**
3. Wait about a minute for it to start

Click the MySQL service → **Variables** tab. You will see:

| Variable | Meaning |
|---|---|
| `MYSQLHOST` | the server address |
| `MYSQLPORT` | the port |
| `MYSQLDATABASE` | the database name (usually `railway`) |
| `MYSQLUSER` | the username (usually `root`) |
| `MYSQLPASSWORD` | a long random password |

Leave them alone. You never copy these by hand.

### Load the tables

Click the MySQL service → **Data** tab → **Query**. Open
`database/schema-cloud.sql` in VS Code, copy **all** of it, paste it in, and
run it.

You should see `All 8 tables created successfully.` Refresh the Data tab and
the 8 tables will be listed, with the sample data already in them.

> **Use `schema-cloud.sql`, not `schema.sql`.** The normal one begins with
> `CREATE DATABASE`, which your Railway user is not permitted to do, and it
> will stop with an "Access denied" error.

---

## 5. Railway — the Java API

In the **same** Railway project (important — the private network only works
inside one project):

1. **New** → **GitHub Repo** → pick your repository
2. Railway reads `railway.json`, sees `"builder": "DOCKERFILE"`, and builds
   using the `Dockerfile`. This takes 2–3 minutes the first time.

### Set the variables

Open the new service → **Variables** → **New Variable**, and add these five.
The `${{...}}` syntax means "take the value from the MySQL service" — type it
exactly, including the service name Railway gave your database:

| Name | Value |
|---|---|
| `DB_HOST` | `${{MySQL.MYSQLHOST}}` |
| `DB_PORT` | `${{MySQL.MYSQLPORT}}` |
| `DB_NAME` | `${{MySQL.MYSQLDATABASE}}` |
| `DB_USER` | `${{MySQL.MYSQLUSER}}` |
| `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |

Do **not** set `PORT` — Railway sets that itself, and overriding it breaks
the routing.

Leave `DB_SSL_MODE` unset. Inside Railway's private network the traffic never
touches the internet, so plain connections are fine. Only set it to
`REQUIRED` if you ever connect to a database on a different provider.

### Get the public address

**Settings** → **Networking** → **Generate Domain**. You get something like:

```
https://hostel-api-production-a1b2.up.railway.app
```

### Prove it works before going further

Open this in your browser:

```
https://YOUR-RAILWAY-URL/api/health
```

You want:

```json
{"success":true,"status":"UP","database":"UP"}
```

| What you see | What it means |
|---|---|
| `"database":"UP"` | everything is correct, continue |
| `"database":"DOWN: ..."` | the Java app is running, the DB variables are wrong — recheck §5 |
| page will not load at all | the build failed — read the Deploy Logs |

**Do not move on until this returns `UP`.** Almost every deployment problem
is easier to find here than after the frontend is involved.

---

## 6. Vercel — the React frontend

1. Go to <https://vercel.com> and sign in with GitHub
2. **Add New** → **Project** → import the same repository
3. Configure:

| Setting | Value |
|---|---|
| **Root Directory** | `hostel-frontend` ← **you must change this** |
| Framework Preset | Vite (auto-detected) |
| Build Command | `npm run build` |
| Output Directory | `dist` |

4. Expand **Environment Variables** and add:

| Name | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://YOUR-RAILWAY-URL/api` |

Note the `/api` on the end, and **no trailing slash**.

5. **Deploy**

You get a URL like `https://hostel-management-xyz.vercel.app`.

> `vercel.json` is already in the frontend folder. It sends every path back
> to `index.html`, which React Router needs — without it, opening
> `/students` directly returns a Vercel 404, even though clicking through
> from the home page works. That inconsistency confuses everyone the first
> time.

---

## 7. The step everybody forgets — tell the API about Vercel

The frontend can now reach the API, but the **browser** will block every
response, because the API has not been told that your Vercel site is allowed.

Go back to Railway → your API service → **Variables** → add:

| Name | Value |
|---|---|
| `ALLOWED_ORIGINS` | `https://hostel-management-xyz.vercel.app` |

Use your real Vercel URL, with `https://`, and **no trailing slash**.

Railway redeploys automatically. Wait a minute, then open your Vercel site
and log in with `admin` / `admin123`.

> **Every Vercel preview deployment gets its own URL** (`...-git-branch-...`).
> Those will be blocked too. For a project demo just add the main URL; if you
> need previews, add them comma-separated:
> `https://main-url.vercel.app,https://preview-url.vercel.app`

---

## 8. Optional — lock the API down a little

**Read this honestly.** As it stands, every API endpoint is open. Anyone who
finds your Railway URL can list your students, or delete them. For a college
demo with fake data that is usually acceptable — but somebody could wipe your
data the night before your viva.

The `admins` table also stores passwords as plain text, and logging in
returns no token, so the login screen is presentation, not security.

**Minimal improvement:** set a shared secret.

1. Railway → API service → Variables → `API_KEY` = any long random string
2. Vercel → Settings → Environment Variables → `VITE_API_KEY` = the same string
3. Redeploy both

Now requests without the matching `X-Api-Key` header get `401 Unauthorized`.
The health check stays open so Railway can still monitor the service.

**What this does not do:** the key is compiled into the JavaScript that every
visitor downloads, so anyone who opens DevTools can read it. It stops
automated scanners and casual poking. It is not real authentication — that
needs a login issuing a per-user token, which is well beyond this project.

If the data really matters, the safer move is to take the public domain down
after your demo (Railway → Settings → Networking → remove the domain).

---

## 9. Troubleshooting — by symptom

### Every page shows "Failed to fetch" / "Network error"

Ninety percent of the time this is **CORS** (§7). Open DevTools → Console. If
you see *"has been blocked by CORS policy"*, `ALLOWED_ORIGINS` on Railway
does not exactly match your Vercel URL. It is case-sensitive, needs
`https://`, and must have no trailing slash.

If the Console instead shows a 404 or the wrong address, `VITE_API_BASE_URL`
is wrong on Vercel. Remember Vite bakes env variables in **at build time** —
after changing it you must **redeploy**, not just reload the page.

### `502 Bad Gateway` from Railway

The container is running but nothing is listening on the port Railway
expects. Check you did **not** set a `PORT` variable yourself.

### Health check says `"database":"DOWN"`

The Java app is fine; the database link is not. Check the five `DB_*`
variables, and that they use `${{MySQL.*}}` references rather than values you
typed. Confirm the MySQL service is actually running.

### Railway build fails

Read **Deploy Logs**, not Build Logs, first. Common causes:
* `lib/mysql-connector-j-*.jar` was not committed → `git add -f lib/`
* a Java compile error → run `compile.bat` locally; it must pass first

### `Table 'railway.students' doesn't exist`

You have not run `schema-cloud.sql` in the Data tab (§4), or you ran it
against a different database.

### `Access denied` when running the SQL

You used `schema.sql` instead of `schema-cloud.sql`.

### Site works, but refreshing `/students` gives a 404

`vercel.json` was not picked up. Check that the **Root Directory** in Vercel
is set to `hostel-frontend`, then redeploy.

### Login fails online but works locally

The `admins` table is empty in the cloud database — the seed data did not
load. Re-run `schema-cloud.sql`.

### It worked, then broke a day later

Check your Railway free credit. When it runs out the services stop.

---

## 10. What was actually tested

Honesty about what I could and could not verify from your laptop:

**Verified here:**

* All 60 Java files compile after the changes
* API reads `PORT` — started on 9999 and 9998 on demand
* API reads `ALLOWED_ORIGINS` — returned the header for a Vercel-style
  origin and correctly withheld it from an unknown origin
* `API_KEY` — `401` without the header, `200` with it, health check exempt
* `DB_*` variables — ran the whole API against a throwaway `cloudtest`
  database using env vars only, with login and dashboard both working
* `schema-cloud.sql` — created all 8 tables plus seed data inside an existing
  database, and is safe to run twice
* Frontend builds clean after the `api.js` change
* Local behaviour unchanged with no env vars set

**Not verified — no way to test from here:**

* The Railway build itself (Docker is not installed on your machine, so the
  `Dockerfile` has not been executed — it is short and standard, but the
  first Railway build is where you would find a typo)
* The real Railway and Vercel dashboards, which change their wording often
* Actual `${{MySQL.*}}` variable substitution

If the first Railway build fails, send me the Deploy Log and I will fix it.

---

## 11. Checklist

- [ ] Code pushed to GitHub, without `bin/` or `node_modules/`
- [ ] Railway project created with **MySQL** added
- [ ] `schema-cloud.sql` run in the Data tab → 8 tables exist
- [ ] API service deployed from the repo in the **same** project
- [ ] Five `DB_*` variables set using `${{MySQL.*}}` references
- [ ] `PORT` **not** set manually
- [ ] Public domain generated
- [ ] `/api/health` returns `"database":"UP"` ← **do not skip**
- [ ] Vercel project created with Root Directory = `hostel-frontend`
- [ ] `VITE_API_BASE_URL` set to the Railway URL ending in `/api`
- [ ] `ALLOWED_ORIGINS` on Railway set to the Vercel URL
- [ ] Logged in online with `admin` / `admin123`

---

## 12. For the viva

Deploying this is worth talking about, and examiners like these questions.

**"Why did the port have to change?"**
A cloud host runs many programs on one machine, so it assigns each one a port
and tells it through an environment variable. Hard-coding 8080 means the host
routes traffic to a port nothing is listening on.

**"What is CORS and why did you hit it?"**
A browser will not let a page from one origin read a response from another
unless the server says it may. My frontend is on Vercel and my API is on
Railway — different origins — so the API has to name the frontend in its
`Access-Control-Allow-Origin` header.

**"Why environment variables instead of editing the file?"**
Because the password would otherwise be committed to GitHub, and because the
same build has to run on a laptop and in the cloud with different settings.
Configuration belongs outside the code.

**"Why doesn't the console app run online?"**
It is interactive — it waits on `Scanner` for keyboard input. A container has
no keyboard, so it would read end-of-file and exit. Only the HTTP server
makes sense to deploy, and it reuses the exact same service and DAO layer.

**"Is your deployment secure?"**
No, and I know why. The API endpoints have no authentication, the passwords
in the `admins` table are plain text, and the optional API key is visible in
the frontend bundle. Fixing it properly needs hashed passwords and
per-user tokens.
