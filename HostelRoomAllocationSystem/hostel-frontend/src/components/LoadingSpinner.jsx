/** Loading spinner component. */
export default function LoadingSpinner({ size = 'normal', text = 'Loading...' }) {
  return (
    <div className="loading-state">
      <div className={`spinner ${size === 'large' ? 'spinner-lg' : ''}`} />
      {text && <span>{text}</span>}
    </div>
  );
}
