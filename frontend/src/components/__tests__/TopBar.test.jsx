import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import TopBar from '../TopBar'

describe('TopBar', () => {
  it('renders tabs and calls onChange with correct modes', async () => {
    const onChange = vi.fn()
    render(<TopBar mode="explore" onChange={onChange} />)

    const user = userEvent.setup()
    expect(screen.getByRole('button', { name: /Explorar/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Crear evento/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Perfil/i })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /Perfil/i }))
    expect(onChange).toHaveBeenCalledWith('profile')

    await user.click(screen.getByRole('button', { name: /Crear evento/i }))
    expect(onChange).toHaveBeenCalledWith('create')
  })
})
