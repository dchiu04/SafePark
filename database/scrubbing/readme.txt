this folder contains two python scripts responsible for cleaning up and importing a CSV into our mysql database. 

the edit_csv script will edit scrub out all data from a CSV that isn't vehicular related and output it as a new .csv

the import_csv script will import the scrubbed csv into the mysql database, replacing all data rows with the new ones.

this is to make updating the data seamless for the future.
