# PlantLink Device Type
http://build.smartthings.com/projects/plantlink/

Now SmartThings can talk to your PlantLinks. This device type does not actually route your PlantLink Zigbee communication through your SmartThings, but rather, it polls data from the PlantLink API.  

## Installation

1) Create a new device type (https://graph.api.smartthings.com/ide/devices)
    *  Name: PlantLink
    *  Author: Kristopher Kubicki
    *  Capabilities:
    *    Battery
    *    Water Sensor 
    *    Polling
    *    Signal Strength

2) Create a new device (https://graph.api.smartthings.com/device/list)
      Name: Your Choice
      Device Network Id: Your Choice
      Type: PlantLink (should be the last option)
      Location: Choose the correct location
      Hub/Group: Leave blank
 
3) Update device preferences
     Click on the new device to see the details.
     Click the edit button next to Preferences
     Fill in your PlantLink ID. You can find it by logging into https://dashboard.myplantlink.com with Chrome
     Then hit Ctrl+Shift+I to bring up the developer interface.  Now when you click on a Link, you'll see a URL
     like this: https://dashboard.myplantlink.com/api/v1/plants/6208145588224000/measurements?limit=7
     Take the 16 digit number and put that in the "Key" field of your device settings

4) Hit Publish -> For Me
