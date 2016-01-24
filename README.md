# liffey
Drush for liferay ( command-line interface that leverages the power of the gogo shell ):
* Commands for system administrators to create/delete/update/list users.
* Example: **liffey users list | grep admin**  (does not substitute, but adds power to the gogo shell)
* Very little code (less than 1000 lines of code on its initial version mostly written in Scala)
* Very easily extendable grammar (using Scala parser combinators)
* **TODO**: assign roles to users command, create/delete/update/list roles command.
* **TODO**: create gogo shell scripts for rutinary tasks (create random users, **download and install marketplace applications with a single command** ...)

### How to use it:

    Get help **liffey help**:
![alt tag](https://raw.githubusercontent.com/roclas/liffey/master/documentation/help_command.gif)


    Update and list users **liffey list users** **liffey update user**:
![alt tag](https://raw.githubusercontent.com/roclas/liffey/master/documentation/update_and_list_command.gif)


    Create and delete users **liffey create user** **liffey delete user**:
![alt tag](https://raw.githubusercontent.com/roclas/liffey/master/documentation/create_and_delete_command.gif)


Want to help?:
* Is there any functionality you would like us to include?
* Contact me, or fork the project add something to it and do a pull request...
* Thank you
