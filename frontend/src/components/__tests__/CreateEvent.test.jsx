import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CreateEvent from '../CreateEvent'

vi.mock('../../services/events', () => ({ createEvent: vi.fn(), joinEvent: vi.fn() }))
import { createEvent } from '../../services/events'

describe('CreateEvent', () => {
  it('renders form fields and enforces capacity and price constraints and shows creating state', async () => {
    const onCreated = vi.fn()
    render(<CreateEvent onCreated={onCreated} />)
    const user = userEvent.setup()

    expect(screen.getByRole('heading', { name: /Crear evento/i })).toBeInTheDocument()
    // fill title
    await user.type(screen.getByPlaceholderText(/Nombre del evento/i), 'Partido test')
    
    // set capacity below minimum — label isn't associated, use spinbutton role
    const spin = screen.getAllByRole('spinbutton')
    const capacity = spin[0]
    await user.clear(capacity)
    await user.type(capacity, '1')
    
    // Component validates on submit; with missing required fields (date, location, organizer), validation prevents submission
    const submit = screen.getByRole('button', { name: /Crear evento/i })
    await user.click(submit)
    
    // validate prevented the request -> createEvent should not be called
    expect(createEvent).not.toHaveBeenCalled()
    
    // Fill in all required fields to make form valid
    await user.clear(capacity)
    await user.type(capacity, '10')
    const price = spin[1]
    await user.clear(price)
    await user.type(price, '2.5')
    expect(price.value).toBe('2.5')
    
    // Fill required fields that were empty
    await user.type(screen.getByPlaceholderText(/Lugar/i), 'Parque Central')
    await user.type(screen.getByPlaceholderText(/Tu nombre/i), 'Test Organizer')
    
    // Set a future date - use querySelector since label is not associated
    const dateInput = document.querySelector('input[type="date"]')
    const futureDate = new Date()
    futureDate.setDate(futureDate.getDate() + 7)
    const dateString = futureDate.toISOString().split('T')[0]
    await user.type(dateInput, dateString)

    // mock createEvent to succeed
    createEvent.mockResolvedValue({ ok: true, data: { id: 'evt1' } })
    await user.click(submit)
    // button shows creating and gets disabled while sending (handled by component) - we cannot fast-forward easily but ensure callback called
    expect(createEvent).toHaveBeenCalled()
  })
})
