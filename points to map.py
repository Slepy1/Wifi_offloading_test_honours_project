from asyncio import sleep
import csv
import datetime
import re
import folium
from folium.plugins import HeatMap

import pandas as pd


locations = []

with open("D:\\download\\honours_project\\experiment1test1wifi\\NULremoved.csv", 'r') as file:
    reader = csv.reader(file)
    next(reader)  # Skip header row
    for row in reader:
        # Extract relevant fields
        timestamp = row[0]
        location = row[1]
        ssid = row[5]

        result = row[9:]


        #signal_strength = result.split(',,')


        for x in result:
            #match = re.search(r'SSID: "(.*?)"', x)
            match = re.search(r'SSID: "(.*?)"', x)
            if match:
                ssid1 = match.group(1)


                if ssid1 == row[5]:
                    match = re.search(r"level: ([-+]?[0-9]*\.?[0-9]*\.?[0-9])", x)
                    if match:
                        if row[5] == ssid1:
                            matched_number = match.group(1)
                            #print(ssid1)
                            #print(matched_number)
                            #print (location)

                             # Extract latitude and longitude from location string
                            match = re.search(r"fused ([-+]?[0-9]*\.?[0-9]+),([-+]?[0-9]*\.?[0-9]+)", location)
                            if match:
                                lat = float(match.group(1))
                                lon = float(match.group(2))
                                locations.append([timestamp, lat, lon, ssid1, matched_number])


# Create Folium map
m = folium.Map(location=[locations[0][1], locations[0][2]], zoom_start=14)

for loc in locations:
    timestamp = datetime.datetime.strptime(loc[0], "%Y-%m-%d %H:%M:%S")
    #timestamp = datetime.datetime.strptime(loc[0], "%Y-%m-%dT%H:%M:%S.%f")
    lat = loc[1]
    lon = loc[2]

    #Extract network type

    popup_str = loc[3] + '\n' + loc[4]

# Save the map to an HTML file
    folium.Marker([lat, lon], popup=str(popup_str), icon=folium.Icon(color='red')).add_to(m)
m.save("D:\\download\\honours_project\\experiment1test1wifi\\map3.html")





        # Filter by SSID
        #if ssid == row[5]:
            # Extract latitude, longitude, and signal strength
            #latitude = float(location.split(',')[0].split('=')[1])
            #longitude = float(location.split(',')[1].split('=')[1])
            #signal_strength = int(signal_strength)
            #print(signal_strength)

