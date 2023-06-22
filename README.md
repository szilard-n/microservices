# Microservices Backend Challenge

## Project Overview

The purpose of this challenge was to create a simple API based on microservice 
architecture, consisting of two microservices: Product Service and Inventory 
Service. The project simulates an e-commerce website, focusing on product management 
and inventory tracking.

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
- **MockServer**: Used for mocking external endpoints in integration tests.
- **DbRider**: Facilitates managing the database state during integration testing.
- **RestAssured**: Testing framework for API testing, ensuring the correctness of endpoints.

## Installation

1. Clone the project from GitHub
2. Navigate to the project directory by running `cd e-com`
3. Run `docker compose up --build -d`
4. Go to `http://localhost:8181/auth`. Log in using `admin` as username and password.
A new realm called **_e-com_** needs to be created. Inside this realm create two clients: 
   - **access-token**, which is going to use the **_Standard Flow_** and **_Direct access grant_** 
   Authentication flows. This is going to be responsible for generating the access token for
   the user.
   - **product-service**, which is going to use the **_Service account roles_** Authentication flow.
   We also need to enable the **_Client authentication_** capability.
5. Next we need to create the required roles for the API. Navigate to the **_Clients_** tab
and create two roles: **_buyer_** and **__seller__**. Navigate to **__Realm roles__** tab and create
two new roles: **__app_buyer__** and **__app_seller__**. Then click on each of them and associate 
the **__buyer__** role, we previously created, with the **__app_buyer__**, by clicking on the 
**__Actions__** dropdown and clicking **__Add associated role__**. Do the same with **__app_seller__**
and **__seller__** role.
6. Lastly we can create two users int the **__Users__** tab, create passwords for them in the
**__Credentials__** tab and assign them **__seller__** and **__buyer__** roles on the 
**__Role mappings__** tab.

At this point we should be able to use the API with the provided [postman collection](./e-com.postman_collection.json).

## Usage

We can use the provided [postman collection](./e-com.postman_collection.json) to send requests to the API.
There are eight endpoints available. All API requests should go to `http://localhost:8080` and 
`http://keycloak:8181/auth/realms/e-com/protocol/openid-connect/token` for access tokens.
- **Get Access Token**: This endpoint is responsible for generating access tokens that we can
use in the following requests. It requires a body of type `x-www-form-urlencoded` that contains
the `grant_type=password`, `client_id=access-token`, `username={username}` and `password={password`
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