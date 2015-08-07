curl -s "http://www.behindthename.com/random/random.php?number=1&gender=m&surname=&all=yes" | grep "/name/" | sed 's/.*\/name\///' | sed 's/".*//'

