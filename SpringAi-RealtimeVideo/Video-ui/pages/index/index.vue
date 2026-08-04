<template>
	<view class="page">
		<view class="topbar">
			<view>
				<text class="title">AI 视频对话</text>
				<text class="subtitle">点击一次开始通话，AI 会自动听你说话、看摄像头并语音回复</text>
			</view>
			<view class="status" :class="{ active: callActive }">
				<text>{{ statusText }}</text>
			</view>
		</view>

		<view v-if="secureWarning" class="warning">
			<text>{{ secureWarning }}</text>
		</view>

		<view class="api-config">
			<text class="api-label">后端地址</text>
			<input class="api-input" v-model="apiBaseUrl" placeholder="http://192.168.0.5:8084" />
			<button class="api-button" @click="saveApiBaseUrl">保存</button>
		</view>

		<view class="workspace">
			<view class="camera-panel">
				<!-- #ifdef H5 -->
				<video ref="cameraVideo" class="camera-video" autoplay muted playsinline></video>
				<canvas ref="frameCanvas" class="frame-canvas"></canvas>
				<!-- #endif -->

				<!-- #ifndef H5 -->
				<view class="unsupported">
					<text>当前页面先支持 H5 调试。App/小程序摄像头需要后续按端适配。</text>
				</view>
				<!-- #endif -->

				<view class="call-actions">
					<button class="call-button" :class="{ danger: callActive }" @click="toggleVideoCall" :disabled="processing">
						{{ callActive ? '结束视频通话' : '开始视频通话' }}
					</button>
				</view>
			</view>

			<view class="chat-panel">
				<scroll-view class="messages" scroll-y :scroll-top="scrollTop">
					<view v-for="(item, index) in messages" :key="index" class="message" :class="item.role">
						<text class="message-role">{{ item.role === 'user' ? '我' : 'AI' }}</text>
						<text class="message-text">{{ item.text }}</text>
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script>
import { assist, getBaseUrl, setBaseUrl, speechToText, textToSpeech } from '../../utils/api.js'

const RECORD_SEGMENT_MS = 4500

export default {
	data() {
		return {
			stream: null,
			mediaRecorder: null,
			recordTimer: null,
			currentPlayer: null,
			playbackToken: 0,
			recordedChunks: [],
			callActive: false,
			cameraReady: false,
			listening: false,
			processing: false,
			secureWarning: '',
			apiBaseUrl: '',
			messages: [
				{
					role: 'ai',
					text: '点击“开始视频通话”后直接说话，我会自动结合摄像头画面回答。'
				}
			],
			scrollTop: 0
		}
	},
	computed: {
		statusText() {
			if (this.processing) {
				return 'AI 正在思考'
			}
			if (this.listening) {
				return '正在听你说话'
			}
			if (this.callActive) {
				return '视频通话中'
			}
			return '未开始'
		}
	},
	onLoad() {
		this.apiBaseUrl = getBaseUrl()
		this.checkSecureContext()
	},
	beforeDestroy() {
		this.endVideoCall()
	},
	methods: {
		checkSecureContext() {
			// #ifdef H5
			const host = window.location.hostname
			const isLocalhost = host === 'localhost' || host === '127.0.0.1'
			const isHttps = window.location.protocol === 'https:'
			if (!isLocalhost && !isHttps) {
				this.secureWarning = '当前地址不是 localhost 或 HTTPS，浏览器会阻止摄像头和麦克风。建议在笔记本本机用 http://localhost:8080 打开前端，再把后端地址填成开发机 IP。'
			}
			// #endif
		},
		saveApiBaseUrl() {
			setBaseUrl(this.apiBaseUrl)
			this.apiBaseUrl = getBaseUrl()
			uni.showToast({ title: '后端地址已保存', icon: 'none' })
		},
		async toggleVideoCall() {
			if (this.callActive) {
				this.endVideoCall()
				return
			}
			await this.startVideoCall()
		},
		async startVideoCall() {
			// #ifndef H5
			uni.showToast({ title: '当前先支持 H5 调试', icon: 'none' })
			return
			// #endif

			// #ifdef H5
			try {
				this.saveApiBaseUrl()
				if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
					throw new Error('浏览器不允许访问摄像头：请用 localhost 或 HTTPS 打开页面')
				}
				this.stream = await navigator.mediaDevices.getUserMedia({
					video: {
						width: { ideal: 1280 },
						height: { ideal: 720 },
						facingMode: 'user'
					},
					audio: {
						echoCancellation: true,
						noiseSuppression: true,
						autoGainControl: true
					}
				})
				const video = this.getCameraVideo()
				video.srcObject = this.stream
				await video.play()
				this.cameraReady = true
				this.callActive = true
				this.pushMessage('ai', '视频通话已开始，你可以直接说话。')
				this.startRecordSegment()
			} catch (error) {
				this.pushMessage('ai', error.message || '无法开始视频通话')
				this.endVideoCall()
			}
			// #endif
		},
		endVideoCall() {
			this.stopAllPlayback()
			this.callActive = false
			this.listening = false
			this.processing = false
			if (this.recordTimer) {
				clearTimeout(this.recordTimer)
				this.recordTimer = null
			}
			if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
				try {
					this.mediaRecorder.stop()
				} catch (error) {
					console.warn('停止录音失败', error)
				}
			}
			if (this.stream) {
				this.stream.getTracks().forEach(track => track.stop())
			}
			this.mediaRecorder = null
			this.stream = null
			this.cameraReady = false
		},
		startRecordSegment() {
			if (!this.callActive || this.processing || !this.stream) {
				return
			}
			try {
				const audioTracks = this.stream.getAudioTracks()
				if (!audioTracks.length) {
					throw new Error('没有可用的麦克风音频轨')
				}
				this.recordedChunks = []
				const audioStream = new MediaStream(audioTracks)
				const options = this.getRecorderOptions()
				this.mediaRecorder = new MediaRecorder(audioStream, options)
				this.mediaRecorder.ondataavailable = event => {
					if (event.data && event.data.size > 0) {
						this.recordedChunks.push(event.data)
					}
				}
				this.mediaRecorder.onerror = event => {
					this.pushMessage('ai', event.error ? event.error.message : '录音发生错误')
					this.listening = false
				}
				this.mediaRecorder.onstop = this.handleRecordingStop
				this.mediaRecorder.start()
				this.listening = true
				this.recordTimer = setTimeout(() => {
					this.stopRecordSegment()
				}, RECORD_SEGMENT_MS)
			} catch (error) {
				this.listening = false
				this.pushMessage('ai', error.message || '无法开始录音')
			}
		},
		stopRecordSegment() {
			if (this.recordTimer) {
				clearTimeout(this.recordTimer)
				this.recordTimer = null
			}
			if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
				this.mediaRecorder.stop()
			}
			this.listening = false
		},
		getRecorderOptions() {
			if (!window.MediaRecorder) {
				throw new Error('当前浏览器不支持 MediaRecorder')
			}
			if (MediaRecorder.isTypeSupported('audio/webm;codecs=opus')) {
				return { mimeType: 'audio/webm;codecs=opus' }
			}
			if (MediaRecorder.isTypeSupported('audio/webm')) {
				return { mimeType: 'audio/webm' }
			}
			return undefined
		},
		async handleRecordingStop() {
			this.listening = false
			if (!this.callActive || !this.recordedChunks.length) {
				return
			}
			this.processing = true
			try {
				const blob = new Blob(this.recordedChunks, { type: 'audio/webm' })
				const transcription = await speechToText(blob)
				const userText = transcription && transcription.text ? transcription.text.trim() : ''
				if (!userText) {
					this.processing = false
					this.scheduleNextSegment()
					return
				}
				this.stopAllPlayback()
				this.pushMessage('user', userText)
				const imageDataUrl = this.captureFrame()
				const result = await assist({
					userText,
					imageDataUrl,
					userHint: '这是视频通话中的当前摄像头画面，请结合画面自然回答。'
				})
				const answer = result.answer || '没有收到 AI 回复'
				this.pushMessage('ai', answer)
				this.playAnswer(answer)
			} catch (error) {
				this.pushMessage('ai', error.message || '本轮视频对话失败')
			} finally {
				this.processing = false
				this.scheduleNextSegment()
			}
		},
		scheduleNextSegment() {
			if (!this.callActive) {
				return
			}
			setTimeout(() => {
				this.startRecordSegment()
			}, 600)
		},
		captureFrame() {
			const video = this.getCameraVideo()
			const canvas = this.getFrameCanvas()
			if (!video || !canvas || !video.videoWidth) {
				throw new Error('摄像头画面还没有准备好')
			}
			const width = 640
			const height = Math.round(width * video.videoHeight / video.videoWidth)
			canvas.width = width
			canvas.height = height
			const context = canvas.getContext('2d')
			context.drawImage(video, 0, 0, width, height)
			return canvas.toDataURL('image/jpeg', 0.72)
		},
		async playAnswer(text) {
			this.stopAllPlayback()
			const token = ++this.playbackToken
			try {
				const audio = await textToSpeech(text)
				// 新一轮用户语音会递增 playbackToken，旧 TTS 即使晚返回也不能播放。
				if (token !== this.playbackToken || !this.callActive) {
					return
				}
				if (!audio || !audio.audioBase64) {
					return
				}
				const source = `data:audio/${audio.audioFormat || 'mp3'};base64,${audio.audioBase64}`
				const player = new Audio(source)
				this.currentPlayer = player
				player.onended = () => {
					if (this.currentPlayer === player) {
						this.currentPlayer = null
					}
				}
				await player.play()
			} catch (error) {
				console.warn('TTS 播放失败', error)
			}
		},
		stopAllPlayback() {
			this.playbackToken += 1
			if (this.currentPlayer) {
				try {
					this.currentPlayer.pause()
					this.currentPlayer.currentTime = 0
					this.currentPlayer.src = ''
					this.currentPlayer.load()
				} catch (error) {
					console.warn('停止播放失败', error)
				}
				this.currentPlayer = null
			}
		},
		pushMessage(role, text) {
			this.messages.push({ role, text })
			this.$nextTick(() => {
				this.scrollTop += 999
			})
		},
		getCameraVideo() {
			const ref = this.$refs.cameraVideo
			if (ref && ref.tagName === 'VIDEO') {
				return ref
			}
			if (ref && ref.$el) {
				const video = ref.$el.tagName === 'VIDEO' ? ref.$el : ref.$el.querySelector('video')
				if (video) {
					return video
				}
			}
			const video = this.$el.querySelector('video.camera-video')
			if (!video) {
				throw new Error('没有找到摄像头视频元素')
			}
			return video
		},
		getFrameCanvas() {
			const ref = this.$refs.frameCanvas
			if (ref && ref.tagName === 'CANVAS') {
				return ref
			}
			if (ref && ref.$el) {
				const canvas = ref.$el.tagName === 'CANVAS' ? ref.$el : ref.$el.querySelector('canvas')
				if (canvas) {
					return canvas
				}
			}
			const canvas = this.$el.querySelector('canvas.frame-canvas')
			if (!canvas) {
				throw new Error('没有找到截图画布')
			}
			return canvas
		}
	}
}
</script>

<style>
page {
	background: #f3f5f8;
}

.page {
	min-height: 100vh;
	padding: 28rpx;
	box-sizing: border-box;
	color: #1f2937;
}

.topbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 24rpx;
}

.title {
	display: block;
	font-size: 44rpx;
	font-weight: 700;
	line-height: 1.2;
}

.subtitle {
	display: block;
	margin-top: 8rpx;
	font-size: 24rpx;
	color: #64748b;
}

.warning {
	margin-bottom: 20rpx;
	padding: 16rpx 20rpx;
	border: 1rpx solid #f59e0b;
	border-radius: 8rpx;
	background: #fffbeb;
	color: #92400e;
	font-size: 24rpx;
	line-height: 1.5;
}

.api-config {
	display: grid;
	grid-template-columns: auto minmax(0, 1fr) 140rpx;
	gap: 12rpx;
	align-items: center;
	margin-bottom: 20rpx;
	padding: 14rpx;
	border: 1rpx solid #dbe3ee;
	border-radius: 8rpx;
	background: #ffffff;
}

.api-label {
	font-size: 24rpx;
	color: #475569;
}

.api-input {
	height: 64rpx;
	padding: 0 18rpx;
	border: 1rpx solid #cbd5e1;
	border-radius: 8rpx;
	font-size: 24rpx;
}

.api-button {
	height: 64rpx;
	line-height: 64rpx;
	border-radius: 8rpx;
	background: #0f172a;
	color: #ffffff;
	font-size: 24rpx;
}

.status {
	padding: 12rpx 18rpx;
	border-radius: 8rpx;
	background: #e5e7eb;
	color: #475569;
	font-size: 24rpx;
}

.status.active {
	background: #d1fae5;
	color: #047857;
}

.workspace {
	display: grid;
	grid-template-columns: minmax(0, 1.05fr) minmax(360rpx, 0.95fr);
	gap: 24rpx;
}

.camera-panel,
.chat-panel {
	background: #ffffff;
	border: 1rpx solid #dbe3ee;
	border-radius: 8rpx;
	overflow: hidden;
}

.camera-video {
	display: block;
	width: 100%;
	height: 62vh;
	min-height: 560rpx;
	background: #0f172a;
	object-fit: cover;
}

.frame-canvas {
	position: fixed;
	left: -9999px;
	top: -9999px;
	width: 1px;
	height: 1px;
}

.unsupported {
	min-height: 520rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx;
	color: #64748b;
	background: #e2e8f0;
}

.call-actions {
	padding: 18rpx;
	border-top: 1rpx solid #e2e8f0;
}

.call-button {
	width: 100%;
	height: 84rpx;
	line-height: 84rpx;
	border-radius: 8rpx;
	background: #2563eb;
	color: #ffffff;
	font-size: 30rpx;
	border: 1rpx solid #2563eb;
}

.call-button.danger {
	background: #dc2626;
	border-color: #dc2626;
}

.chat-panel {
	display: flex;
	flex-direction: column;
	min-height: 62vh;
}

.messages {
	height: 62vh;
	min-height: 560rpx;
	padding: 18rpx;
	box-sizing: border-box;
}

.message {
	margin-bottom: 18rpx;
}

.message-role {
	display: block;
	margin-bottom: 6rpx;
	font-size: 22rpx;
	color: #64748b;
}

.message-text {
	display: inline-block;
	max-width: 92%;
	padding: 14rpx 18rpx;
	border-radius: 8rpx;
	font-size: 27rpx;
	line-height: 1.6;
	background: #eef2ff;
	color: #1e293b;
}

.message.user {
	text-align: right;
}

.message.user .message-text {
	background: #dcfce7;
}

@media screen and (max-width: 900px) {
	.workspace {
		display: flex;
		flex-direction: column;
	}

	.camera-video,
	.messages {
		height: 44vh;
	}
}
</style>
