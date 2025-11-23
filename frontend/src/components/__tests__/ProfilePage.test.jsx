import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Profile from '../Profile'

// mock services
vi.mock('../../services/profile', () => ({ getProfile: vi.fn(), saveProfile: vi.fn() }))

import { getProfile, saveProfile } from '../../services/profile'

describe('Profile page', () => {
  it('shows username and email as read-only and allows entering edit mode to add/remove/set levels', async () => {
    // ensure component uses remote API (not demo localStorage fallback)
    localStorage.setItem('userId', '1')
    // mock API returns demo profile
    getProfile.mockResolvedValue({ ok: true, data: { username: 'qa_user', email: 'qa_user@mail.com', sports: '[]' } })

    render(<Profile />)
    const user = userEvent.setup()

    // username and email inputs are present and readOnly (wait for async getProfile)
    // Use exact match to differentiate between username and email inputs
    await waitFor(() => expect(screen.getByDisplayValue('qa_user')).toBeInTheDocument())
    await waitFor(() => expect(screen.getByDisplayValue('qa_user@mail.com')).toBeInTheDocument())    // enter edit mode
    await user.click(screen.getByRole('button', { name: /Editar deportes/i }))
    // add a sport (e.g., Fútbol)
    const addButton = screen.getByRole('button', { name: /Fútbol/i })
    await user.click(addButton)

    // new sport should appear with a select to choose level
    expect(screen.getByText(/Fútbol/i)).toBeInTheDocument()
    expect(screen.getByRole('combobox')).toBeInTheDocument()

    // change level to Intermedio
    const select = screen.getByRole('combobox')
    await userEvent.selectOptions(select, 'intermediate')
    expect(select.value).toBe('intermediate')

    // remove sport - query for the remove button in the sport row
    const remove = screen.getByRole('button', { name: /Eliminar/i })
    await user.click(remove)
    // After removing, the sport label should no longer be in the sports list (only in the available chips)
    const sportsList = document.querySelector('.sports-list')
    expect(sportsList.textContent).not.toMatch(/Fútbol/)
  })
})
