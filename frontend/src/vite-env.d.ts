/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base URL of the deployed backend API. Empty/undefined falls back to localhost in dev. */
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
