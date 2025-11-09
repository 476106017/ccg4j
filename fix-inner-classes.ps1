$ErrorActionPreference = "Continue"

# 从编译错误中提取需要修复的文件和内部类
$errorOutput = & .\gradlew.bat compileJava 2>&1 | Out-String

# 提取所有错误
$pattern = '([^\\]+\.java):\d+: 错误: (\w+)不是抽象的, 并且未覆盖Card中的抽象方法getRarity'
$matches = [regex]::Matches($errorOutput, $pattern)

$fileClassMap = @{}
foreach ($match in $matches) {
    $fileName = $match.Groups[1].Value
    $className = $match.Groups[2].Value
    
    if (-not $fileClassMap.ContainsKey($fileName)) {
        $fileClassMap[$fileName] = @()
    }
    if ($fileClassMap[$fileName] -notcontains $className) {
        $fileClassMap[$fileName] += $className
    }
}

Write-Host "Found $($fileClassMap.Count) files with missing rarity in inner classes"

$fixed = 0
foreach ($fileName in $fileClassMap.Keys) {
    # 查找文件
    $files = Get-ChildItem -Path "src\main\java" -Recurse -Filter $fileName
    if ($files.Count -eq 0) {
        Write-Host "File not found: $fileName"
        continue
    }
    
    $file = $files[0].FullName
    Write-Host "`nProcessing: $fileName"
    
    $content = Get-Content $file -Raw -Encoding UTF8
    $originalContent = $content
    
    foreach ($className in $fileClassMap[$fileName]) {
        Write-Host "  Adding rarity to class: $className"
        
        # 匹配内部类定义并添加rarity
        # 模式: public/private/protected (static) class ClassName {
        $classPattern = "(\s+)(public|private|protected)\s+(static\s+)?class\s+$className\s+[^{]*\{\s*"
        if ($content -match $classPattern) {
            $indent = $matches[1] + "    "
            $replacement = $matches[0] + "`r`n$indent`private CardRarity rarity = CardRarity.BRONZE;"
            $content = $content -replace $classPattern, $replacement
        }
    }
    
    if ($content -ne $originalContent) {
        [System.IO.File]::WriteAllText($file, $content, (New-Object System.Text.UTF8Encoding $false))
        Write-Host "  ✓ Fixed"
        $fixed++
    } else {
        Write-Host "  ✗ No changes made"
    }
}

Write-Host "`n================================"
Write-Host "Total files fixed: $fixed"
Write-Host "Recompiling..."

& .\gradlew.bat compileJava 2>&1 | Select-String "BUILD" | Select-Object -Last 2
