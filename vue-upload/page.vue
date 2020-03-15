<template>
  <div class="page">
    <!-- <div id="filePicker">选择文件</div>
    <NewWebUpload
      :form-data="urlToken"
      :url="serverUrl"
      :accept-types="acceptTypes"
      upload-button="#filePicker"
      multiple/> -->

    <globalUploaders/>
  </div>
</template>

<script>

// import { getToken } from '@/utils/auth'
// import $ from 'jquery'

// export default {
//   components: {
//   },
//   data () {
//     return {
//       serverUrl: process.env.BASE_API + '/new/upload/uploadFile',
//       urlToken: {},
//       acceptTypes: {
//         title: 'Files',
//         extensions: 'jpg,jpeg,png,pdf,txt,zip,mp3,mp4',
//         mimeTypes: 'image/png, application/pdf, audio/mpeg, audio/mp4, video/mp4, image/jpeg, text/plain, application/zip'
//       }
//     }
//   },
//   computed: {
//   },
//   watch: {},
//   created () {
//     this.urlToken.token = getToken()
//   },
//   mounted () {
//   },
//   methods: {

//   }
// }
</script>

