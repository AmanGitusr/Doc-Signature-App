import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client';

export default function DashboardPage() {
  const [documents, setDocuments] = useState([]);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);
  const navigate = useNavigate();

  const loadDocuments = async () => {
    try {
      const data = await api.listDocuments();
      setDocuments(data);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    loadDocuments();
  }, []);

  const upload = async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const file = form.file.files[0];
    const title = form.title.value;
    const description = form.description.value;
    const data = new FormData();
    data.append('file', file);
    data.append('title', title);
    data.append('description', description);
    setUploading(true);
    setError('');
    try {
      const response = await api.uploadDocument(data);
      navigate(`/documents/${response.document.id}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="stack">
      <section className="hero">
        <div>
          <h1>Document workspace</h1>
          <p>Upload PDFs, request signatures, and track every state transition with an audit trail.</p>
        </div>
        <form className="panel upload-card" onSubmit={upload}>
          <h2>Upload PDF</h2>
          {error ? <div className="error">{error}</div> : null}
          <input name="title" placeholder="Document title" required />
          <textarea name="description" placeholder="Description" rows="3" />
          <input name="file" type="file" accept="application/pdf,.pdf" required />
          <button disabled={uploading}>{uploading ? 'Uploading...' : 'Upload document'}</button>
        </form>
      </section>

      <section>
        <h2>Documents</h2>
        <div className="cards">
          {documents.map((doc) => (
            <article key={doc.id} className="panel doc-card">
              <div className="row">
                <div>
                  <h3>{doc.title}</h3>
                  <p className="muted">{doc.originalFilename}</p>
                </div>
                <span className={`badge status-${doc.status.toLowerCase()}`}>{doc.status}</span>
              </div>
              <p className="muted">Signer: {doc.signerEmail || 'Not requested yet'}</p>
              <Link to={`/documents/${doc.id}`}>Open</Link>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
