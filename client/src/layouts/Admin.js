import React, { useEffect } from 'react';
import { Route, Switch, useHistory } from 'react-router-dom';
import {
  SideNav,
  HeaderContainer,
  HeaderName,
  HeaderGlobalAction,
  HeaderGlobalBar,
  SideNavItems,
  Header,
  SkipToContent,
  HeaderMenuButton,
  SideNavLink,
  OverflowMenu,
  OverflowMenuItem,
} from 'carbon-components-react';
import {
  Notification20,
  User20,
  Workspace20,
  Hospital20,
  EventSchedule20,
  Settings20,
} from '@carbon/icons-react';

import routes from 'routes.js';

function Admin() {
  const history = useHistory();
  const getRoutes = routes => {
    return routes.map((prop, key) => {
      if (prop.layout === '/admin') {
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
    <>
      <HeaderContainer
        render={({ isSideNavExpanded, onClickSideNavExpand }) => (
          <>
            <Header aria-label='IBM Platform Name'>
              {' '}
              <SkipToContent />{' '}
              <HeaderMenuButton
                aria-label='Open menu'
                isCollapsible
                onClick={onClickSideNavExpand}
                isActive={isSideNavExpanded}
              />
              <HeaderName href='#' prefix=''>
                Patient Portal
              </HeaderName>
              <HeaderGlobalBar>
                <HeaderGlobalAction
                  aria-label='Notifications'
                  onClick={() => alert('Notifications clicked')}
                >
                  <Notification20 />
                </HeaderGlobalAction>
                <HeaderGlobalAction
                  aria-label='App Switcher'
                  // onClick={() => alert('App switcher clicked')}
                  // tooltipAlignment='end'
                >
                  <OverflowMenu
                    renderIcon={User20}
                    menuOffset={{ top: 0, left: 0 }}
                  >
                    <OverflowMenuItem
                      onClick={() => history.push('/admin/profile')}
                      itemText='Profile'
                    />
                    <OverflowMenuItem
                      onClick={() => history.push('/auth/login')}
                      itemText='Logout'
                    />
                  </OverflowMenu>
                </HeaderGlobalAction>
              </HeaderGlobalBar>
              <SideNav
                aria-label='Side navigation'
                isRail
                expanded={isSideNavExpanded}
                onOverlayClick={onClickSideNavExpand}
              >
                <SideNavItems>
                  <SideNavLink renderIcon={EventSchedule20} href='#'>
                    Appointments
                  </SideNavLink>
                  <SideNavLink renderIcon={Hospital20} href='#'>
                    Hospitals
                  </SideNavLink>

                  <SideNavLink renderIcon={Settings20} href='#'>
                    Settings
                  </SideNavLink>
                </SideNavItems>
              </SideNav>
            </Header>
            <div
              className={
                isSideNavExpanded ? 'content-expanded' : 'content-mini'
              }
            >
              <Switch>{getRoutes(routes)}</Switch>
            </div>
          </>
        )}
      />
      {/* <div className='wrapper'>
        <Sidebar color={color} image={hasImage ? image : ''} routes={routes} />
        <div className='main-panel' ref={mainPanel}>
          <AdminNavbar />
          <div className='content'>
            <Switch>{getRoutes(routes)}</Switch>
          </div>
          <Footer />
        </div>
      </div> */}
    </>
  );
}

export default Admin;
