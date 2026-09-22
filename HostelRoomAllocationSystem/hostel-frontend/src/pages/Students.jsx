/**
 * Students Page
 * -------------
 * Full CRUD for the student records.
 *
 * Data flow for every action:
 *   React → fetch() → Java StudentHandler → StudentService → StudentDAO → MySQL
 *
 * Demonstrates:
 *   - GET    /api/students         (list / search)
 *   - POST   /api/students         (add)
 *   - PUT    /api/students/{id}    (edit)
 *   - DELETE /api/students/{id}    (delete)
 */
import { useState, useEffect } from 'react';
import {
  getStudents,
  createStudent,
  updateStudent,
  deleteStudent,
} from '../services/api';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import SearchBar from '../components/SearchBar';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';

const DEPARTMENTS = ['CSE', 'ECE', 'EEE', 'MECH', 'CIVIL', 'IT', 'AIDS', 'AIML'];
const YEARS = [1, 2, 3, 4];
const GENDERS = ['MALE', 'FEMALE'];

export default function Students() {
  const { showToast } = useToast();

  /* ---- state ---- */
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  // modal state
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);     // null = add, object = edit
  const [saving, setSaving] = useState(false);

  // delete confirm
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  // form fields
  const [form, setForm] = useState({
    registerNumber: '', name: '', department: 'CSE',
    year: 1, gender: 'MALE', phone: '', email: '', address: '',
  });
  const [formErrors, setFormErrors] = useState({});

  /* ---- fetch ---- */
  useEffect(() => { fetchStudents(); }, []);

  async function fetchStudents(keyword = '') {
    setLoading(true);
    try {
      const res = await getStudents(keyword);
      setStudents(res.data || []);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  function handleSearch(keyword) {
    setSearch(keyword);
    fetchStudents(keyword);
  }

  /* ---- form helpers ---- */
  function openAdd() {
    setEditing(null);
    setForm({
      registerNumber: '', name: '', department: 'CSE',
      year: 1, gender: 'MALE', phone: '', email: '', address: '',
    });
    setFormErrors({});
    setShowForm(true);
  }

  function openEdit(student) {
    setEditing(student);
    setForm({
      registerNumber: student.registerNumber || '',
      name: student.name || '',
      department: student.department || 'CSE',
      year: student.year || 1,
      gender: student.gender || 'MALE',
      phone: student.phone || '',
      email: student.email || '',
      address: student.address || '',
    });
    setFormErrors({});
    setShowForm(true);
  }

  function validate() {
    const errors = {};
    if (!/^[A-Z0-9]{5,15}$/.test(form.registerNumber))
      errors.registerNumber = 'Must be 5-15 uppercase letters/digits';
    if (!form.name.trim() || form.name.trim().length < 2)
      errors.name = 'Name must be at least 2 characters';
    if (!/^[6-9]\d{9}$/.test(form.phone))
      errors.phone = 'Must be a valid 10-digit Indian mobile';
    if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(form.email))
      errors.email = 'Must be a valid email address';
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;
    setSaving(true);
    try {
      if (editing) {
        await updateStudent(editing.studentId, form);
        showToast('Student updated successfully', 'success');
      } else {
        await createStudent(form);
        showToast('Student added successfully', 'success');
      }
      setShowForm(false);
      fetchStudents(search);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    setDeleting(true);
    try {
      await deleteStudent(deleteTarget.studentId);
      showToast('Student deleted', 'success');
      setDeleteTarget(null);
      fetchStudents(search);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setDeleting(false);
    }
  }

  function setField(field, value) {
    setForm(prev => ({ ...prev, [field]: value }));
  }

  /* ---- render ---- */
  if (loading) return <LoadingSpinner size="large" text="Loading students..." />;

  return (
    <div>
      {/* Toolbar */}
      <div className="toolbar">
        <SearchBar
          placeholder="Search by name or register number..."
          value={search}
          onSearch={handleSearch}
        />
        <button className="btn btn-primary" onClick={openAdd}>+ Add Student</button>
      </div>

      {/* Table */}
      {students.length === 0 ? (
        <EmptyState
          icon="🎓"
          title="No students found"
          description={search ? 'Try a different search term.' : 'Add your first student to get started.'}
        />
      ) : (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Reg No</th>
                <th>Name</th>
                <th>Department</th>
                <th>Year</th>
                <th>Gender</th>
                <th>Phone</th>
                <th>Email</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {students.map(s => (
                <tr key={s.studentId}>
                  <td><span className="badge badge-primary">{s.registerNumber}</span></td>
                  <td className="font-medium">{s.name}</td>
                  <td>{s.department}</td>
                  <td>{s.year}</td>
                  <td>
                    <span className={`badge ${s.gender === 'MALE' ? 'badge-info' : 'badge-warning'}`}>
                      {s.gender}
                    </span>
                  </td>
                  <td>{s.phone}</td>
                  <td className="text-muted">{s.email}</td>
                  <td>
                    <div className="action-btns">
                      <button className="btn btn-sm btn-outline" onClick={() => openEdit(s)}>Edit</button>
                      <button className="btn btn-sm btn-danger" onClick={() => setDeleteTarget(s)}>Delete</button>
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
        <Modal title={editing ? 'Edit Student' : 'Add Student'} onClose={() => setShowForm(false)} wide>
          <form onSubmit={handleSubmit} className="form-grid">
            <div className="form-group">
              <label className="form-label">Register Number</label>
              <input
                className={`form-input ${formErrors.registerNumber ? 'input-error' : ''}`}
                value={form.registerNumber}
                onChange={e => setField('registerNumber', e.target.value.toUpperCase())}
                disabled={!!editing}
                placeholder="e.g. 26CSE001"
              />
              {formErrors.registerNumber && <span className="form-error">{formErrors.registerNumber}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                className={`form-input ${formErrors.name ? 'input-error' : ''}`}
                value={form.name}
                onChange={e => setField('name', e.target.value)}
                placeholder="e.g. Rohith S"
              />
              {formErrors.name && <span className="form-error">{formErrors.name}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Department</label>
              <select className="form-select" value={form.department} onChange={e => setField('department', e.target.value)}>
                {DEPARTMENTS.map(d => <option key={d} value={d}>{d}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Year</label>
              <select className="form-select" value={form.year} onChange={e => setField('year', parseInt(e.target.value))}>
                {YEARS.map(y => <option key={y} value={y}>Year {y}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Gender</label>
              <select className="form-select" value={form.gender} onChange={e => setField('gender', e.target.value)}>
                {GENDERS.map(g => <option key={g} value={g}>{g}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Phone</label>
              <input
                className={`form-input ${formErrors.phone ? 'input-error' : ''}`}
                value={form.phone}
                onChange={e => setField('phone', e.target.value)}
                placeholder="e.g. 9876543210"
              />
              {formErrors.phone && <span className="form-error">{formErrors.phone}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Email</label>
              <input
                className={`form-input ${formErrors.email ? 'input-error' : ''}`}
                value={form.email}
                onChange={e => setField('email', e.target.value)}
                placeholder="e.g. rohith@example.com"
              />
              {formErrors.email && <span className="form-error">{formErrors.email}</span>}
            </div>

            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label className="form-label">Address</label>
              <textarea
                className="form-input"
                rows={2}
                value={form.address}
                onChange={e => setField('address', e.target.value)}
                placeholder="Optional"
              />
            </div>

            <div className="form-actions" style={{ gridColumn: '1 / -1' }}>
              <button type="button" className="btn btn-outline" onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Saving...' : editing ? 'Update Student' : 'Add Student'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Delete Confirm */}
      {deleteTarget && (
        <ConfirmDialog
          title="Delete Student"
          message={`Are you sure you want to delete ${deleteTarget.name} (${deleteTarget.registerNumber})? This action cannot be undone.`}
          confirmText="Delete"
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
          loading={deleting}
        />
      )}
    </div>
  );
}
