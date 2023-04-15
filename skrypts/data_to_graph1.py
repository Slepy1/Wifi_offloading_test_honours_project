import pandas as pd
import matplotlib.pyplot as plt
from datetime import timedelta

# Load data from CSV files into separate DataFrames
df1 = pd.read_csv('D:\\download\\honours_project\\skrypts\\battery_v_time\\exp2csv1.csv', names=['Time', 'Battery Percentage'])
df2 = pd.read_csv('D:\\download\\honours_project\\skrypts\\battery_v_time\\exp2csv2.csv', names=['Time', 'Battery Percentage'])
df3 = pd.read_csv('D:\\download\\honours_project\\skrypts\\battery_v_time\\exp2csv3.csv', names=['Time', 'Battery Percentage'])

# Parse the time column as timedelta for each DataFrame
df1['Time'] = pd.to_timedelta(df1['Time'])
df2['Time'] = pd.to_timedelta(df2['Time'])
df3['Time'] = pd.to_timedelta(df3['Time'])

# Convert the timedelta values to total seconds for plotting
df1['Time'] = df1['Time'].dt.total_seconds()
df2['Time'] = df2['Time'].dt.total_seconds()
df3['Time'] = df3['Time'].dt.total_seconds()

# Extract the time and battery percentage data for each DataFrame
time1 = df1['Time']
battery_percentage1 = df1['Battery Percentage']

time2 = df2['Time']
battery_percentage2 = df2['Battery Percentage']

time3 = df3['Time']
battery_percentage3 = df3['Battery Percentage']

# Create a new figure and axis for the plot
fig, ax = plt.subplots()

# Plot the data from each DataFrame on the axis with different colors
ax.plot(time1, battery_percentage1, label='Cellural and WiFi')
ax.plot(time2, battery_percentage2, label='WiFi only')
ax.plot(time3, battery_percentage3, label='Cellural only')

# Add labels and title
ax.set_xlabel('Time (seconds)')
ax.set_ylabel('Battery Percentage')
ax.set_title('Battery Percentage vs. Time')

# Add a legend
ax.legend()

# Show the plot
plt.show()