# Project Name

## Description

This project is a game where you try and guess images of specific images sent from a server.

## Requirements Checklist

- [x] Send Images
- [x] Client Connection
- [x] Server Evaluations
- [x] Game Menu
- [x] Guessing Game
- [x] Leaderboard
- [x] Display Points
- [x] Graceful Exit
- [x] End Current Game
- [x] Robust Protocol
- [x] Robust Programs

## Running the Program

To run the program, follow these steps:

1. Open a terminal.
2. Navigate to the project directory.
3. Run the command: `gradle runServer -Pport=8888`
4. Open another terminal.
5. Navigate to the project directory.
6. Run the command: `gradle runClient -Pport=8888 -Phost="YOUR IP ADDRESS"`

## Protocol Description

-Client
   -starting
   {
      "type": "name",
      "value": "<Player's Name>"
   }

   -choosingCategory
   {
      "type": "category",
      "value": "<Chosen Category>"
   }

   - Restart
   {
      "type": "restart"
   }

   - makeGuess
   {
      "type": "guess",
      "value": "<Guess Input>"
   }

   - makeAllInGuess
   {
      "type": "allinguess",
      "value": "<Guess Input>"
   }

-Server
   -start
   {
      "type": "hello",
      "value": "Hello, please tell me your name.",
   }

   -restart
   {
      "type": "chooseCategory",
      "value": "Please choose a category: animals (a), cities (c), or leader board (l)"
   }

   -name
   {
      "type": "chooseCategory",
      "value": "Hello [Name], please choose a category: animals (a), cities (c), or leader board (l)"
   }

   -incorrect
   {
      "type": "chooseCategory",
      "value": "Invalid category, please choose a category: animals (a), cities (c), or leader board (l)"
   }

   -category
   {
      "type": "RandomImg",
      "value": "<Base64 Encoded Image>"
   }
   {
      "type": "scoreboard",
      "value": "<Leaderboard Data>"
   }
   {
      "value": "Congrats you ended with [Points] points! Press enter to restart."
   }



## Screen Capture

Watch a short screen capture demonstrating the program [here](https://drive.google.com/file/d/168ldAPV1GSsDC138tw0962ZrGrdVLP2t/view?usp=sharing).

## Robustness Design

I designed the program to be robust by implementing error handling mechanisms and input validation. It should be impossible to crash

## Using UDP Protocol

If we used UDP as the protocol, we would need to have the elements be encrypted and decrypted when sent. (thank god we didnt lol)
