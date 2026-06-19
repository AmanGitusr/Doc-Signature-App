import React from 'react';
import { describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginPage from '../pages/LoginPage';

const loginMock = vi.fn();

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => ({ login: loginMock })
}));

const navigateMock = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => navigateMock
  };
});

describe('LoginPage', () => {
  it('submits credentials', async () => {
    loginMock.mockResolvedValueOnce({});
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    await userEvent.type(screen.getByLabelText(/email/i), 'owner@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    await userEvent.click(screen.getByRole('button', { name: /login/i }));

    expect(loginMock).toHaveBeenCalledWith({ email: 'owner@example.com', password: 'password123' });
  });
});
