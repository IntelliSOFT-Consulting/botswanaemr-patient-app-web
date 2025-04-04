import React from 'react';
import { TextInput, Form, Button } from 'carbon-components-react';
import { useHistory } from 'react-router-dom';

export default function ConfirmEmail() {
  const history = useHistory();
  return (
    <>
      <h3>Password reset</h3>
      <p>Please enter your email registered in the system</p>
      <Form>
        <TextInput id='email' labelText='Enter your email' />
        <Button
          className='login-button'
          kind='primary'
          onClick={() => history.push('/auth/login')}
        >
          Send reset link
        </Button>
      </Form>
    </>
  );
}
