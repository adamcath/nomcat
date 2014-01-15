nomcat
======

Do you occasionally need to deploy WAR files, but hate setting up Tomcat? Then nomcat is for you!

nomcat is a thin wrapper around the embedded Jetty server that just serves WAR files and nothing else.


How to install and run nomcat
-----------------------------

You need Java 6 (JDK). Then:

    $ git clone git@github.com:YOU/nomcat.git
    $ cd DIR_WITH_WAR_FILES
    $ NOMCAT_DIR/nomcat
    
    Nomcat: Serving test1.war at /test1
    Nomcat: Serving test2.war at /test2
    
    ##############################
	# Server up on port 10080!
	##############################

nomcat searches in ".", "./dist", and "./wars" for files ending in .war, 
then serve them up.

Want to skip searching and serve a few specific war files?

	$ nomcat hippo.war 
	
Start on a different port?

    $ sudo nomcat -p 80

    ...

    ##############################
    # Server up on port 80!
    ##############################
    
Get full usage:
	
	$ nomcat -h
