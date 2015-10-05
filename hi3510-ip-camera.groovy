/**
* hi3510 ip camera device type
*
* This implementation supports snapshots (low resolution), pan/tilt, and IR LED control.
*
* Known supported cameras:
*     	Tenvis IPROBOT3 (2014)
*     	Foscam 9821 / 8608W
*		Foscam 8608W
*		InStar (unknown models)
*		Wansview NCH536MW
* and probably several others.
*
* To test if your camera is supported, open a web browser and go to:
* 		http://(camera ip address):(camera port)/cgi-bin/hi3510/param.cgi?cmd=getinfrared
* Type in your camera's username/password. The response will be similar to:
*		var infraredstat= ...
*
* Copyright 2015 uncleskippy
*
* This implementation is based on the Foscam Universal Device by skp19:
*				https://github.com/skp19/st_foscam_universal
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*/

metadata {
	definition (name: "HI3510 Camera Device", namespace: "uncleskippy", author: "uncleskippy") {
			capability "Polling"
			capability "Image Capture"

			attribute "ledStatus",   "string"

			command "ledOn"
			command "ledOff"
			command "ledAuto"

			command "left"
			command "right"
			command "up"
			command "down"
	}

	preferences {
		input("ip", "string", title:"Camera IP Address", description: "Camera IP Address", required: true, displayDuringSetup: true)
		input("port", "string", title:"Camera Port", description: "Camera Port", defaultValue: 80 , required: true, displayDuringSetup: true)
		input("username", "string", title:"Camera Username", description: "Camera Username", required: true, displayDuringSetup: true)
		input("password", "password", title:"Camera Password", description: "Camera Password", required: true, displayDuringSetup: true)
		input("mirror", "bool", title:"Mirror?", description: "Camera Mirrored?")
		input("flip", "bool", title:"Flip?", description: "Camera Flipped?")
	}

	tiles {
		standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "", action: "", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
		}

		carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

		standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
			state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
			state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
		}

		standardTile("ledAuto", "device.ledStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "auto", label: "auto", action: "ledAuto", icon: "st.Lighting.light11", backgroundColor: "#53A7C0"
			state "off", label: "auto", action: "ledAuto", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
			state "on", label: "auto", action: "ledAuto", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
		}

		standardTile("ledOn", "device.ledStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "auto", label: "on", action: "ledOn", icon: "st.Lighting.light11", backgroundColor: "#FFFFFF"
			state "off", label: "on", action: "ledOn", icon: "st.Lighting.light11", backgroundColor: "#FFFFFF"
			state "on", label: "on", action: "ledOn", icon: "st.Lighting.light11", backgroundColor: "#FFFF00"
		}

		standardTile("ledOff", "device.ledStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "auto", label: "off", action: "ledOff", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
			state "off", label: "off", action: "ledOff", icon: "st.Lighting.light13", backgroundColor: "#53A7C0"
			state "on", label: "off", action: "ledOff", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
		}

		standardTile("left", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
			state "left", label: "left", action: "left", icon: ""
		}

		standardTile("right", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
			state "right", label: "right", action: "right", icon: ""
		}

		standardTile("up", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "up", label: "up", action: "up", icon: "st.thermostat.thermostat-up"
		}

		standardTile("down", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
			state "down", label: "down", action: "down", icon: "st.thermostat.thermostat-down"
		}

		standardTile("refresh", "device.alarmStatus", inactiveLabel: false, decoration: "flat") {
			state "refresh", action:"polling.poll", icon:"st.secondary.refresh"
		}

		standardTile("blank", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
			state "blank", label: "", action: "", icon: "", backgroundColor: "#FFFFFF"
		}

		main "camera"
		details(["cameraDetails", "take", "up", "blank", "left", "down", "right", "ledAuto", "ledOn", "ledOff"])
	}
}

//TAKE PICTURE
def take() {
	hubGet("/tmpfs/auto.jpg?", true)
}

def ledOn() {
	log.debug("LED changed to: on")
	sendEvent(name: "ledStatus", value: "on");
	hubGet("/cgi-bin/hi3510/param.cgi?cmd=setinfrared&-infraredstat=open&", false)
}

def ledOff() {
	log.debug("LED changed to: off")
	sendEvent(name: "ledStatus", value: "off");
	hubGet("/cgi-bin/hi3510/param.cgi?cmd=setinfrared&-infraredstat=close&", false)
}

def ledAuto() {
	log.debug("LED changed to: auto")
	sendEvent(name: "ledStatus", value: "auto");
	hubGet("/cgi-bin/hi3510/param.cgi?cmd=setinfrared&-infraredstat=auto&", false)
}

//PTZ CONTROLS
def left() {
	if(mirror == "true") {
		hubGet("/cgi-bin/hi3510/ptzctrl.cgi?-step=1&-act=right&", false);
	} else {
		hubGet("/cgi-bin/hi3510/ptzctrl.cgi?-step=1&-act=left&", false);
	}
}

def right() {
	if(mirror == "true") {
		hubGet("/cgi-bin/hi3510/ptzctrl.cgi?-step=1&-act=left&", false);
	} else {
		hubGet("/cgi-bin/hi3510/ptzctrl.cgi?-step=1&-act=right&", false);
	}
}

def up() {
	if(flip == "true") {
		hubGet("/cgi-bin/hi3510/ptzctrl.cgi?-step=1&-act=down&", false);
	} else {
		hubGet("/cgi-bin/hi3510/ptzctrl.cgi?-step=1&-act=up&", false);
	}
}

def down() {
	if(flip == "true") {
		hubGet("/cgi-bin/hi3510/ptzctrl.cgi?-step=1&-act=up&", false);
	} else {
		hubGet("/cgi-bin/hi3510/ptzctrl.cgi?-step=1&-act=down&", false);
	}
}

def poll() {    
	hubGet("/cgi-bin/hi3510/param.cgi?cmd=getinfrared&", false);
}

private hubGet(def apiCommand, def useS3) {
	//Setting Network Device Id
	def iphex = convertIPtoHex(ip)
	def porthex = convertPortToHex(port)
	device.deviceNetworkId = "$iphex:$porthex"
	//log.debug "Device Network Id set to ${iphex}:${porthex}"
	def hostAddress = "${ip}:${port}"
	log.debug("Executing hubaction on " + hostAddress)
	def uri = apiCommand + "usr=${username}&pwd=${password}"
	log.debug uri
	def hubAction = new physicalgraph.device.HubAction(
		method: "GET",
		path: uri,
		headers: [HOST:hostAddress])
	if(useS3) {
		//log.debug "Outputting to S3"
		hubAction.options = [outputMsgToS3:true]
	} else {
		//log.debug "Outputting to local"
		hubAction.options = [outputMsgToS3:false]
	}
	hubAction
}

def parse(String description) {
	//log.debug "Parsing '${description}'"
	def map = stringToMap(description)
	log.debug map
	def result = []

	if (map.bucket && map.key) {
		putImageInS3(map)
	} else if (map.headers && map.body) {
		if (map.body) {
			def body = new String(map.body.decodeBase64())
			if(body.find("infraredstat=\"auto\"")) {
				log.info("Polled: LED Status Auto")
				sendEvent(name: "ledStatus", value: "auto")
			} else if(body.find("infraredstat=\"open\"")) {
				log.info("Polled: LED Status Open")
				sendEvent(name: "ledStatus", value: "on")
			} else if(body.find("infraredstat=\"close\"")) {
				log.info("Polled: LED Status Close")
				sendEvent(name: "ledStatus", value: "off")
			}
		}
	}
	result
}

def putImageInS3(map) {
	def s3ObjectContent
	try {
		def imageBytes = getS3Object(map.bucket, map.key + ".jpg")
		if(imageBytes) {
			s3ObjectContent = imageBytes.getObjectContent()
			def bytes = new ByteArrayInputStream(s3ObjectContent.bytes)
			//log.debug("PutImageInS3: Storing Image")
			storeImage(getPictureName(), bytes)
		}
	} catch(Exception e) {
		log.error e
	} finally {
		if (s3ObjectContent) {
			s3ObjectContent.close()
		}
	}
}

private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
	"image" + "_$pictureUuid" + ".jpg"
}

private String convertIPtoHex(ipAddress) { 
	String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	return hexport
}
