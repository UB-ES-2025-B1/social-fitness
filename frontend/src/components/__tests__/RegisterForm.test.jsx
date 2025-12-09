import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import RegisterForm from '../RegisterForm'

describe('RegisterForm', () => {
  it('llama a onChange por campo y a onSubmit al enviar', async () => {
    const onChange = vi.fn()
    const onSubmit = vi.fn((e) => e.preventDefault())

    render(
      <RegisterForm
        username=""
        email=""
        password=""
        confirm=""
        errors={{}}
        onChange={onChange}
        onSubmit={onSubmit}
        submitting={false}
      />
    )

    // use label queries which are more robust and match the Spanish UI
    await userEvent.type(
      screen.getByLabelText(/nombre de usuario/i),
      'devtest'
    )
    await userEvent.type(
      screen.getByLabelText(/correo electrónico|email/i),
      'devtest@example.com'
    )
    // 'Contraseña' and 'Confirmar contraseña' are different labels; use placeholders to avoid toggle button collision
    await userEvent.type(
      screen.getByPlaceholderText(/Elige una contraseña/i),
      'devtest123'
    )
    await userEvent.type(
      screen.getByPlaceholderText(/Repite tu contraseña/i),
      'devtest123'
    )

    // Al menos se llamó para cada campo alguna vez con la clave correcta
    const calls = onChange.mock.calls.map(([field]) => field)
    expect(calls).toEqual(
      expect.arrayContaining(['username', 'email', 'password', 'confirm'])
    )

    await userEvent.click(
      screen.getByRole('button', { name: /crear cuenta|crear cuenta/i })
    )
    expect(onSubmit).toHaveBeenCalledTimes(1)
  })
})
