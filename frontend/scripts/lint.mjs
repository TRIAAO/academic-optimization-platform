import { access, readFile, readdir } from "node:fs/promises";
import path from "node:path";
import process from "node:process";

const PROJECT_ROOT = process.cwd();
const SOURCE_DIRECTORIES = ["src", "scripts", "tests"];
const SUPPORTED_EXTENSIONS = new Set([".js", ".jsx", ".mjs"]);
const MERGE_CONFLICT_PATTERN = /^(<<<<<<<|=======|>>>>>>>)/m;

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

async function validateFile(filePath) {
  const source = await readFile(filePath, "utf8");
  const relativePath = path.relative(PROJECT_ROOT, filePath);

  if (!source.trim()) {
    throw new Error(`${relativePath} está vazio.`);
  }

  if (source.includes("\u0000")) {
    throw new Error(`${relativePath} contém bytes nulos inválidos.`);
  }

  if (MERGE_CONFLICT_PATTERN.test(source)) {
    throw new Error(`${relativePath} contém marcadores de conflito Git.`);
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
    `Lint concluído: ${files.length} arquivo(s) íntegros e sem conflitos pendentes.`
  );
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
