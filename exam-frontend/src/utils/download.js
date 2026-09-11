export function downloadBlob(response, filename) {
  const blob = new Blob([response.data], {
    type: response.headers['content-type'] || 'application/octet-stream'
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
