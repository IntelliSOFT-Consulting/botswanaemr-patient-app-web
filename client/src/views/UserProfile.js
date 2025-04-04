import React from 'react';

// react-bootstrap components
import {
  Badge,
  Card,
  Form,
  Navbar,
  Nav,
  Container,
  Row,
  Col,
} from 'react-bootstrap';
import {
  Tile,
  TextInput,
  Button,
  FormGroup,
  DatePicker,
  DatePickerInput,
  RadioButtonGroup,
  RadioButton,
} from 'carbon-components-react';

function User() {
  return (
    <>
      <Container fluid>
        <Row className='my-3'>
          <Col md='8'>
            <Tile>
              <Card.Header>
                <Card.Title as='h4'>Edit Profile</Card.Title>
              </Card.Header>
              <Card.Body>
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
                    <Col className='pr-1' md='6'>
                      <TextInput id='email' labelText='Email' />
                    </Col>
                    <Col className='pl-1' md='6'>
                      <TextInput id='phoneNumber' labelText='Phone number' />
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
                          <RadioButton
                            id='male'
                            labelText='Male'
                            value='Male'
                          />
                          <RadioButton
                            id='female'
                            labelText='Female'
                            value='Female'
                          />
                          <RadioButton
                            id='other'
                            labelText='Other'
                            value='Other'
                          />
                        </RadioButtonGroup>
                      </FormGroup>
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
                      <TextInput
                        type='password'
                        id='password'
                        labelText='Password'
                      />
                    </Col>
                  </Row>
                  <Button
                    className='btn-fill pull-right'
                    type='submit'
                    variant='info'
                  >
                    Update Profile
                  </Button>
                  <div className='clearfix'></div>
                </Form>
              </Card.Body>
            </Tile>
          </Col>
          <Col md='4'>
            <Tile className='card-user'>
              <Card.Body>
                <div className='user'>
                  <div className='profile-upload'>
                    <input type='file' name='avatar' />
                  </div>
                  <h5 className='title'>John Doe</h5>
                  <p className='description'>34223</p>
                </div>
              </Card.Body>
            </Tile>
          </Col>
        </Row>
      </Container>
    </>
  );
}

export default User;
