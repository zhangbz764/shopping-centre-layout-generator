<template>
  <section>
    <v-carousel
      cycle
      height="725"
      hide-delimiter-background
      show-arrows-on-hover
    >
      <v-carousel-item
        v-for="(item,i) in items"
        :key="i"
        :src="item.src"
        :to="item.to"
      >

      </v-carousel-item>
    </v-carousel>
  
    <v-container>
      <h1 class="text-center display-2 hidden-xs-only pt-10"> Gallery </h1>
      <h1 class="text-center display-1 hidden-sm-and-up py-5"> Gallery </h1>
    </v-container>
    <v-row class="py-10 mb-10">
      <v-col
        v-for="(theme,id) in themes"
        :key="id"
        class="elevation-0 col-12 col-md-6 col-lg-4 col-xl-3"
      >
      
        <v-hover v-slot:default="{ hover }">
          <v-card
            :class="{ 'on-hover': hover }"
            :elevation="hover ? 10 : 4"
            :to="theme"
            class="mx-auto mb-5"
            max-height="380"
            min-height="30"
            max-width="360"
            min-width="320"
          >
          
            <v-col>
              <v-img :src="'/images/'+theme+'.jpg'">
              </v-img>
              <v-row class="justify-center align-center ma-0">
                <h3 class="my-3"> {{ theme }}</h3>
              </v-row>
            </v-col>
        
        
          </v-card>
        </v-hover>
    
      </v-col>
    </v-row>

  </section>
</template>

<script>
import {examples} from "@/router";

export default {
  name: "Home",
  data: () => ({
    themes: examples,
    items: [
      {
        src: 'images/banner1.gif',
        to: '3d-editor'
      },
      {
        src: 'images/banner2.gif',
        to: 'marching-cubes'
      },
      {
        src: 'images/banner3.gif',
        to: 'shape-2d'
      },
      {
        src: 'images/banner4.gif',
        to: 'undo-redo-canvas'
      },
    ],
  })
}
</script>

<style scoped>

</style>