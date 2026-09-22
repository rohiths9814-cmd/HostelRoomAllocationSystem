/**
 * Search bar with a magnifying glass icon.
 * Supports both:
 *   - Controlled: value + onChange
 *   - Callback:   onSearch (called on each keystroke)
 */
import { useState } from 'react';

export default function SearchBar({ value, onChange, onSearch, placeholder = 'Search...' }) {
  const [internal, setInternal] = useState(value || '');
  const displayValue = value !== undefined ? value : internal;

  function handleChange(e) {
    const v = e.target.value;
    setInternal(v);
    if (onChange) onChange(v);
    if (onSearch) onSearch(v);
  }

  return (
    <div className="search-bar">
      <span className="search-bar-icon">🔍</span>
      <input
        type="text"
        value={displayValue}
        onChange={handleChange}
        placeholder={placeholder}
        aria-label={placeholder}
      />
    </div>
  );
}

