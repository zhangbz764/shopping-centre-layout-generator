<template>
  <v-app>

    <TopNav/>
    <LoaderOption></LoaderOption>
    <router-view></router-view>
    <Footer/>
  </v-app>
</template>

<script>
import TopNav from './components/TopNav'
import Footer from './components/Footer'
import LoaderOption from "@/components/LoaderOption";

export default {
  name: 'App',
  components: {
    TopNav,
    Footer,
    LoaderOption
  },
}
</script>