# CampusConnect Multipart Signup API

## Overview

The CampusConnect API now supports multipart form data for user signup, allowing direct file uploads for student ID images and selfie images instead of base64 encoding.

## Endpoints

### Multipart Signup Endpoint

```http
POST /api/auth/signup
Content-Type: multipart/form-data
```

#### Form Data Parameters:

| Parameter        | Type   | Required | Description                                      |
| ---------------- | ------ | -------- | ------------------------------------------------ |
| `firstName`      | string | Yes      | User's first name                                |
| `lastName`       | string | Yes      | User's last name                                 |
| `email`          | string | Yes      | KNUST student email (@st.knust.edu.gh)           |
| `password`       | string | Yes      | Password (min 8 characters)                      |
| `studentId`      | string | Yes      | KNUST student ID                                 |
| `phoneNumber`    | string | Yes      | Ghana phone number (+233XXXXXXXXX or 0XXXXXXXXX) |
| `studentIdImage` | file   | No       | Student ID card image file                       |
| `selfieImage`    | file   | No       | Selfie image file                                |

#### Validation Rules:

- **Email**: Must be a valid KNUST student email ending with `@st.knust.edu.gh`
- **Password**: Minimum 8 characters
- **Phone Number**: Must match Ghana phone number format: `^(\+233|0)[0-9]{9}$`
- **Image Files**: Must be valid image files (JPEG, PNG, etc.)
- **File Size**: Maximum 10MB per file

#### Response:

```json
{
  "message": "User created successfully. Please verify your phone number.",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid-string",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@st.knust.edu.gh",
    "studentId": "20123456",
    "phoneNumber": "+233201234567",
    "phoneVerified": false,
    "studentIdValidated": false,
    "verificationStatus": "PENDING_VERIFICATION",
    "studentIdImage": {
      "url": "https://res.cloudinary.com/demo/image/upload/q_auto/f_avif/v123456789/student-ids/uuid.jpg",
      "publicId": "student-ids/uuid"
    },
    "selfieImage": {
      "url": "https://res.cloudinary.com/demo/image/upload/q_auto/f_avif/v123456789/selfies/uuid.jpg",
      "publicId": "selfies/uuid"
    },
    "rating": 5.0,
    "totalDeliveries": 0,
    "joinedDate": "2024-01-15T10:30:00Z"
  }
}
```

## Usage Examples

### HTML Form Example:

```html
<form action="/api/auth/signup" method="post" enctype="multipart/form-data">
  <input type="text" name="firstName" placeholder="First Name" required />
  <input type="text" name="lastName" placeholder="Last Name" required />
  <input
    type="email"
    name="email"
    placeholder="Email (@st.knust.edu.gh)"
    required
  />
  <input
    type="password"
    name="password"
    placeholder="Password (min 8 chars)"
    required
  />
  <input type="text" name="studentId" placeholder="Student ID" required />
  <input type="tel" name="phoneNumber" placeholder="Phone Number" required />

  <div class="file-group">
    <label for="studentIdImage">Student ID Image (Optional):</label>
    <input type="file" name="studentIdImage" accept="image/*" />
  </div>

  <div class="file-group">
    <label for="selfieImage">Selfie Image (Optional):</label>
    <input type="file" name="selfieImage" accept="image/*" />
  </div>

  <button type="submit">Sign Up</button>
</form>
```

### JavaScript Fetch Example:

```html
<!-- Add these elements to your HTML -->
<div id="signup-form">
  <form id="signupForm" enctype="multipart/form-data">
    <div class="form-group">
      <input
        type="text"
        id="firstName"
        name="firstName"
        placeholder="First Name"
        required
      />
      <span class="error-message" id="firstName-error"></span>
    </div>

    <div class="form-group">
      <input
        type="email"
        id="email"
        name="email"
        placeholder="Email (@st.knust.edu.gh)"
        required
      />
      <span class="error-message" id="email-error"></span>
    </div>

    <div class="form-group">
      <input
        type="password"
        id="password"
        name="password"
        placeholder="Password (min 8 chars)"
        required
      />
      <span class="error-message" id="password-error"></span>
    </div>

    <div class="form-group">
      <input
        type="file"
        id="studentIdImage"
        name="studentIdImage"
        accept="image/*"
      />
      <label for="studentIdImage">Upload Student ID Image</label>
      <span class="error-message" id="studentId-error"></span>
    </div>

    <div class="form-group">
      <input type="file" id="selfieImage" name="selfieImage" accept="image/*" />
      <label for="selfieImage">Upload Selfie Image</label>
      <span class="error-message" id="selfie-error"></span>
    </div>

    <button type="submit" id="submitBtn">
      <span class="btn-text">Sign Up</span>
      <span class="loading-spinner" style="display: none;">⏳</span>
    </button>
  </form>
</div>

<!-- Success/Error Messages -->
<div id="success-message" class="alert alert-success" style="display: none;">
  <h3>✅ Account Created Successfully!</h3>
  <p>
    Please verify your phone number. Check your phone for the verification code.
  </p>
  <button onclick="redirectToVerification()">Continue to Verification</button>
</div>

<div id="error-message" class="alert alert-error" style="display: none;">
  <h3>❌ Signup Failed</h3>
  <p id="error-text"></p>
  <button onclick="hideError()">Try Again</button>
</div>
```

```javascript
// Enhanced JavaScript with UI feedback and actions
document
  .getElementById("signupForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    // Clear previous errors
    clearErrors();

    // Get form data
    const formData = new FormData();
    const form = e.target;

    // Add text fields
    formData.append("firstName", form.firstName.value.trim());
    formData.append("lastName", form.lastName.value.trim());
    formData.append("email", form.email.value.trim());
    formData.append("password", form.password.value);
    formData.append("studentId", form.studentId.value.trim());
    formData.append("phoneNumber", form.phoneNumber.value.trim());

    // Add files if selected
    const studentIdFile = form.studentIdImage.files[0];
    const selfieFile = form.selfieImage.files[0];

    if (studentIdFile) {
      // Validate file size (10MB limit)
      if (studentIdFile.size > 10 * 1024 * 1024) {
        showFieldError(
          "studentId-error",
          "Student ID image must be less than 10MB"
        );
        return;
      }
      formData.append("studentIdImage", studentIdFile);
    }

    if (selfieFile) {
      if (selfieFile.size > 10 * 1024 * 1024) {
        showFieldError("selfie-error", "Selfie image must be less than 10MB");
        return;
      }
      formData.append("selfieImage", selfieFile);
    }

    // Show loading state
    showLoadingState();

    try {
      const response = await fetch("/api/auth/signup", {
        method: "POST",
        body: formData,
      });

      const result = await response.json();

      if (response.ok) {
        // Success! Store token and show success message
        localStorage.setItem("authToken", result.token);
        localStorage.setItem("userEmail", result.user.email);
        localStorage.setItem("phoneNumber", result.user.phoneNumber);

        showSuccessMessage(result.message, result.user);

        // Auto-redirect after 3 seconds
        setTimeout(() => {
          redirectToVerification();
        }, 3000);
      } else {
        // Handle different types of errors
        handleErrorResponse(result);
      }
    } catch (error) {
      // Network error
      showErrorMessage(
        "Network Error",
        "Unable to connect to the server. Please check your internet connection and try again."
      );
    } finally {
      hideLoadingState();
    }
  });

// UI Helper Functions
function showLoadingState() {
  const submitBtn = document.getElementById("submitBtn");
  const btnText = submitBtn.querySelector(".btn-text");
  const spinner = submitBtn.querySelector(".loading-spinner");

  submitBtn.disabled = true;
  btnText.textContent = "Creating Account...";
  spinner.style.display = "inline";
}

function hideLoadingState() {
  const submitBtn = document.getElementById("submitBtn");
  const btnText = submitBtn.querySelector(".btn-text");
  const spinner = submitBtn.querySelector(".loading-spinner");

  submitBtn.disabled = false;
  btnText.textContent = "Sign Up";
  spinner.style.display = "none";
}

function showSuccessMessage(message, user) {
  // Hide form and show success message
  document.getElementById("signup-form").style.display = "none";
  document.getElementById("error-message").style.display = "none";

  const successDiv = document.getElementById("success-message");
  successDiv.style.display = "block";

  // Update success message with user details
  successDiv.querySelector(
    "p"
  ).innerHTML = `Welcome ${user.firstName}! ${message}<br>
     <strong>Next Steps:</strong><br>
     1. Verify your phone number: ${user.phoneNumber}<br>
     2. Check your phone for the verification code<br>
     3. Complete your profile verification`;

  // Scroll to success message
  successDiv.scrollIntoView({ behavior: "smooth" });
}

function showErrorMessage(title, message) {
  document.getElementById("success-message").style.display = "none";

  const errorDiv = document.getElementById("error-message");
  errorDiv.querySelector("h3").textContent = title;
  errorDiv.querySelector("#error-text").textContent = message;
  errorDiv.style.display = "block";

  // Scroll to error message
  errorDiv.scrollIntoView({ behavior: "smooth" });
}

function handleErrorResponse(result) {
  switch (result.errorCode) {
    case "VALIDATION_ERROR":
      showErrorMessage("❌ Validation Error", result.message);
      // Highlight specific fields if validation details are provided
      break;

    case "CONFLICT":
      if (result.message.includes("email")) {
        showFieldError(
          "email-error",
          "This email is already registered. Try signing in instead."
        );
        showErrorMessage(
          "❌ Account Already Exists",
          "An account with this email already exists. Would you like to sign in instead?"
        );
        addSignInLink();
      } else {
        showErrorMessage("❌ Account Already Exists", result.message);
      }
      break;

    case "UNEXPECTED_ERROR":
      if (result.message.includes("upload")) {
        showErrorMessage(
          "❌ Upload Failed",
          "Failed to upload your images. Please check your files and try again."
        );
      } else {
        showErrorMessage("❌ Server Error", result.message);
      }
      break;

    default:
      showErrorMessage(
        "❌ Signup Failed",
        result.message || "An unexpected error occurred."
      );
  }
}

function showFieldError(fieldId, message) {
  const errorElement = document.getElementById(fieldId);
  if (errorElement) {
    errorElement.textContent = message;
    errorElement.style.display = "block";
    errorElement.style.color = "#dc3545";
  }
}

function clearErrors() {
  const errorElements = document.querySelectorAll(".error-message");
  errorElements.forEach((element) => {
    element.textContent = "";
    element.style.display = "none";
  });
}

function hideError() {
  document.getElementById("error-message").style.display = "none";
  document.getElementById("signup-form").style.display = "block";
}

function addSignInLink() {
  const errorDiv = document.getElementById("error-message");
  const existingLink = errorDiv.querySelector(".signin-link");

  if (!existingLink) {
    const signInLink = document.createElement("a");
    signInLink.href = "/signin";
    signInLink.textContent = "Sign In Instead";
    signInLink.className = "signin-link btn-secondary";
    signInLink.style.marginLeft = "10px";

    errorDiv.querySelector("button").parentNode.appendChild(signInLink);
  }
}

function redirectToVerification() {
  // Store current user state
  localStorage.setItem("signupComplete", "true");

  // Redirect to phone verification page
  window.location.href = "/verify-phone";
}

// File preview functionality (bonus feature)
document
  .getElementById("studentIdImage")
  .addEventListener("change", function (e) {
    previewImage(e.target, "student-id-preview");
  });

document.getElementById("selfieImage").addEventListener("change", function (e) {
  previewImage(e.target, "selfie-preview");
});

function previewImage(input, previewId) {
  if (input.files && input.files[0]) {
    const reader = new FileReader();

    reader.onload = function (e) {
      let preview = document.getElementById(previewId);
      if (!preview) {
        preview = document.createElement("img");
        preview.id = previewId;
        preview.style.maxWidth = "200px";
        preview.style.maxHeight = "200px";
        preview.style.marginTop = "10px";
        preview.style.borderRadius = "8px";
        input.parentNode.appendChild(preview);
      }
      preview.src = e.target.result;
    };

    reader.readAsDataURL(input.files[0]);
  }
}
```

```css
/* Add these CSS styles for better UI */
.form-group {
  margin-bottom: 20px;
}

.error-message {
  display: block;
  margin-top: 5px;
  font-size: 14px;
  color: #dc3545;
}

.alert {
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
}

.alert-success {
  background-color: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.alert-error {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.loading-spinner {
  margin-left: 5px;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
  padding: 10px 15px;
  text-decoration: none;
  border-radius: 4px;
  display: inline-block;
}
```

### cURL Example:

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: multipart/form-data" \
  -F "firstName=John" \
  -F "lastName=Doe" \
  -F "email=john.doe@st.knust.edu.gh" \
  -F "password=securepassword123" \
  -F "studentId=20123456" \
  -F "phoneNumber=+233201234567" \
  -F "studentIdImage=@/path/to/student-id.jpg" \
  -F "selfieImage=@/path/to/selfie.jpg"
```

## Benefits of Multipart Upload

1. **Efficiency**: Direct file upload is more efficient than base64 encoding
2. **File Size**: No 33% size increase from base64 encoding
3. **Browser Support**: Native browser file upload support
4. **User Experience**: Standard file selection UI
5. **Validation**: Server-side file type and size validation

## Important Note

The `/api/auth/signup` endpoint now exclusively accepts multipart form data. The previous JSON-based signup with base64 encoded images has been replaced with this more efficient multipart implementation.

## Error Responses

### Validation Errors (400 Bad Request):

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "statusCode": 400,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### File Upload Errors (500 Internal Server Error):

```json
{
  "errorCode": "UNEXPECTED_ERROR",
  "message": "Failed to upload student ID image: Network error",
  "statusCode": 500,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Duplicate User (409 Conflict):

```json
{
  "errorCode": "CONFLICT",
  "message": "User with this email already exists",
  "statusCode": 409,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Security Notes

- All uploaded images are validated for file type and size
- Images are stored securely on Cloudinary with auto-optimization
- Phone verification is required after signup
- JWT token is provided for immediate authentication
