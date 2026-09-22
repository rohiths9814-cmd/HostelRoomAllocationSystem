/**
 * Rooms Page
 * ----------
 * Full CRUD for hostel rooms with block association and occupancy tracking.
 *
 * Data flow:
 *   React → fetch() → Java RoomHandler → RoomService → RoomDAO → MySQL
 *
 * Demonstrates:
 *   - GET    /api/rooms             (list with status filter)
 *   - GET    /api/rooms/available   (rooms with free beds)
 *   - GET    /api/blocks            (for dropdown)
 *   - POST   /api/rooms             (add)
 *   - PUT    /api/rooms/{id}        (edit)
 *   - PUT    /api/rooms/{id}/maintenance (toggle)
 *   - DELETE /api/rooms/{id}        (delete)
 */
import { useState, useEffect } from 'react';
import {
  getRooms, createRoom, updateRoom, deleteRoom,
  setRoomMaintenance, getBlocks, getRoomOccupants,
} from '../services/api';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';

const STATUS_OPTIONS = [
  { value: '', label: 'All Rooms' },
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'PARTIALLY_OCCUPIED', label: 'Partial' },
  { value: 'FULL', label: 'Full' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
];

export default function Rooms() {
  const { showToast } = useToast();

  const [rooms, setRooms] = useState([]);
  const [blocks, setBlocks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');

  // form modal
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ roomNumber: '', blockId: 0, floor: 1, capacity: 2 });
  const [formErrors, setFormErrors] = useState({});

  // delete confirm
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  // occupants modal
  const [occupantsRoom, setOccupantsRoom] = useState(null);
  const [occupants, setOccupants] = useState([]);
  const [loadingOccupants, setLoadingOccupants] = useState(false);

  /* ---- fetch ---- */
  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    setLoading(true);
    try {
      const [roomsRes, blocksRes] = await Promise.all([getRooms(), getBlocks()]);
      setRooms(roomsRes.data || []);
      setBlocks(blocksRes.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  async function fetchRooms(status = '') {
    try {
      const res = await getRooms(status);
      setRooms(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  function handleFilter(status) {
    setStatusFilter(status);
    fetchRooms(status);
  }

  /* ---- occupants ---- */
  async function viewOccupants(room) {
    setOccupantsRoom(room);
    setLoadingOccupants(true);
    try {
      const res = await getRoomOccupants(room.roomId);
      setOccupants(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoadingOccupants(false);
    }
  }

  /* ---- maintenance ---- */
  async function toggleMaintenance(room) {
    const under = room.status !== 'MAINTENANCE';
    try {
      await setRoomMaintenance(room.roomId, under);
      showToast(under ? 'Room set to maintenance' : 'Maintenance cleared', 'success');
      fetchRooms(statusFilter);
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  /* ---- form ---- */
  function openAdd() {
    setEditing(null);
    setForm({ roomNumber: '', blockId: blocks.length > 0 ? blocks[0].blockId : 0, floor: 1, capacity: 2 });
    setFormErrors({});
    setShowForm(true);
  }

  function openEdit(room) {
    setEditing(room);
    setForm({
      roomNumber: room.roomNumber || '',
      blockId: room.blockId || 0,
      floor: room.floor || 1,
      capacity: room.capacity || 2,
    });
    setFormErrors({});
    setShowForm(true);
  }

  function validate() {
    const errors = {};
    if (!/^[A-Za-z]+-\d{3}$/.test(form.roomNumber))
      errors.roomNumber = 'Must be like A-101 (letters-3digits)';
    if (!form.blockId || form.blockId === 0)
      errors.blockId = 'Select a block';
    if (form.capacity < 1 || form.capacity > 6)
      errors.capacity = 'Must be 1-6';
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;
    setSaving(true);
    try {
      if (editing) {
        await updateRoom(editing.roomId, form);
        showToast('Room updated', 'success');
      } else {
        await createRoom(form);
        showToast('Room added', 'success');
      }
      setShowForm(false);
      fetchRooms(statusFilter);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    setDeleting(true);
    try {
      await deleteRoom(deleteTarget.roomId);
      showToast('Room deleted', 'success');
      setDeleteTarget(null);
      fetchRooms(statusFilter);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setDeleting(false);
    }
  }

  function setField(field, value) {
    setForm(prev => ({ ...prev, [field]: value }));
  }

  function statusBadge(status) {
    const map = {
      AVAILABLE: 'badge-success',
      PARTIALLY_OCCUPIED: 'badge-warning',
      FULL: 'badge-danger',
      MAINTENANCE: 'badge-info',
    };
    return <span className={`badge ${map[status] || 'badge-info'}`}>{status}</span>;
  }

  if (loading) return <LoadingSpinner size="large" text="Loading rooms..." />;

  return (
    <div>
      {/* Toolbar */}
      <div className="toolbar">
        <div className="filter-group">
          {STATUS_OPTIONS.map(opt => (
            <button
              key={opt.value}
              className={`btn btn-sm ${statusFilter === opt.value ? 'btn-primary' : 'btn-outline'}`}
              onClick={() => handleFilter(opt.value)}
            >
              {opt.label}
            </button>
          ))}
        </div>
        <button className="btn btn-primary" onClick={openAdd}>+ Add Room</button>
      </div>

      {/* Table */}
      {rooms.length === 0 ? (
        <EmptyState icon="🏠" title="No rooms found" description="Add a room or change the filter." />
      ) : (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Room No</th>
                <th>Block</th>
                <th>Floor</th>
                <th>Capacity</th>
                <th>Occupied</th>
                <th>Available</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rooms.map(r => (
                <tr key={r.roomId}>
                  <td className="font-medium">{r.roomNumber}</td>
                  <td>{r.blockName || 'Block ' + r.blockId}</td>
                  <td>{r.floor}</td>
                  <td>{r.capacity}</td>
                  <td>{r.occupiedBeds}</td>
                  <td>{r.availableBeds}</td>
                  <td>{statusBadge(r.status)}</td>
                  <td>
                    <div className="action-btns">
                      {r.occupiedBeds > 0 && (
                        <button className="btn btn-sm btn-outline" onClick={() => viewOccupants(r)}>
                          👥 View
                        </button>
                      )}
                      <button className="btn btn-sm btn-outline" onClick={() => openEdit(r)}>Edit</button>
                      <button
                        className="btn btn-sm btn-outline"
                        onClick={() => toggleMaintenance(r)}
                      >
                        {r.status === 'MAINTENANCE' ? '✓ Clear' : '🔧 Maint'}
                      </button>
                      <button className="btn btn-sm btn-danger" onClick={() => setDeleteTarget(r)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Add / Edit Modal */}
      {showForm && (
        <Modal title={editing ? 'Edit Room' : 'Add Room'} onClose={() => setShowForm(false)}>
          <form onSubmit={handleSubmit} className="form-grid">
            <div className="form-group">
              <label className="form-label">Room Number</label>
              <input
                className={`form-input ${formErrors.roomNumber ? 'input-error' : ''}`}
                value={form.roomNumber}
                onChange={e => setField('roomNumber', e.target.value.toUpperCase())}
                placeholder="e.g. A-101"
              />
              {formErrors.roomNumber && <span className="form-error">{formErrors.roomNumber}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Block</label>
              <select
                className={`form-select ${formErrors.blockId ? 'input-error' : ''}`}
                value={form.blockId}
                onChange={e => setField('blockId', parseInt(e.target.value))}
              >
                <option value={0}>-- Select Block --</option>
                {blocks.map(b => (
                  <option key={b.blockId} value={b.blockId}>
                    {b.blockName} ({b.gender})
                  </option>
                ))}
              </select>
              {formErrors.blockId && <span className="form-error">{formErrors.blockId}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Floor</label>
              <input
                type="number" min={0} max={10}
                className="form-input"
                value={form.floor}
                onChange={e => setField('floor', parseInt(e.target.value) || 0)}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Capacity (beds)</label>
              <input
                type="number" min={1} max={6}
                className={`form-input ${formErrors.capacity ? 'input-error' : ''}`}
                value={form.capacity}
                onChange={e => setField('capacity', parseInt(e.target.value) || 1)}
              />
              {formErrors.capacity && <span className="form-error">{formErrors.capacity}</span>}
            </div>

            <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
              <button type="button" className="btn btn-outline" onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Saving...' : editing ? 'Update Room' : 'Add Room'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Occupants Modal */}
      {occupantsRoom && (
        <Modal
          title={`Occupants — Room ${occupantsRoom.roomNumber}`}
          onClose={() => setOccupantsRoom(null)}
          wide
        >
          {loadingOccupants ? (
            <LoadingSpinner text="Loading occupants..." />
          ) : occupants.length === 0 ? (
            <p className="text-muted" style={{ padding: 24 }}>No occupants.</p>
          ) : (
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr><th>Reg No</th><th>Name</th><th>Block</th><th>Since</th></tr>
                </thead>
                <tbody>
                  {occupants.map(o => (
                    <tr key={o.allocationId}>
                      <td><span className="badge badge-primary">{o.registerNumber}</span></td>
                      <td>{o.studentName}</td>
                      <td>{o.blockName}</td>
                      <td>{o.allocationDate}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Modal>
      )}

      {/* Delete Confirm */}
      {deleteTarget && (
        <ConfirmDialog
          title="Delete Room"
          message={`Delete room ${deleteTarget.roomNumber}? This cannot be undone.`}
          confirmText="Delete"
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </div>
  );
}
