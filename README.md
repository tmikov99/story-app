# 📖 Interactive Story Platform Backend

This is the backend service for an interactive storytelling application. It provides APIs for user authentication, story creation and management, comments, notifications, and more. It is built with **Spring Boot**, uses **JWT** for authentication, and integrates with **Firebase** for file storage.

---

## 📂 Project Structure

This backend contains the following main features:

- **Authentication** (`/api/auth`)
- **User Management** (`/api/user`)
- **Story Management** (`/api/story`)
- **Page Management** (`/api/page`)
- **Playthrough Tracking** (`/api/playthrough`)
- **Commenting** (`/api/comments`)
- **File Uploads (Firebase)** (`/api/files`)
- **Notifications** (`/api/notifications`)

---

## 🔐 Authentication

- **Login**: `/api/auth/login`
- **Register**: `/api/auth/register`
- **Logout**: `/api/auth/logout`
- **Refresh Token**: `/api/auth/refresh`
- **Email Verification**: `/api/auth/verify`

JWT access tokens are returned in the response body, while refresh tokens are stored in HTTP-only secure cookies.

---

## 👤 User Management

- **Get Current User**: `GET /api/user`
- **Get User by Username**: `GET /api/user/{username}`
- **Update Profile Picture**: `POST /api/user/picture`
- **Change Password**: `PUT /api/user/password`
- **Forgot Password**: `POST /api/user/forgot-password`
- **Reset Password**: `POST /api/user/reset-password`

---

## 📚 Story API

- **Create/Update/Delete Story**:
    - `POST /api/story/create`
    - `PUT /api/story/update/{storyId}`
    - `DELETE /api/story/{id}`
- **Publish/Archive Story**:
    - `PUT /api/story/publish/{storyId}`
    - `PUT /api/story/archive/{storyId}`
- **Like/Favorite Story**:
    - `POST /api/story/like/{storyId}`
    - `POST /api/story/favorite/{storyId}`
- **Story Retrieval**:
    - Get own stories: `GET /api/story/mine`
    - Search stories: `GET /api/story?q=keyword`
    - Get trending/favorite/liked stories
    - Preview and view by ID

---

## 📄 Page Management

- **Get Pages by Story**:
    - `GET /api/page/story/{storyId}`
    - `GET /api/page/{storyId}/page/{pageNumber}`
    - `GET /api/page/story/{storyId}/map`
- **Create/Update/Delete Page**:
    - `POST /api/page/create`
    - `PUT /api/page/{pageId}`
    - `DELETE /api/page/{pageId}`

---

## 🎮 Playthrough System

- **Start/Resume/Delete Playthrough**:
    - `POST /api/playthrough/start/{storyId}`
    - `POST /api/playthrough/{playthroughId}/load`
    - `DELETE /api/playthrough/{playthroughId}`
- **Choose a Page**: `PATCH /api/playthrough/{playthroughId}/choose/{nextPage}`
- **Get Playthrough Info**:
    - `GET /api/playthrough/{playthroughId}`
    - `GET /api/playthrough/{playthroughId}/currentPage`

---

## 💬 Comments

- **Add Comment**: `POST /api/comments/story/{storyId}`
- **Delete Comment**: `DELETE /api/comments/{id}`
- **Get Comments by Story**: `GET /api/comments/story/{storyId}`
- **Get User's Comments**: `GET /api/comments/mine`

---

## 🔔 Notifications

- **List Notifications**: `GET /api/notifications`
- **Mark as Read**: `PUT /api/notifications/read`

---

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.4.4**
- **Spring Security + JWT (via JJWT)**
- **Spring Data JPA**
- **MySQL** (via `mysql-connector-j`)
- **Firebase Admin SDK** for file storage and integration
- **Jakarta Persistence API 3.1**
- **HTTP Client (Apache HttpClient 5)**
- **Email support via Spring Mail**
- **Pagination using Spring `Pageable`**
- **RESTful API design**
- **Testing** with Spring Boot Test and Mockito

---

## 🧪 Running Locally

```bash
# Clone the repo
# Configure application.properties or application.yml
# Add database credentials, JWT secrets, Firebase credentials, etc.
# Run the application
./mvnw spring-boot:run