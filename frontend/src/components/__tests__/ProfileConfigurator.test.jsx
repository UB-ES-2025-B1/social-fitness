import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'
import ProfileConfigurator from '../ProfileConfigurator.jsx'

describe('ProfileConfigurator (básico)', () => {
  const user = userEvent.setup()

  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('valida que no se puede avanzar sin seleccionar ningún deporte', async () => {
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})
    render(<ProfileConfigurator />)

  // the UI shows "Siguiente" in Spanish; accept both Catalan/Spanish variants
      await user.click(screen.getByRole('button', { name: /Següent|Siguiente/i }))

      // The component displays an inline error banner instead of calling window.alert
      expect(screen.getByText(/Selecciona al menos un deporte/i)).toBeInTheDocument()
      expect(
        screen.getByRole('heading', { name: /Configura tu perfil deportivo/i })
      ).toBeInTheDocument()
  })

  it('flujo feliz: selecciona deportes, define niveles y emite el payload al completar', async () => {
    const onComplete = vi.fn()
    render(<ProfileConfigurator onComplete={onComplete} />)

    // Paso 1
  // accept localized sport names (with/without diacritics)
  await user.click(screen.getByRole('button', { name: /Fútbol|Futbol/i }))
  await user.click(screen.getByRole('button', { name: /Bàsquet|Básquet|Basquet/i }))
  await user.click(screen.getByRole('button', { name: /Següent|Siguiente/i }))

    // Paso 2: acotar al bloque de Futbol y elegir "Intermedi" SOLO para Futbol
  const futbolHeading = screen.getByRole('heading', { name: /Fútbol|Futbol/i, level: 3 })
    const futbolSection = futbolHeading.closest('section')
    expect(futbolSection).not.toBeNull()                 // <-- garantizamos que existe

    const futbolScope = within(futbolSection)
  await user.click(futbolScope.getByRole('button', { name: /Intermedi|Intermedio/i }))

    // Completar
  await user.click(screen.getByRole('button', { name: /Completar|Finalizar|Terminar/i }))

    // onComplete recibe el payload esperado
    expect(onComplete).toHaveBeenCalledTimes(1)
    const payload = onComplete.mock.calls[0][0]
    expect(payload).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ id: 'football', level: 'intermediate' }),
        expect.objectContaining({ id: 'basketball', level: null }),
      ])
    )
  })
})
