Add-Type -AssemblyName System.Drawing

# Create a 32x32 bitmap
$bmp = New-Object System.Drawing.Bitmap(32, 32)
$g = [System.Drawing.Graphics]::FromImage($bmp)

# Fill with blue background
$g.FillRectangle([System.Drawing.Brushes]::Blue, 0, 0, 32, 32)

# Draw white circle
$g.FillEllipse([System.Drawing.Brushes]::White, 8, 8, 16, 16)

# Draw "S" for Sales
$font = New-Object System.Drawing.Font("Arial", 10, [System.Drawing.FontStyle]::Bold)
$g.DrawString("S", $font, [System.Drawing.Brushes]::Blue, 11, 9)

# Clean up
$g.Dispose()
$font.Dispose()

# Save as BMP first
$bmp.Save("installer\assets\app-icon.bmp", [System.Drawing.Imaging.ImageFormat]::Bmp)

# Try to create ICO format (basic)
$ico = New-Object System.Drawing.Icon("installer\assets\app-icon.bmp")
$ico.Save("installer\assets\app-icon.ico")
$ico.Dispose()

$bmp.Dispose()

Write-Host "Icon created successfully!"
