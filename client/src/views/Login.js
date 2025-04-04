// IBM login page web
import React from 'react';
import {
  Tile,
  Button,
  InlineNotification,
  Form,
  TextInput,
  ToastNotification,
} from 'carbon-components-react';
import { Link, useHistory } from 'react-router-dom';

export default function Login({ error }) {
  const history = useHistory();
  return (
    <>
      <h1>Welcome</h1>
      {error && (
        <InlineNotification
          lowContrast
          kind='error'
          title='Error'
          subtitle={error}
        />
      )}

      <Form className='login-form'>
        <TextInput
          autoComplete='off'
          id='email'
          labelText='Email'
          placeholder='username@gmail.com'
          required
        />
        <TextInput
          id='password'
          placeholder='password'
          labelText='Password'
          type='password'
        />
        <Link className='forgot-password-link ' to='/auth/forgot-password'>
          Forgot password?
        </Link>
        <Button
          className='login-button'
          kind='primary'
          renderIcon={() => <i className='fas fa-sign-in-alt'></i>}
          onClick={() => history.push('/admin/dashboard')}
        >
          Sign in
        </Button>
        <p className='signup-link'>
          Don't have an account yet? <Link to='/auth/register'>Sign up</Link>
        </p>
      </Form>
    </>
  );
}
