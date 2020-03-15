export const ACCEPT_CONFIG = {
  // image: ['.png', '.jpg', '.jpeg', '.gif', '.bmp'],
  // video: ['.mp4', '.rmvb', '.mkv', '.wmv', '.flv'],
  // document: ['.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.pdf', '.txt', '.tif', '.tiff'],
  image: ['.png'],
  video: ['.mp4'],
  document: ['.pdf'],
  audio: ['mp3'],
  compressed : ['zip'],
  getAll () {
    return [...this.image, ...this.video, ...this.document, ...this.audio, ...this.compressed]
  }
}
