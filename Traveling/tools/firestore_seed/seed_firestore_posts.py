#!/usr/bin/env python3
import argparse
import os
import sys
from datetime import datetime, timezone

import firebase_admin
from firebase_admin import credentials, firestore

DEFAULT_COLLECTION = "posts"


def build_seed_posts():
    now = datetime.now(timezone.utc).isoformat()
    return [
        {
            "authorId": "seed-admin",
            "authorName": "Corentin",
            "locationName": "Paris",
            "description": "Seed post 1 generated on " + now,
            "period": "May 2026",
            "howToGetThere": "Metro",
            "likeCount": 0,
            "commentCount": 0,
            "reportCount": 0,
            "likedBy": [],
            "createdAt": firestore.SERVER_TIMESTAMP,
        },
        {
            "authorId": "seed-admin",
            "authorName": "Lina",
            "locationName": "Lyon",
            "description": "Seed post 2 generated on " + now,
            "period": "May 2026",
            "howToGetThere": "Train",
            "likeCount": 0,
            "commentCount": 0,
            "reportCount": 0,
            "likedBy": [],
            "createdAt": firestore.SERVER_TIMESTAMP,
        },
        {
            "authorId": "seed-admin",
            "authorName": "Mehdi",
            "locationName": "Marseille",
            "description": "Seed post 3 generated on " + now,
            "period": "May 2026",
            "howToGetThere": "Bus",
            "likeCount": 0,
            "commentCount": 0,
            "reportCount": 0,
            "likedBy": [],
            "createdAt": firestore.SERVER_TIMESTAMP,
        },
    ]


def init_firestore(creds_path, project_id):
    if not os.path.isfile(creds_path):
        raise FileNotFoundError("Credentials file not found: " + creds_path)
    cred = credentials.Certificate(creds_path)
    if not firebase_admin._apps:
        options = {"projectId": project_id} if project_id else None
        firebase_admin.initialize_app(cred, options)
    return firestore.client()


def write_posts(db, collection, posts, dry_run):
    if dry_run:
        print("DRY RUN: would write", len(posts), "documents into", collection)
        return
    batch = db.batch()
    for index, data in enumerate(posts, start=1):
        doc_ref = db.collection(collection).document()
        batch.set(doc_ref, data)
        if index % 450 == 0:
            batch.commit()
            batch = db.batch()
    batch.commit()
    print("Wrote", len(posts), "documents into", collection)


def parse_args(argv):
    parser = argparse.ArgumentParser(description="Seed Firestore posts collection")
    parser.add_argument("--credentials", required=True, help="Path to Firebase Admin SDK JSON")
    parser.add_argument("--project-id", default=None, help="Optional Firebase project ID")
    parser.add_argument("--collection", default=DEFAULT_COLLECTION, help="Collection name (default: posts)")
    parser.add_argument("--dry-run", action="store_true", help="Do not write, only print actions")
    return parser.parse_args(argv)


def main(argv):
    args = parse_args(argv)
    posts = build_seed_posts()
    db = init_firestore(args.credentials, args.project_id)
    write_posts(db, args.collection, posts, args.dry_run)


if __name__ == "__main__":
    main(sys.argv[1:])

