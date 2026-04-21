import requests
import json

API_KEY = "AIzaSyDIQGtQusevkOEHoHEs3JwoIp-30-e-f5Q"
URL = f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={API_KEY}"

credentials = [
  {"name": "Admin User", "email": "admin@mindmatrix.com", "password": "admin123", "role": "ADMIN"},
  {"name": "Sarah Chen", "email": "lead@mindmatrix.com", "password": "lead123", "role": "LEAD"},
  {"name": "John Doe", "email": "employee@mindmatrix.com", "password": "employee123", "role": "EMPLOYEE"},
  {"name": "Michael Brown", "email": "michael.brown@mindmatrix.com", "password": "password123", "role": "LEAD"},
  {"name": "Jane Smith", "email": "jane.smith@mindmatrix.com", "password": "password123", "role": "EMPLOYEE"},
  {"name": "Alex Wilson", "email": "alex.wilson@mindmatrix.com", "password": "password123", "role": "EMPLOYEE"},
  {"name": "Emily Davis", "email": "emily.davis@mindmatrix.com", "password": "password123", "role": "EMPLOYEE"},
  {"name": "Robert Lee", "email": "robert.lee@mindmatrix.com", "password": "password123", "role": "EMPLOYEE"},
  {"name": "Admin User (Case Insensitive Test)", "email": "Admin@MindMatrix.com", "password": "admin123", "role": "ADMIN"}
]

print("Testing credentials against Firebase Auth...\n")

for cred in credentials:
    payload = {
        "email": cred["email"],
        "password": cred["password"],
        "returnSecureToken": True
    }
    response = requests.post(URL, json=payload)
    if response.status_code == 200:
        print(f"✅ SUCCESS: {cred['name']} ({cred['email']}) - Role: {cred['role']}")
    else:
        try:
            error_message = response.json().get('error', {}).get('message', 'Unknown Error')
        except:
            error_message = response.text
        print(f"❌ FAILED:  {cred['name']} ({cred['email']}) - Role: {cred['role']}")
        print(f"   Reason: {error_message}")
