import os
import re
import sys
import sqlite3
from openpyxl import load_workbook


NUM_PATTERN = re.compile(r"^(\d+)$")

def get_company_database():
    company_db = "magunas_product_data.db"
    
    try:
        with sqlite3.connect(company_db) as conn:
            cursor = conn.cursor()
            cursor.execute("CREATE table IF NOT EXISTS product_data(id INTEGER PRIMARY KEY NOT NULL, product_description TEXT NOT NULL, package TEXT);")             
    
    except sqlite3.OperationalError:
        return False
    
    return company_db
    
    
def insert_to_database(db, code, desc, pkg):
    if db:
        with sqlite3.connect(db) as conn:
            cursor = conn.cursor()
            cursor.execute("INSERT INTO product_data VALUES (?, ?, ?);", (code, desc, pkg))           
            print(f" => Added: {code} > '{desc}' added")
            
    else:
        print(" Database error!")
        
    return
    
    
def get_data_from_database(db):
    if db:
        with sqlite3.connect(db) as conn:
            cursor = conn.cursor()
            product_codes = cursor.execute("SELECT id FROM product_data;").fetchall()             
            
        return product_codes
          
    else:
        print(" Database error!")
        
        return False
        
    
def get_valid_excel_filenames(filepath="."):
    valid_excel_files = []
    
    for file in os.listdir(filepath):
        if re.match("MAGUNAS[\w\d_]+.xls", file, re.I):
            valid_excel_files.append(file)
            
        else:
            continue
            
    return valid_excel_files
    
    
def update_data():
    excel_fnames = get_valid_excel_filenames()
    
    db = get_company_database()
    
    product_codes = get_data_from_database(db)
    
    if isinstance(product_codes, list):
        product_codes = [int(code[0]) for code in product_codes]
        
        for fname in excel_fnames:
            workbook = load_workbook(fname)
            active_sheet = workbook.active

            for row in active_sheet.rows:
                product_code = row[0].value
                product_desc = row[1].value
                product_pack = row[2].value
            
                if NUM_PATTERN.match(product_code):
                    product_code = int(product_code)
                 
                else:
                    continue
                    
                if product_code not in product_codes:
                    insert_to_database(db, product_code, product_desc, product_pack)
                    
                else:
                    print(f" => Exists: {product_code} exists!")
            
                    continue
                    
        print("\n [√] Data updated successfully! \n")
                    
    else:
        print(" * Data entry error!")
            
if __name__ == "__main__":
    update_data()
