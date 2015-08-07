killall "Flash Player"
sleep 1
open -a /Applications/Flash\ Player\ 16.app --args "http://tankionline.com/AlternativaLoader.swf?config=c23.tankionline.com/config.xml&resources=s.tankionline.com&lang=de&locale=de"
sleep 5
cd /Users/tanki2/Desktop/TankiBot/
java -jar sirTankiBot.jar
