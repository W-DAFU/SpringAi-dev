const API_PORT = '8084'
const API_BASE_STORAGE_KEY = 'video_api_base_url'

export function getDefaultBaseUrl() {
	if (typeof window === 'undefined' || !window.location) {
		return `http://localhost:${API_PORT}`
	}
	const hostname = window.location.hostname || 'localhost'
	return `http://${hostname}:${API_PORT}`
}

export function getBaseUrl() {
	if (typeof window === 'undefined' || !window.localStorage) {
		return getDefaultBaseUrl()
	}
	return window.localStorage.getItem(API_BASE_STORAGE_KEY) || getDefaultBaseUrl()
}

export function setBaseUrl(value) {
	if (typeof window === 'undefined' || !window.localStorage) {
		return
	}
	const normalized = value && value.trim() ? value.trim().replace(/\/$/, '') : getDefaultBaseUrl()
	window.localStorage.setItem(API_BASE_STORAGE_KEY, normalized)
}

async function requestJson(path, data) {
	const url = getBaseUrl() + path
	try {
		const response = await fetch(url, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(data)
		})
		const body = await response.json()
		if (!response.ok || body.success === false) {
			throw new Error(body.message || `请求失败：HTTP ${response.status}`)
		}
		return body.data
	} catch (error) {
		throw new Error(`${error.message || '网络请求失败'}，接口地址：${url}`)
	}
}

export function assist(payload) {
	return requestJson('/api/realtime-video/assist', payload)
}

export function textToSpeech(text) {
	return requestJson('/api/realtime-video/text-to-speech', { text })
}

export async function speechToText(audioBlob) {
	const url = getBaseUrl() + '/api/realtime-video/speech-to-text'
	const formData = new FormData()
	formData.append('audioFile', audioBlob, 'recording.webm')

	try {
		const response = await fetch(url, {
			method: 'POST',
			body: formData
		})
		const body = await response.json()
		if (!response.ok || body.success === false) {
			throw new Error(body.message || `语音识别失败：HTTP ${response.status}`)
		}
		return body.data
	} catch (error) {
		throw new Error(`${error.message || '语音识别请求失败'}，接口地址：${url}`)
	}
}
