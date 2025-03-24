# File Sharing System


## Overview

Created an app that replicates a file sharing system that could be used by roommates to share important files such as bills and receipts over a central server 

## Primary Functions

The file sharing clients connects to a central server, which responds to a single client command, and  then disconnects. Each time the client needs to issue another command, it reconnects before sending the command. The server responds to the following commands:
- `DIR`
  - Returns a listing of the contents of the shared folder, on the server’s machine
  - The server disconnects immediately after sending the list of files to the client
- `UPLOAD filename`
  - Immediately after the newline after this command will be the contents of a text file
  - The server connects the text from this text file, and save it as a new file filename
  - The server disconnects immediately after saving the text file’s contents
- `DOWNLOAD filename`
  - The server loads the text from the text file filename, and immediately sends that text to the client and disconnects

### Server
The server does not have any user interface, but it is multi-threaded. Each incoming client connection is handled with a separate thread (`ClientHandler`). This thread, and its corresponding socket, remains open only until the command has been handled.

### Client
The client has a simple user interface. When the client is started, the computer name and shared folder path are passed as command-line arguments. The client then shows a split screen showing two
lists (`Jlist`). Both lists consist of filenames. On the left are the list of all files in the clients local folder. On the right will be the list of files in the shared folder of the server.
##
When an `UPLOAD` or `DOWNLOAD` occurs, the user interface will need to refresh both lists of files to show  the newly uploaded or downloaded file. 

When the Refresh button is pressed, a `DIR` command is sent to the server to refresh the server list.

Below is an image of the UI
<div align="center">
    <img src="Screenshot 2025-03-24 134533.png" alt="Assignment 2 Output">
</div>

 
