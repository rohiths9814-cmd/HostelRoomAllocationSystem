/**
 * Fees Page
 * ---------
 * Hostel fee management with payment recording.
 *
 * Data flow:
 *   React → fetch() → Java FeeHandler → FeeService → FeeDAO → MySQL
 *
 * Demonstrates:
 *   - GET    /api/fees              (list with status filter)
 *   - POST   /api/fees              (add fee record)
 *   - PUT    /api/fees/{id}/pay     (mark as paid)
 *   - DELETE /api/fees/{id}         (delete)
 */
import { useState, useEffect } from 'react';
import {
  getFees, addFee, markFeePaid, deleteFee,
  getStudents,
} from '../services/api';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';

const STATUS_FILTERS = [
  { value: '', label: 'All Fees' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'PAID', label: 'Paid' },
];

export default function Fees() {
  const { showToast } = useToast();

  const [fees, setFees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');

  // add fee modal
  const [showForm, setShowForm] = useState(false);
  const [students, setStudents] = useState([]);
  const [form, setForm] = useState({ studentId: 0, amount: '' });
  const [saving, setSaving] = useState(false);

  // pay confirm
  const [payTarget, setPayTarget] = useState(null);
  const [paying, setPaying] = useState(false);

  // delete
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => { fetchFees(); }, []);

  async function fetchFees(status = '') {
    setLoading(true);
    try {
      const res = await getFees(status);
      setFees(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  function handleFilter(status) {
    setStatusFilter(status);
    fetchFees(status);
  }

  /* ---- Create ---- */
  async function openCreate() {
    setShowForm(true);
    setForm({ studentId: 0, amount: '' });
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
    const amount = parseFloat(form.amount);
    if (isNaN(amount) || amount < 1000 || amount > 200000) {
      showToast('Amount must be between ₹1,000 and ₹2,00,000', 'warning');
      return;
    }
    setSaving(true);
    try {
      await addFee({ studentId: form.studentId, amount });
      showToast('Fee record added', 'success');
      setShowForm(false);
      fetchFees(statusFilter);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setSaving(false);
    }
  }

  /* ---- Mark Paid ---- */
  async function handlePay() {
    setPaying(true);
    try {
      await markFeePaid(payTarget.feeId);
      showToast('Payment recorded', 'success');
      setPayTarget(null);
      fetchFees(statusFilter);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setPaying(false);
    }
  }

  /* ---- Delete ---- */
  async function handleDelete() {
    setDeleting(true);
    try {
      await deleteFee(deleteTarget.feeId);
      showToast('Fee record deleted', 'success');
      setDeleteTarget(null);
      fetchFees(statusFilter);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setDeleting(false);
    }
  }

  function formatCurrency(amount) {
    return '₹' + Number(amount).toLocaleString('en-IN');
  }

  if (loading) return <LoadingSpinner size="large" text="Loading fees..." />;

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
        <button className="btn btn-primary" onClick={openCreate}>+ Add Fee</button>
      </div>

      {/* Table */}
      {fees.length === 0 ? (
        <EmptyState icon="💰" title="No fee records" description="Add a fee or change the filter." />
      ) : (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Student</th>
                <th>Amount</th>
                <th>Due Date</th>
                <th>Payment Date</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {fees.map(f => (
                <tr key={f.feeId}>
                  <td>{f.feeId}</td>
                  <td className="font-medium">{f.studentName || 'Student #' + f.studentId}</td>
                  <td className="font-medium">{formatCurrency(f.amount)}</td>
                  <td>{f.dueDate || '—'}</td>
                  <td>{f.paymentDate || '—'}</td>
                  <td>
                    <span className={`badge ${f.status === 'PAID' ? 'badge-success' : 'badge-warning'}`}>
                      {f.status}
                    </span>
                  </td>
                  <td>
                    <div className="action-btns">
                      {f.status === 'PENDING' && (
                        <button className="btn btn-sm btn-primary" onClick={() => setPayTarget(f)}>
                          💳 Pay
                        </button>
                      )}
                      <button className="btn btn-sm btn-danger" onClick={() => setDeleteTarget(f)}>
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

      {/* Add Fee Modal */}
      {showForm && (
        <Modal title="Add Fee Record" onClose={() => setShowForm(false)}>
          <form onSubmit={handleCreate} className="form-grid">
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
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

            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label">Amount (₹)</label>
              <input
                type="number"
                className="form-input"
                value={form.amount}
                onChange={e => setForm(prev => ({ ...prev, amount: e.target.value }))}
                placeholder="e.g. 25000"
                min={1000}
                max={200000}
              />
              <span className="form-hint">Between ₹1,000 and ₹2,00,000</span>
            </div>

            <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
              <button type="button" className="btn btn-outline" onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Adding...' : 'Add Fee'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Pay Confirm */}
      {payTarget && (
        <ConfirmDialog
          title="Record Payment"
          message={`Mark fee #${payTarget.feeId} (${formatCurrency(payTarget.amount)}) as PAID?`}
          confirmText="Record Payment"
          variant="info"
          onConfirm={handlePay}
          onCancel={() => setPayTarget(null)}
          loading={paying}
        />
      )}

      {/* Delete Confirm */}
      {deleteTarget && (
        <ConfirmDialog
          title="Delete Fee Record"
          message={`Delete fee #${deleteTarget.feeId}?`}
          confirmText="Delete"
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </div>
  );
}
