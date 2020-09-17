#include <Arduino.h>

#define PIN_DIRECTION_RIGHT 3
#define PIN_DIRECTION_LEFT 4
#define PIN_MOTOR_PWM_RIGHT 5
#define PIN_MOTOR_PWM_LEFT 6

boolean finished = false;
String command = "";

int leftspeed = 0;
int rightspeed = 0;
int leftdir = 0;
int rightdir = 0;

void setup()

{
  Serial.begin(9600);

  pinMode(PIN_DIRECTION_LEFT, OUTPUT);
  pinMode(PIN_MOTOR_PWM_LEFT, OUTPUT);
  pinMode(PIN_DIRECTION_RIGHT, OUTPUT);
  pinMode(PIN_MOTOR_PWM_RIGHT, OUTPUT);
}

void motor(int pwmspeed, int motor)
{ // motorSpeed is een getal van 1 - 7, dir is de directie van de motor(1=vooruit,-1=achteruit), motor geeft aan welke motor word aangestuurd(0 = motor A, 1 = Motor B)
  bool dir = pwmspeed < 0;
  int motorSpeed = abs(pwmspeed);

  if (motor == 0)
  {
    analogWrite(PIN_MOTOR_PWM_LEFT, motorSpeed);
    digitalWrite(PIN_DIRECTION_LEFT, dir);
  }
  else if (motor == 1)
  {
    analogWrite(PIN_MOTOR_PWM_RIGHT, motorSpeed);
    digitalWrite(PIN_DIRECTION_RIGHT, !dir);
  }
}

int negative(char in)
{
  if (in == '-')
  {
    return -1;
  }
  else
  {
    return 1;
  }
}

void readCommand()
{
  char base = command.charAt(0);

  if (base == 'L')
  {
    int pwm = command.substring(2).toInt() * negative(command.charAt(1));
    leftspeed = pwm;
  }
  else if (base == 'R')
  {
    int pwm = command.substring(2).toInt() * negative(command.charAt(1));
    rightspeed = pwm;
  }
  else if (base == 'A')
  {
    int action = String(command.charAt(1)).toInt();
    if (action == 0)
    {
      //pinMode(biemPin, 1);
    }
    else if (action == 1)
    {
      //pinMode(biemPin, 0);
    }
  }

  motor(leftspeed, 0);
  motor(rightspeed, 1);
}

void loop()
{
  if (finished) {
    readCommand();
    command = "";
    finished = false;
  }

  while (Serial.available()) {
    char in = Serial.read();
    if (in == '\n') {
      finished = true;
    } else {
      command += in;
    }
  }
}