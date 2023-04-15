import csv
from datetime import datetime

output_list = []

with open('D:\\download\\honours_project\\exp2test3_fin\\testData601Test1WifiOnly.csv') as csv_file:
    file_content = csv_file.read().replace('\0', ' ')
    csv_reader = csv.reader(file_content.splitlines(), delimiter=',')
    line_count = 0

    for row in csv_reader:
        if line_count == 0:
            timedifference = datetime.strptime(row[0].strip(), '%Y-%m-%dT%H:%M:%S.%f')  # remove leading white spaces
            line_count += 1
        
        current_time = datetime.strptime(row[0].strip(), '%Y-%m-%dT%H:%M:%S.%f')  # remove leading white spaces
        
        test = current_time - timedifference
        print(f'\t Timestamp - {row[0]}, Battery - {row[4]}, TimeDifference - {test}')
        output_list.append([test, row[4]])
        #output_list.append(f'\t{test},{row[4]}')
        line_count += 1
    print(f'Processed {line_count} lines.')


with open('D:\\download\\honours_project\\skrypts\\exp2csv3.csv', mode='w', newline='') as test3:
    test3 = csv.writer(test3, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    for row in output_list:
        test3.writerow(row)