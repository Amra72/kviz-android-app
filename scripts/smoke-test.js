"use strict";

const http = require("http");
const { spawn } = require("child_process");

const PORT = 3399;
const API_KEY = "smoke-test-key";
const baseUrl = `http://127.0.0.1:${PORT}`;

function request(method, pathname, body) {
  return new Promise((resolve, reject) => {
    const payload = body ? JSON.stringify(body) : "";
    const req = http.request(
      `${baseUrl}${pathname}`,
      {
        method,
        headers: {
          "Content-Type": "application/json",
          "X-API-Key": API_KEY,
          "Content-Length": Buffer.byteLength(payload)
        }
      },
      res => {
        let raw = "";
        res.on("data", chunk => {
          raw += chunk;
        });
        res.on("end", () => {
          const parsed = raw ? JSON.parse(raw) : null;
          resolve({ status: res.statusCode, body: parsed });
        });
      }
    );
    req.on("error", reject);
    req.end(payload);
  });
}

async function waitForServer(child) {
  for (let attempt = 0; attempt < 30; attempt += 1) {
    try {
      await request("GET", "/");
      return;
    } catch {
      await new Promise(resolve => setTimeout(resolve, 100));
    }
  }

  child.kill();
  throw new Error("Server se nije pokrenuo na vrijeme.");
}

async function main() {
  const child = spawn(process.execPath, ["server.js", "--reset"], {
    cwd: __dirname + "/..",
    env: { ...process.env, PORT: String(PORT), API_KEY, DB_PATH: __dirname + "/../data/smoke-test.sqlite" },
    stdio: "ignore"
  });

  try {
    await waitForServer(child);

    const predmeti = await request("GET", "/predmet");
    const grupe = await request("GET", "/predmet/1/grupa");
    const kvizovi = await request("GET", "/student/demo/kviz");
    const pitanja = await request("GET", "/kviz/1/pitanja");
    const taken = await request("POST", "/student/demo/kviz/1");
    const score = await request("POST", `/student/demo/kviztaken/${taken.body.id}/odgovor`, {
      idPitanje: 2,
      odgovor: 1
    });

    const checks = [
      predmeti.status === 200 && predmeti.body.length >= 3,
      grupe.status === 200 && grupe.body.length >= 2,
      kvizovi.status === 200 && kvizovi.body.length >= 1,
      pitanja.status === 200 && pitanja.body.length >= 2,
      taken.status === 200 || taken.status === 201,
      score.status === 200 && Number.isInteger(score.body)
    ];

    if (checks.every(Boolean)) {
      console.log("Smoke test OK.");
      return;
    }

    throw new Error("Jedna ili vise provjera nije prosla.");
  } finally {
    child.kill();
  }
}

main().catch(error => {
  console.error(error.message);
  process.exitCode = 1;
});
