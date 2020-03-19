<template>
  <div class="page">
    <el-checkbox-group v-model="files" @change="handleCheckedFileChange">
      <el-checkbox v-for="type in fileTypes" :label="type.cityName" :key="type.id">{{ type.cityName }}</el-checkbox>
    </el-checkbox-group>

    <el-button v-if="isShow" type="primary" plain @click="isChoiceType">上传文件</el-button>
    <el-button v-if="isEdit" type="primary" plain @click="changeUpload">上传文件</el-button>

    <globalUploaders v-if="isShow" :accept-types="files" @successData="getSuccessData"/>
    <div v-if="isEdit" style="margin-top: 10px;">
      <el-tag v-for="f in thisFile" :key="f.id" closable @close="handleClose(f)">{{ f.name }}</el-tag>
    </div>
  </div>
</template>

<style>
  .el-tag + .el-tag {
    margin-left: 10px;
  }
  .button-new-tag {
    margin-left: 10px;
    height: 32px;
    line-height: 30px;
    padding-top: 0;
    padding-bottom: 0;
  }
  .input-new-tag {
    width: 90px;
    margin-left: 10px;
    vertical-align: bottom;
  }
</style>

<script>
// 按钮数据
const fileType = [{ id: 1, cityName: 'MP3' }, { id: 2, cityName: 'MP4' }, { id: 3, cityName: 'PNG' }, { id: 4, cityName: 'PDF' }, { id: 5, cityName: 'ZIP' }]
// 全选按钮
// const AllType = ['PDF', 'MP3', 'MP4', 'PNG', 'ZIP']
// 测试编辑回显
const myFile = [
  { id: '31231232132132', name: '测试1.mp3', type: 1 },
  { id: '3123123213213', name: '测试2.mp4', type: 2 },
  { id: '3123123213132', name: '测试3.png', type: 3 },
  { id: '3123132132132', name: '测试4.pdf', type: 4 },
  { id: '3231232132132', name: '测试5', type: 5 }
]
export default {
  components: {
  },
  data () {
    return {
      isShow: false,
      isEdit: false,
      fileTypes: fileType,

      // 选中类型
      files: [],
      // 测试编辑
      thisFile: '',
      // 上传成功后的回调数据 和表单数据一起传给后台
      successFiles: []
    }
  },
  computed: {
  },
  watch: {
    // 监听文件数据 控制是否显示组件
    thisFile (val) {
      if (val.length > 0) {
        this.isShow = false
        this.$nextTick(() => { this.isEdit = true })
      } else {
        this.isEdit = false
        this.$nextTick(() => { this.isShow = true })
      }
    }
  },
  created () {
  },
  mounted () {
    this.thisFile = ''
    this.isShow = true
  },
  methods: {
    getSuccessData (val) {
      this.successFiles.push(val)
    },
    isChoiceType () {
      // 校验是否选择类型
      if (this.files.length === 0) {
        this.$message({
          message: '请选择上传类型！',
          type: 'warning'
        })
        return
      }

      this.$Bus.$emit('openUploader')
    },
    changeUpload () {
      this.isEdit = false
      this.isShow = true
      this.isChoiceType()
    },
    handleClose (tag) {
      this.$confirm('该操作不可逆，确认删除？')
        .then(_ => {
          // 数据库软删除，oss中的直接删
          // axios.get(createUrl)
          this.thisFile.splice(this.thisFile.indexOf(tag), 1)

          this.$message({
            type: 'success',
            message: '删除成功'
          })
        })
        .catch(_ => {})
      // 移除tag
      if (this.thisFile.length === 0) {
        this.changeUpload()
      }
    },
    handleCheckedFileChange (value) {
      // 改变选择后重新加载组件
      this.isShow = false
      this.$nextTick(() => { this.isShow = true })
    }
  }
}
</script>

