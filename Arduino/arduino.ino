
/**
 * Input format: <password(10)>/<instruction>~
*/
#include <Servo.h>

//temp
String password = "123456789";
//temp

//constants
String WRONG_PASSWORD = "wrong_password";
String LOCK_SUCCESS = "lock_success";
String UNLOCK_SUCCESS = "unlock_success";
String UNLOCK_INSTRUCTION = "unlock_instruction";
String LOCK_INSTRUCTION = "lock_instruction";
String CHANGE_PASSWORD_INSTRUCTION = "change_password_instruction";

//Variable for storing received readInput
String readInput = "";    
char bytes;

Servo myservo;

void setup()
{
    Serial.begin(9600);                                
    pinMode(13, OUTPUT);  
    myservo.attach(9);
}
void loop()
{
   if(Serial.available() > 0)      // Send readInput only when you receive readInput:
   {
      delay(10);
      bytes = Serial.read();        //Read the incoming readInput & store into bytes
      if(bytes == '~') 
      {
        String instruction = getInstruction(readInput);
        String inputPassword = getPassword(readInput);
        if(instruction == UNLOCK_INSTRUCTION) 
        {
          unlock(inputPassword);                  
        } 
        else if(instruction == LOCK_INSTRUCTION) 
        {
          lock();
        }
        else if(instruction == CHANGE_PASSWORD_INSTRUCTION) 
        {
          changePassword();
        }
      }
      else 
      {
        readInput += bytes;
      }
   }
}

//gets the password from the input from android device
String getPassword(String readInput)
{
  //TODO: write the parsing code
  return password;
}

//gets the instruction from the input from android device
String getInstruction(String readInput)
{
  //TODO: write the parsing coder
  return UNLOCK_INSTRUCTION;
}

//takes input from android and checks if inputed password matches
void unlock(String inputPassword) 
{
  if(inputPassword == password)
  {
    //send back to android that unlock was successful
    Serial.print(UNLOCK_SUCCESS + "~");      
    for (int pos = 0; pos <= 180; pos += 1) { 
      // goes from 0 degrees to 180 degrees
      // in steps of 1 degree
      myservo.write(pos);              // tell servo to go to position in variable 'pos'
      delay(15);                       // waits 15ms for the servo to reach the position
    }
  } 
  else 
  {
    Serial.print(WRONG_PASSWORD + "~");
  }
}

void lock() 
{
    Serial.print(LOCK_SUCCESS + "~");      
    for (int pos = 0; pos <= 180; pos += 1) { 
      // goes from 0 degrees to 180 degrees
      // in steps of 1 degree
      myservo.write(pos);              // tell servo to go to position in variable 'pos'
      delay(15);                       // waits 15ms for the servo to reach the position
    }
}

//function to change password
void changePassword()
{
  //TODO
}


