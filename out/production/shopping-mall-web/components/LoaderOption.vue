<template>
  <section>
  
    <v-row justify="center">
      <v-dialog
        v-model="dialog"
        max-width="360"
        persistent
      >
        <v-card
          class="pa-4"
        >
          <h2 class="mt-2">导入选项</h2>
          <v-divider class="my-3"></v-divider>
          <h3 class="mt-2">模型状态</h3>
          <v-radio-group
            v-model="option.status"
            row
            @change="updateOption"
          >
            <v-radio
              label="成组"
              value="grouped"
            ></v-radio>
            <v-spacer></v-spacer>
            <v-radio
              label="融合"
              value="merged"
            ></v-radio>
            <v-spacer></v-spacer>
            <v-radio
              label="原始"
              value="raw"
            ></v-radio>
        
          </v-radio-group>
        
          <v-divider class="mb-3"></v-divider>
          <h3>其他设置</h3>
        
          <v-row
            no-gutters
          >
            <v-col>
              <v-switch
                v-model="option.selectable"
                label="物件可选"
                @change="updateOption"
              ></v-switch>
            </v-col>
          
            <v-col>
              <v-switch
                v-model="option.doubleSide"
                label="双面材质"
                @change="updateOption"
              ></v-switch>
            </v-col>
          </v-row>
          <v-row
            no-gutters
          >
            
            <v-col>
              <v-switch
                v-model="option.toCamera"
                :disabled=!toCamera
                label="朝向相机"
                @change="updateOption"
              ></v-switch>

            </v-col>
  
            <v-col>
              <v-switch
                v-model="option.ZtoY"
                :disabled=!ZtoY
                label="映射Y至Z"
                @change="updateOption"
              ></v-switch>
  
            </v-col>
          </v-row>
          <v-row
            no-gutters
          >
          
            <v-col>
              <v-switch
                v-model="option.shadow"
                label="阴影"
                @change="updateOption"
              ></v-switch>
          
            </v-col>
          
            <v-col>
              <v-switch
                v-model="option.edge"
                :disabled=!edge
                label="边线"
                @change="updateOption"
              ></v-switch>
          
            </v-col>
          </v-row>
        
        
          <v-card-actions
            class="px-0"
          >
            <v-spacer></v-spacer>
            <v-btn
              depressed
              @click="load=false;dialog=false"
            >
              取消
            </v-btn>
            <v-btn
              color="primary"
              depressed
              @click="load=true;dialog=false"
            >
              载入
            </v-btn>
          </v-card-actions>
      
        </v-card>
      </v-dialog>
    </v-row>
  </section>
</template>

<script>
import {loaderOption} from "@/creator/Loader";

export default {
  
  name: "LoaderOption",
  data: () => ({
    dialog: false,
    load: true,
    toCamera: true,
    ZtoY: true,
    edge: true
  }),
  mounted() {
    window.LoaderOption = this;
  },
  computed: {
    option: {
      get() {
        return loaderOption;
      },
      // set(val) {
      //   Object.keys(val).forEach((it) => {
      //     loaderOption[it] = val[it];
      //   })
      //   console.log()
      // }
    }
  },
  methods: {
    updateOption() {
      window.LoaderOption.toCamera = (loaderOption.status === "merged");
      window.LoaderOption.ZtoY = (loaderOption.status !== "raw");
      window.LoaderOption.edge = (loaderOption.status !== "raw");
    },
  }
  
}
</script>

<style scoped>

</style>