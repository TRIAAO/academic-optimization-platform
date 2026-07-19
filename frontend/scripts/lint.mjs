import { access, readFile, readdir } from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import { transformWithEsbuild } from "vite";

const PROJECT_ROOT = process.cwd();
const SOURCE_DIRECTORIES = ["src", "scripts", "tests"];
const SUPPORTED_EXTENSIONS = new Set([".js", ".jsx", ".mjs"]);
const RELATIVE_IMPORT_PATTERN =
  /(?:import|export)\s+(?:[\s\S]*?\s+from\s+)?["'](\.[^"']+)["']/g;
const SIDE_EFFECT_IMPORT_PATTERN = /import\s*["'](\.[^"']+)["']/g;

async function pathExists(filePath) {
  try {
    await access(filePath);
    return true;
  } catch {
    return false;
  }
}

async function collectFiles(directoryPath) {
  if (!(await pathExists(directoryPath))) {
    return [];
  }

  const entries = await readdir(directoryPath, { withFileTypes: true });
  const nestedFiles = await Promise.all(
    entries
      .filter((entry) => !entry.name.startsWith("."))
      .map(async (entry) => {
        const entryPath = path.join(directoryPath, entry.name);

        if (entry.isDirectory()) {
          return collectFiles(entryPath);
        }

        return SUPPORTED_EXTENSIONS.has(path.extname(entry.name))
          ? [entryPath]
          : [];
      })
  );

  return nestedFiles.flat().sort();
}

function extractRelativeImports(source) {
  const imports = new Set();

  for (const pattern of [RELATIVE_IMPORT_PATTERN, SIDE_EFFECT_IMPORT_PATTERN]) {
    pattern.lastIndex = 0;
    let match;

    while ((match = pattern.exec(source)) !== null) {
      imports.add(match[1]);
    }
  }

  return [...imports];
}

async function resolveRelativeImport(importerPath, importPath) {
  const absoluteBase = path.resolve(path.dirname(importerPath), importPath);
  const extension = path.extname(absoluteBase);
  const candidates = extension
    ? [absoluteBase]
    : [
        absoluteBase,
        `${absoluteBase}.js`,
        `${absoluteBase}.jsx`,
        `${absoluteBase}.mjs`,
        `${absoluteBase}.json`,
        `${absoluteBase}.css`,
        path.join(absoluteBase, "index.js"),
        path.join(absoluteBase, "index.jsx")
      ];

  for (const candidate of candidates) {
    if (await pathExists(candidate)) {
      return candidate;
    }
  }

  return null;
}

async function validateFile(filePath) {
  const source = await readFile(filePath, "utf8");
  const extension = path.extname(filePath);
  const loader = extension === ".jsx" ? "jsx" : "js";

  await transformWithEsbuild(source, filePath, {
    loader,
    jsx: "automatic",
    sourcemap: false,
    target: "es2022"
  });

  const unresolvedImports = [];
  for (const importPath of extractRelativeImports(source)) {
    const resolved = await resolveRelativeImport(filePath, importPath);
    if (!resolved) {
      unresolvedImports.push(importPath);
    }
  }

  if (unresolvedImports.length > 0) {
    throw new Error(
      `${path.relative(PROJECT_ROOT, filePath)} possui importações não resolvidas: ${unresolvedImports.join(
        ", "
      )}`
    );
  }
}

async function main() {
  const fileGroups = await Promise.all(
    SOURCE_DIRECTORIES.map((directory) =>
      collectFiles(path.join(PROJECT_ROOT, directory))
    )
  );
  const files = fileGroups.flat();

  if (files.length === 0) {
    throw new Error("Nenhum arquivo JavaScript/JSX foi encontrado para validação.");
  }

  const failures = [];

  for (const filePath of files) {
    try {
      await validateFile(filePath);
    } catch (error) {
      failures.push(error instanceof Error ? error.message : String(error));
    }
  }

  if (failures.length > 0) {
    console.error("Falha na validação estática do frontend:\n");
    failures.forEach((failure) => console.error(`- ${failure}`));
    process.exitCode = 1;
    return;
  }

  console.log(
    `Validação estática concluída: ${files.length} arquivo(s) com sintaxe e importações válidas.`
  );
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
