import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.transforms import Bbox
from matplotlib.path import Path

df = pd.read_csv('D:\\download\\honours_project\\skrypts\\test.txt')
df.head()

#BBox = (df.longitude.min(), df.longitude.max(), df.latitude.min(), df.latitude.max()) > (-2.9950, -2.9555, 56.4667, 56.4455)
BBox = (-2.9928, -2.9662, 56.4657, 56.4507)

ruh_m = plt.imread('D:\\download\\honours_project\\skrypts\\map2.png')



fig, ax = plt.subplots(figsize = (8,7))
ax.scatter(df.longitude, df.latitude, zorder=1, alpha= 1, c='b', s=10)
ax.set_title('Plotting Spatial Data on Riyadh Map')
ax.set_xlim(BBox[0],BBox[1])
ax.set_ylim(BBox[2],BBox[3])
ax.imshow(ruh_m, zorder=0, extent=BBox)
#ax.imshow(ruh_m, zorder=0, extent = BBox)#aspect= "equal"
plt.show()