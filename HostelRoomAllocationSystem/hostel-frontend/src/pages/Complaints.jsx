/**
 * Complaints Page
 * ---------------
 * CRUD for hostel complaints with status workflow.
 *
 * Status workflow:  PENDING → IN_PROGRESS → RESOLVED
 *
 * Data flow:
 *   React → fetch() → Java ComplaintHandler → ComplaintService → ComplaintDAO → MySQL
 */
import { useState, useEffect } from 'react';
import {
  getComplaints, createComplaint,
  updateComplaintStatus, deleteComplaint,
  getStudents,
} from '../services/api';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';

const CATEGORIES = ['ELECTRICITY', 'WATER', 'CLEANING', 'WIFI', 'FURNITURE', 'OTHER'];
const STATUS_FILTERS = [
  { value: '', label: 'All' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'RESOLVED', label: 'Resolved' },
];

export default function Complaints() {
  const { showToast } = useToast();

  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');

  // new complaint
  const [showForm, setShowForm] = useState(false);
  const [students, setStudents] = useState([]);
  const [form, setForm] = useState({ studentId: 0, category: 'ELECTRICITY', description: '' });
  const [saving, setSaving] = useState(false);

  // delete
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => { fetchComplaints(); }, []);

  async function fetchComplaints(status = '') {
    setLoading(true);
    try {
      const res = await getComplaints(status);
      setComplaints(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  function handleFilter(status) {
    setStatusFilter(status);
    fetchComplaints(status);
  }

  /* ---- Create ---- */
  async function openCreate() {
    setShowForm(true);
    setForm({ studentId: 0, category: 'ELECTRICITY', description: '' });
    try {
      const res = await getStudents();
      setStudents(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  async function handleCreate(e) {
    e.preventDefault();
    if (!form.studentId) {
      showToast('Select a student', 'warning');
      return;
    }
    if (form.description.trim().length < 5) {
      showToast('Description must be at least 5 characters', 'warning');
      return;
    }
    setSaving(true);
    try {
      await createComplaint(form);
      showToast('Complaint registered', 'success');
      setShowForm(false);
      fetchComplaints(statusFilter);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setSaving(false);
    }
  }

  /* ---- Status update ---- */
  async function changeStatus(complaint, newStatus) {
    try {
      await updateComplaintStatus(complaint.complaintId, newStatus);
      showToast(`Status updated to ${newStatus}`, 'success');
      fetchComplaints(statusFilter);
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  /* ---- Delete ---- */
  async function handleDelete() {
    setDeleting(true);
    try {
      await deleteComplaint(deleteTarget.complaintId);
      showToast('Complaint deleted', 'success');
      setDeleteTarget(null);
      fetchComplaints(statusFilter);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setDeleting(false);
    }
  }

  function statusBadge(status) {
    const map = {
      PENDING: 'badge-warning',
      IN_PROGRESS: 'badge-info',
      RESOLVED: 'badge-success',
    };
    return <span className={`badge ${map[status] || 'badge-info'}`}>{status.replace('_', ' ')}</span>;
  }

  function categoryIcon(cat) {
    const icons = {
      ELECTRICITY: '⚡', WATER: '💧', CLEANING: '🧹',
      WIFI: '📶', FURNITURE: '🪑', OTHER: '📋',
    };
    return icons[cat] || '📋';
  }

  if (loading) return <LoadingSpinner size="large" text="Loading complaints..." />;

  return (
    <div>
      {/* Toolbar */}
      <div className="toolbar">
        <div className="filter-group">
          {STATUS_FILTERS.map(opt => (
            <button
              key={opt.value}
              className={`btn btn-sm ${statusFilter === opt.value ? 'btn-primary' : 'btn-outline'}`}
              onClick={() => handleFilter(opt.value)}
            >
              {opt.label}
            </button>
          ))}
        </div>
        <button className="btn btn-primary" onClick={openCreate}>+ New Complaint</button>
      </div>

      {/* Table */}
      {complaints.length === 0 ? (
        <EmptyState icon="📝" title="No complaints" description="No complaints match the current filter." />
      ) : (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Student</th>
                <th>Category</th>
                <th>Description</th>
                <th>Filed</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {complaints.map(c => (
                <tr key={c.complaintId}>
                  <td>{c.complaintId}</td>
                  <td className="font-medium">{c.studentName || 'Student #' + c.studentId}</td>
                  <td>{categoryIcon(c.category)} {c.category}</td>
                  <td className="text-muted" style={{ maxWidth: 250, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {c.description}
                  </td>
                  <td>{c.complaintDate}</td>
                  <td>{statusBadge(c.status)}</td>
                  <td>
                    <div className="action-btns">
                      {c.status === 'PENDING' && (
                        <button className="btn btn-sm btn-outline" onClick={() => changeStatus(c, 'IN_PROGRESS')}>
                          ▶ Start
                        </button>
                      )}
                      {(c.status === 'PENDING' || c.status === 'IN_PROGRESS') && (
                        <button className="btn btn-sm btn-primary" onClick={() => changeStatus(c, 'RESOLVED')}>
                          ✓ Resolve
                        </button>
                      )}
                      <button className="btn btn-sm btn-danger" onClick={() => setDeleteTarget(c)}>
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* New Complaint Modal */}
      {showForm && (
        <Modal title="Register Complaint" onClose={() => setShowForm(false)} wide>
          <form onSubmit={handleCreate} className="form-grid">
            <div className="form-group">
              <label className="form-label">Student</label>
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

            <div className="form-group">
              <label className="form-label">Category</label>
              <select
                className="form-select"
                value={form.category}
                onChange={e => setForm(prev => ({ ...prev, category: e.target.value }))}
              >
                {CATEGORIES.map(c => <option key={c} value={c}>{categoryIcon(c)} {c}</option>)}
              </select>
            </div>

            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label">Description</label>
              <textarea
                className="form-input"
                rows={3}
                value={form.description}
                onChange={e => setForm(prev => ({ ...prev, description: e.target.value }))}
                placeholder="Describe the issue in detail (at least 5 characters)"
              />
            </div>

            <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
              <button type="button" className="btn btn-outline" onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Submitting...' : 'Submit Complaint'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Delete Confirm */}
      {deleteTarget && (
        <ConfirmDialog
          title="Delete Complaint"
          message={`Delete complaint #${deleteTarget.complaintId}?`}
          confirmText="Delete"
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </div>
  );
}
