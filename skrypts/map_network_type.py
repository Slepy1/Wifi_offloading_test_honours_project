import csv
import datetime
import re
import folium

# Read CSV file
filename = "D:\\download\\honours_project\\experiment1test2lte\\planedata.csv"  # Replace with the actual filename
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
    #timestamp = datetime.datetime.strptime(loc[0], "%Y-%m-%d %H:%M:%S")
    timestamp = datetime.datetime.strptime(loc[0], "%Y-%m-%dT%H:%M:%S.%f")
    lat = loc[1]
    lon = loc[2]

    #Extract network type
    if loc[3] == "4G":
        folium.Marker([lat, lon], popup=str(timestamp), icon=folium.Icon(color='green')).add_to(m)
    elif loc[3] == "No Connection":
        folium.Marker([lat, lon], popup=str(timestamp), icon=folium.Icon(color='red')).add_to(m)
    else:
        folium.Marker([lat, lon], popup=str(timestamp), icon=folium.Icon(color='white')).add_to(m)

# Save the map to an HTML file
m.save("D:\\download\\honours_project\\experiment1test2lte\\networktype.html")