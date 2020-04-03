import pandas as pd
from sqlalchemy import create_engine

df = pd.read_csv('crime_data_csv_all_years_lat_lng.csv', header = 0)
print(df)


engine = create_engine('mysql://admin:debbysucks@vancouver-vehicular-crimes.c4ztbtstxmqe.us-east-1.rds.amazonaws.com/crimes')
with engine.connect() as conn, conn.begin():
    df.to_sql('vehicular', conn, if_exists='append', index=False)
