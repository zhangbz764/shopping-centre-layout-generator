<template>
  <v-footer
    dark
    fixed
    padless
  >
    <v-card
      class="flex"
      flat
      tile
    >
      <v-card-title class="py-2 white--text body-1">
        
        {{ new Date().getFullYear() }} - ©
        <a class="text-decoration-none" href="http://labaaa.org" style="color:#ffffff;" target="_blank">
          <strong>Inst. AAA</strong>
        </a>
        
        <v-spacer></v-spacer>
        
        <v-btn
          v-for="(icon, id) in icons"
          :key="id"
          :href="icon.link"
          class="mx-2 mx-md-4"
          icon
          target="_blank"
        >
          <v-icon color="white" size="24px">{{ icon.icon }}</v-icon>
        </v-btn>
      </v-card-title>
    </v-card>
  </v-footer>
</template>

<script>
export default {
  data: () => ({
    icons: [
      {
        icon: 'mdi-wechat',
        link: 'https://mp.weixin.qq.com/s?__biz=MzI1MTUyNzgyNA==&mid=2247485505&idx=1&sn=dd492448cc0569974fba05cdc75bf655&chksm=e9f0dfbede8756a8797c19cb962982ad710c43c0e0d22374df883d7cbf6f1cdfb4058d744fc9&mpshare=1&scene=1&srcid=09025qJJpFfxEZTw8etuTEVb&sharer_sharetime=1599048348621&sharer_shareid=f42994bbf9057896029f97f8fa15e27d&key=2364869c3e3351dc9598d22acca2bab5765e62027c678bdfd055a3e1b75266212c54407c7d3e4dc8b1a8a495d96f4c480eb652778a8451907a98a7b5df491ee79310709ef58a96694ad02c5f3e5af973fb1a611f4ef5fbaeb5c888c99f50863684e5a0cc18db9f3e7e156cab512c60dd8c26933676f46905ce29f2713b9f8957&ascene=1&uin=ODgyMTYwMjYy&devicetype=Windows+10+x64&version=62090529&lang=zh_CN&exportkey=ASaqAYrmEzGLwNFh3tZ%2FivI%3D&pass_ticket=Uq7ftByDDvAGl947IjvhSK4%2FymUkfy9j4b%2FTI%2FusJgXpb0ZSPcZ6u0YZ1cuj2a5E'
      },
      {icon: 'mdi-gmail', link: 'mailto://moyichen@seu.edu.cn'},
      {icon: 'mdi-github', link: 'https://github.com/Inst-AAA/archiweb'},
    ],
    
  }),
}
</script>
