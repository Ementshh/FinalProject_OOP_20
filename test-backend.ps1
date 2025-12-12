# Backend Health Check Script
# Run this before starting the frontend to ensure backend is ready

Write-Host "`n=== Backend Health Check ===" -ForegroundColor Cyan
Write-Host "Testing connection to http://localhost:8080/api/players/login`n" -ForegroundColor Yellow

$body = '{"username":"healthcheck"}'

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/players/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 5
    
    Write-Host "SUCCESS - Backend is RUNNING!" -ForegroundColor Green
    Write-Host "`nResponse:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 5
    Write-Host "`nDatabase connection: OK" -ForegroundColor Green
    Write-Host "Player API: OK" -ForegroundColor Green
    Write-Host "`nYou can now start the Frontend!" -ForegroundColor Green
    
} catch {
    Write-Host "ERROR - Backend is NOT running or not ready!" -ForegroundColor Red
    Write-Host "`nError: $($_.Exception.Message)" -ForegroundColor Red
    
    Write-Host "`n--- Troubleshooting Steps ---" -ForegroundColor Yellow
    Write-Host "1. Make sure backend is started:" -ForegroundColor White
    Write-Host "   cd Backend" -ForegroundColor Gray
    Write-Host "   ./gradlew bootRun" -ForegroundColor Gray
    
    Write-Host "`n2. Wait for this message in backend log:" -ForegroundColor White
    Write-Host "   Started BackendApplication in X.XXX seconds" -ForegroundColor Gray
    
    Write-Host "`n3. Check if port 8080 is available:" -ForegroundColor White
    Write-Host "   netstat -ano | findstr :8080" -ForegroundColor Gray
    
    Write-Host "`n4. Then run this test again" -ForegroundColor White
    Write-Host ""
}
