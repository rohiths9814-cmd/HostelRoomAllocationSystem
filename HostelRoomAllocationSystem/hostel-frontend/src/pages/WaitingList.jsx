/**
 * Waiting List Page
 * -----------------
 * Shows the queue of students waiting for a room.
 *
 * Data flow:
 *   React → fetch() → Java WaitingListHandler → WaitingListService → WaitingListDAO → MySQL
 *
 * Demonstrates:
 *   - GET    /api/waiting-list         (all waiting entries)
 *   - POST   /api/waiting-list         (add student to queue)
 *   - DELETE /api/waiting-list/{id}    (remove from queue)
 *
 * The backend uses both a LinkedList (FIFO) and a PriorityQueue to
 * demonstrate different ordering strategies. The API returns the
 * priority-ordered list.
 */
import { useState, useEffect } from 'react';
import {
  getWaitingList, addToWaitingList, removeFromWaitingList,
  getUnallocatedStudents,
} from '../services/api';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';

export default function WaitingList() {
  const { showToast } = useToast();

  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);

  // add modal
  const [showForm, setShowForm] = useState(false);
  const [students, setStudents] = useState([]);
  const [form, setForm] = useState({ studentId: 0, priority: 5 });
  const [saving, setSaving] = useState(false);

  // remove confirm
  const [removeTarget, setRemoveTarget] = useState(null);
  const [removing, setRemoving] = useState(false);

  useEffect(() => { fetchList(); }, []);

  async function fetchList() {
    setLoading(true);
    try {
      const res = await getWaitingList();
      setEntries(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  /* ---- Add ---- */
  async function openAdd() {
    setShowForm(true);
    setForm({ studentId: 0, priority: 5 });
    try {
      const res = await getUnallocatedStudents();
      setStudents(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  async function handleAdd(e) {
    e.preventDefault();
    if (!form.studentId) {
      showToast('Select a student', 'warning');
      return;
    }
    setSaving(true);
    try {
      await addToWaitingList(form);
      showToast('Added to waiting list', 'success');
      setShowForm(false);
      fetchList();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setSaving(false);
    }
  }

  /* ---- Remove ---- */
  async function handleRemove() {
    setRemoving(true);
    try {
      await removeFromWaitingList(removeTarget.waitingId);
      showToast('Removed from waiting list', 'success');
      setRemoveTarget(null);
      fetchList();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setRemoving(false);
    }
  }

  function priorityBadge(p) {
    if (p <= 2) return <span className="badge badge-danger">Priority {p}</span>;
    if (p <= 3) return <span className="badge badge-warning">Priority {p}</span>;
    return <span className="badge badge-info">Priority {p}</span>;
  }

  if (loading) return <LoadingSpinner size="large" text="Loading waiting list..." />;

  return (
    <div>
      {/* Toolbar */}
      <div className="toolbar">
        <div>
          <span className="text-muted" style={{ fontSize: '0.9rem' }}>
            {entries.length} student{entries.length !== 1 ? 's' : ''} waiting
          </span>
        </div>
        <button className="btn btn-primary" onClick={openAdd}>+ Add to Queue</button>
      </div>

      {/* Info card */}
      <div className="card" style={{ marginBottom: 20, padding: '16px 20px', background: 'var(--surface-2)' }}>
        <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
          <strong>Priority 1</strong> = Highest (served first) &nbsp;|&nbsp;
          <strong>Priority 5</strong> = Lowest (served last).
          When priorities tie, the student who requested earlier is served first (FIFO).
        </p>
      </div>

      {/* Table */}
      {entries.length === 0 ? (
        <EmptyState icon="⏳" title="Waiting list is empty" description="No students are waiting for a room." />
      ) : (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Position</th>
                <th>Reg No</th>
                <th>Name</th>
                <th>Priority</th>
                <th>Requested</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {entries.map((entry, index) => (
                <tr key={entry.waitingId}>
                  <td className="font-medium">#{index + 1}</td>
                  <td><span className="badge badge-primary">{entry.registerNumber}</span></td>
                  <td className="font-medium">{entry.studentName}</td>
                  <td>{priorityBadge(entry.priority)}</td>
                  <td>{entry.requestDate}</td>
                  <td><span className="badge badge-warning">{entry.status}</span></td>
                  <td>
                    <button className="btn btn-sm btn-danger" onClick={() => setRemoveTarget(entry)}>
                      Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Add Modal */}
      {showForm && (
        <Modal title="Add to Waiting List" onClose={() => setShowForm(false)}>
          <form onSubmit={handleAdd} className="form-grid">
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label">Student (unallocated)</label>
              <select
                className="form-select"
                value={form.studentId}
                onChange={e => setForm(prev => ({ ...prev, studentId: parseInt(e.target.value) }))}
              >
                <option value={0}>-- Select Student --</option>
                {students.map(s => (
                  <option key={s.studentId} value={s.studentId}>
                    {s.registerNumber} — {s.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label">Priority (1 = Highest, 5 = Lowest)</label>
              <select
                className="form-select"
                value={form.priority}
                onChange={e => setForm(prev => ({ ...prev, priority: parseInt(e.target.value) }))}
              >
                <option value={1}>1 — Highest (urgent / medical / final year)</option>
                <option value={2}>2 — High</option>
                <option value={3}>3 — Medium</option>
                <option value={4}>4 — Low</option>
                <option value={5}>5 — Lowest (normal request)</option>
              </select>
            </div>

            <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
              <button type="button" className="btn btn-outline" onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Adding...' : 'Add to Queue'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Remove Confirm */}
      {removeTarget && (
        <ConfirmDialog
          title="Remove from Waiting List"
          message={`Remove ${removeTarget.studentName} from the waiting list?`}
          confirmText="Remove"
          onConfirm={handleRemove}
          onCancel={() => setRemoveTarget(null)}
          loading={removing}
        />
      )}
    </div>
  );
}
