import folium
import webbrowser





class Map:
    def __init__(self, center, zoom_start):
        self.center = center
        self.zoom_start = zoom_start
    
    def showMap(self):
        #Create the map
        my_map = folium.Map(location = self.center, zoom_start = self.zoom_start)

        # Add a marker to the map
        folium.Marker(location=[56.45328214584041, -2.9793285125403544]).add_to(my_map)

        #Display the map
        my_map.save("map.html")
        webbrowser.open("map.html")


#Define coordinates of where we want to center our map
coords = [56.45328214584041, -2.9793285125403544]
map = Map(center = coords, zoom_start = 13)

map.showMap()