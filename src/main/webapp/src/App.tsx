import { useState } from 'react';
import { getText } from './api';

type LoadState = 'idle' | 'loading' | 'success' | 'error';

export function App() {
  const [health, setHealth] = useState('Not checked');
  const [profile, setProfile] = useState<string>('Not loaded');
  const [status, setStatus] = useState<LoadState>('idle');

  async function checkHealth() {
    setStatus('loading');
    try {
      setHealth(await getText('/health'));
      setStatus('success');
    } catch (error) {
      setHealth(error instanceof Error ? error.message : 'Request failed');
      setStatus('error');
    }
  }

  async function loadProfile() {
    setStatus('loading');
    try {
      const response = await fetch('/auth/info', {
        headers: {
          Accept: 'application/json, text/plain',
        },
      });

      if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}`);
      }

      const text = await response.text();
      try {
        setProfile(JSON.stringify(JSON.parse(text), null, 2));
      } catch {
        setProfile(text);
      }
      setStatus('success');
    } catch (error) {
      setProfile(error instanceof Error ? error.message : 'Request failed');
      setStatus('error');
    }
  }

  return (
    <main className="app-shell">
      <section className="intro">
        <p className="eyebrow">Autotest service</p>
        <h1>React frontend starter</h1>
        <p>
          This app is intentionally small: React is wired in, the backend is called through REST
          endpoints, and GitHub OAuth stays on the Spring Boot side.
        </p>
      </section>

      <section className="actions" aria-label="Backend actions">
        <button type="button" onClick={checkHealth}>
          Check API health
        </button>
        <button type="button" onClick={loadProfile}>
          Load current user
        </button>
        <a className="button-link" href="/oauth2/authorization/github">
          Sign in with GitHub
        </a>
      </section>

      <section className="results" aria-live="polite">
        <article>
          <h2>Health</h2>
          <p>{health}</p>
        </article>
        <article>
          <h2>User</h2>
          <pre>{profile}</pre>
        </article>
      </section>

      <p className={`request-status request-status-${status}`}>Request status: {status}</p>
    </main>
  );
}
