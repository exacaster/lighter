# Configuring Sparkmagic

Lighter works as a replacement for Livy, when you are using it with Sparkmagic.
Make those changes to Sparkmagic configuration json file, to make it work with lighter:

```json
{
  "kernel_python_credentials" : {
      "username": "",
      "password": "",
      "url": "http://lighter.spark:8080/lighter/api",
      "auth": "None"
  },
  "livy_session_startup_timeout_seconds": 600,
  "custom_headers": {
    "X-Compatibility-Mode": "sparkmagic"
  }
}
```
