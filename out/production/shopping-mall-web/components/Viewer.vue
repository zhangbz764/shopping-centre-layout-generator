<template>
  
  <section>
    <div
      id="gui-container"
      style="position:absolute;right:0;z-index:999"
    ></div>
    
    <InfoCard
      id="info-card"
      style="position:absolute;top:64px;left:20px;z-index:1"
    ></InfoCard>
    <div
      id="info"
      style="position:fixed;top:64px;left:20px;z-index:1"
    >
    
    </div>
    
    <ViewSelectCard
      style="position:fixed;bottom:64px;left:0px;right:0px;margin-left: auto;"
    ></ViewSelectCard>
    
    <template style="background:#ffffff;">
      <div id="container" class="d-flex justify-sm-center">
      </div>
    </template>
  
  </section>

</template>

<script>
import InfoCard from "@/components/InfoCard";
import ViewSelectCard from "@/components/ViewSelectCard";

export default {
  name: 'Viewer',
  components: {ViewSelectCard, InfoCard},
  data: () => ({}),
  mounted() {
    const test = require('@/examples' + this.$route.path + '.js');
    test.main();
  },
  
}
</script>

<style>

.v-application .title {
  font-size: 0.9rem !important;
}

.dg .c input[type=checkbox] {
  margin-top: 3px;
}

.dg .c input {
  font-size: 12px;
  height: 1.25rem;
  text-align: center;
  line-height: normal;
}

select {
  background: #FFFFFF;
  /* -webkit-writing-mode: horizontal-tb !important; */
  appearance: menulist;
  font-size: 12px;
}
</style>
