/* 
* PROJET DOMOTIQUE STI2D SIN
* 
* Arduino Uno
* 
* A0: Grove Capteur Temperature & Humidité
* A1: Grove Luminosite LDR
* A2: NC
* A3: NC
* A4: NC
* A5: NC
* 
* pin0 : RX rs232 non utilise
* pin1 : TX rs232 non utilise
* pin2 : NC
* pin3 : NC
* 
* pin4 : Digital Output VR monter
* pin5 : Digital Output VR descendre
* pin6 : Digital Output Prise
* pin7 : Digital Output Plafonnier
* pin8 : Digital Output Radiateur
* 
* pin9 : NC
* pin10 : NC
* pin11 : NC
* pin12 : NC
* pin13 : NC 
* 
* I2C : Grove Afficheur LCD RGB
* I2C : Grove Barometre
* 
* Shield Ethernet sans POE
* Shield Grove
* 
*/

#include <Ethernet.h>
#include <EthernetUdp.h>
#include <Wire.h> 
#include <rgb_lcd.h>
#include <SPI.h>
#include <DHT.h>
#include <Seeed_BMP280.h>

#define DHTPIN A0
#define DHTTYPE DHT11   // DHT 11

#define RELAIS_MONTEE 4
#define RELAIS_DESCENTE 5
#define RELAIS_PRISE 6
#define RELAIS_LUMIERE 7
#define RELAIS_CHAUFFAGE 8

DHT dht(DHTPIN, DHTTYPE);

BMP280 bmp280;

//Shield Ethernet sans Aoe Numero 2
byte mac[]={0x90,0xA2,0xDA,0x0F,0x2C,0x28};
IPAddress ip_shield(192,168,1,205);

EthernetUDP UDP;
IPAddress remote = UDP.remoteIP();

rgb_lcd lcd;
const int colorR = 255;
const int colorG = 0;
const int colorB = 0;

void setup() {
  // put your setup code here, to run once:
  Ethernet.begin(mac,ip_shield);
  UDP.begin(5500);
  
  Serial.begin(9600);
  
  dht.begin();

  if(!bmp280.init())
  {
    Serial.println("Device error!");
  }

  pinMode( RELAIS_MONTEE , OUTPUT); //Montee du store
  pinMode( RELAIS_DESCENTE , OUTPUT); //Desecente du store
  pinMode( RELAIS_PRISE , OUTPUT); //Prise
  pinMode( RELAIS_LUMIERE , OUTPUT); //Lumiere
  pinMode( RELAIS_CHAUFFAGE , OUTPUT); //Chauffage

  digitalWrite(RELAIS_MONTEE , HIGH);
  digitalWrite(RELAIS_DESCENTE , HIGH);
  digitalWrite(RELAIS_PRISE , HIGH);
  digitalWrite(RELAIS_LUMIERE , HIGH);
  digitalWrite(RELAIS_CHAUFFAGE , HIGH);
  
  lcd.begin(16, 2);
  lcd.setRGB(colorR, colorG, colorB);                      
  lcd.print("Maquette chambre");
  lcd.setCursor(0,1);
  lcd.print("IP:");
  lcd.setCursor(3,1);
  lcd.print(ip_shield);
 
  delay(1000);
  lcd.clear();
}

void loop() {
  // Protocole UDP
  int Size=UDP.parsePacket();
  char message[Size];
  String message2;
  int entrer_clavier;

  //Variable Capteur temperature et humidité
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  if (isnan(t) || isnan(h)) 
  {
      lcd.setCursor(0,0);
      lcd.print("Pas de DHT11");
      
      Serial.println("Pas de capteur DHT"); 
  } 
  else 
  {
      lcd.setCursor(0,0);
      lcd.print("T=");
      lcd.setCursor(2,0);
      lcd.print(t);
      lcd.setCursor(6,0);
      lcd.print((char)223);
      lcd.setCursor(7,0);
      lcd.print("C H=");
      lcd.setCursor(11,0);
      lcd.print(h);
      lcd.setCursor(15,0);
      lcd.print("%");
  }

  //Taux de lumiere
  int Taux_lux = analogRead(A1);
  lcd.setCursor(0,1);
  lcd.print("L=");
  lcd.setCursor(2,1);
  lcd.print(Taux_lux);
  lcd.setCursor(5,1);
  lcd.print("Lux");
  
  //Capteur Barometrique
   float pressure = bmp280.getPressure()/100;
   float temp=bmp280.getTemperature();
   float alti=bmp280.calcAltitude(pressure)*100;
   
  //Pression atmospherique
  lcd.setCursor(8,1);
  lcd.print("P=");
  lcd.setCursor(10,1);
  lcd.print(pressure);
  lcd.setCursor(14,1);
  lcd.print("Pa");

  if(Size>0)
  {
    lcd.clear();
    UDP.read(message,Size);

    //Convertion du message char UDP en string
    int cases = 0;
    while (cases != Size)
    {
      message2 = message2 + message[cases];
      cases = cases+1;
    }
  
    lcd.setCursor(0,0);
    lcd.print("Msg UDP:");
    lcd.setCursor(8,0);
    lcd.print(message2);
    lcd.setCursor(0,1);
    lcd.print("Taille Msg UDP:");
    lcd.setCursor(15,1);
    lcd.print(Size);
    
    delay(500);
    lcd.clear();

    if(message2=="Acquer")
    {
      lcd.setCursor(0,0);
      lcd.print("Le message est : ");
      lcd.setCursor(0,1);
      lcd.print("Capteur actualisé");
      
      delay(1);
      lcd.clear();
      
      UDP.beginPacket(UDP.remoteIP(), UDP.remotePort());
      
      UDP.write("Temperature = ");
      UDP.print(t);
      UDP.write(" C");
      UDP.write("\n");
      UDP.write("Humidite = ");
      UDP.print(h);
      UDP.write(" %");
      UDP.write("\n");
      UDP.write("Pression = ");
      UDP.print(pressure);
      UDP.write(" Pa");
      UDP.write("\n");
      UDP.write("Luminosite = ");
      UDP.print(Taux_lux);
      UDP.write(" Lux");
      UDP.write("\n");
      UDP.endPacket();
    }
    else
    {
      char  ReplyBuffer[] = "acknowledged";
      UDP.beginPacket(UDP.remoteIP(), UDP.remotePort());
      UDP.write(ReplyBuffer);
      UDP.endPacket();
    }
  }

  /*Liste instruction pour B4A :
   * Montee : Monter le store
   * Arrete : Arreter le store
   * Descen : Desecndre le store
   * ELumiere : Eteindre la lumiere
   * ALumiere : Allumer la lumiere
   * AChauffage : Allumer le chauffage
   * EChauffage : Eteindre le chauffage
   * APrise : Allumer les prises
   * EPrise : Eteindre les prises
   */

   if(message2=="Arrete")
   {
    digitalWrite(RELAIS_MONTEE , HIGH);
    digitalWrite(RELAIS_DESCENTE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Arreter Store : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    
    delay(1);
    lcd.clear();
   }

   if(message2=="Montee")
   {
    digitalWrite(RELAIS_MONTEE , LOW);
    digitalWrite(RELAIS_DESCENTE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Monter Store : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    
    delay(1);
    lcd.clear();
   }

   if(message2=="Descen")
   {
    digitalWrite(RELAIS_MONTEE , HIGH);
    digitalWrite(RELAIS_DESCENTE , LOW);

    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Descendre Store : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    
    delay(1);
    lcd.clear();
   }
   
  if(message2=="APrise")
  {
    digitalWrite(RELAIS_PRISE , LOW);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Allumer Prise : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    
    delay(1);
    lcd.clear();
  }
  
  if(message2=="EPrise")
  {
    digitalWrite(RELAIS_PRISE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Eteindre Prise :");
    lcd.setCursor(0,1);
    lcd.print("good");
    
    delay(1);
    lcd.clear();
  }

  if(message2=="ALumie")
  {
    digitalWrite(RELAIS_LUMIERE , LOW);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Allumer Eclairage :");
    lcd.setCursor(0,1);
    lcd.print("Good");
    
    delay(1);
    lcd.clear();
  }
  
  if(message2=="ELumie")
  {
    digitalWrite(RELAIS_LUMIERE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Eteindre Eclairage : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    
    delay(1);
    lcd.clear();
  }

  if(message2=="AChauf")
  {
    digitalWrite(RELAIS_CHAUFFAGE , LOW);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Allumer Chauffage : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    
    delay(1);
    lcd.clear();   
  }
  
  if(message2=="EChauf")
  {
    digitalWrite(RELAIS_CHAUFFAGE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Eteindre Chauffage:");
    lcd.setCursor(0,1);
    lcd.print("good");
    
    delay(1);
    lcd.clear();
  }  
}
