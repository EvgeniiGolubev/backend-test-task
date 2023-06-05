# Project Description
The goal of this project is to develop a REST API for a social media platform. The API enables users to register, login, create posts, exchange messages, follow other users, and view their activity feed.

## Functional Requirements:

### Authentication and Authorization:
<ul>
  <li>Users can register in the system by providing a username, email, and password.</li>
  <li>Users can login to the system by providing valid credentials.</li>
  <li>The API ensures the confidentiality of user data, including password hashing and the use of JWT (JSON Web Token) for authentication.</li>
</ul>

### Post Management:
<ul>
  <li>Users can create new posts by providing text, a title, and attaching images.</li>
  <li>Users can view posts from other users.</li>
  <li>Users can update and delete their own posts.</li>
</ul>

### User Interaction:
<ul>
  <li>Users can send friend requests to other users. The user sending the request remains a subscriber until they unsubscribe themselves.</li>
  <li>If the recipient user accepts the friend request, both users become friends.</li>
  <li>If the recipient user rejects the friend request, the user sending the request remains a subscriber.</li>
  <li>Friends are also subscribers to each other.</li>
  <li>If one friend removes the other from their friends' list, they also unsubscribe. The other user remains a subscriber.</li>
  <li>Friends can send messages to each other.</li>
</ul>

### Subscriptions and Activity Feed:
<ul>
  <li>The user's activity feed displays the latest posts from the users they follow.</li>
  <li>The activity feed supports pagination and sorting by post creation time.</li>
</ul>

### Error Handling:
<ul>
  <li>The API handles and returns error messages for incorrect requests or internal server issues.</li>
  <li>The API validates the input data and returns messages for incorrect formats.</li>
</ul>

### API Documentation:
The API documentation is available at the following address: http://localhost:8080/swagger-ui/index.html

## Technologies and Tools:
<ul>
  <li>Spring Boot for developing the REST API.</li>
  <li>MySQL for data storage.</li>
  <li>Swagger for API documentation.</li>
  <li>To test the functionality of the project, a frontend was set up using Vue.js and Webpack Dev Server.</li>
</ul>

## Project Setup:
<ol>
  <li>Configure the application.properties file by specifying the required data for connecting to your MySQL database. Also, provide your JWT key and the directory for file uploads.</li>
  <li>In the command line, execute the following command: <code>mvn spring-boot:run</code></li>
</ol>
