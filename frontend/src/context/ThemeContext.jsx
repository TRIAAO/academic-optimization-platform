import { createContext, useContext, useEffect, useMemo, useState } from "react";

const ThemeContext = createContext(null);
const STORAGE_KEY = "academic-platform-theme";

function getSystemTheme() {
  if (typeof window === "undefined") {
    return "light";
  }

  return window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
}

function getInitialTheme() {
  if (typeof document !== "undefined") {
    const documentTheme = document.documentElement.dataset.theme;

    if (documentTheme === "light" || documentTheme === "dark") {
      return documentTheme;
    }
  }

  if (typeof window !== "undefined") {
    const storedTheme = window.localStorage.getItem(STORAGE_KEY);

    if (storedTheme === "light" || storedTheme === "dark") {
      return storedTheme;
    }
  }

  return getSystemTheme();
}

function applyTheme(theme) {
  const root = document.documentElement;
  const isDark = theme === "dark";

  root.classList.toggle("dark", isDark);
  root.dataset.theme = theme;
  root.style.colorScheme = theme;

  const themeColor = document.querySelector('meta[name="theme-color"]');
  themeColor?.setAttribute("content", isDark ? "#020617" : "#f8fafc");
}

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(getInitialTheme);

  useEffect(() => {
    applyTheme(theme);
    window.localStorage.setItem(STORAGE_KEY, theme);
  }, [theme]);

  const value = useMemo(
    () => ({
      theme,
      isDark: theme === "dark",
      setTheme,
      toggleTheme() {
        setTheme((currentTheme) =>
          currentTheme === "dark" ? "light" : "dark"
        );
      }
    }),
    [theme]
  );

  return (
    <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);

  if (!context) {
    throw new Error("useTheme deve ser utilizado dentro de ThemeProvider.");
  }

  return context;
}
