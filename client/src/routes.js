import Dashboard from 'views/Dashboard.js';
import UserProfile from 'views/UserProfile.js';
import Notifications from 'views/Notifications.js';
import Login from 'views/Login.js';
import Register from 'views/Register';
import ConfirmEmail from 'views/ConfirmEmail';
import PasswordReset from 'views/PasswordReset';

const dashboardRoutes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    icon: 'nc-icon nc-chart-pie-35',
    component: Dashboard,
    layout: '/admin',
  },
  {
    path: '/profile',
    name: 'User Profile',
    icon: 'nc-icon nc-circle-09',
    component: UserProfile,
    layout: '/admin',
  },
  {
    path: '/notifications',
    name: 'Notifications',
    icon: 'nc-icon nc-bell-55',
    component: Notifications,
    layout: '/admin',
  },
  {
    path: '/login',
    name: 'Login',
    icon: 'nc-icon nc-tap-01',
    component: Login,
    layout: '/auth',
  },
  {
    path: '/register',
    name: 'Register',
    icon: 'nc-icon nc-tap-01',
    component: Register,
    layout: '/auth',
  },
  {
    path: '/forgot-password',
    name: 'Forgot Password',
    icon: 'nc-icon nc-tap-01',
    component: ConfirmEmail,
    layout: '/auth',
  },
  {
    path: '/reset-password',
    name: 'Reset Password',
    icon: 'nc-icon nc-tap-01',
    component: PasswordReset,
    layout: '/auth',
  },
];

export default dashboardRoutes;
