import requests
import json
import sys

API_KEY = "AIzaSyDIQGtQusevkOEHoHEs3JwoIp-30-e-f5Q"
SIGNUP_URL = f"https://identitytoolkit.googleapis.com/v1/accounts:signUp?key={API_KEY}"
LOGIN_URL = f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={API_KEY}"

credentials = [
  {"name": "Admin User", "email": "admin@mindmatrix.com", "password": "admin123", "role": "ADMIN"},
  {"name": "Sarah Chen", "email": "lead@mindmatrix.com", "password": "lead123", "role": "LEAD"},
  {"name": "John Doe", "email": "employee@mindmatrix.com", "password": "employee123", "role": "EMPLOYEE"},
  {"name": "Michael Brown", "email": "michael.brown@mindmatrix.com", "password": "password123", "role": "LEAD"},
  {"name": "Jane Smith", "email": "jane.smith@mindmatrix.com", "password": "password123", "role": "EMPLOYEE"},
  {"name": "Alex Wilson", "email": "alex.wilson@mindmatrix.com", "password": "password123", "role": "EMPLOYEE"},
  {"name": "Emily Davis", "email": "emily.davis@mindmatrix.com", "password": "password123", "role": "EMPLOYEE"},
  {"name": "Robert Lee", "email": "robert.lee@mindmatrix.com", "password": "password123", "role": "EMPLOYEE"}
]

print("Seeding and testing credentials against Firebase Auth...\n")

for cred in credentials:
    payload = {
        "email": cred["email"],
        "password": cred["password"],
        "returnSecureToken": True
    }
    
    # Try logging in first
    login_response = requests.post(LOGIN_URL, json=payload)
    if login_response.status_code == 200:
        print(f"✅ ALREADY EXISTS & SUCCESS: {cred['name']} ({cred['email']})")
    else:
        # If login fails, try to sign up
        signup_response = requests.post(SIGNUP_URL, json=payload)
        if signup_response.status_code == 200:
            print(f"✅ CREATED & SUCCESS: {cred['name']} ({cred['email']})")
            
            # Now verify sign-in works
            verify_response = requests.post(LOGIN_URL, json=payload)
            if verify_response.status_code == 200:
                print(f"   -> Login Verified")
            else:
                print(f"   -> Login Verification FAILED")
        else:
            try:
                error_message = signup_response.json().get('error', {}).get('message', 'Unknown Error')
            except:
                error_message = signup_response.text
            print(f"❌ FAILED TO CREATE: {cred['name']} ({cred['email']})")
            print(f"   Reason: {error_message}")
