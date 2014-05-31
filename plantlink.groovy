/**
 *  PlantLink
 *
 *  Copyright 2014 Kristopher Kubicki
 *
 * INSTALLATION
 * =========================================
 * 1) Create a new device type (https://graph.api.smartthings.com/ide/devices)
 *     Name: PlantLink
 *     Author: Kristopher Kubicki
 *     Capabilities:
 *         Battery
 *         Water Sensor 
 *         Polling
 *         Signal Strength
 *
 * 2) Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: PlantLink (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * 3) Update device preferences
 *     Click on the new device to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your PlantLink ID. You can find it by logging into https://dashboard.myplantlink.com with Chrome
 *     Then hit Ctrl+Shift+I to bring up the developer interface.  Now when you click on a Link, you'll see a URL
 *     like this: https://dashboard.myplantlink.com/api/v1/plants/6208145588224000/measurements?limit=7
 *     Take the 16 digit number and put that in the "Key" field of your device settings
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
 preferences {
    input("username", "text", title: "Username", description: "Your PlantLink username (usually an email address)")
    input("password", "password", title: "Password", description: "Your PlantLink password")
    input("serial", "serial", title: "Key", description: "Your 16-digit PlantLink Key")
}
 
metadata {
	definition (name: "PlantLink", namespace: "PlantLink", author: "Kristopher Kubicki") {
		capability "Battery"
		capability "Polling"
		capability "Water Sensor"
		capability "Signal Strength"
        attribute "fuel", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
    
// I find if I don't take pictures of my plants, I have no idea what the app is telling me to water. 
        standardTile("fuel", "device.fuel", width: 2, height: 2, canChangeIcon: false, canChangeBackground: true) {
            state "Thirsty", label: 'Thirsty', backgroundColor: "#D2CE90", icon:"st.Outdoor.outdoor2"
            state "OK", label: 'OK', backgroundColor: "#1e9cbb", icon:"st.Outdoor.outdoor2"
            state "Soggy", label: 'Soggy', backgroundColor: "#153591", icon:"st.Outdoor.outdoor2"
        }
        
      
        valueTile("water", "device.water", inactiveLabel: false, decoration: "flat") {
            state "default", label:'${currentValue}%', unit:"Water Sensor"
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
            state "default", label:'${currentValue}%', unit:"Battery"
        }
        valueTile("lqi", "device.lqi", inactiveLabel: false, decoration: "flat") {
            state "default", label:'${currentValue}%', unit:""
        }
        standardTile("refresh", "device.fuel", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        main "fuel"
        details(["fuel","water", "battery", "lqi", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'battery' attribute
	// TODO: handle 'water' attribute
	// TODO: handle 'lqi' attribute
	// TODO: handle 'rssi' attribute

}

// handle commands
def poll() {
    log.debug "Executing PlantLink 'poll'"
    
// Rather than login to the PlantLink authentication API, just use the Authentication Basic 
// protocol, which seems to work just fine for this application

    def String access_token = "$username:$password".toString().bytes.encodeBase64() 
//	log.debug( "Using token access_token ${access_token} for ${settings.serial}" )


	def params = [
		uri:  "https://oso-tech.appspot.com/api/v1/plants/${settings.serial}/measurements?limit=1",
        headers: [
            'Authorization': "Basic ${access_token}"
        ],
    ]
    
 
	httpGet(params) {
		response ->
		log.debug "Request was successful, ${response.data}"
        
// This is the soil moisture representation as a percentage.  Unfortunately its not very useful
// for most people as what is optimial is different for every plant. 
      	def water = response.data.moisture[0].multiply(100).toInteger()
        log.debug( "Water: ${water}" )
		sendEvent(name: 'water', value: water)
        
// LQI is the signal link quality index.  Usually they are on a scale of 0 to 255, but ST
// seems to put it on a scale of 0 to 100
        def  lqi = response.data.signal[0].multiply(100).toInteger()
        log.debug( "Signal: ${lqi}" )
		sendEvent(name: 'lqi', value: lqi)
        
// This is the battery level indicator.  I don't know if its representitive of the actual
// battery level or some kind of battery quality indicator

        def  battery = response.data.battery[0].multiply(100).toInteger()
        log.debug( "Battery: ${battery}" )
		sendEvent(name: 'battery', value: battery)
       
// Defining logic for what the tile says should go here.  I just made some stuff up
//  but a final application should use response.data.predicted_water_needed[0] to determine if the plant 
// needs water right now. I completely inappropriately called this value "fuel"

        def fuel = "Thirsty"
        if(response.data.plant_fuel_level[0] > 0) { 
        	fuel = "OK"
        }
        if(response.data.plant_fuel_level[0] > 1) { 
        	fuel = "Soggy"
        }
        log.debug( "Fuel: ${fuel}" )
		sendEvent(name: 'fuel', value: fuel)
        
        
    }
}
