// IBM login page web
import React from 'react';
import {
  Tile,
  Button,
  InlineNotification,
  Form,
  FormGroup,
  TextInput,
  DatePicker,
  DatePickerInput,
  RadioButtonGroup,
  RadioButton,
} from 'carbon-components-react';

import { Link, useHistory } from 'react-router-dom';
import logo from '../assets/img/botswana.png';

export default function Profile({ error }) {
  const history = useHistory();
  return (
    <>
      <h3>Register</h3>
      <p style={{ marginBottom: '10px' }}>
        Have an account? <Link to='/auth/login'>Log in</Link>
      </p>
      <Form>
        <div className='profile-upload'>
          <input type='file' name='avatar' />
        </div>
        {/* first and last name inputs on same row */}
        <div className='doubles'>
          <TextInput id='firstName' labelText='First name' />
          <TextInput id='lastName' labelText='Last name' />
        </div>
        <div className='doubles'>
          <DatePicker dateFormat='d/m/Y' datePickerType='simple'>
            <DatePickerInput
              id='dateOfBirth'
              placeholder='dd/mm/yyyy'
              labelText='Date of birth'
              type='text'
              width='100%'
            />
          </DatePicker>
          <FormGroup legendText='Gender'>
            <RadioButtonGroup legend='Gender' name='gender'>
              <RadioButton id='male' labelText='Male' value='Male' />
              <RadioButton id='female' labelText='Female' value='Female' />
              <RadioButton id='other' labelText='Other' value='Other' />
            </RadioButtonGroup>
          </FormGroup>
        </div>
        <div className='doubles'>
          <TextInput type='email' id='email' labelText='Email' />
          <TextInput type='tel' id='phoneNumber' labelText='Phone number' />
        </div>
        <div className='doubles'>
          <TextInput
            id='patientId'
            labelText='Patient ID number'
            type='number'
          />
          <TextInput
            id='nationalId'
            labelText='National ID/Passport number'
            type='number'
          />
        </div>
        <TextInput id='username' labelText='Username' />
        <div className='doubles'>
          <TextInput type='password' id='password' labelText='Password' />
          <TextInput
            type='password'
            id='confirmPassword'
            labelText='Confirm password'
          />
        </div>
        {/* Register button */}

        <Button
          className='submit-button'
          onClick={() => history.push('/admin/dashboard')}
          type='submit'
        >
          Register
        </Button>
      </Form>
    </>
  );
}
