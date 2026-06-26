#!/usr/bin/env node
"use strict";

const fs = require("fs");
const path = require("path");

const IOS_SRC_FILE = "GoogleService-Info.plist";

function getIosDestinations(projectRoot) {
  const iosPlatformPath = path.join(projectRoot, "platforms", "ios");
  if (!fs.existsSync(iosPlatformPath)) {
    return [];
  }

  const entries = fs.readdirSync(iosPlatformPath, { withFileTypes: true });
  const xcodeProjects = new Set(
    entries
      .filter((entry) => entry.isDirectory() && entry.name.endsWith(".xcodeproj"))
      .map((entry) => entry.name.replace(/\.xcodeproj$/, ""))
  );

  const destinations = [];
  for (const entry of entries) {
    if (!entry.isDirectory()) {
      continue;
    }

    const projectName = entry.name;
    if (!xcodeProjects.has(projectName)) {
      continue;
    }

    const resourcePath = path.join(iosPlatformPath, projectName, "Resources");
    if (fs.existsSync(resourcePath)) {
      destinations.push(path.join(resourcePath, IOS_SRC_FILE));
    }
  }

  return destinations;
}

module.exports = function(context) {
  const platforms = (context && context.opts && context.opts.platforms) || [];
  if (!platforms.includes("ios")) {
    return;
  }

  const projectRoot = (context && context.opts && context.opts.projectRoot) || process.cwd();
  const sourcePath = path.join(projectRoot, IOS_SRC_FILE);
  if (!fs.existsSync(sourcePath)) {
    console.log(
      "[firebase-ml-kit-barcode-scanner] " +
        "No GoogleService-Info.plist in project root; skipping iOS Firebase credentials copy."
    );
    return;
  }

  const destinations = getIosDestinations(projectRoot);
  if (destinations.length === 0) {
    console.log(
      "[firebase-ml-kit-barcode-scanner] " +
        "iOS platform detected but no matching Resources folder found yet; skipping copy."
    );
    return;
  }

  const plist = fs.readFileSync(sourcePath);
  for (const destination of destinations) {
    fs.writeFileSync(destination, plist);
    console.log("[firebase-ml-kit-barcode-scanner] Copied GoogleService-Info.plist to " + destination);
  }
};
