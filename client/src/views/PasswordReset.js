import React from 'react';
import { TextInput, Form, Button } from 'carbon-components-react';
import { useHistory } from 'react-router-dom';

export default function PasswordReset() {
  const history = useHistory();
  return (
    <>
      <h3>Password reset</h3>
      <Form>
        <TextInput id='password' labelText='Enter new password' />
        <TextInput id='confirm-password' labelText='Confirm password' />
        <Button
          className='login-button'
          kind='primary'
          onClick={() => history.push('/auth/login')}
        >
          Reset password
        </Button>
      </Form>
    </>
  );
}
