import React from 'react';
import { describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import PublicSigningPage from '../pages/PublicSigningPage';

const apiMock = vi.hoisted(() => ({
  getPublicSigning: vi.fn(),
  finalizePublicSigning: vi.fn()
}));

vi.mock('../api/client', () => ({
  api: apiMock
}));

vi.mock('../components/DocumentPreview', () => ({
  default: () => <div data-testid="preview" />
}));

describe('PublicSigningPage', () => {
  it('finalizes signing', async () => {
    apiMock.getPublicSigning.mockResolvedValueOnce({
      title: 'Agreement',
      originalFilename: 'agreement.pdf',
      status: 'PENDING_SIGNATURE',
      downloadUrl: '/api/public/signatures/token/download'
    });
    apiMock.finalizePublicSigning.mockResolvedValueOnce({
      status: 'SIGNED'
    });

    render(
      <MemoryRouter initialEntries={['/sign/token']}>
        <Routes>
          <Route path="/sign/:token" element={<PublicSigningPage />} />
        </Routes>
      </MemoryRouter>
    );

    await screen.findByText('Agreement');
    await userEvent.click(screen.getByRole('button', { name: /finalize/i }));

    expect(apiMock.finalizePublicSigning).toHaveBeenCalled();
  });
});
