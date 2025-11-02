## CampusConnect API Usage (Frontend)

This guide documents the API endpoints exposed by the CampusConnect backend for frontend integration. It includes request formats, authentication, and response payloads.

## Base URL and Auth

- Base URL: `http://localhost:8080` (default unless overridden)
- Most endpoints require a JWT in the `Authorization` header.

### Auth Header

```http
Authorization: Bearer <JWT_TOKEN>
```

### Standard Error Response

```json
{
  "errorCode": "VALIDATION_ERROR | NOT_FOUND | UNAUTHORIZED | FORBIDDEN | CONFLICT | UNEXPECTED_ERROR",
  "message": "Human readable message",
  "statusCode": 400,
  "timestamp": "2025-01-01T12:34:56.000"
}
```

## Auth

### Signup (multipart)

```http
POST /api/auth/signup
Content-Type: multipart/form-data
```

Form fields:

- `firstName` (string, required)
- `lastName` (string, required)
- `email` (string, required, must end with @st.knust.edu.gh)
- `password` (string, required, min 8)
- `studentId` (string, required)
- `phoneNumber` (string, required, +233XXXXXXXXX or 0XXXXXXXXX)
- `studentIdImage` (file, required)
- `selfieImage` (file, required)

Response (AuthResponseDTO):

```json
{
  "message": "User created successfully. Please verify your phone number.",
  "token": "<JWT>",
  "user": {
    "id": "string",
    "firstName": "string",
    "lastName": "string",
    "email": "string",
    "studentId": "string",
    "phonNumber": "string",
    "phoneVerified": false,
    "studentIdValidated": false,
    "studentIdValidationScore": 0,
    "verificationStatus": "PENDING_VERIFICATION",
    "studentIdImage": { "url": "string", "publicId": "string" },
    "selfieImage": { "url": "string", "publicId": "string" },
    "rating": 5.0,
    "totalDeliveries": 0,
    "joinedDate": "2025-01-01T12:34:56"
  }
}
```

Note: In this response only, the phone field is currently serialized as `phonNumber`.

### Sign in

```http
POST /api/auth/signin
Content-Type: application/json

{
  "email": "user@st.knust.edu.gh",
  "password": "********"
}
```

Response: same shape as signup response (AuthResponseDTO).

### Get current user

```http
GET /api/auth/me
Authorization: Bearer <JWT>
```

Response (UserProfileResponseDTO):

```json
{
  "id": "string",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "studentId": "string",
  "phoneNumber": "string",
  "phoneVerified": true,
  "studentIdValidated": false,
  "verificationStatus": "PENDING_VERIFICATION",
  "profileImage": "string",
  "rating": 4.75,
  "totalDeliveries": 12,
  "joinedDate": "2025-01-01T12:34:56",
  "isOnline": true,
  "lastSeen": "2025-01-01T12:34:56"
}
```

### Logout

```http
POST /api/auth/logout
Authorization: Bearer <JWT>
```

Response:

```json
"Logged out successfully"
```

### Verify phone

```http
POST /api/auth/verify-phone
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "verificationCode": "123456"
}
```

Response:

```json
"Phone number verified successfully"
```

### Resend verification code

```http
POST /api/auth/resend-verification
Authorization: Bearer <JWT>
```

Response:

```json
"Verification code sent successfully"
```

## Users

### Get user profile by id

```http
GET /api/users/{userId}
Authorization: Bearer <JWT>
```

Response: UserProfileResponseDTO (see above).

### Update user profile

```http
PUT /api/users/profile
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "firstName": "string?",
  "lastName": "string?",
  "phoneNumber": "+233XXXXXXXXX?",
  "profileImage": "string?"
}
```

Response: UserProfileResponseDTO.

### Change password

```http
PUT /api/users/change-password
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "currentPassword": "********",
  "newPassword": "********"
}
```

Response:

```json
"Password changed successfully"
```

### Update online status

```http
PUT /api/users/online-status?isOnline=true|false
Authorization: Bearer <JWT>
```

Response:

```json
"Online status updated"
```

## Trips

### Create trip

```http
POST /api/trips
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "fromLocation": { "type": "CAMPUS|OFF_CAMPUS", "campusLocation": "string?", "offCampusLocation": "string?" },
  "toLocation": { "type": "CAMPUS|OFF_CAMPUS", "campusLocation": "string?", "offCampusLocation": "string?" },
  "departureDate": "string",
  "departureTime": "2025-01-30T15:00:00",
  "availableSeats": 3,
  "pricePerDelivery": 25.50,
  "vehicleType": "CAR|MOTORCYCLE|BICYCLE|WALKING",
  "recurring": false,
  "description": "string?",
  "contactInfo": "string?"
}
```

Response (TripResponseDTO):

```json
{
  "id": "string",
  "travelerId": "string",
  "travelerName": "string",
  "travelerPhone": "string",
  "travelerRating": 5.0,
  "fromLocation": {
    "type": "CAMPUS",
    "campusLocation": "string",
    "offCampusLocation": null
  },
  "toLocation": {
    "type": "OFF_CAMPUS",
    "campusLocation": null,
    "offCampusLocation": "string"
  },
  "departureTime": "2025-01-30T15:00:00",
  "transportMethod": "CAR",
  "maxDeliveries": 3,
  "currentDeliveries": 0,
  "pricePerDelivery": 25.5,
  "isRecurring": false,
  "status": "ACTIVE",
  "description": "string",
  "createdAt": "2025-01-01T12:34:56"
}
```

### List trips (with filters)

```http
GET /api/trips?from=string&to=string&departureDate=2025-01-30T00:00:00&page=1&limit=35
```

Response (PaginatedResponseDTO<TripResponseDTO>):

```json
{
  "data": [
    {
      /* TripResponseDTO */
    }
  ],
  "pagination": {
    "page": 1,
    "totalPages": 10,
    "totalItems": 350
  }
}
```

### Get trip by id

```http
GET /api/trips/{tripId}
```

Response: TripResponseDTO.

### Update trip

```http
PUT /api/trips/{tripId}
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "departureTime": "2025-01-31T15:00:00?",
  "pricePerDelivery": 30.00?,
  "maxDeliveries": 4?,
  "status": "ACTIVE|COMPLETED|CANCELLED"?,
  "description": "string?"
}
```

Response: TripResponseDTO.

### Cancel trip

```http
DELETE /api/trips/{tripId}
Authorization: Bearer <JWT>
```

Response: HTTP 204 No Content.

### Get my trips

```http
GET /api/trips/my-trips?status=ACTIVE|COMPLETED|CANCELLED
Authorization: Bearer <JWT>
```

Response: `Array<TripResponseDTO>`

## Delivery Requests

### Create delivery request

```http
POST /api/delivery-requests
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "pickupLocation": { "type": "CAMPUS|OFF_CAMPUS", "campusLocation": "string?", "offCampusLocation": "string?" },
  "dropoffLocation": { "type": "CAMPUS|OFF_CAMPUS", "campusLocation": "string?", "offCampusLocation": "string?" },
  "itemDescription": "string",
  "itemSize": "SMALL|MEDIUM|LARGE",
  "priority": "NORMAL|HIGH|URGENT",
  "paymentAmount": 10.00,
  "pickupDate": "2025-01-30",
  "pickupTime": "15:30:00",
  "contactInfo": "string",
  "specialInstructions": "string?"
}
```

Response (DeliveryRequestResponseDTO):

```json
{
  "id": "string",
  "userId": "string",
  "userName": "string",
  "userPhone": "string",
  "userEmail": "string",
  "userRating": 4.9,
  "pickupLocation": {
    "type": "CAMPUS",
    "campusLocation": "string",
    "offCampusLocation": null
  },
  "dropoffLocation": {
    "type": "OFF_CAMPUS",
    "campusLocation": null,
    "offCampusLocation": "string"
  },
  "itemDescription": "string",
  "itemSize": "MEDIUM",
  "priority": "NORMAL",
  "paymentAmount": 10.0,
  "pickupDate": "2025-01-30",
  "pickupTime": "15:30:00",
  "contactInfo": "string",
  "specialInstructions": "string",
  "status": "PENDING|MATCHED|IN_TRANSIT|DELIVERED|CANCELLED",
  "matchedTripId": null,
  "completedAt": null,
  "createdAt": "2025-01-01T12:34:56"
}
```

### List available delivery requests

```http
GET /api/delivery-requests
```

Response: `Array<DeliveryRequestResponseDTO>`

### Get delivery request by id

```http
GET /api/delivery-requests/{requestId}
```

Response: DeliveryRequestResponseDTO.

### Get my delivery requests (by status)

```http
GET /api/delivery-requests/my-requests?status=PENDING|MATCHED|IN_TRANSIT|DELIVERED|CANCELLED
Authorization: Bearer <JWT>
```

Response: `Array<DeliveryRequestResponseDTO>`

### Accept delivery request for a trip (traveler only)

```http
POST /api/delivery-requests/{requestId}/accept?tripId={tripId}
Authorization: Bearer <JWT>
```

Response: DeliveryRequestResponseDTO.

### Mark in transit (traveler only)

```http
PUT /api/delivery-requests/{requestId}/in-transit
Authorization: Bearer <JWT>
```

Response: DeliveryRequestResponseDTO.

### Complete delivery (traveler only)

```http
PUT /api/delivery-requests/{requestId}/complete
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "deliveryProof": "string?",
  "notes": "string? (max 500)"
}
```

Response: DeliveryRequestResponseDTO.

### Cancel delivery request (requester only)

```http
DELETE /api/delivery-requests/{requestId}
Authorization: Bearer <JWT>
```

Response:

```json
"Delivery request cancelled successfully"
```

## Messages

### Send message

```http
POST /api/messages
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "conversationId": "userA-userB",
  "content": "Hello there!",
  "type": "TEXT|IMAGE|SYSTEM"
}
```

Response (MessageResponseDTO):

```json
{
  "id": "string",
  "conversationId": "string",
  "senderId": "string|system",
  "senderName": "string|System",
  "content": "string",
  "type": "TEXT",
  "isRead": false,
  "createdAt": "2025-01-01T12:34:56"
}
```

### Get messages in conversation

```http
GET /api/messages/conversations/{conversationId}
Authorization: Bearer <JWT>
```

Response: `Array<MessageResponseDTO>`

### Get my conversation ids

```http
GET /api/messages/conversations
Authorization: Bearer <JWT>
```

Response: `Array<string>`

### Mark conversation as read

```http
PUT /api/messages/conversations/{conversationId}/read
Authorization: Bearer <JWT>
```

Response:

```json
"Messages marked as read"
```

### Get unread count for conversation

```http
GET /api/messages/conversations/{conversationId}/unread-count
Authorization: Bearer <JWT>
```

Response:

```json
{ "unreadCount": 2 }
```

### Send a system message

```http
POST /api/messages/system?conversationId={id}&content={text}
Authorization: Bearer <JWT>
```

Response: MessageResponseDTO.

### Send delivery notification (system)

```http
POST /api/messages/delivery-notification?requesterId={id}&travelerId={id}&content={text}
Authorization: Bearer <JWT>
```

Response: MessageResponseDTO.

## Reviews

### Create review

```http
POST /api/reviews
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "revieweeId": "string",
  "deliveryId": "string",
  "rating": 5,
  "comment": "string?",
  "type": "SENDER|TRAVELER"
}
```

Response (ReviewResponseDTO):

```json
{
  "id": "string",
  "reviewerId": "string",
  "reviewerName": "string",
  "revieweeId": "string",
  "revieweeName": "string",
  "rating": 5,
  "comment": "string",
  "deliveryId": "string",
  "type": "TRAVELER",
  "createdAt": "2025-01-01T12:34:56"
}
```

### Get review by id

```http
GET /api/reviews/{reviewId}
```

Response: ReviewResponseDTO.

### Reviews for a user

```http
GET /api/reviews/user/{userId}
```

Response: `Array<ReviewResponseDTO>`

### Reviews written by a user

```http
GET /api/reviews/user/{userId}/given
```

Response: `Array<ReviewResponseDTO>`

### Reviews for a delivery

```http
GET /api/reviews/delivery/{deliveryId}
```

Response: `Array<ReviewResponseDTO>`

### User rating stats

```http
GET /api/reviews/user/{userId}/rating
```

Response:

```json
{ "averageRating": 4.92, "reviewCount": 13 }
```

### Can user review this delivery

```http
GET /api/reviews/can-review?userId={id}&deliveryId={id}
```

Response:

```json
{ "canReview": true }
```

## Admin (requires ROLE_ADMIN)

```http
GET /api/admin/users
GET /api/admin/users/pending-verification
PUT /api/admin/users/{userId}/verification-status?status=PENDING_VERIFICATION|VERIFIED|REJECTED
PUT /api/admin/users/{userId}/activate
PUT /api/admin/users/{userId}/deactivate
GET /api/admin/stats
```

Responses:

- Users: `Array<UserProfileResponseDTO>`
- Verification update: `"User verification status updated to {STATUS}"`
- Activate/deactivate: `"User activated/deactivated successfully"`
- Stats:

```json
{ "totalUsers": 100, "verifiedUsers": 80, "pendingUsers": 20 }
```

## Uploads (Cloudinary)

### Upload image (file)

```http
POST /api/upload/image
Content-Type: multipart/form-data
Form: file=<image>, folder=uploads?
```

Response (ImageData):

```json
{
  "url": "https://.../q_auto/f_avif/...",
  "publicId": "folder/uuid",
  "width": 800,
  "height": 600
}
```

### Upload image (base64)

```http
POST /api/upload/image/base64
Content-Type: application/json

{ "image": "data:image/jpeg;base64,...", "folder": "uploads" }
```

Response: ImageData (see above).

### Delete image

```http
DELETE /api/upload/image/{publicId}
```

Response:

```json
{ "message": "Image deleted successfully" }
```

## Health

```http
GET /api/health
```

Response:

```json
{
  "status": "UP",
  "timestamp": "2025-01-01T12:34:56",
  "service": "CampusConnect API",
  "version": "1.0.0"
}
```
