import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'

vi.mock('../../services/events', () => {
  return {
    listEvents: vi.fn(),
    joinEvent: vi.fn(),
  }
})

import { listEvents, joinEvent } from '../../services/events'
import EventExplorer from '../EventExplorer.jsx'

const FAKE_EVENTS = [
  {
    id: 'e1',
    title: 'Partido amistoso en el parque',
    sport: 'Fútbol',
    date: 'mié, 8 oct',
    time: '18:00',
    location: 'Parque Central',
    organizer: 'Carlos M.',
    participants: 8,
    capacity: 22,
    price: 5,
    image: 'http://example.com/img1.jpg',
  },
  {
    id: 'e2',
    title: 'Torneo 3vs3',
    sport: 'Básquet',
    date: 'jue, 9 oct',
    time: '16:30',
    location: 'Pista Municipal',
    organizer: 'Ana S.',
    participants: 6,
    capacity: 12,
    price: 8,
    image: 'http://example.com/img2.jpg',
  },
]

describe('EventExplorer (básico)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('carga y muestra los eventos al montar', async () => {
    // la API devuelve ok con una lista de eventos
    listEvents.mockResolvedValueOnce({ ok: true, data: FAKE_EVENTS })

    render(<EventExplorer />)

    // encabezado y controles básicos
    expect(screen.getByRole('heading', { name: /Explorar Eventos/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Buscar/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Filtrar resultados/i })).toBeInTheDocument()

    // espera a que se pinten los eventos
    await waitFor(() => {
      expect(screen.getByText('Partido amistoso en el parque')).toBeInTheDocument()
      expect(screen.getByText('Torneo 3vs3')).toBeInTheDocument()
    })

    // comprobamos que se llamó a la API una vez en el montaje
    expect(listEvents).toHaveBeenCalledTimes(1)
  })

  it('envía la query al pulsar "Buscar"', async () => {
    listEvents.mockResolvedValue({ ok: true, data: FAKE_EVENTS })

    render(<EventExplorer />)

    const input = screen.getByPlaceholderText(/Buscar eventos, deportes o ubicaciones/i)
    await userEvent.clear(input)
    await userEvent.type(input, 'parque')

    await userEvent.click(screen.getByRole('button', { name: /Buscar/i }))

    // la última llamada a listEvents debe incluir q: 'parque'
    expect(listEvents).toHaveBeenLastCalledWith(expect.objectContaining({ q: 'parque' }))
  })

  it('abre el modal de filtros y aplica filtros al pulsar "Guardar"', async () => {
    listEvents.mockResolvedValue({ ok: true, data: FAKE_EVENTS })
    render(<EventExplorer />)

    // abrir modal
    await userEvent.click(screen.getByRole('button', { name: /Filtrar resultados/i }))
    // dentro del modal: seleccionar un deporte y escribir ubicación
    await userEvent.click(screen.getByRole('button', { name: 'Fútbol' })) // chip que se activa
    const ubicacion = screen.getByPlaceholderText(/Buscar por población/i)
    await userEvent.type(ubicacion, 'Barcelona')

    // guardar filtros
    await userEvent.click(screen.getByRole('button', { name: /Guardar/i }))

    // se vuelve a pedir a la API con los filtros aplicados
    expect(listEvents).toHaveBeenLastCalledWith(
      expect.objectContaining({
        sports: expect.arrayContaining(['Fútbol']),
        location: 'Barcelona',
      })
    )
  })

  it('permite "Unirse" a un evento y vuelve a recargar la lista', async () => {
    // 1ª carga
    listEvents.mockResolvedValueOnce({ ok: true, data: FAKE_EVENTS })
    // join ok
    joinEvent.mockResolvedValueOnce({ ok: true })
    // recarga tras unirse
    listEvents.mockResolvedValueOnce({ ok: true, data: FAKE_EVENTS })

    render(<EventExplorer />)

    // espera a que aparezcan los eventos
    await waitFor(() => {
      expect(screen.getByText('Partido amistoso en el parque')).toBeInTheDocument()
    })

    // pulsa el botón "Unirse" del primer evento
    const joinButtons = screen.getAllByRole('button', { name: /Unirse/i })
    await userEvent.click(joinButtons[0])

    // se llamó a joinEvent con el id del primer evento
    expect(joinEvent).toHaveBeenCalledWith('e1')

    // tras unirse, vuelve a cargar la lista
    expect(listEvents).toHaveBeenCalledTimes(2)
  })
})
