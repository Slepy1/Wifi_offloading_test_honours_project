



import pandas as pd
import matplotlib.pyplot as plt
from datetime import datetime

# Load the CSV file into a Pandas DataFrame
df = pd.read_csv('D:\\download\\honours_project\\experiment1test3wifilte\\results.csv')

# Convert the "updatedAt" column to datetime
df['updatedAt'] = pd.to_datetime(df['updatedAt'])

# Get the minimum "updatedAt" value
min_updatedAt = df['updatedAt'].min()

# Calculate the time difference in seconds from the minimum "updatedAt" value
df['timeDifference'] = (df['updatedAt'] - min_updatedAt).dt.total_seconds()

# Sort the DataFrame by "timeDifference" column in ascending order
df = df.sort_values('timeDifference', ascending=True)

# Reset the index to represent the sorted order of "timeDifference" values
df = df.reset_index(drop=True)

# Create a line plot of "timeDifference" values with markers at every data point
plt.plot(df['timeDifference'], df.index, marker='o', markersize=4)

# Set x-axis label
plt.xlabel('Time Difference (seconds)')

# Set y-axis label
plt.ylabel('Index')

# Set title
plt.title('Graph of Time Difference (seconds) with Markers')

# Show the plot
plt.show()