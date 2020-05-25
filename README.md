# Video-Streaming

Syed Rehman

NOTE: 	Because of lack of time, this program is not multi-platform compatible. 
	I can only gurantee performance for windows-10. 

How to run this software
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


to run on windown (command prompt only) :

cd into 			cd Streaming\src
then run the exact command 	javac p1\Server.java & java p1/Server				 
Note that I used '/' intentionally instead of '\' sometimes so careful
if the command does not work, run them separately -
				
				javac p1\Server.java
				java p1/Server

the program should run on an while loop so do your testing while its running.
Although you should keep an eye on cmd prompt so you know whats going on .


How to test your software
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
To test the program open Chrome (as I tested on Chrome). Search (must be https)-

localhost:8000/    	[note: this will upgrade to https (tls 1.2) automatically, if
 			 it does not, then run the following instead] 
		     
https://localhost:8000/

My website should load. You can check control+shift+i  to check the responses and 
requests. I've included a video without any sound inside of the Streaming folder.

Navigate at your lesure. There are 2 sites.-

	https://localhost:8000/
	https://localhost:8000/myVideo.html   	[note this site will only load the most
						 recently uploaded video as required]
	
Objectives~
1. If you go to-- https://localhost:8000/   while the server is running, you can upload
any mp4 videos of any size. I do not own any other type of video, so I could not test 
with any other types. But I made my server so that I should work with any type of videos.
Please do not test other types until the end just incase it crashes the server.
You would have to then remove any files (folder and all in) vids folder. 	
[ex .. Streaming\src\p1\website\vids\file0]

2. I did step 3 and 4 thus as directed in the documents, skipped this part. 

3. Now once you upload and submit an mp4 video you can see on the cmd prompt that the 
file is (or allready done being uploaded) being downloaded and I created a simple 
process bar with each dot is like 5% complete. After its done downloading it will start 
converting via FFMpeg to HLS format (file0.m3u8) . Once done you will be directed to -  
https://localhost:8000/file-upload 
where the streaming of your video will begin. 

4. Once the video is uploaded and converted you will be auto directed to it. You can 
always manually go to   https://localhost:8000/myVideo.html  to see the lastest video
upload.


The architecture of your software
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

I used java. And I'm strictly using SSLSocket. Where Im using my self certified cert
for tls-1.2. I used FFMpeg as directed to convert mp4 to HLS. I used cmd inside java to 
run FFMpeg and thus converted it. I've used -

https://vjs.zencdn.net/7.7.5/video-js.css    for styling the video player and 
https://vjs.zencdn.net/7.7.5/video.js    for the javascript that call TS files for 
					 streaming. Same as prof did.


For the front end, nothing but html, css and javascript is used. 

