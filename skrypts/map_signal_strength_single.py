from asyncio import sleep
import csv
import datetime
import re
import folium
from folium.plugins import HeatMap

import pandas as pd
import numpy as np

def get_marker_color(signal_strength):
    # Convert signal strength to float
    signal_strength = float(signal_strength)
    
    # Define color scale
    if signal_strength <= -90:
        return 'black'  # Bad signal strength
    elif signal_strength <= -80:
        return 'red'  # Moderate signal strength
    elif signal_strength <= -70:
        return 'orange'
    elif signal_strength <= -60:
        return 'green' # Good signal strength
    elif signal_strength <= -50:
        return 'blue'
    else:
        return 'white' 

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

                             # Extract latitude and longitude from location string
                            match = re.search(r"fused ([-+]?[0-9]*\.?[0-9]+),([-+]?[0-9]*\.?[0-9]+)", location)
                            if match:
                                lat = float(match.group(1))
                                lon = float(match.group(2))
                                locations.append([timestamp, lat, lon, row[2], row[4], ssid1, matched_number])

# Create Folium map
m = folium.Map(location=[locations[0][1], locations[0][2]], zoom_start=14)

for loc in locations:
    timestamp = datetime.datetime.strptime(loc[0], "%Y-%m-%d %H:%M:%S")
    #timestamp = datetime.datetime.strptime(loc[0], "%Y-%m-%dT%H:%M:%S.%f")
    lat = loc[1]
    lon = loc[2]

    #Extract network type

    popup_str = "Network type- " + loc[3] + '\n,' + "Battery- " +  loc[4] + '\n,' + "SSID- " +  loc[5] + '\n,' + "Signal strength- " + '\n,' + loc[6]

    marker_color = get_marker_color(loc[6])
    
# Create marker with the defined color
    folium.Marker([lat, lon], popup=str(popup_str), icon=folium.Icon(color=marker_color)).add_to(m)


m.save("D:\\download\\honours_project\\experiment1test1wifi\\map41.html")


