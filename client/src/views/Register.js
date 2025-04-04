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
import { Badge, Card, Navbar, Nav, Container, Row, Col } from 'react-bootstrap';

import { Link, useHistory } from 'react-router-dom';
import logo from '../assets/img/botswana.png';

export default function Register({ error }) {
  const history = useHistory();
  return (
    <>
      <h3>Register</h3>

      <Form>
        <Row className='my-3'>
          <Col className='pr-1' md='6'>
            <TextInput id='firstName' labelText='First name' />
          </Col>
          <Col className='px-1' md='6'>
            <TextInput id='lastName' labelText='Last name' />
          </Col>
        </Row>
        <Row className='my-3'>
          <Col md='6'>
            <DatePicker dateFormat='d/m/Y' datePickerType='simple'>
              <DatePickerInput
                id='dateOfBirth'
                placeholder='dd/mm/yyyy'
                labelText='Date of birth'
                type='text'
                width='100%'
              />
            </DatePicker>
          </Col>
          <Col md='6'>
            <FormGroup legendText='Gender'>
              <RadioButtonGroup
                legend='Gender'
                name='gender'
                defaultSelected='Male'
              >
                <RadioButton id='male' labelText='Male' value='Male' />
                <RadioButton id='female' labelText='Female' value='Female' />
                <RadioButton id='other' labelText='Other' value='Other' />
              </RadioButtonGroup>
            </FormGroup>
          </Col>
        </Row>
        <Row className='my-3'>
          <Col className='pr-1' md='6'>
            <TextInput id='email' labelText='Email' />
          </Col>
          <Col className='pl-1' md='6'>
            <TextInput id='phoneNumber' labelText='Phone number' />
          </Col>
        </Row>

        <Row className='my-3'>
          <Col md='6'>
            <TextInput
              id='patientId'
              labelText='Patient ID number'
              type='number'
            />
          </Col>
          <Col md='6'>
            <TextInput
              id='nationalId'
              labelText='National ID/Passport number'
              type='number'
            />
          </Col>
        </Row>
        <Row className='my-3'>
          <Col md='6'>
            <TextInput id='username' labelText='Username' />
          </Col>
          <Col md='6'>
            <TextInput type='password' id='password' labelText='Password' />
          </Col>
        </Row>
        <Button className='btn-fill pull-right' type='submit' variant='info'>
          Register
        </Button>

        <p style={{ marginTop: '10px' }} className='signup-link'>
          Already have an account? <Link to='/auth/login'>Sign in</Link>
        </p>
        <div className='clearfix'></div>
      </Form>
    </>
  );
}
