## Table of Contents

1. [Team](#team)
2. [Storit](#Storit)
   1. [Overview](##Overview)
   2. [Usage](##Usage)
3. [Development](#Development)
   1. [Storit-Client](##Storit-Client)
   2. [Storit-Firebase](##Storit-Firebase)
   3. [Storit-Server](##Storit-Server)
   4. [Storit-Tracker](##Storit-Tracker)
   5. [Libraries](##Libraries)
   6. [Goals&Accomplishments](##Goals&Accomplishments)
4. [Team](#Team)

# Storit

Storit is a peer to peer distributed cloud storage mobile application.<br/>
Available on Android and IOS.<br/>
The application is not currently live.

<img src="./storit-poster.png" alt="Storit intro poster: store user data on other user's devices" width="350.081" height="600">

## Overview

Storit provides an alternate cloud storage platform to help reduce e-waste by utilizing existing mobile devices storage medium. Storit users can choose to run their device as servers and earn some currency. Users can also pay the monthly fee and gain access to reliable and secure cloud storage.

## Usage

Download the application from the app store. C

# Development

The storit architecture is broken into 4 major components:

- Client (Android/IOS)
- Server (Android/IOS)
- Tracker (Backend-api)
- Firebase (Database/Authentication)

This repo corresponds to Android portion of the client/server component

## Storit-Client

The client is built using android and ios. The client handles authentication using firebase auth by retrieving a token from firebase
and passing it with each request. The client also gives user access to files and folders stored in
storit storage. The client also performs file splitting, encryption, and transfer. On the other hand, it also performs file retrieval, sorting, merging, decryption.

## Storit-Firebase

Firebase api is used to authenticate users either through a local strategy or through google strategy, providing a verifiable token
on successful auth. Firebase is also responsible for storing user and file data in a NOSQL database.

## Storit-Server

The server is built using android and ios. The server constantly runs a server in the background of the device.
It also handles chunk storage and retrieval. It uses static file storage allocation by allocating an large file of devised storage
and reduces the file size upon receiving a new file chunk.

## Storit-Tracker

The tracker is responsible for deciding the number of chunks an their location for each file. The tracker also tracks where all chunks are
located.


## Libraries

Storit uses the following libraries:

- [Android](https://developer.android.com/) - used to develop android version of mobile application
- [Swift](https://developer.apple.com/swift/) - used to develop ios version of mobile application
- [Firebase](https://firebase.google.com/) - used for local and google authentication
- [Firestore](https://firebase.google.com/docs/firestore) - used to store user/file data in NOSQL database
- [Nodejs](https://nodejs.org/en/) - used to program in javascript with v8 javascript engine
- [Express](https://expressjs.com/) - used to develop tracker server and handle socket requests
- [MYSQL](https://www.mysql.com/) - used to track server information
- [socket.io](https://socket.io/) - used to set up peer to peer connection and communicate with tracker
- [Webrtc](https://webrtc.org/) - used to form peer to peer connections and transfer file chunks over data channel
- [Git](https://git-scm.com/) - used to collaborate manage application versions

## Goals&Accomplishments

- [x] Brainstorming of ideas
- [x] Propose ideas
- [x] Feasibility study
- [x] Required document
- [x] Design document
- [x] Complete tracker server
- [x] Network of app (iOS & Android) and File Splitting
- [x] Deploy Tracker
- [x] Authentication and file storage using Firebase
- [x] Integration of UI/UX, tracker, and Networking
- [x] Simple file sharing app (for POC)
- [x] Updated design document
- [x] UI/UX of app (iOS & Android)
- [x] Functional Test
- [x] User Acceptance Test
- [x] Smart algorithm for server selection
- [x] File structure of files
- [x] Encryption of files (iOS & Android)
- [x] Backup file chunks
- [x] Update reports and test results

# Team

- **Team Leader**: Humaid Khan
- **Team Scribe**: Fidel Lim
- **Development Team Members**: Fidel Lim, Humaid Khan, Mahmoud Elmohtaseb
