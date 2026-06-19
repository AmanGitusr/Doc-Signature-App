import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api/client';
import DocumentPreview from '../components/DocumentPreview';
import SignatureOverlay from '../components/SignatureOverlay';

export default function DocumentDetailPage() {
  const { id } = useParams();
  const [document, setDocument] = useState(null);
  const [signature, setSignature] = useState(null);
  const [audit, setAudit] = useState([]);
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const [placement, setPlacement] = useState({ x: 12, y: 18 });

  useEffect(() => {
    const load = async () => {
      try {
        const [doc, auditEvents] = await Promise.all([api.getDocument(id), api.getAudit(id)]);
        setDocument(doc);
        setAudit(auditEvents);
        try {
          const req = await api.getSignature(id);
          setSignature(req);
        } catch {
          setSignature(null);
        }
      } catch (err) {
        setError(err.message);
      }
    };
    load();
  }, [id]);

  const createSignature = async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    setCreating(true);
    setError('');
    try {
      const payload = {
        documentId: Number(id),
        signerName: form.signerName.value,
        signerEmail: form.signerEmail.value,
        pageNumber: Number(form.pageNumber.value),
        xPercent: placement.x,
        yPercent: placement.y,
        signatureText: form.signatureText.value
      };
      const response = await api.createSignature(payload);
      setSignature(response);
      const updated = await api.getDocument(id);
      setDocument(updated);
    } catch (err) {
      setError(err.message);
    } finally {
      setCreating(false);
    }
  };

  if (error) {
    return <div className="panel error">{error}</div>;
  }

  if (!document) {
    return <div className="panel">Loading document...</div>;
  }

  return (
    <div className="stack">
      <section className="panel">
        <div className="row">
          <div>
            <h1>{document.title}</h1>
            <p className="muted">{document.originalFilename}</p>
          </div>
          <span className={`badge status-${document.status.toLowerCase()}`}>{document.status}</span>
        </div>
        <p>{document.description || 'No description provided.'}</p>
      </section>

      <section className="detail-grid">
        <div className="panel">
          <h2>Preview</h2>
          <DocumentPreview downloadUrl={`/api/docs/${id}/download`} />
          <SignatureOverlay value={placement} onChange={setPlacement} />
        </div>

        <form className="panel" onSubmit={createSignature}>
          <h2>Request signature</h2>
          <label>
            Signer name
            <input name="signerName" defaultValue={signature?.signerName || ''} required />
          </label>
          <label>
            Signer email
            <input name="signerEmail" type="email" defaultValue={signature?.signerEmail || ''} required />
          </label>
          <label>
            Page number
            <input name="pageNumber" type="number" min="1" defaultValue={signature?.pageNumber || 1} required />
          </label>
          <label>
            Signature text
            <input name="signatureText" defaultValue={signature?.signatureText || 'Approved electronically'} />
          </label>
          <div className="muted">Stamp position: {placement.x.toFixed(1)}%, {placement.y.toFixed(1)}%</div>
          <button disabled={creating}>{creating ? 'Saving...' : 'Create signing request'}</button>
          {signature ? (
            <div className="token-box">
              <div className="muted">Public signing link</div>
              <code>{`${window.location.origin}/sign/${signature.token}`}</code>
            </div>
          ) : null}
        </form>
      </section>

      <section className="panel">
        <h2>Audit log</h2>
        <div className="audit-list">
          {audit.map((event) => (
            <div key={event.id} className="audit-item">
              <strong>{event.action}</strong>
              <span className="muted">{event.details}</span>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
