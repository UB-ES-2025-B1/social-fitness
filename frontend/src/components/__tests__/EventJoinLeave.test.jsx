import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

vi.mock('../../services/events', () => ({
  listEvents: vi.fn(),
  joinEvent: vi.fn(),
  leaveEvent: vi.fn(),
}))
import { listEvents, joinEvent, leaveEvent } from '../../services/events'
import EventExplorer from '../EventExplorer'

const FAKE_EVENTS = [
  { id: 'e1', title: 'E1', sport: 'Fútbol', date: 'hoy', time: '18:00', location: 'X', organizer: 'A', participants: 1, capacity: 10, price: 0, image: '' },
  { id: 'e2', title: 'E2', sport: 'Tenis', date: 'mañana', time: '10:00', location: 'Y', organizer: 'B', participants: 0, capacity: 5, price: 1, image: '' },
]

describe('Event join/leave flows', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // make listEvents return FAKE_EVENTS
    listEvents.mockResolvedValue({ ok: true, data: FAKE_EVENTS })
    // ensure joined storage is clean
    try { localStorage.removeItem('joinedEvents:anon') } catch (e) {}
  })

  it('shows joined events group collapsed by default and can expand', async () => {
    joinEvent.mockResolvedValue({ ok: true })
    render(<EventExplorer />)
    const user = userEvent.setup()

    // wait for events to load
    await waitFor(() => expect(screen.getByText('E1')).toBeInTheDocument())

    // before joining, no joined group
    expect(screen.queryByText(/Tus eventos/)).not.toBeInTheDocument()

    // join first event
    const joinButtons = screen.getAllByRole('button', { name: /Unirse/i })
    await user.click(joinButtons[0])
    await waitFor(() => expect(joinEvent).toHaveBeenCalledWith('e1'))

    // joined group header present and collapsed
    expect(screen.getByText(/Tus eventos \(1\)/i)).toBeInTheDocument()
    // content is collapsed by default -> the joined event should not be visible inside group until expanded
    expect(screen.queryByText('E1', { selector: '.joined-group .group-list *' })).not.toBeInTheDocument()

    // expand
    await user.click(screen.getByRole('button', { name: /Tus eventos \(1\)/i }))
    // after expand, joined event visible in the joined section
    await waitFor(() => expect(screen.getByText('E1')).toBeInTheDocument())
  })

  it('allows leaving an event and removes it from joined section; shows error on failure', async () => {
    // set initial joined state by mocking joinEvent then leaving
    joinEvent.mockResolvedValue({ ok: true })
    leaveEvent.mockResolvedValueOnce({ ok: true }).mockResolvedValueOnce({ ok: false })

    render(<EventExplorer />)
    const user = userEvent.setup()
    await waitFor(() => expect(screen.getByText('E1')).toBeInTheDocument())

    // join an event
    await user.click(screen.getAllByRole('button', { name: /Unirse/i })[0])
    await waitFor(() => expect(joinEvent).toHaveBeenCalled())

    // expand joined group
    await user.click(screen.getByRole('button', { name: /Tus eventos \(1\)/i }))
    await waitFor(() => expect(screen.getByText('E1')).toBeInTheDocument())

    // click leave button (now visible after expanding the group)
    const joinedSection = screen.getByText(/Tus eventos \(1\)/i).closest('section')
    await waitFor(() => expect(within(joinedSection).getByText(/Salir de evento/i)).toBeTruthy())
    const leaveBtn = within(joinedSection).getByText(/Salir de evento/i)
    await user.click(leaveBtn)
    await waitFor(() => expect(leaveEvent).toHaveBeenCalledWith('e1'))
    
    // after leaving, event should move back to other events
    await waitFor(() => expect(screen.queryByText('E1')).toBeInTheDocument())
    const otherGroup = screen.getByText(/Otros eventos/i).closest('section')
    expect(otherGroup).toBeTruthy()

    // Test leave failure: rejoin and mock failure
    leaveEvent.mockResolvedValue({ ok: false })
    await user.click(screen.getAllByRole('button', { name: /Unirse/i })[0])
    await waitFor(() => expect(joinEvent).toHaveBeenCalled())
    
    // try to leave - this should show error
    const leaveButtons = screen.getAllByText(/Salir de evento/i)
    await user.click(leaveButtons[0])
    await waitFor(() => expect(screen.getByText(/No se pudo salir del evento|Network error/i)).toBeTruthy())
  })
})
