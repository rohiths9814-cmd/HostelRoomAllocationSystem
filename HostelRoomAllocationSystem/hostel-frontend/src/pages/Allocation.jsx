/**
 * Allocation Page
 * ---------------
 * Three workflows on one page:
 *   1. Allocate — assign a room to a student  (POST /api/allocations)
 *   2. Transfer — move a student to another room (PUT /api/allocations/transfer)
 *   3. Checkout — release a student from their room (PUT /api/allocations/checkout)
 *
 * Plus the active-allocations table showing who lives where.
 *
 * Each operation triggers a JDBC TRANSACTION in the Java backend:
 *   allocate  = insert allocation + update room occupancy
 *   transfer  = close old + free old bed + take new bed + insert new
 *   checkout  = close allocation + free bed
 */
import { useState, useEffect } from 'react';
import {
  getActiveAllocations, getAllAllocations,
  getUnallocatedStudents, getAvailableRooms,
  allocateRoom, transferStudent, checkoutStudent,
} from '../services/api';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';

export default function Allocation() {
  const { showToast } = useToast();

  const [activeTab, setActiveTab] = useState('active'); // active | history
  const [allocations, setAllocations] = useState([]);
  const [loading, setLoading] = useState(true);

  // Allocate modal
  const [showAllocate, setShowAllocate] = useState(false);
  const [unallocated, setUnallocated] = useState([]);
  const [available, setAvailable] = useState([]);
  const [allocForm, setAllocForm] = useState({ studentId: 0, roomId: 0 });
  const [allocating, setAllocating] = useState(false);

  // Transfer modal
  const [transferTarget, setTransferTarget] = useState(null);
  const [newRoomId, setNewRoomId] = useState(0);
  const [transferring, setTransferring] = useState(false);

  // Checkout confirm
  const [checkoutTarget, setCheckoutTarget] = useState(null);
  const [checkingOut, setCheckingOut] = useState(false);

  useEffect(() => { fetchAllocations(); }, [activeTab]);

  async function fetchAllocations() {
    setLoading(true);
    try {
      const res = activeTab === 'active'
        ? await getActiveAllocations()
        : await getAllAllocations();
      setAllocations(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  /* ---- Allocate ---- */
  async function openAllocate() {
    setShowAllocate(true);
    setAllocForm({ studentId: 0, roomId: 0 });
    try {
      const [stuRes, roomRes] = await Promise.all([
        getUnallocatedStudents(),
        getAvailableRooms(),
      ]);
      setUnallocated(stuRes.data || []);
      setAvailable(roomRes.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  async function handleAllocate(e) {
    e.preventDefault();
    if (!allocForm.studentId || !allocForm.roomId) {
      showToast('Select both a student and a room', 'warning');
      return;
    }
    setAllocating(true);
    try {
      const res = await allocateRoom(allocForm.studentId, allocForm.roomId);
      showToast(`${res.data?.studentName} allocated to ${res.data?.roomNumber}`, 'success');
      setShowAllocate(false);
      fetchAllocations();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setAllocating(false);
    }
  }

  /* ---- Transfer ---- */
  async function openTransfer(alloc) {
    setTransferTarget(alloc);
    setNewRoomId(0);
    try {
      const res = await getAvailableRooms();
      setAvailable(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  async function handleTransfer(e) {
    e.preventDefault();
    if (!newRoomId) {
      showToast('Select a new room', 'warning');
      return;
    }
    setTransferring(true);
    try {
      const res = await transferStudent(transferTarget.studentId, newRoomId);
      showToast(`${res.data?.studentName} transferred to ${res.data?.roomNumber}`, 'success');
      setTransferTarget(null);
      fetchAllocations();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setTransferring(false);
    }
  }

  /* ---- Checkout ---- */
  async function handleCheckout() {
    setCheckingOut(true);
    try {
      await checkoutStudent(checkoutTarget.studentId);
      showToast(`${checkoutTarget.studentName} checked out`, 'success');
      setCheckoutTarget(null);
      fetchAllocations();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setCheckingOut(false);
    }
  }

  function statusBadge(status) {
    const map = {
      ACTIVE: 'badge-success',
      CHECKED_OUT: 'badge-info',
      TRANSFERRED: 'badge-warning',
    };
    return <span className={`badge ${map[status] || 'badge-info'}`}>{status}</span>;
  }

  if (loading) return <LoadingSpinner size="large" text="Loading allocations..." />;

  return (
    <div>
      {/* Tabs + Action */}
      <div className="toolbar">
        <div className="filter-group">
          <button
            className={`btn btn-sm ${activeTab === 'active' ? 'btn-primary' : 'btn-outline'}`}
            onClick={() => setActiveTab('active')}
          >
            Active Allocations
          </button>
          <button
            className={`btn btn-sm ${activeTab === 'history' ? 'btn-primary' : 'btn-outline'}`}
            onClick={() => setActiveTab('history')}
          >
            Full History
          </button>
        </div>
        <button className="btn btn-primary" onClick={openAllocate}>+ Allocate Room</button>
      </div>

      {/* Table */}
      {allocations.length === 0 ? (
        <EmptyState
          icon="🏠"
          title="No allocations found"
          description={activeTab === 'active' ? 'No students are currently allocated.' : 'No allocation history yet.'}
        />
      ) : (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Reg No</th>
                <th>Student</th>
                <th>Room</th>
                <th>Block</th>
                <th>Allocated</th>
                {activeTab === 'history' && <th>Checkout</th>}
                <th>Status</th>
                {activeTab === 'active' && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {allocations.map(a => (
                <tr key={a.allocationId}>
                  <td>{a.allocationId}</td>
                  <td><span className="badge badge-primary">{a.registerNumber}</span></td>
                  <td className="font-medium">{a.studentName}</td>
                  <td>{a.roomNumber}</td>
                  <td>{a.blockName}</td>
                  <td>{a.allocationDate}</td>
                  {activeTab === 'history' && <td>{a.checkoutDate || '—'}</td>}
                  <td>{statusBadge(a.status)}</td>
                  {activeTab === 'active' && (
                    <td>
                      <div className="action-btns">
                        <button className="btn btn-sm btn-outline" onClick={() => openTransfer(a)}>
                          🔄 Transfer
                        </button>
                        <button className="btn btn-sm btn-danger" onClick={() => setCheckoutTarget(a)}>
                          🚪 Checkout
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Allocate Modal */}
      {showAllocate && (
        <Modal title="Allocate Room" onClose={() => setShowAllocate(false)} wide>
          <form onSubmit={handleAllocate} className="form-grid">
            <div className="form-group">
              <label className="form-label">Student (unallocated)</label>
              <select
                className="form-select"
                value={allocForm.studentId}
                onChange={e => setAllocForm(prev => ({ ...prev, studentId: parseInt(e.target.value) }))}
              >
                <option value={0}>-- Select Student --</option>
                {unallocated.map(s => (
                  <option key={s.studentId} value={s.studentId}>
                    {s.registerNumber} — {s.name} ({s.gender})
                  </option>
                ))}
              </select>
              {unallocated.length === 0 && (
                <span className="form-error">No unallocated students available</span>
              )}
            </div>

            <div className="form-group">
              <label className="form-label">Room (available)</label>
              <select
                className="form-select"
                value={allocForm.roomId}
                onChange={e => setAllocForm(prev => ({ ...prev, roomId: parseInt(e.target.value) }))}
              >
                <option value={0}>-- Select Room --</option>
                {available.map(r => (
                  <option key={r.roomId} value={r.roomId}>
                    {r.roomNumber} — {r.blockName} ({r.availableBeds} bed{r.availableBeds !== 1 ? 's' : ''} free)
                  </option>
                ))}
              </select>
              {available.length === 0 && (
                <span className="form-error">No rooms with free beds</span>
              )}
            </div>

            <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
              <button type="button" className="btn btn-outline" onClick={() => setShowAllocate(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={allocating}>
                {allocating ? 'Allocating...' : 'Allocate Room'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Transfer Modal */}
      {transferTarget && (
        <Modal
          title={`Transfer ${transferTarget.studentName}`}
          onClose={() => setTransferTarget(null)}
        >
          <div style={{ padding: '0 24px 8px' }}>
            <p className="text-muted">
              Currently in room <strong>{transferTarget.roomNumber}</strong> ({transferTarget.blockName}).
              Select a new room below.
            </p>
          </div>
          <form onSubmit={handleTransfer} className="form-grid">
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label">New Room</label>
              <select
                className="form-select"
                value={newRoomId}
                onChange={e => setNewRoomId(parseInt(e.target.value))}
              >
                <option value={0}>-- Select Room --</option>
                {available.filter(r => r.roomId !== transferTarget.roomId).map(r => (
                  <option key={r.roomId} value={r.roomId}>
                    {r.roomNumber} — {r.blockName} ({r.availableBeds} bed{r.availableBeds !== 1 ? 's' : ''} free)
                  </option>
                ))}
              </select>
            </div>
            <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
              <button type="button" className="btn btn-outline" onClick={() => setTransferTarget(null)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={transferring}>
                {transferring ? 'Transferring...' : 'Transfer Student'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Checkout Confirm */}
      {checkoutTarget && (
        <ConfirmDialog
          title="Checkout Student"
          message={`Check out ${checkoutTarget.studentName} (${checkoutTarget.registerNumber}) from room ${checkoutTarget.roomNumber}?`}
          confirmText="Checkout"
          variant="danger"
          onConfirm={handleCheckout}
          onCancel={() => setCheckoutTarget(null)}
          loading={checkingOut}
        />
      )}
    </div>
  );
}
