<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      max-width="750"
    >
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          v-bind="attrs"
          v-on="on"
          dark
          icon
          large
        >
          <v-icon color="black">mdi-cog</v-icon>
        </v-btn>
      </template>
      <v-card
        class="pa-2"
      >
        <v-card-title class="headline justify-center">
          {{ title }}
        </v-card-title>
        <v-card-text
          v-if="info.length > 0"
          class="justify-center text-center">
          <span v-html="info"></span>
  
        </v-card-text>
        <v-card-title class="justify-center">
          Hotkeys
        </v-card-title>
        <v-card-text class="pa-3">
          <v-col
            v-for="id in (manual.length/2)"
            :key="id"
          >
            <v-row no-gutters>
              <v-spacer></v-spacer>
              <v-col>
                {{ manual[(id - 1) * 2].split(":")[0] }}
              
              </v-col>
              <v-col>
                {{ manual[(id - 1) * 2].split(":")[1] }}
              </v-col>
              <v-spacer></v-spacer>
              <v-col>
                {{ manual[(id - 1) * 2 + 1].split(":")[0] }}
              
              </v-col>
              <v-col>
                {{ manual[(id - 1) * 2 + 1].split(":")[1] }}
              </v-col>
              <v-spacer></v-spacer>
            </v-row>
          </v-col>
          <v-spacer></v-spacer>
        
        
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-row>
</template>

<script>

export default {
  name: "OptionCard",
  data: () => ({
    value: 1,
    dialog: false,
    title: "ArchiWeb User Manual",
    info: "",
    manual: ["W:Translate", "E:Rotate", "R:Scale", "+/-:Size"
      , "Q:World/Local", "S:Snap", "C:Camera", "1-9:Camera View"]
  }),
  mounted() {
    window.OptionCard = this;
  },
  
}
</script>

<style scoped>

</style>