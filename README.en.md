# Sky-Take-Out System

## Project Introduction

Sky-Take-Out is a fully-featured food delivery ordering system that includes a management backend and a user-facing application. It supports functions such as dish management, set meal management, order processing, data statistics, user login, etc., meeting the digital operation needs of the catering industry.

## Technical Architecture

- Backend: Spring Boot + MyBatis + MySQL + Redis + WebSocket  
- Frontend: Not included in the code (can be integrated via REST API)  
- File Storage: Alibaba Cloud OSS  
- Payment Integration: WeChat Pay  
- Other Features: JWT authentication, caching, scheduled tasks, global exception handling  

## Main Functional Modules

### Management Backend Features

- **Employee Management**: Login, logout, account status management, employee information maintenance  
- **Category Management**: CRUD operations for dish/set meal categories  
- **Dish Management**: Dish information maintenance, taste settings, start/stop sales  
- **Set Meal Management**: Set meal creation, dish association, start/stop sales  
- **Order Management**: Order status management (accept, reject, cancel, dispatch, complete), order queries  
- **Data Statistics**: Revenue statistics, order statistics, user statistics, sales ranking  
- **Shop Management**: Business status setting and query  
- **Dashboard**: Today's data overview, order/dish/set meal statistics  
- **File Upload**: General file upload interface  

### User Application Features

- **User Login**: WeChat authorization login  
- **Address Book Management**: Add, delete, modify, and query delivery addresses; set default address  
- **Dish Browsing**: View dishes by category  
- **Set Meal Browsing**: View set meals by category, view dishes included in a set meal  
- **Shopping Cart Management**: Add items, view cart, clear cart, reduce item quantity  
- **Order Management**: Place orders, make payments, view order history, view order details, cancel orders, "order again" feature, order reminder  
- **Store Status**: Check whether the store is open for business  

## Core Technical Features

- **JWT Authentication**: Separate token verification for admin backend and user app  
- **Redis Caching**: Used for caching shop status and set meal data  
- **WebSocket**: Real-time message push in the management backend  
- **WeChat Pay Integration**: Integrated with WeChat Pay API v3  
- **Global Exception Handling**: Unified exception response format  
- **Automatic Field Filling**: Custom annotations used to automatically fill creation/update time and user info  
- **Scheduled Tasks**: Handle expired and delivered orders  

## Module Structure

- **sky-common**: Common constants, exceptions, utility classes  
- **sky-pojo**: Data Transfer Objects (DTO), Entity classes, View Objects (VO)  
- **sky-server**: Core business logic, controllers, mappers, service implementations, etc.  

## Development and Deployment Requirements

- JDK 1.8+  
- MySQL 5.7+  
- Redis 3.0+  
- Alibaba Cloud OSS account configuration  
- WeChat Pay merchant account configuration  
- Maven 3.0+ for project building  

## API Documentation

API documentation generated using Swagger2:  
- Management backend documentation URL: `/swagger-ui.html`  
- User application documentation URL: `/swagger-ui.html`  

## Security Features

- Comprehensive custom exception handling (account lockout, password errors, login failures, etc.)  
- Interface access control (via JWT interceptor)  
- Database operation security (unified SQL exception handling)  

## Extensibility Design

- All business logic abstracted through interfaces for easy future expansion  
- Cache annotations (`@Cacheable`, `@CacheEvict`) used to improve system performance  
- Modular design with low coupling between functional components  

## Notes

- Third-party service parameters such as WeChat Pay and Alibaba Cloud OSS must be configured in `application.yml`  
- Redis caching is recommended for production environments  
- File upload functionality requires Alibaba Cloud OSS configuration  
- The WeChat Pay callback interface requires a public network accessible URL  

This project is a complete solution for food delivery services, ideal for catering businesses looking to quickly build their own online ordering system.