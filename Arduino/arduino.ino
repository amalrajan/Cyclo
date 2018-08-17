
/**
 * Input format: <password(10)>/<instruction>~
*/


//temp
String password = "123456789";
//temp

//constants
String WRONG_PASSWORD = "wrong_password";
String SUCCESS = "success";
String UNLOCK_INSTRUCTION = "unlock_instruction";
String CHANGE_PASSWORD_INSTRUCTION = "change_password_instruction";

//Variable for storing received readInput
String readInput = "";    
char bytes;



void setup()
{
    Serial.begin(9600);                                
    pinMode(13, OUTPUT);  
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
  //TODO: write the parsing code
  return UNLOCK_INSTRUCTION;
}

//takes input from android and checks if inputed password matches
void unlock(String inputPassword) 
{
  if(inputPassword == password)
  {
    //send back to android that unlock was successful
    Serial.print(SUCCESS + "~");      
  } 
  else 
  {
    Serial.print(WRONG_PASSWORD + "~");
  }
}

//function to change password
void changePassword()
{
  //TODO
}


