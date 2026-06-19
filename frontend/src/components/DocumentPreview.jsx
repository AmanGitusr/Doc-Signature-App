import React, { useEffect, useState } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import { api } from '../api/client';
import workerSrc from '../../node_modules/react-pdf/node_modules/pdfjs-dist/build/pdf.worker.min.mjs?url';

pdfjs.GlobalWorkerOptions.workerSrc = workerSrc;

export default function DocumentPreview({ downloadUrl, authenticated = true }) {
  const [file, setFile] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    let objectUrl = null;
    const load = async () => {
      try {
        setError('');
        const response = authenticated ? await fetch(`${api.baseUrl}${downloadUrl}`, {
          headers: api.getToken() ? { Authorization: `Bearer ${api.getToken()}` } : {}
        }) : await fetch(`${api.baseUrl}${downloadUrl}`);
        if (!response.ok) {
          throw new Error('Unable to load PDF preview');
        }
        const blob = await response.blob();
        if (active) {
          objectUrl = URL.createObjectURL(blob);
          setFile(objectUrl);
        }
      } catch (err) {
        if (active) {
          setError(err.message);
        }
      }
    };
    load();
    return () => {
      active = false;
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [downloadUrl, authenticated]);

  if (error) {
    return <div className="panel error">{error}</div>;
  }

  if (!file) {
    return <div className="panel">Loading preview...</div>;
  }

  return (
    <Document file={file} loading={<div className="panel">Loading preview...</div>} onLoadError={(err) => setError(err.message)}>
      <Page pageNumber={1} width={700} renderTextLayer={false} renderAnnotationLayer={false} />
    </Document>
  );
}
