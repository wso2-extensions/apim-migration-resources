import xml.etree.ElementTree as ET
import fileinput
from properties import *
from shutil import copyfile

def uncomment_xml(file, key_word):
    """This function will uncomment the code part in file that is with the given key_word"""

    f = open(file, "r+")

    new_text = ""
    while True:
        line = f.readline()
        if not line:
            break
        if line.count(key_word) > 0:
            print('%s found in the code, about to uncomment...' % key_word)
            new_text += line.replace("!-- ", "").replace("--", "")
        else:
            new_text += line
    f.close()
    f = open(file, "w+")
    f.write(new_text)
    print("Successfully uncommented!")
    f.close()


def edit_xml(file, key_word, phrase_to_replace):
    """This function will replace the code part in file by phrase_to_replace after searching by given key_word"""
    f = open(file, "r+")

    phrase = ""
    while True:
        line = f.readline()
        if not line:
            break
        if line.count(key_word) > 0:
            print('%s found in the code, about to change with given phrase...' % key_word)
            phrase += phrase_to_replace
        else:
            phrase += line
    f.close()
    f = open(file, "w+")
    f.write(phrase)
    print("Successfully replaced with given phrase!")
    f.close()

def edit_toml(file, key_word, phrase_to_replace):
    """This function will replace the code part in file by phrase_to_replace after searching by given key_word"""
    f = open(file, "r+")

    phrase = ""
    while True:
        line = f.readline()
        if not line:
            break
        if line.count(key_word) > 0:
            print('%s found in the code, about to change with given phrase' % key_word)
            phrase += phrase_to_replace+'\n'
        else:
            phrase += line
    f.close()
    f = open(file, "w+")
    f.write(phrase)
    print("Successfully replaced with given phrase!")
    f.close()

def master_datasource_conf(path, key, value):
    """Find and replace the elements in xml file"""

    tree = ET.parse(path)
    root = tree.getroot()
    i = 0

    for rank in root.iter(key):
        if i < 3:
            if isinstance(value, list):
                rank.text = value[i]
            else:
                rank.text = value
        i += 1

    tree.write(path)

def conf_deployment_toml(version):
    toml_file = "../data/API-M_%s/deployment.toml" % version
    template_file = '../data/API-M_%s/%s/deployment.toml' % (version, DB_TYPE)

    if DB_TYPE == "mysql":
        copyfile(template_file, toml_file)
        urls = ['url = \"jdbc:mysql://%s:%d/%s?useSSL=false\"' % (HOST, PORT, AM_DB),
               'url = \"jdbc:mysql://%s:%d/%s?useSSL=false\"' % (HOST, PORT, USER_DB),
               'url = \"jdbc:mysql://%s:%d/%s?useSSL=false\"' % (HOST, PORT, REG_DB)]

        default_urls = ['url = \"jdbc:mysql://localhost:3306/amdb?useSSL=false\"',
                       'url = \"jdbc:mysql://localhost:3306/userdb?useSSL=false\"',
                       'url = \"jdbc:mysql://localhost:3306/regdb?useSSL=false\"']

    elif DB_TYPE == "mssql":
        copyfile(template_file, toml_file)
        urls = ['url = \"jdbc:sqlserver://%s:%d;databaseName=%s;SendStringParametersAsUnicode=false\"' % (HOST, PORT, AM_DB),
               'url = \"jdbc:sqlserver://%s:%d;databaseName=%s;SendStringParametersAsUnicode=false\"' % (HOST, PORT, USER_DB),
               'url = \"jdbc:sqlserver://%s:%d;databaseName=%s;SendStringParametersAsUnicode=false\"' % (HOST, PORT, REG_DB)]

        default_urls = ['url = \"jdbc:sqlserver://localhost:1433;databaseName=amdb;SendStringParametersAsUnicode=false\"',
                       'url = \"jdbc:sqlserver://localhost:1433;databaseName=userdb;SendStringParametersAsUnicode=false\"',
                       'url = \"jdbc:sqlserver://localhost:1433;databaseName=regdb;SendStringParametersAsUnicode=false\"']

    elif DB_TYPE == "oracle":
        copyfile(template_file, toml_file)
        urls = ['url = \"jdbc:oracle:thin:@%S:%d/%s\"' % (HOST, PORT, SID)]
        default_url = ['url = \""jdbc:oracle:thin:@localhost:1521/sid\"']

    elif DB_TYPE == "postgresql":
        copyfile(template_file, toml_file)
        urls = ['url = \"jdbc:postgresql://%S:%d/%s\"' % (HOST, PORT, AM_DB),
               'url = \"jdbc:postgresql://%S:%d/%s\"' % (HOST, PORT, USER_DB),
               'url = \"jdbc:postgresql://%S:%d/%s\"' % (HOST, PORT, REG_DB)]

        default_urls = ['url = \"jdbc:postgresql://localhost:5432/amdb\"',
                       'url = \"jdbc:postgresql://localhost:5432/userdb\"',
                       'url = \"jdbc:postgresql://localhost:5432/regdb\"']

    i = 0
    for url in urls:
        edit_toml(toml_file, default_urls[i], url)
        i += 1
    default_username = 'username = \"root\"'
    username = 'username = \"%s\"' % (USER_NAME)
    default_password = 'password = \"root\"'
    password = 'password = \"%s\"' % (PWD)
    edit_toml(toml_file, default_username, username)
    edit_toml(toml_file, default_password, password)

def conf_master_datasource():
    """Configure all the required parts in master-datasource.xml file to work API manager with given type of database in properties file"""

    if DB_TYPE == "mysql":
        file_path = '../data/dbconnectors/mysql/master-datasources.xml'
        url = ['jdbc:mysql://%s:%d/%s?useSSL=false' % (HOST, PORT, REG_DB),
               'jdbc:mysql://%s:%d/%s?useSSL=false' % (HOST, PORT, USER_DB),
               'jdbc:mysql://%s:%d/%s?useSSL=false' % (HOST, PORT, AM_DB)]
        try:
            master_datasource_conf(file_path, 'url', url)
            master_datasource_conf(file_path, 'username', USER_NAME)
            master_datasource_conf(file_path, 'password', PWD)
            print("Successfully configured master-datasource.xml file for MySQL database!")
        except:
            print("ERROR: configuring master datasource for MySQL database!!!")
    elif DB_TYPE == "oracle":
        file_path = '../data/dbconnectors/oracle/master-datasources.xml'
        url = 'jdbc:oracle:thin:%s@%s:%d/%s' % (USER_NAME, HOST, PORT, SID)
        try:
            master_datasource_conf(file_path, 'url', url)
            master_datasource_conf(file_path, 'username', USER_NAME)
            master_datasource_conf(file_path, 'password', PWD)
            print("Successfully configured master-datasource.xml file for Oracle database!")
        except:
            print("ERROR: configuring master datasource for Oracle database!!!")
    elif DB_TYPE == "mssql":
        file_path = '../data/dbconnectors/mssql/master-datasources.xml'
        url = ['jdbc:sqlserver://%s:%d;databaseName=%s;SendStringParametersAsUnicode=false' % (HOST, PORT, REG_DB),
               'jdbc:sqlserver://%s:%d;databaseName=%s;SendStringParametersAsUnicode=false' % (HOST, PORT, USER_DB),
               'jdbc:sqlserver://%s:%d;databaseName=%s;SendStringParametersAsUnicode=false' % (HOST, PORT, AM_DB)]
        try:
            master_datasource_conf(file_path, 'url', url)
            master_datasource_conf(file_path, 'username', USER_NAME)
            master_datasource_conf(file_path, 'password', PWD)
            print("Successfully configured master-datasource.xml file for MSSQL database!")
        except:
            print("ERROR: configuring master datasource for MSSQL database!!!")
    elif DB_TYPE == "postgresql":
        file_path = '../data/dbconnectors/postgresql/master-datasources.xml'
        url = ['jdbc:postgresql://%s:%d/%s' % (HOST, PORT, REG_DB),
               'jdbc:postgresql://%s:%d/%s' % (HOST, PORT, USER_DB),
               'jdbc:postgresql://%s:%d/%s' % (HOST, PORT, AM_DB)]
        try:
            master_datasource_conf(file_path, 'url', url)
            master_datasource_conf(file_path, 'username', USER_NAME)
            master_datasource_conf(file_path, 'password', PWD)
            print("Successfully configured master-datasource.xml file for PostgreSQL database!")
        except:
            print("ERROR: configuring master datasource for PostgreSQL database!!!")
    else:
        print("Database type is invalid!!!")
