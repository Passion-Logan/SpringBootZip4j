<template>
  <div id="global-uploader">

    <!-- 上传 -->
    <uploader
      ref="uploader"
      :options="options"
      :auto-start="false"
      :file-status-text="statusText"
      class="uploader-app"
      @file-added="onFileAdded"
      @file-success="onFileSuccess"
      @file-progress="onFileProgress"
      @file-error="onFileError">
      <uploader-unsupport/>

      <uploader-btn id="global-uploader-btn" ref="uploadBtn" :attrs="attrs">选择文件</uploader-btn>

      <uploader-list>
        <div slot-scope="props" :class="{'collapse': collapse}" class="file-panel">
          <div class="file-title">
            <span>文件列表</span>
          </div>

          <ul class="file-list">
            <li v-for="file in props.fileList" :key="file.id">
              <uploader-file ref="files" :class="'file_' + file.id" :file="file" :list="true"/>
            </li>
            <div v-if="!props.fileList.length" class="no-file"><i class="iconfont icon-empty-file"/> 暂无待上传文件</div>
          </ul>
        </div>
      </uploader-list>

    </uploader>

  </div>
</template>

<script>
/**
 *   全局上传插件
 *   调用方法：$Bus.$emit('openUploader', {}) 打开文件选择框，参数为需要传递的额外参数
 *   监听函数：$Bus.$on('fileAdded', fn); 文件选择后的回调
 *            $Bus.$on('fileSuccess', fn); 文件上传成功的回调
 */
import { ACCEPT_CONFIG } from './js/config'
import SparkMD5 from 'spark-md5'
import $ from 'jquery'
import { getToken } from '@/utils/auth'

import { mergeFile } from '@/api/upload'

export default {
  components: {},
  data () {
    return {
      options: {
        target: process.env.BASE_API + '/new/upload/chunk',
        // query: { test: '123' },
        chunkSize: '2048000',
        fileParameterName: 'file',
        maxChunkRetries: 3,
        // 是否开启服务器分片校验
        testChunks: true,
        // 服务器分片校验函数，秒传及断点续传基础
        checkChunkUploadedByResponse: function (chunk, message) {
          console.log(message)
          // const objMessage = JSON.parse(message)
          // if (objMessage.skipUpload) {
          //   return true
          // }

          // return (objMessage.uploaded || []).indexOf(chunk.offset + 1) >= 0

          return (message.uploaded || []).indexOf(chunk.offset + 1) >= 0
        },
        headers: {
          token: getToken()
        }
      },
      attrs: {
        // accept: ACCEPT_CONFIG.getAll()
        accept: 'image/png, application/pdf, audio/mpeg, audio/mp4, video/mp4, image/jpeg, text/plain, application/zip'
      },
      statusText: {
        success: '成功了',
        error: '出错了',
        uploading: '上传中',
        paused: '校验MD5',
        waiting: '等待中'
      },
      collapse: false,
      fileInfo: {
        chunkNumber: '',
        chunkSize: '',
        currentChunkSize: '',
        totalSize: '',
        identifier: '',
        filename: '',
        relativePath: '',
        totalChunks: ''
      },
      fileList: [],
      successFile: {
        name: '',
        path: ''
      }
    }
  },
  computed: {
    uploader () {
      return this.$refs.uploader.uploader
    }
  },
  watch: {},
  created () {
  },
  mounted () {
  },
  destroyed () {
    this.$Bus.$off('openUploader')
  },
  methods: {
    onFileAdded (file) {
      this.computeMD5(file)

      this.$Bus.$emit('fileAdded')
    },
    onFileProgress (rootFile, file, chunk) {
      console.log(`上传中 ${file.name}，chunk：${chunk.startByte / 1024 / 1024} ~ ${chunk.endByte / 1024 / 1024}`)
    },
    onFileSuccess (rootFile, file, message, chunk) {
      this.fileInfo.filename = file.name
      this.fileInfo.identifier = file.uniqueIdentifier

      mergeFile(this.fileInfo).then(response => {
        if (response.code === 2000) {
          this.fileList.push(response.data)
        }
      })
    },
    onFileError (rootFile, file, response, chunk) {
      this.$message({
        message: response,
        type: 'error'
      })
    },

    /**
     * 计算md5，实现断点续传及秒传
     * @param file
     */
    computeMD5 (file) {
      const fileReader = new FileReader()
      const time = new Date().getTime()
      const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
      let currentChunk = 0
      const chunkSize = 10 * 1024 * 1000
      const chunks = Math.ceil(file.size / chunkSize)
      const spark = new SparkMD5.ArrayBuffer()

      file.pause()

      loadNext()

      fileReader.onload = e => {
        spark.append(e.target.result)

        if (currentChunk < chunks) {
          currentChunk++
          loadNext()

          // 实时展示MD5的计算进度
          this.$nextTick(() => {
            $(`.myStatus_${file.id}`).text('校验MD5 ' + ((currentChunk / chunks) * 100).toFixed(0) + '%')
          })
        } else {
          const md5 = spark.end()
          this.computeMD5Success(md5, file)
          console.log(`MD5计算完毕：${file.name} \nMD5：${md5} \n分片：${chunks} 大小:${file.size} 用时：${new Date().getTime() - time} ms`)
        }
      }

      fileReader.onerror = function () {
        this.error(`文件${file.name}读取出错，请检查该文件`)
        file.cancel()
      }

      function loadNext () {
        const start = currentChunk * chunkSize
        const end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize
        fileReader.readAsArrayBuffer(blobSlice.call(file.file, start, end))
      }
    },

    computeMD5Success (md5, file) {
      // 将自定义参数直接加载uploader实例的opts上
      Object.assign(this.uploader.opts, {
        query: {
          ...this.params
        }
      })

      file.uniqueIdentifier = md5
      file.resume()
      // this.statusRemove(file.id)
    },
    error (msg) {
      this.$notify({
        title: '错误',
        message: msg,
        type: 'error',
        duration: 2000
      })
    }
  }
}
</script>

<style scoped lang="scss">
    #global-uploader {
        .uploader-app {
            width: 520px;
        }

        .file-panel {
            width: 100%;
            background-color: #fff;
            border: 1px solid #e2e2e2;
            border-radius: 7px 7px 0 0;
            box-shadow: 0 0 10px rgba(0, 0, 0, .2);

            .file-title {
                display: flex;
                height: 40px;
                line-height: 40px;
                padding: 0 15px;
                border-bottom: 1px solid #ddd;
            }

            .file-list {
                padding: 0;
                position: relative;
                height: 240px;
                overflow-x: hidden;
                overflow-y: auto;
                background-color: #fff;

                > li {
                    list-style: none;
                    background-color: #fff;
                }
            }

            &.collapse {
                .file-title {
                    background-color: #E7ECF2;
                }
            }
        }

        .no-file {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            font-size: 16px;
        }

        /deep/.uploader-file-icon {
            &:before {
                content: '' !important;
            }

            &[icon=image] {
                background: url(./images/image-icon.png);
            }
            &[icon=video] {
                background: url(./images/video-icon.png);
            }
            &[icon=document] {
                background: url(./images/text-icon.png);
            }
        }

        /deep/.uploader-file-actions > span {
            margin-right: 6px;
        }
    }

</style>