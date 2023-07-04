# Microservices Backend Challenge

## Project Overview

The purpose of this challenge was to create a simple API based on microservice 
architecture, consisting of two microservices: Product Service and Inventory 
Service. The project simulates an e-commerce website, focusing on product management 
and inventory tracking.

## Challenge Requirements

- Keycloak: Set up Keycloak to provide user authentication and authorization.
- JWT: Use JSON Web Tokens (JWT) for secure information transmission and implement authentication and authorization.
- Docker Compose: Use Docker Compose to containerize and deploy Keycloak and the 2 services.
- Server-Server Authentication: Implement server-server authentication between the service 1 and service 2 with Keycloak.
- Documentation: Write a README file and other relevant documentation.
- Testing: Write automated tests to validate the functionality.

## Key Features and Functionalities

- **Product Service**: Handles product-related requests such as product creation, 
removal, purchase, searching, restocking, and product preview.
- **Inventory Service**: Manages inventory by updating the available units 
for a product based on purchase, removal, or restocking requests.
- **Service Authentication**: Both microservices utilize Keycloak for authentication. 
Product Service authenticates with Keycloak using the client credentials flow to obtain 
a token before making requests. Inventory Service receives the token in the request 
header and verifies it with Keycloak for service authentication.
- **Role-based Authorization**: Certain endpoints in the Product Service require user 
authentication with specific roles, while others are accessible without authentication.
User authentication is also implemented with keycloak.
- **Integration Test**: The project includes integration tests to ensure the functionality 
and reliability of each endpoint.

## Technology Stack

- **Docker Compose and Docker**: Used for containerization and managing the deployment 
of the microservices. Each service contains a Dockerfile with two stages: first one to build
the projects and second one to create the image.
- **Spring Boot**: Framework used for building the microservices, providing a robust 
and scalable development environment.
- **Keycloak**: Integrated as the identity and access management solution for authentication 
and authorization.
- **PostgreSQL**: The database is divided into three schemas: one for the Product Service, one for 
the Inventory Service, and one for Keycloak.
- **MockServer**: Used for mocking external endpoints in integration tests.
- **DbRider**: Facilitates managing the database state during integration testing.
- **RestAssured**: Testing framework for API testing, ensuring the correctness of endpoints.

## Installation

1. Clone the project from GitHub
2. Navigate to the project directory by running `cd e-com`
3. Run `docker compose up --build -d`
4. Wait a few seconds for the containers to start and then go to `http://localhost:8181/auth`. Log in using `admin` 
as username and password.
5. Now we need to import our realm that contains the clients, roles, and users. Click on the dropdown
from the upper left corner and click on **__Create Realm__**
6. Press the **__Browse__** button and select the provided [realm export](./realm-export.json)
and press **__Create__**
7. Next step is to add `keycloak` to our **__hosts__** file so that we can send requests to 
`http://keycloak:8181/**`. This ensures that the JWT issuer hostname is the same across our system
and the docker containers.
    - If you are on a **Linux/Unix** based system:
      1. run `sudo nano /etc/hosts` in your terminal.
      2. Append `127.0.0.1  keycloak` to the end of the file.
      3. Press `ctr + x`, then `y` and `enter` to save and exit the file.
    - If you are on a **Windows** machine:
      1. Open the text editor in **Administrator Mode**.
      2. In the text editor, open `C:\Windows\System32\drivers\etc\hosts`.
      3. Append `127.0.0.1  keycloak` to the end of the file.
      4. Save the changes and exit the editor.

At this point we should be able to use the API with the provided [postman collection](./e-com.postman_collection.json).

## Usage

We can use the provided [postman collection](./e-com.postman_collection.json) to send requests to the API.
There are eight endpoints available. All API requests should go to `http://localhost:8080` and 
`http://keycloak:8181/auth/realms/e-com/protocol/openid-connect/token` for access tokens.
- **Get Access Token**: This endpoint is responsible for generating access tokens that we can
use in the following requests. It requires a body of type `x-www-form-urlencoded` that contains
the `grant_type=password`, `client_id=access-token`, `username={username}` and `password={password}`
values.
- **Create Product**: Endpoint responsible for creating new products. It requires an access token
for a user that has a role of **__seller__**. Request body example:
```json
{
    "name": "Java Book",
    "description": "Book about Java",
    "price": 13.99,
    "quantity": 100
}
```
- **Get All Products**: This endpoint will get all the available products. No access token is required.
- **Preview Product**: Will fetch a specific product with more details like availability in inventory.
No access token is required.
- **Search For Product By Name**: will return all the products that contain the given value in their names.
No token required.
- **Delete Product**: Will remove a product, and it's inventory. Access token with role **__seller__**
required.
- **Buy Product**: Will simulate a purchase event and the product's quantity from inventory will be
decremented if available. Access token with role of either **__seller__** or **__buyer__** required. 
Example request body:
```json
{
    "productId": "12e6218a-aa4d-4a79-90ba-3ccd8b6088a3",
    "quantity": 500
}
```
- **Restock**: will simulate the restocking of a product. Access token with role **__seller__**
  required. Example request body:
```json
{
    "productId": "12e6218a-aa4d-4a79-90ba-3ccd8b6088a3",
    "quantity": 20
}
```

## Diagrams

- [Database Relation](./db.png)
- [Architecture](./architecture.png)
