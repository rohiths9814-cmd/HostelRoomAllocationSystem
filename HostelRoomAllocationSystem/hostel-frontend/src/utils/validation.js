/**
 * Frontend validation helpers.
 *
 * These mirror the regex rules in the Java backend's Validation.java.
 * Frontend validation is for user experience only — the backend
 * validates again before touching the database.
 */

export function isValidRegisterNumber(value) {
  // 2 digits, 2-4 uppercase letters, 3 digits. Example: 26CSE001
  return /^\d{2}[A-Z]{2,4}\d{3}$/.test(value);
}

export function isValidName(value) {
  // Starts with a letter, allows letters, spaces, dots. 2-50 chars.
  return /^[A-Za-z][A-Za-z \.]{1,49}$/.test(value);
}

export function isValidDepartment(value) {
  // Letters only, 2-30 chars.
  return /^[A-Za-z]{2,30}$/.test(value);
}

export function isValidYear(value) {
  const year = parseInt(value, 10);
  return !isNaN(year) && year >= 1 && year <= 4;
}

export function isValidGender(value) {
  return value === 'MALE' || value === 'FEMALE';
}

export function isValidPhone(value) {
  // Indian mobile: starts with 6-9, exactly 10 digits
  return /^[6-9]\d{9}$/.test(value);
}

export function isValidEmail(value) {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value);
}

export function isValidRoomNumber(value) {
  // Letters, hyphen, 3 digits. Example: A-101
  return /^[A-Za-z]+-\d{3}$/.test(value);
}

export function isValidCapacity(value) {
  const cap = parseInt(value, 10);
  return !isNaN(cap) && cap >= 1 && cap <= 6;
}

/**
 * Validates a student form and returns an errors object.
 * Each key is a field name, the value is the error message.
 * If the returned object is empty, the form is valid.
 */
export function validateStudentForm(data) {
  const errors = {};

  if (!data.registerNumber || !isValidRegisterNumber(data.registerNumber)) {
    errors.registerNumber = 'Must be like 26CSE001 (2 digits, 2-4 letters, 3 digits)';
  }
  if (!data.name || !isValidName(data.name)) {
    errors.name = 'Must start with a letter, 2-50 characters';
  }
  if (!data.department || !isValidDepartment(data.department)) {
    errors.department = 'Letters only, 2-30 characters';
  }
  if (!isValidYear(data.year)) {
    errors.year = 'Must be between 1 and 4';
  }
  if (!isValidGender(data.gender)) {
    errors.gender = 'Select a gender';
  }
  if (!data.phone || !isValidPhone(data.phone)) {
    errors.phone = '10 digits starting with 6, 7, 8 or 9';
  }
  if (!data.email || !isValidEmail(data.email)) {
    errors.email = 'Enter a valid email address';
  }

  return errors;
}
