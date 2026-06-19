import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api/client';
import DocumentPreview from '../components/DocumentPreview';

export default function PublicSigningPage() {
  const { token } = useParams();
  const [data, setData] = useState(null);
  const [action, setAction] = useState('SIGNED');
  const [signatureText, setSignatureText] = useState('');
  const [rejectionReason, setRejectionReason] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.getPublicSigning(token)
      .then(setData)
      .catch((err) => setError(err.message));
  }, [token]);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = { action, signatureText, rejectionReason };
      const response = await api.finalizePublicSigning(token, payload);
      setData((current) => current ? { ...current, signatureRequest: response, status: response.status } : current);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  if (error) {
    return <div className="panel error">{error}</div>;
  }

  if (!data) {
    return <div className="panel">Loading signing link...</div>;
  }

  return (
    <div className="stack">
      <section className="panel">
        <h1>{data.title}</h1>
        <p className="muted">{data.originalFilename}</p>
        <span className={`badge status-${data.status.toLowerCase()}`}>{data.status}</span>
      </section>

      <section className="detail-grid">
        <div className="panel">
          <h2>Preview</h2>
          <DocumentPreview downloadUrl={data.downloadUrl} authenticated={false} />
        </div>

        <form className="panel" onSubmit={submit}>
          <h2>Finalize signing</h2>
          <label>
            Action
            <select value={action} onChange={(e) => setAction(e.target.value)}>
              <option value="SIGNED">Sign</option>
              <option value="REJECTED">Reject</option>
            </select>
          </label>
          <label>
            Signature text
            <input value={signatureText} onChange={(e) => setSignatureText(e.target.value)} placeholder="Approved electronically" />
          </label>
          <label>
            Rejection reason
            <textarea value={rejectionReason} onChange={(e) => setRejectionReason(e.target.value)} rows="4" />
          </label>
          <button disabled={saving}>{saving ? 'Submitting...' : 'Finalize'}</button>
        </form>
      </section>
    </div>
  );
}
