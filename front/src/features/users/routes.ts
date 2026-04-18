import type { RouteRecordRaw } from 'vue-router';

import LoginPage from '@features/users/pages/LoginPage.vue';
import RegisterPage from '@features/users/pages/RegisterPage.vue';

export const routesNames = {
    login: 'users.login',
    register: 'users.register',
} as const;

const routes: RouteRecordRaw[] = [
    {
        path: '/user/login',
        name: routesNames.login,
        component: LoginPage,
    },
    {
        path: '/user/register',
        name: routesNames.register,
        component: RegisterPage,
    },
];

export default routes;