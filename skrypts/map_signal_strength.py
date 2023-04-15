import csv
import datetime
import re
import folium
from folium.plugins import HeatMap

# Read CSV file
filename = "D:\\download\\honours_project\\experiment1test1wifi\\NULremoved.csv"  # Replace with the actual filename
locations = []
network = []

with open(filename, "r") as csvfile:
    reader = csv.reader(csvfile)
    for row in reader:
        if len(row) >= 2:
            timestamp = row[0].strip()  # Remove leading/trailing spaces
            location_str = row[1]

            # Extract latitude and longitude from location string
            match = re.search(r"fused ([-+]?[0-9]*\.?[0-9]+),([-+]?[0-9]*\.?[0-9]+)", location_str)
            if match:
                lat = float(match.group(1))
                lon = float(match.group(2))
                locations.append([timestamp, lat, lon, row[2], row[3]])


# Create Folium map
m = folium.Map(location=[locations[0][1], locations[0][2]], zoom_start=14)

# Add markers to the map
for loc in locations:
    timestamp = datetime.datetime.strptime(loc[0], "%Y-%m-%d %H:%M:%S")
    #timestamp = datetime.datetime.strptime(loc[0], "%Y-%m-%dT%H:%M:%S.%f")
    lat = loc[1]
    lon = loc[2]

    #Extract network type
    if loc[3] == "WIFI" and loc[4] == "No Connection":
        folium.Marker([lat, lon], popup=str(timestamp), icon=folium.Icon(color='red')).add_to(m)
    elif loc[3] == "WIFI" and loc[4] != "No Connection":
        folium.Marker([lat, lon], popup=str(timestamp), icon=folium.Icon(color='green')).add_to(m)
    else:
        folium.Marker([lat, lon], popup=str(timestamp)).add_to(m)

# Save the map to an HTML file







with open("D:\\download\\honours_project\\experiment1test1wifi\\NULremoved.csv", 'r') as file:
    reader = csv.reader(file)
    next(reader)  # Skip header row
    for row in reader:
        # Extract relevant fields
        timestamp = row[0]
        location = row[1]
        ssid = row[5]
        signal_strength = row[9].split('|')

        match = re.search(r"level: ([-+]?[0-9]*\.?[0-9]*\.?[0-9])", row[9])
        if match:
            matched_number = match.group(1)
            print(matched_number)
        # Filter by SSID
        #if ssid == row[5]:
            # Extract latitude, longitude, and signal strength
            #latitude = float(location.split(',')[0].split('=')[1])
            #longitude = float(location.split(',')[1].split('=')[1])
            #signal_strength = int(signal_strength)
            #print(signal_strength)


















m = folium.Map(location=[locations[0][1], locations[0][2]], zoom_start=14)

# Add a heatmap layer with the filtered data
locations = df_filtered.iloc[:, 2].str.split(',', expand=True)
signal_strengths = df_filtered.iloc[:, 8].astype(float)  # Update index to match the field that contains signal strength for the desired SSID
heat_data = [[row[0], row[1], strength] for row, strength in zip(locations.itertuples(index=False), signal_strengths)]
HeatMap(heat_data).add_to(m)

# Display the map
#m.save("D:\\download\\honours_project\\experiment1test1wifi\\map2.html")