/*
 * Developed for the OpenBot project (https://openbot.org) by:
 *
 * Ivo Zivkov
 * izivkov@gmail.com
 *
 * Date: Mon Nov 29 2021
 */

export function Buttons (connection) {
  const toggleMirror = () => {
    const video = document.getElementById('video')

    const isMirrored = video.style.cssText !== ''
    this.setMirrored(!isMirrored)
  }

  const mirrorButton = document.getElementById('mirror_button')
  if (mirrorButton) {
    mirrorButton.onclick = toggleMirror
  }

  const toggleSound = () => {
    const video = document.getElementById('video')
    video.muted = !video.muted

    document.getElementById('sound_button').src = video.muted ? 'icons/volume_off_black_24dp.svg' : 'icons/volume_up_black_24dp.svg'
  }

  const soundButton = document.getElementById('sound_button')
  if (soundButton) {
    soundButton.onclick = toggleSound
  }

  const switchCamera = () => {
    if (typeof connection.send === 'function') {
      console.log('[Web] SWITCH_CAMERA pressed');
      connection.send(JSON.stringify({ command: 'SWITCH_CAMERA' }))
      console.log('[Web] SWITCH_CAMERA sent to robot');
    }
  }

  const cameraSwitchButton = document.getElementById('camera_switch_button')
  if (cameraSwitchButton) {
    cameraSwitchButton.onclick = switchCamera
  }

  this.setMirrored = mirrored => {
    const video = document.getElementById('video')

    video.style.cssText = mirrored
      ? '-moz-transform: scale(-1, 1) translateX(50%); -webkit-transform: scale(-1, 1) translateX(50%); -o-transform: scale(-1, 1) translateX(50%); transform: scale(-1, 1) translateX(50%); filter: FlipH;'
      : 'translateX(-50%)'

    document.getElementById('mirror_button').src = mirrored ? 'icons/flip_black_24dp-mirrored.svg' : 'icons/flip_black_24dp.svg'
  }

  const goFullscreen = () => {
    const video = document.getElementById('video')
    if (video.requestFullscreen) {
      video.requestFullscreen()
    } else if (video.webkitRequestFullscreen) {
      video.webkitRequestFullscreen()
    } else if (video.msRequestFullscreen) {
      video.msRequestFullscreen()
    }
  }

  const fullscreenButton = document.getElementById('fullscreen')
  if (fullscreenButton) {
    fullscreenButton.onclick = goFullscreen
  }
}
