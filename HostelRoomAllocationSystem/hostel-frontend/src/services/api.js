/**
 * API Service Layer
 * -----------------
 * Every API call in the React app goes through this file.
 * Components never write fetch() themselves.
 *
 * The base URL comes from .env (VITE_API_BASE_URL).
 * Every function returns a parsed JSON response or throws an error.
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Optional shared secret.
 *
 * Only needed if the backend was started with an API_KEY environment
 * variable set. Locally it is undefined and no header is sent.
 *
 * Be aware: anything in a VITE_ variable is baked into the JavaScript
 * that every visitor downloads, so this is NOT a real secret. It keeps
 * random bots out; it does not make the API private.
 */
const API_KEY = import.meta.env.VITE_API_KEY;

/**
 * Generic fetch wrapper.
 * - Adds Content-Type header for JSON bodies
 * - Adds the API key header when one is configured
 * - Parses the JSON response
 * - Throws with a human-readable message on errors
 */
async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;

  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(API_KEY ? { 'X-Api-Key': API_KEY } : {}),
      ...(options.headers || {}),
    },
  };

  // Don't send Content-Type for GET/DELETE with no body
  if (!config.body) {
    delete config.headers['Content-Type'];
  }

  try {
    const response = await fetch(url, config);

    // Try to parse JSON even on error responses (backend sends error messages as JSON)
    let data;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      const text = await response.text();
      data = { success: false, message: text || 'Unknown error' };
    }

    if (!response.ok) {
      // Use the backend's message if available
      const errorMessage = data.message || `HTTP error ${response.status}`;
      const error = new Error(errorMessage);
      error.status = response.status;
      error.data = data;
      throw error;
    }

    return data;
  } catch (error) {
    // Network errors (server not running, CORS blocked, etc.)
    if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
      throw new Error('Unable to connect to the server. Make sure the Java backend is running on port 8080.');
    }
    throw error;
  }
}

// ============================================================
// AUTH
// ============================================================

export async function login(username, password) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
}

// ============================================================
// DASHBOARD
// ============================================================

export async function getDashboardStats() {
  return request('/dashboard');
}

// ============================================================
// STUDENTS
// ============================================================

export async function getStudents(search = '') {
  const query = search ? `?search=${encodeURIComponent(search)}` : '';
  return request(`/students${query}`);
}

export async function getStudent(id) {
  return request(`/students/${id}`);
}

export async function getUnallocatedStudents() {
  return request('/students/unallocated');
}

export async function createStudent(data) {
  return request('/students', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function updateStudent(id, data) {
  return request(`/students/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export async function deleteStudent(id) {
  return request(`/students/${id}`, {
    method: 'DELETE',
  });
}

// ============================================================
// BLOCKS
// ============================================================

export async function getBlocks() {
  return request('/blocks');
}

export async function createBlock(data) {
  return request('/blocks', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// ============================================================
// ROOMS
// ============================================================

export async function getRooms(status = '') {
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  return request(`/rooms${query}`);
}

export async function getRoom(id) {
  return request(`/rooms/${id}`);
}

export async function getAvailableRooms() {
  return request('/rooms/available');
}

export async function getRoomOccupants(id) {
  return request(`/rooms/${id}/occupants`);
}

export async function createRoom(data) {
  return request('/rooms', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function updateRoom(id, data) {
  return request(`/rooms/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export async function setRoomMaintenance(id, underMaintenance) {
  return request(`/rooms/${id}/maintenance`, {
    method: 'PUT',
    body: JSON.stringify({ underMaintenance }),
  });
}

export async function deleteRoom(id) {
  return request(`/rooms/${id}`, {
    method: 'DELETE',
  });
}

// ============================================================
// ALLOCATIONS
// ============================================================

export async function getActiveAllocations() {
  return request('/allocations');
}

export async function getAllAllocations() {
  return request('/allocations/all');
}

export async function getStudentHistory(studentId) {
  return request(`/allocations/student/${studentId}`);
}

export async function allocateRoom(studentId, roomId) {
  return request('/allocations', {
    method: 'POST',
    body: JSON.stringify({ studentId, roomId }),
  });
}

export async function transferStudent(studentId, newRoomId) {
  return request('/allocations/transfer', {
    method: 'PUT',
    body: JSON.stringify({ studentId, newRoomId }),
  });
}

export async function checkoutStudent(studentId) {
  return request('/allocations/checkout', {
    method: 'PUT',
    body: JSON.stringify({ studentId }),
  });
}

// ============================================================
// COMPLAINTS
// ============================================================

export async function getComplaints(status = '') {
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  return request(`/complaints${query}`);
}

export async function createComplaint(data) {
  return request('/complaints', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function updateComplaintStatus(id, status) {
  return request(`/complaints/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ status }),
  });
}

export async function deleteComplaint(id) {
  return request(`/complaints/${id}`, {
    method: 'DELETE',
  });
}

// ============================================================
// FEES
// ============================================================

export async function getFees(status = '') {
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  return request(`/fees${query}`);
}

export async function getStudentFees(studentId) {
  return request(`/fees/student/${studentId}`);
}

export async function addFee(data) {
  return request('/fees', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function markFeePaid(id) {
  return request(`/fees/${id}/pay`, {
    method: 'PUT',
  });
}

export async function deleteFee(id) {
  return request(`/fees/${id}`, {
    method: 'DELETE',
  });
}

// ============================================================
// WAITING LIST
// ============================================================

export async function getWaitingList() {
  return request('/waiting-list');
}

export async function addToWaitingList(data) {
  return request('/waiting-list', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function removeFromWaitingList(id) {
  return request(`/waiting-list/${id}`, {
    method: 'DELETE',
  });
}

// ============================================================
// REPORTS
// ============================================================

export async function getReportSummary() {
  return request('/reports/summary');
}

export async function getBlockVacancy() {
  return request('/reports/block-vacancy');
}

export async function getComplaintStats() {
  return request('/reports/complaint-stats');
}

export async function getFeeStats() {
  return request('/reports/fee-stats');
}
