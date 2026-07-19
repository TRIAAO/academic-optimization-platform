import { access, readFile, readdir } from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import { transformWithEsbuild } from "vite";

const PROJECT_ROOT = process.cwd();
const SOURCE_DIRECTORIES = ["src", "scripts", "tests"];
const SUPPORTED_EXTENSIONS = new Set([".js", ".jsx", ".mjs"]);
const MERGE_CONFLICT_PATTERN = /^(<<<<<<<|=======|>>>>>>>)/m;
const DEBUGGER_PATTERN = /(^|[^\w])debugger\s*;/;

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
  const extension = path.extname(filePath);
  const loader = extension === ".jsx" ? "jsx" : "js";
  const relativePath = path.relative(PROJECT_ROOT, filePath);

  if (MERGE_CONFLICT_PATTERN.test(source)) {
    throw new Error(`${relativePath} contém marcadores de conflito Git.`);
  }

  if (DEBUGGER_PATTERN.test(source)) {
    throw new Error(`${relativePath} contém uma instrução debugger.`);
  }

  await transformWithEsbuild(source, filePath, {
    loader,
    jsx: "automatic",
    sourcemap: false,
    target: "es2022"
  });
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
    `Validação estática concluída: ${files.length} arquivo(s) sem erros de sintaxe, conflitos ou debugger.`
  );
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
