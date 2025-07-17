# Keep-Alive Service Documentation

## Overview

The Keep-Alive Service is designed to prevent the Render.com application from going to sleep due to inactivity. Free-tier cloud services often have a sleep timeout (typically 15 minutes) after which the service becomes inactive and needs to be "woken up" by an incoming request.

## How It Works

The service automatically makes HTTP requests to the application's health endpoints every 14 minutes (before the 15-minute timeout) to keep the service active and responsive.

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `KEEP_ALIVE_ENABLED` | `true` | Enable/disable the keep-alive service |
| `KEEP_ALIVE_URL` | Auto-detected | The URL to ping (auto-detects Render.com URL) |
| `KEEP_ALIVE_INTERVAL` | `840000` | Ping interval in milliseconds (14 minutes) |

### Application Properties

```properties
# Keep-Alive Service Configuration
app.keep-alive.enabled=${KEEP_ALIVE_ENABLED:true}
app.keep-alive.url=${KEEP_ALIVE_URL:}
app.keep-alive.interval=${KEEP_ALIVE_INTERVAL:840000}
```

## Endpoints

### Health Check Endpoints

- `GET /api/health` - Basic health check
- `GET /api/health/detailed` - Detailed health check with system info
- `GET /api/health/alive` - Simple alive response
- `GET /api/health/ping` - Manual trigger for keep-alive ping

### Example Responses

#### Basic Health Check
```json
{
  "status": "UP",
  "timestamp": "2025-07-17T10:30:00",
  "service": "Sales Management Backend",
  "version": "1.0.0"
}
```

#### Manual Ping Trigger
```json
{
  "status": "SUCCESS",
  "message": "Manual keep-alive ping triggered",
  "timestamp": "2025-07-17T10:30:00"
}
```

## Features

### Automatic URL Detection
- Automatically detects Render.com deployment using `RENDER_EXTERNAL_URL` environment variable
- Falls back to localhost for development environments

### Smart Endpoint Selection
- Tries `/actuator/health` endpoint first (if Spring Actuator is available)
- Falls back to `/api/auth/test` endpoint
- Uses custom `/api/health` endpoint as final fallback

### Error Handling
- Graceful error handling prevents scheduler from stopping
- Detailed logging for monitoring and debugging
- Non-blocking async execution

### Manual Control
- Manual ping trigger via API endpoint
- Service status monitoring
- Enable/disable functionality

## Deployment on Render.com

### Automatic Configuration
The service automatically configures itself when deployed on Render.com:

1. Detects the `RENDER_EXTERNAL_URL` environment variable
2. Sets up the keep-alive URL automatically
3. Starts pinging every 14 minutes

### Manual Configuration
You can override the automatic configuration by setting environment variables in your Render.com service:

```bash
KEEP_ALIVE_ENABLED=true
KEEP_ALIVE_URL=https://your-app-name.onrender.com
KEEP_ALIVE_INTERVAL=840000
```

## Monitoring

### Logs
The service provides detailed logging:

```
2025-07-17 10:30:00 [KeepAlive-1] INFO  KeepAliveService - Keep-alive ping successful to https://your-app.onrender.com/api/health at 2025-07-17 10:30:00 - Status: 200 OK
```

### Status Check
Get the current status of the keep-alive service:

```bash
curl https://your-app.onrender.com/api/health/detailed
```

## Development

### Local Development
For local development, the service can be disabled or configured to use localhost:

```properties
app.keep-alive.enabled=false
# OR
app.keep-alive.url=http://localhost:8080
```

### Testing
Trigger a manual ping for testing:

```bash
curl https://your-app.onrender.com/api/health/ping
```

## Benefits

1. **Prevents Sleep**: Keeps your Render.com service active 24/7
2. **Improved Response Time**: No cold start delays for users
3. **Better User Experience**: Consistent performance
4. **Automatic**: No manual intervention required
5. **Configurable**: Can be customized or disabled as needed

## Considerations

- Uses minimal resources (small HTTP requests every 14 minutes)
- Does not affect your application's core functionality
- Can be disabled for production environments with paid hosting
- Logs can be monitored to ensure proper operation

## Troubleshooting

### Service Not Pinging
1. Check if `KEEP_ALIVE_ENABLED` is set to `true`
2. Verify the `KEEP_ALIVE_URL` is correct
3. Check application logs for error messages
4. Ensure the health endpoints are accessible

### Manual Testing
```bash
# Test health endpoint
curl https://your-app.onrender.com/api/health

# Trigger manual ping
curl https://your-app.onrender.com/api/health/ping

# Check detailed status
curl https://your-app.onrender.com/api/health/detailed
```
