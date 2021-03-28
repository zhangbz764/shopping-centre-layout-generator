<template>
  <v-card
    class="mx-auto overflow-y-auto"
    color="rgba(250, 250, 250, 0.5)"
    max-height="600"
    max-width="344"
  >
    <v-list-item>
      <v-list-item-content
      >
        <div id="object-title" class="overline mb-1">
          Object Info
        </div>
        
        <v-text-field
          :value="info.uuid"
          dense
          label="uuid"
          outlined
          readonly
        >
        </v-text-field>
        
        <div
          v-if="Object.keys(info.position).length > 0"
          class="overline">
          Position
        </div>
        <v-row>
          <v-col
            v-for="(ti, t) in Object.keys(info.position)"
            :key="t"
            :cols="12/Object.keys(info.position).length"
          >
            <v-text-field
              :ref="ti"
              :label="ti"
              :rules="[rules.number]"
              :value="info.position[ti]"
              dense
              outlined
              :readonly="!updateFlag"
              @change="updateScene"></v-text-field>
          </v-col>
        
        </v-row>
        
        <div
          v-if="Object.keys(info.model).length > 0"
          class="overline"
        >
          Model
        </div>
        <v-row>
          <v-col
            v-for="(ti, t) in Object.keys(info.model)"
            :key="t"
            :cols="12/Object.keys(info.model).length"
          >
            <v-text-field
              :ref="ti"
              :label="ti"
              :value="info.model[ti]"
              dense
              outlined
              :readonly="!updateFlag"
              @change="updateScene"></v-text-field>
          </v-col>
        </v-row>
        
        <div
          v-if="Object.keys(info.properties).length > 0"
          class="overline"
        >
          Properties
        </div>
        <v-list-item-title
          v-for="(ti,t) in Object.keys(info.properties)"
          :key="t"
          class="pt-2"
        >
          <v-textarea
            :label="ti"
            :value="info.properties[ti]"
            auto-grow
            outlined
            readonly
            rows="0"
          ></v-textarea>
        
        </v-list-item-title>
      </v-list-item-content>
    
    
    </v-list-item>
    
    <v-card-actions
      class="ma-2"
    >
      <v-btn
        color="rgba(250,250,250,0.8)"
        @click="updateScene()"
      >
        Submit Change
      </v-btn>
      <v-spacer></v-spacer>
      <v-btn
        color="rgba(0,0,0,0.6)"
        dark
        @click="hideInfoCard(false)"
      >
        Close
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>

export default {
  name: "InfoCard",
  data() {
    return {
      updateFlag: false,
      updateObject: function () {
      },
      show: false,
      info: {
        uuid: 'uuid',
        position: {},
        model: {},
        properties: {}
      },
      rules: {
        required: value => !!value || 'Required.',
        number: value => {
          const num = Number(value);
          return !isNaN(num) && isFinite(num) || 'Not a number';
        }
      },
    }
  },
  mounted() {
    window.InfoCard = this;
    this.hideInfoCard(this.show);
    this.init();
  },
  methods: {
    init() {
      if (this.$route.path === '/') {
        const index = require('@/index.js');
        if (index.updateObject !== undefined) {
          this.updateFlag = true;
          this.updateObject = index.updateObject;
        }
        // index.updateObject(this.info.uuid, this.info.model);
      } else {
        const examples = require('@/examples' + this.$route.path + '.js');
        if (examples.updateObject !== undefined) {
          this.updateFlag = true;
          this.updateObject = examples.updateObject;
        }
        // examples.updateObject(this.info.uuid, this.info.model);
      }
    },
    hideInfoCard(isShow) {
      this.show = isShow;
      let e = document.getElementById('info-card');
      e.style.display = isShow ? "block" : "none";
    },
    updateScene() {
      if (this.updateFlag) {
        console.log(this.$refs)
        for (let k of Object.keys(this.info.position)) {
          this.info.position[k] = Number(this.$refs[k][0].lazyValue);
        }
      
        for (let k of Object.keys(this.info.model)) {
          this.info.model[k] = Number(this.$refs[k][0].lazyValue);
        }
        this.updateObject(this.info.uuid, this.info.model);
      }
    
    
    },
    
  }
}
</script>

<style scoped>
.scroll {
  overflow-y: scroll
}
</style>