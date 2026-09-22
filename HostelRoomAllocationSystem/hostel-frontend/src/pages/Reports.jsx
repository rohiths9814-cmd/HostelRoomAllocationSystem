/**
 * Reports Page
 * ------------
 * Read-only analytics dashboard with four report sections.
 *
 * Data flow:
 *   React → fetch() → Java ReportHandler → multiple DAOs → MySQL
 *
 * Demonstrates:
 *   - GET /api/reports/summary          → overall statistics
 *   - GET /api/reports/block-vacancy    → vacancy per block
 *   - GET /api/reports/complaint-stats  → complaints by status & category
 *   - GET /api/reports/fee-stats        → fee totals
 */
import { useState, useEffect } from 'react';
import {
  getReportSummary, getBlockVacancy,
  getComplaintStats, getFeeStats,
} from '../services/api';
import { useToast } from '../components/Toast';
import LoadingSpinner from '../components/LoadingSpinner';

export default function Reports() {
  const { showToast } = useToast();

  const [summary, setSummary] = useState(null);
  const [blockVacancy, setBlockVacancy] = useState([]);
  const [complaintStats, setComplaintStats] = useState(null);
  const [feeStats, setFeeStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchAll(); }, []);

  async function fetchAll() {
    setLoading(true);
    try {
      const [sRes, bRes, cRes, fRes] = await Promise.all([
        getReportSummary(),
        getBlockVacancy(),
        getComplaintStats(),
        getFeeStats(),
      ]);
      setSummary(sRes.data);
      setBlockVacancy(bRes.data || []);
      setComplaintStats(cRes.data);
      setFeeStats(fRes.data);
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  function formatCurrency(amount) {
    return '₹' + Number(amount || 0).toLocaleString('en-IN');
  }

  function percent(val, total) {
    if (!total || total === 0) return '0%';
    return ((val / total) * 100).toFixed(1) + '%';
  }

  if (loading) return <LoadingSpinner size="large" text="Generating reports..." />;

  return (
    <div>
      {/* ─── Section 1: Summary ─── */}
      <div className="card" style={{ marginBottom: 24 }}>
        <h2 className="section-title">📊 Overall Summary</h2>
        {summary && (
          <div className="stat-grid" style={{ marginTop: 16 }}>
            <div className="stat-card"><div className="stat-card-label">Students</div><div className="stat-card-value">{summary.totalStudents}</div></div>
            <div className="stat-card"><div className="stat-card-label">Blocks</div><div className="stat-card-value">{summary.totalBlocks}</div></div>
            <div className="stat-card"><div className="stat-card-label">Rooms</div><div className="stat-card-value">{summary.totalRooms}</div></div>
            <div className="stat-card"><div className="stat-card-label">Total Beds</div><div className="stat-card-value">{summary.totalBeds}</div></div>
            <div className="stat-card"><div className="stat-card-label">Occupied</div><div className="stat-card-value">{summary.occupiedBeds}</div></div>
            <div className="stat-card"><div className="stat-card-label">Free Beds</div><div className="stat-card-value">{summary.freeBeds}</div></div>
            <div className="stat-card">
              <div className="stat-card-label">Occupancy</div>
              <div className="stat-card-value">{summary.occupancyPercent?.toFixed(1)}%</div>
              <div style={{ marginTop: 8, background: 'var(--border)', borderRadius: 4, height: 8, overflow: 'hidden' }}>
                <div style={{
                  width: `${Math.min(summary.occupancyPercent || 0, 100)}%`,
                  height: '100%',
                  background: summary.occupancyPercent > 80 ? 'var(--danger)' : 'var(--primary)',
                  borderRadius: 4,
                  transition: 'width 0.5s ease',
                }} />
              </div>
            </div>
            <div className="stat-card"><div className="stat-card-label">Active Alloc.</div><div className="stat-card-value">{summary.activeAllocations}</div></div>
          </div>
        )}

        {/* Room status breakdown */}
        {summary && (
          <div style={{ display: 'flex', gap: 12, marginTop: 20, flexWrap: 'wrap' }}>
            <span className="badge badge-success">Available: {summary.availableRooms}</span>
            <span className="badge badge-warning">Partial: {summary.partialRooms}</span>
            <span className="badge badge-danger">Full: {summary.fullRooms}</span>
            <span className="badge badge-info">Maintenance: {summary.maintenanceRooms}</span>
            <span className="badge badge-warning">Waiting: {summary.waitingStudents}</span>
          </div>
        )}
      </div>

      {/* ─── Section 2: Block Vacancy ─── */}
      <div className="card" style={{ marginBottom: 24 }}>
        <h2 className="section-title">🏢 Block-wise Vacancy</h2>
        {blockVacancy.length === 0 ? (
          <p className="text-muted" style={{ padding: '16px 0' }}>No block data available.</p>
        ) : (
          <div className="table-wrapper" style={{ marginTop: 16 }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Block</th>
                  <th>Gender</th>
                  <th>Total Beds</th>
                  <th>Occupied</th>
                  <th>Free</th>
                  <th>Occupancy</th>
                </tr>
              </thead>
              <tbody>
                {blockVacancy.map(b => (
                  <tr key={b.blockId}>
                    <td className="font-medium">{b.blockName}</td>
                    <td>
                      <span className={`badge ${b.gender === 'MALE' ? 'badge-info' : 'badge-warning'}`}>
                        {b.gender}
                      </span>
                    </td>
                    <td>{b.totalBeds}</td>
                    <td>{b.occupiedBeds}</td>
                    <td className="font-medium">{b.freeBeds}</td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <div style={{ flex: 1, maxWidth: 100, background: 'var(--border)', borderRadius: 4, height: 8, overflow: 'hidden' }}>
                          <div style={{
                            width: percent(b.occupiedBeds, b.totalBeds),
                            height: '100%',
                            background: b.freeBeds === 0 ? 'var(--danger)' : 'var(--primary)',
                            borderRadius: 4,
                          }} />
                        </div>
                        <span className="text-muted" style={{ fontSize: '0.8rem' }}>
                          {percent(b.occupiedBeds, b.totalBeds)}
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ─── Section 3 & 4: Complaints + Fees side by side ─── */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        {/* Complaint Stats */}
        <div className="card">
          <h2 className="section-title">📝 Complaint Statistics</h2>
          {complaintStats && (
            <div style={{ marginTop: 16 }}>
              <p className="font-medium" style={{ marginBottom: 12 }}>
                Total Complaints: <strong>{complaintStats.total}</strong>
              </p>

              <h4 style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: 8 }}>By Status</h4>
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 16 }}>
                {complaintStats.byStatus && Object.entries(complaintStats.byStatus).map(([status, count]) => (
                  <span key={status} className={`badge ${status === 'RESOLVED' ? 'badge-success' : status === 'IN_PROGRESS' ? 'badge-info' : 'badge-warning'}`}>
                    {status.replace('_', ' ')}: {count}
                  </span>
                ))}
              </div>

              <h4 style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: 8 }}>By Category</h4>
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                {complaintStats.byCategory && Object.entries(complaintStats.byCategory).map(([cat, count]) => (
                  <span key={cat} className="badge badge-primary">{cat}: {count}</span>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Fee Stats */}
        <div className="card">
          <h2 className="section-title">💰 Fee Statistics</h2>
          {feeStats && (
            <div style={{ marginTop: 16 }}>
              <div className="stat-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
                <div className="stat-card">
                  <div className="stat-card-label">Pending Fees</div>
                  <div className="stat-card-value" style={{ color: 'var(--warning)' }}>{feeStats.pendingCount}</div>
                  <div className="stat-card-sub">{formatCurrency(feeStats.totalPending)}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-card-label">Collected</div>
                  <div className="stat-card-value" style={{ color: 'var(--success)' }}>{feeStats.paidCount}</div>
                  <div className="stat-card-sub">{formatCurrency(feeStats.totalCollected)}</div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
