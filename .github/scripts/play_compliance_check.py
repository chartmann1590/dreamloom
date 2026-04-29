#!/usr/bin/env python3
"""Basic Google Play compliance guardrails for Dreamloom."""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BUILD_FILE = ROOT / "app" / "build.gradle.kts"
MANIFEST_FILE = ROOT / "app" / "src" / "main" / "AndroidManifest.xml"

ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
MIN_TARGET_SDK = 34
MIN_COMPILE_SDK = 34

# Sensitive/restricted permissions that should not silently slip in.
RESTRICTED_PERMISSIONS = {
    "android.permission.READ_SMS",
    "android.permission.RECEIVE_SMS",
    "android.permission.SEND_SMS",
    "android.permission.READ_CALL_LOG",
    "android.permission.WRITE_CALL_LOG",
    "android.permission.PROCESS_OUTGOING_CALLS",
    "android.permission.QUERY_ALL_PACKAGES",
}


def extract_int(pattern: str, text: str, label: str) -> int:
    match = re.search(pattern, text)
    if not match:
        raise ValueError(f"Unable to read {label} from {BUILD_FILE}")
    return int(match.group(1))


def main() -> int:
    errors: list[str] = []
    warnings: list[str] = []

    build_text = BUILD_FILE.read_text(encoding="utf-8")
    compile_sdk = extract_int(r"compileSdk\s*=\s*(\d+)", build_text, "compileSdk")
    target_sdk = extract_int(r"targetSdk\s*=\s*(\d+)", build_text, "targetSdk")

    if compile_sdk < MIN_COMPILE_SDK:
        errors.append(
            f"compileSdk is {compile_sdk}, but Google Play guardrail requires >= {MIN_COMPILE_SDK}."
        )
    if target_sdk < MIN_TARGET_SDK:
        errors.append(
            f"targetSdk is {target_sdk}, but Google Play guardrail requires >= {MIN_TARGET_SDK}."
        )

    tree = ET.parse(MANIFEST_FILE)
    manifest = tree.getroot()

    declared_permissions = {
        node.attrib.get(f"{ANDROID_NS}name", "")
        for node in manifest.findall("uses-permission")
    }
    forbidden = sorted(
        permission for permission in declared_permissions if permission in RESTRICTED_PERMISSIONS
    )
    if forbidden:
        errors.append(
            "Restricted Play-sensitive permissions detected: " + ", ".join(forbidden)
        )

    application = manifest.find("application")
    if application is None:
        errors.append("Manifest is missing an <application> node.")
    else:
        allow_backup = application.attrib.get(f"{ANDROID_NS}allowBackup")
        if allow_backup == "true":
            warnings.append(
                "android:allowBackup is true. Consider disabling backup for stronger privacy posture."
            )

        for activity in application.findall("activity"):
            has_intent_filter = activity.find("intent-filter") is not None
            exported = activity.attrib.get(f"{ANDROID_NS}exported")
            if has_intent_filter and exported is None:
                activity_name = activity.attrib.get(f"{ANDROID_NS}name", "<unnamed>")
                errors.append(
                    f"Activity {activity_name} has an intent-filter but no android:exported value."
                )

    print("Play compliance check completed.")
    print(f"- compileSdk: {compile_sdk}")
    print(f"- targetSdk: {target_sdk}")

    for warning in warnings:
        print(f"WARNING: {warning}")
    for error in errors:
        print(f"ERROR: {error}")

    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
