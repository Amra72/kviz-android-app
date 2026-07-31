"use strict";

const express = require("express");
const fs = require("fs");
const path = require("path");
const { DatabaseSync } = require("node:sqlite");

const app = express();
const port = Number(process.env.PORT || 3000);
const host = process.env.HOST || "127.0.0.1";
const apiKey = process.env.API_KEY || "";
const allowReset = process.env.ALLOW_RESET === "true";
const dataDir = path.join(__dirname, "data");
const dbPath = process.env.DB_PATH || path.join(dataDir, "rma26.sqlite");

const rateLimitWindowMs = 60_000;
const rateLimitMax = Number(process.env.RATE_LIMIT_MAX || 120);
const rateLimitBuckets = new Map();

const seed = {
  predmeti: [
    { id: 1, naziv: "Razvoj mobilnih aplikacija", godina: 3 },
    { id: 2, naziv: "Web tehnologije", godina: 2 },
    { id: 3, naziv: "Baze podataka", godina: 2 }
  ],
  grupe: [
    { id: 1, naziv: "RMA Grupa 1", idPredmeta: 1 },
    { id: 2, naziv: "RMA Grupa 2", idPredmeta: 1 },
    { id: 3, naziv: "WT Grupa 1", idPredmeta: 2 },
    { id: 4, naziv: "BP Grupa 1", idPredmeta: 3 }
  ],
  kvizovi: [
    { id: 1, naziv: "Kviz 1", idPredmeta: 1, idGrupe: 1, datumPocetka: "2021-05-10T08:00:00.000Z", datumKraj: "2021-06-10T23:59:00.000Z", trajanje: 20 },
    { id: 2, naziv: "Kviz 2", idPredmeta: 1, idGrupe: 2, datumPocetka: "2021-05-20T08:00:00.000Z", datumKraj: "2021-06-20T23:59:00.000Z", trajanje: 30 },
    { id: 3, naziv: "HTTP i REST", idPredmeta: 2, idGrupe: 3, datumPocetka: "2021-05-12T08:00:00.000Z", datumKraj: "2021-06-12T23:59:00.000Z", trajanje: 15 },
    { id: 4, naziv: "SQL osnove", idPredmeta: 3, idGrupe: 4, datumPocetka: "2021-05-15T08:00:00.000Z", datumKraj: "2021-06-15T23:59:00.000Z", trajanje: 25 }
  ],
  pitanja: [
    {
      id: 1,
      idKviza: 1,
      naziv: "Android activity",
      tekstPitanja: "Koja metoda se najcesce koristi za inicijalizaciju pocetne aktivnosti?",
      opcije: ["onStart", "onCreate", "onPause", "onDestroy"],
      tacan: 1
    },
    {
      id: 2,
      idKviza: 1,
      naziv: "Coroutine dispatcher",
      tekstPitanja: "Koji dispatcher je prikladan za mrezne zahtjeve?",
      opcije: ["Dispatchers.Main", "Dispatchers.IO", "Dispatchers.Default", "GlobalScope"],
      tacan: 1
    },
    {
      id: 3,
      idKviza: 2,
      naziv: "Repository pattern",
      tekstPitanja: "Koji sloj obicno sakriva izvor podataka od ViewModel-a?",
      opcije: ["Repository", "Activity", "Layout", "Adapter"],
      tacan: 0
    },
    {
      id: 4,
      idKviza: 2,
      naziv: "MVVM",
      tekstPitanja: "Koja komponenta priprema podatke za prikaz?",
      opcije: ["ViewModel", "Manifest", "Gradle", "Drawable"],
      tacan: 0
    },
    {
      id: 5,
      idKviza: 3,
      naziv: "HTTP GET",
      tekstPitanja: "Koja HTTP metoda se tipicno koristi za citanje resursa?",
      opcije: ["POST", "GET", "PATCH", "DELETE"],
      tacan: 1
    },
    {
      id: 6,
      idKviza: 4,
      naziv: "Primary key",
      tekstPitanja: "Sta primarni kljuc treba obezbijediti u tabeli?",
      opcije: ["Jedinstvenost reda", "Sortiranje uvijek opadajuce", "Brisanje duplikata automatski", "Samo tekstualne vrijednosti"],
      tacan: 0
    }
  ],
  upisi: [
    { hash: "demo", idGrupe: 1 },
    { hash: "demo", idGrupe: 3 }
  ],
  kvizTaken: [
    { id: 1, hash: "demo", idKviza: 1, datumRada: "2021-05-21T12:00:00.000Z", zavrsen: false },
    { id: 2, hash: "demo", idKviza: 3, datumRada: "2021-05-21T12:30:00.000Z", zavrsen: false }
  ],
  odgovori: [
    { id: 1, idKvizTaken: 1, idPitanje: 1, odgovor: 1 },
    { id: 2, idKvizTaken: 1, idPitanje: 2, odgovor: 0 },
    { id: 3, idKvizTaken: 2, idPitanje: 5, odgovor: 1 }
  ]
};

fs.mkdirSync(dataDir, { recursive: true });

const db = new DatabaseSync(dbPath);
db.exec("PRAGMA journal_mode = WAL;");
db.exec("PRAGMA foreign_keys = ON;");

createSchema();
if (process.argv.includes("--reset") || isDatabaseEmpty()) {
  resetDatabase();
}

app.disable("x-powered-by");
app.set("trust proxy", true);
app.use(express.json({ limit: "20kb" }));
app.use(securityHeaders);
app.use(rateLimit);
app.use(requireApiKeyForWrites);

const router = express.Router();

router.get(["/", "/docs", "/api-docs"], (req, res) => {
  res.send({
    name: "RMA26 local backend",
    storage: "SQLite",
    database: dbPath,
    demoHash: "demo",
    note: "Ako je API_KEY postavljen, POST/PUT zahtjevi traze X-API-Key ili Authorization: Bearer.",
    repositories: {
      PitanjeKvizRepository: ["GET /kviz/:id/pitanja", "GET /pitanje/kviz/:id"],
      TakeKvizRepository: ["POST /student/:hash/kviz/:id", "GET /student/:hash/kviztaken"],
      OdgovorRepository: [
        "GET /student/:hash/kviz/:id/odgovori",
        "POST /student/:hash/kviztaken/:id/odgovor"
      ],
      KvizRepository: ["GET /kviz", "GET /kviz/:id", "GET /student/:hash/kviz"],
      PredmetIGrupaRepository: [
        "GET /predmet",
        "GET /grupa",
        "GET /predmet/:id/grupa",
        "POST /student/:hash/grupa/:id",
        "GET /student/:hash/grupa"
      ]
    }
  });
});

router.post("/reset", (req, res) => {
  if (!allowReset) return res.status(404).send({ error: "Ruta nije pronadjena." });
  resetDatabase();
  res.send({ ok: true });
});

router.get(["/predmet", "/predmeti"], (req, res) => {
  res.send(db.prepare("SELECT id, naziv, godina FROM predmeti ORDER BY id").all().map(predmet));
});

router.get(["/grupa", "/grupe"], (req, res) => {
  res.send(db.prepare("SELECT id, naziv, id_predmeta FROM grupe ORDER BY id").all().map(grupa));
});

router.get(["/predmet/:id/grupa", "/predmeti/:id/grupe"], (req, res) => {
  const idPredmeta = requiredId(req.params.id, "idPredmeta");
  const grupe = db
    .prepare("SELECT id, naziv, id_predmeta FROM grupe WHERE id_predmeta = ? ORDER BY id")
    .all(idPredmeta)
    .map(grupa);

  res.send(grupe);
});

router.get(["/kviz", "/kvizovi"], (req, res) => {
  res.send(getKvizovi());
});

router.get(["/kviz/:id", "/kvizovi/:id"], (req, res) => {
  const kviz = getKvizovi("WHERE k.id = ?", [requiredId(req.params.id, "id")])[0] || null;
  res.status(kviz ? 200 : 404).json(kviz);
});

router.get(["/kviz/:id/pitanja", "/kvizovi/:id/pitanja", "/pitanje/kviz/:id", "/pitanja/kviz/:id"], (req, res) => {
  res.send(getPitanja(requiredId(req.params.id, "idKviza")));
});

router.get(["/student/:hash/grupa", "/student/:hash/grupe", "/student/:hash/upisaneGrupe"], (req, res) => {
  res.send(getUpisaneGrupe(req.params.hash));
});

router.post(["/student/:hash/grupa/:id", "/student/:hash/grupe/:id"], (req, res) => {
  const idGrupe = requiredId(req.params.id, "idGrupe");
  const postoji = db.prepare("SELECT id FROM grupe WHERE id = ?").get(idGrupe);

  if (!postoji) return res.json(false);

  db.prepare("INSERT OR IGNORE INTO upisi (hash, id_grupe) VALUES (?, ?)").run(req.params.hash, idGrupe);
  res.json(true);
});

router.get(["/student/:hash/kviz", "/student/:hash/kvizovi", "/student/:hash/upisaniKvizovi"], (req, res) => {
  res.send(getKvizovi("WHERE k.id_grupe IN (SELECT id_grupe FROM upisi WHERE hash = ?)", [req.params.hash]));
});

router.post(["/student/:hash/kviz/:id", "/student/:hash/kvizovi/:id"], (req, res) => {
  const idKviza = requiredId(req.params.id, "idKviza");
  const postoji = db.prepare("SELECT id FROM kvizovi WHERE id = ?").get(idKviza);

  if (!postoji) return res.status(404).json(null);

  db.prepare(`
    INSERT OR IGNORE INTO kviz_taken (hash, id_kviza, datum_rada, zavrsen)
    VALUES (?, ?, ?, 0)
  `).run(req.params.hash, idKviza, new Date().toISOString());

  const taken = db
    .prepare("SELECT id, hash, id_kviza, datum_rada, zavrsen FROM kviz_taken WHERE hash = ? AND id_kviza = ?")
    .get(req.params.hash, idKviza);

  res.send(kvizTaken(taken));
});

router.get(["/student/:hash/kviztaken", "/student/:hash/pocetiKvizovi"], (req, res) => {
  const pokusaji = db
    .prepare("SELECT id, hash, id_kviza, datum_rada, zavrsen FROM kviz_taken WHERE hash = ? ORDER BY id")
    .all(req.params.hash)
    .map(kvizTaken);

  res.send(pokusaji.length ? pokusaji : null);
});

router.get(["/student/:hash/kviz/:id/odgovori", "/student/:hash/kvizovi/:id/odgovori"], (req, res) => {
  const idKviza = requiredId(req.params.id, "idKviza");
  const odgovori = db.prepare(`
    SELECT o.id, o.id_kviz_taken, o.id_pitanje, o.odgovor
    FROM odgovori o
    JOIN kviz_taken kt ON kt.id = o.id_kviz_taken
    WHERE kt.hash = ? AND kt.id_kviza = ?
    ORDER BY o.id
  `).all(req.params.hash, idKviza).map(odgovor);

  res.send(odgovori);
});

router.post(["/student/:hash/kviztaken/:id/odgovor", "/student/:hash/pocetiKvizovi/:id/odgovor"], (req, res) => {
  res.json(postaviOdgovor(req.params.hash, req.params.id, req.body));
});

router.put(["/student/:hash/kviztaken/:id/odgovor", "/student/:hash/pocetiKvizovi/:id/odgovor"], (req, res) => {
  res.json(postaviOdgovor(req.params.hash, req.params.id, req.body));
});

app.use(router);
app.use("/api", router);

app.use((req, res) => {
  res.status(404).send({ error: "Ruta nije pronadjena." });
});

app.use((error, req, res, next) => {
  const status = error.status || 400;
  res.status(status).send({ message: error.message || "Greska u zahtjevu" });
});

app.listen(port, host, () => {
  console.log(`RMA26 backend radi na http://${host}:${port}`);
  console.log(`SQLite baza: ${dbPath}`);
  console.log("Demo hash: demo");
  console.log(apiKey ? "API_KEY zastita je ukljucena za POST/PUT zahtjeve." : "API_KEY nije postavljen.");
});

function securityHeaders(req, res, next) {
  res.setHeader("Access-Control-Allow-Origin", process.env.CORS_ORIGIN || "*");
  res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization,X-API-Key");
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("Referrer-Policy", "no-referrer");
  res.setHeader("Cache-Control", "no-store");

  if (req.method === "OPTIONS") return res.sendStatus(204);
  next();
}

function rateLimit(req, res, next) {
  const now = Date.now();
  const key = req.ip || req.socket.remoteAddress || "unknown";
  const bucket = rateLimitBuckets.get(key);

  if (!bucket || bucket.expiresAt <= now) {
    rateLimitBuckets.set(key, { count: 1, expiresAt: now + rateLimitWindowMs });
    return next();
  }

  bucket.count += 1;
  if (bucket.count > rateLimitMax) {
    return res.status(429).send({ error: "Previse zahtjeva. Pokusajte ponovo za minutu." });
  }

  next();
}

function requireApiKeyForWrites(req, res, next) {
  if (!apiKey || !["POST", "PUT", "PATCH", "DELETE"].includes(req.method)) return next();

  const headerKey = req.get("X-API-Key");
  const bearer = req.get("Authorization");
  if (headerKey === apiKey || bearer === `Bearer ${apiKey}`) return next();

  res.status(401).send({ error: "Nedostaje ili nije ispravan API kljuc." });
}

function requiredId(value, name) {
  const id = Number(value);
  if (!Number.isInteger(id) || id <= 0) {
    const error = new Error(`${name} mora biti pozitivan cijeli broj.`);
    error.status = 400;
    throw error;
  }
  return id;
}

function createSchema() {
  db.exec(`
    CREATE TABLE IF NOT EXISTS predmeti (
      id INTEGER PRIMARY KEY,
      naziv TEXT NOT NULL,
      godina INTEGER NOT NULL
    );

    CREATE TABLE IF NOT EXISTS grupe (
      id INTEGER PRIMARY KEY,
      naziv TEXT NOT NULL,
      id_predmeta INTEGER NOT NULL,
      FOREIGN KEY (id_predmeta) REFERENCES predmeti(id)
    );

    CREATE TABLE IF NOT EXISTS kvizovi (
      id INTEGER PRIMARY KEY,
      naziv TEXT NOT NULL,
      id_predmeta INTEGER NOT NULL,
      id_grupe INTEGER NOT NULL,
      datum_pocetka TEXT NOT NULL,
      datum_kraj TEXT NOT NULL,
      trajanje INTEGER NOT NULL,
      FOREIGN KEY (id_predmeta) REFERENCES predmeti(id),
      FOREIGN KEY (id_grupe) REFERENCES grupe(id)
    );

    CREATE TABLE IF NOT EXISTS pitanja (
      id INTEGER PRIMARY KEY,
      id_kviza INTEGER NOT NULL,
      naziv TEXT NOT NULL,
      tekst_pitanja TEXT NOT NULL,
      opcije TEXT NOT NULL,
      tacan INTEGER NOT NULL,
      FOREIGN KEY (id_kviza) REFERENCES kvizovi(id)
    );

    CREATE TABLE IF NOT EXISTS upisi (
      hash TEXT NOT NULL,
      id_grupe INTEGER NOT NULL,
      PRIMARY KEY (hash, id_grupe),
      FOREIGN KEY (id_grupe) REFERENCES grupe(id)
    );

    CREATE TABLE IF NOT EXISTS kviz_taken (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      hash TEXT NOT NULL,
      id_kviza INTEGER NOT NULL,
      datum_rada TEXT NOT NULL,
      zavrsen INTEGER NOT NULL DEFAULT 0,
      UNIQUE (hash, id_kviza),
      FOREIGN KEY (id_kviza) REFERENCES kvizovi(id)
    );

    CREATE TABLE IF NOT EXISTS odgovori (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      id_kviz_taken INTEGER NOT NULL,
      id_pitanje INTEGER NOT NULL,
      odgovor INTEGER NOT NULL,
      UNIQUE (id_kviz_taken, id_pitanje),
      FOREIGN KEY (id_kviz_taken) REFERENCES kviz_taken(id) ON DELETE CASCADE,
      FOREIGN KEY (id_pitanje) REFERENCES pitanja(id)
    );
  `);
}

function isDatabaseEmpty() {
  return db.prepare("SELECT COUNT(*) AS total FROM predmeti").get().total === 0;
}

function resetDatabase() {
  db.exec("BEGIN IMMEDIATE;");
  try {
    db.exec(`
      DELETE FROM odgovori;
      DELETE FROM kviz_taken;
      DELETE FROM upisi;
      DELETE FROM pitanja;
      DELETE FROM kvizovi;
      DELETE FROM grupe;
      DELETE FROM predmeti;
      DELETE FROM sqlite_sequence WHERE name IN ('kviz_taken', 'odgovori');
    `);

    for (const p of seed.predmeti) {
      db.prepare("INSERT INTO predmeti (id, naziv, godina) VALUES (?, ?, ?)").run(p.id, p.naziv, p.godina);
    }

    for (const g of seed.grupe) {
      db.prepare("INSERT INTO grupe (id, naziv, id_predmeta) VALUES (?, ?, ?)").run(g.id, g.naziv, g.idPredmeta);
    }

    for (const k of seed.kvizovi) {
      db.prepare(`
        INSERT INTO kvizovi (id, naziv, id_predmeta, id_grupe, datum_pocetka, datum_kraj, trajanje)
        VALUES (?, ?, ?, ?, ?, ?, ?)
      `).run(k.id, k.naziv, k.idPredmeta, k.idGrupe, k.datumPocetka, k.datumKraj, k.trajanje);
    }

    for (const p of seed.pitanja) {
      db.prepare(`
        INSERT INTO pitanja (id, id_kviza, naziv, tekst_pitanja, opcije, tacan)
        VALUES (?, ?, ?, ?, ?, ?)
      `).run(p.id, p.idKviza, p.naziv, p.tekstPitanja, JSON.stringify(p.opcije), p.tacan);
    }

    for (const u of seed.upisi) {
      db.prepare("INSERT INTO upisi (hash, id_grupe) VALUES (?, ?)").run(u.hash, u.idGrupe);
    }

    for (const kt of seed.kvizTaken) {
      db.prepare(`
        INSERT INTO kviz_taken (id, hash, id_kviza, datum_rada, zavrsen)
        VALUES (?, ?, ?, ?, ?)
      `).run(kt.id, kt.hash, kt.idKviza, kt.datumRada, kt.zavrsen ? 1 : 0);
    }

    for (const o of seed.odgovori) {
      db.prepare("INSERT INTO odgovori (id, id_kviz_taken, id_pitanje, odgovor) VALUES (?, ?, ?, ?)")
        .run(o.id, o.idKvizTaken, o.idPitanje, o.odgovor);
    }

    db.exec("COMMIT;");
  } catch (error) {
    db.exec("ROLLBACK;");
    throw error;
  }
}

function getKvizovi(where = "", params = []) {
  return db.prepare(`
    SELECT
      k.id,
      k.naziv,
      k.id_predmeta,
      k.id_grupe,
      k.datum_pocetka,
      k.datum_kraj,
      k.trajanje,
      p.naziv AS naziv_predmeta,
      g.naziv AS naziv_grupe
    FROM kvizovi k
    JOIN predmeti p ON p.id = k.id_predmeta
    JOIN grupe g ON g.id = k.id_grupe
    ${where}
    ORDER BY k.id
  `).all(...params).map(kviz);
}

function getPitanja(idKviza) {
  return db
    .prepare("SELECT id, id_kviza, naziv, tekst_pitanja, opcije, tacan FROM pitanja WHERE id_kviza = ? ORDER BY id")
    .all(idKviza)
    .map(pitanje);
}

function getUpisaneGrupe(hash) {
  return db.prepare(`
    SELECT g.id, g.naziv, g.id_predmeta
    FROM grupe g
    JOIN upisi u ON u.id_grupe = g.id
    WHERE u.hash = ?
    ORDER BY g.id
  `).all(hash).map(grupa);
}

function postaviOdgovor(hash, idKvizTakenValue, body) {
  const idKvizTaken = requiredId(idKvizTakenValue, "idKvizTaken");
  const idPitanje = requiredId(body.idPitanje || body.pitanjeId, "idPitanje");
  const odgovorIndex = Number(body.odgovor);

  const row = db.prepare(`
    SELECT kt.id AS id_kviz_taken, p.id AS id_pitanje, p.opcije
    FROM kviz_taken kt
    JOIN pitanja p ON p.id = ?
    WHERE kt.id = ? AND kt.hash = ? AND p.id_kviza = kt.id_kviza
  `).get(idPitanje, idKvizTaken, hash);

  if (!row) return -1;

  const opcije = JSON.parse(row.opcije);
  if (!Number.isInteger(odgovorIndex) || odgovorIndex < 0 || odgovorIndex >= opcije.length) return -1;

  db.prepare(`
    INSERT INTO odgovori (id_kviz_taken, id_pitanje, odgovor)
    VALUES (?, ?, ?)
    ON CONFLICT(id_kviz_taken, id_pitanje) DO UPDATE SET odgovor = excluded.odgovor
  `).run(idKvizTaken, idPitanje, odgovorIndex);

  return calculatePoints(idKvizTaken);
}

function calculatePoints(idKvizTaken) {
  return db.prepare(`
    SELECT COUNT(*) AS total
    FROM odgovori o
    JOIN pitanja p ON p.id = o.id_pitanje
    WHERE o.id_kviz_taken = ? AND o.odgovor = p.tacan
  `).get(idKvizTaken).total;
}

function predmet(row) {
  return { id: row.id, naziv: row.naziv, godina: row.godina };
}

function grupa(row) {
  return { id: row.id, naziv: row.naziv, idPredmeta: row.id_predmeta };
}

function kviz(row) {
  return {
    id: row.id,
    naziv: row.naziv,
    idPredmeta: row.id_predmeta,
    idGrupe: row.id_grupe,
    datumPocetka: row.datum_pocetka,
    datumKraj: row.datum_kraj,
    trajanje: row.trajanje,
    nazivPredmeta: row.naziv_predmeta || null,
    nazivGrupe: row.naziv_grupe || null
  };
}

function pitanje(row) {
  return {
    id: row.id,
    idKviza: row.id_kviza,
    naziv: row.naziv,
    tekstPitanja: row.tekst_pitanja,
    opcije: JSON.parse(row.opcije),
    tacan: row.tacan
  };
}

function kvizTaken(row) {
  return {
    id: row.id,
    hash: row.hash,
    idKviza: row.id_kviza,
    datumRada: row.datum_rada,
    zavrsen: Boolean(row.zavrsen),
    osvojenBodovi: calculatePoints(row.id)
  };
}

function odgovor(row) {
  return {
    id: row.id,
    idKvizTaken: row.id_kviz_taken,
    idPitanje: row.id_pitanje,
    odgovor: row.odgovor
  };
}
