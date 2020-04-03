from pyproj import Proj
import numpy as np
import pandas as pd

myProj = Proj(proj='utm', zone=10, ellps='WGS84')
df = pd.read_csv("crimedata_csv_all_years.csv", delimiter= ',')
lon, lat = myProj(100, 100, inverse=True)

def get_lon_lat(x):
    return myProj(x[0], x[1], inverse=True)

df = df.query('TYPE == "Theft of Vehicle" or TYPE == "Theft from Vehicle" or TYPE == "Theft of Bicycle"')

s = df[['X','Y']].apply(get_lon_lat, axis=1)
df['lng'] = s.str[0]
df['lat'] = s.str[1]

df.to_csv("crime_data_csv_all_years_lat_lng3.csv")
