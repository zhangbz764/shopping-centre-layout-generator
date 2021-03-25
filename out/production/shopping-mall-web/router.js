import Vue from 'vue'
import VueRouter from 'vue-router'
import Viewer from './components/Viewer'
import Home from './components/Home'

const examples = [
  '2d-editor', '3d-editor', 'camera-viewer',
  'marching-cubes', 'material-example', 'shape-2d',
  'undo-redo-canvas', 'image-3d', 'java-backend-example',
  'archijson-geometry','quick3d'
]
const routes = [];
examples.forEach((item) => {
  let res = item.split('-');
  for (let i = 0; i < res.length; ++i) {
    res[i] = res[i].replace(/^\S/, s => s.toUpperCase());
  }
  let title = res.join(' ');
  routes.push({
    path: '/' + item,
    name: item,
    component: Viewer,
    props: () => {
      window.currentApp = item
    },
    meta: {title: 'ArchiWeb ' + title}
  })
})

routes.push(
  {path: '/', name: 'home', component: Home, meta: {title: 'ArchiWeb'}}
)

Vue.use(VueRouter)

const router = new VueRouter({
  routes
})
// eslint-disable-next-line no-unused-vars
router.afterEach((to, from) => {
  // Use next tick to handle router history correctly
  // see: https://github.com/vuejs/vue-router/issues/914#issuecomment-384477609
  Vue.nextTick(() => {
    document.title = to.meta.title || 'ArchiWeb';
  });
});
export {
  router as default,
  examples
}
