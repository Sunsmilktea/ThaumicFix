# 移除Java文件中的__OBFID行
$javaFiles = Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse

foreach ($file in $javaFiles) {
    $filePath = $file.FullName
    
    # 读取文件内容
    $content = Get-Content -Path $filePath -Encoding UTF8 -Raw
    
    # 移除__OBFID行
    $pattern = '\s*private\s+static\s+final\s+String\s+__OBFID\s*=\s*"[^"]*"\s*;\s*'
    $content = $content -replace $pattern, ''
    
    # 写回文件
    Set-Content -Path $filePath -Value $content -Encoding UTF8
    
    Write-Host "Processed: $filePath"
}

Write-Host "OBFID removal completed!"
