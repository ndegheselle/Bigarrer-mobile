import Default from '@/layouts/Default.vue';
import type { RouteRecordRaw } from 'vue-router';

import HomePage from '@/pages/HomePage.vue';
import authRoutes from '@features/users/routes';
import arRoutes from '@features/AR/routes';

const routes: RouteRecordRaw[] = [
    {
        path: '',
        component: Default,
        children: [
            {
                path: '',
                component: HomePage,
            },
            ...authRoutes,
            ...arRoutes,
        ]
    }
];

export default routes;