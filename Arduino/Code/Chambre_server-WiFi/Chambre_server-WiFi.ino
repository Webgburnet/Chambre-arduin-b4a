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
* D0 : RX rs232 non utilise
* D1 : TX rs232 non utilise
* D2 : NC
* D3 : NC
* D4 : Digital Output VR monter
* D5 : Digital Output VR descendre
* D6 : Digital Output Prise
* D7 : Digital Output Plafonnier
* D8 : Digital Output Radiateur
* D9 : NC
* D10 : SS Shield-Ethernet-Arduino
* D11 : MOSI Shield-Ethernet-Arduino
* D12 : MISO Shield-Ethernet-Arduino
* D13 : SCK Shield-Ethernet-Arduino
* 
* I2C : Grove Afficheur LCD RGB
* I2C : Grove Barometre
* 
* Shield Ethernet sans POE
* Shield Grove
* 
*/
#include <WiFi.h>
#include <WiFiUdp.h>
#include <Wire.h>
#include <SPI.h>
#include <rgb_lcd.h>
#include <DHT.h>

#define DHTPIN A0
#define DHTTYPE DHT11   // DHT 21

#define RELAIS_MONTEE 4
#define RELAIS_DESCENTE 5
#define RELAIS_PRISE 6
#define RELAIS_LUMIERE 7
#define RELAIS_CHAUFFAGE 8

DHT dht(DHTPIN, DHTTYPE);

//Shield Wifi
int status = WL_IDLE_STATUS;
char ssid[] = "Sti-2k18-SIN";         // Nom du réseau wifi
char pass[] = "Sti-2k18-Sin";       // votre mot de passe réseau wifi(utilisez pour WPA ou comme clé pour WEP)
int keyIndex = 0;                 // votre numéro d'index de clé de réseau (nécessaire uniquement pour WEP)
unsigned int localPort = 5500;        // port local sur lequel écouter
WiFiUDP Udp; // Déclaration du type de protocol employé qui sera ici du Udp

String message2="";
String entrer_clavier="";

rgb_lcd lcd;
const int colorR = 255;
const int colorG = 0;
const int colorB = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.println("Debut Setup");
  status = WiFi.begin(ssid,pass);
  Serial.print("SSID : ");
  Serial.println(WiFi.SSID());

  // affiche l'adresse IP du Shield WiFi :
  IPAddress ip= WiFi.localIP();
  WiFi.config(ip);
  Serial.print("Addresse IP : ");
  Serial.println(ip);

  // affiche la longeur du signal reçu :
  long rssi = WiFi.RSSI();
  Serial.print("longeur du Signal (RSSI) : ");
  Serial.print(rssi);
  Serial.println(" dBm");
  Udp.begin(localPort);

  dht.begin();

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
  lcd.print(ip);
 
  delay(1000);
  lcd.clear();
  Serial.println("Fin Setup");
  Serial.println("Liste instruction ");
  Serial.println(" * Montee : Monter le store");
  Serial.println(" * Arrete : Arreter le store");
  Serial.println(" * Descen : Desecndre le store");
  Serial.println(" * ELumiere : Eteindre la lumiere");
  Serial.println(" * ALumiere : Allumer la lumiere");
  Serial.println(" * AChauffage : Allumer le chauffage");
  Serial.println(" * EChauffage : Eteindre le chauffage");
  Serial.println(" * APrise : Allumer les prises");
  Serial.println(" * EPrise : Eteindre les prises");

  Serial.println(" Fin Setup");
}

void loop() {
  // Protocole Udp
  int packetSize = Udp.parsePacket();

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
  
  if(packetSize)
  {
    lcd.clear();
	  
    Serial.print("Taille du packet recu : ");
    Serial.println(packetSize);
    Serial.print("de l'adresse IP ");
    IPAddress remoteIp = Udp.remoteIP();
    Serial.print(remoteIp);
    Serial.print(", sur le port ");
    Serial.println(Udp.remotePort());
    // read the packet into packetBufffer
    int len = Udp.read(packetBuffer, 255);
    if (len > 0) {
      packetBuffer[len] = 0;
    }
    message2=packetBuffer;
    Serial.println("Contents:");
    Serial.print(packetBuffer);
    Serial.print(" ; ");
    Serial.println(message2);
  
    lcd.setCursor(0,0);
    lcd.print("Msg Udp:");
    lcd.setCursor(8,0);
    lcd.print(message2);
    lcd.setCursor(0,1);
    lcd.print("Taille Msg Udp:");
    lcd.setCursor(15,1);
    lcd.print(packetSize);
    
    delay(500);
    lcd.clear();

    if(message2=="Acquer")
    {
      Serial.print("Le message est : ");
      Serial.print("Capteur actualisé");
	    
      lcd.setCursor(0,0);
      lcd.print("Le message est : ");
      lcd.setCursor(0,1);
      lcd.print("Capteur actualisé");
      
      delay(1);
      lcd.clear();
      
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      
      Udp.write("Temperature = ");
      Udp.print(t);
      Udp.write(" C");
      Udp.write("\n");
      Udp.write("Humidite = ");
      Udp.print(h);
      Udp.write(" %");
      Udp.write("\n");
      Udp.write("Pression = ");
      Udp.print(pressure);
      Udp.write(" Pa");
      Udp.write("\n");
      Udp.write("Luminosite = ");
      Udp.print(Taux_lux);
      Udp.write(" Lux");
      Udp.write("\n");
      Udp.endPacket();
    }
    else
    {
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      Udp.write(ReplyBuffer);
      Udp.endPacket();
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
	
   if (Serial.available() >0)
   {
	 entrer_clavier=Serial.readString();
   }	   
   
   if(message2=="Arrete" ||entrer_clavier=="Arrete")
   {
    digitalWrite(RELAIS_MONTEE , HIGH);
    digitalWrite(RELAIS_DESCENTE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Arreter Store : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    entrer_clavier="";
    delay(1);
    lcd.clear();
   }

   if(message2=="Montee"||entrer_clavier=="Montee")
   {
    digitalWrite(RELAIS_MONTEE , LOW);
    digitalWrite(RELAIS_DESCENTE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Monter Store : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    entrer_clavier="";
    delay(1);
    lcd.clear();
   }

   if(message2=="Descen"||entrer_clavier=="Descen")
   {
    digitalWrite(RELAIS_MONTEE , HIGH);
    digitalWrite(RELAIS_DESCENTE , LOW);

    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Descendre Store : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    entrer_clavier="";
    delay(1);
    lcd.clear();
   }
   
  if(message2=="APrise"||entrer_clavier=="APrise")
  {
    digitalWrite(RELAIS_PRISE , LOW);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Allumer Prise : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    entrer_clavier="";
    delay(1);
    lcd.clear();
  }
  
  if(message2=="EPrise"||entrer_clavier=="EPrise")
  {
    digitalWrite(RELAIS_PRISE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Eteindre Prise :");
    lcd.setCursor(0,1);
    lcd.print("good");
    entrer_clavier="";
    delay(1);
    lcd.clear();
  }

  if(message2=="ALumie"||entrer_clavier=="ALumie")
  {
    digitalWrite(RELAIS_LUMIERE , LOW);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Allumer Eclairage :");
    lcd.setCursor(0,1);
    lcd.print("Good");
    entrer_clavier="";
    delay(1);
    lcd.clear();
  }
  
  if(message2=="ELumie"||entrer_clavier=="ELumie")
  {
    digitalWrite(RELAIS_LUMIERE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Eteindre Eclairage : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    entrer_clavier="";
    delay(1);
    lcd.clear();
  }

  if(message2=="AChauf"||entrer_clavier=="AChauf")
  {
    digitalWrite(RELAIS_CHAUFFAGE , LOW);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Allumer Chauffage : ");
    lcd.setCursor(0,1);
    lcd.print("good");
    entrer_clavier="";
    delay(1);
    lcd.clear();   
  }
  
  if(message2=="EChauf"||entrer_clavier=="EChauf")
  {
    digitalWrite(RELAIS_CHAUFFAGE , HIGH);
    
    lcd.clear(); 
    lcd.setCursor(0,0);
    lcd.print("Eteindre Chauffage:");
    lcd.setCursor(0,1);
    lcd.print("good");
    entrer_clavier="";
    delay(1);
    lcd.clear();
  }  
}
