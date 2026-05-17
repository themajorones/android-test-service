import { FormEvent, useEffect, useState } from 'react';
import { deleteResource, getJson, sendJson } from './api';

type Tab = 'ollama' | 'docker' | 'android' | 'logs';

type Ollama = {
  id: number;
  name: string;
  baseUrl: string;
  enabled: boolean;
  status: string;
  model: string;
};

type Docker = {
  id: number;
  name: string;
  baseUrl: string;
  enabled: boolean;
  status: string;
  apiVersion?: string;
  os?: string;
  arch?: string;
  nvidiaRuntimeAvailable: boolean;
  gpuDevicesJson?: string;
};

type AndroidVM = {
  id: number;
  dockerId: number;
  dockerName: string;
  name: string;
  image: string;
  containerId?: string;
  containerName?: string;
  adbHost?: string;
  adbPort?: number;
  accelerationMode: string;
  status: string;
};

type TaskLog = {
  id: number;
  type: string;
  status: string;
  content: string;
  result?: string;
  startedAt?: number;
  endedAt?: number;
};

type OllamaModel = {
  name: string;
  model: string;
  size: number;
};

export function App() {
  const [tab, setTab] = useState<Tab>('ollama');
  const [ollama, setOllama] = useState<Ollama[]>([]);
  const [docker, setDocker] = useState<Docker[]>([]);
  const [android, setAndroid] = useState<AndroidVM[]>([]);
  const [logs, setLogs] = useState<TaskLog[]>([]);
  const [models, setModels] = useState<OllamaModel[]>([]);
  const [selectedVmDetail, setSelectedVmDetail] = useState('');
  const [status, setStatus] = useState('Idle');

  const [ollamaForm, setOllamaForm] = useState({
    name: 'bigscreen',
    baseUrl: 'http://bigscreen:11434',
    model: '',
  });
  const [dockerForm, setDockerForm] = useState({
    name: 'bigscreen',
    baseUrl: 'http://bigscreen:2375',
  });
  const [androidForm, setAndroidForm] = useState({
    dockerId: '',
    name: 'redroid-1',
    image: 'redroid/redroid:15.0.0_64only-latest',
    accelerationMode: 'GUEST',
  });

  async function refresh() {
    const [nextOllama, nextDocker, nextAndroid, nextLogs] = await Promise.all([
      getJson<Ollama[]>('/api/connections/ollama'),
      getJson<Docker[]>('/api/connections/docker'),
      getJson<AndroidVM[]>('/api/connections/android'),
      getJson<TaskLog[]>('/api/task-logs'),
    ]);
    setOllama(nextOllama);
    setDocker(nextDocker);
    setAndroid(nextAndroid);
    setLogs(nextLogs);
  }

  useEffect(() => {
    refresh().catch((error) => setStatus(message(error)));
    const timer = window.setInterval(() => refresh().catch((error) => setStatus(message(error))), 30_000);
    return () => window.clearInterval(timer);
  }, []);

  async function fetchOllamaModels() {
    setStatus('Fetching Ollama models');
    const nextModels = await sendJson<OllamaModel[]>('/api/connections/ollama/models', 'POST', {
      baseUrl: ollamaForm.baseUrl,
    });
    setModels(nextModels);
    setOllamaForm((current) => ({ ...current, model: current.model || nextModels[0]?.name || '' }));
    setStatus(`Loaded ${nextModels.length} models`);
  }

  async function submitOllama(event: FormEvent) {
    event.preventDefault();
    await sendJson<Ollama>('/api/connections/ollama', 'POST', ollamaForm);
    setModels([]);
    await refresh();
    setStatus('Ollama connection saved');
  }

  async function submitDocker(event: FormEvent) {
    event.preventDefault();
    await sendJson<Docker>('/api/connections/docker', 'POST', dockerForm);
    await refresh();
    setStatus('Docker connection saved');
  }

  async function submitAndroid(event: FormEvent) {
    event.preventDefault();
    await sendJson('/api/connections/android', 'POST', {
      ...androidForm,
      dockerId: Number(androidForm.dockerId || docker[0]?.id),
    });
    await refresh();
    setStatus('Android VM creation queued');
  }

  async function stopVm(id: number) {
    await sendJson(`/api/connections/android/${id}/stop`, 'POST');
    await refresh();
    setStatus('Android VM stopped');
  }

  async function deleteVm(id: number) {
    await deleteResource(`/api/connections/android/${id}`);
    await refresh();
    setStatus('Android VM deleted');
  }

  async function loadVmDetail(id: number) {
    const detail = await getJson<unknown>(`/api/connections/android/${id}`);
    setSelectedVmDetail(JSON.stringify(detail, null, 2));
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Autotest service</p>
          <h1>Connection manager</h1>
        </div>
        <div className="button-row">
          <a className="button-link" href="/oauth2/authorization/github">Sign in</a>
          <button type="button" onClick={() => refresh().then(() => setStatus('Refreshed'))}>
            Refresh
          </button>
        </div>
      </header>

      <nav className="tabs" aria-label="Connection manager sections">
        {(['ollama', 'docker', 'android', 'logs'] as Tab[]).map((item) => (
          <button
            key={item}
            className={tab === item ? 'active' : ''}
            type="button"
            onClick={() => setTab(item)}
          >
            {label(item)}
          </button>
        ))}
      </nav>

      {tab === 'ollama' && (
        <section className="panel">
          <form className="form-grid" onSubmit={(event) => submitOllama(event).catch((error) => setStatus(message(error)))}>
            <label>
              Name
              <input value={ollamaForm.name} onChange={(event) => setOllamaForm({ ...ollamaForm, name: event.target.value })} />
            </label>
            <label>
              Base URL
              <input value={ollamaForm.baseUrl} onChange={(event) => setOllamaForm({ ...ollamaForm, baseUrl: event.target.value })} />
            </label>
            <label>
              Model
              <select value={ollamaForm.model} onChange={(event) => setOllamaForm({ ...ollamaForm, model: event.target.value })}>
                <option value="">Select model</option>
                {models.map((model) => (
                  <option key={model.name} value={model.name}>
                    {model.name}
                  </option>
                ))}
              </select>
            </label>
            <div className="button-row">
              <button type="button" onClick={() => fetchOllamaModels().catch((error) => setStatus(message(error)))}>
                Fetch models
              </button>
              <button type="submit">Save Ollama</button>
            </div>
          </form>
          <DataTable
            rows={ollama}
            columns={['name', 'baseUrl', 'model', 'status', 'enabled']}
            onDelete={(row) => deleteResource(`/api/connections/ollama/${row.id}`).then(refresh)}
          />
        </section>
      )}

      {tab === 'docker' && (
        <section className="panel">
          <form className="form-grid" onSubmit={(event) => submitDocker(event).catch((error) => setStatus(message(error)))}>
            <label>
              Name
              <input value={dockerForm.name} onChange={(event) => setDockerForm({ ...dockerForm, name: event.target.value })} />
            </label>
            <label>
              Base URL
              <input value={dockerForm.baseUrl} onChange={(event) => setDockerForm({ ...dockerForm, baseUrl: event.target.value })} />
            </label>
            <div className="button-row">
              <button type="submit">Save Docker</button>
            </div>
          </form>
          <DataTable
            rows={docker}
            columns={['name', 'baseUrl', 'status', 'apiVersion', 'os', 'arch', 'nvidiaRuntimeAvailable']}
            onDelete={(row) => deleteResource(`/api/connections/docker/${row.id}`).then(refresh)}
          />
        </section>
      )}

      {tab === 'android' && (
        <section className="panel">
          <form className="form-grid" onSubmit={(event) => submitAndroid(event).catch((error) => setStatus(message(error)))}>
            <label>
              Docker
              <select value={androidForm.dockerId || docker[0]?.id || ''} onChange={(event) => setAndroidForm({ ...androidForm, dockerId: event.target.value })}>
                {docker.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Name
              <input value={androidForm.name} onChange={(event) => setAndroidForm({ ...androidForm, name: event.target.value })} />
            </label>
            <label>
              Image
              <input value={androidForm.image} onChange={(event) => setAndroidForm({ ...androidForm, image: event.target.value })} />
            </label>
            <label>
              Acceleration
              <select value={androidForm.accelerationMode} onChange={(event) => setAndroidForm({ ...androidForm, accelerationMode: event.target.value })}>
                <option value="GUEST">GUEST</option>
                <option value="HOST">HOST</option>
                <option value="AUTO">AUTO</option>
              </select>
            </label>
            <div className="button-row">
              <button type="submit">Create Android VM</button>
            </div>
          </form>
          <DataTable
            rows={android}
            columns={['name', 'dockerName', 'image', 'status', 'adbHost', 'adbPort', 'accelerationMode']}
            onDetail={(row) => loadVmDetail(row.id).catch((error) => setStatus(message(error)))}
            onStop={(row) => stopVm(row.id).catch((error) => setStatus(message(error)))}
            onDelete={(row) => deleteVm(row.id).catch((error) => setStatus(message(error)))}
          />
          {selectedVmDetail && <pre className="detail">{selectedVmDetail}</pre>}
        </section>
      )}

      {tab === 'logs' && (
        <section className="panel">
          <DataTable rows={logs} columns={['id', 'type', 'status', 'startedAt', 'endedAt', 'content', 'result']} />
        </section>
      )}

      <p className="request-status" aria-live="polite">{status}</p>
    </main>
  );
}

function DataTable<T extends { id: number }>({
  rows,
  columns,
  onDelete,
  onDetail,
  onStop,
}: {
  rows: T[];
  columns: string[];
  onDelete?: (row: T) => void;
  onDetail?: (row: T) => void;
  onStop?: (row: T) => void;
}) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((column) => <th key={column}>{column}</th>)}
            {(onDelete || onDetail || onStop) && <th>actions</th>}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>
              {columns.map((column) => <td key={column}>{cellValue(row, column)}</td>)}
              {(onDelete || onDetail || onStop) && (
                <td>
                  <div className="table-actions">
                    {onDetail && <button type="button" onClick={() => onDetail(row)}>Detail</button>}
                    {onStop && <button type="button" onClick={() => onStop(row)}>Stop</button>}
                    {onDelete && <button type="button" onClick={() => onDelete(row)}>Delete</button>}
                  </div>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function cellValue(row: Record<string, unknown>, column: string) {
  const value = row[column];
  if (value === undefined || value === null || value === '') {
    return '—';
  }
  if (typeof value === 'boolean') {
    return value ? 'yes' : 'no';
  }
  if (typeof value === 'number' && column.endsWith('At')) {
    return new Date(value).toLocaleString();
  }
  const text = String(value);
  return text.length > 140 ? `${text.slice(0, 140)}…` : text;
}

function label(tab: Tab) {
  return tab === 'android' ? 'Android VMs' : tab[0].toUpperCase() + tab.slice(1);
}

function message(error: unknown) {
  return error instanceof Error ? error.message : 'Request failed';
}
