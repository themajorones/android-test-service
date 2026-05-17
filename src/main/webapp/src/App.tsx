import { FormEvent, useEffect, useState } from 'react';
import { deleteResource, getJson, sendJson } from './api';

type Tab = 'ollama' | 'docker' | 'android' | 'logs';
type HealthTab = Exclude<Tab, 'logs'>;

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
  const [lastHealthChecks, setLastHealthChecks] = useState<Record<HealthTab, Record<number, number>>>({
    ollama: {},
    docker: {},
    android: {},
  });
  const [selectedVmDetail, setSelectedVmDetail] = useState('');
  const [status, setStatus] = useState('Idle');

  const [editingOllamaId, setEditingOllamaId] = useState<number | null>(null);
  const [editingDockerId, setEditingDockerId] = useState<number | null>(null);
  const [editingAndroidId, setEditingAndroidId] = useState<number | null>(null);

  const [ollamaForm, setOllamaForm] = useState({
    name: 'bigscreen',
    baseUrl: 'http://bigscreen:11434',
    model: '',
    enabled: true,
  });
  const [dockerForm, setDockerForm] = useState({
    name: 'bigscreen',
    baseUrl: 'http://bigscreen:2375',
    enabled: true,
  });
  const [androidForm, setAndroidForm] = useState({
    dockerId: '',
    name: 'redroid-1',
    image: 'redroid/redroid:15.0.0_64only-latest',
    accelerationMode: 'GUEST',
  });

  useEffect(() => {
    loadAllLists().catch((error) => setStatus(message(error)));
  }, []);

  useEffect(() => {
    loadTab(tab).catch((error) => setStatus(message(error)));
    const timer = window.setInterval(() => {
      refreshTabHealth(tab)
        .then(() => loadTab(tab))
        .catch((error) => setStatus(message(error)));
    }, 30_000);
    return () => window.clearInterval(timer);
  }, [tab]);

  async function loadAllLists() {
    await Promise.allSettled([loadOllama(), loadDocker(), loadAndroid(), loadLogs()]);
  }

  async function loadTab(nextTab: Tab) {
    if (nextTab === 'ollama') {
      await loadOllama();
    } else if (nextTab === 'docker') {
      await loadDocker();
    } else if (nextTab === 'android') {
      await Promise.all([loadDocker(), loadAndroid()]);
    } else {
      await loadLogs();
    }
  }

  async function refreshTabHealth(nextTab: Tab) {
    if (nextTab === 'logs') {
      return;
    }
    await sendJson(`/api/connections/${nextTab}/health`, 'POST');
    markHealthChecked(nextTab);
  }

  async function loadOllama() {
    setOllama(await getJson<Ollama[]>('/api/connections/ollama'));
  }

  async function loadDocker() {
    setDocker(await getJson<Docker[]>('/api/connections/docker'));
  }

  async function loadAndroid() {
    setAndroid(await getJson<AndroidVM[]>('/api/connections/android'));
  }

  async function loadLogs() {
    setLogs(await getJson<TaskLog[]>('/api/task-logs'));
  }

  function markHealthChecked(nextTab: HealthTab) {
    const rows = nextTab === 'ollama' ? ollama : nextTab === 'docker' ? docker : android;
    const checkedAt = Date.now();
    setLastHealthChecks((current) => ({
      ...current,
      [nextTab]: rows.reduce<Record<number, number>>((checks, row) => {
        checks[row.id] = checkedAt;
        return checks;
      }, { ...current[nextTab] }),
    }));
  }

  function withLastHealthCheckedAt<T extends { id: number }>(rows: T[], nextTab: HealthTab) {
    const checks = lastHealthChecks[nextTab];
    return rows.map((row) => ({
      ...row,
      lastHealthCheckedAt: checks[row.id],
    }));
  }

  async function fetchOllamaModels() {
    setStatus('Fetching Ollama models');
    const nextModels = await sendJson<OllamaModel[]>('/api/connections/ollama/models', 'POST', {
      baseUrl: ollamaForm.baseUrl,
    });
    setModels(nextModels);
    setOllamaForm((current) => ({
      ...current,
      model: nextModels.some((model) => model.name === current.model) ? current.model : nextModels[0]?.name || '',
    }));
    setStatus(`Loaded ${nextModels.length} models`);
  }

  async function submitOllama(event: FormEvent) {
    event.preventDefault();
    const path = editingOllamaId == null ? '/api/connections/ollama' : `/api/connections/ollama/${editingOllamaId}`;
    const method = editingOllamaId == null ? 'POST' : 'PUT';
    await sendJson<Ollama>(path, method, ollamaForm);
    resetOllamaForm();
    await loadOllama();
    setStatus('Ollama connection saved');
  }

  async function submitDocker(event: FormEvent) {
    event.preventDefault();
    const path = editingDockerId == null ? '/api/connections/docker' : `/api/connections/docker/${editingDockerId}`;
    const method = editingDockerId == null ? 'POST' : 'PUT';
    await sendJson<Docker>(path, method, dockerForm);
    resetDockerForm();
    await loadDocker();
    setStatus('Docker connection saved');
  }

  async function submitAndroid(event: FormEvent) {
    event.preventDefault();
    const payload = {
      ...androidForm,
      dockerId: Number(androidForm.dockerId || docker[0]?.id),
    };
    if (editingAndroidId == null) {
      await sendJson('/api/connections/android', 'POST', payload);
      setStatus('Android VM creation queued');
    } else {
      await sendJson(`/api/connections/android/${editingAndroidId}`, 'PUT', payload);
      setStatus('Android VM saved');
    }
    resetAndroidForm();
    await loadAndroid();
    await loadLogs();
  }

  function editOllama(row: Ollama) {
    setEditingOllamaId(row.id);
    setOllamaForm({
      name: row.name,
      baseUrl: row.baseUrl,
      model: row.model,
      enabled: row.enabled,
    });
    setModels([{ name: row.model, model: row.model, size: 0 }]);
    setStatus('Editing Ollama connection');
  }

  function editDocker(row: Docker) {
    setEditingDockerId(row.id);
    setDockerForm({
      name: row.name,
      baseUrl: row.baseUrl,
      enabled: row.enabled,
    });
    setStatus('Editing Docker connection');
  }

  function editAndroid(row: AndroidVM) {
    setEditingAndroidId(row.id);
    setAndroidForm({
      dockerId: String(row.dockerId),
      name: row.name,
      image: row.image,
      accelerationMode: row.accelerationMode,
    });
    setStatus('Editing Android VM');
  }

  async function stopVm(id: number) {
    await sendJson(`/api/connections/android/${id}/stop`, 'POST');
    await loadAndroid();
    setStatus('Android VM stopped');
  }

  async function deleteVm(id: number) {
    await deleteResource(`/api/connections/android/${id}`);
    await loadAndroid();
    setStatus('Android VM deleted');
  }

  async function loadVmDetail(id: number) {
    const detail = await getJson<unknown>(`/api/connections/android/${id}`);
    setSelectedVmDetail(JSON.stringify(detail, null, 2));
  }

  function resetOllamaForm() {
    setEditingOllamaId(null);
    setModels([]);
    setOllamaForm({ name: 'bigscreen', baseUrl: 'http://bigscreen:11434', model: '', enabled: true });
  }

  function resetDockerForm() {
    setEditingDockerId(null);
    setDockerForm({ name: 'bigscreen', baseUrl: 'http://bigscreen:2375', enabled: true });
  }

  function resetAndroidForm() {
    setEditingAndroidId(null);
    setAndroidForm({
      dockerId: '',
      name: 'redroid-1',
      image: 'redroid/redroid:15.0.0_64only-latest',
      accelerationMode: 'GUEST',
    });
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
          <button type="button" onClick={() => loadTab(tab).then(() => setStatus('Loaded from database'))}>
            Load list
          </button>
          {tab !== 'logs' && (
            <button type="button" onClick={() => refreshTabHealth(tab).then(() => loadTab(tab)).then(() => setStatus('Health checked'))}>
              Check health
            </button>
          )}
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
                  <option key={model.name} value={model.name}>{model.name}</option>
                ))}
              </select>
            </label>
            <label className="checkbox-label">
              <input
                checked={ollamaForm.enabled}
                type="checkbox"
                onChange={(event) => setOllamaForm({ ...ollamaForm, enabled: event.target.checked })}
              />
              Enabled
            </label>
            <div className="button-row">
              <button type="button" onClick={() => fetchOllamaModels().catch((error) => setStatus(message(error)))}>
                Refresh models
              </button>
              <button type="submit">{editingOllamaId == null ? 'Save Ollama' : 'Update Ollama'}</button>
              {editingOllamaId != null && <button type="button" onClick={resetOllamaForm}>Cancel</button>}
            </div>
          </form>
          <DataTable
            rows={withLastHealthCheckedAt(ollama, 'ollama')}
            columns={['name', 'baseUrl', 'model', 'status', 'lastHealthCheckedAt', 'enabled']}
            onEdit={editOllama}
            onDelete={(row) => deleteResource(`/api/connections/ollama/${row.id}`).then(loadOllama)}
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
            <label className="checkbox-label">
              <input
                checked={dockerForm.enabled}
                type="checkbox"
                onChange={(event) => setDockerForm({ ...dockerForm, enabled: event.target.checked })}
              />
              Enabled
            </label>
            <div className="button-row">
              <button type="submit">{editingDockerId == null ? 'Save Docker' : 'Update Docker'}</button>
              {editingDockerId != null && <button type="button" onClick={resetDockerForm}>Cancel</button>}
            </div>
          </form>
          <DataTable
            rows={withLastHealthCheckedAt(docker, 'docker')}
            columns={['name', 'baseUrl', 'status', 'lastHealthCheckedAt', 'apiVersion', 'os', 'arch', 'nvidiaRuntimeAvailable']}
            onEdit={editDocker}
            onDelete={(row) => deleteResource(`/api/connections/docker/${row.id}`).then(loadDocker)}
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
                  <option key={item.id} value={item.id}>{item.name}</option>
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
              <button type="submit">{editingAndroidId == null ? 'Create Android VM' : 'Update Android VM'}</button>
              {editingAndroidId != null && <button type="button" onClick={resetAndroidForm}>Cancel</button>}
            </div>
          </form>
          <DataTable
            rows={withLastHealthCheckedAt(android, 'android')}
            columns={['name', 'dockerName', 'image', 'status', 'lastHealthCheckedAt', 'adbHost', 'adbPort', 'accelerationMode']}
            onDetail={(row) => loadVmDetail(row.id).catch((error) => setStatus(message(error)))}
            onEdit={editAndroid}
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
  onEdit,
  onStop,
}: {
  rows: T[];
  columns: string[];
  onDelete?: (row: T) => void;
  onDetail?: (row: T) => void;
  onEdit?: (row: T) => void;
  onStop?: (row: T) => void;
}) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((column) => <th key={column}>{column}</th>)}
            {(onDelete || onDetail || onEdit || onStop) && <th>actions</th>}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 && (
            <tr>
              <td colSpan={columns.length + 1}>No records loaded.</td>
            </tr>
          )}
          {rows.map((row) => (
            <tr key={row.id}>
              {columns.map((column) => <td key={column}>{cellValue(row, column)}</td>)}
              {(onDelete || onDetail || onEdit || onStop) && (
                <td>
                  <div className="table-actions">
                    {onDetail && <button type="button" onClick={() => onDetail(row)}>Detail</button>}
                    {onEdit && <button type="button" onClick={() => onEdit(row)}>Edit</button>}
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
    return '-';
  }
  if (typeof value === 'boolean') {
    return value ? 'yes' : 'no';
  }
  if (typeof value === 'number' && column.endsWith('At')) {
    return new Date(value).toLocaleString();
  }
  const text = String(value);
  return text.length > 140 ? `${text.slice(0, 140)}...` : text;
}

function label(tab: Tab) {
  return tab === 'android' ? 'Android VMs' : tab[0].toUpperCase() + tab.slice(1);
}

function message(error: unknown) {
  return error instanceof Error ? error.message : 'Request failed';
}
