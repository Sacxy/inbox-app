# jb-messaging-app
A simple Messaging app inspired from emails but a very stripped down version, with a backend on Spring Boot using Apache Cassandra in the data layer, Spring Security for Authentication using Github, Frontend on Thymeleaf.


---

## Design Ideas for Pages

1. Login Page : Simple Github Login Page **Insert Mockup Images**
2. Home Page : A Folder/Label section which will be carried along to all other pages (Just like in an email), A Counter maybe on the folders to show the number of read an unread messages in their respective folder/label, A Compose Button to well 'compose a message', A centre card showing all the messages (Just like an email)
3. Message Details Page : The folder will be their too, The counter-value to be changed when seeing a new message. the message detail box will have the standard the email structure with the reply and reply-all option buttons and a button to go back
4. Compose Message Page : The folder like Drax will be incredibly still and present again, the standard email structure for composing a message will be there with a send button, a very cool send button (P0 -> Make the button really cool) with a sound when a msg is sent and whenever recieving, like the euro trip "Email Motherfucker"

---

## Cassandra Data Modelling

Some Notes: While in typical RDBMS you would design your data and then your application based around the data and how you are going to use joins and other stuff to desing your app, in the cassandra world its rather the opposite, here you start of designing your data in the way your application works.

Very Scalabe and Very fast!

### Entity Diagram of the Data (Roughly)

![Untitled Workspace](https://user-images.githubusercontent.com/41153916/164339878-a033294f-9c46-4eea-bfe8-dc1541ad5554.png)

- So a User is the Main actor in all of this, So a user can see many messages and a message can be sent to many user hence the many-to-many relationship b/w the user and the message.

- A single user can have many folders/ labels but a single folder cannot be distributed/shared between multiple users hence the one-to-many relationship b/w the user and the label/folder.

- A folder can have many messages and a message can be in many folders (Just like an email) e.g - Inbox, Important, Archive folders can have the same messages hence the many-to-many relationship b/w the label/folder and the message.


### Data Models in Cassandra

Note: In cassandra while designing the model, think of the UI of a page with the perfect table you'll need to render the data. So that is how you are going to build the data model so that the UI loads like the Flash!

- folder_by_user: for each user there is doing to be an associated folder, using the userId as the primary key or the partitioning key in cassandra along wiht the name/label as the clustering column and color, unread count
- messages_by_user_folder: userId as the PK, label PK, messageId, introducing a timestamp for ordering the data in chronological order (Just like an email) so it will be declared as the clustering column in descending order of sorting, again trying to produce the ideal table to show for the given page this will also contain all the message data too... To, Subject, is_read(has the uder read the message or not)

**now we have the need for the messageId to give us the message details whenever the user clicks on any message**

- message_by_id: messageId as the PK, from, to soting the userId, subject and body, not tackling the attachment thingy


so the first problem is the unread counter, how do we decrease the counter by 1 when the user reads a message, in a relational databse we could have made a transcation in which the is_read is being handled and the counter is being decremented in the same transaction, but we don't want the limitations of the RDBMS world still holding a power over us.

Cassandra has an idea of a counter, so we are goin to use that, it does have certain limitations, like when a counter is declared in a table the PK cannot be a counter value and there cannot be a non-counter column not as a primary key, so the first tabke kinda breaks that rule so we are going to create a separated table for counter.

**folder_by_user will now not have the unread count column**

new table

- unread_email_stats: userId (PK), label (C) and num_unread as the counter.

Queries:
1. Q1 user_home_page: calling the folder_by_user and the unread_email_stats


so here is the rough! data model for the cassandra tables to get us going, enough with the drawing stuff now we can code.

![Untitled Workspace (1)](https://user-images.githubusercontent.com/41153916/164343840-d9b7cb97-43c4-4d19-8a78-899c1bacf9dd.png)

## Setting up Hosted Cassandra instance on DataStax AstraDB

very liberal with the free tier can do almost  any experimental work with this tier.

Go to the site and create a new serverless database.

- To connect the cassandra DB to our spring boot application we will be using token based connection, which can be easily generated on the datastax site.
- These Token values are to be kept confidencial, if found by anyone, your DB can be easily accessed and exploited.

## Creating the Spring boot application

Normally you would go to start.spring.io to create a new spring boot application with all the dependencies and you would be good to go, but here we like being lazy so we are going to pick up the boilerplate/starter code for it.

### The starter project contains OAuth and the provider is Github, uses Github Id to Authenticate the users onto our app, it has spring security and has the placeholder for Github login.

- put the clientID and clientSecret in the application.yaml file
- be careful and not expose these keys to the whole world, coz people are vicious lol
- and then you are good to go.

Let me explain a little bit more about the starter, it contains a simple thymleaf template page for login, and a very simple code for authentication

## Connecting Apache Cassandra to our spring boot 

Generate the Token from DataStax.
- change the application.yaml file for the configurations needed to connect to the DB
- Schema-action : This is an interesting thing, there are options like create, create_if_not_exits, none, recreate & recreate_drop_unused. Each have their own purpose to manipulate the schema, when developing choose whatever suits your needs but when in production you wanna change that to none, well coz you don't wanna mess up the tables
- we gonna use recreate_drop_unused : the db gonna be wiped everytime you start your application
- now if i start the app it will start to look for the local cassandra instance, but we have a hosted one, so we gotta tell it where to connect to

Add this to yaml file to tell it where to connect:

    astra.db:
        id: 
        region: 
        keyspace: 
        application.token:

Not Done yet, one last thing left; The *secure-bundle.zip* file

    datastax.astra:
        secure-connect-bundle:

Add this to your application.yaml file and give the location of the secure-connect.zip file which can be downloaded off the connect->Java location in the datastax site.

- You can put the secure-connect.zip file in the resources folder of the project, so you won't need to add the file location, as src/main/resources in any maven project is in the classpath.

Add This to your main java file (InboxApp.java) 

    @Bean
    public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
        Path bundle = astraProperties.getSecureConnectBundle().toPath();
        return builder -> builder.withCloudSecureConnectBundle(bundle);
    }

This is neccessary to have for spring boot application to use the astra secure-connect bundle to connect to the Database 








