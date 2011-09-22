GoFleet


# What is this? 

This is a modular open source project to control fleets. Of course, as it is modular
and open source, you can change it the way you want, but it was designed as a framework
to track gis elements (events, issues, people,...). 

It is split on four different parts:
 - Mobile Client: Light client where you can send info related to issues, see where 
are your partners, calculate shortest routes to goals,...
 - Desktop Client: Where you can either administer or use it as a mobile client.
 - Server: Centralizes all the generic work of all clients. Has the whole database.
 - WebService: Closely related to the server, it gives the clients info and functionality.

You can have as many desktop or mobile clients as you want, as long as your server is big enough.

##  I still don't understand what is this project
 
Let's try with a simple example:
 
Imagine you own a transport company. Imagine you can install a tiny computer with a touchscreen and 
a gps on every van. This is the software you need.

You can have a record for every package: which van has it (or has to deliver it), who is driving that
van and, of course, where is it on this precise moment. If you have to deliver a package, you can
see who is nearest to the goal. On a single view, you can see which packages are late and which has 
been delivered in time. You can also see who are the fastest drivers, so you can reward them.

On the van, the driver can calculate the shortest path to his destiny and can see in real time 
all the info about the delivering packages. The driver can also see who's near so they can drink 
beer after work.

But, as it is modular and open source, you can add as many extensions as you can imagine. 

#  I have more questions, where can I ask them? 

Get involved on the mailing list: https://lists.morfeo-project.org/mailman/listinfo/gofleet-devel
