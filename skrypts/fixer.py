# Open the CSV file in binary mode
with open('D:\\download\\honours_project\\test1\\testData11Test1WifiOnly.csv', 'rb') as file:
    lines = file.readlines()

    # Iterate through the lines of the CSV file
    for i, line in enumerate(lines):
            # Check if the line has more fields than expected (60)
                # Remove the null characters from the line
                line = line.replace(b'\x00', b'')
                # Split the line into fields using a comma separator
                fields = line.split(b',')
                # Keep only the first 60 fields
                lines[i] = b','.join(fields[:60])
                print(f"Fixed line {i + 1}")
                break

# Write the fixed lines back to the CSV file
with open('D:\\download\\honours_project\\skrypts\\your_file.csv', 'wb') as file:
    file.writelines(lines)

print("CSV file fixed successfully.")