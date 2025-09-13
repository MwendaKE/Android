import sqlite3, os, re


old_db = sqlite3.connect("SuperMe.db")
categ = "Novelist"

def convert_duration_to_days(duration_str):
    """
    Convert a duration string like '2 Week(s)' or '3 Month(s)' to number of days.
    Returns 0 if the string is invalid or doesn't match the expected pattern.
    Assumes: 1 Month = 30 days, 1 Week = 7 days.
    """
    if not duration_str or not isinstance(duration_str, str):
        return 0

    duration_str = duration_str.strip()
    parts = duration_str.split()

    if len(parts) != 2:
        return 0

    try:
        number = int(parts[0])
        unit = parts[1].lower()

        if "month" in unit:
            return number * 30
        elif "week" in unit:
            return number * 7
        elif "day" in unit:
            return number
        else:
            return 0
    except ValueError:
        return 0

def get_data(table):
    cursor = old_db.cursor()
    query = cursor.execute(f"SELECT * FROM {table}")
    data = query.fetchall()
    cursor.close()
    
    return data
    
def create_authors_table():
    cursor = old_db.cursor()
    cursor.execute(f"CREATE TABLE IF NOT EXISTS authors_new (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                   "author_name TEXT NOT NULL UNIQUE, " +
                   "author_categ TEXT NOT NULL);");
    cursor.close()

def create_categs_table():
    cursor = old_db.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS categories_new (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                   "category TEXT NOT NULL);");
    cursor.close()

###########
###########    
def create_books_table():
    cursor = old_db.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS books_new (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                   "title TEXT NOT NULL, " +
                   "author INTEGER NOT NULL, " +
                   "review TEXT NOT NULL, " +
                   "category INTEGER NOT NULL, " +
                   "read INTEGER CHECK (read IN (0,1,2)) NOT NULL);"
                   )
    cursor.close()
    
#-------

    
def create_quotes_table():
    cursor = old_db.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS quotes_new (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                   "quote TEXT NOT NULL, " +
                   "author INTEGER NOT NULL, " +
                   "category INTEGER NOT NULL);")
    cursor.close()
    
def create_notes_table():
    cursor = old_db.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS notes (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
				   "datetime TEXT NOT NULL, " +
				   "importance INTEGER CHECK (importance IN (0,1)) NOT NULL, " +
                   "notes TEXT NOT NULL);");
                   
    cursor.close()
    
def create_songs_table():
    cursor = old_db.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS songs_new (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                   "title TEXT NOT NULL, " +
                   "artist INTEGER NOT NULL, " +
                   "lyrics TEXT NOT NULL, " +
                   "genre INTEGER NOT NULL);")
    cursor.close()
    
def create_poems_table():
    cursor = old_db.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS poems_new (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                   "title TEXT NOT NULL, " +
                   "poem TEXT NOT NULL, " +
                   "poet INTEGER NOT NULL);")
    cursor.close()
    
    #==========SUMRYS========
def create_summaries_table():
    cursor = old_db.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS summaries (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				   "title TEXT NOT NULL, " +
                   "author TEXT NOT NULL, " +
				   "favorite INTEGER CHECK (favorite IN (0,1)) NOT NULL, " +
				   "summary TEXT NOT NULL);");
				   
    cursor.close()
    #============
    
def create_articles_table():
    cursor = old_db.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS articles_new (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                   "title TEXT NOT NULL, " +
                   "category INTEGER NOT NULL, " +
                   "body TEXT NOT NULL, " +
                   "writer INTEGER NOT NULL, " +
                   "date TEXT NOT NULL, " +
                   "reference TEXT);")
    cursor.close()
    
def create_tasks_table():
    cursor = old_db.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS tasks_new (" +
                   "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                   "title TEXT NOT NULL, " +
                   "sdate TEXT NOT NULL, " +
                   "stime TEXT, " +
                   "edate TEXT NOT NULL, " +
                   "duration INTEGER NOT NULL, " +
                   "category INTEGER NOT NULL, " +
                   "success INTEGER CHECK (success IN (0,1)) NOT NULL, " +
                   "description TEXT NOT NULL);")
    cursor.close()


###########
###########

def get_books():
    cursor = old_db.cursor()
    cursor.execute("SELECT author, book_title, category, review, book_read FROM books")
    books_data = cursor.fetchall()
    cursor.close()
    
    authors = get_new_authors()
    categs = get_new_categs()
    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for author, title, categ, review, read in books_data:
        author_int = get_author_id(author, authors)
        categ_int = get_categ_id(categ, categs)
        insert_cursor.execute("INSERT OR IGNORE INTO books_new (title, author, review, category, read) VALUES (?,?,?,?,?)", (title, author_int, review, categ_int, read))
        print(f"Inserted '{title}' by '{author}' of id '{author_int}' and category '{categ}'")
        
    old_db.commit()  # Save changes!
    insert_cursor.close()
    
#------
    #=============
def get_summaries():
    sums_path = "/storage/emulated/0/DCI-X/BOOKS/Summaries"
    summaries_data = []
    summaries = os.listdir(sums_path)

    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for s in summaries:
        title_part = s.split("-BY-")
        title = re.split(".txt", title_part[0], re.I)[0] if ".txt".lower() in title_part[0] else title_part[0]
        author = re.split(".txt", title_part[1], re.I)[0] if len(title_part) == 2 else "Unknown Author"
        sumry = open(os.path.join(sums_path, s)).read()
        favorite = 0
        insert_cursor.execute("INSERT OR IGNORE INTO summaries (title, author, favorite, summary) VALUES (?,?,?,?)", (title.strip(), author.strip(), favorite, sumry))
        print(f"Inserted Sumry->'{title}' and author '{author}'")
        
    old_db.commit()  # Save changes!
    insert_cursor.close()
    
    
def get_notes():
    data = [
            ("Laptop Budget",
            "2025-02-11",
            1,
            "Ensure to buy a laptop. Heres the budget breakdown: \n Budget: 30K"
            ),
            
            ("Debts",
            "2025-04-10",
            1,
            "Debts to recover next month: Carol: 5000, Nkirote: 700, Moraa: 1200"
            )
           ]

    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for datum in data:
        title, date, importance, note = datum
        insert_cursor.execute("INSERT OR IGNORE INTO notes (title, datetime, importance, notes) VALUES (?,?,?,?)", (title, date, importance, note))
        print(f"Inserted Note->'{title}' and date '{date}'")
        
        
    old_db.commit()  # Save changes!
    insert_cursor.close()
    
    #==========
def get_quotes():
    cursor = old_db.cursor()
    cursor.execute("SELECT author, quote, category FROM quotes")
    quotes_data = cursor.fetchall()
    cursor.close()
    
    authors = get_new_authors()
    categs = get_new_categs()
    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for author, quote, categ in quotes_data:
        author_int = get_author_id(author, authors)
        categ_int = get_categ_id(categ, categs)
        insert_cursor.execute("INSERT OR IGNORE INTO quotes_new (author, quote, category) VALUES (?,?,?)", (author_int, quote, categ_int))
        print(f"Inserted Quote ->'{author_int}' and category '{categ}'")
        
    old_db.commit()  # Save changes!
    insert_cursor.close()
    
def get_songs():
    cursor = old_db.cursor()
    cursor.execute("SELECT artist_name, song_title, lyries, genre FROM songs")
    quotes_data = cursor.fetchall()
    cursor.close()
    
    authors = get_new_authors()
    categs = get_new_categs()
    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for author, title, lyrics, categ in quotes_data:
        author_int = get_author_id(author, authors)
        categ_int = get_categ_id(categ, categs)
        insert_cursor.execute("INSERT OR IGNORE INTO songs_new (title, artist, lyrics, genre) VALUES (?,?,?,?)", (title, author_int, lyrics, categ_int))
        print(f"Inserted Song ->'{author_int}' and category '{categ}'")
        
        {"songs_new", "songs", "title, artist, lyrics, genre", "title, artist, lyrics, genre"},
			
    old_db.commit()  # Save changes!
    insert_cursor.close()
    
def get_poems():
    cursor = old_db.cursor()
    cursor.execute("SELECT title, body, author FROM poems")
    poems_data = cursor.fetchall()
    cursor.close()
    
    authors = get_new_authors()
    categs = get_new_categs()
    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for title, poem, author in poems_data:
        author_int = get_author_id(author, authors)
        insert_cursor.execute("INSERT OR IGNORE INTO poems_new (title, poem, poet) VALUES (?,?,?)", (title, poem, author_int))
        print(f"Inserted peom ->'{author_int}' and category '{categ}'")
       
    old_db.commit()  # Save changes!
    insert_cursor.close()
    
def get_tasks():
    cursor = old_db.cursor()
    cursor.execute("SELECT task_name, start_date, duration, category, success, description, end_date, start_time FROM tasks")
    tasks_data = cursor.fetchall()
    cursor.close()
   
    categs = get_new_categs()
    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for title, sdate, duration, categ, success, desc, edate, stime in tasks_data:
        categ_int = get_categ_id(categ, categs)
        stime = "0000"
        edate = edate + " " + stime
        duration = convert_duration_to_days(duration)
        insert_cursor.execute("INSERT OR IGNORE INTO tasks_new (title, sdate, stime, edate, duration, category, success, description) VALUES (?,?,?,?,?,?,?,?)", (title, sdate, stime, edate, duration, categ_int, success, desc))
        print(f"Inserted Tasks -> '{title}' category '{categ_int}'")
        
    old_db.commit()  # Save changes!
    insert_cursor.close()
     
#-------
    
def get_new_authors():
    authors = []
    cursor = old_db.cursor()
    cursor.execute("SELECT id, author_name FROM authors_new")
    data = cursor.fetchall()
    cursor.close()
    
    for _id, author in data:
        author_item = (_id, author)
        authors.append(author_item)
        
    return authors
    
def get_new_categs():
    categs = []
    cursor = old_db.cursor()
    cursor.execute("SELECT id, category FROM categories_new")
    data = cursor.fetchall()
    cursor.close()
    
    for _id, categ in data:
        categ_item = (_id, categ)
        categs.append(categ_item)
        
    return categs
    
def get_author_id(name, authors):
    for id, author_name in authors:
        if author_name == name:
            return id
    return 0  # Return None if not found
    
def get_categ_id(name, categs):
    for id, categ_name in categs:
        if categ_name == name:
            return id
    return 0  # Return None if not foun
     
##-------
             
def get_books_authors():
    cursor = old_db.cursor()
    query = cursor.execute("SELECT author_name FROM books_authors")
    data = query.fetchall()
    cursor.close()
    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for author in data:
        insert_cursor.execute("INSERT OR IGNORE INTO authors_new (author_name, author_categ) VALUES (?,?)", (author[0], categ))
        print(f"Inserted {author}")

    old_db.commit()  # Save changes!
    insert_cursor.close()
    
    
def get_quotes_authors():
    cursor = old_db.cursor()
    query = cursor.execute("SELECT author_name FROM quotes_authors")
    data = query.fetchall()
    cursor.close()
    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for author in data:
        insert_cursor.execute("INSERT OR IGNORE INTO authors_new (author_name, author_categ) VALUES (?,?)", (author[0], categ))
        print(f"Inserted {author}")

    old_db.commit()  # Save changes!
    insert_cursor.close()
    
    
def get_songs_artists():
    cursor = old_db.cursor()
    query = cursor.execute("SELECT artist_name FROM songs_artists")
    data = query.fetchall()
    cursor.close()
    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for author in data:
        insert_cursor.execute("INSERT OR IGNORE INTO authors_new (author_name, author_categ) VALUES (?,?)", (author[0], categ))
        print(f"Inserted {author}")

    old_db.commit()  # Save changes!
    insert_cursor.close()
    
    
def get_article_writers():
    cursor = old_db.cursor()
    query = cursor.execute("SELECT writer_name FROM article_writers")
    data = query.fetchall()
    cursor.close()
    
    # Open a new cursor for insert
    insert_cursor = old_db.cursor()
    
    for author in data:
        insert_cursor.execute("INSERT OR IGNORE INTO authors_new (author_name, author_categ) VALUES (?,?)", (author[0], categ))
        print(f"Inserted {author}")
        
    old_db.commit()  # Save changes!
    insert_cursor.close()
    
    
def get_poets():
    cursor = old_db.cursor()
    query = cursor.execute("SELECT poet_name FROM poets")
    data = query.fetchall()
    cursor.close()

    insert_cursor = old_db.cursor()
    for author in data:
        insert_cursor.execute("INSERT OR IGNORE INTO authors_new (author_name, author_categ) VALUES (?,?)", (author[0], categ))
        print(f"Inserted {author}")

    old_db.commit()
    insert_cursor.close()
    
    
def get_song_genres():
    cursor = old_db.cursor()
    query = cursor.execute("SELECT genre FROM songs_genres")
    data = query.fetchall()
    cursor.close()

    insert_cursor = old_db.cursor()
    for genre in data:
        insert_cursor.execute("INSERT OR IGNORE INTO categories_new (category) VALUES (?)", (genre[0],))
        print(f"Inserted '{genre[0]}'")

    old_db.commit()
    insert_cursor.close()
    
    
def get_all_categs():
    cursor = old_db.cursor()
    cursor.execute("SELECT category FROM all_categories")
    data = cursor.fetchall()
    cursor.close()

    # Open a new cursor for insert
    insert_cursor = old_db.cursor()

    for categ in data:
        insert_cursor.execute("INSERT OR IGNORE INTO categories_new (category) VALUES (?)", (categ[0],))
        print(f"Inserted '{categ[0]}'")

    old_db.commit()  # Save changes!
    insert_cursor.close()
    
#--
create_authors_table()
create_categs_table()
#--
create_books_table()
create_quotes_table()
create_songs_table()
create_poems_table()
create_summaries_table()
#create_articles_table()
create_tasks_table()
create_notes_table()
#--
#--
get_books_authors()
get_quotes_authors()
get_songs_artists()
get_article_writers()
get_poets()
get_all_categs()
get_song_genres()
get_summaries()
get_notes()
#--
get_books()
get_tasks()
get_songs()
get_quotes()
get_poems()

        
