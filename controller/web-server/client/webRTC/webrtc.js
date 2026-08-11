export function WebRTC (connection) {
    const {RTCPeerConnection} = window

    let peerConnection = null

    this.handle = (data) => {
        if (!peerConnection) {
            return
        }

        const {RTCSessionDescription, RTCIceCandidate} = window
        let webRtcEvent
        if (typeof data === 'string') {
            webRtcEvent = JSON.parse(data)
        } else {
            webRtcEvent = data
        }
        // WebRTC type
        switch (webRtcEvent.type) {
            case 'offer':
                peerConnection.setRemoteDescription(
                    new RTCSessionDescription({sdp: webRtcEvent.sdp, type: 'offer'})
                )
                doAnswer()
                break

            case 'candidate': {
                const candidate = new RTCIceCandidate({
                    candidate: webRtcEvent.candidate,
                    sdpMid: webRtcEvent.id,
                    sdpMLineIndex: webRtcEvent.label
                })
                peerConnection.addIceCandidate(candidate)
                break
            }

            case 'bye':
                this.stop()
                break
        }
    }

    const doAnswer = async () => {
        const answer = await peerConnection.createAnswer()
        await peerConnection.setLocalDescription(answer)
        connection.send(JSON.stringify({webrtc_event: answer}))
    }

    this.start = () => {
        peerConnection = new RTCPeerConnection()

        this.dataChannel = peerConnection.createDataChannel('dataChannel')

        peerConnection.ondatachannel = (event) => {
            event.channel.onopen = () => {}
        }

        this.dataChannel.onopen = () => {}
        this.dataChannel.onmessage = () => {}
        const video = document.getElementById('video')

        video.srcObject = new MediaStream()
        video.srcObject.getTracks().forEach((track) => peerConnection.addTrack(track))

        peerConnection.ontrack = (event) => {
            video.srcObject = event.streams[0]
        }
    }

    this.stop = () => {
        if (peerConnection) {
            peerConnection.close()
        }
        peerConnection = null
    }

    this.send = (message) => {
        if (this.dataChannel && this.dataChannel.readyState === 'open') {
            this.dataChannel.send(message)
        }
    }
}
