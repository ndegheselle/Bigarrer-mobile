import type { RouteRecordRaw } from 'vue-router';

import TestPage from './pages/TestPage.vue';

export const routesNames = {
    test: 'AR.test',
} as const;

const routes: RouteRecordRaw[] = [
    {
        path: '/AR/test',
        name: routesNames.test,
        component: TestPage,
    },
];

export default routes;