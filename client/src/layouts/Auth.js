import React from 'react';
import { Route, Switch } from 'react-router-dom';
import { Tile } from 'carbon-components-react';
import routes from 'routes.js';
import logo from '../assets/img/botswana.png';

export default function Auth() {
  const getRoutes = routes => {
    return routes.map((prop, key) => {
      if (prop.layout === '/auth') {
        return (
          <Route
            path={prop.layout + prop.path}
            render={props => <prop.component {...props} />}
            key={key}
          />
        );
      } else {
        return null;
      }
    });
  };
  return (
    <div className='login-container'>
      <div className='login-tile'>
        <Switch>{getRoutes(routes)}</Switch>
      </div>
      <div>
        <img src={logo} alt='logo' />
      </div>
    </div>
  );
}
