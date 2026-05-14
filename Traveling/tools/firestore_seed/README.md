# Firestore seed tool

This folder contains a small script to seed the `posts` collection in Firestore using the Firebase Admin SDK.

## Install

```bash
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
```

## Run

```bash
python3 seed_firestore_posts.py --credentials /path/to/admin-sdk.json
```

Use `--dry-run` to verify without writing.

