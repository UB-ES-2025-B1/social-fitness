import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import LoginForm from '../LoginForm'

describe('LoginForm', () => {
  it('dispara los handlers al teclear y submit', async () => {
    const onUsernameChange = vi.fn()
    const onPasswordChange = vi.fn()
    const onSubmit = vi.fn((e) => e.preventDefault())

    render(
      <LoginForm
        username=""
        password=""
        errors={{}}
        onUsernameChange={onUsernameChange}
        onPasswordChange={onPasswordChange}
        onSubmit={onSubmit}
        submitting={false}
      />
    )

    await userEvent.type(
      screen.getByPlaceholderText(/enter your username/i),
      'devtest'
    )
    await userEvent.type(
      screen.getByPlaceholderText(/enter your password/i),
      'devtest123'
    )

    // Se llaman al escribir (muchas veces, una por tecla)
    expect(onUsernameChange).toHaveBeenCalled()
    expect(onPasswordChange).toHaveBeenCalled()

    // Click en "Sign in" -> onSubmit
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }))
    expect(onSubmit).toHaveBeenCalledTimes(1)
  })
})
